# Android System-Icon Identity Repair

> **Status:** Unreleased follow-up prepared after `v1.25.0`. This note describes the source-level repair and does not claim a tagged APK or universal device verification before the next release is approved.

## Purpose and scope

This follow-up addresses stale visual identities that can survive an in-place Android update. It covers the phone full-colour launcher identity, Wear OS launcher identity, monochrome status-bar glyph, active Adhan alert, persistent next-prayer countdown, and Quran recitation media card. It does **not** change prayer calculations, Adhan audio settings, Quran text, recitation audio, notification permission state, or user-owned notification-channel settings.

| Surface | Current implementation | Explicit boundary |
|---|---|---|
| Launcher and round launcher | The phone manifest points to new `v2028` adaptive-icon resources using the approved geometric Islamic mark. | Android launchers control masks, themed-icon tinting, cache timing, and badge presentation. |
| Wear OS launcher | The Wear manifest points to a dedicated `v2028` geometric-star-and-mihrab resource; the previous standalone crescent-and-star resource is removed. | Wear OS controls the final circular presentation and cache refresh timing. |
| Status bar | Every production notification producer uses `ic_muslim_status_bar_v2028`. | Android applies the final light/dark system tint to a small notification icon. |
| Active Adhan | A fresh notification ID is used and every earlier retained Adhan identity is cancelled. | Visibility and alert behaviour remain subject to permission, channel settings, and the operating system. |
| Next-prayer countdown | A fresh ongoing-card ID is used and every earlier countdown identity is cancelled. | This remains a silent, system-rendered status surface rather than a custom full-colour panel. |
| Quran recitation | The media foreground service uses a fresh ID, clears both prior cards, and retains standard `MediaStyle` controls. | Android and media-system templates control the transport-card layout. |

## Resource and notification migration

Android can retain a running notification or cache an adaptive icon across an application update. The repair therefore changes resource identities **and** cancels the old cards. Adhan, countdown, and recitation cards intentionally do not attach a `setLargeIcon`; the only app-supplied visual is the current monochrome status glyph, while Android may show the current application identity in its own template.

| Component | Retired identity or identities | Current identity |
|---|---:|---:|
| Launcher / round launcher | `v1252`, `v2026`, `v2027` | `ic_muslim_launcher_v2028` and `ic_muslim_launcher_round_v2028` |
| Wear OS launcher | `ic_wear_launcher` | `ic_wear_launcher_v2028` |
| Status-bar glyph | `ic_muslim_status_bar_v1252`, `ic_muslim_status_bar_v2026`, `ic_muslim_status_bar_v2027` | `ic_muslim_status_bar_v2028` |
| Active Adhan card | `1001`, `1005`, `1010`, `1012` | `1014` |
| Next-prayer countdown | `1003`, `1004`, `1011`, `1013` | `1015` |
| Quran recitation media card | `7006`, `7007` | `7008` |

`IconIdentityMigrationReceiver` receives `ACTION_MY_PACKAGE_REPLACED` and cancels the retired Adhan, countdown, and recitation cards immediately after an in-place upgrade, before the user opens the app or a foreground service restarts. The application startup path and the relevant foreground services retain the same cleanup as defence in depth.

## Verification approach

The implementation is verified through focused source checks, Kotlin tests, Android instrumentation coverage, and continuous integration. Device tests post synthetic retained cards for every retired ID, invoke the matching production migration function, and require the Android active-notification list to stop containing them. The package-replaced receiver calls those same tested production cleanup functions immediately after an in-place upgrade. The tests also assert the `v2028` launcher and status-bar identities.

| Check | Evidence expected before a public repair release |
|---|---|
| Resource audit | No production source or packaged resource reference remains to retired `v1252`, `v2026`, or `v2027` identities. |
| Static quality | `git diff --check`, relevant Gradle unit tests, Android Lint, and Detekt pass. |
| Application instrumentation | Adhan, countdown, and Quran notification-migration tests compile and pass in the real application APK environment. |
| Continuous integration | The `main` workflow is green before a repair tag is created. |
| Physical-device review | Install over a compatible older build and confirm that retained cards disappear immediately after the package update; then verify a newly posted Adhan/countdown card and Quran playback card. |

> Android notification small icons are intentionally monochrome; the platform applies their final system tint. See [Android Developers — Create a notification](https://developer.android.com/develop/ui/views/notifications/build-notification) and [Android Developers — Adaptive icons](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive).

## Upgrade and support notes

Install the next repair APK over a compatible existing installation. The package-replaced receiver cancels retained cards without requiring an app launch; opening Muslim afterward remains safe and refreshes scheduled work normally. A launcher may still refresh its cached icon on its own timing, because launcher caches are controlled by the launcher rather than app data. No uninstall, data reset, or change to saved worship settings is required.

For a reproducible defect, include the Android version, device and launcher, notification type, whether the update was installed in place, and a screenshot with personal information removed. Project ownership and support links are limited to the confirmed public links below.

| Purpose | Link |
|---|---|
| Project and maintainer profile | [Alaa on GitHub](https://github.com/Alaa91H) |
| Voluntary support | [Ko-fi: alaa91h](https://ko-fi.com/alaa91h) |
