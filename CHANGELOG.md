# Changelog

All notable changes to Muslim are documented here. Release notes use the same
sectioned format as v1.10.0 and are generated from the commits for each tag.

## Muslim v1.25.22

### Quran Surah List UI/UX Refinement

- Replaced scattered spacing values in the Khatma progress card with the shared Islamic spacing tokens for a more consistent reading-oriented rhythm.
- Marked each surah row as a semantic button so keyboard, switch-access, and TalkBack users receive a clear interactive affordance while opening a surah remains unchanged.
- Preserved offline Quran data, surah navigation, bookmarks, playback actions, reading progress, translations, and existing RTL/LTR behaviour.

### Verification

- UI, accessibility, responsive-layout, visual-identity, documentation, and design-system guards passed locally.
- The main CI workflow passed Quality (build, unit tests, Lint, and Detekt), Android emulator tests, Closed beta APK generation, and signed release artifact generation before this tag.
- This remains an incremental UI/UX refinement and does not claim that every application surface has been completely redesigned.

## Muslim v1.25.21

### Prayer Home UI/UX Refinement

- Replaced scattered Prayer Home spacing values with the shared Islamic spacing tokens for a more consistent visual rhythm across prayer cards, day navigation, and daily/monthly actions.
- Added an explicit accessibility state description for the next prayer row so TalkBack users can distinguish it from the other prayer times without relying on colour alone.
- Preserved the existing prayer calculation engine, Muslim World League and Isha settings, GPS flow, per-prayer customisation, notification behaviour, daily/monthly navigation, and completion tracking.

### Verification

- The preceding implementation commit passed UI/UX contract checks, accessibility checks, responsive-layout checks, Detekt, the full Quality workflow, Android emulator tests, Closed beta APK generation, and signed release-artifact generation in CI.
- This release remains an incremental UI/UX refinement; it does not claim that every application screen has been completely redesigned.

## Muslim v1.25.20

### Shared UI Component Foundation

- Added a reusable `IslamicListItem` for secondary navigation and settings-style surfaces, with a consistent title/subtitle hierarchy, semantic Material colours, RTL-safe trailing affordances, and a comfortable 64dp minimum row height.
- Updated the More hub to use the shared list-item component instead of a screen-local card implementation, reducing visual duplication while preserving existing section ordering, hide/show preferences, and navigation destinations.
- Added localized Arabic and English accessibility text for opening More items and extended the design-system CI guard so this shared component remains adopted by the hub.
- Kept prayer calculations, GPS recovery, Hadith Paging, Quran navigation, notifications, local-first data boundaries, and current launcher/notification identity unchanged.

### Verification

- UI/UX static guards, accessibility checks, responsive-layout checks, Islamic visual-identity checks, and Detekt passed locally.
- The tagged workflow must pass Quality, emulator tests, and signed artifact generation before this release is considered published.

## Muslim v1.25.18

### Home Screen UI/UX Refinement

- Improved the Home screen's location control with a clear localized action description for screen readers while preserving the existing location-selection flow.
- Added an accessible summary for the next-prayer card, including the prayer name, scheduled time, and remaining countdown.
- Made the daily/monthly view and sharing actions responsive equal-width controls so translated labels remain readable on narrow phone screens.
- Added a CI-protected UI/UX contract covering accessibility semantics, localization, and responsive action layout.

## Muslim v1.25.19
### GPS Selection and Dynamic MWL Isha
- Serialized the complete location-selection action from GPS acquisition through coordinate-to-IANA resolution, persistence and refresh work, preventing overlapping taps or permission callbacks from racing through the same ViewModel flow.
- Preserved the recoverable GPS provider boundaries and the latest device-level GPS failure and success-flow regression coverage already present on main.
- Added a calculation regression that explicitly keeps the default Muslim World League Isha input at a 17° solar angle with zero fixed-minute interval and verifies location/date-dependent output. No fixed `22:08` time or hidden Isha offset is used.

## Muslim v1.25.17

### Silent Qibla Alignment and GPS Selection Resilience

- Removed the automatic `ToneGenerator` confirmation beep from Qibla alignment. The compass retains a non-audio haptic pulse only, so Qibla alignment cannot emit the unwanted system notification tone.
- Hardened the shared fused-location request: coroutine cancellation now cancels the active Play Services request, and failures while registering task listeners remain contained as the existing recoverable unavailable-location state.
- Isolated post-save work after a valid GPS fix. Alarm rescheduling, countdown foreground-service refresh, and widget refresh are best-effort follow-up actions; failures in those derived services cannot discard the saved location or terminate the picker flow.
- Added failure containment inside the Next Adhan countdown service for foreground-service startup and asynchronous notification refresh failures.
- Added regression coverage for Play Services listener-registration failure, successful GPS persistence when a derived scheduler fails, and a device-level Compose flow that presses **Use my current location** and returns through the saved callback.

## Muslim v1.25.16

Updates install directly over v1.25.15 or earlier releases with the same package
name (`org.muslim.app`) and stable signing identity. No uninstall or data reset
is required.

### Silent Qibla Alignment and GPS Selection Resilience

- Removed the automatic `ToneGenerator` confirmation beep from Qibla alignment. The compass now provides an optional non-audio haptic pulse only, so entering or aligning the Qibla screen cannot emit a system notification tone.
- Hardened the shared fused-location request after the Android permission flow: coroutine cancellation now cancels the active Play Services request, and failures while registering task listeners are contained as the existing recoverable unavailable-location result.
- Isolated the post-save work triggered by a valid GPS fix. Alarm rescheduling, the countdown foreground-service refresh, and widget refresh are now best-effort follow-up actions; a platform, notification, OEM, alarm, or widget failure can no longer discard the stored location or terminate the picker flow.
- Added failure containment inside the Next Adhan countdown service for foreground-service startup and asynchronous notification refresh failures, preventing a derived notification update from crashing the app process after location selection.
- Expanded regression coverage with a Play Services listener-registration failure test, a successful GPS-save test with a failing derived scheduler, and a device-level Compose test that presses **Use my current location**, persists a valid fix, and returns through the saved callback. The existing static contract now prevents the automatic Qibla tone and the new GPS safety boundaries from being removed.

### Nearby Mosques Static-Analysis Clean-up

- Removed two unused imports (`kotlin.math.PI` in the mosque repository and the
  Compose `padding` modifier in the mosque tab) that Detekt flagged as dead code
  in the Nearby Mosques feature shipped in v1.25.15. This is a source-cleanliness
  change only: the mosque search, distance ordering, radius persistence, and
  external-directions behaviour are unchanged.

## Muslim v1.25.15

### Nearby Mosques in Qibla

- Added a second, localized **Nearby Mosques** top tab inside the existing Qibla screen. The compass, sensor lifecycle, calibration guidance, and Qibla-direction calculation remain in the original tab, while the mosque lookup starts only when the new tab is entered.
- Reused the established foreground-location permission path and injected `LocationProvider`; no additional location client, permission manager, tracking loop, embedded map, map tile renderer, Google Maps SDK, or maps API key was added. The lightweight Overpass query is bounded to 1, 3, 5, or 10 km (5 km by default), rate-conscious, cancellable, and sent through the shared network client with the application user agent.
- Added locally calculated Haversine distances and nearest-first ordering, optional addresses, and external directions. Google Maps is preferred when installed, with the standard `geo:` intent as the compatible fallback; both routes retain the stored mosque coordinates rather than user coordinates.
- Persisted the selected search radius and raw mosque places through the existing preferences store. Cached place names can appear immediately during a new location fix without showing stale distances; once a current fix is available, distances are recalculated locally. Fresh cache avoids unnecessary requests, while stale/offline cache and location/permission failures expose explicit recoverable UI states.
- Added Arabic and English resources, screen-reader descriptions, unit coverage for query boundaries, parsing/cache freshness, local ordering, explicit states, cache-first presentation, cancellation, radius persistence, and exact navigation coordinates. Added a device-level tab-selection regression and a CI contract guard that prevents regression to a duplicate location stack or embedded-map implementation.

## Muslim v1.25.14

### GPS Crash Resilience and Regression Coverage

- Removed the remaining construction-time failure path from GPS selection. The fused Google Play Services client and Android framework location manager are now resolved only when a current location is requested and are each contained behind a recoverable boundary. A device with unavailable, corrupted, or vendor-modified location services therefore receives the existing retryable GPS-unavailable state instead of a process-terminating exception.
- Preserved both Android foreground-location grants: precise permission continues to request a high-accuracy live fix, while approximate permission remains a supported path with local fused and framework fallbacks. Provider results are now rejected before persistence when latitude or longitude is non-finite or outside the valid geographic range.
- Added five Android-environment unit regressions for deferred initialization, simultaneous Google Play Services and OEM framework failures, approximate-permission fallback, invalid coordinates, and missing permission. Added ViewModel regressions that prove provider and reverse-geocoding exceptions are surfaced as the recoverable `gps_failed` state.
- Added a device-level no-crash regression to the location module and made it an explicit part of the required Android emulator suite. The release pipeline now exercises this exact failure mode on a booted Android emulator before a tagged release can publish.

### Verification

- The release candidate passed debug assembly, unit tests, Android Lint, Detekt, and the Android emulator suite in CI. The GPS recovery contract guard also verifies that the deferred initialization, safety checks, and regression tests remain present in future changes.

## Muslim v1.25.13

### GPS startup resilience

- Moved construction of the fused Google Play Services client and Android `LocationManager` behind recoverable, application-context lookups. A device whose Play Services or OEM location stack fails during client creation now returns the existing retryable GPS-unavailable state instead of terminating the process before a location request can begin.
- Preserved approximate/precise permission support, live high-accuracy retrieval, fused and platform fallbacks, coordinate-derived IANA timezone resolution, and normal coroutine cancellation behavior. Added a CI guard that prevents client or platform-manager construction from escaping the GPS recovery boundary.

## Muslim v1.25.12

### Arabic Hadith Index and Device-Path Reliability

- Localized all **417** collection-scoped Hadith chapter-index labels for the Arabic UI. The original source chapter title remains the Room/Paging/navigation key, while English and non-Arabic interfaces retain their existing source labels.
- Reworked synthesized-Adhan `AudioTrack` ownership around one synchronization lock. Every native write, start, pause, volume update, invalidation, stop, and release now shares the same session boundary, and ownership is cleared before release to prevent a stale writer from reaching Android’s invalid track state.
- Added a second defensive failure boundary to the fused-location provider and one unified recoverable coroutine boundary for GPS, manual coordinates, city selection, reverse geocoding, coordinate-to-IANA resolution, persistence, alarm rescheduling, countdown refresh, and widget refresh. Cancellation remains propagated normally.
- Added CI checks that require complete Arabic chapter-label coverage and serialized audio-track release/write handling, while preserving the existing Hadith paging, Isha, notification, and GPS contract checks.

## Muslim v1.25.11

### Reliability and Adaptive Islamic Design

- Fixed a concurrent synthesized-Adhan audio race that could attempt an `AudioTrack` write after the track had been stopped or released. Invalidated platform audio streams now end the fallback session safely instead of propagating a playback exception to the application.
- Hardened GPS selection so provider, reverse-geocoding, timezone-resolution, persistence, and rescheduling failures are reported through the existing recoverable location state. Coroutine cancellation still propagates normally, and saved coordinates continue to require a coordinate-derived IANA timezone.
- Fixed per-book Hadith preparation in Android packages that expose compressed assets without the `.gz` suffix. The repository now opens the selected collection as a GZIP stream when available and falls back to Android’s unpacked NDJSON representation while retaining bounded 150-row imports, book scoping, chapter indexing, and Paging.
- Replaced technical Hadith preparation errors in the UI with a localized, retryable offline-data message. File paths and implementation exceptions are no longer presented to readers.
- Replaced the illustrative Hadith catalogue covers with compact resources derived from verified public-domain photographs of physical Hadith volumes and historical manuscript/title-page sources. The provenance record documents each resource and does not represent a general volume photograph as a specific publisher edition.
- Added a central application scaffold, adaptive content frame, and reduced-motion preference provider. The prayer home, GPS location, Hadith library, More hub, and Settings now share a deliberate width bound on large screens while specialist reading surfaces keep their own layout responsibility.
- Added CI guardrails for the AudioTrack/GPS/Hadith failure recoveries and central adaptive design-system adoption, alongside the existing accessibility, identity, prayer-calculation, notification, content, and paging checks.

## Muslim v1.25.10

### Lazy Hadith Library, Location Recovery and Prayer Alerts

- Replaced the single eager Hadith corpus with nine independently compressed, versioned book assets and a compact catalogue. Opening the library now reads only collection metadata; selecting a book imports that book alone in bounded 150-row transactions, indexes its chapters locally, and displays its pages through Paging. The catalogue currently covers Sahih al-Bukhari, Sahih Muslim, Sunan Abi Dawud, Jami at-Tirmidhi, Sunan an-Nasai, Sunan Ibn Majah, Muwatta Malik, Riyad as-Salihin and Forty Hadith of al-Nawawi, with source and distribution records tracked per bundled asset.
- Redesigned the Hadith entry experience around original local book-cover artwork, collection cards, chapter indexes, and book-scoped search. No hidden all-library import is triggered by entering the catalogue or by the daily-Hadith worker.
- Restored resilient foreground GPS selection for prayer times. The permission entry accepts Android approximate or precise foreground location, requests a high-accuracy live fix when precise access is available, and falls back to the most recent fused or platform location only when needed. Every selected coordinate still resolves its local IANA timezone before saving, never the device timezone.
- Kept the default Isha calculation on the documented Muslim World League profile: 17° solar depression, zero manual Isha adjustment by default, and the configured Seventh of the Night high-latitude bound. Added explicit assertions alongside the Adhan-reference seasonal and global Isha vectors; no Berlin-specific branch or hidden Isha compensation has been introduced.
- Promoted Ramadan to the bottom navigation only while the app-wide adjusted Umm al-Qura date is in Ramadan. It appears between Quran and Qibla, making four primary destinations plus More; outside Ramadan the existing More shortcut remains and no background scheduler is required for the rule.
- Made the next-prayer time green in the permanent prayer-time notification and made remaining/elapsed duration red in both the live notification and settings preview. The old duration-colour picker was removed because it no longer represented the live notification behavior.
- Removed the prayer-row route to the full settings screen. Its alert icon now opens the persisted per-prayer customisation dialog directly and leaves the user on the prayer-times home screen. The dialog derives safe width and content-height bounds from the device, remains scrollable on constrained displays, and has a persisted comfortable/compact information-density option without hiding functional controls.
- Preserved and strengthened the active Adhan contract: the foreground card is public on the lock screen, high priority, ongoing, non-auto-cancelable, and has one executable Stop Adhan action that does not require unlock authentication. Removing the app task reaffirms the active card; natural playback completion or that explicit Stop action ends the service. Android system/channel settings still control the final availability of lock-screen and heads-up presentation. Hardware volume keys retain their Android-owned volume behavior and are not represented as an unreliable stop control.
- Added CI guardrails for lazy Hadith asset inventory, GPS permission and IANA-zone handling, MWL Isha defaults, semantic countdown colours, direct home-dialog customisation, and the non-dismissible Adhan lock-screen lifecycle.

## Muslim v1.25.9

### Prayer-Time Calculation Integrity and Global Baseline

- Replaced the country-driven automatic calculation-method path with an explicit global Muslim World League baseline. Standard Asr and the Seventh of the Night high-latitude rule are now the persisted defaults; all supported methods, the Hanafi Asr choice, the other two high-latitude rules and manual per-prayer offsets remain deliberate user settings.
- Introduced one immutable `PrayerCalculationProfile` built from saved settings and consumed by the prayer home, countdown, widget, Adhan scheduler, notification settings, Ramadan surfaces and travel high-latitude preview. This removes duplicate parameter assembly and keeps the rendered civil minute and scheduled Adhan instant aligned.
- Preserved the astronomical instant after explicit offsets for validation and diagnostics, then rounds exactly once to the final minute shared by display and alarm scheduling. The transit interval is now applied by the calculator before that final rounding.
- Replaced the manual/GPS location path’s device-timezone assignment with a local coordinate-to-IANA resolver. It runs off the UI thread, keeps location data on-device and refuses to persist a new coordinate when an IANA zone cannot be resolved rather than silently scheduling it in an unrelated device timezone. An incomplete legacy location without a stored IANA zone is now treated as unset until the user selects or saves a valid location again.
- Added 2026 Muslim World League / Seventh of the Night regression vectors generated from the verified Adhan Kotlin reference implementation: five Berlin seasonal dates and global cases for London, Stockholm, Oslo, Helsinki, Reykjavik, Toronto, Riyadh, Cairo, Istanbul, New York, Tokyo, Singapore and Sydney. The existing Edinburgh high-latitude variants and polar guards remain in place.
- Added a CI calculation-integrity guard that protects the global defaults, unified profile, one-time rounding, local IANA lookup and removal of country-method forcing.

## Muslim v1.25.8

### Per-Prayer Alert Controls and Live Adhan Ownership

- Added a clear alert-status icon to each prayer-time row. For the five scheduled prayers, it opens the existing full customisation dialog with alert type, bundled Adhan selection, live preview, per-prayer or global volume, vibration and manual time adjustment; sunrise remains visibly unavailable because it is not scheduled as an Adhan alarm.
- Consolidated the dialog save path into one persisted update so a single reschedule observes a complete per-prayer configuration rather than partial changes.
- Moved the active Adhan notification’s normal publication into the live foreground service that owns playback, preventing a receiver-owned preliminary card from losing foreground-service protection while audio is still active.
- Preserved the explicit Stop Adhan action as the sole intentional termination path; the service reaffirms its active card if the app task is removed, and the one-time direct-audio recovery retains the same ongoing card while fallback sound plays.
- Extended the static Adhan lifecycle guard to require service ownership, task-removal recovery and the home-screen per-prayer customisation entry point.

## Muslim v1.25.7

### Travel and Expat Surface Consistency

- Unified the travel-distance controls and assessment, travel guidance, offline compass, and high-latitude planning surfaces on shared Modern Islamic Minimalism cards and semantic state treatments.
- Promoted location, calculation-context and travel guidance notices to shared informational or neutral state surfaces, while presenting GPS failure through the shared critical state.
- Standardised the GPS refresh, departure-location and prayer-settings actions with shared accessible buttons; GPS permissions, origin storage, distance assessment, compass direction, high-latitude preview and prayer-settings navigation are unchanged.
- Extended the visual-identity verifier to protect the shared travel and expat surface, action and critical-state treatment.

## Muslim v1.25.6

### Hajj Days Calculator Surface Consistency

- Unified the Hajj-days calculator introduction, entered-date result and key-day cards on the shared Modern Islamic Minimalism surface primitive.
- Replaced the screen-local invalid Hijri-date error with the shared critical state surface, giving a clearer recovery hierarchy without changing digit handling, parsing, calendar calculation, Gregorian conversion or relative-day semantics.
- Extended the visual-identity verifier to require the shared Hajj calculator card and critical-state treatment.

## Muslim v1.25.5

### Family Guidance Surface Consistency

- Unified Ruqyah passages and audio rows, baby-name results and empty search feedback, Aqiqah application/reminder/date surfaces, and family guidance articles on the shared Modern Islamic Minimalism card and state primitives.
- Replaced screen-local informational and caution cards with semantic positive and warning surfaces, preserving their original educational scope, translation selection and safe external-audio validation.
- Replaced the Aqiqah form’s local outlined control with the shared accessible secondary action while preserving date parsing, schedule calculation and reminder availability rules.
- Extended the visual-identity verifier to ensure the Family Life experience continues to use the shared card, state and secondary-action primitives.

## Muslim v1.25.4

### Reading, Planning and Utility Surface Consistency

- Applied the shared Modern Islamic Minimalism card, action, section and state primitives to the Ramadan overview, the reusable Ramadan habit tracker, Zakat calculator, reference library, and Quran downloads hub.
- Refined Ramadan into calmer primary and planning surfaces while preserving fasting-day tracking, Iftar/Suhoor timing, reminder controls and the local habit model. Location-dependent Iftar recovery is now presented through a semantic critical state surface.
- Standardised Zakat price retrieval, Nisab outcomes, saved-calculation action and history-empty feedback without changing calculation inputs, country/currency selection, local history or optional price providers.
- Reworked reference-library book, search-empty and reading-paragraph surfaces for more consistent scholarly hierarchy while retaining Arabic/English content, search, copy/share and internal book/topic navigation.
- Applied the same low-elevation, semantic surfaces and accessible primary action to Quran recitation-download summaries, coverage, reciter status and active transfer cards without changing scopes, queue management, downloads or deletion confirmation.
- Extended the visual-identity verifier to guard this secondary-surface component adoption alongside existing theme, accessibility and system-icon checks.

## Muslim v1.25.3

### Persistent Adhan Delivery and Notification Lifecycle

- Made the active Adhan notification a non-dismissible, ongoing foreground card with one explicit **Stop Adhan** action. It is public on the lock screen, requests high-priority heads-up presentation, and remains present while the live service owns playback.
- Separated settings previews from live scheduled Adhan sessions. The settings stop control now terminates an explicit preview only; it cannot stop a live Adhan session.
- Removed the legacy notification-dismissal policy and its settings controls. Natural audio completion and the explicit notification action remain the intentional end points, while service commands are redelivered if Android recreates the process mid-playback.
- Cancels the pre-prayer reminder as the real Adhan window begins and when the active Adhan card is posted, preventing both cards from appearing together.
- Rotated the Android Adhan channel to `adhan_alert_v3` with the existing high-importance, audible, vibrating defaults. This gives existing installs a fresh channel whose initial configuration supports a lock-screen card and heads-up alert; Android system settings remain user-controlled.
- Extended Android regression coverage for the ongoing/non-auto-cancelable, public, high-priority Adhan notification, its single explicit action, and reminder retirement.

## Muslim v1.25.2

### Secondary Experience Polish

- Refined the Qibla screen into a calm, responsive hierarchy with a semantic location surface, accessible compass description, current-location recovery state, and a device-independent geometric Qibla marker instead of an emoji glyph.
- Made Tasbih more tactile and accessible by preserving its single generous counting action, adding a clear screen-reader description, applying semantic progress-ring colours, and standardising devotional, sound, and action surfaces.
- Reworked the Learning Centre into a structured knowledge experience with visually distinct destinations, calmer topic rows, consistent section hierarchy, and readable step and note surfaces without changing learning content or favourites.
- Unified Islamic Finance guidance, provider selection, debt validation, empty states, ledger entries, and primary actions on the shared design-system surfaces while retaining local data, provider, and calculation boundaries.
- Improved the glanceable Wear OS companion hierarchy for next prayer, countdown, Tasbih, synchronization, and explicit haptic-state feedback; no paired-phone or Wear data contract changed.

## Muslim v1.25.1

### Strict System Icon Cleanup

- Rotated the phone launcher, round-launcher, themed monochrome, status-bar, and dedicated Wear OS launcher identities again to fresh `v2028` resources, then removed every packaged retired resource so no current producer can resolve the retired identity.
- Rotated the active Adhan card from `1012` to `1014`, the persistent next-prayer countdown from `1013` to `1015`, and Quran `MediaStyle` playback from `7007` to `7008`. Each migration cancels the latest retired card and every documented older card.
- Added an internal `ACTION_MY_PACKAGE_REPLACED` receiver that performs cleanup immediately after an in-place upgrade, without depending on an application launch or foreground-service restart.
- Extended direct Android instrumentation coverage for the expanded Adhan, countdown, and Quran-retirement lists, and updated the identity repair documentation to state Android-owned cache and template limits precisely.

## Muslim v1.25.0

### System Notification Identity Repair

- Replaced the Android-facing launcher, round-launcher, themed monochrome, and status-bar resource identities with `v2027` resources built from the approved geometric Islamic mark. The full-colour mark remains exclusive to the adaptive launcher; the dedicated notification glyph remains monochrome for Android system tinting.
- Updated every current notification producer to use `ic_muslim_status_bar_v2027`, including active Adhan, next-prayer countdown, Quran recitation playback and download surfaces, reminders, Ramadan, Adhkar, Hadith, finance, learning, and app-update notifications.
- Rotated the active Adhan notification from `1010` to `1012` and the persistent next-prayer countdown from `1011` to `1013`. Their migration paths now remove every recorded retired card before a current card is posted, preventing an older system-retained card from coexisting with the new identity.
- Rotated the Quran `MediaStyle` foreground notification from `7006` to `7007`. The application-start and service-start paths explicitly cancel the retired card before the current playback card is published.
- Removed retired `v1252` and `v2026` launcher and status-bar resource files from production resources. The manifest now resolves only through the new `v2027` adaptive launcher and round-launcher identities.
- Kept the Adhan, countdown, and Quran media builders free of an app-provided `setLargeIcon`; Android may present the current application icon through its own notification template, while the small icon remains the system-compliant monochrome glyph.

### Documentation and Regression Coverage

- Added a dedicated system-notification repair note with the resource/notification migration map, upgrade behaviour, verification scope, and Android-owned rendering limits.
- Updated the repository README, project status, visual-identity note, and documentation index to distinguish full-colour launcher artwork from monochrome system-notification glyphs and to link only the maintainer’s confirmed public GitHub and Ko-fi pages.
- Extended Android instrumentation coverage for retained Adhan and countdown cards, the `v2027` application identity, and Quran recitation-card retirement. Added unit coverage that requires the countdown’s current and retired IDs to remain distinct.

### Verification

- v1.25.0 was built and published after the tag workflow passed its debug builds, unit tests, Android Lint, Detekt, emulator tests, signed release checks, and artifact publishing steps.

## Muslim v1.24.16

This update installs over v1.24.15 with the same package name (`org.muslim.app`) and stable signing identity. It preserves every saved Adhan selection and the exact global/per-prayer volume, including existing low-volume settings.

## Notification and Launcher Identity Repair

- Migrated the launcher, round launcher entry point, themed monochrome layer, and every notification small icon to the fresh `v1252` identity. The launcher foreground is now placed inside a 34% transparent safe margin so Android adaptive masks can show the complete supplied artwork instead of clipping its edges.
- Removed all `v1251` launcher, status-bar, and active-Adhan large-image resources from the application package. The active Adhan card no longer attaches a large icon, preventing OEM templates from rendering a stale/second badge beside the current app identity.
- Migrated active-Adhan delivery from id `1001` to id `1005`. App startup, direct alert posting, and foreground-service startup each cancel the retired id, so a saved card from an earlier APK is removed without waiting for the next prayer.

## Adhan Delivery Hardening

- Kept the exact-alarm receiver and `mediaPlayback` foreground service as the primary delivery path, then strengthened the offline AudioTrack recovery path for current Android background-audio constraints.
- Replaced the long static AudioTrack buffer with a checked streaming path that verifies initialization and every PCM write before recording an audio start. It now uses `USAGE_ALARM` with `CONTENT_TYPE_SONIFICATION` consistently for MediaPlayer, AudioTrack, and transient exclusive audio focus.
- Added a terminal journal failure when the direct recovery path cannot start output, so Verify Adhan shows a stage-specific condition rather than leaving a generic unconfirmed result.
- Extended the real APK delivery probe to run at the retained user-selected 17% Adhan volume and assert that the value remains unchanged.

## Verification

- Added a device-level migration test that posts a retired active-Adhan card and verifies its explicit cancellation. The existing real exact-alarm probe continues to require both active-notification retention and confirmed audio start.
- Completed the local CI-equivalent audit: bundled-content inventory, phone and Wear debug assembly, unit tests, Android Lint, and Detekt across 396 Kotlin files. Release publication remains gated on the Android 14 emulator and full CI.

## Muslim v1.24.15

This release installs directly over v1.24.14 or earlier releases with the same package name (`org.muslim.app`) and stable signing identity. No uninstall, data reset, or change to Adhan sound settings is required.

## Unified Visual Identity

- Replaced the launcher foreground, adaptive monochrome layer, round launcher entry point, active-Adhan large image, and every small notification entry point with the newly supplied teal-and-gold Islamic identity.
- Removed the retired launcher and notification identity resource files rather than leaving them packaged alongside the replacement. The Android manifest now points only to the new adaptive launcher resources.
- Kept notification small icons intentionally monochrome, as required by Android status bars. The new crescent, arch, and orbit glyph is a fresh single-color representation of the supplied identity; the supplied full-color artwork is reserved for the launcher and expanded active-Adhan alert.

## Two-Line Next-Adhan Card

- Rebuilt the permanent, silent next-Adhan countdown so its collapsed form contains exactly one line: the next Adhan name, its wall-clock time, and the remaining duration. The remaining-duration segment is styled with the user-selected emphasis color (red by default).
- Moved the missed-Adhan information to the expanded surface only. Its one additional line contains the missed Adhan name, its wall-clock time, and the elapsed duration, with the elapsed segment styled using the same emphasis color.
- Kept the countdown card free of a large image and retained the explicit cancellation of the retired `1003` card before the current `1004` card is posted. This prevents an older saved card from visually coexisting with the new countdown.

## Verification

- Added an application-level Android test that verifies the compact line excludes missed-Adhan information, the expanded line contains it without extra lines, and both duration segments carry the red foreground span. The countdown builder continues to omit a large-icon call; Android may still attach internal metadata for a small icon, so this is not inferred from that system-generated field.
- Retained the device test that posts and then cancels the retired countdown identity. Local APK assembly, prayer-times unit tests, and application Android-test compilation passed before CI gating.

## Muslim v1.24.14

Updates install directly over v1.24.13 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Countdown Identity and Service-Reach Repair

- Migrated the permanent, silent next-prayer countdown from notification id `1003` to the fresh id `1004`. Every service start explicitly cancels the retired ongoing card before posting the new card, removing a status notification retained by Android from older builds and preventing its old large artwork from surviving an update.
- Kept the replacement countdown card deliberately separate from the active Adhan alert: it uses only the current monochrome status-bar glyph and does not attach a large branded image. The system's single application identity remains the only branded icon in the collapsed notification surface.
- Added a bounded foreground-service reach watchdog for Audible Adhan delivery. If Android accepts `startForegroundService()` but the service never reaches its own journal checkpoint, the receiver cancels the stalled start and launches an offline synthesized AudioTrack fallback under a partial wake lock using the exact sound volume chosen by the user.
- Preserved the service-owned MediaPlayer and AudioTrack recovery path whenever the service does start, avoiding a competing player or any automatic modification of global/per-prayer sound settings.

## Regression Coverage

- Added a device test that posts a retired countdown card then verifies the migration cancels it from Android's active-notification list. Added a unit test enforcing the distinct retired and current countdown identities.
- Passed local app unit tests, Android Lint, Detekt, Debug assembly, and compilation of the application-level device suite. The release remains gated on the Android emulator and full CI before public publication.

## Muslim v1.24.13

Updates install directly over v1.24.11 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Adhan Audio Startup Repair

- Corrected the audio-start failure evidenced by the on-device **Verify Adhan** result. The short-lived broadcast receiver no longer starts a competing audio fallback while the foreground playback service is still preparing MediaPlayer. The playback service now owns the complete audio lifecycle under its wake lock.
- Added a two-stage offline recovery path. If a bundled or custom MediaPlayer source does not reach playback in time, the running service replaces it with a local synthesised AudioTrack fallback. It records recovery as an in-progress `AudioFallbackStarted` stage and only reports a terminal error if AudioTrack also fails to enter the playing state.
- Added a bounded local fallback even when the foreground-notification start fails, so an Android foreground-notification error cannot by itself turn an otherwise valid Adhan request into silence. All paths retain the exact user-selected sound mode and global/per-prayer volume, including low values such as 17%; they are never reset or rewritten.
- Extended the Verify Adhan observation window to cover the exact-alarm dispatch, MediaPlayer startup timeout, and AudioTrack fallback window. The screen therefore waits for the final delivery result instead of declaring an early failure while recovery is still running.
- Corrected a false-negative notification check exposed by the Android 14 emulator: Android can accept `NotificationManager.notify()` before its active-notification list is updated. The app now waits a bounded confirmation window before reporting that the active Adhan alert was not retained.
- Tightened release automation so signed artifacts and public GitHub releases now depend on successful quality and real-APK emulator jobs; a failed device test cannot publish a future tagged release.

## Real APK Instrumentation Coverage

- Moved Adhan and prayer-home device tests from a standalone feature-library test APK to the actual Muslim application APK. The tests now execute with `MuslimApplication`, Hilt, the merged production manifest, the registered receiver, foreground service, Android notification channel, and AlarmManager.
- Added an end-to-end scheduled-probe device test that grants the required test permissions, persists a real location and audible configuration, schedules the same exact probe as Verify Adhan, and requires receiver reach, retained active alert, and confirmed audio startup.
- The full CI run passed content checks, unit tests, Android Lint, Detekt, signed release artifacts, the real-APK emulator suite, and the new scheduled Adhan probe. A physical user handset is not connected to the release environment, so physical audibility is still verified on-device after installation.

## Muslim v1.24.11

Updates install directly over v1.24.10 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Verified Adhan Alert Delivery

- Replaced the previous success-by-exception model for the active Adhan alert. The app now checks the Android notification runtime permission, global application notification state, and the user-owned Adhan channel before posting. It records the specific blocked condition instead of interpreting a non-throwing `notify()` call as proof that the user could see an alert.
- After posting, the receiver confirms that Android retains the active notification with the expected Adhan identifier and channel before the scheduled verification reports a visible alert as posted. The settings readiness card now uses the same system preflight as the receiver, so its permission/channel result cannot disagree with real delivery.
- Corrected a race in the audio fallback path. The previous implementation stopped the foreground service immediately before starting the fallback through the same singleton player; service teardown could then stop the newly started fallback. The fallback now replaces the pending player safely, while the existing bounded service timeout performs cleanup later.
- Added a bounded partial wake lock to the rare direct-fallback path, keeping the CPU awake long enough for audio startup and completion when Android rejects a foreground-service start. The user's selected sound option and exact global or per-prayer volume remain unchanged.

## Regression Coverage

- Added an Android emulator test that grants notification access, posts the actual Adhan alert, and asserts that it appears in Android's active-notification list with the current Adhan channel. This test runs alongside the existing prayer-home device tests.
- Retained the existing checks for exact-alarm access, receiver reach, foreground-service start, visible alert result, and confirmed audio start. The release environment has no connected physical handset, so on-device audibility is never claimed without user-device evidence.

## Muslim v1.24.10

Updates install directly over v1.24.9 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Non-destructive Adhan Delivery

- Removed the destructive Adhan "recovery" behavior. App startup and **Verify Adhan** now preserve the user's global and per-prayer volume, sound mode, bundled recording, custom file selection, and global/per-prayer volume mode exactly as saved. A silent, vibration-only, or low-volume choice is reported as a diagnostic condition; it is never rewritten to 80% or replaced with another sound.
- Replaced the retired Android Adhan channel with the fresh high-priority `adhan_alert_v2` channel. Android retains the behavior of an existing notification channel after creation, so the new identifier prevents a legacy silent app-created channel from being reused while still leaving all subsequent system channel controls to the user. [1]
- Extended the scheduled delivery journal to record the active Adhan alert as **posted** or **blocked** independently of receiver reach, foreground-service startup, and confirmed audio startup. The readiness card now requires both a posted active alert and confirmed audio output before it reports a successful scheduled verification, and it shows the recorded failure detail when Android rejects notification posting.
- Receiver failures are now recorded in the delivery journal rather than ending silently. The audio fallback path remains independent of visible-notification delivery, while respecting the exact volume and sound mode selected by the user.

## Strict Notification Identity Replacement

- Replaced all current launcher, large-Adhan, and monochrome status-bar resource identifiers with v1.24.10 identifiers. The application manifest now references only the new standard and round adaptive launcher resources; retired source assets and references are removed.
- Kept the official full-colour circular emblem for the launcher and expanded Adhan/reminder artwork. Every notification producer uses the fresh, correctly framed monochrome circular status-bar glyph, as required by Android's small-notification-icon rendering.
- Removed the large branded artwork from the permanent low-importance prayer-countdown status card. It remains a deliberately silent status notification and can no longer visually resemble the active Adhan alert.

## Verification

- Added regression coverage for non-mutating low/silent Adhan diagnostics, fresh Adhan-channel identity, independent active-notification status, and readiness requirements for both visible alert posting and audio startup.
- Passed content-manifest generation, focused prayer-time tests, core-notifications tests, full app unit tests, Android Lint, Detekt, and Debug assembly locally. A fresh 1.24.10 APK resource audit confirmed package `org.muslim.app`, application label `Muslim`, v1.24.10 identity resources, and no retired icon resource identifiers.
- The release environment has no connected physical Android device, so no claim is made that sound was physically heard here. After installation, run **Verify Adhan** and use the separate active-alert and sound results to identify any remaining device permission, channel, volume, or output-routing condition.

### References

[1]: https://developer.android.com/develop/ui/views/notifications/channels "Android Developers — Create and manage notification channels"

## Muslim v1.24.9

Updates install directly over v1.24.8 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Adhan Self-Recovery and Verification

- Added a self-recovery path for persisted Adhan configurations that cannot produce audible delivery. A stored 1% volume, a Silent choice, or a vibration-only choice for any obligatory prayer is restored to the offline bundled Adhan with one global audible level (80%) when the updated app opens or when **Verify Adhan** is explicitly run.
- The recovery re-schedules prayer alarms immediately. The verification action also rebuilds the Android Adhan channel from its official high-priority defaults before it submits the real scheduled probe, preventing a retained silent channel from hiding the alert under silent notifications.
- Preserved the existing exact-alarm receiver, foreground playback, audio-start journal, wake lock, and direct local fallback. The new recovery removes invalid persisted configuration before those delivery stages run rather than treating a failed prerequisite as a successful test.
- Added regression coverage for low-volume and Silent/VibrateOnly recovery, including the restored global volume and bundled-audio defaults.

## Icon Identity and Android Cache Replacement

- Introduced new launcher and large-Adhan resource identifiers, so Android reloads the official colour emblem rather than reusing a previously cached launcher or notification asset. The launcher foreground is enlarged, centered, and preserved as a complete circle with a 69–70px transparent margin on its 512px canvas.
- Introduced a new dedicated monochrome status-bar resource name and updated every app notification producer to use it. The prior small-icon resource and the prior large-Adhan resource are removed from the source tree.
- Updated the application manifest to the new standard and round adaptive launcher identifiers. The full-colour circular emblem is the launcher and large Adhan artwork; the system-tinted circular crescent-and-mosque vector is the status-bar glyph.

## Quran Reader Simplification

- Made **From the selected ayah to the end of the Mushaf** the reader’s default recitation range. Starting playback from a surah now enters the continuous end-of-Mushaf path; shorter ranges remain explicit choices.
- Removed Quran search and whole-Mushaf word-frequency completely from navigation, reader/list entry points, the More hub, UI state, data helpers, persisted search history, tests, and all localized strings.
- Removed the Quran FTS entity, DAO, dependency injection binding, seed/index work, and repository search API. Room database version 4 drops the retired `ayah_fts` table while retaining Quran content and bookmarks.

## Verification

- Passed production content-manifest verification, targeted Quran/prayer-time tests, full application unit tests, Android Lint, Detekt, and Debug assembly before release tagging.
- The release environment does not have access to the user's physical handset. The in-app scheduled **Verify Adhan** action remains the required on-device proof because it only passes after the receiver/service path records audio startup.

## Muslim v1.24.8

Updates install directly over v1.24.7 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Official Application and Notification Identity

- Adopted the supplied full-colour circular mosque emblem as the official Muslim launcher and large-notification identity. The complete ornamental ring is retained on a transparent foreground with a navy adaptive-icon background, eliminating white corners and preventing launcher-mask clipping.
- Added a dedicated circular monochrome crescent-and-mosque glyph for Android status bars. It is separate from the colour artwork and remains within the 24dp viewport so Android can apply its required white or system tint reliably.
- Added a separate monochrome adaptive-icon layer for Android themed icons and kept both standard and round launcher definitions on the same official colour foreground.
- Replaced the prior large Adhan/next-prayer artwork with the official full-colour emblem. The new large-notification resource retains the complete circle with a transparent guard margin.
- Removed legacy launcher foreground, background, brand bitmap, and the former large-notification vector from the source tree. No launcher or notification source reference remains to those retired assets.

## Verification

- Reviewed the generated colour resources visually and verified that the launcher artwork bounds are centered and fully inside Android's 66/108 adaptive-icon safe-content proportion. The large-notification artwork remains centered with its own non-clipping guard margin.
- Passed production content-manifest verification, Debug assembly, application unit tests, Android Lint, and Detekt before release tagging.

## Muslim v1.24.7

Updates install directly over v1.24.6 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Scheduled Adhan Recovery and Diagnostics

- Reworked all background rescheduling receivers to retain their `goAsync()` broadcast hand-off until persisted prayer settings have been read and fresh alarms have been submitted. This prevents Android from reclaiming the receiver process mid-reschedule.
- Rebuild prayer alarms after `BOOT_COMPLETED`, an in-place app update (`MY_PACKAGE_REPLACED`), and Android's exact-alarm-access grant broadcast. The last case matters because Android cancels future exact alarms when **Alarms & reminders** access is revoked; a later grant no longer depends on opening Muslim manually. [1]
- Kept the permanent next-Adhan countdown optional and non-fatal during recovery: a countdown-service failure no longer stops the critical prayer-alarm schedule.
- Expanded the **Verify Adhan** card with the recorded failing stage from the real scheduled probe. When Android notifications or the Adhan channel are blocked, its recovery control opens the applicable app or channel settings. When exact alarms are unavailable, it opens **Alarms & reminders** directly.
- Preserved the strict delivery evidence rule: a permission check, foreground-service request, or preview never counts as verified audio; only a recent scheduled probe that reaches audio startup passes the sound check.

## Research and Verification

- Added an in-repository Android reliability review with platform references, device-test boundaries, and comparative open-source research. The findings prioritize observable scheduled delivery over speculative feature expansion. [2] [3] [4]
- Added unit coverage for the reschedule actions that recover after an app update and exact-alarm access grant. Focused prayer-time tests, full application unit tests, Android Lint, and Detekt passed locally before release tagging.
- No physical Android device or local AVD was connected to the release environment. Therefore this release does not claim a physically heard Adhan in this environment; after installation, use **Verify Adhan** and follow the shown diagnostic or recovery control if the probe fails.

### References

[1]: https://developer.android.com/develop/background-work/services/alarms "Android Developers — Schedule alarms"
[2]: https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start "Android Developers — Foreground-service background-start restrictions"
[3]: https://developer.android.com/develop/ui/compose/notifications/notification-permission "Android Developers — Notification runtime permission"
[4]: https://developer.android.com/develop/background-work/background-tasks/broadcasts "Android Developers — Broadcasts overview"

## Muslim v1.24.6

Updates install directly over v1.24.5 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Adhan Delivery and Notification Identity

- Replaced the remaining legacy large notification artwork with a dedicated full-colour emerald crescent-and-star vector. The compliant monochrome status-bar icon is unchanged.
- Made the receiver publish the visible Adhan alert before foreground playback begins. A successful foreground-service start is still never treated as proof that audio began; the confirmed-audio journal and direct local fallback remain in force.
- Separated visual-notification permission from the user’s enabled Adhan audio choice. If Android rejects the visible notification request, that failure is non-fatal and the receiver still attempts audible delivery. The foreground service may nevertheless remain subject to Android background-execution policy.
- Added regression coverage for the delivery policy: disabling visual presentation never disables enabled Adhan audio, while disabling Adhan itself stops both paths.

## Quran Search, Tajweed, and Tafsir

- Removed the search-input delay and retained canonical offline matching, so common keyboard spelling such as `الله` matches Uthmani `ٱللَّهِ` immediately. A single-word result now shows that exact word form’s whole-Mushaf frequency using the same canonical tokenization as search and highlighting.
- Moved the optional Tajweed-colour control from **Meanings & Tafsir** into the reader’s outer overflow menu. It remains off by default and the meanings/tafsir dialog no longer contains a duplicate switch.
- Removed the legacy **Sample** tafsir placeholder from upgrades and new reader entry paths. The cited QuranEnc downloader for **At-Tafsir Al-Muyassar** remains the only production tafsir downloader in this release.
- Did not bundle the requested additional tafsir texts without a source-specific, redistributable licence and a no-login retrieval path. QUL’s public catalogue lists the titles but gates JSON/SQLite exports behind sign-in and directs users to verify each resource licence; bypassing that gate or reusing unverified mirrors would not be a lawful production source. [1]

## Verification

- Added focused unit coverage for the notification/audio delivery boundary and Arabic Uthmani normalization. Full application tests, Android Lint, Detekt, content-manifest verification, main-branch CI, and tagged-release CI are required before publication.

### References

[1]: https://qul.tarteel.ai/faq "Quranic Universal Library FAQ"

## Muslim v1.24.5

Updates install directly over v1.24.4 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Adhan Delivery and Prayer Notifications

- Updated the permanent next-Adhan notification to match the current app identity. Its collapsed form now contains only the next prayer name and time; its expanded form shows remaining time and, when relevant, the missed prayer with elapsed time. Attention timing uses the app error colour, red by default.
- Added the matching large adaptive app icon to the next-prayer, Adhan, and reminder notifications while retaining the required monochrome status-bar icon.
- Strengthened audible Adhan delivery. A foreground-service start is no longer treated as proof of sound: the receiver waits for a confirmed audio-start journal entry and stops the service before launching a direct local audio fallback if confirmation is missing.
- Retained the scheduled **Verify** delivery probe as the user-facing evidence path. It verifies application-side audio startup; device volume, output routing, and Android system policy can still affect audibility.

## Private Prayer Accountability

- Moved the five-prayer daily check-in into **Self Accountability**. The same device-local completion record is preserved; it has no account, analytics, synchronization, score, or devotional ranking.
- Made Home visibility an explicit optional preference that is disabled by default. Users may enable the Home card from Self Accountability without creating a second tracker or losing existing entries.

## Quran Search, Tajweed, and Tafsir

- Unified Quran search, word-frequency suggestions, root derivation, occurrence counting, and raw-text highlighting around the same canonical Quran tokenization. Typed hamza variants, tatweel, Uthmani marks, and multi-word prefix input now remain consistent from lookup through result statistics.
- Added regression coverage for a multi-word query containing hamza variants and tatweel, including its count and highlight spans.
- Added opt-in Hafs tajweed colour annotations. They are disabled by default under **Meanings & Tafsir**, are rendered only from validated annotation spans, and are presentation assistance rather than independent scholarly review. The bundled annotations are attributed in the content manifest to `cpfair/quran-tajweed` under CC BY 4.0.
- Added a complete, locally stored downloader for **At-Tafsir Al-Muyassar** from QuranEnc. Every surah is validated against the bundled Mushaf before a complete 6,236-ayah pack replaces an installed copy atomically; the reader shows per-surah progress, supports a retry by downloading again, and never exposes a stale selected source.
- The tafsir control displays the QuranEnc source URL and preserves supplied text unchanged. The current Arabic QuranEnc endpoint does not publish a machine-readable version field, so this release does not invent one or claim that the Arabic tafsir text is translated into the application UI languages. A source without a documented, working automated retrieval path is not exposed as a production download.

## Hadith Library, Qibla, and Navigation

- Moved Daily Hadith notification configuration and its preview into the Hadith Library settings dialog, opened from the upper app-bar settings icon, rather than leaving controls in the library content surface.
- Simplified Qibla to a compass-only experience. The Kaaba emoji marks the bearing; the former pink/gold directional treatment and map mode are gone.
- Removed MapLibre, nearby-mosque discovery, offline-map routing, and the runtime map engine from the application. Learning and history now present their curated location material as accessible text guides rather than interactive maps.

## Quality and Content Controls

- Added source verification notes for QuranEnc and the separate Saadi website. QuranEnc’s published republication conditions require source attribution, unmodified content, transcript preservation, and version reporting when the publisher provides it; the live Arabic endpoint did not expose a version field during review. [1]
- Passed local application unit tests, Android Lint, Detekt, focused Quran/Qibla/prayer-time tests, and the production content-manifest verification before release tagging. Main-branch CI, emulator validation, signed artifact verification, and tagged-release CI remain required before publication.

### Install

- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app remains signed with the same release key, so the update installs directly over prior Muslim releases.

### References

[1]: https://quranenc.com/en/home/api/ "QuranEnc API and republication terms"

## Muslim v1.24.4

Updates install directly over v1.24.3 with the same package name (`org.muslim.app`) and stable signing identity. No uninstall or data reset is required.

## Quran Reader, Search, and Navigation
- Added an explicit **Play from selected ayah** action. After tapping an ayah, the primary selected-ayah action now starts there and continues only to the end of the current surah; it never silently restarts from ayah one.
- Enabled manual Mushaf-style continuation between adjacent surahs. Swiping beyond the first or final page now opens the preceding or following surah at the matching reading edge.
- Made meanings and tafsir opt-in for new installations, keeping the Quran text as the focused first reading view.
- Reworked offline Quran query tokenization and Arabic normalization for hamza variants, tatweel, punctuation, Uthmani marks, and multi-word prefix search. The validated local corpus matcher is now authoritative, so a device-specific FTS issue cannot make search appear empty.
- Added regression coverage for normalized Arabic search input including hamza, tatweel, and punctuation.
- Removed the reader’s fixed night palette override: the dark reader now respects the selected app palette, dynamic colours, and accessibility contrast. Page-edge ornaments frame the Mushaf surface without appearing behind ayahs.

## Prayer Home and Notification Refinement
- Added distinct prayer icons to the next-prayer card and every row of today’s prayer times, while retaining non-colour-only emphasis for the upcoming prayer.
- Added more visible but restrained Islamic corner ornaments to the prayer cards and private daily check-in card.
- Simplified the permanent next-Adhan notification to a compact two-line presentation: a title plus one status line. Remaining time and the optional missed prayer share the attention colour, red by default.
- Unified app-generated notification symbols around a monochrome arch, crescent, and eight-point-star mark derived from the adaptive app identity. The legacy prayer notification drawable is no longer used.
- Replaced the repeated “A good start” habit wording with a concise neutral tracking state.

## Localization and Verification
- Added Arabic copy for the selected-ayah playback action and completed the new resource key across all supported locale files with a safe English fallback.
- Passed local application unit tests, Android Lint, Detekt, and the bundled-content manifest verification before release tagging. Main-branch CI, emulator validation, signing gates, and tagged-release artifacts are required before publication.

### Install
- Download the APK from the GitHub Release and open it (allow “install from unknown sources” if prompted).
- The app remains signed with the same release key, so the update installs directly over prior Muslim releases.

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
