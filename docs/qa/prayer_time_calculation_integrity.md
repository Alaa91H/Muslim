# Prayer-Time Calculation Integrity Audit

> **Status:** Implemented on the `main` development line; pending the release gate at the time of this document update.
> **Scope:** Software calculation and data-flow verification. This is not a religious certification, a timetable-provider endorsement, or evidence of physical-device verification.

## Purpose

This audit traced the application’s prayer-time data path from saved location and preferences through the calculator, home screen, next-prayer countdown, widget, Adhan scheduler, notification preview, Ramadan Iftar/Suhoor consumers, and travel high-latitude preview. It addressed two integrity risks: a country-keyword “automatic” calculation-method replacement that could change a user’s convention after selecting a location, and manual/GPS coordinates that could be stored using the device timezone rather than the location’s civil IANA timezone.

The audit also compared the core astronomical engine with the maintained open-source Adhan Kotlin reference implementation. The project calculator is algorithmically aligned with the reference for the chosen baseline: solar transit, sunrise/sunset elevation, shadow-length Asr, Fajr/Isha angle solving, high-latitude night portions, method/user offsets, and final minute rounding. [1] [2]

## Calculation contract

| Setting | Global automatic baseline | User-controlled alternatives |
|---|---|---|
| Calculation method | Muslim World League: Fajr 18°, Isha 17° | Every existing supported method and custom Fajr/Isha angles |
| Asr | Standard, shadow length 1 | Hanafi, shadow length 2 |
| High latitude | Seventh of the Night | Middle of the Night or angle-based bound |
| Manual correction | Zero minutes | Per-prayer signed minute offsets |
| Final civil time | Nearest minute after method/user offsets | No separate alert rounding path |

The settings conversion now creates one immutable `PrayerCalculationProfile`. All production consumers receive the profile rather than reconstructing a partial `PrayerParameters` value. The profile deliberately combines the method’s angles/minutes and intrinsic offsets with Asr school, high-latitude rule, user adjustments, and final rounding policy. The core calculator retains the post-offset astronomical instant for diagnostics, then creates one rounded civil-minute instant used by both UI rendering and alarm scheduling.

> The global baseline is predictable rather than prescriptive. A user who needs a different convention can make that choice deliberately; location selection never replaces it in the background.

## Root-cause findings and remediation

| Finding | Risk | Remediation |
|---|---|---|
| Automatic method selection matched a location to a bundled city/country and changed the active method. | Selecting GPS/manual/city data could silently make a jurisdictional choice for the user. | Automatic now means the fixed MWL global baseline. The country-keyword selector has been removed; all other methods remain explicit settings. |
| `highLatitudeRule` was nullable and legacy/missing values implicitly selected a latitude recommendation. | Two users could receive different defaults depending on location, and every consumer had to reconstruct fallback logic. | The persisted default is non-null Seventh of the Night. Legacy missing/invalid DataStore values resolve safely to that value; Middle and angle-based rules remain selectable. |
| Manual/GPS save used the device’s default timezone. | A selected place outside the device’s civil timezone could yield a valid-looking but incorrect local timetable and schedule. | Coordinates are resolved to IANA locally before persistence. Failure to resolve a valid IANA zone produces no new saved location rather than falling back silently. |
| UI, widget, scheduler, Ramadan and notification code assembled parameters independently. | A future setting or rounding change could make a visible prayer minute differ from the scheduled alarm minute. | Every consumer now uses `PrayerCalculationProfile`; the calculator exposes only one final rounded instant map for alerts and display. |
| `dhuhrMinutes` existed in parameters but was not applied inside the engine. | A supported profile field could be ineffective. | Solar-transit minutes are applied before method/user offsets and final rounding, with a focused unit test. |

## Reference comparison

A read-only clone of Adhan Kotlin at revision `e23d14385f6203954256b935ef91dc3c6565de34` successfully passed its JVM test task after installing a Java compiler. A temporary comparison test generated 70 MWL / Standard Asr / Seventh of the Night vectors for 14 cities on five 2026 dates. The generated values were transcribed into project regression cases for five Berlin seasonal dates and a geographically diverse 14-city matrix. [1] [2]

| Berlin date | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha |
|---|---:|---:|---:|---:|---:|---:|
| 15 Jan 2026 | 06:06 | 08:10 | 12:17 | 14:04 | 16:22 | 18:19 |
| 15 Apr 2026 | 04:43 | 06:09 | 13:07 | 16:56 | 20:05 | 21:31 |
| 15 Jun 2026 | 03:41 | 04:43 | 13:08 | 17:32 | 21:31 | 22:33 |
| 27 Aug 2026 | 04:42 | 06:09 | 13:09 | 16:56 | 20:06 | 21:32 |
| 15 Oct 2026 | 05:38 | 07:32 | 12:53 | 15:35 | 18:12 | 19:59 |

The regression suite additionally includes London, Stockholm, Oslo (summer and winter), Helsinki, Reykjavik, Toronto, Riyadh, Cairo, Istanbul, New York, Tokyo, Singapore, and Sydney. Existing Edinburgh tests retain explicit coverage of each high-latitude option; the calculator’s existing polar invalid-result guard remains intact. The vectors validate code equivalence to the reference for these inputs, not legal authority for a particular calculation convention.

## Local IANA timezone resolution

The resolver uses `timezonemap` 4.5 locally and does not transmit coordinates. The implementation loads a small region around the point on a background dispatcher, with a global fallback only near the International Date Line where a simple regional longitude range cannot cross the discontinuity. The selected zone is validated with `ZoneId.of` before persistence. The project records the source’s stated licensing: code is MIT and the bundled timezone-boundary data is ODbL. [3]

This is a deliberate offline trade-off. The dependency adds a timezone-boundary dataset and must be reviewed as a third-party data update when upgraded. It avoids a network lookup and avoids assuming a device timezone represents arbitrary coordinates, but its boundary data version is not a live legal or geographic authority. IANA remains the source of the zone identifier system; Android’s local zone-rule data converts the selected identifier into offsets and DST transitions. [4]

## Verification and operational limits

| Verification layer | Evidence added or retained | Limit |
|---|---|---|
| Static contract | `verify_prayer_calculation_integrity.py` checks defaults, shared profile, rounding, IANA resolver and prohibited country/device-timezone paths. | It cannot execute Android or prove a third-party boundary dataset is current. |
| Unit tests | Berlin seasonal vectors, global reference matrix, Dhuhr interval, raw/final minute alignment, existing high-latitude and polar cases, and local IANA coordinate cases. | They do not test every city, date, device timezone database, OEM or local mosque timetable. |
| CI quality and emulator | Debug builds, unit tests, Lint, Detekt and the established Android emulator suite are release gates. | CI/emulator success is not a physical-device, battery-policy, lock-screen, accessibility-service or religious-provider certification. |
| User control | Method, Asr rule, high-latitude rule, custom angles and minute offsets remain visible settings. | Users/community leaders remain responsible for choosing a convention appropriate to their practice. |

## References

[1]: [Batoul Apps — Adhan Kotlin](https://github.com/batoulapps/adhan-kotlin)

[2]: [Adhan Kotlin `PrayerTimes` calculation source](https://github.com/batoulapps/adhan-kotlin/blob/main/adhan/src/commonMain/kotlin/com/batoulapps/adhan2/PrayerTimes.kt)

[3]: [Dustin Johnson — timezonemap](https://github.com/dustin-johnson/timezonemap)

[4]: [IANA — Time Zone Database](https://www.iana.org/time-zones)
