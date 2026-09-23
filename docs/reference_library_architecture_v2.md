# Reference Library Architecture v2

This document defines the migration path for the in-app Islamic Reference Library from a small set of Kotlin-authored summaries to a maintainable, source-aware, searchable reference system.

## Goals

1. Keep the current Arabic/English reader working while the content layer is migrated incrementally.
2. Separate the UI from the storage format through a `ReferenceRepository` boundary.
3. Support a hierarchy of **Book -> Chapter -> Article -> Section -> Paragraph** without duplicating topic data.
4. Make every article capable of carrying structured citations, related topics, search keywords, editorial state, and review metadata.
5. Make Arabic search resilient to harakat, tatweel, and common alif/hamza spelling variants.
6. Add deterministic validation that can run in unit tests and CI before reference content ships.

## Runtime model

The reader consumes `ReferenceBook`, `RefTopic`, `RefSection`, and `RefParagraph` regardless of whether a book originated in legacy Kotlin or a versioned JSON content pack. The original three books now use versioned assets at runtime, and the same model supports newly added reference books.

### New metadata

- `RefChapter`: groups canonical topic IDs into chapters.
- `ReferenceCitation`: typed source metadata with a locator and optional URL/notes.
- `ReferenceReviewStatus`: `Draft`, `NeedsReview`, or `Reviewed`.
- `keywordsAr` / `keywordsEn`: hidden search aliases.
- `relatedTopicIds`: local or qualified cross-book links.
- `contentRevision`: independent content-schema revision.

Legacy articles default to `NeedsReview`. That default is deliberate: migrating an existing text into the richer model must not imply that it received a new independent scholarly review.

## Repository boundary

`ReferenceRepository` is the UI-facing content contract. Android now constructs the runtime catalogue from versioned `reference_*_v2.json` packs. The three legacy Kotlin books remain only as migration fallbacks while CI parity tests guarantee that their original content is preserved.

Current catalogue:

| Book | Asset | Status |
| --- | --- | --- |
| Introduction to Islam | `reference_islam_v2.json` | migrated + expanded |
| Prophetic Biography | `reference_sira_v2.json` | migrated + expanded |
| Stories of the Prophets | `reference_prophets_v2.json` | migrated + expanded |
| Companions of the Prophet | `reference_companions_v2.json` | asset-native |
| Mothers of the Believers | `reference_mothers_v2.json` | asset-native |
| Ahl al-Bayt | `reference_ahl_al_bayt_v2.json` | asset-native |
| Rashidun Caliphs | `reference_rashidun_v2.json` | asset-native |

Remaining storage work:

1. Keep extending the source-aware JSON corpus and editorial validation.
2. Add a prebuilt Room/FTS index when corpus size or profiling shows that in-memory ranked search is no longer appropriate.
3. Remove the three legacy Kotlin fallback objects only after the migration series is merged and release parity has been exercised.

## Citation rules

A citation is declared once at article level and referenced by ID from paragraphs or sections.

Examples of source families:

- Quran: locator such as `2:255`.
- Hadith: collection + hadith identifier.
- Classical book: edition-aware volume/page or chapter locator.
- Historical/academic source: stable bibliographic locator and optional URL.
- Internal: links to an approved first-party reference dataset.

An article may only be marked `Reviewed` when it has at least one structured citation and an ISO-8601 `lastReviewed` date. This is a structural release rule, not a substitute for scholarly review.

## Validation

`ReferenceContentValidator` detects:

- duplicate book, chapter, topic, section, or citation IDs;
- blank bilingual titles, summaries, or paragraphs;
- empty sections;
- chapter links to missing topics;
- related-topic links to missing targets;
- paragraph/section references to undeclared citations;
- reviewed articles without citations;
- missing or malformed review dates;
- invalid content revisions.

The validator is pure Kotlin so it can be reused by unit tests, build tooling, and future content import scripts.

## Search

Architecture v2 adds ranked library-wide search while preserving book-local search.

Arabic normalization currently:

- strips harakat and Quranic combining marks used in ordinary search text;
- strips tatweel;
- unifies `أ إ آ ٱ` with `ا`;
- unifies `ى` with `ي`;
- normalizes hamza-bearing waw/ya forms;
- applies Unicode compatibility normalization;
- collapses repeated whitespace.

Ranking prioritizes exact/prefix title matches, then keyword, title containment, summary, section title, and paragraph text.

## Current reader capabilities

The library home now supports ranked search across every registered book. Book pages retain chapter grouping and book-local search, while article pages render structured citation lines, a full sources section, editorial review state, and navigable cross-book related topics.

## Next implementation milestones

The architecture is now ready for editorial depth rather than additional structural migration. The next milestones are:

1. expand the new Companions/Ahl al-Bayt/Mothers/Rashidun books from introductory biographies into deeper sourced articles;
2. add source-level review metadata (reviewer, source edition, review notes) without exposing private reviewer data in the shipped corpus;
3. add bookmarks/recently-read state for long-form reference reading;
4. add Room/FTS only after measuring the expanded corpus on low-memory Android devices;
5. remove legacy Kotlin fallbacks after the stacked migration PRs are merged and release parity has been confirmed.
