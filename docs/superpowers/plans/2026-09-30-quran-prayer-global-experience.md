# Quran, Prayer and Global Experience Implementation Plan

> For agentic workers: use executing-plans task by task; preserve current data and religious content. User authorized implementation, CI repair, push, merge and deletion of merged branches.

**Goal:** Complete the user's 2026-09-30 requirements while finishing UI/UX V2 integration.
**Architecture:** Keep offline Quran/prayer domain contracts, move Mushaf paging onto global page order, extend provider-backed recitation/content catalogues, share persisted preferences across phone/car/watch/cast. Custom Cast rendering has a separate receiver and a versioned payload.
**Tech Stack:** Kotlin/Compose, Room/DataStore, Media3/media session, Android Auto/Wear/Cast, Python quality tooling, GitHub Actions.
**Spec:** User message 2026-09-30 and docs/design/ui_ux_v2_execution_plan.md.

## Constraints and review focus

- Preserve every existing reciter ID and downloaded file; add verified sources without substituting one reciter's recording for another.
- Preserve exact Quran text, ayah/page numbering, prayer calculation and scheduling semantics.
- Persist explicit user overrides; change defaults only when a preference is absent.
- Opening Aal Imran then paging backwards must show the preceding Mushaf page containing Al Baqarah, never jump to Al Fatiha. Pages containing multiple surahs remain complete.
- Tafsir following must be independent of manual browsing and have an explicit persisted off switch.
- Respect Android notification/automotive/Cast platform contracts; runtime hardware evidence stays open when hardware is unavailable.
- Human-level translation quality cannot be inferred from generated locale folders: coverage, provenance and review must be tracked.

## Task 1: Book-style Quran navigation

Files: QuranReaderViewModel.kt, QuranReaderScreen.kt, a focused Mushaf page helper and regression tests.
- [x] Load the complete offline Mushaf once and group all ayahs by canonical page.
- [x] Initialize at the requested surah/ayah, preserve page order across surahs and complete shared pages.
- [x] Remove asynchronous previous-surah edge chaining; make metadata/ayah loading atomic.
- [x] Cover Aal Imran → previous page, first/last page and wide spreads; verify in unit tests, Compose swipe-direction instrumentation, and the book-navigation instrumentation on both CI API levels (runs `37105383786` and `37125639839`).

## Task 2: Reciters and verified quality

Files: Reciter.kt, provider catalogue/quality metadata, reciter/download selection, source validation tests/scripts.
- [x] Compare historic and live catalogues; preserve old IDs and restore missing valid recordings.
- [x] Inspect EveryAyah, MP3Quran and Quran Foundation primary sources; distinguish per-ayah files from full-surah audio/timing.
- [x] Add a localized searchable picker for the current per-ayah reciter catalogue; keep a stable ID for users with saved selections.
- [ ] Add other verified sources and playback scopes; do not treat full-surah streams as per-ayah audio.
- [ ] Validate every URL/content type/audio bitrate with provenance rather than label-only quality claims; preserve resumable/offline download behavior.

## Task 3: Tafsir follow, Quran search and content languages

Files: QuranPrefsRepository.kt, QuranReaderViewModel.kt, QuranReaderSettingsSheet.kt, QuranSupplementRepository, Quran search UI/domain.
- [x] Persist follow-recited-ayah (default on when supplement is enabled), resolve playback ayah independently, expose off switch.
- [x] Add offline Arabic Quran search with tashkeel/alef normalization, all-words and exact-phrase modes, occurrence and ayah totals, and direct navigation to each matching ayah. Translation-language search remains open until attributed translation corpora are available.
- [ ] Expand provider-backed translations with language/translator/source selection, downloads and offline attribution.

## Task 4: Prayer preferences, notifications and monthly screen

Files: PrayerSettings.kt/repository, PrayerSettingsScreen.kt, quiet-hours prefs, Adhan/NextAdhan notifications/receiver, HomeScreen/navigation.
- [x] Verify quiet hours default 22:00–06:00 at feature activation without replacing saved times (`NotificationPrefsRepositoryTest`).
- [x] Default global Adhan volume on for unset preferences; preserve explicit per-prayer overrides (`PrayerSettingsVolumeTest`).
- [x] Replace exposed long sound list with a compact popup selector and previews.
- [x] Cancel approaching-Adhan reminder on actual Adhan dispatch and playback-service start, including recovery/fallback paths (`AdhanAlarmReceiver`, `AdhanPlaybackService`, lifecycle tests).
- [x] Keep prayer-times notification ongoing with a custom expanded layout. Android System UI still controls user-forced collapse and channel-level notification permissions.
- [x] Move monthly timetable to an independent destination with complete chronological table and RTL/sticky header.
- [x] Remove duplicated lower compass Kaaba emoji.

## Task 5: Car, Cast and watch integration

Files: existing media browser/session, automotive preference/screen metadata, Cast sender and receiver, Wear resource icons.
- [x] Extend Android Auto browsing with all curated reciter folders and surahs, preferring complete offline audio and streaming missing ayahs directly over HTTPS through the shared Quran player queue; include legacy media IDs, Arabic Quran text search, saved bookmarks, and resumable queue/repeat state.
- [x] Inspect Alaa91H/QuranLiveStream receiver/layout contracts; its Amiri and Cairo font subsets now include their SIL OFL 1.1 notices and source attribution (`QuranLiveStream` commit `6802aeb`).
- [ ] Add Cast playback payload with ayah, selected translation, Arabic/English tafsir and configurable world prayer locations; synchronize playback and settings.
- [x] Build the custom CAF receiver and configurable real receiver ID path; actual Console registration and HTTPS deployment remain external setup.
- [x] Match the Wear launcher artwork to the phone launcher using a size-optimized copy of the phone foreground asset. A Wear notification icon path is not present in the current module.
- [ ] Separate tested sender/receiver contracts from unverified real car/watch/Cast behavior.

## Task 6: Localization and strict tag-based CI

Files: scripts/localize.py/resource audit, language settings, Gradle versioning, workflows and quality configs.
- [ ] Audit all locales, missing/fallback strings, hardcoded UI text and placeholders; record translation review/provenance.
- [ ] Complete supported language coverage and detailed strings using reliable content resources; keep religious translations attributed.
- [x] Harden localization source-copy detection against punctuation/diacritic-only changes and Arabic baseline fallbacks while ignoring Android format tokens; add regression coverage.
- [ ] Audit tag-derived versions and release gates, enable strict actionable warning/error checks without regenerating/silencing findings automatically.
- [ ] Pass build/unit/lint/Detekt/instrumentation/visual contracts, push and monitor CI; fix actual failures.
- [ ] Complete reviewed screenshot baselines and comparison gate, integrate branches into main, delete only merged branches and verify main CI.

## Current evidence (2026-09-30)

- `52270729` established the UI/UX V2 integration baseline. `e8ac775f` added Mushaf global-page navigation, monthly prayer screen, popup Adhan selection, tafsir follow controls, restored two verified EveryAyah entries, Qibla emoji removal, and CI emulator batching. `f463eb22` fixed the missing test-tag import. `5cb37341` preserved per-prayer audio overrides while defaulting the persisted master-volume preference to enabled.
- On CI run `36756348324`, APK assembly and unit tests passed; Family Life emulator jobs passed on API 26 and 36. Lint failed because the two new tafsir-follow strings were missing from 153 existing Quran locale files. Emulator jobs were still running when this plan was updated; inspect their results before claiming completion.
- On CI run `36759111996`, Quality (APK, unit tests, lint, Detekt) and Family Life emulator jobs passed. Both prayer/Qibla emulator jobs lost ADB during the app-wide UI screenshot matrix; API 36 failed while running `UiUxV2MatrixInstrumentedTest` with `AdbCommandRejectedException: device offline`. The capture script now returns failure on device loss and skips app-regression reruns, preserving first-failure diagnostics.
- Local verification for Android Auto update: `:feature:feature-quran:testDebugUnitTest` passed (120 tests, 1 skipped); `:app:lintDebug` passed; Android app/resources compiled through lint; `bash -n scripts/capture_uiux_v2_artifacts.sh` and `scripts/verify_uiux_v2_qa_matrix.py` passed. This is emulator/build verification, not real Android Auto vehicle validation.
- Quran search local verification: `:feature:feature-quran:testDebugUnitTest` and `:feature:feature-quran:lintDebug` passed; the new search suite reports 5 tests, 0 failures. `:wear:assembleDebug` passed with the independent Wear icon resource.
- Direct local Detekt analyzed 609 Kotlin files with zero findings. Quran/prayer static contracts and three visual-comparison policy tests passed. The global `scripts/localize.py --check` reports extensive pre-existing missing-resource and placeholder problems across unrelated modules; do not present it as a Quran-only check.
- No physical Android phone is available (user confirmed). Physical TalkBack/performance/fold/car/watch/Cast evidence remains an explicit acceptance gate.
- Local verification on `ed2da577`: Quran, notifications, datastore, core Cast, and app unit-test suites plus `:app:compileDebugKotlin` passed (`BUILD SUCCESSFUL`, 486 actionable tasks; Android SDK configured locally).
- CI run `37105383786` on `ed2da577`: Family Life emulator tests passed on API 26 and 36; prayer/Qibla emulator tests passed on API 26 and 36, including the Arabic/English pager and book-page instrumentation. Quality stopped at the existing strict localization completeness gate before build/unit/lint/Detekt. API 36 ADB briefly reported offline, recovered, and completed its emulator job successfully.
- Cast provenance and licensing follow-up: QuranLiveStream `npm test` passed all five repository quality gates and receiver protocol/state tests; commit `6802aeb` adds the Amiri/Cairo SIL OFL 1.1 texts and attribution alongside the bundled subsets.
- Offline Cast security regression: a new `LocalCastMediaServerTest` failed at the exact TTL boundary, exposing an inclusive-expiry bug. Expiry now rejects requests at `elapsed >= ttl`; the focused Cast server tests pass after the fix.
- Android Auto streaming follow-up: Android Auto now lists the full curated reciter/surah catalogue, uses downloaded files when present and HTTPS EveryAyah URLs otherwise, including bookmarks/search and resumable sessions. `:feature:feature-quran:testDebugUnitTest`, `:feature:feature-quran:compileDebugKotlin`, and `:feature:feature-quran:lintDebug` passed; the new shared-queue URL-preservation test passed. Live vehicle and network-playback validation remains unverified.
- Monthly prayer performance follow-up: moved month-grid generation out of the once-per-second prayer countdown recomposition path. The grid now rebuilds only when prayer settings/location or the visible month changes; `MonthlyPeriodFlowTest` covers month-only invalidation. Prayer-times unit tests, Kotlin compilation, and Android lint pass locally.
- CI run `37109239879` on `08c990f7`: all four Family Life and prayer/Qibla API 26/36 emulator jobs passed. Quality failed at the existing localization gate (`MISSING=111253`, `PLACEHOLDER=231`, `UNTRANSLATED=9380`), so build/unit/lint/Detekt and release-artifact jobs were skipped.
- Live reciter-source audit on 2026-09-30: the app has 46 EveryAyah per-ayah choices. Its upstream `recitations.js` lists 79 entries, with the unused entries appearing to be lower-bitrate copies or alternate upload labels, so they are not added as duplicate reciters. The live EveryAyah folder test exposed a dead Ibrahim Akhdar 64kbps folder; the 32kbps folder returns MP3 for 001001, 002286 and 114006, and the saved catalogue ID is preserved when updating its source.
- MP3Quran full-surah groundwork on 2026-10-03: the v3 reciter API exposes recording variants and per-recording surah availability. Muslim now has a separate secure parser/client and HTTP source probe; live HEAD checks returned `audio/mpeg`, positive lengths, and byte ranges for 001, 002, and 114 on server6 plus surah 018 on server11. Bitrate is not supplied by this API and is not inferred. The shared playback/session integration and UI remain open; see `docs/quran-recitation-sources.md`.
- MP3Quran's live API returned 241 reciters, including 154 complete Hafs moshafs; these are full-surah streams and cannot be placed in the existing ayah queue without breaking ayah sync. Treat them as a separate provider/playback-scope project. `QuranLiveStream` is a standalone broadcast server; the inspected repository does not itself supply a ready-made Android Cast receiver.
- PR `#110` CI run `37120194523`: all four Family Life and prayer/Qibla emulator jobs passed on API 26 and 36. Quality failed at localization completeness (`MISSING=111253`, `MIXED_LANGUAGE=32`, `PLACEHOLDER=77`, `UNTRANSLATED=9380`); build, unit tests, lint, Detekt, and release jobs were skipped. Local duplicate-source synchronization reused 245 unambiguous, placeholder-compatible translations, reducing the local gate to 120497 findings. Google gtx returned HTTP 429 during a Quran-only missing-value draft run; the run was stopped before it wrote resource files. Translation drafts still require fluent review before release. A shared 0.8-second provider request throttle now has unit coverage; rerun the provider job after cooldown and verify the strict gate again.
- Cast payload construction is now extracted from `QuranReaderScreen` into `QuranCastMapper` with JVM coverage for active ayah, selected translation, ayah-scoped tafsir, queue/repeat/position and prayer metadata. `:feature:feature-quran:testDebugUnitTest`, `:feature:feature-quran:lintDebug`, and `detekt` passed locally; this is state-mapping verification, not real Cast hardware validation.
- Translation provider cooldown follow-up: HTTP 429 and transient provider backoff are now shared by all locale workers through the request throttle, while honoring numeric `Retry-After` values. `python -m unittest discover -s scripts/tests -p test_localize.py -v` passes (24 tests). This reduces concurrent request amplification but does not provide translations or clear the current completeness gate; the provider still needs a reliable quota/source and human review.
- Mushaf paging CI follow-up on PR #110 (`37123623047`): API 26 navigation/emulator tests passed; API 36's isolated pager-direction test passed, but the full Quran reader page-round-trip test failed for Arabic and English after the injected drag did not cross the pager snap threshold consistently. Re-running API 36 reproduced the issue. Increased the end-to-end test drag span from 56% to 64% of display width while keeping both touch points away from Android's edge-back zones; `:app:compileDebugAndroidTestKotlin` succeeds locally. A fresh CI device run is required to determine whether the larger gesture resolves the API 36 failure.
- Localization checker follow-up: added Unicode-normalized source-copy detection that ignores punctuation, diacritics, and Android format tokens, and checks copied Arabic baseline text in non-Arabic locales. All 27 `scripts.tests.test_localize` tests pass. The Quran-only audit now reports `MISSING=2710`, `UNTRANSLATED=1094`, and `UNTRANSLATED_BASE=161`; the strict release gate remains failing, and machine-generated drafts were discarded after review found untranslated/unsafe values.
- Mushaf gesture follow-up on PR #110 (`37125639839`): the first API 36 attempt lost its emulator during a UI matrix batch; after a failed-job rerun, API 36 completed successfully. XML reports confirm both Arabic mirrored page-round-trip and English left-to-right page-round-trip tests passed, along with both focused pager-direction tests. API 26 and both Family Life emulator jobs also passed. Quality remains blocked at localization completeness before build/unit/lint/Detekt/release jobs.
