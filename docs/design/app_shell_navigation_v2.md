# Muslim UI/UX 2.0 — App Shell & Navigation

Date: 2026-09-29  
Phase: UX05  
Status: implemented, pending CI gate

## Goal

Use one adaptive application shell while preserving the existing route graph, seasonal Ramadan behavior, deep links and tab-state restoration.

## Primary navigation

Primary destinations remain unchanged:

- Prayer
- Quran
- Qibla
- More
- Ramadan only when the existing adjusted-Hijri rule promotes it

No new top-level destination was added.

## Compact windows

Compact windows keep Material 3 bottom navigation.

The existing compact-label behavior is preserved for narrow tab slots so Ramadan's temporary fifth destination does not unnecessarily break labels.

## Medium and Expanded windows

When the current destination is one of the visible primary tabs:

- Medium/Expanded windows prefer Navigation Rail.
- The same routes, labels and icons are used.
- Selection state comes from the same Navigation destination hierarchy.
- Bottom navigation is suppressed while the rail is active.

Secondary/detail screens preserve the existing behavior of not showing primary navigation.

## Back stack and state

Tab navigation continues to use:

- `popUpTo(findStartDestination())`
- `saveState = true`
- `launchSingleTop = true`
- `restoreState = true`

This preserves the previous per-tab state behavior.

## Responsive source of truth

The root app shell consumes:

`IslamicLayout.adaptiveSpec(maxWidth)`

It does not introduce a competing app-level 600/840dp breakpoint.

## Voice navigation

Voice-navigation behavior remains unchanged and continues to use the shared scaffold FAB slot.

## Feature parity

UX00 routes and deep links remain untouched. The UI/UX V2 feature-parity verifier now additionally guards:

- bottom navigation helper
- navigation rail helper
- use of the shared adaptive policy
- save/restore state behavior

## Verification gate

UX05 is complete only when:
- build succeeds,
- unit tests succeed,
- Android lint succeeds,
- Detekt succeeds,
- API 26 and API 36 prayer/Qibla emulator tests succeed,
- Family Life emulator tests succeed,
- the UI/UX V2 feature-parity verifier succeeds.
