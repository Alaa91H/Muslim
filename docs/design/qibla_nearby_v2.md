# Muslim UI/UX 2.0 — UX10 Qibla & Nearby Mosques

Date: 2026-09-29  
Phase: UX10  
Status: implementation complete, pending CI gate

## Goal

Make Qibla direction the primary task, reduce permanent technical detail, and simplify Nearby Mosques controls without changing location, compass, search, sorting, radius, navigation, sharing or offline/cache behavior.

## Qibla hierarchy

The compass remains the dominant visual element.

The location block is intentionally quieter and now uses a shared low-emphasis group instead of a large primary-colored card.

Immediately below the compass, the user sees only the information required to orient the device:

- Qibla bearing
- turn left/right instruction or aligned state
- flat-device/calibration guidance when needed

Technical/reference data is progressive disclosure under **Details**:

- true phone heading
- cardinal heading
- distance to Makkah

The existing alignment haptic remains unchanged.

## Responsive behavior

Qibla now consumes:

`IslamicLayout.adaptiveSpec(maxWidth)`

Local 360dp/600dp app-layout breakpoints were removed. Exact compass geometry may still use the real local bounds because the dial is a specialized rendering surface.

## Nearby Mosques

The feature remains intentionally lightweight and does not embed a map SDK.

The top area is reduced to:

- current-location context
- one refresh icon
- one search-radius row

Choosing the radius opens a shared `MuslimBottomSheet` instead of a local dropdown.

Results use:

- `MuslimSearchBar`
- `MuslimSegmentedControl` for nearest/name sorting
- `MuslimOverflowMenu` for share/copy
- direct Map and Directions actions for each mosque

## Feature parity preserved

UX10 preserves:

- Qibla bearing calculation
- true-heading/declination correction
- level detection
- calibration guidance
- live GPS refresh
- one-time alignment haptic
- Qibla/Nearby Mosques tabs
- shared location permission flow
- selectable mosque radius
- cached/offline mosque states
- name/address search
- distance/name sorting
- nearest-mosque marker
- external map opening
- turn-by-turn navigation
- share
- copy address/coordinates
- cancellation of mosque work when leaving the tab

## No embedded map regression

Nearby Mosques continues to use external map/navigation intents. UX10 does not add Google Maps, Mapbox, MapLibre or another embedded map SDK.

## Verification

`verify_qibla_v2.py` guards:

- compass-first hierarchy
- progressive technical details
- shared adaptive width policy
- removal of local 360/600dp breakpoints
- shared V2 mosque controls
- removal of local dropdown/search controls
- radius/search/sort/map/directions/share/copy feature parity

The existing `verify_nearby_mosques_contract.py` continues to guard location, cache, navigation and dependency boundaries.

## Exit gate

UX10 closes only when:

- [ ] debug builds succeed
- [ ] unit tests succeed
- [ ] Android lint succeeds
- [ ] Detekt succeeds
- [ ] Qibla/Prayer emulator tests pass on API 26 and 36
- [ ] Family Life emulator tests pass on API 26 and 36
- [ ] Nearby Mosques contract succeeds
- [ ] UX10 Qibla V2 contract succeeds
