# Muslim — a complete, open-source Islamic app

> **Official name:** Muslim — one unified name for the project, the package and the app. The launcher label is translated automatically with the display language (185+ locales).

A complete, fully free and open-source Android app for every Muslim. It starts with astronomically accurate prayer times, the Adhan and Qibla, then grows to the Quran, hadith, adhkar, Ramadan, a zakat calculator and more.

- Free forever — no ads, no subscriptions
- Open source (GPLv3)
- Privacy first: no tracking, your data stays on your device
- Offline-first: works without the internet after initial setup

> 📄 **The comprehensive project reference:** read [`PROJECT_PROMPT.md`](PROJECT_PROMPT.md) — the single source of truth: vision, architecture, design system, roadmap (8 phases), and the accuracy and religious-content standards.

---

## Status

| Step | Status |
|---|---|
| Multi-module structure | ✅ Complete — builds successfully |
| Phase 1: Adhan, prayer times, Qibla, Hijri calendar | ✅ Complete — astronomical engine, Qibla (compass + map + distance), Hijri, exact Adhan scheduling, notifications, multi-size widgets, app shortcuts, export |
| Phase 2: Quran | ✅ Core complete — Uthmani text in Room, reader (font size / night mode / translation / tafsir), FTS search, bookmarks, last-read, downloadable recitations, ayah of the day |
| Phase 3: Hadith | ✅ Core complete — library + FTS search + hadith of the day + bookmarks + share |
| Phase 4: Adhkar & tasbih | ✅ Complete — sourced adhkar with counters, electronic misbaha (daily log + widget) |
| Phase 5: Learning | ✅ Core complete — wudu/ghusl/tayammum/prayer step-by-step + special prayers + rak'ah table + madhhab differences |
| Phase 6: Ramadan | ✅ Complete — suhoor/iftar countdown, exact alerts, fasting tracker, automatic Hijri adjustment |
| Phase 7: Zakat | ✅ Complete — zakat al-mal (nisab + debt deduction), zakat al-fitr, yearly log |
| Phases 8 & extras | 🔶 In progress — privacy policy + about/privacy screens |

> 🗺️ **Full completion plan:** see [`COMPLETION_PLAN.md`](COMPLETION_PLAN.md).

We do not move to a new phase until the current one is complete and tested on a real device.

## Content sources

- **Quran text:** Uthmani script from the [Tanzil](https://tanzil.net/) project (via the alquran.cloud dataset) — 6236 ayahs with per-surah metadata (name, revelation type) and ayah positions (juz/page).
- **Prayer-time algorithms:** ported and verified against the [Adhan](https://github.com/batoulapps/Adhan-Kotlin) library (MIT, attributed).

Religious content (Quran text, hadith and their grading, adhkar, rulings) is subject to specialist religious review, separate from code review, before the official release.

## Technical architecture

- **100% Kotlin + Jetpack Compose** (no XML views)
- **Material 3 Expressive** — Dynamic Color (Material You) with manual fallback palettes
- **Clean Architecture** — presentation ← domain ← data
- **MVI/MVVM** with `StateFlow`/`SharedFlow`
- **Hilt** for DI, **Coroutines + Flow** for async
- **Room** (preloaded data) + **DataStore** (preferences)
- **AlarmManager** (exact background Adhan) + **WorkManager** (non-critical tasks)
- **Glance** (home-screen widget for the next prayer with countdown, 3 sizes)
- **Retrofit/OkHttp/kotlinx.serialization** — for optional networking only

### Modules (18)

```
app · core-common · core-ui · core-database · core-datastore
core-network · core-notifications · core-location
feature-prayer-times · feature-qibla · feature-quran · feature-hadith
feature-adhkar · feature-tasbih · feature-learn · feature-ramadan
feature-zakat · feature-settings · feature-reference
```

Each feature module depends only on core modules, never on another feature module.

The five tabs: **Home · Quran · Times · Qibla · More** — More contains: settings, hadith, adhkar, tasbih, Ramadan, zakat, learning, the reference library and downloads.

## Development environment (2026)

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

## Versioning — tied to git tags (never hardcoded)

`versionName` and `versionCode` are derived at build time from the nearest `v*` git tag (`git describe` in `app/build.gradle.kts`). There is no hardcoded version anywhere.

To publish a release the "tag-first" way (so the APK version always matches the tag):

```bash
./scripts/release.sh            # bumps the patch (v1.2.0 -> v1.3.0)
./scripts/release.sh 2.0.0      # explicit version
```

The script tags and pushes, waits for CI to build the signed APK from that tag, downloads the artifact and creates the GitHub Release with it attached.

## Release signing — stable key for install-over updates

Every release must be signed with the same key so updates install over the existing install without uninstalling first. The fully automated flow:

```bash
./scripts/create-signing-keystore.sh   # creates release.keystore + keystore.properties automatically
./scripts/setup-github-signing.sh      # uploads the same key to GitHub Actions secrets (once)
```

- **Locally:** signing is read from `keystore.properties` (git-ignored).
- **In CI:** read from the `SIGNING_KEYSTORE` (Base64), `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD` secrets — the same key, so CI produces an APK with the same local signature.
- Without either source, the debug key is used automatically, producing an installable APK (for testing, not store publishing).

## Contributing

See [`CONTRIBUTING.md`](CONTRIBUTING.md) and [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md).

Important: religious content is reviewed separately from code — see section 10 of `PROJECT_PROMPT.md`.

## Contact & support

- **GitHub:** [github.com/Alaa91H](https://github.com/Alaa91H)
- **Email:** [alahus2591@gmail.com](mailto:alahus2591@gmail.com)
- **Telegram:** [t.me/Alaa91h](https://t.me/Alaa91h)
- **Support development:** [ko-fi.com/alaa91h](https://ko-fi.com/alaa91h)

## License

[GPLv3](LICENSE) — any modified version of the app stays open source.
