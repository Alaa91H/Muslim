# Hadith Chapter-Title Localisation

> **Scope.** This record covers UI display labels for chapter indexes. It does not alter any Hadith Arabic text, English translation, grade, source attribution, Room query key, search token, collection count, or chapter order.

## Implementation

The bundled collection assets retain the existing source chapter title as their stable value. That value remains the key used by Room, Paging, list keys, and chapter navigation. `HadithChapterArabicTitles` is a collection-scoped lookup table that supplies an Arabic **display** title only when the app UI language is Arabic. English and other non-Arabic UI languages continue to show the source title.

| Collection group | Display-title source | Representation decision |
|---|---|---|
| Bukhari, Muslim, Abu Dawud, Tirmidhi, an-Nasai, Ibn Majah, Muwatta Malik | `metadata.section_details.<section>.name_native` in the Hadith API translation branch. [1] | The `name_native` value is used only as a chapter-index label matched to the current immutable source title. |
| Riyad as-Salihin, Forty Hadith of al-Nawawi | Small reviewed Arabic UI-label mapping for the project’s retained owner-attested corpus. | It mirrors common Arabic book-title conventions and remains distinct from the bundled text. |

The maintainer utility `scripts/generate_hadith_chapter_title_localization.py` uses the pinned upstream data commit [`a6643f9fe8555c6cc804a79c0525c5283a92294c`](https://github.com/IsmailHosenIsmailJames/hadith-api/commit/a6643f9fe8555c6cc804a79c0525c5283a92294c), verifies that every non-blank chapter label in each bundled asset has a matching Arabic display label, and only then emits Kotlin. `scripts/verify_hadith_chapter_localization.py` repeats the coverage check in CI and also verifies that navigation still passes the source title rather than the localized display string.

## Coverage and boundary

The current bundled assets have **417 collection-scoped non-blank chapter labels**. A source string that is repeated in two books may legitimately have distinct display labels, so the lookup key includes the collection id. The implementation avoids a database schema migration and preserves installed users’ existing book preparation and bookmarks.

The upstream contributor describes the `name_native` fields as language translations. [1] This project therefore represents them as interface localization rather than an independently verified scholarly edition or a replacement for original chapter headings. Any future scholarly or source review can update display labels without changing the Hadith records or their local search/paging contracts.

## References

[1] [fawazahmed0/hadith-api pull request #134 — book and chapter native-name fields](https://github.com/fawazahmed0/hadith-api/pull/134)
