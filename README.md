# Muslim

> **Muslim** is an open-source, privacy-first Android application for daily worship, Quran and hadith study, Islamic learning, practical utilities, and carefully bounded companion-device features. It is designed to remain useful without an account and to keep personal study and worship data on the device by default.

The project is maintained as a Kotlin, Jetpack Compose, multi-module Android codebase. It includes a phone application and an opt-in paired Wear OS companion. The repository is licensed under [GPL-3.0](LICENSE).

| Principle | What it means in this project |
|---|---|
| **Local first** | Prayer settings, Quran progress, tasbih, notes, flashcards, and most feature state are stored on the device. |
| **Explicit optional networking** | Downloads, maps, mosque search, release checks, and a user-configured home-automation bridge are optional rather than required for core worship features. |
| **Truthful scope** | The app does not claim religious certification, provider approval, direct Google Home/Alexa skills, or bundled media that it does not contain. |
| **Reviewable implementation** | Feature modules, content-boundary notes, tests, static verifiers, and release workflows are kept in this public repository. |

## Product map

The phone app has four primary destinations: **Prayer Times**, **Quran**, **Qibla**, and **More**. The More hub groups supporting features into worship, knowledge, tools, and app controls; users may reorder or hide those groups. Related learning destinations are consolidated to avoid duplicate top-level entry points: for example, the Names of Allah and Hajj/Umrah experiences live inside the Learning Centre rather than also appearing as duplicate More shortcuts.

| Area | Implemented capabilities |
|---|---|
| **Prayer and Adhan** | Local prayer-time calculation, selectable calculation methods and Asr school, high-latitude guidance, saved/manual location or one-time location refresh, Hijri adjustment, per-prayer notification controls, Adhan playback, reminders, quiet hours, and next-prayer countdowns. |
| **Quran** | Offline Quran reading, bookmarks and reading progress, translations/tafsir where supplied, Arabic search and word-frequency tools, reciter selection, playback, and user-managed recitation downloads. |
| **Hadith Library** | An offline bundled corpus prepared from compressed NDJSON in small local batches, Arabic-normalized FTS search, collection filters, bookmarks, sharing/copying, daily hadith notifications, and Room/Compose Paging for bounded list and search loading. |
| **Adhkar and Tasbih** | Categorised adhkar, local counters, optional haptics, tasbih logs, a widget, and optional reminder surfaces under user control. |
| **Learning Centre** | Structured guides for faith, purification, salah, fasting, zakat, funerals, and selected madhhab-orientation material, together with integrated Names of Allah and Hajj/Umrah experiences. |
| **Reference and study** | Local reference material, historical timeline and schematic atlas content, a scholarly-library starter catalogue, citation-aware notes, and local flashcards. The scholarly-library starter is not a redistributed copy of third-party digital libraries or publisher editions. |
| **Life and travel guides** | New-Muslim and Noorani-style learning support, family-life guidance, Islamic will/funeral planning, traveller and expatriate tools, transport-prayer orientation, local qibla compass, and high-latitude explanatory material. |
| **Finance and utilities** | Educational transactions material, stock-screening provider shortcuts, a local debt ledger, zakat tools, offline-map management, mosque search, update controls, notification controls, permissions, and accessibility settings. |
| **Accessibility** | TalkBack-oriented labels and guidance, high-contrast and clearer-Arabic-reading options, visible one-shot voice navigation, and carefully labelled external supplementary sign-language learning links. |
| **Companion devices** | Android Auto browsing for already-downloaded Quran recitations only; an opt-in, paired Wear OS tasbih and next-prayer companion; and an optional HTTPS home-automation event bridge. |

## Product UI and accessibility system

The Compose interface follows **Modern Islamic Minimalism**: clear Arabic and Latin hierarchy, calm tonal surfaces, restrained geometric detail, and central semantic components rather than feature-specific visual inventions. The current main line applies this system to the flagship Prayer Times, Quran, Hadith, More, and Settings experiences as well as focused consistency passes across Qibla, Tasbih, Adhkar, Learning, Islamic Finance, Wear, Ramadan planning, Zakat, the reference library, Quran downloads, and Family Life.

| Surface | Experience decision | Preserved boundary |
|---|---|---|
| **Qibla** | A semantic location and direction hierarchy, calibration/recovery states, and a device-independent geometric Qibla marker replace text-glyph rendering. | Direction calculation, sensor use, location permissions, and confirmation feedback remain unchanged. |
| **Tasbih and Adhkar** | The count remains the obvious accessible action; supporting devotional text uses quiet, consistent surfaces. | Local counters, repetition limits, haptics, reminders, sharing, and persistence are unchanged. |
| **Learning and finance** | Grouped knowledge paths, readable steps, semantic notices, validation, empty states, and actions improve task clarity. | Educational content, local debt data, and external screening-provider boundaries remain unchanged. |
| **Ramadan and Zakat** | Ramadan planning, habit progress, Iftar/Suhoor feedback, price retrieval, Nisab outcomes, and saved calculations use calm shared surfaces and clear state treatment. | Fasting records, prayer completion, Ramadan reminders, Zakat inputs, price-provider boundaries, calculations, and history remain unchanged. |
| **Reference and downloads** | Reference content uses more deliberate book, search-empty, and reading surfaces; Quran download summaries, coverage, reciter state, and transfer cards share the same hierarchy. | Study content, language switching, internal navigation, downloads, reciter queues, local files, and deletion confirmation remain unchanged. |
| **Family Life** | Ruqyah, baby-name results, Aqiqah planning and family guidance use shared cards, state surfaces and an accessible secondary action. | Bilingual content, safe-audio validation, external audio launch, name search, date parsing, schedule calculation and reminder persistence remain unchanged. |
| **Wear OS** | Next prayer, countdown, Tasbih, synchronization, and haptic state are organised for fast reading on a small round display. | The companion remains opt-in and paired; the phone stays authoritative. |

See [`docs/design/ui_ux_transformation_plan.md`](docs/design/ui_ux_transformation_plan.md) for system decisions, scope, and verification boundaries.

## Hadith library reliability and loading model

The Hadith screen is deliberately designed for a large offline corpus without blocking the main UI thread or materialising the full library as one list.

> The corpus is shipped as a compressed line-delimited JSON asset. On first preparation, it is read on `Dispatchers.IO` and inserted into Room in bounded batches. Browsing and Arabic search then use Room-backed Paging sources, so Compose receives only the visible page and its prefetch window.

The screen exposes preparation progress, an explicit retry action if preparation fails, and load-state retry controls for a failed page. The daily-hadith lookup reads one deterministic row by offset instead of observing the entire table. This approach follows Android's Paging architecture: `PagingSource` in the data layer, `Pager`/`PagingData` in the view-model layer, and `LazyPagingItems` in the Compose UI. See the [official Paging overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) and [paged-data guide](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data).

The full asset currently contains **35,691** line-delimited records. It is a packaged offline dataset, not a claim that all editions, translations, grading choices, or scholarly contexts are exhaustive. Religious-content and source review remain separate from software review.

## Cross-platform and automation boundaries

| Integration | Delivered behaviour | Deliberate boundary |
|---|---|---|
| **Android Auto** | Exposes media browse, transport, and voice-search handling through the existing playback service. | Only fully downloaded Quran recitations appear. The car experience does not start downloads, render video, or present general interactive content while driving. |
| **Wear OS** | A non-standalone paired companion shows a minimal next-prayer/countdown snapshot and supports a one-tap tasbih increment with optional haptic feedback. | Synchronisation is opt-in. The phone remains the authoritative calculator and store; the payload excludes location, account data, calculation settings, and audio. |
| **Home automation** | A user-selected HTTPS endpoint can receive a minimal `adhan_started` event after local audible Adhan playback begins. | It is disabled by default, sends no audio or location, and is not a direct Google Assistant, Google Home, or Alexa Action/Skill. |

The bridge rejects non-HTTPS destinations and stores its optional bearer secret through Android Keystore-backed encryption rather than ordinary preferences. The Android Auto, Wear, and bridge decisions are documented in [`docs/iot_feature_sources.md`](docs/iot_feature_sources.md).

## Content, source, and safety boundaries

The project contains educational and devotional material, not personalised legal or religious rulings. Calculation output, travel-distance references, high-latitude explanations, financial tools, and historical schematics are deliberately labelled with their respective limits. Users should consult qualified local scholars and applicable local law when a personal ruling or legal decision is required.

The Scholarly Library does **not** bundle Maktaba Shamela files, publisher editions, or other third-party digital-library content. Its starter catalogue contains original study guidance and metadata. Users can select an authorised local JSON content pack only when each imported book identifies its source and licence. See [`docs/scholar_library_content_policy.md`](docs/scholar_library_content_policy.md).

Additional feature-boundary notes are maintained under [`docs/`](docs), including accessibility, finance, history, Noorani/new-Muslim, traveller, and IoT implementation notes. The detailed implementation and documentation map is available in [`docs/PROJECT_STATUS.md`](docs/PROJECT_STATUS.md). Source attribution and specific content decisions should be read with the relevant feature document rather than inferred from a generic screen title.

## Privacy and permissions

The app does not require an account. It does not include advertising, behavioural analytics, or marketing SDKs. Prayer settings, bookmarks, tasbih data, study notes, flashcards, and local reading state remain on the device unless Android's own backup mechanisms apply or the user explicitly selects an external operation.

Permissions are requested only for the relevant capability, such as a one-time location refresh, notifications, exact alarms, the optional adhkar overlay, or notification access for a selected playback behaviour. Network-backed features are optional: recitation/translation downloads, map tiles, mosque discovery, release checks, and the user-configured HTTPS bridge. The bridge is the only home-automation network event and remains disabled until configured.

Read the full, feature-specific policy in [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md).

## System identity and notification repair

The current `main` branch replaces the prior Android-facing icon identities with a new geometric Islamic app mark. The full-colour mark is used only for the adaptive launcher and round launcher; every notification producer uses the dedicated monochrome `v2028` status-bar vector required by Android. The migration covers active Adhan, next-prayer countdown, Quran-recitation media, downloads, reminders, and other system-notification paths.

A resource-name change alone cannot remove a notification that Android retained from an earlier APK. A package-replaced receiver, the application, and the relevant services explicitly cancel the known older Adhan, countdown, and Quran-recitation notification IDs before publishing their current cards. The receiver runs immediately after an in-place package update, so cleanup does not depend on the user opening the app first. The replacement uses fresh current IDs, which prevents a retained card from sharing the visual identity of the newly posted card. The app deliberately does not supply a `setLargeIcon` image to the Adhan, countdown, or Quran media cards; Android may still render its own application identity in a system template.

A live Adhan is a non-dismissible, ongoing foreground card with one explicit **Stop Adhan** action. It uses the fresh high-importance `adhan_alert_v3` channel, requests public lock-screen visibility and high-priority heads-up presentation, and cancels the prior pre-prayer reminder when the real Adhan begins. Settings previews are explicitly separate and cannot stop a live scheduled Adhan. Android still owns the final lock-screen, banner, notification-permission, and user-edited channel behaviour.

| System surface | Current behaviour | What Android still controls |
|---|---|---|
| Launcher | New `v2028` adaptive and round-icon resource identities reference the approved full-colour geometric mark. | Mask shape, themed-icon tint, badge and cache-refresh timing. |
| Status bar | Every small notification icon resolves to the `v2028` monochrome geometric glyph. | Final light/dark/system tint and status-bar layout. |
| Active Adhan | A persistent foreground card uses the `adhan_alert_v3` high-importance channel, public lock-screen visibility, and a single explicit Stop Adhan action; it also retires the prior reminder. | The system and user-owned Android settings control final banner, lock-screen, interruption, and channel presentation. |
| Next-prayer countdown | Retired ongoing cards are cancelled; a fresh quiet card ID is then posted. | Permission, channel state, grouping and visibility. |
| Quran media playback | The foreground `MediaStyle` service clears its retired card before publishing its new media-card ID. | Media-card template, controls layout and lock-screen presentation. |

See [`docs/qa/notification_identity_repair.md`](docs/qa/notification_identity_repair.md) for the exact migration map, verification strategy, and upgrade note. Android documents the system-owned notification-template model and adaptive-icon masking separately. [1] [2]

## Architecture

The repository uses Kotlin/JVM 17, AGP 9.3.1, Gradle 9.5, Kotlin 2.2.10, Compose BOM 2026.08.00, compile/target SDK 37, and phone min SDK 26. The Wear companion has min SDK 30. Version declarations are centralised in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

```text
app                         Phone application, navigation, application wiring
wear                        Paired Wear OS companion application
core:core-common            Shared text, time, model, and utility code
core:core-design-system     Theme and reusable design primitives
core:core-ui                Shared Compose UI utilities
core:core-database          Shared database infrastructure
core:core-datastore         Preferences and local settings
core:core-network           Optional networking infrastructure
core:core-notifications     Notification and scheduling integration
core:core-location          Location and prayer-related location support
core:core-permissions       User-visible permission orchestration
feature:*                   Independently scoped product features
```

Feature modules depend on core modules rather than on other feature modules. The app module owns top-level navigation and assembles feature destinations. Hilt provides dependency injection; Room and DataStore provide local persistence; Coroutines and Flow provide asynchronous state; WorkManager and AlarmManager support scheduled work and prayer alerts.

## Build, test, and verify

Install JDK 17 and Android SDK platform 37, then configure `local.properties` with the SDK path. The examples below use the Gradle wrapper.

```bash
# Phone and Wear debug packages
./gradlew :app:assembleDebug :wear:assembleDebug

# Unit tests and static quality gates
./gradlew testDebugUnitTest :wear:testDebugUnitTest
./gradlew lintDebug
./gradlew detekt

# Targeted safeguards for the most recent large-data/navigation work
python3 scripts/verify_hadith_paging_and_navigation.py
python3 scripts/verify_scholar_library.py
python3 scripts/verify_iot_integration.py
python3 scripts/verify_islamic_visual_identity.py
```

The GitHub Actions workflow builds both applications, runs unit tests, Android Lint, Detekt, and emulator tests, and creates signed phone and Wear release APKs. Tagged `v*` builds publish a GitHub Release with both artifacts. The release workflow is an automated safety net, not a substitute for device, vehicle, watch, accessibility, or content-provider review.

## Release and installation

Application version information is derived from Git tags by the Gradle build. A release is created only after the relevant `main` CI run is green, using an annotated `v*` tag. The tag-triggered workflow produces and attaches:

| Artifact | Purpose |
|---|---|
| `app-release.apk` | Signed phone APK. |
| `wear-release.apk` | Signed paired Wear OS companion APK. |

Download published artifacts from the [GitHub Releases page](https://github.com/Alaa91H/Muslim/releases). A release signed with the established key installs over an earlier compatible install. Only install release files from a source you trust.

## Contributing

Start with [`CONTRIBUTING.md`](CONTRIBUTING.md), keep strings in resources, preserve module boundaries, run the relevant Gradle checks and feature verifier, and document any new content or privacy boundary. Religious content, source licences, translations, scholarly classifications, and software changes require separate review paths.

## Connect With Me

The confirmed public maintainer profile is [Alaa on GitHub](https://github.com/Alaa91H). Use repository issues for reproducible defects or documentation corrections; no additional personal contact channel is asserted here.

## Support Me

Voluntary support is available through [Ko-fi: alaa91h](https://ko-fi.com/alaa91h). This is the confirmed public support link associated with the maintainer profile.

## Licence

The project is distributed under [GPL-3.0](LICENSE).

### References

[1]: https://developer.android.com/develop/ui/views/notifications/build-notification "Android Developers — Create a notification"
[2]: https://developer.android.com/develop/ui/views/launch/icon_design_adaptive "Android Developers — Adaptive icons"
