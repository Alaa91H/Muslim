# v1.24.16 Adhan delivery findings

## Sources consulted

1. Android Developers, [Background audio hardening](https://developer.android.com/about/versions/17/changes/bg-audio), updated 2026-08-14.
2. Android Developers, [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms).
3. Android Developers, [Manage audio focus](https://developer.android.com/media/optimize/audio-focus).

## Applicable facts

* Android's exact `RTC_WAKEUP` alarms are time-sensitive interruptions, and Android documents them as exempt from the normal foreground-service launch restrictions. `SCHEDULE_EXACT_ALARM` is user-controlled on Android 12+ and must be checked before scheduling.
* Android 17 background-audio hardening can silently suppress background audio interaction. The documented exception for an app using an exact alarm applies to changes to streams with `USAGE_ALARM`; an active `mediaPlayback` foreground service is still the correct lifecycle owner for background playback.
* Android documents that audio focus must be requested immediately before playback, using attributes equivalent to the player attributes. On Android 15+, requesting focus requires the app to be foreground or already running a foreground service.
* Under hardening, `AudioTrack.write()` can fail persistently without an exception. A fallback must check buffer initialization and every write result rather than treating `PLAYSTATE_PLAYING` by itself as evidence that audio frames reached the output path.

## Code changes motivated by those facts

* The active Adhan foreground service already owns `mediaPlayback`, starts foreground before requested audio, and is invoked by the exact receiver.
* The fallback path was changed from one large `MODE_STATIC` `AudioTrack` buffer to a checked `MODE_STREAM` writer. It verifies initialization and every write, uses `USAGE_ALARM` plus `CONTENT_TYPE_SONIFICATION`, and reports start only after the first successful frame write and actual play state.
* Audio focus attributes are now the same alarm/sonification attributes used by both MediaPlayer and AudioTrack, with transient exclusive focus for the finite Adhan interruption.

## Remaining physical-device limitation

The sandbox cannot attach or hear the user's phone. The release gate must therefore prove receiver delivery, Android active notification retention, service start, and successful audio-frame start on an Android emulator. The in-app Verify Adhan journal must remain the source of the exact terminal stage on a handset.

## UI and accessibility references

1. Android Developers, [Accessibility in Jetpack Compose](https://developer.android.com/develop/ui/compose/accessibility), updated 2026-06-13.
2. Android Developers, [Principles for improving app accessibility](https://developer.android.com/guide/topics/ui/accessibility/principles), updated 2026-07-21.
3. Material Design 3, [Cards guidelines](https://m3.material.io/components/cards/guidelines).

### Applied UI decisions

* The Adhan readiness status now uses a bounded Surface with a status icon and text, not color alone. This makes readiness and failure scannable and follows Android guidance to communicate categories through non-color cues.
* The card groups the single topic of delivery readiness, its checks, recovery actions, and the Verify Adhan action in one hierarchy. It does not nest a separately scrolling region inside the settings page.
* The state icon is decorative because the adjacent localized status text supplies the meaning; interactive recovery controls keep explicit labels.

### Future UI review scope

The complete visual review remains bounded to public screens, navigation hierarchy, dynamic text scaling, meaningful content descriptions, touch targets, color contrast, and adaptive layouts. It is not represented as a claim that every possible device/locale state is defect-free without device testing.
