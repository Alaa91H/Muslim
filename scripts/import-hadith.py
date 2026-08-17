#!/usr/bin/env python3
"""Import the complete Six Books of hadith into the app's corpus format.

Source (licensed, open):
  the hadith-api project — github.com/fawazahmed0/hadith-api — served via the
  jsDelivr CDN (https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/).
  The API itself is MIT-licensed; the classical Arabic texts are public-domain
  works; the English translations keep their original copyrights (attribution
  in the source project). The official Sunnah.com API (api.sunnah.com) is an
  alternative source for the Arabic text (free for personal/research use with a
  free API key).

The pipeline:
  1. Fetches `ara-{book}.json` (+ `eng-{book}.json` unless --no-eng) for each
     requested book of the Six Books.
  2. Maps each hadith into the app schema
     ({collection, chapter, number, arabic, translation, grade, source}).
  3. Deduplicates by fingerprinting the diacritic-normalized Arabic text
     (فحص التكرار): in-book always, across books only with --dedupe-across-books.
  4. Writes feature/feature-hadith/src/main/assets/hadith_full.json
     ({note, version, hadiths:[...]}) — the app loads this file when present
     and falls back to the bundled curated sample otherwise.
  5. Re-verifies the output file: asserts zero duplicate fingerprints and
     prints a report (fetched / kept / duplicates, per book + total size).

Usage:
  python scripts/import-hadith.py                       # all six books
  python scripts/import-hadith.py --books bukhari,muslim
  python scripts/import-hadith.py --limit 25            # smoke test
  python scripts/import-hadith.py --no-eng --out /tmp/hadith.json
  python scripts/import-hadith.py --self-check          # verify dedupe logic

Note: the full Six Books are ~34k hadiths; the resulting JSON is tens of MB.
The output file is git-ignored by default — only bundle it if your release
strategy accepts the APK size, and keep the curated sample as the default.
"""

from __future__ import annotations

import argparse
import gzip
import hashlib
import json
import os
import re
import shutil
import sys
import time
import urllib.request

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_OUT = os.path.join(
    REPO_ROOT, "feature", "feature-hadith", "src", "main", "assets", "hadith_full.json"
)
DEFAULT_CACHE = os.path.join(REPO_ROOT, ".gradle", "hadith-api-cache")

CDN = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions"

# App collection ids (see HadithCollection) + the Arabic attribution shown to users.
BOOKS = {
    "bukhari": {"collection": "bukhari", "source_ar": "صحيح البخاري"},
    "muslim": {"collection": "muslim", "source_ar": "صحيح مسلم"},
    "abudawud": {"collection": "abudawud", "source_ar": "سنن أبي داود"},
    "tirmidhi": {"collection": "tirmidhi", "source_ar": "جامع الترمذي"},
    "nasai": {"collection": "nasai", "source_ar": "سنن النسائي"},
    "ibnmajah": {"collection": "ibnmajah", "source_ar": "سنن ابن ماجه"},
}

# Harakat / tashkeel / tatweel / waqf marks / special characters stripped for
# the dedupe fingerprint (they do not change the matn).
_DIACRITICS = re.compile(
    r"[\u064B-\u065F\u0670\u0610-\u061A\u0640\u06D6-\u06ED\u200C\u200D]"
)
_LETTER_FOLDS = str.maketrans("أإآٱةى", "ااااهي")


def normalize_arabic(text: str) -> str:
    """Strip diacritics and fold letter variants so identical matns match."""
    t = _DIACRITICS.sub("", text or "")
    t = t.translate(_LETTER_FOLDS)
    return re.sub(r"\s+", " ", t).strip()


def fingerprint(text: str) -> str:
    return hashlib.sha256(normalize_arabic(text).encode("utf-8")).hexdigest()


def dedupe(items, across_books: bool) -> tuple[list, int]:
    """Returns (kept, skipped) — in-book always, cross-book only if requested."""
    seen = set()
    kept = []
    skipped = 0
    for item in items:
        fp = fingerprint(item["arabic"])
        key = fp if across_books else (item["collection"], fp)
        if key in seen:
            skipped += 1
            continue
        seen.add(key)
        kept.append(item)
    return kept, skipped


def fetch_json(url: str, cache_dir: str):
    name = url.rsplit("/", 1)[-1]
    path = os.path.join(cache_dir, name)
    if os.path.exists(path):
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    last_error = None
    for attempt in range(3):
        try:
            with urllib.request.urlopen(url, timeout=180) as r:
                data = json.loads(r.read().decode("utf-8"))
            os.makedirs(cache_dir, exist_ok=True)
            with open(path, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False)
            return data
        except Exception as exc:  # noqa: BLE001 - transient network errors
            last_error = exc
            time.sleep(2 * (attempt + 1))
    raise RuntimeError(f"failed to fetch {url}: {last_error}")


def coerce_number(n):
    """Normalize a source hadithnumber into an int for the app's Int? schema.

    The source uses floats for sub-narrations (e.g. 402.2) and occasionally
    strings; truncate the decimal suffix so the value always decodes as an int.
    """
    if n is None:
        return None
    if isinstance(n, bool):
        return int(n)
    if isinstance(n, int):
        return n
    if isinstance(n, float):
        return int(n)
    try:
        return int(n)
    except (TypeError, ValueError):
        return None


def pick_grade(grades) -> str:
    """Prefer Al-Albani's verdict, else the first available one."""
    if not grades:
        return "—"
    for g in grades:
        if "albani" in str(g.get("name", "")).lower():
            return str(g.get("grade", "—"))
    return str(grades[0].get("grade", "—"))


def load_book(book_id: str, with_eng: bool, limit: int, cache_dir: str) -> tuple[list, str]:
    cfg = BOOKS[book_id]
    ara = fetch_json(f"{CDN}/ara-{book_id}.json", cache_dir)
    eng = None
    if with_eng:
        eng = fetch_json(f"{CDN}/eng-{book_id}.json", cache_dir)

    sections = ara.get("metadata", {}).get("sections", {}) or {}
    eng_by_number = {}
    if eng:
        for h in eng.get("hadiths", []):
            eng_by_number[h.get("hadithnumber")] = h.get("text", "")

    items = []
    for h in ara.get("hadiths", []):
        raw_number = h.get("hadithnumber")
        if limit and len(items) >= limit:
            break
        arabic_text = (h.get("text") or "").strip()
        if not arabic_text:
            # Section/header placeholders without a matn — not real hadiths.
            continue
        book = (h.get("reference") or {}).get("book")
        chapter = sections.get(str(book), "") if book is not None else ""
        items.append(
            {
                "collection": cfg["collection"],
                "chapter": chapter or None,
                "number": coerce_number(raw_number),
                "arabic": arabic_text,
                "translation": eng_by_number.get(raw_number, "") if eng else "",
                "grade": pick_grade(h.get("grades")),
                "source": cfg["source_ar"],
            }
        )
    return items, ara.get("metadata", {}).get("name", book_id)


def run_self_check() -> None:
    """Validates the dedupe + normalization logic on synthetic fixtures."""
    a = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
    b = "بسم الله الرحمن الرحيم"
    c = "إِنَّمَا الْأَعْمَالُ بِالنِّيَّاتِ"
    assert normalize_arabic(a) == normalize_arabic(b), "normalization fold failed"
    assert fingerprint(a) == fingerprint(b), "fingerprint fold failed"
    items = [
        {"collection": "bukhari", "arabic": c},
        {"collection": "bukhari", "arabic": a},
        {"collection": "bukhari", "arabic": b},  # duplicate of #1 (normalized)
        {"collection": "muslim", "arabic": a},   # cross-book duplicate
    ]
    kept, skipped = dedupe(items, across_books=False)
    assert skipped == 1 and len(kept) == 3, "in-book dedupe failed"
    kept_x, skipped_x = dedupe(items, across_books=True)
    assert skipped_x == 2 and len(kept_x) == 2, "cross-book dedupe failed"
    print("self-check OK: normalization, fingerprints, in-book & cross-book dedupe")


def main() -> int:
    parser = argparse.ArgumentParser(description="Import the Six Books of hadith")
    parser.add_argument("--books", default=",".join(BOOKS), help="comma list of book ids")
    parser.add_argument("--limit", type=int, default=0, help="max hadiths per book (0 = all)")
    parser.add_argument("--no-eng", action="store_true", help="skip English translations")
    parser.add_argument("--dedupe-across-books", action="store_true", help="also drop cross-book matn duplicates")
    parser.add_argument("--out", default=DEFAULT_OUT, help="output JSON path")
    parser.add_argument("--cache-dir", default=DEFAULT_CACHE, help="download cache dir")
    parser.add_argument("--self-check", action="store_true", help="run fixture checks and exit")
    parser.add_argument("--version", type=int, default=10, help="corpus version (bump to force reseed)")
    args = parser.parse_args()

    if args.self_check:
        run_self_check()
        return 0

    book_ids = [b.strip() for b in args.books.split(",") if b.strip()]
    unknown = [b for b in book_ids if b not in BOOKS]
    if unknown:
        print(f"unknown books: {unknown}; known: {list(BOOKS)}", file=sys.stderr)
        return 2

    all_items = []
    report = []
    for book_id in book_ids:
        items, name = load_book(book_id, with_eng=not args.no_eng, limit=args.limit, cache_dir=args.cache_dir)
        kept, dupes = dedupe(items, across_books=args.dedupe_across_books)
        all_items.extend(kept)
        report.append(f"  {book_id:10s} {name:24s} fetched={len(items):6d} kept={len(kept):6d} dupes={dupes}")
        print(f"  {book_id}: {len(items)} fetched, {len(kept)} kept, {dupes} duplicates skipped")

    if not all_items:
        print("nothing imported — aborting", file=sys.stderr)
        return 3

    out = {
        "note": "Complete Six Books imported from the licensed hadith-api project "
        "(github.com/fawazahmed0/hadith-api). Bump 'version' when regenerating so "
        "installed copies reseed.",
        "version": args.version,
        "hadiths": all_items,
    }

    os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=1)

    # The repo ships the compressed form (hadith_full.json.gz, ~11 MB) so every
    # build carries the full corpus without a 54 MB blob in git or the APK.
    gz_path = args.out + ".gz"
    with open(args.out, "rb") as src, gzip.open(gz_path, "wb", compresslevel=9) as dst:
        shutil.copyfileobj(src, dst, length=1024 * 1024)

    # فحص التكرار: re-open the written file and assert zero duplicates using
    # the same keying as the dedupe step (per-book by default, or global only
    # with --dedupe-across-books). Cross-book matn repeats are legitimate — a
    # hadith often appears in both Bukhari and Muslim.
    with open(args.out, encoding="utf-8") as f:
        written = json.load(f)
    keys = []
    for h in written["hadiths"]:
        fp = fingerprint(h["arabic"])
        keys.append(fp if args.dedupe_across_books else (h["collection"], fp))
    dup_count = len(keys) - len(set(keys))
    if dup_count:
        print(f"FAIL: {dup_count} duplicate fingerprints in the output file!", file=sys.stderr)
        return 4

    size_mb = os.path.getsize(args.out) / (1024 * 1024)
    gz_mb = os.path.getsize(gz_path) / (1024 * 1024)
    print("\n=== import report ===")
    print("\n".join(report))
    print(f"  TOTAL kept: {len(written['hadiths'])} hadiths across {len(book_ids)} books")
    print(f"  Duplicate check: PASS (0 duplicates in {args.out})")
    print(f"  Output: {args.out} ({size_mb:.1f} MB) + {gz_path} ({gz_mb:.1f} MB compressed)")
    print("  Commit the .gz asset so every build ships the full corpus; the raw")
    print("  JSON stays git-ignored and is regenerated on re-import.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
