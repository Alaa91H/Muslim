# Adhan Reliability Review — v1.24.6 Follow-up

**Reviewed:** 2026-08-25.

## Scope and evidence boundary

The review traces the on-device path from persisted prayer settings through `AlarmManager`, the alarm receiver, foreground media playback, the notification channel, and the user-facing delivery probe. A successful CI build or emulator UI test is **not** proof that a particular physical phone emitted audible sound: device alarm volume, notification permission, channel state, OEM battery policy, and audio routing remain device-controlled.

## Confirmed Android platform constraints

Android documents that an exact alarm used to complete a user-requested action is exempt from normal foreground-service background-start restrictions. For Android 12+, the app must hold `SCHEDULE_EXACT_ALARM` (or an eligible `USE_EXACT_ALARM` declaration); the former can be revoked and Android explicitly instructs apps to reschedule when it is granted again. [1] [2]

Android 13+ requires `POST_NOTIFICATIONS` for non-exempt notifications. A foreground service may still be started without that permission, but its notice is not shown in the notification drawer when permission is denied. Android therefore requires the app to distinguish a visible-notification failure from a foreground media-playback failure. [3]

Android notification channels preserve user-selected visual and auditory behavior after creation. An application cannot programmatically raise a user-modified channel's importance or sound; it should read the actual channel and lead the user directly to that channel’s system settings. [4]

Android also warns that a process hosting only a manifest receiver can be killed after `onReceive()` returns. A receiver must not launch an untracked background thread and return; it must use a bounded asynchronous receiver hand-off or another system-managed mechanism. [5]

## Confirmed code gaps to correct

| Finding | Consequence | Intended correction |
|---|---|---|
| `BootReceiver` and `TimeChangeReceiver` launch a coroutine and return from `onReceive()` immediately. | The system may kill the process before DataStore is read and fresh alarms are scheduled. | Use `goAsync()` with a bounded I/O coroutine and always call `finish()`; add direct unit coverage for the hand-off policy. |
| No receiver handles `ACTION_MY_PACKAGE_REPLACED`. | An APK update can leave alarms absent until the user manually opens the app. | Reschedule immediately after in-place application update. |
| No receiver handles `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`. | Alarms can remain absent after the user grants Alarms & reminders outside the app flow. | Confirm the permission and rebuild the schedule on the platform broadcast. |
| The readiness card shows only pass/fail rows. | A user reporting “nothing happened” cannot see the stored failure stage, exception type, current channel importance, or a targeted system-settings action. | Expose a concise diagnostic detail and recovery actions for notification permission, the Adhan channel, exact alarms, and battery optimization. |
| Channel creation intentionally leaves existing channels untouched. | A legacy/silent/blocked system channel can persist across app updates while in-app defaults look correct. | Respect the user's system choice, detect it truthfully, and provide a direct channel-settings action rather than pretending the app can overwrite it. |
| The direct in-receiver audio fallback has no long-lived component after foreground-service failure. | It is a best-effort last resort only; the process can be killed after the broadcast completes. | Retain it as a short recovery attempt, but make the exact-alarm foreground-service path and user-visible failure reason authoritative. |

## Open device-specific evidence needed

A physical-device run of **Verify Adhan** is needed after the repair. The app should report the receiver stage, foreground-service start stage, audio-start confirmation, and any failure detail. If it fails after the application-side repair, the next evidence must include the device model, Android version, whether the app was battery-restricted, notification/channel state, alarm-stream volume/output, and the displayed diagnostic detail.

## References

[1] Android Developers, [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms).

[2] Android Developers, [Restrictions on starting a foreground service from the background](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).

[3] Android Developers, [Notification runtime permission](https://developer.android.com/develop/ui/compose/notifications/notification-permission).

[4] Android Developers, [Create and manage notification channels](https://developer.android.com/develop/ui/compose/notifications/channels).

[5] Android Developers, [Broadcasts overview](https://developer.android.com/develop/background-work/background-tasks/broadcasts).

## Comparative-project findings and product prioritisation

The F-Droid listing for Al-Azan 2.3.0 confirms that a mature privacy-first prayer application treats alarm reliability as a distinct product surface: optional per-prayer audio, pre/post reminders, explicit upcoming-alarm visibility, battery-optimization access, widgets, and opening an alarm screen during an active Adhan. [6] Its current release declares the same core Android capabilities relevant here: exact scheduling, media-playback foreground service, notifications, startup handling, wake lock, battery-optimization request, and vibration. This is comparative product evidence only; no source code, media, religious content, or licence-incompatible material is copied.

Five Prayers Android similarly lists offline/manual location, per-prayer Adhan, device-file audio, pre-prayer reminders, DND handling, widgets, monthly times, and a reliable user-facing ability to silence an active Adhan. [7] Muslim already implements most of these capabilities. The highest-impact missing follow-up is therefore not feature breadth but trustworthy **delivery observability**: show the next scheduled alarms and give the user a specific recovery action when Android blocks notification permission, a channel, exact alarms, or battery execution.

The most relevant implementation pattern found in a recently updated open-source Android Athan project is to re-arm the schedule immediately after every alarm and to reschedule on boot, package replacement, and timezone change. Its approach is reference material only and will not be copied. [8]

## Additional platform testing finding

Android's Doze/App Standby documentation supports device-level testing with ADB and platform battery-state simulation. The release verification plan must therefore include a connected-device check of `dumpsys alarm`, exact-alarm permission, app notification permission/channel state, Alarm-stream volume, and the app's persisted delivery diagnostic; an emulator-only UI test is insufficient. [9]

## Additional references

[6] F-Droid, [Al-Azan — Prayer Times](https://f-droid.org/en/packages/com.github.meypod.al_azan/).

[7] Five Prayers, [five-prayers-android README](https://github.com/Five-Prayers/five-prayers-android).

[8] nandxorandor, [Athan receiver and startup handling](https://github.com/nandxorandor/Athan).

[9] Android Developers, [Optimize for Doze and App Standby](https://developer.android.com/training/monitoring-device-state/doze-standby).
