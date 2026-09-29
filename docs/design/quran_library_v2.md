# Muslim UI/UX 2.0 — UX09 Quran Downloads & Bookmarks

Date: 2026-09-29  
Phase: UX09  
Status: implementation complete, pending CI gate

## Goal

Reduce Quran library-management clutter without removing any download, deletion, pause/resume, scheduling, bookmark or navigation capability.

## Bookmarks

The standalone bookmarks destination now uses the shared V2 screen hierarchy:

- `MuslimScreen`
- `MuslimTopBar`
- calm bookmark cards instead of a bare divider list
- consistent shared spacing/elevation tokens
- direct navigation to the saved ayah remains unchanged

The empty state is preserved.

## Downloads shell

The downloads hub now uses:

- `MuslimAdaptiveScreen`
- `MuslimTopBar`
- the same reciter pager/tabs and persisted selected reciter behavior
- the existing global and per-reciter download state

No route or ViewModel capability changed.

## New-download configuration

The dense inline configuration block was extracted into `QuranDownloadConfigurationV2.kt`.

It preserves:

- single-ayah download
- full-surah download
- full-Quran download
- verified size when available
- estimated size fallback
- start-download validation
- night-only mode
- configurable night start/end window

The three download scopes now use one `MuslimSegmentedControl`.

Night scheduling is progressive disclosure through `MuslimExpandableSection`, so infrequent scheduling controls no longer consume permanent screen space.

## Library hierarchy

The global Surah-coverage matrix is now collapsed by default and can be expanded on demand.

Per-reciter downloaded Surahs are also collapsed by default because the reciter summary already exposes:

- downloaded ayah count
- total size
- completion percentage

Opening the downloaded-content section still exposes:

- persisted Mushaf/completion sorting
- per-Surah completion progress
- per-Surah deletion

## Destructive actions

Repeated permanent delete buttons were reduced:

- delete-all-for-reciter moved into a contextual overflow menu
- per-Surah delete moved into a contextual overflow menu
- existing confirmation dialogs remain mandatory before deletion

No destructive action became one-tap destructive.

## Active downloads

Pause, resume and cancel remain directly available where the task is active because these are frequent, immediate task controls rather than secondary configuration.

Existing status and progress indicators remain.

## Feature parity

UX09 preserves:

- all reciters
- persisted selected reciter
- reciter swipe/tab navigation
- all three download scopes
- size probing/estimates
- night download scheduling
- background tasks
- pause
- resume
- cancel
- Surah deletion
- reciter-library deletion
- library rescan/summary/coverage
- persisted Surah sort mode
- bookmark opening

## Verification

`verify_quran_library_v2.py` guards:

- V2 screen shells
- extracted configuration panel
- segmented scope selector
- progressive disclosure
- contextual destructive actions
- pause/resume/cancel/delete feature parity
- bookmarks navigation

CI runs this verifier beside the existing Quran Reader, playback and persisted-session contracts.

## Exit gate

UX09 closes only when:

- [ ] debug builds succeed
- [ ] unit tests succeed
- [ ] Android lint succeeds
- [ ] Detekt succeeds
- [ ] prayer/Qibla emulator tests API 26 and 36 succeed
- [ ] Family Life emulator tests API 26 and 36 succeed
- [ ] Quran playback/session/reader/library verifiers succeed
