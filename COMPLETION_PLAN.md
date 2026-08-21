# Muslim — Completion Plan (working document)

> **Last inspected:** 20 August 2026 — **status updated:** 20 August 2026
> **Document type:** operational implementation plan (complements `PROJECT_PROMPT.md`).
> Every item carries: goal, steps, acceptance criteria, and dependencies. ✅ marks are updated with every real completion.

---

## 0. Executive summary

**Muslim** is a multi-module Islamic app (22 Gradle modules) built with Clean Architecture + Hilt + Compose. The build is **green** (Debug and Release with R8), **all unit tests pass**, and **lint is clean (0 issues)**. All feature modules are implemented, wired into the UI, and localized into 190+ languages. Releases are tag-driven, signed with a stable key, and published automatically by `scripts/release.sh`.

**Bottom line:** the project is ready to install — `./gradlew :app:assembleRelease` produces a signed, shrunk APK; `./scripts/release.sh` takes it from commit to a published GitHub Release with no manual steps.

---

## 1. Current status (20 August 2026)

### 1.1 Automated verification

| Check | Result |
|---|---|
| `./gradlew :app:assembleDebug` | ✅ BUILD SUCCESSFUL |
| `./gradlew :app:assembleRelease` (R8 + signing) | ✅ Signed APK (`app/build/outputs/apk/release/app-release.apk`, CN=Muslim) |
| `./gradlew testDebugUnitTest` | ✅ All unit tests green |
| `./gradlew lintDebug` (whole app) | ✅ 0 issues |
| CI (GitHub Actions) | ✅ assemble + unit tests + lint per push; release-apk job builds the signed APK per tag |

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
2. **Fully automatic release script** (`scripts/release.sh`) — auto-commit → changelog → tag → push → wait for the exact tag-triggered CI run → APK signature verification → GitHub Release. No manual steps.
3. **Interactive qibla compass + GPS + mosque finder on MapLibre** — Kaaba marker 🕋, live degrees, haptic/sound alignment feedback, mosque markers with info windows, and find-nearest expansion.
4. **Recitation playback as system media** — MediaSession, media notification (play/pause/next), audio-focus handling, pause-on-notifications, and continuous surah-to-surah playback to the end of the Quran.
5. **Unified notification manager & permission manager** — per-category toggles, quiet hours, live previews, channel status; one-tap permission onboarding.
6. **In-app update checker** — daily/weekly/monthly check against GitHub Releases, changelog + size, download via DownloadManager, install via the system installer.
7. **World localization** — every module translated into 190+ languages with format-specifier-safe machine translation (`scripts/localize.py`).

---

## 2. Remaining (by priority)

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
