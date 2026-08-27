#!/usr/bin/env python3
"""Generate the tracked inventory of bundled religious and audio content.

The generated manifest is deterministic. Human source, licence, and religious
review decisions live in docs/content/content_approvals.json and are merged into
it, so regenerating hashes never erases an approval decision.
"""
from __future__ import annotations

import gzip
import hashlib
import json
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "docs/content/content_manifest.json"
APPROVALS = ROOT / "docs/content/content_approvals.json"
REQUIRED_APPROVAL_FIELDS = ("source", "licence", "religious_review")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def count_ndjson_gzip(path: Path) -> int:
    with gzip.open(path, "rt", encoding="utf-8") as stream:
        return sum(1 for line in stream if line.strip())


def content_kind(path: Path) -> str:
    normalized = path.as_posix()
    if "/feature-hadith/" in normalized:
        return "hadith_corpus"
    if "/feature-quran/" in normalized:
        return "quran_content"
    if "/feature-prayer-times/" in normalized:
        return "adhan_audio"
    raise ValueError(f"Unsupported content path: {path}")


def content_id(path: Path) -> str:
    return path.relative_to(ROOT).as_posix().replace("/", ".").replace(".", "_")


def load_approvals() -> dict[str, dict[str, Any]]:
    if not APPROVALS.exists():
        return {}
    parsed = json.loads(APPROVALS.read_text(encoding="utf-8"))
    approvals = parsed.get("approvals")
    if not isinstance(approvals, dict):
        raise ValueError("content_approvals.json must contain an approvals object")
    return approvals


def is_development_sample(path: Path, approval: dict[str, Any]) -> bool:
    """Treat sample-named assets as blocked unless the owner explicitly promotes them."""
    return "sample" in path.name.lower() and not approval.get("production_pack_confirmed")


def production_status(path: Path, approval: dict[str, Any]) -> tuple[str, list[str]]:
    missing = [field for field in REQUIRED_APPROVAL_FIELDS if not approval.get(field)]
    if is_development_sample(path, approval):
        return "blocked_development_sample", [
            "Replace this development-only sample with an approved production pack or set production_pack_confirmed with a documented owner approval.",
            *[f"Provide {field}." for field in missing],
        ]
    if missing:
        return "blocked_pending_review", [f"Provide {field}." for field in missing]
    return "approved", []


def asset_record(path: Path, approval: dict[str, Any]) -> dict[str, object]:
    relative = path.relative_to(ROOT).as_posix()
    status, actions = production_status(path, approval)
    record: dict[str, object] = {
        "id": content_id(path),
        "kind": content_kind(path),
        "path": relative,
        "size_bytes": path.stat().st_size,
        "sha256": sha256(path),
        "production_status": status,
        "source": approval.get("source"),
        "licence": approval.get("licence"),
        "attribution": approval.get("attribution"),
        "religious_review": approval.get("religious_review"),
        "notes": approval.get("notes"),
        "required_actions": actions,
    }
    if path.name.endswith(".ndjson.gz"):
        record["record_count"] = count_ndjson_gzip(path)
        record["declared_collections"] = approval.get(
            "declared_collections",
            "Must be verified from the import provenance before release.",
        )
    if is_development_sample(path, approval):
        record["development_only"] = True
    elif "sample" in path.name.lower():
        record["production_pack_confirmed"] = True
    return record


def main() -> None:
    approvals = load_approvals()
    paths = sorted(
        [
            *ROOT.glob("feature/feature-hadith/src/main/assets/**/*"),
            *ROOT.glob("feature/feature-quran/src/main/assets/*"),
            *ROOT.glob("feature/feature-prayer-times/src/main/res/raw/*"),
        ]
    )
    known_ids = {content_id(path) for path in paths if path.is_file()}
    orphaned = sorted(set(approvals) - known_ids)
    if orphaned:
        raise ValueError(f"Approvals reference missing assets: {', '.join(orphaned)}")
    assets = [asset_record(path, approvals.get(content_id(path), {})) for path in paths if path.is_file()]
    manifest = {
        "schema_version": 2,
        "generated_from": "scripts/generate_content_manifest.py",
        "approvals_file": "docs/content/content_approvals.json",
        "release_policy": {
            "rule": "Only assets with production_status=approved may ship in a production release.",
            "approval_requirements": list(REQUIRED_APPROVAL_FIELDS),
        },
        "assets": assets,
        "summary": {
            "asset_count": len(assets),
            "approved_for_production": sum(
                1 for asset in assets if asset["production_status"] == "approved"
            ),
            "blocked_pending_review": sum(
                1 for asset in assets if asset["production_status"] != "approved"
            ),
        },
    }
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {len(assets)} assets to {OUTPUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
