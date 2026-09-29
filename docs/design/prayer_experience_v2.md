# Muslim UI/UX 2.0 — Prayer Experience V2

Date: 2026-09-29  
Phase: UX06  
Status: implemented, pending CI gate

## Scope

UX06 applies the first user-visible UI/UX 2.0 redesign to Prayer Home and per-prayer Adhan customization while preserving prayer calculation, scheduling and persisted settings.

## Prayer Home hierarchy

The Prayer Home now follows this order:

1. Hijri date with previous/next period navigation
2. Gregorian date + location
3. one dominant Next Prayer hero
4. one section for either Daily Prayer Times or Monthly Timetable
5. optional local prayer tracker

The decorative band that consumed the upper part of the screen was removed from this surface. Top padding is intentionally compact.

## Duplicate date removal

The Gregorian date is shown in the compact header and is no longer repeated under “Today’s Prayer Times”.

Previous/next navigation is integrated with the header instead of requiring another duplicated date block below the timetable.

## Prayer row visual language

The daily prayer rows use one coherent time-of-day icon family:

- Fajr — twilight
- Sunrise — sun
- Dhuhr — daylight
- Asr — later-day sun
- Maghrib — dusk
- Isha — night

The next prayer remains emphasized through a semantic container and typography, not a separate unrelated layout.

## Alert/customization state

Raw per-prayer volume percentages are no longer shown in the daily list.

The row now displays a compact state control:

- active audible Adhan
- vibration-only
- silent/disabled

The state is communicated by icon + accessibility state description, not color alone.

All existing sound, volume, vibration and adjustment settings remain preserved.

## Secondary actions

Share and Daily/Monthly mode switching moved into the shared overflow menu.

This reduces permanently exposed controls while keeping both actions reachable.

## Adhan customization

The per-prayer customization experience now uses the shared `MuslimBottomSheet` instead of the old fixed-width AlertDialog.

The same existing settings remain available:
- information density
- alert type
- time adjustment
- bundled sound
- preview
- global/per-prayer volume
- vibration

No persisted setting was removed.

## Monthly prayer timetable

The old seven-column calendar grid that showed only Fajr/Maghrib has been replaced by an imsakiyah-style timetable.

Each day is one row. The leading day column is naturally placed on the right in Arabic RTL layouts.

Columns show:
- Fajr
- Sunrise
- Dhuhr
- Asr
- Maghrib
- Isha

The table:
- lists days sequentially from day 1 to the end of the month,
- horizontally scrolls when the device is too narrow,
- highlights the selected day,
- gives Friday a quiet alternate surface,
- keeps the prayer-name header at the top of the table content.

## Period navigation

The Home ViewModel now exposes period-aware navigation:
- daily mode: previous/next day
- monthly mode: previous/next month

The underlying prayer calculator and profile are unchanged.

## Data contract

`DayTimes` now owns a complete `Map<Prayer, LocalTime>` for monthly display.

Compatibility getters for Fajr and Maghrib remain available.

A unit test verifies that all Prayer enum entries survive in the monthly timetable data model.

## Verification

UX06 is complete only after:
- feature-parity verifier passes,
- Home UI/UX contract verifier passes,
- responsive Adhan customization verifier passes,
- design-system verifier passes,
- Prayer Home unit/instrumented tests pass,
- full debug build, lint and Detekt pass,
- API 26 and API 36 emulator gates pass.
