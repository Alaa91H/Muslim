# v1.24.5 Tafsir Source Verification

**Reviewed:** 2026-08-25.

The official [QuranEnc API documentation](https://quranenc.com/en/home/api/) documents per-surah retrieval at `https://quranenc.com/api/v1/translation/sura/{translation_key}/{sura_number}` and states that the translations list normally contains a key, version, last-update timestamp, localized title, and description. Its published republication terms require: no content modification; clear publisher/source attribution to QuranEnc.com; the version number; preservation of transcript information; reporting notes; updating to the latest issued version; and no inappropriate advertising.

The live `arabic_moyassar` per-surah endpoint returned a complete JSON payload for surah 1 on 2026-08-25, with the expected ayah fields and `footnotes: null`. The official browse page identifies the source as **Arabic Language — At-Tafsir Al-Muyassar** and associates it with QuranEnc. However, the current translations-list endpoint does not return Arabic-language rows, so it does not expose a machine-readable version or last-update field for this source at this time.

The previously configured `arabic_saadi` QuranEnc per-surah endpoint returned an empty 200 response on 2026-08-25 and is not listed in the live QuranEnc translations listing. The separate official Saadi site, [saadi.islamenc.com](https://saadi.islamenc.com/id/browse/sura/1), is reachable and displays the text, but no documented bulk API/download terms or version metadata were verified. It must therefore **not** be retained as an automated, production download source without a documented authorization/API path.

**Release decision:** retain the fully validated QuranEnc Al-Muyassar downloader only after it records and displays its source/attribution and download timestamp, clearly says that QuranEnc’s live version metadata was unavailable at download time, and provides a link to the source. Do not claim a version number that the source did not provide. Remove the non-working automated Saadi option; it remains possible to add a second source later when a documented, authorized endpoint with version/transcript metadata is available.

The interface may be localized, but the downloaded Al-Muyassar text remains the Arabic text supplied by the publisher and must not be represented as a translation into the application's UI language.

Additional browser review of the official Al-Muyassar page on 2026-08-25 confirmed that it exposes a **Browse Old Version** link to `https://old.quranenc.com/en`, but the current page does not expose a current version number in its rendered source information. The application must therefore link to the official source and record the retrieval time; it must not fabricate a version value. The live page also carries QuranEnc’s general caution that human renderings of Quranic meanings are not error-free.

## Follow-up verification — 2026-08-25

- **Altafsir is not an implementation source.** Its published terms prohibit automated downloading, transferring, monitoring, copying, reproduction, and public distribution of content without prior written permission. Do not add any Altafsir endpoint, scraper, bundled pack, or download option from that site.
- **Quran Foundation / Quran.com offers an authenticated Content API** with Arabic tafsir resources including Tabari and Baghawi in its documented resource examples. Its current developer terms allow in-app display but prohibit redistributing raw content and normally limit caching to one week unless the approved Content Sync exception is used and a sync is performed at least every seven days. The official quickstart requires a backend-held client credential and production permission; a mobile APK must not embed a client secret.
- The legacy public `api.quran.com/api/v4` endpoints returned data during a passive compatibility check, but the official migration path is the authenticated Quran Foundation API. Treat legacy public access as non-contractual and do not build a production downloader around it.
- **QuranEnc** remains suitable only for source keys, content, and terms that have been individually verified. Its official API documentation requires preserving content unchanged, publisher/source attribution, version number, transcript information, and current updates when republishing.

Sources: https://www.altafsir.com/TafsirTerms.asp ; https://api-docs.quran.foundation/legal/developer-terms/ ; https://api-docs.quran.foundation/docs/quickstart/ ; https://quranenc.com/en/home/api/

A passive 2026-08-25 check of the legacy public Quran.com v4 resource list surfaced Arabic resources for **Al-Saadi** (`91`), **Ibn Kathir** (`14`), **Al-Tabari** (`15`), **Al-Baghawi** (`94`), and **Al-Qurtubi** (`90`). The same response did not list Al-Mukhtasar, Adwa' al-Bayan, Fath al-Qadir, Al-Muharrar al-Wajiz, or Al-Tahrir wa al-Tanwir. This is catalog evidence only, not production permission: do not use legacy public endpoints for bundled or indefinite offline downloads. Quran Foundation's current terms and authenticated API path govern production access.

## QUL alternative check — 2026-08-25

Tarteel's Quranic Universal Library (QUL) catalog lists all ten requested Arabic tafsir resources: As-Saadi (308), Al-Mukhtasar (251), Ibn Kathir (22), Al-Qurtubi (23), Al-Baghawi (27), Al-Tabari (37), Al-Muharrar Al-Wajiz (509), Al-Tahrir wa Al-Tanwir (25), Adwa' Al-Bayan (525), and Fath Al-Qadir (494). Its resources page says resources are intended to be downloaded and packaged with a project, while its FAQ says each resource may have distinct licensing and must be checked individually. However, the actual JSON/SQLite export buttons open a mandatory login dialog and the public resource page does not expose resource-level licence metadata. Therefore QUL does not provide a **no-login, source-licence-verifiable** automated download path for this release. Do not bypass the login, scrape its content, or use mirrors that re-host the same material without the original rights record.

The public `spa5k/tafsir_api` project is MIT-licensed as code, but it identifies Quran.com, Altafsir, and QUL as underlying content sources. The code licence does not establish redistribution rights for the underlying tafsir text, so it is not an approved replacement source.

Sources: https://qul.tarteel.ai/resources ; https://qul.tarteel.ai/faq ; https://qul.tarteel.ai/resources/tafsir/308 ; https://github.com/spa5k/tafsir_api
