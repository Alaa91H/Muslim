# Changelog

All notable changes to Muslim are documented here. Release notes use the same
sectioned format as v1.10.0 and are generated from the commits for each tag.

## Muslim v1.24.3

Updates install directly over v1.24.2 with the same package name (`org.muslim.app`) and stable signing key. No uninstall or data reset is required.

## Prayer Home, Adhan Controls, and Reminders
- Reordered the prayer home screen so the primary **Today’s prayer times** card remains directly below the next-prayer card. The private daily prayer check-in now sits lower in the page.
- Added a stronger, non-colour-only visual treatment for the next prayer inside the daily times card.
- Added a **Stop Adhan** action to the active Adhan notification.
- Added optional controls to allow dismissing the active Adhan notification and, separately, to stop active Adhan audio when that notification is dismissed. The notification remains persistent by default.
- Set the default prayer reminder lead time to **15 minutes**. Users can still disable the reminder or select another lead time.

## Reliable Quran Search and Selected-Ayah Playback
- Hardened offline Quran search with a normalized local fallback when FTS is empty, stale, or rejects an edge-case query. Search remains fully on-device and supports normalized Arabic prefix matching.
- Preserved the selected ayah as an explicit playback start point. If a previous recitation is paused, selecting another ayah and pressing Play now begins from the newly selected ayah rather than resuming the prior one.

## Personal Appearance Controls
- Added curated light and dark palette families: **Classic Islamic**, **Emerald**, **Midnight**, and **Sand**. Dynamic wallpaper colours remain available as an explicit system-colour option.
- Added global card-corner preferences for **Compact**, **Soft**, and **Rounded** surfaces.
- Added switchable low-opacity Islamic ornament styles for supported backgrounds: **Geometric**, **Arabesque**, **Stars**, and **Minimal**.
- Updated the adaptive launcher artwork with a new high-contrast Islamic arch, crescent, and eight-point-star mark. The Adhan notification now uses a matching monochrome status-bar symbol.

## Verification
- Added unit coverage for normalized Quran-search fallback behavior.
- Passed local Debug compilation, Quran and prayer-time unit tests, application unit tests, Android Lint, Detekt, and the production content-manifest gate before release tagging. Tagged CI, signed artifact builds, and emulator validation are required before publication.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app remains signed with the same release key, so the update installs directly over prior Muslim releases.

## Muslim v1.24.2

Updates install directly over v1.24.1 with the same package name (`org.muslim.app`) and stable signing key. No uninstall or data reset is required.

## 🔔 Hardened Scheduled Adhan Delivery
- Reworked the Adhan verification path so it no longer reports readiness from permissions and configuration alone. The in-app verification action now schedules a short exact-alarm probe through the same broadcast receiver and foreground-service path used for an actual prayer time.
- Added an on-device delivery journal with explicit checkpoints for probe scheduling, receiver delivery, foreground-service request, foreground-service startup, and confirmed `MediaPlayer` or `AudioTrack` playback startup.
- The Adhan readiness state now requires confirmed audio startup from a recent scheduled probe. A direct preview, a granted permission, or a successful service-start request is not treated as proof of scheduled audio delivery.
- Added explicit failure recording for unavailable exact alarms, foreground-service startup failures, silent or vibration-only configuration during a sound probe, notification startup failures, and missing audio-start confirmation.
- Re-schedules prayer alarms when the app resumes from Android system settings, ensuring that an exact schedule replaces any earlier degraded schedule after the user grants exact-alarm access.

## 📖 Quran Playback and Reading Flow
- Added a direct play action beside each Surah in the Quran index. It opens the reader and starts from the first ayah of the selected Surah.
- Made whole-Surah playback the default reader range, while retaining deliberate options for a single ayah or continuing from a selected ayah to the end of the Mushaf.
- Improved manual vertical reading in the Mushaf view. The active ayah and saved reading position follow the visible reading edge during user scrolling, without pulling the reader back to an old position. Audio follow-along remains authoritative during active recitation.

## 🕌 Private Daily Prayer Check-in
- Added a device-local daily check-in for the five obligatory prayers: Fajr, Dhuhr, Asr, Maghrib, and Isha. Sunrise is intentionally excluded.
- The check-in remains private to the device, has no account, analytics, network synchronization, points, streaks, or religious compliance score, and retains only a bounded recent history.

## 🧪 Verification and Release Quality
- Added focused unit coverage for manual Quran reading follow behavior, Adhan delivery-stage semantics, and the stricter Adhan readiness rule.
- Verified the production content inventory and passed Debug builds, unit tests, Android Lint, Detekt, Android Emulator checks, signed release artifact builds, and signed closed-beta artifact builds on the merged main branch before tagging.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app remains signed with the same release key, so the update installs directly over previous releases.
- After installation, open Adhan Settings and run **Verify**. The check schedules a real delivery probe after approximately ten seconds and passes only when the application confirms audio startup. Device alarm volume and the selected audio output still need to be audible.

## Muslim v1.24.0

Updates install directly over v1.23.0 (same stable signing key — no uninstall needed).

## 🌙 Modern Islamic Minimalism
- Introduced a calm, central Islamic design system with warm ivory light surfaces, deep green night surfaces, and a dedicated Mushaf Sepia reader scheme. Antique gold is limited to tertiary accents rather than dominant panels, glow, or bright-gold decoration.
- Added shared spacing, 20dp/24dp surface radii, low elevation, short motion, accessible touch-target tokens, and global Material shapes so new and existing components inherit a consistent, restrained visual hierarchy.
- Made the Islamic palette the default for new installs by making wallpaper-derived dynamic colour an explicit opt-in; existing users retain their saved appearance preference.

## ✦ Vector Ornament & Component System
- Added reusable Android vector ornaments for Geometric 8/12, Star 8/12, Arabesque, Mushaf Divider, Surah Header, and Corner details. Decorative art is intentionally low-opacity, has no semantic role, and does not depend on Unicode ornament glyphs.
- Added quiet `IslamicCard`, primary-button, and secondary-button primitives, plus an internal Compose showcase for reviewing the light, dark, and Mushaf-paper contexts without adding a user-facing developer route.
- Refined the prayer home surface and Quran index with subtle vector header/background treatment, shared outlines, and existing Material icons, without replacing established navigation or controls.

## 📖 Focused Quran Reader
- Refined the Quran reader’s Surah header, Basmala divider, ayah marker, current-recitation feedback, and bottom recitation player with shared tokens and compact vector bands.
- Preserved reader themes, Arabic font choices, RTL support, text sizing, bookmarks, last-read progress, downloads, and playback workflows. Quran body text and ayah data were not changed, and no decorative pattern is rendered behind verses.

## ♿ Documentation, Testing & CI Quality Gate
- Added detailed English design documentation covering palettes, components, ornament opacity, accessibility, responsive behavior, reader quietness, and verification boundaries.
- Added palette/token unit coverage and `scripts/verify_islamic_visual_identity.py`, which checks required tokens and vector assets and guards against the obsolete text ornament and bright gold in core visual paths.
- The visual-identity pull request passed Debug APK builds, unit tests, Android Lint, Detekt, and Android Emulator CI gates before this changelog update. The final main and tagged-release runs are verified separately before publication.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## Muslim v1.23.0

Updates install directly over v1.22.0 (same stable signing key — no uninstall needed).

## 📖 Hadith Library Stability & Bounded Loading
- Replaced the eager full-corpus JSON read and complete in-memory Hadith list with a compressed NDJSON corpus streamed on an I/O dispatcher and written to Room in bounded 150-row batches.
- Added Room Paging and Compose Paging for collection browsing and Arabic FTS search, with a 24-row page size, prefetch window, stable item keys, debounced search and `viewModelScope` caching.
- Reworked the daily-hadith lookup to read one deterministic row by offset instead of observing the entire corpus.
- Added visible offline-library preparation progress, recoverable preparation failure, page-load retry and empty-result states, so a preparation or page error no longer terminates the Hadith screen.

## 🧭 Clearer Learning Navigation & Modern UI APIs
- Consolidated the Names of Allah and Hajj/Umrah entry points under the Learning Centre, removing their duplicate top-level More shortcuts and duplicate app routes while preserving internal back navigation.
- Replaced actionable deprecated Compose tab/icon APIs and the deprecated Arabic `Locale` constructor in touched feature paths, including finance, learning, travel and history screens.
- Added safe localisation fallbacks and format auditing for new Hadith loading strings across the project’s language resources.

## 📚 Documentation & Privacy Accuracy
- Rewrote the English README as a truthful product, architecture, build and release guide; added a detailed project-status document and a documentation index.
- Expanded the English privacy policy and contributor guide with actual device storage, optional networking, Wear OS, Android Auto, home-bridge, large-data, content-licence and review boundaries.
- Marked the historical planning prompt as planning context and linked it to the current English implementation documentation, avoiding an outdated roadmap being mistaken for the completed product inventory.

## 🧪 Engineering & CI Quality Gate
- Added static verifiers for Hadith streaming/Paging/navigation, documentation status and Hadith locale format completeness; retained the Scholar Library and IoT verifiers.
- The merged pull request passed Debug APK builds, unit tests, Android Lint, Detekt and Android Emulator CI gates before this changelog update.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

## Muslim v1.22.0

Updates install directly over v1.21.0 (same stable signing key — no uninstall needed).

## 📚 Advanced Scholar Library
- Added a dedicated on-device Scholar Library with a searchable starter catalogue of 18 major-reference reading paths spanning fiqh, usul al-fiqh, aqidah, hadith sciences, tafsir, Arabic studies and history.
- Added Arabic-normalized offline full-text search, science-category filters, book metadata and citation-ready study passages with chapter, volume and page fields where supplied.
- The bundled starter material is deliberately original editorial study guidance, not reproduced full books, publisher editions or content copied from Maktaba Shamela or another third-party library.

## 🗂️ References, Notes & Flashcards
- Added selectable study passages, local notes tied to their exact citation, and a study desk that preserves book, author, chapter and optional volume/page context beside every saved note.
- Added citation-linked flashcards with an intentionally simple device-local spaced-review schedule, including reveal, remembered, again and delete controls.
- Added user-selected JSON content-pack import for authorised texts. Each imported book must declare a source and licence summary; imports are size-bounded, indexed locally and never trigger automatic remote downloads.

## 🔐 Content & Privacy Boundaries
- Added a documented content policy and pack schema clarifying that a classical author’s age does not by itself permit copying modern edited editions or another organisation’s digital files.
- Library text, notes and flashcards stay in the private on-device Room database; the feature does not transmit the user’s study material, account data or reading activity.
- The research workspace is a study organiser, not a fatwa engine or substitute for checking sources, editions, context and qualified scholarship.

## 🧪 Engineering & CI Quality Gate
- Added the standalone `feature-scholar-library` module, Room FTS index, Hilt wiring, navigation from More and focused Arabic-search tests.
- Added `scripts/verify_scholar_library.py` to validate catalogue identifiers, source/licence metadata, category coverage, search-index wiring, navigation and content-policy presence.
- Retained the required Debug APK, unit-test, Android Lint, Detekt and Android Emulator gates for the release workflow.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app stays signed with the same release key, so updates install directly over previous versions.

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
