#!/usr/bin/env python3
"""Verify that the tracked content manifest matches bundled content assets."""
from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "docs/content/content_manifest.json"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def bundled_paths() -> set[str]:
    paths = [
        *ROOT.glob("feature/feature-hadith/src/main/assets/**/*"),
        *ROOT.glob("feature/feature-quran/src/main/assets/*"),
        *ROOT.glob("feature/feature-prayer-times/src/main/res/raw/*"),
    ]
    return {path.relative_to(ROOT).as_posix() for path in paths if path.is_file()}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--production",
        action="store_true",
        help="Fail unless every bundled asset is approved for production.",
    )
    args = parser.parse_args()
    failures: list[str] = []
    if not MANIFEST.exists():
        print(f"Missing manifest: {MANIFEST.relative_to(ROOT)}")
        return 1

    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    assets = manifest.get("assets")
    if not isinstance(assets, list):
        print("Manifest does not contain an assets array")
        return 1

    by_path = {asset.get("path"): asset for asset in assets if isinstance(asset, dict)}
    actual_paths = bundled_paths()
    manifest_paths = set(by_path)
    for path in sorted(actual_paths - manifest_paths):
        failures.append(f"Bundled asset missing from manifest: {path}")
    for path in sorted(manifest_paths - actual_paths):
        failures.append(f"Manifest references missing asset: {path}")

    for path in sorted(actual_paths & manifest_paths):
        asset = by_path[path]
        actual_hash = sha256(ROOT / path)
        if asset.get("sha256") != actual_hash:
            failures.append(f"SHA-256 mismatch: {path}")
        status = asset.get("production_status")
        if status == "approved":
            for field in ("source", "licence", "religious_review"):
                if not asset.get(field):
                    failures.append(f"Approved asset lacks {field}: {path}")
        if asset.get("development_only") and status == "approved":
            failures.append(f"Development sample marked approved: {path}")
        if args.production and status != "approved":
            failures.append(f"Production-blocked asset: {path} ({status})")

    if failures:
        print("CONTENT MANIFEST CHECK FAILED")
        print("\n".join(f"- {failure}" for failure in failures))
        return 1
    print(f"CONTENT MANIFEST CHECK PASSED ({len(actual_paths)} assets)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
