# Quran Translation and Tafsir Catalogue Implementation Plan

> **For agentic workers:** Execute natively in this task. Keep source content distinct from UI localization and never generate religious text to fill a gap.

**Goal:** Add a provenance-aware catalogue and strict completeness gate for Quran meaning translations and translated tafsir packs.

**Architecture:** Pack metadata and the two religious-text kinds remain separately identified. Downloads/imports are validated against the 6,236 local ayah identifiers, duplicate/missing entries, footnotes, and source metadata before any database replacement. UI locale support is audited separately from actually available trusted religious-content languages; missing packs remain unavailable and are never represented as complete.

**Tech Stack:** Kotlin, Room, Kotlin Serialization, Android Gradle, Python quality-gate scripts, QuranEnc public API.

**Spec:** User request in this conversation (2026-10-04).

## Global Constraints

- Never substitute unreviewed machine translation for missing Quran or tafsir content.
- Keep Quran meaning translations distinct from translations of tafsir works.
- Preserve source-provided text and footnotes with attribution, edition, translator, publisher, URL, version, and licence.
- Do not install/advertise a pack as complete until its full expected scope validates.

## Review Focus

- Missing, duplicate, unknown, or blank ayah rows must reject a pack before writes.
- Footnotes must remain attached and referenced without silent dropping.
- Same-language works from different translators must coexist in the catalogue.
- Incomplete legacy/sample data must not be presented as a complete production pack.
- UI locale presence must not be mistaken for religious-content availability.

---

### Task 1: Versioned pack contract and completeness validator

**Files:** `feature/feature-quran/.../data/QuranSupplementRepository.kt`, focused domain/data validator and tests, pack-format documentation.

- [x] Define one manifest contract for meaning and tafsir translations, source metadata, review state, ayah rows, and footnotes.
- [x] Validate metadata, all unique local ayah IDs, non-empty text, footnote counts, and declared checksums before installation.
- [x] Add contract cases for complete, missing, duplicate, blank text, checksum/footnote mismatch, and invalid metadata packs.

### Task 2: Source-aware installed catalogue

**Files:** translation/tafsir Room entities and DAOs, `AppDatabase.kt` migration/schema, Quran domain presentation models and repository queries.

- [x] Allow multiple works for one language and persist stable pack IDs plus provenance metadata.
- [x] Preserve existing rows as unverified legacy data; do not expose them as verified catalogue packs.
- [x] Keep translated tafsir source identity separate from Quran meaning translation identity.

### Task 3: Atomic downloads and UI truthfulness

**Files:** Quran supplement repository, reader ViewModel/UI selectors, QuranEnc source catalogue.

- [x] Present separate catalogue sections for Quran meanings, translated tafsir, and original tafsir, grouped by text language/source/contributor. Listed all 28 non-Arabic Al-Mukhtasar editions currently present in QuranEnc's index as translated-tafsir candidates; downloads remain disabled where translator/reviewer/version metadata is not supplied. Enabled Uzbek At-Tafsir Al-Muyassar using its official index metadata (translator, source-reported review, version 1.0.0) and require those fields to still match before download.
- [x] Validate complete ayah and footnote coverage before an atomic install.
- [x] Remove fallback to a different language when the selected language has no verified pack.
- [x] Surface source attribution and clear unavailable/incomplete states.

### Task 4: Release quality gate and provenance repair

**Files:** `scripts/`, `.github/workflows/`, `docs/localization/quality-gates.md`, `docs/content/*`, Quran sample assets.

- [x] Audit every declared UI locale against the verified meaning/tafsir catalogue without auto-translating content.
- [x] Make release quality checks fail when declared coverage is missing or pack claims disagree with actual data.
- [x] Remove the two 11-ayah assets from production seeding and approvals.
- [x] Document trustworthy pack requirements and the exact current coverage gap.
