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


## Prayer home and location

The Design System rollout now covers the primary prayer entry surfaces:

- Prayer Home uses `IslamicSecondaryButton` for share and daily/monthly actions.
  Existing 2 dp values inside tracker/grid cells remain local because they are
  compact grid geometry rather than reusable page spacing.
- Location uses `IslamicPrimaryButton` for saving manual coordinates,
  `IslamicSecondaryButton` for GPS acquisition, `IslamicSpacing` for all
  page/form layout spacing, and `IslamicListItem` for city search results.

Search fields and coordinate fields remain Material `OutlinedTextField` controls
because they are specialized data-entry widgets rather than generic app surfaces.


## Quran reader and Hadith library

The Design System rollout now protects the main reading surfaces without flattening
their specialized reading geometry:

- Quran Reader uses `IslamicCard` for the translation/tafsir supplement panel and
  `IslamicSelectableCard` for reciter selection. Raw Material `Card` patterns are
  no longer allowed in the reader.
- Surah List is already clean and is now protected from raw ordinary cards/actions
  and raw dp layout spacing.
- Hadith catalogue, book header, stat cards, chapter rows, and grade/source metadata
  use `IslamicSpacing` for ordinary padding/gaps while their custom book-cover
  dimensions, typography, palette, and decorative geometry remain feature-owned.

Quran page composition, active-ayah highlighting, Arabic text metrics, audio-control
geometry, and other reader-specific measurements remain explicit. They are not
forced into the generic spacing scale because changing them can affect Mushaf
readability and playback ergonomics.


## Phase 2 closure

The Design System consolidation now treats the remaining high-use worship surfaces
as strict consumers:

- Qibla uses the shared spacing scale for all ordinary layout gaps.
- Adhkar library/reader uses shared primary/secondary actions and centralized
  spacing.
- Adhkar Settings uses `IslamicCard`, `IslamicPrimaryButton`,
  `IslamicSecondaryButton`, and `IslamicSpacing` for ordinary surfaces/actions
  while sliders, dropdowns, TTS controls, color picking, and preview drawing remain
  specialized.
- Tasbih uses `IslamicSpacing` throughout ordinary screen layout.
- Ramadan and the reusable Habit Tracker use `IslamicSpacing` throughout ordinary
  screen layout.

Together with the previously protected Settings, Prayer, Location, Quran, Hadith,
More, Qibla-mosque, and Scholar Library surfaces, this closes the Phase 2 shared
Design System foundation. Future UI work should add a new local primitive only when
its behavior is genuinely feature-specific; otherwise it should consume the shared
semantic component layer.

Phase 3 may now focus on Quran Reader, audio, downloads/offline behavior, and reading
reliability without continuing broad visual-system migration in parallel.
