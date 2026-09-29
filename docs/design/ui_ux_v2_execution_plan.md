# Muslim UI/UX 2.0 — execution plan

Status: active  
Branch: `ux/ui-ux-2`  
Started: 2026-09-29

## Goal

Modernize and simplify the entire Muslim UI while preserving every current capability, route, deep link, preference, calculation, notification behavior, data contract, and offline behavior.

Core rule:

> Hide complexity; do not remove capability.

## Non-negotiable guardrails

1. Feature parity must remain 100%.
2. UI refactors must not change prayer calculations, Quran/Hadith content, scheduling semantics, persisted data, or religious content.
3. Secondary actions move to overflow menus, bottom sheets, or expandable advanced sections instead of remaining permanently exposed.
4. Shared design-system components are preferred over feature-local visual patterns.
5. Avoid card-within-card layouts unless hierarchy genuinely requires it.
6. RTL/LTR, TalkBack, large text, dark/light themes, reduced motion, compact/medium/expanded layouts remain first-class.
7. Every phase closes only after its relevant automated checks are green.

## UX00 — Freeze functional contracts

- Inventory routes and parameterized routes.
- Inventory external deep links/app shortcuts.
- Inventory persisted user preferences.
- Inventory More hub destinations.
- Record current visual baseline/screenshots for critical screens.
- Add a feature-parity verifier to CI.
- Do not begin UX01 until UX00 is closed.

## UX01 — Full UI/UX audit

Audit every screen/dialog/sheet/menu for purpose, primary action, secondary actions, duplication, unused space, excessive cards, local spacing, accessibility, RTL and adaptive-layout issues.

## UX02 — Design Tokens V2

Centralize spacing, typography, shapes, elevation, icon sizing, colors and motion.

## UX03 — Shared Components V2

Introduce/standardize:
- MuslimScreen
- MuslimTopBar
- MuslimHero
- MuslimSection / MuslimGroup
- MuslimListItem / MuslimSettingsItem
- MuslimStatusChip
- MuslimSegmentedControl
- MuslimSearchBar / MuslimFilterBar
- MuslimBottomSheet / MuslimActionSheet / MuslimOverflowMenu
- MuslimInlineMessage
- MuslimSkeleton
- MuslimReaderScaffold
- MuslimProgressHeader / MuslimMetric
- MuslimExpandableSection

## UX04 — Responsive foundation

Use shared compact/medium/expanded policies instead of scattered width/height thresholds.

## UX05 — App shell/navigation

Keep the primary destinations Prayer, Quran, Qibla and More, with the existing Ramadan seasonal behavior. Preserve navigation state and deep links.

## UX06 — Prayer experience

### Home
- Remove excessive whitespace above the date/header.
- Keep Hijri date, Gregorian date and location compact and non-duplicated.
- Make the next prayer the single dominant hero.
- Remove the duplicated date below “Today’s Prayer Times”.
- Render prayer times as clean rows rather than visually heavy nested cards.
- Replace inconsistent prayer symbols with one coherent icon family for Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha.
- Replace exposed volume percentages such as `100%` with a compact state-based alert/customization indicator.
- The indicator must distinguish enabled Adhan, short alert, vibration-only, silent, disabled and custom states without relying on color alone.
- Move volume, sound, vibration, offsets and advanced configuration into a structured bottom sheet/settings surface.
- Keep only one or two primary actions exposed; move secondary actions such as sharing/month view/secondary settings into overflow/sheets.

### Monthly view
Replace the dense calendar grid as the primary monthly prayer view with an imsakiyah-style timetable:
- Days run sequentially from the first to the last day of the month.
- Arabic layout reads naturally RTL.
- Prayer names are column headers at the top.
- Each day is a row.
- Show Fajr, Sunrise, Dhuhr, Asr, Maghrib and Isha for every day.
- Include Gregorian/Hijri context without crowding.
- Highlight today and Friday calmly.
- Prefer a sticky prayer header.
- Preserve a traditional calendar only as an optional secondary view if it still provides value.

## UX07 — Quran home

Continue Reading hero, unified search, Surah/Juz/Bookmarks segmented navigation and cleaner Surah rows.

## UX08 — Quran Reader and player

- Reading-first layout.
- Contextual ayah action sheet instead of permanently exposed actions.
- Collapsible/auto-hiding reader chrome where appropriate.
- Compact mini player with expanded player on demand.
- Structured reading/audio settings sheet.

### Quran highlight rendering fix
The highlight system must use actual text layout geometry:
- Split a multi-line highlight into per-line rectangles.
- Never cover adjacent lines or glyphs.
- Never move the text when highlight state changes.
- Keep controlled insets/radius and stable geometry.
- Cover recitation tracking, selected ayah, search result and manual selection states.
- Test small/medium/large text, RTL, portrait, landscape, light/dark and multi-line ayat.

### Ayah marker color consistency
- Fix the first ayah number rendered on every Mushaf page so it never inherits the green primary/accent color merely because it is the first ayah in the page slice.
- All normal ayah-number ornaments on a page must use the same gold/bronze Quran marker color, including the first visible ayah.
- Selection, playback, search/open and tap highlighting may affect the surrounding ayah highlight state, but must not accidentally recolor only the first page ayah marker.
- The rule must hold across light, dark and sepia reading themes, portrait/landscape, different font sizes and page boundaries.
- Add an automated visual/style contract that explicitly checks first-page-ayah marker styling versus subsequent ayah markers.

## UX09–UX25 — Remaining feature redesign

Sequentially redesign:
- Quran Downloads/Bookmarks
- Qibla/Nearby Mosques
- More hub
- Adhkar/Tasbih
- Ramadan/Habits
- Hadith
- Learning Centre
- Hajj/Names/Noorani/Traveler
- Family/Funeral/Will
- Zakat/Finance
- Reference/History
- Scholar Library
- Settings
- Privacy/Permissions/Notifications
- Storage/Downloads/Updates
- Wear OS
- Android Auto

All must preserve existing capability and use the shared patterns.

## UX26–UX31 — Completion gates

- Tablet/Foldable pass
- RTL/localization/200% font pass
- Screenshot/visual regression infrastructure
- Performance/motion polish
- Final consistency audit
- Full regression/release gate

## Definition of Done

A phase is not complete until:
- all previous routes and deep links still resolve,
- all relevant persisted settings remain represented,
- no intentional feature is lost,
- accessibility and RTL checks pass,
- relevant unit/instrumented tests pass,
- lint and Detekt pass,
- design-system and feature-parity verifiers pass,
- visual baselines are updated intentionally when the infrastructure exists,
- CI is green.
