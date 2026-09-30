# UI/UX V2 feature-parity baseline

Captured from `main@9fae19da5f6d073b83b1a45a096467c9ba9d8326` on 2026-09-29.

This document is the functional freeze for the UI/UX V2 migration. A visual redesign may reorganize where an action is shown, but it must not silently remove the capabilities below.

## Primary destinations

- `home`
- `quran`
- `qibla`
- `more`
- `ramadan` when the existing adjusted-Hijri Ramadan rule promotes it

## Navigation routes

Stable/static routes:

- `quran/bookmarks`
- `settings`
- `accessibility`
- `settings/smart-devices`
- `settings/prayer`
- `settings/notifications`
- `settings/permissions`
- `settings/more-order`
- `settings/about`
- `settings/privacy`
- `settings/update`
- `hadith`
- `adhkar`
- `tasbih`
- `ramadan`
- `habits`
- `zakat`
- `finance`
- `learn`
- `learn/family-life`
- `learn/funeral-will`
- `learn/noorani-new-muslim`
- `learn/traveler-expat`
- `reference`
- `history`
- `quran/downloads`
- `scholar-library`
- `scholar-library/study`
- `scholar-library/authors`
- `scholar-library/review`
- `scholar-library/data`
- `location`

Parameterized routes:

- `quran/reader/{surahNumber}?ayah={ayah}&autoplay={autoplay}`
- `settings/prayer?alertPrayer={alertPrayer}`
- `scholar-library/book/{bookId}`
- `scholar-library/path/{pathId}`
- `scholar-library/session/{pathId}`

## External deep-link compatibility

The current app maps these URI families and they must keep resolving after the redesign:

- `muslim://times` → prayer home
- `muslim://qibla` → qibla
- `muslim://settings/update` → update screen
- `muslim://accessibility` → accessibility
- `muslim://settings` → settings
- `muslim://hadith` → hadith
- `muslim://finance` → Islamic finance
- `muslim://noorani` → Noorani/New Muslim
- `muslim://traveler` → traveler/expat
- `muslim://history` → Islamic history
- `muslim://learn` → learning centre

## More hub capability surface

The current More hub exposes:

### Worship
- Adhkar
- Tasbih
- Ramadan shortcut when it is not promoted to the bottom bar
- Habits

### Knowledge
- Hadith
- Learn
- Noorani/New Muslim
- Traveler/Expat
- Family Life
- Funeral/Will
- Reference
- Islamic History
- Scholar Library

### Tools
- Zakat
- Islamic Finance
- Quran downloads

### App
- Accessibility
- Settings

The redesigned More hub may collapse, search, reorder or place these in quick access, but it must keep every destination reachable.

## App-level persisted preferences

The V2 migration must preserve values for:

- themeMode
- dynamicColor
- amoledBlack
- colorPalette
- cardCornerStyle
- ornamentStyle
- ornamentIntensity
- languageCode
- reduceAnimations
- startTab
- timeFormat24h
- accessibilityReadingMode
- informationDensity
- accessibilityHighContrast
- voiceNavigationEnabled
- wearCompanionEnabled
- showPrayerTrackerOnHome
- smartHomeBridgeEnabled
- smartHomeBridgeEndpoint
- moreSectionOrder
- hiddenMoreSections
- updateCheckEnabled
- updateCheckFrequency
- updateChannel
- autoUpdateEnabled
- autoUpdateWifiOnly
- lastUpdateCheckEpoch
- nearbyMosqueSearchRadiusKm
- nearbyMosqueCacheJson
- nearbyMosqueCacheSavedAtEpochMillis
- lastNotifiedUpdateVersion
- updateDownloadId
- updateDownloadVersion
- updateDownloadFileName
- updateDownloadSha256
- updateDownloadVersionCode

## Prayer settings contract

The redesign must preserve:

- calculation method and manual-method state
- custom Fajr/Isha angles
- Asr method
- high-latitude rule
- manual adjustments
- location, timezone and elevation
- global Adhan enablement
- per-prayer Adhan sounds/files
- global/per-prayer Adhan volume
- bundled/custom sound selection
- global/per-prayer vibration
- reminder minutes
- notification dismissibility
- stop-Adhan-on-dismiss behavior
- DND enablement and duration
- Hijri adjustment

The Prayer Home V2 may stop displaying raw volume percentages in the daily list, but those values remain preserved and editable in the customization surface.

## Other persisted feature state

### Notifications
Preserve category enablement, sound, vibration, importance, badge, quiet-hours configuration, missed-Adhan behavior and related appearance settings.

### Adhkar
Preserve overlay appearance, disabled/favorite dhikr IDs, speech settings, morning/evening reminders, periodic reminders, short-dhikr filtering and reminder windows.

### Family Life
Preserve Aqiqah date/reminder settings, article favorites/recents and completed checklist items.

### Hadith
Preserve bookmarks, seed version and daily notification settings.

### Funeral / Will
Preserve content-version acknowledgement, intro/legal/privacy visibility preferences and device-auth draft protection.

### Hajj / Learn
Preserve Hajj checked steps, learning favorites, completed lessons, last opened lesson and quiz answer records.

### Quran
Preserve:
- last Surah/global/within-Surah reading position
- reader theme
- read-through position
- reader font size
- reciter
- supplement enablement/language
- Tajweed enablement
- selected Tafsir source
- night-download schedule
- continuous playback end behavior
- keep-screen-on
- download sorting

### Reference reader
Preserve bookmarks, last-read location and font step.

## User-requested V2 visual requirements frozen at UX00

These are explicit acceptance requirements for later phases:

1. Prayer Home: remove the excessive empty space above the date/header.
2. Prayer Home: remove the duplicated date under “Today’s Prayer Times”.
3. Prayer rows: replace exposed volume percentages with clear state-based customization/alert indicators.
4. Prayer rows: redesign the prayer symbols/icons as one coherent, professional icon family.
5. Prayer Monthly: make the default monthly view an imsakiyah-style timetable with days sequentially from the start to the end of the month and all prayer times shown under prayer headers.
6. Quran Reader: fix overlapping multi-line text highlighting so highlight geometry follows the actual laid-out text and never obscures adjacent lines/glyphs.
7. Across the app: move secondary/advanced options into consistent overflow menus, bottom sheets or expandable sections and recover wasted screen space without removing capability.

## Visual baseline status

The current repository does not yet contain a comprehensive screenshot-regression baseline for every critical screen. UX00 therefore freezes the functional contract immediately and records the required visual baseline matrix below. CI is configured to capture the Prayer Home Arabic/light screen on emulator APIs 26 and 36 as an artifact. Run #1338 uploaded a 40-byte `run-as: unknown package` error message; #1339 failed on a shell-output marker; #1340 passed the screenshot test but did not produce the shared-storage copy; and #1341 passed all 13 instrumentation tests but its 180-second host poll expired before Gradle finished its 4m40s emulator task. Run #1342 passed the workflow and uploaded valid 320x640 PNGs from both emulator APIs. CI allows a 10-minute poll, pulls the app-specific external file while instrumentation runs, and validates the PNG before upload. It does not compare screenshots, establish reviewed goldens, or satisfy the full matrix.

Critical baseline screens:
- Prayer Home
- Prayer Monthly
- Quran Home
- Quran Reader
- Qibla
- More
- Hadith
- Settings

Required baseline variants:
- Arabic RTL / English LTR
- Light / Dark
- normal font / large font / 200% font
- compact phone / expanded device

Until UX28 lands, intentional UI changes must be reviewed against the current UI and the explicit acceptance requirements in `ui_ux_v2_execution_plan.md`. The configured CI capture covers only Prayer Home Arabic/light; this workspace has no connected local emulator/device and cannot capture or visually compare the remaining matrix.
