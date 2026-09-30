# Muslim UI/UX 2.0 — UX08 Quran Reader & Recitation V2

Date: 2026-09-29  
Phase: UX08  
Status: implementation in progress

## Goals

Make the Quran reader reading-first while preserving all current playback, bookmark, Tajweed, supplement, download, session-restore and offline behavior.

## Highlight rendering

All ayah state highlights now use one line-aware renderer:

- tapped ayah
- active playback ayah
- ayah opened from search/bookmark/resume
- selected ayah

The reader no longer uses `SpanStyle(background = ...)` for ayah state backgrounds.

### Geometry contract

For each highlighted ayah:

1. Resolve actual wrapped lines with `TextLayoutResult`.
2. Intersect the ayah character range with each real line.
3. Scan glyph bounding boxes so Arabic bidi punctuation and the ayah marker do not produce incorrect logical-edge assumptions.
4. Draw one rounded rectangle per line.
5. Shrink the rectangle vertically inside the Compose line bounds instead of expanding it.
6. Keep a real gap between adjacent lines.
7. Exclude the trailing separator from the ayah highlight range.
8. Never change text layout when highlight state changes.

Playback may retain a stronger fill and border, but it uses the exact same line-local geometry as every other highlight state.

## Ayah marker color contract

The Mushaf page renderer must keep ayah-number ornaments visually consistent:

- the first visible ayah marker on every page uses the same gold/bronze marker color as later ayah markers,
- page slicing must not cause the first ayah marker to inherit the green primary/accent color,
- selected/tapped/opened/playback states may highlight the ayah text/background but must not create a one-off first-marker color,
- the contract applies to light, sepia and dark reading themes and across page transitions,
- regression coverage must include a page whose first visible ayah is not ayah 1 of the Surah.

## Contextual ayah actions

Tapping an ayah still selects and scrolls to it, and now opens one shared `MuslimActionSheet`.

The sheet preserves/provides:

- play from selected ayah
- add/remove bookmark
- meanings/tafsir controls
- share ayah
- copy ayah

The permanent bookmark action is removed from the top bar because bookmarking belongs to the selected ayah context.

## Reader settings

The previous long top-bar dropdown is replaced by one shared `MuslimBottomSheet`.

It contains:

- reading theme
- font size
- keep screen on
- Tajweed
- meanings/tafsir
- Surah details
- Quran downloads

The reading theme is no longer a permanently exposed top-bar icon.

## Recitation bar

The permanent player surface now exposes only the frequent controls:

- current reciter identity
- current Surah/ayah and elapsed time when active
- previous
- play/pause
- next
- recitation settings
- stop while active

Advanced controls move into a recitation settings sheet:

- reciter selection
- repeat count / continuous mode
- stop at end of Mushaf for continuous playback
- playback range

This keeps every existing capability while reducing the height and control density of the permanent player.

## Feature parity preserved

UX08 must not change:

- recitation queue semantics
- local-first audio behavior
- download behavior
- persisted reader theme/font/Tajweed/supplement preferences
- session restore
- retry behavior
- repeat semantics
- playback range semantics
- Surah navigation
- opening-Basmala rules
- bookmark persistence
- current/last-read persistence

## Tests and guards

- `QuranTextHighlightRendererTest` verifies adjacent line bounds remain separated.
- `QuranReaderAyahActionsTest` verifies shared/copied ayah text formatting.
- `verify_quran_reader_v2.py` prevents hard SpanStyle ayah backgrounds from returning.
- CI runs the reader verifier with the existing Quran playback and session-restore contracts.

## UX08 exit gate

UX08 closes only when:

- [ ] debug app + Wear builds succeed
- [ ] Quran unit tests succeed
- [ ] all project unit tests succeed
- [ ] Android lint succeeds
- [ ] Detekt succeeds
- [ ] prayer/Qibla emulator tests API 26 and 36 succeed
- [ ] Family Life emulator tests API 26 and 36 succeed
- [ ] existing Quran playback/session verifiers succeed
- [ ] Quran Reader V2 verifier succeeds
