# Muslim — Completion Plan (working document)

> **Last inspected:** 20 August 2026 — **status updated:** 20 August 2026
> **Document type:** operational implementation plan (complements `PROJECT_PROMPT.md`).
> Every item carries: goal, steps, acceptance criteria, and dependencies. ✅ marks are updated with every real completion.

---

## 0. Executive summary

**Muslim** is a multi-module Islamic app (22 Gradle modules) built with Clean Architecture + Hilt + Compose. Build, unit-test, lint, Detekt, and emulator results must be confirmed from the current CI run before describing a commit as green. Interface translations are available across many locale catalogs; the full localization audit reports an existing coverage backlog. Production APK releases are tag-driven and signed with the repository's stable key.

The single `.github/workflows/ci.yml` workflow builds signed phone and Wear APKs on ordinary runs. Only a `vMAJOR.MINOR.PATCH` tag that passes all required gates publishes versioned APK assets to a GitHub Release.

---

## 1. Current status (20 August 2026)

### 1.1 Automated verification

| Check | Result |
|---|---|
| `./gradlew :app:assembleDebug` | ✅ BUILD SUCCESSFUL |
| `./gradlew :app:assembleRelease` (R8 + signing) | ✅ Signed APK (`app/build/outputs/apk/release/app-release.apk`, CN=Muslim) |
| `./gradlew testDebugUnitTest` | ✅ All unit tests green |
| `./gradlew lintDebug` (whole app) | ✅ 0 issues |
| CI (GitHub Actions) | Single workflow builds signed APKs on each run and publishes only versioned APKs for validated tags |

### 1.2 Module map

| Module | Status |
|---|---|
| `app` | ✅ 4 tabs: Prayer Times · Quran · Qibla · More (settings & every secondary feature under More) |
| `feature-prayer-times` + `feature-qibla` | ✅ Prayer times (all methods, auto-detection, juristic Asr, high latitudes, elevation), exact Adhan (18+ bundled sounds, per-prayer customization), persistent countdown notification, Qibla compass + map + GPS |
| `feature-quran` | ✅ Uthmani text + reader (font/theme/night/translation/tafsir) + FTS word search + linguistic frequency + recitations (44 reciters, repeat modes, downloads, resume, background) + bookmarks + ayah of the day |
| `feature-hadith` | ✅ Library + FTS search + hadith of the day + bookmarks + share (curated sample; complete Six Books import via script) |
| `feature-adhkar` | ✅ Sourced adhkar with counters + floating bubble reminders (interval, duration, short-only mode) + categories |
| `feature-tasbih` | ✅ Electronic misbaha (vibration, goals, 30-day log, chart) + widget |
| `feature-ramadan` | ✅ Suhoor/iftar countdown + exact alerts (Iftar/Suhoor toggles, Ramadan-aware by default) + fasting tracker + Hijri adjustment |
| `feature-zakat` | ✅ Zakat al-mal (nisab + debt deduction) + zakat al-fitr + yearly log |
| `feature-learn` | ✅ Wudu/ghusl/tayammum/prayer + special prayers + rak'ah tables + madhhab differences |
| `feature-reference` | ✅ Reference library (99 Names of Allah, stories of the prophets, Islamic history, Hajj & Umrah guide with checklists, and more) |
| `feature-settings` | ✅ Settings hub (theme, language, start screen, time format, prayer/adhan, More-screen order) + unified notification manager + unified permission manager + About + Privacy + in-app update checker |
| `core-*` | ✅ All core modules working — prayer engine in `core-common/prayer`, map stack in `core-ui/map`, notifications in `core-notifications`, permissions in `core-permissions` |

### 1.3 Recently completed items (this session)

1. **Offline maps, interactive custom picker & storage management** — download cities/countries/custom areas; interactive pan/zoom picker with a live bounds rectangle, width slider, and real-time size estimate; StatFs-based low-storage warning with a delete-largest-region action.
2. **Tag-gated APK delivery** — `.github/workflows/ci.yml` runs verification and signed APK builds; validated version tags publish only the phone and Wear APK files after remote asset verification.
3. **Interactive qibla compass + GPS + mosque finder on MapLibre** — Kaaba marker 🕋, live degrees, haptic/sound alignment feedback, mosque markers with info windows, and find-nearest expansion.
4. **Recitation playback as system media** — MediaSession, media notification (play/pause/next), audio-focus handling, pause-on-notifications, and continuous surah-to-surah playback to the end of the Quran.
5. **Unified notification manager & permission manager** — per-category toggles, quiet hours, live previews, channel status; one-tap permission onboarding.
6. **In-app update checker** — daily/weekly/monthly check against GitHub Releases, changelog + size, download via DownloadManager, install via the system installer.
7. **World localization** — locale catalogs are generated with format-specifier-safe translation tooling (`scripts/localize.py`); coverage and translation-review gaps remain visible in the full localization audit.

---

## 2. Remaining (by priority)

### Active tasks — reported 26 August 2026

| Priority | Task | Scope and acceptance criteria |
|---|---|---|
| P0 | **Restore reliable Adhan and notification delivery on Android 17** | Trace the complete path: permission state, notification channels, exact-alarm capability, alarm scheduling, broadcast delivery, foreground-service start, audio focus and playback. Remove the 15-second delay from the in-app test path or present an accurate, immediate diagnostic. Verify on an API 37/Android 17 emulator or device with automated coverage for both the normal and denied-permission paths. |
| P0 | **Whole-project quality audit and zero-known-regressions loop** | Audit every module, manifest declaration, navigation route, background path, resource, localisation, test and build variant. Record reproducible defects, fix their root causes and add regression tests. Run a repeatable quality gate (compile, unit tests, lint, static checks and device tests where available); “zero errors” means zero findings in that defined gate, not an unprovable infinite claim. |
| P0 | **Verified GitHub delivery and release** | Push only the verified project state to `Alaa91H/Muslim`, monitor the exact commit's GitHub Actions run, resolve reproducible CI failures, then create and push a semantically versioned tag and GitHub Release with a professional English changelog. Do not publish a release until the defined quality gate and CI are green. |
| P1 | **World-class modern Islamic-app UX/UI programme** | Establish an accessible, coherent design system and audit every screen, state and setting for hierarchy, discovery, responsiveness, RTL/localisation, dark mode, typography and Android large-screen support. Deliver the redesign incrementally with visual/regression verification, without sacrificing the existing religious functionality. |

The Adhan reliability task is the current first implementation priority because it affects a core religious function.

### P1 — Religious/technical completions

| Item | Description | Size |
|---|---|---|
| Full Six Books + Riyad as-Saliheen + Arba'in bundled | Generate the DB from a licensed source and ship it (currently a curated sample + import script) | XL |
| Tajweed colorization in the reader | Color-coded tajweed rules for correct reading | M |
| Word-by-word translation | Per-word meaning in the reader | L |
| Nisab auto-refresh (gold/silver) | Optional network fetch with manual override kept | M |
| Last-ten-nights & Laylat al-Qadr alerts | Seasonal notifications in Ramadan | S |

### P2 — Expansion

- Community translation platform (Weblate/Crowdin).
- Kids mode (simplified learning).
- Share hadith as a designed image.
- Wear OS companion (tasbih + next-prayer countdown).
- Android Auto (adhkar + recitations while driving).
- Multi-family profiles and backup/restore.

### P0 — Before final store launch

| Item | Status |
|---|---|
| Privacy policy in the repo + in-app | ✅ Done (`PRIVACY_POLICY.md` + Privacy screen) |
| Release signing + R8 | ✅ Done (stable key, `create-signing-keystore.sh` + `setup-github-signing.sh`) |
| Final package name registration | ⬜ At actual store registration |
| Specialist religious review (Quran, hadith, adhkar, rulings) | ⬜ Independent review channel |
| Manual testing on real devices (API 26 and 37) | ⬜ Requires device/emulator |
| Community translation platform | ⬜ Planned |

---

## 3. Risks & recommendations (living list)

| Risk | Recommendation |
|---|---|
| R8 may strip future reflective paths | Run `./gradlew :app:analyzeReleaseR8Config` when adding reflection-based features |
| Android 13+ permission & exact-alarm restrictions | Test on modern devices + transparent guidance card |
| Huge religious datasets | Import/generation tooling + automated review |
| APK size | R8 enabled; heavy content downloadable on demand; recitations streamed/downloaded |
| Content licensing (Tanzil, recitations, tafsir) | Document every source + comply with its terms |

---

*This document is an operational plan; `PROJECT_PROMPT.md` remains the vision/architecture reference and the final source of truth.*

### 2026-10-04 continuation — Quran Cast supplement synchronization

- Cast now observes locally installed translation and tafsir for the currently playing ayah, independent of the reader's manual tafsir cursor or source filter. It sends only the selected translation language plus Arabic and English tafsir when Quran supplements are enabled.
- Tafsir language tags now prefer the validated pack language metadata, and receiver source labels include available title, translator, and publisher details.
- Verification: `:feature:feature-quran:testDebugUnitTest` passed locally (111 tasks, 1 executed; all feature tests). Targeted Android lint was started but manually stopped after analysis stalled under high JVM memory use; it is not counted as passed. Detekt passed locally across 665 Kotlin files; Android lint remains unverified locally and the full CI quality job is the authoritative gate after push.
- Translation pack completeness and Cast hardware testing remain outstanding; this increment does not change release readiness.

### 2026-10-04 continuation — Android 17 Adhan verification gate

- Added a targeted API 37 Android 17 emulator job to the single CI workflow. It runs the real AlarmManager delivery probe through the manifest receiver and foreground playback service, while leaving the API 26/36 UI matrix unchanged.
- The APK-release contract checker now requires this Android 17 job as a prerequisite for signed APK builds. Static lifecycle checks and the release-contract checker pass locally; the new API 37 runner remains unverified until GitHub Actions completes.
- No version tag was created: the release-only Quran text gate currently reports 0 complete packs and 375 required language/type gaps across 188 supported UI languages.

### 2026-10-04 continuation — Android 17 emulator package correction

- The first API 37 CI attempt failed before emulator boot because the runner tried to install platforms;android-37, which is not the preview package name. No Adhan delivery assertion ran in that attempt.
- Updated the targeted job to request the stable API 36 platform while explicitly selecting Android 17's 37.0 system image (channel: beta); the job still asserts the booted device reports SDK 37 before executing the delivery probe.
- python scripts/verify_ci_apk_release.py and git diff --check pass for this change. Await exact-runner CI confirmation before treating Android 17 as verified.
- Release remains blocked by the audited absence of validated Quran translation/tafsir packs (0 complete packs; 375 coverage gaps across 188 supported UI languages); no release tag should be created until that gate is satisfied.

### 2026-10-04 continuation — Android 17 image input and Arabic page-turn regression

- CI confirmed the emulator runner receives an unquoted YAML decimal `37.0` as `37`, then looks for the nonexistent `system-images;android-37;default;x86_64`. The image input is now quoted as a string (`"37.0"`) and the release contract verifier requires that exact value.
- Quality, both Family Life emulator jobs, and API 26 prayer/Qibla tests passed on commit `0df863d7`; the API 36 reader instrumentation test failed its Arabic right-to-left page round trip when returning from page 49 to page 50. Investigate/re-run before changing reader behavior.
- The API 37 probe did not execute because its preview system image package was not found. Keep the quality gate blocked until a runner configuration launches the actual SDK 37 image and the delivery test runs.
