# Muslim UI/UX 2.0 — UX12 Adhkar & Tasbih

Date: 2026-09-29  
Phase: UX12  
Status: implementation complete, pending CI gate

## Goal

Make Adhkar and Tasbih calmer, faster and easier to operate with one hand while preserving every existing worship, reminder, speech, counting, session, history and customization capability.

## Adhkar

### Library

The Adhkar library now uses the shared V2 shell and interaction patterns:

- `MuslimScreen`
- `MuslimTopBar`
- `MuslimSearchBar`
- `MuslimFilterBar`
- `MuslimSettingsItem`
- `MuslimOverflowMenu`

The screen preserves:
- category filtering,
- text search,
- favorites-only filtering,
- favorites,
- counters,
- reset,
- copy,
- share,
- text-to-speech,
- Morning/Evening reminder master control,
- full Reader mode,
- settings,
- visibility customization.

Secondary actions no longer consume a permanent row of icon buttons on every card.

### Reader

Reader mode keeps one dhikr in focus at a time with:
- progress header,
- previous/next,
- finish action,
- the same counter/favorite/copy/share/speech behavior as the library.

### Settings and customization

Settings use progressive disclosure for infrequent options. The customization list remains independently accessible and keeps the persisted visibility contract.

## Tasbih

Tasbih is now counter-first:

- phrase/category selection stays compact,
- the large circular counter remains the dominant surface,
- the phrase and tap guidance stay close to the counter,
- virtue content is collapsible,
- undo remains directly available,
- reset/reset-all move to shared overflow,
- session configuration is collapsible,
- target presets/custom target remain available,
- Free / Target / Rounds session modes remain available,
- round-complete sound remains configurable,
- daily/weekly/session history moves to the Activity section.

## Feature parity

UX12 preserves:

### Adhkar
- library contents,
- categories,
- search,
- favorites,
- counters,
- reminders,
- periodic/morning/evening scheduling,
- overlay behavior,
- TTS,
- copy/share,
- Reader mode,
- persisted visibility and preferences.

### Tasbih
- per-phrase counters,
- category/phrase selection,
- haptic counting,
- undo,
- reset/reset all,
- custom targets,
- 33/99/100/1000 presets,
- Free/Target/Rounds modes,
- rounds goal,
- target sound,
- daily total,
- weekly history,
- session history,
- persistence,
- widget behavior.

## Verification

`verify_adhkar_tasbih_v2.py` guards:

- V2 shell adoption,
- search/filter/disclosure hierarchy,
- Adhkar reader/settings/customization routes,
- Adhkar reminder/speech/counter/favorite/copy/share contracts,
- Tasbih counter-first hierarchy,
- session modes,
- history,
- target/sound controls,
- shared overflow usage,
- no regression to local popup menus.

## Exit gate

UX12 closes only when:
- [ ] static UX12 contract succeeds,
- [ ] debug builds succeed,
- [ ] unit tests succeed,
- [ ] lint succeeds,
- [ ] Detekt succeeds,
- [ ] prayer/Qibla emulator tests pass on API 26 and 36,
- [ ] Family Life emulator tests pass on API 26 and 36,
- [ ] Development APK is published successfully.
