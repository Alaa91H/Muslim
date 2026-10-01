# Localization and religious-text quality gates

## Interface resources

Interface text lives in Android string resources. `scripts/localize.py --check`
parses the app, core, and feature modules and fails on invalid XML, duplicate
keys, missing or empty values, missing English source resources, and formatting
tokens that do not match their source. The CI workflow runs this check and its
unit tests on every pull request. `--fill-missing` only adds absent values;
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

The existing QuranEnc integration downloads At-Tafsir Al-Muyassar and validates
all ayahs before replacing the installed source. It is not a multilingual
Quran-translation catalogue or a claim that every supported UI language has a
reviewed Quran meaning or tafsir. Do not use interface-resource coverage as
evidence that religious-text coverage is complete.
