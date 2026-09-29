# Muslim UI/UX 2.0 — UX11 More Hub

Date: 2026-09-29  
Phase: UX11  
Status: implementation complete, pending CI gate

## Goal

Turn More into a calm, searchable secondary-feature hub without removing any destination, persisted customization or seasonal behavior.

## Hub hierarchy

The legacy always-expanded section list is replaced by:

- shared `MuslimScreen`
- shared `MuslimTopBar`
- one `MuslimSearchBar`
- one `MuslimExpandableSection` per visible section
- shared `MuslimSettingsItem` rows for destinations

The Worship section is expanded initially. Other sections remain collapsed until opened.

When search is active, matching sections expand automatically so a result is never hidden behind a collapsed section.

## Direct customization

A single Tune action in the More top bar opens the existing More-order route directly.

Users no longer have to discover section customization indirectly through Settings.

## Feature parity preserved

All existing secondary destinations remain represented:

- Adhkar
- Tasbih
- Ramadan
- Habits
- Hadith
- Learning
- Noorani / New Muslim
- Traveller / Expat
- Family Life
- Funeral / Will
- Reference
- Islamic History
- Scholar Library
- Zakat
- Islamic Finance
- Quran downloads
- Accessibility
- Settings

The seasonal Ramadan rule is unchanged: when Ramadan is already promoted to primary navigation, the duplicate More shortcut stays hidden.

## Persisted customization

Existing DataStore contracts remain unchanged:

- section order
- hidden sections
- default section identifiers

The customization screen still provides:

- long-press drag reorder
- show/hide switch for each section
- immediate persistence after drag
- restore-default action

## More-order screen V2

The customization destination now uses:

- `MuslimScreen`
- `MuslimTopBar`
- `MuslimInlineMessage`
- `IslamicSecondaryButton`
- shared semantic spacing

Drag state is isolated from rendering in `MoreReorderState`, reducing UI coupling while preserving the original gesture behavior.

## Accessibility

- Search has a localized clear description.
- The Customize action has a localized content description.
- Drag handles expose a localized reorder semantic.
- Destination rows keep explicit icon and trailing-navigation semantics.

## Verification

`verify_more_hub_v2.py` guards:

- V2 shared components
- search and expandable hierarchy
- section order/hide contracts
- every destination callback
- seasonal Ramadan behavior
- direct More-order route
- drag, switch, persistence and reset capabilities
- absence of the legacy raw TopAppBar / IslamicListItem hub pattern

## Exit gate

UX11 closes only when:

- [ ] static UX11 contract passes
- [ ] debug builds succeed
- [ ] unit tests succeed
- [ ] Android lint succeeds
- [ ] Detekt succeeds
- [ ] Prayer/Qibla emulator tests API 26 and 36 succeed
- [ ] Family Life emulator tests API 26 and 36 succeed
- [ ] all previous UI/UX V2 contracts remain green
