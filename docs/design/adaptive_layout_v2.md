# Muslim UI/UX 2.0 — Adaptive Layout Foundation

Date: 2026-09-29  
Phase: UX04  
Status: implemented, pending CI gate

## Goal

Provide one shared responsive policy for ordinary application screens so feature modules stop inventing independent phone/tablet thresholds.

## Width classes

The shared `IslamicLayout` policy defines:

- Compact: 0–599dp
- Medium: 600–839dp
- Expanded: 840dp+

## Adaptive specification

`IslamicLayout.adaptiveSpec(width)` returns:

- width class
- maximum content width
- horizontal page padding
- whether navigation rail is preferred
- whether a two-pane layout is supported

### Compact
- readable max width: 760dp
- page horizontal padding: `IslamicSpacing.PageHorizontal`
- bottom navigation preferred
- no two-pane layout

### Medium
- readable max width: 760dp
- horizontal padding: `IslamicSpacing.Large`
- navigation rail preferred
- single-pane content by default

### Expanded
- wide max width: 1200dp
- horizontal padding: `IslamicSpacing.Section`
- navigation rail preferred
- two-pane layouts allowed

## Shared UI primitives

### MuslimAdaptiveContentFrame

Translates actual available width into the shared adaptive spec, centers content, applies the semantic max width, and applies the correct horizontal page gutter.

### MuslimAdaptiveScreen

Combines:
- application scaffold
- system inset handling
- adaptive content frame

Feature modules receive the resolved `MuslimAdaptiveLayoutSpec` rather than re-deriving width classes locally.

## Insets

`MuslimAppScaffold` now exposes `contentWindowInsets` with Material 3 scaffold defaults. Existing callers retain existing behavior while future specialized shells can explicitly override inset policy without bypassing the shared scaffold.

## Migration rule

Ordinary screen layout must use the shared adaptive specification.

Local `BoxWithConstraints` remains valid only for specialized internal geometry, including:
- Qibla compass diameter
- Quran page/glyph/highlight geometry
- map/chart geometry
- other rendering that depends on exact local bounds rather than app navigation/layout class

## Deferred feature migrations

Prayer Home and Qibla currently contain local layout thresholds. They are intentionally not rewritten in UX04 because their user-visible hierarchy changes belong to UX06 and UX10 respectively. Their future migrations must consume this shared policy.

## Verification

- Design-system unit tests cover 599/600/839/840dp boundaries.
- Tests verify navigation-rail and two-pane policy.
- CI design-system verifier requires adaptive primitives to consume `IslamicLayout.adaptiveSpec(maxWidth)`.
- Full build, lint, Detekt and emulator gates must pass before UX05 begins.
