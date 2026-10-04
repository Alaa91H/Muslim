#!/usr/bin/env python3
"""Strict catalogue and coverage gate for Quran meaning and tafsir packs."""
from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
import re
import sys
from urllib.parse import urlparse
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "docs/quran/text_packs/catalog.json"
AYAH_COUNT = 6236
MEANING = "meaning_translation"
TAFSIR = "tafsir_translation"
TAFSIR_ORIGINAL = "tafsir_original"


def supported_languages() -> list[str]:
    spec = importlib.util.spec_from_file_location("muslim_localize", ROOT / "scripts/localize.py")
    if spec is None or spec.loader is None:
        raise RuntimeError("Cannot load scripts/localize.py supported locale catalogue")
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return sorted(set(module.supported_locale_catalog()) | {"ar", "en"})


def required_kinds(language: str) -> set[str]:
    if language == "ar":
        return {TAFSIR_ORIGINAL}
    return {MEANING, TAFSIR}


def validate_pack(relative_path: str, expected_kind: str, expected_language: str) -> tuple[dict[str, Any] | None, list[str]]:
    errors: list[str] = []
    path = (ROOT / relative_path).resolve()
    if ROOT not in path.parents or not path.is_file():
        return None, [f"{relative_path}: missing or outside repository"]
    try:
        pack = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        return None, [f"{relative_path}: invalid JSON: {error}"]
    if pack.get("schemaVersion") != 2:
        errors.append(f"{relative_path}: unsupported schemaVersion")
    manifest = pack.get("manifest")
    entries = pack.get("entries")
    if not isinstance(manifest, dict) or not isinstance(entries, list):
        return manifest if isinstance(manifest, dict) else None, errors + [f"{relative_path}: malformed pack"]
    if manifest.get("kind") != expected_kind or manifest.get("languageTag") != expected_language:
        errors.append(f"{relative_path}: catalog metadata does not match manifest")
    for field in (
        "id", "title", "work", "translator", "publisher", "sourceUrl", "license", "version",
        "reviewer", "reviewReference", "sourceAttribution",
    ):
        if not isinstance(manifest.get(field), str) or not manifest[field].strip():
            errors.append(f"{relative_path}: missing provenance field {field}")
    source_url = urlparse(str(manifest.get("sourceUrl", "")))
    review_url = urlparse(str(manifest.get("reviewReference", "")))
    if source_url.scheme != "https" or not source_url.hostname:
        errors.append(f"{relative_path}: sourceUrl must use HTTPS")
    if review_url.scheme != "https" or not review_url.hostname:
        errors.append(f"{relative_path}: reviewReference must use HTTPS")
    if source_url.hostname and review_url.hostname and source_url.hostname.lower() != review_url.hostname.lower():
        errors.append(f"{relative_path}: source and review evidence must share a publisher domain")
    if manifest.get("reviewStatus") not in {"source_reviewed", "editor_reviewed"}:
        errors.append(f"{relative_path}: content review is not declared")
    if manifest.get("expectedAyahCount") != AYAH_COUNT or len(entries) != AYAH_COUNT:
        errors.append(f"{relative_path}: expected exactly {AYAH_COUNT} ayahs")

    numbers: list[int] = []
    footnote_count = 0
    for index, entry in enumerate(entries):
        if not isinstance(entry, dict):
            errors.append(f"{relative_path}: entry {index} is not an object")
            continue
        number = entry.get("globalNumber")
        if not isinstance(number, int) or isinstance(number, bool):
            errors.append(f"{relative_path}: entry {index} has invalid globalNumber")
        else:
            numbers.append(number)
        if not isinstance(entry.get("text"), str) or not entry["text"].strip():
            errors.append(f"{relative_path}: ayah {number} has empty text")
        footnotes = entry.get("footnotes")
        if not isinstance(footnotes, list) or any(not isinstance(note, str) or not note.strip() for note in footnotes):
            errors.append(f"{relative_path}: ayah {number} has invalid footnotes")
        else:
            footnote_count += len(footnotes)
    if len(numbers) != len(set(numbers)):
        errors.append(f"{relative_path}: duplicate ayah numbers")
    if set(numbers) != set(range(1, AYAH_COUNT + 1)):
        errors.append(f"{relative_path}: ayah coverage is not exactly 1..{AYAH_COUNT}")
    if manifest.get("footnoteCount") != footnote_count:
        errors.append(f"{relative_path}: footnote count does not match source entries")
    canonical_entries = [
        {"globalNumber": entry.get("globalNumber"), "text": entry.get("text"), "footnotes": entry.get("footnotes")}
        for entry in entries if isinstance(entry, dict)
    ]
    serialized_entries = json.dumps(canonical_entries, ensure_ascii=False, separators=(",", ":"))
    digest = hashlib.sha256(serialized_entries.encode("utf-8")).hexdigest()
    if not re.fullmatch(r"[a-fA-F0-9]{64}", str(manifest.get("sha256", ""))) or digest != str(manifest.get("sha256", "")).lower():
        errors.append(f"{relative_path}: entries checksum mismatch")
    return manifest, errors


def audit() -> list[str]:
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    if catalog.get("schema_version") != 1 or catalog.get("required_ayah_count") != AYAH_COUNT:
        raise ValueError("Unsupported Quran text pack catalogue schema")
    errors: list[str] = []
    installed: dict[str, set[str]] = {}
    seen_ids: set[str] = set()
    for item in catalog.get("packs", []):
        if not isinstance(item, dict):
            errors.append("catalog.json contains a malformed pack declaration")
            continue
        pack_id = item.get("id")
        if not isinstance(pack_id, str) or pack_id in seen_ids:
            errors.append(f"duplicate or invalid catalogue pack id: {pack_id}")
            continue
        seen_ids.add(pack_id)
        manifest, pack_errors = validate_pack(
            str(item.get("path", "")), str(item.get("kind", "")), str(item.get("languageTag", "")),
        )
        errors.extend(pack_errors)
        if manifest and manifest.get("id") != pack_id:
            errors.append(f"{pack_id}: catalogue id does not match manifest")
        if not pack_errors:
            installed.setdefault(str(item["languageTag"]), set()).add(str(item["kind"]))
    missing: list[str] = []
    for language in supported_languages():
        for kind in sorted(required_kinds(language) - installed.get(language, set())):
            missing.append(f"{language}:{kind}")
    if missing:
        errors.append(
            f"declared-pack coverage missing for {len(missing)} language/type combinations: "
            + ", ".join(missing[:30])
            + (" ..." if len(missing) > 30 else "")
        )
    print(
        f"Quran text catalogue: {len(seen_ids)} packs; "
        f"{len(supported_languages())} supported UI languages; {len(missing)} required coverage gaps."
    )
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--release", action="store_true", help="fail release readiness on any incomplete pack or language gap")
    args = parser.parse_args()
    errors = audit()
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        if args.release:
            return 1
        print("Coverage report only; release mode would reject these gaps.")
    else:
        print("All declared packs and required language coverage are complete.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
