# Changelog

All notable changes to Muslim are documented here. Release notes use the same
sectioned format as v1.10.0 and are generated from the commits for each tag.

## Muslim v1.21.0

Updates install directly over v1.20.0 (same stable signing key — no uninstall needed).

## 🚗 Android Auto: Safe Local Recitation
- Added an Android Auto media-browse integration backed by the existing Quran playback service and media session, including standard browse, transport and voice-search handling.
- The car catalogue deliberately exposes only recitations already complete on the paired phone. It does not start downloads, render video, or present interactive screens while driving; unavailable search results state that the requested surah has not been downloaded.
- Added the Android Automotive media capability declaration and final-manifest filters required for system discovery and Android Auto voice search.

## ⌚ Wear OS Paired Companion
- Added a dedicated Wear OS companion APK with an intentionally small paired-phone experience: next-prayer name, local countdown, tasbih phrase/count, a one-tap tasbih increment and an optional haptic response.
- Added filtered Wear Data Layer contracts for the minimum prayer and tasbih snapshot. Watch synchronization is disabled by default, shares no location or account data, and the phone remains the authoritative prayer-time calculator and tasbih store.
- Added Wear-specific application metadata, launcher artwork, local snapshot validation and a Data Layer listener filtered to the app’s documented state path.

## 🏠 Optional Home-Automation Bridge
- Added the user-selected HTTPS bridge rather than claiming a direct Google Home or Alexa Skill. The bridge is disabled by default and can send a compact `adhan_started` event only after local audible adhan playback begins.
- The endpoint must be HTTPS; an optional bearer token is stored locally with Android Keystore rather than DataStore. The event contains the prayer label, occurrence time and source only—never audio, location, credentials, prayer calculations or account data.
- Added a Smart Devices settings destination with explicit explanation of the Android Auto, watch and bridge boundaries, plus user-controlled enablement and endpoint/token removal.

## 🧪 Engineering & CI Quality Gate
- Added focused unit coverage for Wear snapshot validity and HTTPS endpoint validation, a static IoT integration verifier, and documented official-source, privacy, deployment and operational boundaries.
- Registered the Wear module in the full CI build, quality gates and tagged-release artifacts. Retained build-output caching while disabling configuration-cache storage for a documented AGP/Wear navigation-task incompatibility.
- The merged main commit passed Debug APK, unit-test, Android Lint, Detekt, Android Emulator and signed Release APK CI gates before this changelog update.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## Muslim v1.20.0

Updates install directly over v1.19.0 (same stable signing key — no uninstall needed).

## ♿ Accessibility Centre & Clearer Arabic Reading
- Added a dedicated Accessibility Centre, reachable from More, Settings and `muslim://accessibility`, with practical TalkBack guidance, labelled controls and clearly scoped accessibility-review notices.
- Added a persisted high-contrast theme option and a clearer Arabic-reading option for Quran mushaf text and primary adhkar, using bundled Noto Sans Arabic with expanded line spacing. The reading option is a legibility preference, not a diagnosis or treatment for dyslexia.
- Bundled the official Noto Sans Arabic font under its SIL Open Font License 1.1 and recorded source, licensing and usage boundaries in the accessibility documentation.

## 🎙️ Explicit One-Shot Voice Navigation
- Added an optional visible Listen control that starts recognition only after the user presses it, requests microphone permission only then, ends each session on a result, error or dismissal, and does not store audio or transcripts in the app.
- Added Arabic, English and numeric command matching for local Quran metadata, including `اقرأ سورة الكهف`, alongside practical app-destination commands.
- Prefers Android on-device recognition where the device offers it and requests offline preference; recognition availability and any provider/network processing remain device- and service-dependent.

## 🤟 Supplementary Sign-Language Learning Links
- Added clearly labelled external British Sign Language (BSL) links for wudu and salah learning. The videos are not embedded or copied into the APK, are supplementary rather than a fatwa, and are not represented as universal or Arabic sign language.
- Preserved an explicit invitation for ongoing review with blind/low-vision users, Deaf sign-language communities and qualified religious educators; this release improves access but is not a certification of complete accessibility compliance.

## 🧪 Engineering & CI Quality Gate
- Added focused unit coverage for Arabic/English route commands, Arabic/English/numeric surah commands and unknown commands, plus a static verifier for permissions, privacy boundaries, resources, navigation, font licensing and source notes.
- The merged main commit passed Debug APK, unit-test, Android Lint, Detekt, Android Emulator and signed Release APK CI gates before this changelog update.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## Muslim v1.19.0

Updates install directly over v1.18.0 (same stable signing key — no uninstall needed).

## 🏛️ Islamic History & Civilization
- Added a dedicated bilingual History & Civilization destination with an interactive timeline spanning the Prophetic mission, Rashidun period, Umayyads, Abbasids, regional civilizations, Ottomans, and modern-era orientation.
- Added concise era cards with selected milestones and explicit source-review framing; the existing detailed Sira material remains separately available in the Reference Library.
- Added selected contextual biographies of commanders, scholars, physicians, thinkers, and architects, including al-Khwarizmi, al-Razi, Ibn al-Haytham, Ibn Sina, Ibn Khaldun, and Mimar Sinan. The writing deliberately avoids unsupported “sole inventor” claims.

## 🗺️ Interactive Historical Atlas
- Added a MapLibre atlas with tappable place markers, zoom controls, original local vector overlays, and selectable layers for Hijaz/Sira locations, broad Umayyad and Ottoman reaches, and Abbasid learning/trade networks.
- Added educational orientation for selected battle locations and caravan/trade corridors, including overland Baghdad–Samarkand and maritime Gulf–Indian Ocean connections.
- Clearly labels every state area, route, and marker as an approximate educational schematic—not a fixed political border, sovereignty claim, exhaustive gazetteer, reconstructed itinerary, or complete historical atlas.
- Added direct access from More and the `muslim://history` deep link. Historical overlays remain local; the existing OpenFreeMap basemap follows the app’s established online/offline-map behaviour.

## 🧪 Engineering & CI Quality Gate
- Extended the shared MapLibre wrapper with reusable generic pin and replaceable polygon-overlay layers for non-mosque educational maps.
- Added focused integrity coverage for chronology, bilingual content, schematic areas, route data, personalities, and restrained historical language, plus static resource/navigation/deep-link/map integration checks.
- Added source, licensing, network-behaviour, map-boundary and specialist-review notes. No third-party historical map images, atlas plates, or biography text were bundled.
- Retained the required Debug APK, unit-test, Android Lint, Detekt and Android Emulator gates; all passed on the merged main commit before tagging.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## Muslim v1.18.0

Updates install directly over v1.17.0 (same stable signing key — no uninstall needed).

## 🧳 Traveller & Expat Corner
- Added a dedicated travel destination with a locally saved departure point, explicit one-time GPS refresh, and a great-circle distance reference with selectable 80 km and 90 km thresholds.
- Kept the distance result intentionally educational: it is labelled as a straight-line technical reference, not a personal ruling on qasr or jam’, and it explains that route, intended stay, school of jurisprudence and local scholarly guidance matter.
- Added practical, safety-aware plane and train orientation for prayer, water use, wudu and asking about tayammum or constrained postures from a qualified teacher when conditions require it.
- Added a live local qibla compass based on device sensors, geomagnetic declination and the established qibla bearing; after a coordinate is available it needs no network connection and does not start background tracking.

## 🌍 High-Latitude Prayer Orientation
- Added an explainer for the existing Middle-of-the-Night, One-Seventh-of-the-Night and twilight-angle calculations, plus a same-engine Fajr/Isha preview for the saved prayer location and a direct link to Prayer Settings.
- Shows an educational 48°/66° latitude-band orientation, states that scholarly bodies and mosque timetables may adopt different estimations, and refers users to their recognised local authority rather than asserting a universal fatwa.
- Corrected automatic high-latitude-rule selection so the same recommendation applies at corresponding southern as well as northern latitudes.

## 🧪 Engineering & CI Quality Gate
- Added focused unit coverage for great-circle distance, both threshold states, guide-review boundaries and the complete high-latitude-rule catalogue.
- Added a static resource/navigation/deep-link/privacy verifier and a source-and-boundaries document for the travel, transport and high-latitude material.
- Retained the required Debug APK, unit-test, Android Lint, Detekt and Android Emulator gates; all passed on the merged main commit before tagging.
- Suppressed the documented AGP compatibility notice required by the project’s temporary KSP bridge, keeping the release checks free of actionable project warnings.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## Muslim v1.17.0

Updates install directly over v1.16.0 (same stable signing key — no uninstall needed).

## 📚 Qaida Noorania & Arabic Reading Foundations
- Added a dedicated interactive Qaida Noorania-inspired learning destination with all 28 Arabic letters, original visual memory cues for broad articulation areas, and tap-to-listen Arabic pronunciation through the device’s installed text-to-speech voice.
- Added gradual practice for short vowels, long vowels, sukūn and shaddah, with explicit learner-facing guidance that these prompts are not a substitute for feedback from a qualified Arabic or Quran teacher.
- Kept the section free of copied Qaida book pages, third-party recordings or unverified visual assets; the original visual cues and device-local voice avoid distributing unlicensed instructional media.

## 🤝 New Muslim Corner
- Added a calm, privacy-conscious first-steps guide in Arabic, English, French and Spanish, covering the testimony of faith, a simple purification orientation, paced prayer and Quran learning, and ways to find respectful local support.
- Added clear educational and scholarly-review notices: accepting Islam is presented as a free personal choice, and practical fiqh details are referred to qualified teachers or scholarly organisations.
- Added a More-menu card and the `muslim://noorani` deep link for direct access.

## 🧪 Engineering & CI Quality Gate
- Added focused unit coverage for the 28-letter catalogue, staged reading progression, multilingual new-Muslim guides, choice-respecting language and purification guidance.
- Added static XML/resource, navigation and deep-link verification plus documented content sources and the audio/visual licensing decision.
- Retained the required Debug APK, unit-test, Android Lint, Detekt and Android Emulator gates; all passed on the merged main commit before tagging.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

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
