# Localization and religious-text quality gates

## Interface resources

Interface text lives in Android string resources. `scripts/localize.py --check`
parses the app, core, and feature modules and fails on invalid XML, duplicate
keys, missing or empty values, missing English source resources, unchanged
multiword source text, and formatting tokens that do not match their source.
The check prints issue totals by category even when individual diagnostics are
capped. The CI workflow runs this check and its unit tests on every pull request.
`--fill-missing` only adds absent values;
`--repair-formats` only replaces values with damaged format tokens;
`--complete-languages` only creates missing locale files. These commands keep
existing localized values intact.

The check verifies resource structure and Android formatting. It cannot certify
translation meaning, tone, or regional usage. Machine-generated interface text
must be reviewed by a fluent speaker before it is described as professionally
reviewed.

## Quran meanings, tafsir, and hadith

The general interface localizer reads `strings.xml` resources only. It must not
translate Quran ayahs, meanings, tafsir, or hadith bodies. Those texts remain
separate content packs and must preserve their source edition and wording.

Before a religious-text pack is treated as complete, its manifest and tests
must verify:

- language tag, work or edition, translator or editor, publisher/source URL,
  version, and redistribution licence;
- complete coverage for the stated scope, with no missing or duplicate Quran
  ayah numbers (the full Quran contains 6,236 ayahs);
- the source's text, footnotes, numbering, grading, and attribution without
  silent edits;
- human review by a qualified reader of the target language and the source
  edition.

Hadith translations also retain the collection, book/chapter, hadith number,
edition, and any supplied grading. Tafsir metadata records the work and author;
the language of a tafsir is explicit and is never inferred from the selected
UI language.

The Quran text pack catalogue is separate from Android UI localization. Each
work declares whether it is a Quran meaning translation, translated tafsir, or
original-language tafsir, plus language tag, work, translator, publisher,
source URL, licence, version, review status, ayah coverage, footnote count, and
SHA-256. Multiple works and translators may coexist in one language.

`scripts/verify_quran_text_packs.py` validates every pack listed in
`docs/quran/text_packs/catalog.json`: metadata, all 6,236 distinct ayah IDs,
non-empty text, retained non-empty footnotes, their declared count, and the
content checksum. Release mode additionally requires a complete meaning
translation and translated tafsir for every supported UI language, plus an
original Arabic tafsir for Arabic. The CI tag path runs this mode and blocks a
release while any language/type coverage is absent. Ordinary CI prints a
coverage report without representing missing content as available.

At install time, the app applies full-coverage and checksum checks before a
database transaction. It installs the pack metadata and entries together only
after validation; partial downloads cannot replace an installed work.
Footnotes stay attached to their ayahs and the reader never falls back to
religious text in another language. Existing unverified database rows have no
verified catalogue record and are not advertised as installed packs.

The former `quran_*_sample.json` assets each held only 11 of 6,236 ayahs and
were removed from production assets and the approval inventory. No automatic
translation fills missing language packs. UI locale coverage is not
religious-text coverage; a language remains unsupported for meanings or
translated tafsir until a sourced, complete pack is supplied.
