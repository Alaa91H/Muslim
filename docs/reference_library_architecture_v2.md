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

The current reader still consumes `ReferenceBook`, `RefTopic`, `RefSection`, and `RefParagraph`. Architecture v2 extends these models without breaking the existing three bundled books.

### New metadata

- `RefChapter`: groups canonical topic IDs into chapters.
- `ReferenceCitation`: typed source metadata with a locator and optional URL/notes.
- `ReferenceReviewStatus`: `Draft`, `NeedsReview`, or `Reviewed`.
- `keywordsAr` / `keywordsEn`: hidden search aliases.
- `relatedTopicIds`: local or qualified cross-book links.
- `contentRevision`: independent content-schema revision.

Legacy articles default to `NeedsReview`. That default is deliberate: migrating an existing text into the richer model must not imply that it received a new independent scholarly review.

## Repository boundary

`ReferenceRepository` is now the UI-facing content contract. The initial implementation wraps the existing Kotlin content objects. Future work can replace that implementation with an asset- or Room-backed repository without changing the reader's public model.

Planned migration:

1. Introduce schema and repository boundary. **(this change)**
2. Define versioned JSON/NDJSON asset schema and parser.
3. Migrate one book at a time from Kotlin source into bundled content packs.
4. Add a prebuilt Room/FTS index when the corpus is large enough to justify it.
5. Remove the legacy Kotlin content objects only after parity tests pass.

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

## Next implementation milestone

The next PR should introduce the versioned asset schema and migrate **Introduction to Islam** first. The migration must include parity tests proving that:

- every legacy topic remains reachable;
- Arabic and English text is preserved;
- IDs stay stable;
- search results remain available;
- citations/review metadata can be added without changing reader code.

After that migration is stable, the same path can be used for Sira and Prophets before expanding the corpus to the larger reference roadmap.
