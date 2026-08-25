# v1.24.5 Tafsir Source Verification

**Reviewed:** 2026-08-25.

The official [QuranEnc API documentation](https://quranenc.com/en/home/api/) documents per-surah retrieval at `https://quranenc.com/api/v1/translation/sura/{translation_key}/{sura_number}` and states that the translations list normally contains a key, version, last-update timestamp, localized title, and description. Its published republication terms require: no content modification; clear publisher/source attribution to QuranEnc.com; the version number; preservation of transcript information; reporting notes; updating to the latest issued version; and no inappropriate advertising.

The live `arabic_moyassar` per-surah endpoint returned a complete JSON payload for surah 1 on 2026-08-25, with the expected ayah fields and `footnotes: null`. The official browse page identifies the source as **Arabic Language — At-Tafsir Al-Muyassar** and associates it with QuranEnc. However, the current translations-list endpoint does not return Arabic-language rows, so it does not expose a machine-readable version or last-update field for this source at this time.

The previously configured `arabic_saadi` QuranEnc per-surah endpoint returned an empty 200 response on 2026-08-25 and is not listed in the live QuranEnc translations listing. The separate official Saadi site, [saadi.islamenc.com](https://saadi.islamenc.com/id/browse/sura/1), is reachable and displays the text, but no documented bulk API/download terms or version metadata were verified. It must therefore **not** be retained as an automated, production download source without a documented authorization/API path.

**Release decision:** retain the fully validated QuranEnc Al-Muyassar downloader only after it records and displays its source/attribution and download timestamp, clearly says that QuranEnc’s live version metadata was unavailable at download time, and provides a link to the source. Do not claim a version number that the source did not provide. Remove the non-working automated Saadi option; it remains possible to add a second source later when a documented, authorized endpoint with version/transcript metadata is available.

The interface may be localized, but the downloaded Al-Muyassar text remains the Arabic text supplied by the publisher and must not be represented as a translation into the application's UI language.

Additional browser review of the official Al-Muyassar page on 2026-08-25 confirmed that it exposes a **Browse Old Version** link to `https://old.quranenc.com/en`, but the current page does not expose a current version number in its rendered source information. The application must therefore link to the official source and record the retrieval time; it must not fabricate a version value. The live page also carries QuranEnc’s general caution that human renderings of Quranic meanings are not error-free.
