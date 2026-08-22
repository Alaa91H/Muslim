# Changelog

All notable changes to Muslim are documented here. Release notes use the same
sectioned format as v1.10.0 and are generated from the commits for each tag.

## Muslim v1.16.0

Updates install directly over v1.15.0 (same stable signing key — no uninstall needed).

## ⚖️ Funerals & Islamic Will
- Added a private, local-first Islamic-will draft with executor details, rights and debts, funeral wishes, guardianship notes, charitable bequests and a deliberate system-share action.
- Added clear privacy and legal-review notices, alongside an educational checklist and concise source-aware guidance.
- Added an expandable funeral guide covering the moments after death, washing, shrouding, funeral prayer, condolences and cemetery etiquette, with a reminder that details vary by school of jurisprudence and local law.

## 💼 Islamic Economy & Finance
- Added a bilingual transactions guide covering ethical sale and purchase, lending, debt documentation and contemporary e-commerce considerations.
- Added an Islamic-stock screening workspace that copies the entered ticker and opens the selected licensed provider page; it explicitly distinguishes information from a Shariah ruling or investment advice.
- Added a private local debt ledger for amounts owed to and by the user, separate currency subtotals, due-date validation and optional device-local repayment reminders.
- Added a dedicated finance notification category in Settings so repayment reminders remain under user control.

## 🧪 Engineering & CI Quality Gate
- Added focused unit coverage for the will template, funeral guidance, finance content and multi-currency debt totals, plus static resource-integration checks.
- Added the `feature-finance` module and registered its navigation, deep link and notification integrations.
- Updated the Gradle setup action to v6 and retained the required Debug APK, unit-test, Android Lint, Detekt and emulator quality gates.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## Muslim v1.15.0

Updates install directly over v1.14.0 (same stable signing key — no uninstall needed).

## 👨‍👩‍👧 Family Life & Islamic Guidance
- **Ruqyah الشرعية**: added a detailed offline-safe guide, Quranic passages, validated EveryAyah audio links, and a clear medical-safety notice.
- **Islamic baby names**: searchable boys' and girls' catalog with Arabic names, transliterations, meanings and gender filters.
- **Aqiqah planner**: calculates the seventh, fourteenth and twenty-first days, stores the birth date locally, and schedules a reboot-safe WorkManager reminder.
- **Marriage and family reference**: expanded articles covering engagement, nikah, mahr, marital rights, conflict safety, parenting and child protection.

## 🕌 Prayer, Notifications & Ramadan
- Added focused Ramadan and Islamic-occasion calculations with unit coverage for fasting days and Hajj dates.
- Improved the unified notification manager with localized channels, quiet-hours controls and family reminders.
- Added live effective-prayer-volume previews and retained per-prayer notification customization.

## 🧭 Maps & Field Services
- Mosque search now uses resilient OpenStreetMap/Overpass queries, cached results, retry behavior and expanded worldwide fallback coverage.
- Added offline-map area management, interactive selection, current-location centering and storage warnings.
- Added reusable halal E-number classification with conservative unknown/ questionable handling.

## 📖 Quran, Hadith & Reference Library
- Added Quran backup/restore models, tajweed markup support, richer search metrics and Arabic-text normalization tests.
- Expanded learning, Hajj/Umrah, prophets, family-life and reference content with focused domain tests.
- Added full hadith-corpus integrity checks and localized presentation improvements.

## 🌍 Globalization & Accessibility
- Preserved the world-language resource tree with BCP-47 locale folders and localized settings/feature strings.
- Kept RTL/LTR, 12/24-hour time, dynamic start-tab selection and adaptive layouts across the application.

## 🧪 Engineering & CI Quality Gate
- Added a required GitHub Actions quality gate for Debug APK, unit tests, Android Lint, Detekt and real Android Emulator tests.
- Failure artifacts include test XML, Lint, static-analysis and emulator reports for diagnosis.
- Added focused tests for permissions, volume settings, mosque caching, aqiqah dates, halal classification and family content.
- Release automation builds, signs, verifies and publishes the APK when a `v*` tag is pushed.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## v1.14.0

- Mosque finder reliability, one-step back navigation, master adhan volume and update-check fixes.

## v1.13.0

- Permission and notification managers moved into the main Settings flow.

## v1.12.0

- Automatic release publication from tag-triggered GitHub Actions.

## v1.11.0

- Offline-map interactive picker, smart storage management and fully automatic release script.

## v1.10.0

- 44 verified reciters, reciter picker, More-section reorder, mosque markers, offline maps and notification manager improvements.

## v1.9.0

- Offline maps, mosque markers, crash reporting, format-audit tests and reciter summaries.

## v1.8.0

- Full world-language localization and remaining feature work.

## v1.7.0

- Recitation control notification, meanings/tafsir controls, hadith expansion and bundled adhan sounds.

## v1.6.0

- Unified notification manager with master switches for every notifier.

## v1.5.0

- Full offline content, Six Books corpus, adhan recordings and Quran player improvements.

## v1.4.0

- Six Books hadith import pipeline with duplicate checking.

## v1.3.0

- Quran mushaf layout, reciter downloads, surah details and dynamic versioning.

## v1.2.0

- Custom periodic adhkar reminders with bubble support.

## v1.1.0

- CI maintenance and release automation improvements.

## v1.0.0

- Initial Muslim Android application with prayer times, qibla, Hijri date and adhan support.
