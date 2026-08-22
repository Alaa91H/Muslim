# Privacy Policy — Muslim

> **Last updated:** 22 August 2026

## Privacy position

**Muslim is local-first by default.** The application does not require an account and does not intentionally include advertising, behavioural analytics, marketing trackers, or a user-profile backend. Most worship, reading, and study functions work from packaged or device-local data.

This policy describes the behaviour implemented in the application repository. Android, a chosen network provider, the operating system, or a website opened by the user may have their own policies that are outside the application's control.

## Data processed on the device

| Data category | Examples | Purpose | Default destination |
|---|---|---|---|
| Prayer and location settings | Selected city, manually entered coordinates, one-time GPS result, calculation method, time format | Prayer times, qibla, travel and high-latitude tools | Device-local storage |
| Worship state | Adhan/notification preferences, adhkar counters, tasbih totals, fasting markers, zakat and debt entries | Personalisation and continuity | Device-local storage |
| Reading and study state | Quran bookmarks/progress, hadith bookmarks, scholarly-library notes, citations, flashcards, imported authorised packs | Reading and study workflows | Device-local Room/DataStore storage |
| Bundled content | Quran, feature guides, selected hadith corpus, starter catalogue metadata | Offline presentation and search | Application package and local database after preparation |
| Optional bridge configuration | HTTPS endpoint and on/off preference | User-configured home automation event | Endpoint preference is local; optional bearer secret is encrypted through Android Keystore |
| Wear companion snapshot | Next-prayer label/countdown and tasbih phrase/count | Opt-in paired watch display and one-tap tasbih request | Sent only through the Google Play services Data Layer to paired app nodes when enabled |

The app does not upload the local study database, notes, flashcards, tasbih log, Quran progress, or saved prayer preferences as a default behaviour.

## Permissions

Permissions are requested in context and can be refused. A denied optional permission does not prevent unrelated offline features from working.

| Permission or system access | When it is used | User control |
|---|---|---|
| **Location** | A user selects automatic/current location for prayer times, qibla, mosque search, or travel tools. | Use a manual location instead, deny permission, or revoke it in Android settings. The app does not start continuous background tracking for ordinary use. |
| **Notifications** | Adhan, reminders, the daily hadith, Ramadan notifications, and other categories enabled by the user. | Category settings and Android notification controls remain available. |
| **Exact alarms / alarm scheduling** | Time-sensitive Adhan and selected reminders. | Android may require a dedicated system setting; disabling it can affect punctual delivery. |
| **Overlay** | Only the optional floating adhkar reminder surface. | The feature remains off unless enabled and can be revoked in system settings. |
| **Notification access** | Only an explicitly selected playback-related behaviour, such as pausing recitation for notifications. | It is optional and revocable in Android settings. |
| **Vibration/haptics** | Tasbih feedback, accessibility feedback, and optional Wear interaction. | Controlled by device/system and feature settings where applicable. |

## Optional network activity

Core reading, calculation, adhkar, tasbih, local study, and downloaded Quran recitation playback do not require a network connection. Network use occurs only for selected capabilities such as the following:

| Optional operation | External destination or protocol | Data boundary |
|---|---|---|
| Quran recitation or translation download | User-selected content provider | The request needed to retrieve the selected file; provider terms apply. |
| Map tiles and mosque discovery | Map/OSM-compatible providers used by the relevant screen | Map query/location information needed by that operation; provider terms apply. |
| Release update check | GitHub Releases | Application/version request metadata handled by GitHub. The feature is opt-in. |
| External educational or screening link | Website deliberately opened by the user | The chosen site controls its own data practices. |
| Home-automation bridge | User-configured HTTPS endpoint | A compact `adhan_started` event after local audible Adhan begins: schema version, event name, lowercase prayer name, occurrence time, and source. No audio, location, account data, prayer calculation settings, or token value is sent in the event body. |

The home bridge rejects non-HTTPS endpoints and remains disabled until the user configures and enables it. It is **not** a direct Google Assistant, Google Home, or Alexa service integration.

## Paired Wear OS companion

Wear synchronisation is disabled by default. When enabled by the user, the phone publishes only a minimal versioned snapshot: next prayer name, next occurrence/countdown information, tasbih phrase, and tasbih count. The phone remains authoritative for prayer calculations and persistent tasbih state. The payload intentionally excludes precise location, account identity, audio, and calculation preferences.

The watch can send a narrowly filtered tasbih-increment request. Data transfer depends on the Google Play services Data Layer and the paired device relationship; Google’s policies apply to that platform service.

## Android Auto and vehicle behaviour

The Android Auto integration presents only complete Quran recitations already downloaded on the paired phone. It does not initiate driving-time downloads, show video, or transmit a user profile to a vehicle provider. Vehicle systems, Android Auto, and media hardware can apply their own privacy policies.

## Imported scholarly-library content

The Scholarly Library never downloads a third-party corpus automatically. A user can choose a local JSON content pack only when it carries source and licence information. The app indexes the chosen text on device for search, notes, citations, and flashcards. Users are responsible for ensuring that an imported file is authorised for their intended use and distribution.

## Security and storage limits

The optional home-bridge bearer secret uses Android Keystore-backed cryptography. Other local data uses Android application-private storage, Room, and DataStore as appropriate to the feature. No software can guarantee protection if a device is rooted, compromised, unlocked for another person, or restored by an operating-system backup service.

If Android backup is enabled, Android or the device manufacturer may include eligible application data in a cloud/device backup under its own settings and privacy policy. Review the relevant Android backup configuration and account settings if this matters to you.

## No sale, advertising, or required accounts

The project does not sell user data, require an application account, embed advertising SDKs, or use a marketing analytics SDK as part of the implemented product. This statement does not cover third-party websites, map services, content providers, Android system services, or other applications that the user elects to use.

## Changes and contact

Privacy behaviour can change when a feature changes. Material changes should be documented in this file, the release changelog, and the relevant feature-boundary document under [`docs/`](docs).

For questions or corrections, open a repository issue or contact the project maintainer through the repository contact information. For implementation detail, review the source code and the companion documents [`docs/iot_feature_sources.md`](docs/iot_feature_sources.md) and [`docs/scholar_library_content_policy.md`](docs/scholar_library_content_policy.md).
