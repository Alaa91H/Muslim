# Lazy Hadith Library, Location Recovery and Notification Audit

**Status:** Published as [`v1.25.10`](https://github.com/Alaa91H/Muslim/releases/tag/v1.25.10) after the tag CI completed successfully: quality, Android emulator, and signed release-artifact jobs passed. The phone and Wear APK SHA-256 values downloaded from the release matched GitHub's published asset digests. This workspace still has no configured Android SDK, so its local evidence remains limited to static guards and Detekt.

## Scope

This audit covers the new on-demand Hadith catalogue, foreground GPS recovery for prayer times, the existing Muslim World League Isha contract, seasonal Ramadan navigation, semantic next-prayer notification colours, direct per-prayer customisation, and the live Adhan lock-screen notification.

| Surface | Implemented contract | Verification boundary |
|---|---|---|
| Hadith library | The catalogue is metadata-only. A user opening a collection imports that single compressed book in bounded 150-row Room transactions; chapters and search stay scoped to the active book and Compose consumes Paging. | Static asset, repository, DAO and UI guards pass locally. Android memory profiling and device scrolling remain CI/device work. |
| Location | The picker accepts approximate or precise foreground permission. Precise access requests a high-accuracy current fix; a recent fused/platform fix is a local fallback. Coordinates must resolve to an IANA zone before persistence. | Code and static contract pass locally. Availability also depends on a user-enabled Android location provider and granted permission. |
| Isha | The default profile is MWL: Fajr 18°, Isha 17°, Standard Asr, zero manual Isha offset and Seventh-of-the-Night high-latitude bound. | Seasonal Berlin and 14-city vectors are compared with the Adhan Kotlin reference in the unit suite. Religious/community convention choices remain user controlled. |
| Ramadan navigation | Ramadan occupies the bottom bar only when the app-wide adjusted Umm al-Qura date has Hijri month 9. It is inserted between Quran and Qibla; outside the month it remains an item in More. | Pure unit coverage checks Ramadan boundaries and the user adjustment. No background job is used or needed. |
| Countdown notification | The upcoming prayer time is green; remaining and elapsed durations are red. The duration-colour picker was removed because live delivery is deliberately semantic and non-configurable. | Static guard and Android instrumentation coverage are configured. System templates ultimately render spans. |
| Per-prayer alert icon | Tapping a scheduled-prayer alert icon opens the persisted customisation dialog in the home screen and does not navigate to prayer settings. Its width and visible content height respond to screen dimensions, it scrolls safely when constrained, and the user can persist comfortable or compact information density. | Static guards protect the modal-only and responsive paths; emulator UI coverage remains CI-gated. |
| Live Adhan | While playback owns the foreground service, its public high-priority, ongoing, non-auto-cancelable notification remains in the drawer and lock screen. The sole action is Stop Adhan and it does not require authentication when invoked from the lock screen. Task removal re-posts the same card. | Android instrumentation verifies the card configuration and active-card posting. Lock-screen visibility and heads-up availability remain user/channel/OEM controlled. |

## Hadith data and deferred loading

The previous release carried one 56,918,179-byte uncompressed `hadith_full.ndjson` entry inside the APK. It was deflated by APK packaging, but the app-facing data model still represented one corpus and made all content too easy to import as a single unit. The current implementation replaces it with nine book assets totaling **37,919 streamed records** and a generated manifest. The catalogue reads only fixed `HadithCollection` metadata, including the cover resource and counts. It does not call the importer.

| Collection | Data source in this change | Import behavior |
|---|---|---|
| Sahih al-Bukhari, Sahih Muslim, Abu Dawud, Tirmidhi, an-Nasai, Ibn Majah, Muwatta Malik | Versioned Arabic/English editions from `fawazahmed0/hadith-api`, whose repository declares The Unlicense. [1] [2] | Only the selected book is opened through `GZIPInputStream`, parsed line by line, and committed in batches of 150. |
| Riyad as-Salihin, Forty Hadith of al-Nawawi | Prior owner-attested production corpus, preserved without text transformation. | Same selected-book import path. |

The per-asset source, licence, attribution and review status are recorded in `docs/content/content_approvals.json` and machine-verified through `docs/content/content_manifest.json`. This tracks distribution provenance; it is not a claim of an independent scholarly certification.

## GPS recovery and Isha diagnosis

The GPS feature was not removed, but its practical permission path was too narrow: the UI asked only for `ACCESS_FINE_LOCATION`, and the provider rejected an Android approximate-location grant even though approximate foreground coordinates remain valid for a locally calculated timetable. The repaired flow requests Android's fine/coarse permission pair, accepts either grant, uses high-accuracy current location for a precise grant, and keeps two local fallbacks when a fresh fused fix is temporarily unavailable. It never sends coordinates to a network service. A coordinate is still saved only after local IANA timezone resolution, preventing an unrelated device timezone from changing the civil timetable.

No new numerical Isha workaround was applied because the canonical calculator already operates on the intended default: the MWL Isha angle is **17°**, the default manual Isha adjustment is **0 minutes**, and all final times share one rounded minute for display and alarms. New explicit assertions retain those settings and the existing Adhan-reference vectors. The user can deliberately choose another supported method or a manual adjustment; that choice is disclosed in settings rather than hidden in location handling.

## v1.25.9 APK size audit

The published phone APK grew from **32,911,816 bytes** in v1.25.8 to **61,616,011 bytes** in v1.25.9, a measured increase of **28,704,195 bytes**. This was not caused by the notification changes or the direct customisation UI. The dominant new payload was the local worldwide coordinate-to-IANA timezone boundary dataset required to avoid assigning arbitrary coordinates the device timezone.

| Published v1.25.9 APK component | Compressed APK bytes | Effect |
|---|---:|---|
| `timezonemap-4.5-2020d.tar.zstd` | 25,213,768 | Local global timezone-boundary data. |
| `libzstd-jni` native binaries (four ABIs) | 3,341,708 | Decompression support for that local dataset. |
| Prior `hadith_full.ndjson` entry | 11,735,173 | Existing compressed APK payload; it already existed in v1.25.8. |

The new per-book GZIP files total 12,201,020 bytes before APK packaging. They are introduced for bounded runtime loading and future book-level replacement, **not** claimed as a package-size reduction over the old highly deflated single file. The project therefore retains the global local IANA lookup for correctness and privacy, while recording the material size trade-off plainly. The `timezonemap` project documents its local timezone lookup and separately identifies its code and timezone-boundary data licences. [3]

## Lock-screen controls and hardware keys

The Adhan notification explicitly requests `VISIBILITY_PUBLIC`, exposes its Stop Adhan broadcast action without `setAuthenticationRequired(true)`, and belongs to a high-importance foreground-service channel. Android documents that public notifications can appear on the lock screen and that notification actions can be invoked directly there; users retain control over per-channel lock-screen visibility. [4]

The requested **Stop Adhan** button is therefore the supported, testable quick control on a locked device. Hardware volume keys are intentionally not repurposed to stop Adhan. Android owns those keys for volume adjustment, and a normal foreground-service notification cannot reliably intercept them while the device is locked across OEMs or system versions. A media-session migration solely to repurpose volume keys would change the product from a focused alarm notification into a system media-control session and still would not guarantee that behavior; it is not represented as a supported promise. The responsive customisation dialog instead offers a persisted **Comfortable** or **Compact** information-density choice while retaining every control and enforcing bounded scrollable content at short heights.

## Automated checks

The quality gate now runs the following checks before build, unit, lint, Detekt, emulator and signed-artifact jobs:

```text
scripts/generate_content_manifest.py
scripts/verify_content_manifest.py
scripts/verify_hadith_paging_and_navigation.py
scripts/verify_prayer_location_notification_contract.py
scripts/verify_adhan_notification_lifecycle.py
scripts/verify_responsive_customization_layout.py
```

The local static pass does not substitute for Android compilation, emulator tests, real lock-screen validation, OEM notification presentation, user Android permissions/channel settings, location-provider availability, or qualified religious review.

## References

[1] [fawazahmed0/hadith-api repository](https://github.com/fawazahmed0/hadith-api)

[2] [The Unlicense](https://unlicense.org/)

[3] [timezonemap project](https://github.com/dustin/java-timezone-map)

[4] [Android Developers: About notifications](https://developer.android.com/develop/ui/compose/notifications)

[5] [Android Developers: Background playback with a MediaSessionService](https://developer.android.com/media/media3/session/background-playback)
