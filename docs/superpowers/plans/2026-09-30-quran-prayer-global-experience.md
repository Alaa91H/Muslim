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
- [ ] Load the complete offline Mushaf once and group all ayahs by canonical page.
- [ ] Initialize at the requested surah/ayah, preserve page order across surahs and complete shared pages.
- [ ] Remove asynchronous previous-surah edge chaining; make metadata/ayah loading atomic.
- [ ] Cover Aal Imran → previous page, first/last page and wide spreads; verify in CI.

## Task 2: Reciters and verified quality

Files: Reciter.kt, provider catalogue/quality metadata, reciter/download selection, source validation tests/scripts.
- [ ] Compare historic and live catalogues; preserve old IDs and restore missing valid recordings.
- [ ] Inspect EveryAyah, MP3Quran and Quran Foundation primary sources; distinguish per-ayah files from full-surah audio/timing.
- [ ] Offer broad searchable localized reciter selection, styles, available scope and highest verified source quality.
- [ ] Validate URLs/content type/audio bitrate with provenance rather than label-only quality claims; preserve resumable/offline download behavior.

## Task 3: Tafsir follow, Quran search and content languages

Files: QuranPrefsRepository.kt, QuranReaderViewModel.kt, QuranReaderSettingsSheet.kt, QuranSupplementRepository, Quran search UI/domain.
- [ ] Persist follow-recited-ayah (default on when supplement is enabled), resolve playback ayah independently, expose off switch.
- [ ] Support accurate match/occurrence totals, normalized Arabic search, exact/word modes and usable navigation to results.
- [ ] Expand provider-backed translations with language/translator/source selection, downloads and offline attribution.

## Task 4: Prayer preferences, notifications and monthly screen

Files: PrayerSettings.kt/repository, PrayerSettingsScreen.kt, quiet-hours prefs, Adhan/NextAdhan notifications/receiver, HomeScreen/navigation.
- [ ] Verify quiet hours default 22:00–06:00 at feature activation without replacing saved times.
- [ ] Default global Adhan volume on for unset preferences; preserve explicit per-prayer overrides.
- [ ] Replace exposed long sound list with searchable modal selector and previews.
- [ ] Cancel approaching-Adhan reminder on every actual Adhan dispatch path, including silent/fallback paths.
- [ ] Keep prayer-times notification ongoing with an expanded custom layout; document OS-controlled dismissal/expansion behavior accurately.
- [ ] Move monthly timetable to an independent destination with complete chronological table and RTL/sticky header.
- [ ] Remove duplicated lower compass Kaaba emoji.

## Task 5: Car, Cast and watch integration

Files: existing media browser/session, automotive preference/screen metadata, Cast sender and receiver, Wear resource icons.
- [ ] Audit and extend Android Auto browsing, downloaded/streaming playback, reciter/favorite/resume customization and safe error handling.
- [ ] Inspect Alaa91H/QuranLiveStream display/source contracts and reuse with attribution/license compatibility.
- [ ] Add Cast playback payload with ayah, selected translation, Arabic/English tafsir and configurable world prayer locations; synchronize playback and settings.
- [ ] Build custom receiver; expose registered receiver ID configuration where external registration is required, never invent credentials/IDs.
- [ ] Make Wear launcher and notification assets match main app.
- [ ] Separate tested sender/receiver contracts from unverified real car/watch/Cast behavior.

## Task 6: Localization and strict tag-based CI

Files: scripts/localize.py/resource audit, language settings, Gradle versioning, workflows and quality configs.
- [ ] Audit all locales, missing/fallback strings, hardcoded UI text and placeholders; record translation review/provenance.
- [ ] Complete supported language coverage and detailed strings using reliable content resources; keep religious translations attributed.
- [ ] Audit tag-derived versions and release gates, enable strict actionable warning/error checks without regenerating/silencing findings automatically.
- [ ] Pass build/unit/lint/Detekt/instrumentation/visual contracts, push and monitor CI; fix actual failures.
- [ ] Complete reviewed screenshot baselines and comparison gate, integrate branches into main, delete only merged branches and verify main CI.

## Current evidence

- 52270729 pushed; CI 36746288974 in progress. Local direct UTF-8 Detekt analyzed 604 files successfully; 28 static checks and three visual comparison policy tests passed.
- Fresh source review confirms Quran sheet scrolling fix; runtime tests await CI.
- No physical phone is available (user confirmed). Physical TalkBack/performance/fold/car/watch/Cast evidence remains an explicit acceptance gate.
