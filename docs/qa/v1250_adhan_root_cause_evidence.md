# v1.24.10 Adhan delivery root-cause evidence

## Evidence captured on 2026-08-26

The published v1.24.10 source manifest (`app/src/main/AndroidManifest.xml`) declares `INTERNET`, `WAKE_LOCK`, `RECORD_AUDIO`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, and `REQUEST_INSTALL_PACKAGES`, but it does **not** declare the Android permissions required by the Adhan delivery path:

- `android.permission.POST_NOTIFICATIONS` — required for Android 13+ notification permission requests and visible notifications.
- `android.permission.SCHEDULE_EXACT_ALARM` — required for the exact-alarm capability used by `AdhanScheduler.scheduleDeliveryProbe()` and regular prayer alarms on Android 12+.
- `android.permission.FOREGROUND_SERVICE` — required for the `AdhanPlaybackService` foreground playback path.
- `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK` — required for a media-playback foreground service on Android 14+.

`AdhanScheduler` requires `AlarmManager.canScheduleExactAlarms()` before it schedules the ten-second Verify probe. Its production scheduler falls back to inexact alarms when the capability is unavailable. The app manifest as published cannot obtain the exact-alarm capability because it does not request the corresponding special permission.

The manifest also has no declaration for `AdhanPlaybackService` or `AdhanAlarmReceiver` despite the scheduler constructing an explicit broadcast `Intent` for `AdhanAlarmReceiver` and starting `AdhanPlaybackService`. This must be reconciled with merged-manifest artifacts and feature manifests before release.

This is a concrete root-cause candidate for the reported absence of both visible Adhan notification and audio. It supersedes speculative channel-only explanations and must be repaired with manifest declarations and verified in the packaged release APK.

## Correction after packaged-APK inspection

The application-level manifest alone is not the packaged manifest. The v1.24.10 APK was inspected with Android build tools and **does include** the required merged declarations:

- `SCHEDULE_EXACT_ALARM`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `AdhanAlarmReceiver` under `org.muslim.app.feature.prayertimes.notifications`
- `AdhanPlaybackService` under `org.muslim.app.feature.prayertimes.notifications` with the `mediaPlayback` foreground-service type

Therefore, the absence of those declarations from `app/src/main/AndroidManifest.xml` is not itself the production root cause; they are correctly contributed by `feature/feature-prayer-times/src/main/AndroidManifest.xml` and were confirmed inside the signed release APK.

The default bundled Makkah Adhan resource was also extracted from the published APK and verified as a valid mono MP3 stream with a duration of approximately 201.3 seconds. The bundled default audio asset is present and decodable.

## Remaining evidence-based risk areas

1. `SCHEDULE_EXACT_ALARM` is denied by default for new Android 13+ installs until the user grants special access. The current production scheduler degrades regular prayer alarms to inexact delivery when access is unavailable, while the Verify probe declines to run. This must be made unmistakable in the UI and device evidence.
2. `NotificationManager.notify()` does not provide a reliable visible-delivery acknowledgement when the app notification permission or the channel is blocked. The current journal records a successful call as `Posted`, which can be a false positive. The implementation needs a system-state precheck and active-notification verification.
3. The user-selected effective app volume can be 0–24%, or the sound option can be Silent/VibrateOnly. The app must preserve it exactly, but Verify needs an explicit, non-mutating statement that the selected configuration cannot be physically audible, instead of only a binary failed row.
4. The permanent next-prayer countdown is intentionally a silent low-importance notification. It must remain diagnostically and visually separate from the active Adhan alert.

## Corrective design selected for the next patch

The audit did not find a missing packaged permission, a missing packaged receiver/service, or an invalid default bundled audio file. It found a **compound reliability defect** in the observable delivery path:

- The app currently treats a non-throwing `NotificationManager.notify()` call as proof that an active Adhan alert was posted. Android can suppress that alert when the app notification permission or channel is blocked without throwing; the journal can therefore mark an invisible alert as posted.
- When foreground-service audio has not confirmed within four seconds, the receiver stops `AdhanPlaybackService` and immediately starts the direct fallback through the same singleton `AdhanSoundPlayer`. Service teardown calls `soundPlayer.stop()`, creating a race that can terminate the just-started fallback. This is a plausible direct explanation for missing audio on a slow or partially failing service-start path.
- Exact alarm access is a user-granted special access and is denied by default for new Android 13+ installs. It must be confirmed and made visible as a hard scheduling prerequisite; a regular inexact fallback cannot prove the expected prayer-time behavior.

The repair will therefore:

1. Add a system notification preflight (runtime permission, app notifications enabled, Adhan channel exists and is not disabled) and verify the active Adhan notification after posting before recording it as `Posted`.
2. Preserve the user's selected sound and exact volume while removing the fallback race: do not tear down the foreground service before the shared player has begun the fallback; stop the service only after fallback completion. Use a bounded wake lock for the rare direct fallback when foreground-service startup is rejected.
3. Persist and display a granular scheduled-delivery status that distinguishes exact-alarm access, receiver reach, visible alert result, foreground service start, and actual audio start. No diagnostic action will rewrite preferences.
4. Add regression coverage for blocked app/channel notification conditions and for the no-race fallback order, then verify the signed APK and package identity again.

## Authoritative Android platform references

- Android Developers, **Schedule alarms**: https://developer.android.com/develop/background-work/services/alarms
  - Exact alarms are not affected by foreground-service launch restrictions when the appropriate exact-alarm permission is declared and granted.
  - `SCHEDULE_EXACT_ALARM` must be checked with `canScheduleExactAlarms()`; the platform cancels future exact alarms when the access is revoked and broadcasts `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` when it is granted.

- Android Developers, **Schedule exact alarms are denied by default**: https://developer.android.com/about/versions/14/changes/schedule-exact-alarms
  - For new Android 13+ targeted installs, `SCHEDULE_EXACT_ALARM` is denied by default unless a pre-grant/exemption applies.
  - The documented flow is: check `canScheduleExactAlarms()`, invoke `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` when needed, check again on resume, and reschedule after the permission-state broadcast.

- Android Developers, **Foreground service types**: https://developer.android.com/develop/background-work/services/fgs/service-types
  - Android 14+ requires an appropriate foreground-service type and type-specific permission. The published APK declares `mediaPlayback`, `FOREGROUND_SERVICE`, and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`; media playback has no additional runtime prerequisite.
  - Android 15+ prohibits launching a media-playback foreground service from `BOOT_COMPLETED`; the app's normal prayer alarm receiver is not a boot receiver.
