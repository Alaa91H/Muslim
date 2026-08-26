# System Notification Identity Repair

> **Status:** Unreleased; this note describes the change prepared on the `main` branch and does not claim that a tagged APK has been published or tested on every device.

## Purpose and scope

This repair addresses stale visual identities that can survive an in-place Android application update. It covers the full-colour launcher identity, the monochrome status-bar glyph, the active Adhan card, the persistent next-prayer countdown, and the Quran recitation media card. It does **not** alter prayer calculations, Adhan audio settings, Quran text, recitation audio, notification permission state, or Android notification-channel settings.

| Surface | Current implementation | Explicit boundary |
|---|---|---|
| Launcher and round launcher | The manifest points to new `v2027` adaptive-icon resource names using the approved geometric Islamic mark. | Android launchers choose masks, caching behaviour, themed-icon tinting, and badge presentation. |
| Status bar | Notification producers use the new `ic_muslim_status_bar_v2027` monochrome vector. | Android controls the final light/dark or system tint used for a small notification icon. |
| Active Adhan | The active alert uses a new notification ID and removes all retained legacy Adhan cards before a current card is posted. | Whether an alert is visible, audible, or prominent remains subject to notification permission, channel settings, and the operating system. |
| Next-prayer countdown | The persistent countdown uses a new notification ID and removes retained legacy countdown cards. | The card remains a system-rendered, silent status surface rather than a custom full-colour brand panel. |
| Quran recitation | The foreground media service uses a new notification ID, removes the retired card at application and service start, and retains `MediaStyle` transport controls. | Android and media-system templates control the precise layout of the transport card. |

## Resource and notification migration

Android may retain a running notification or cache an adaptive-icon resource across an application update. The fix therefore changes resource identities as well as cancelling the old cards. It intentionally does not attach a `setLargeIcon` image to Adhan, countdown, or Quran-recitation cards. Android may still display an application icon in its own template; after this change, the application identity resolves through the new launcher resources.

| Component | Retired identity or identities | Current identity |
|---|---:|---:|
| Launcher / round launcher | `v1252`, `v2026` resources | `ic_muslim_launcher_v2027` and `ic_muslim_launcher_round_v2027` |
| Status-bar glyph | `ic_muslim_status_bar_v1252`, `ic_muslim_status_bar_v2026` | `ic_muslim_status_bar_v2027` |
| Active Adhan card | `1001`, `1005`, `1010` | `1012` |
| Next-prayer countdown | `1003`, `1004`, `1011` | `1013` |
| Quran recitation media card | `7006` | `7007` |

The application startup path clears all retired cards. The Adhan and countdown service paths also clear their respective retired IDs before posting current work. The Quran foreground service clears its retired media-card ID during `onCreate`, so the migration remains effective when Android recreates the service independently of a normal application launch.

## Verification approach

The implementation is verified through focused source checks, Kotlin tests, and Android application instrumentation coverage. The device tests post a synthetic retained legacy card, invoke the matching migration function, and require the system active-notification list to stop containing that card. They also assert the `v2027` status-bar resource for Adhan and countdown notification construction, the manifest launcher identity, and the Quran card-ID separation.

| Check | Evidence expected before a public release |
|---|---|
| Resource audit | No production source or packaged resource reference remains to the retired `v1252` or `v2026` icon identities. |
| Static quality | `git diff --check`, relevant Gradle unit tests, Android Lint, and Detekt pass. |
| Application instrumentation | Adhan, countdown, and Quran notification-migration tests compile and pass in the real application APK environment. |
| Continuous integration | The `main` workflow is green before a release tag is created. |
| Physical-device review | After installing over a compatible older build, open Muslim once, then confirm a new Adhan/countdown card and a Quran playback card on the target Android version and launcher. |

> Android notification small icons are intentionally monochrome; the platform applies their final system tint. See [Android Developers — Create a notification](https://developer.android.com/develop/ui/views/notifications/build-notification) for the platform notification model and [Android Developers — Adaptive icons](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive) for launcher-icon masking behaviour.

## Upgrade and support notes

Install the new APK over a compatible existing installation, then open the app once. This allows the startup migration to cancel retained cards from earlier versions. A launcher may refresh its cached icon immediately or after its normal process/cache refresh; this is launcher behaviour rather than an app data migration. No uninstall, data reset, or change to saved worship settings is required by this repair.

For a reproducible defect, include the Android version, device/launcher, notification type, whether the app was opened after updating, and a screenshot with personal information removed. Project ownership and support links are limited to the confirmed public links below.

| Purpose | Link |
|---|---|
| Project and maintainer profile | [Alaa on GitHub](https://github.com/Alaa91H) |
| Voluntary support | [Ko-fi: alaa91h](https://ko-fi.com/alaa91h) |
