# Muslim

> **One name everywhere.** *Muslim* is the official name of the project, the application package, and the app itself. The launcher label is translated automatically with the display language (190+ locales).

**Muslim** is a free, open-source, privacy-first Android application that accompanies a Muslim throughout the day — from astronomically accurate prayer times and the Adhan to the Quran, hadith, adhkar, electronic tasbih, Ramadan tools, and a zakat calculator — all in one beautifully designed, fully offline-capable app.

- **Free forever** — no ads, no subscriptions, no in-app purchases
- **Open source** (GPLv3) — the code is public and auditable
- **Privacy first** — no tracking, no analytics SDKs, no accounts; your data stays on your device
- **Offline-first** — the entire app works without an internet connection
- **World-ready** — the whole interface is translated into 190+ languages

> 📄 **Project reference:** [`PROJECT_PROMPT.md`](PROJECT_PROMPT.md) is the single source of truth for the vision, architecture, design system, roadmap, and the accuracy and religious-content standards.

---

## ✨ Features

### 🕌 Prayer Times & Adhan
- Astronomically accurate prayer times via a **pure-Kotlin astronomical engine** (solar declination & equation of time), verified against the [Adhan](https://github.com/batoulapps/Adhan-Kotlin) library
- **All major calculation methods**: Umm al-Qura (Makkah), Egyptian General Authority, ISNA, MWL, Karachi, Tehran, Jafari, and an **Automatic method** that picks the best fit for your location
- **Juristic school support** for Asr (Shafi'i/standard vs. Hanafi), plus high-latitude rules (Midnight, One-Seventh, Angle-based) for the far north
- Manual & automatic location (GPS), elevation-aware calculations, DST-safe
- **The Adhan itself**: 18+ high-quality bundled adhan sounds (including Makkah, Madinah, and the classic Madinah 1952 recording), with per-prayer sound selection, volume, vibration, and preview
- Exact alarm scheduling with a foreground-service fallback so the Adhan always rings
- Optional silent mode during prayer, pre-adhan reminders, and a **persistent countdown notification** (next prayer + time remaining, updated live)
- Hijri calendar with automatic and manual adjustment

### 🧭 Qibla
- Precise compass using the device sensors + GPS
- Map view with your position, the Kaaba, and the great-circle route
- Kaaba emoji marker 🕋, live heading degrees, and haptic/sound feedback when you face the qibla

### 📖 Quran
- Complete Quran in **Uthmani script** (6236 ayahs) with per-surah metadata, juz, page, and ayah positions
- Professional reader: adjustable font size, comfortable reading themes, night mode, bookmarking, last-read position
- **Recitations**: 44+ renowned reciters, audio playback with per-ayah highlighting, repeat modes (single ayah / to end of surah / continuous through the whole Quran), playback speed, background downloading with resume, and a mini player
- **Tafsir & translations** in multiple languages, searchable
- **Word search** (FTS) with occurrence counts and locations
- **Linguistic frequency**: the most repeated words in the entire Quran
- Ayah of the day, bookmarks, and reading progress

### 📚 Hadith & Learning
- Curated hadith library — Arba'in an-Nawawiyyah and famous hadiths of the Six Books — with full-text search
- **Complete Six Books import** script from a licensed open source
- Hadith of the day (WorkManager-scheduled notification)
- Step-by-step learning guides: wudu, ghusl, tayammum, prayer, special prayers, rak'ah tables, and madhhab differences
- Reference library (99 Names of Allah, stories of the prophets, Islamic history, and more)

### 📿 Adhkar, Tasbih & Worship
- Sourced adhkar (morning/evening, sleep, waking, prayer, travel…) with persistent counters
- **Floating bubble reminders** over any app, with customizable interval, display duration, and short-dhikr-only mode
- Electronic **tasbih** with haptic feedback, daily/weekly logs, charts, and a home-screen widget
- Ramadan: suhoor/iftar countdowns, exact alerts (Iftar & Suhoor toggles with Ramadan-aware default), fasting tracker, and automatic Hijri adjustment
- Zakat: zakat al-mal (nisab + debt deduction), zakat al-fitr, and a yearly log

### 🗺️ Maps & More
- **Offline maps** (OpenStreetMap via MapLibre GL — no API key): download cities, countries, or a **custom area** with an interactive pan/zoom picker and a live size estimate; smart storage management warns when space is low and suggests deleting the largest region
- **Nearby mosques** on an interactive map with distance, directions, and search expansion
- Unified **notification manager** with per-category toggles, quiet hours, and live previews
- Unified **permission manager** with one-tap onboarding
- **In-app update checker** (GitHub Releases) with changelog and one-tap download/install
- Settings for everything: theme, dynamic color, font size, time format (12/24h), start screen, language, and section order on the More screen

---

## 📱 Screens

The app uses four primary tabs (per Material guidance):

1. **Prayer Times** — today's times, next prayer countdown, Adhan status
2. **Quran** — the mushaf reader, search, frequency, bookmarks
3. **Qibla** — compass and map
4. **More** — settings, hadith, adhkar, tasbih, Ramadan, zakat, learning, reference library, downloads, offline maps, and more

---

## 🏗️ Technical architecture

- **100% Kotlin + Jetpack Compose** — no XML views
- **Material 3 Expressive** — Dynamic Color (Material You) with manual fallback palettes
- **Clean Architecture** — presentation ← domain ← data, with MVI/MVVM and `StateFlow`/`SharedFlow`
- **Hilt** for dependency injection, **Coroutines + Flow** for async
- **Room** (preloaded data) + **DataStore** (preferences)
- **AlarmManager** (exact background Adhan) + **WorkManager** (non-critical tasks)
- **Glance** (home-screen widgets)
- **Retrofit/OkHttp/kotlinx.serialization** for optional networking only

### Modules (22)

```
app
core: common · ui · database · datastore · design-system · network · notifications · location · permissions
feature: prayer-times · qibla · quran · hadith · adhkar · tasbih · learn · ramadan · zakat · reference · settings
```

Each feature module depends only on core modules — never on another feature module.

---

## 🌍 Localization

Every user-facing string lives in its own `values-XX/strings.xml` for **190+ world languages**, generated by a machine-translation pipeline (`scripts/localize.py`) that protects Android format specifiers (`%1$s`, `%2$d`, `%%`, `\n`) and XML entities. The app name is also translated per display language. Right-to-left (Arabic, Hebrew, Urdu…) and left-to-right layouts are fully supported.

---

## 🛠️ Development environment (2026)

| Tool | Version |
|---|---|
| Android Gradle Plugin | 9.3.1 (bundled Kotlin — no `kotlin-android` plugin) |
| Gradle | 9.5.0 |
| Kotlin (bundled in AGP) | 2.2.10 |
| KSP | 2.2.10-2.0.2 |
| Compose BOM | 2026.08.00 |
| compileSdk / targetSdk | 37 (Android 17) |
| minSdk | 26 (Android 8) |
| JDK | 17 |

> All versions are centralized in `gradle/libs.versions.toml`.

## Building

```bash
# Requires: JDK 17, Android SDK (platform 37) — path in local.properties
./gradlew :app:assembleDebug     # debug APK
./gradlew :app:assembleRelease   # release APK (R8 + signing)
./gradlew testDebugUnitTest      # unit tests
./gradlew lintDebug              # lint
./gradlew :app:installDebug      # install on a connected device/emulator
```

## 🔖 Versioning — tied to git tags (never hardcoded)

`versionName` and `versionCode` are derived at build time from the nearest `v*` git tag (`git describe` in `app/build.gradle.kts`). There is no hardcoded version anywhere.

To publish a release the "tag-first" way (so the APK version always matches the tag):

```bash
./scripts/release.sh            # bumps the patch (v1.2.0 -> v1.3.0)
./scripts/release.sh 2.0.0      # explicit version
```

`release.sh` is **fully automatic**: it commits the working tree, generates a changelog from the previous tag, tags and pushes, waits for the exact tag-triggered CI run, downloads and verifies the signed APK, and creates the GitHub Release with the changelog attached — no manual steps.

## 🔐 Release signing — stable key for install-over updates

Every release is signed with the same key so updates install over the existing install without uninstalling first:

```bash
./scripts/create-signing-keystore.sh   # creates release.keystore + keystore.properties automatically
./scripts/setup-github-signing.sh      # uploads the same key to GitHub Actions secrets (once)
```

- **Locally:** signing is read from `keystore.properties` (git-ignored).
- **In CI:** read from the `SIGNING_KEYSTORE` (Base64), `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, and `SIGNING_KEY_PASSWORD` secrets — the same key, so CI produces an APK with the same local signature.
- Without either source, the debug key is used automatically (installable APK for testing).

## 📦 Installation

Download the signed APK from the [Releases](https://github.com/Alaa91H/Muslim/releases) page and open it (allow "install from unknown sources" if prompted). Because every release keeps the same signing key, new versions install directly over previous ones — your data and settings are preserved.

## Content sources

- **Quran text:** Uthmani script from the [Tanzil](https://tanzil.net/) project (via the alquran.cloud dataset) — 6236 ayahs with per-surah metadata (name, revelation type) and ayah positions (juz/page).
- **Prayer-time algorithms:** ported and verified against the [Adhan](https://github.com/batoulapps/Adhan-Kotlin) library (MIT, attributed).
- **Hadith corpus:** curated sample (Arba'in an-Nawawiyyah + famous hadiths of the Six Books); the **complete Six Books** can be imported with

  ```bash
  python scripts/import-hadith.py                     # all six books
  python scripts/import-hadith.py --books bukhari,muslim --limit 25   # smoke test
  python scripts/import-hadith.py --self-check       # verify the dedupe logic
  ```

  The importer fetches from the licensed, open [hadith-api](https://github.com/fawazahmed0/hadith-api) project (MIT-licensed API; classical Arabic texts are public domain; translations keep their original copyrights) via the jsDelivr CDN, maps hadiths into the app schema, and **deduplicates by fingerprinting the diacritic-normalized Arabic text** — the output file is re-verified to contain zero duplicates before it is written.

- **Maps:** OpenStreetMap data via [MapLibre GL Native](https://maplibre.org) + [OpenFreeMap](https://openfreemap.org) vector tiles (free, no API key).

Religious content (Quran text, hadith and their grading, adhkar, rulings) is subject to specialist religious review, separate from code review, before the official release.

## 🤝 Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) and [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

Important: religious content is reviewed separately from code — see section 10 of `PROJECT_PROMPT.md`.

## 📄 Privacy

See [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md) — no tracking, no data collection, everything stays on your device.

## Contact & support

- **GitHub:** [github.com/Alaa91H](https://github.com/Alaa91H)
- **Email:** [alahus2591@gmail.com](mailto:alahus2591@gmail.com)
- **Telegram:** [t.me/Alaa91h](https://t.me/Alaa91h)
- **Support development:** [ko-fi.com/alaa91h](https://ko-fi.com/alaa91h)

## License

[GPLv3](LICENSE) — any modified version of the app stays open source.
