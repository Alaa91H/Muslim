# Muslim UI/UX 2.0 — Quran Home V2

Date: 2026-09-29  
Phase: UX07  
Status: implemented, pending CI gate

## Goal

Turn the Quran primary destination into one coherent discovery surface without removing any existing route, playback action, bookmark capability or reading-progress state.

## Primary hierarchy

The Quran Home now presents:

1. Quran title / reading identity
2. one unified search field
3. Surahs / Juz / Bookmarks segmented navigation
4. contextual content for the active segment

The previous separate bookmark icon in the top header is no longer the only discovery path.

## Continue Reading

When the Surahs segment is active and search is empty, the last-read position is promoted to a `MuslimHero`.

The hero preserves the existing resume callback and opens the exact stored global ayah.

## Khatma progress

Khatma progress remains visible and keeps the same source values.

It now uses the shared `MuslimProgressHeader` inside a quiet `MuslimGroup` instead of a feature-specific card hierarchy.

## Unified search

One search field filters the active Quran Home segment.

### Surahs
Matches:
- exact Surah number
- Arabic name
- English name
- translated title

### Juz
Matches Juz number.

### Bookmarks
Matches:
- Surah name
- ayah number
- ayah text

Search does not require network access.

## Surahs

Surah rows were simplified while preserving:

- Surah number
- Arabic name
- English name
- translated title
- ayah count
- Meccan/Medinan classification
- direct Surah open
- direct playback action

Supporting metadata is consolidated so each row has one clear primary reading action and one explicit playback action.

## Juz

The Quran Home now exposes all Juz entry points.

`SurahListViewModel` derives the first global ayah of each Juz from the same offline Quran repository used by the reader.

Selecting a Juz opens the reader at the first ayah of that Juz through the existing reader navigation contract.

A unit test protects Juz ordering and first-ayah selection.

## Bookmarks

Bookmarks are now visible inline as the third Quran Home segment.

The existing dedicated Bookmarks route remains available and is still reachable from the segment action, preserving navigation compatibility.

Opening an inline bookmark uses the existing reader navigation path and exact global ayah.

## Loading and empty states

Quran Home now uses shared V2 loading and empty-state components.

No screen-local spinner or raw ordinary Material card/button pattern was introduced.

## Feature parity

UX07 preserves:
- `quran` root route
- Quran Reader route
- dedicated Bookmarks route
- resume reading
- direct Surah playback
- stored Khatma progress
- existing Surah metadata
- offline-first behavior

## Verification

UX07 is complete only after:
- Quran unit tests pass,
- design-system verifier passes,
- feature-parity verifier passes,
- debug build passes,
- Android lint passes,
- Detekt passes,
- existing emulator suites remain green.
