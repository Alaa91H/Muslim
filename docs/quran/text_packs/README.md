# Quran text pack format

The catalogue at `catalog.json` lists only complete, sourced works. A pack file
contains exactly this versioned shape:

```json
{
  "schemaVersion": 2,
  "manifest": {
    "id": "stable-source-work-id",
    "kind": "meaning_translation | tafsir_translation | tafsir_original",
    "languageTag": "BCP-47 tag for the text itself",
    "title": "Published edition title",
    "work": "Source work",
    "translator": "Named translator or source-credited editorial team",
    "publisher": "Publisher or source organization",
    "sourceAttribution": "Source-provided contributor and review details",
    "sourceUrl": "https://...",
    "license": "Exact redistribution terms",
    "version": "Publisher version",
    "reviewer": "Qualified reviewer or credited source review",
    "reviewReference": "https://...",
    "expectedAyahCount": 6236,
    "footnoteCount": 0,
    "sha256": "SHA-256 of canonical serialized entries",
    "reviewStatus": "source_reviewed | editor_reviewed"
  },
  "entries": [
    { "globalNumber": 1, "text": "Exact source wording", "footnotes": [] }
  ]
}
```

The example entry is a schema illustration, not a Quran content pack. Every
installable file must contain all 6,236 Quran global ayah numbers exactly once.
`sha256` is computed over compact UTF-8 JSON for the `entries` array in the
field order `globalNumber`, `text`, `footnotes`, preserving source wording and
footnote text. The declared footnote count is the sum of each entry's footnote
array length. Any missing or blank text, duplicate/missing ayah, invalid
provenance, checksum mismatch, or footnote-count mismatch rejects the entire
pack before database writes.

The structural importer verifies completeness, checksum, URL shape, and that
the manifest contains source/review declarations. It cannot authenticate those
declarations or independently certify the translator, publisher, license, or
religious review. Imported metadata must therefore be presented as publisher-
reported provenance, never as Muslim's independent endorsement. Only provider
integrations that re-fetch and match source catalogue metadata may label the
provider's review claim as source-reported.

`meaning_translation` and `tafsir_translation` are separate kinds. Arabic
commentary that is not a translation uses `tafsir_original`. Translators and
editions sharing a language must use separate stable pack IDs. Content is
never machine-translated to satisfy the release language matrix.

The current repository intentionally lists zero production packs. The former
partial samples have been removed. Add a work to the catalog only after its
complete source file and metadata are reviewed; the release gate will validate
the actual content file before accepting the declaration.

The app queries QuranEnc's public translation catalogue at runtime and uses it
only for Quran meaning translations. It preserves source descriptions and only
enables download when that description explicitly names a reviewer or editorial
supervision. Its site also lists Al-Mukhtasar translations in multiple
languages and an Uzbek At-Tafsir Al-Muyassar translation. The app lists all 28
non-Arabic Al-Mukhtasar editions in the QuranEnc index separately as translated
tafsir candidates. It disables their installation because the public index
does not expose the named translator, reviewer, and edition version needed for
this project's verified catalogue.
The QuranEnc index attributes the Uzbek Al-Muyassar translation to Ismail
Yaqub, reports review by Islamic Center IxlosOrg, and lists edition 1.0.0. The
app re-fetches that source card and checks the work, translator, reviewer,
version, and language before allowing a complete 6,236-ayah download. This
source-reported review is not represented as independent Muslim endorsement.
QuranEnc requires re-publications to preserve and show the edition version.
On 2026-10-04, all 114 source API endpoints returned 6,236 distinct, non-empty
ayah rows for this edition; the in-app installer still repeats coverage and
checksum validation before its atomic database write.
Other translated-tafsir candidates remain unavailable until their missing
translator/reviewer/version metadata can be established. Translated-tafsir
packages may also be imported from JSON and are structurally validated before
installation. A successful structural validation is not, by itself, an
independent theological review.

QuranEnc publishes explicit reuse terms for its translations in its [API
documentation](https://quranenc.com/ar/home/api/): preserve content unchanged,
credit the publisher and QuranEnc, preserve the edition version information,
report observations, update to new source editions, and avoid inappropriate
advertising alongside the meanings. Its public API documents translation list,
ayah, and surah endpoints, but not a separate tafsir-content endpoint. This
permits source-controlled user-initiated downloads where the app validates the
full pack and displays source attribution/version; it does not make a meaning
translation into translated tafsir. The app must continue to recheck source
metadata and coverage before installation.

Tanzil's translation catalogue is a discovery index, not blanket permission to
bundle its translations. Its current [terms](https://tanzil.net/trans/) limit
the translations to non-commercial use unless permission is obtained from each
translator or publisher, require a link back when more than three listed
translations are used in an application, prohibit republishing the complete
list without Tanzil's direct permission, and disclaim guarantees of translation
authenticity or accuracy. Tanzil also documents that its editions come from
many upstream sources ([provenance list](https://tanzil.net/docs/translations_resources)).
Muslim is currently described as free and ad-free, but that product fact alone
does not establish the contractual meaning of "non-commercial" for every
distribution channel or downstream publisher. Therefore no Tanzil translation
is bundled or marked approved from the catalogue listing alone. Each candidate
needs an edition-specific rights basis, named edition/translator and reviewer
evidence, source version, and complete text before it can enter `catalog.json`.
The Arabic Quran text's separate Tanzil CC BY 3.0 license does not grant rights
to the translations. This distinction is recorded in
[`docs/content/license_research.md`](../../content/license_research.md).

Quran Foundation documents catalogue and paginated tafsir-content endpoints,
including author and resource metadata, but their current API schema documents
authorization failures and required request headers. No Quran Foundation app
credentials or license-specific approval are configured in this repository, so
those endpoints are not treated as a downloadable, approved source yet. The
release gate intentionally remains red until every supported UI language has a
complete, source-attributed meaning translation and translated tafsir (and
Arabic original tafsir); locale UI coverage is not evidence that Quran text
content exists in that language. See the provider's [tafsir catalogue](https://api-docs.quran.com/docs/content_apis_versioned/4.0.0/tafsirs/)
and [tafsir content API](https://api-docs.quran.com/docs/content_apis_versioned/4.0.0/tafsir/).
