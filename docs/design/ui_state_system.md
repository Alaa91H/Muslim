# Shared UI state system

Date: 2026-09-24

## Goal

Feature screens should not invent a new visual language for loading, empty,
recoverable-error, offline, or permission-required states. State logic remains
feature-owned; presentation is standardized in `core-ui`.

## Shared components

The following composables are the preferred presentation primitives:

- `MuslimLoadingState`
- `MuslimEmptyState`
- `MuslimErrorState`
- `MuslimOfflineState`
- `MuslimPermissionRequiredState`

All are built on `MuslimStateSurface`, so typography, spacing, container roles,
touch targets, and recovery actions remain consistent across themes and screen sizes.

## Content state

Normal content remains feature-owned. A feature should render its content directly
once data is available rather than wrapping all successful content in another
generic container.

## Recovery actions

A single recovery action keeps the existing lightweight TextButton presentation for
backward compatibility. When a state has both a primary and a secondary recovery
path, the primary action receives stronger button prominence while the secondary
action remains a TextButton.

Examples:

- empty nearby-mosque results: expand radius + retry;
- error: retry;
- permission required: request/try permission flow again;
- offline cache: communicate degraded/offline operation while cached content remains usable.

## Accessibility

The state is never communicated by color alone. Every state includes a textual title;
optional supporting text and icons reinforce meaning. Loading uses a progress
indicator together with text. Actions retain the project-wide minimum touch target.

## Initial adoption

The first migration intentionally covers different product areas:

- Nearby Mosques: loading, empty, error, offline, permission-required, and search-empty states;
- Quran Bookmarks: empty state;
- Scholar Library data manager: no-installed-packs state.

The design-system CI verifier checks these representative surfaces so local state
implementations do not silently reappear.

## Migration rule

When touching a screen that contains a local loading/empty/error/offline/permission
card, migrate that state to the shared component unless the screen requires a
documented interaction that the shared API cannot express. In that case, extend the
shared API in a backward-compatible way before adding another screen-local pattern.


## Expanded adoption

The second migration wave extends the shared states to additional high-traffic flows:

- Hadith: corpus import progress, book-load failure, paging loading, paging failure,
  and no-results states.
- Quran Downloads: no active tasks and no coverage data.
- Updates: update-check loading, update-service unavailable, and download failure.

Transfer progress such as active APK/Quran download percentages remains feature-specific
because it represents measurable work rather than a generic screen loading state.
