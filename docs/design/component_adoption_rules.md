# Shared component and spacing adoption

Date: 2026-09-24

## Purpose

The application already owns a design-system layer. Feature screens should consume
that layer instead of reconstructing Material surfaces, actions, and layout spacing
locally.

This rule is about consistency and maintainability, not hiding Material Compose.
Feature-specific controls such as text fields, progress bars, menus, tabs, and
specialized interactive widgets may continue to use Material components directly
when the shared layer does not add meaningful semantics.

## Preferred shared components

Use the following for ordinary content surfaces and actions:

- `IslamicCard` for standard feature cards and status/content containers.
- `IslamicPrimaryButton` for the principal action in a section.
- `IslamicSecondaryButton` for secondary/cancel/navigation actions.
- the shared `Muslim*State` family for loading, empty, error, offline, and
  permission-required presentation.

These components centralize touch targets, shapes, borders, elevations, and semantic
colors.

## Spacing

Use `IslamicSpacing` for layout padding and gaps.

The scale is intentionally small and semantic:

- `XSmall`: small inline separation.
- `Small`: compact icon/text and control gaps.
- `Compact`: closely related controls or compact sections.
- `Medium`: standard card/content padding.
- `Comfortable`: generous content spacing.
- `Large`: major local separation.
- `PageHorizontal`: screen horizontal gutter.
- `SectionVertical`: separation between independent content sections.

Raw dp values remain valid for measurements that are not layout spacing, such as a
progress indicator stroke width or a specialized geometry calculation.

## Initial strict surfaces

The CI design-system verifier now treats the following as reference surfaces:

- Settings Update screen: shared cards, buttons, and spacing.
- Scholar Library data manager: shared cards, buttons, and spacing.
- Quran Bookmarks: centralized spacing tokens.

These screens should remain free of screen-local ordinary `Card`, `Button`, and
`OutlinedButton` patterns.

## Migration rule

When modifying another feature screen:

1. Reuse an existing shared component when its semantics match.
2. Replace repeated spacing literals with `IslamicSpacing`.
3. Do not wrap measurable transfer progress in a generic loading state.
4. Keep specialized Material controls local when they express behavior the shared
   layer does not own.
5. If several features need the same new pattern, add it to the shared layer before
   duplicating it across screens.


## Expanded strict surfaces

The next adoption wave promotes additional high-use screens to strict shared-component
ownership:

- Notification Settings: ordinary cards and primary/secondary actions use the shared
  Islamic components while system dialogs, switches, chips, and text actions remain
  specialized Material controls.
- Prayer Settings: adjustment, readiness, battery, location, and per-prayer Adhan
  surfaces/actions use the shared component layer.
- Hadith: the remaining ordinary daily-Hadith card uses `IslamicCard`.
- Quran Downloads: all ordinary layout padding/gaps now use `IslamicSpacing`;
  progress-bar dimensions remain explicit geometry.

The CI verifier now protects these surfaces from reintroducing ordinary raw
`Card`, `Button`, or `OutlinedButton` patterns. It also requires Quran Downloads
to remain free of raw dp layout-spacing literals.


## Secondary settings surfaces

The settings consolidation now also treats these screens as strict Design System
consumers:

- Accessibility: information/toggle cards use `IslamicCard`; layout spacing and
  ordinary icon sizing use shared tokens.
- Permissions: the summary surface uses `IslamicCard`, the runtime grant action
  uses `IslamicPrimaryButton`, and status-row spacing is tokenized.
- About: informational sections use `IslamicCard` and shared spacing/icon tokens.
- Privacy: page content padding uses `IslamicSpacing`.
- Smart Devices: page spacing uses `IslamicSpacing` and the save action uses
  `IslamicPrimaryButton`.

Specialized Material controls remain intentionally local: switches, chips, text
fields, text-only utility buttons, top-app-bar navigation, and system-facing
controls do not need a shared wrapper unless multiple features develop the same
semantic pattern.


## Main settings and appearance

The Settings consolidation now covers the two largest remaining surfaces:

- Main Settings uses `IslamicSpacing` for ordinary layout gaps and shared
  `IslamicPrimaryButton` / `IslamicSecondaryButton` actions for update workflows.
- Appearance uses `IslamicSpacing` for editor layout and the shared
  `IslamicSelectableCard` for palette and corner-style choices.

`IslamicSelectableCard` owns selection semantics, selected/unselected border
emphasis, elevation, shape integration, and content padding. Feature previews still
own their internal illustrative geometry, so raw dp values remain acceptable for
swatch sizes, skeleton heights, preview offsets, and other non-layout drawing
measurements.

With this step, ordinary Settings surfaces are expected to consume the shared Design
System by default; new raw Material `Card`, `Button`, or `OutlinedButton`
patterns should require a documented specialized interaction rather than becoming a
new local convention.
