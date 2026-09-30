# Muslim UI/UX 2.0 — UX01 full interface audit

Date: 2026-09-29  
Branch: `ux/ui-ux-2`  
Baseline: `main@9fae19da5f6d073b83b1a45a096467c9ba9d8326`

## Scope and method

UX01 audits the existing presentation layer before changing shared tokens or behavior.

The audit covered:
- the root app shell/navigation,
- every routed primary/secondary feature surface frozen by UX00,
- the major settings surfaces,
- the major reader/library surfaces,
- prayer customization and monthly/daily presentation,
- Wear OS,
- supporting UI files used by the routed screens,
- the existing design-system tokens and adoption rules.

The static survey directly inspected 53 high-value user-facing Kotlin UI files in addition to the route/deep-link/preference inventory created in UX00. The remaining non-screen presentation helpers remain feature-owned and are migrated together with their parent phase.

## Executive finding

The application already has a credible shared foundation, but adoption is uneven.

The newer/high-use surfaces — especially Prayer Home, Location, More, several Settings screens, Quran list/downloads, Hadith and parts of Qibla/Adhkar/Ramadan — already consume `IslamicSpacing`, `IslamicCard`, `MuslimContentFrame`, shared state surfaces, or shared actions.

The largest visual debt is concentrated in older or feature-rich screens that still build their own visual language with local `dp` geometry and raw Material cards/buttons. This means UI/UX 2.0 should **extend and enforce the existing design system**, not replace it.

## Existing strengths to preserve

- `IslamicSpacing` already provides a usable central scale.
- `IslamicIconSize`, `IslamicRadius`, `IslamicElevation`, `IslamicMotion` and `MuslimTouchTarget` already centralize important primitives.
- `IslamicCard`, `IslamicSelectableCard`, `IslamicListItem`, `MuslimSectionHeader` and shared state surfaces already exist.
- `MuslimAppScaffold` owns the app background/ornament layer and scaffold slots.
- `MuslimContentFrame` already caps ordinary task content at 760dp.
- Reduced-motion preferences are already bound through the app theme.
- Light/dark/high-contrast/dynamic/AMOLED appearance paths already exist.
- Existing CI already prevents regressions on selected strict surfaces.

## Foundation gaps

### AUDIT-DS-01 — Missing semantic screen layer

There is no single shared primitive that owns all of:
- system insets,
- compact/medium/expanded layout policy,
- screen title/top bar behavior,
- content width,
- optional scrolling,
- primary/overflow action hierarchy.

Action: UX03 introduces `MuslimScreen` and `MuslimTopBar`.

### AUDIT-DS-02 — No shared bottom-sheet/action-sheet language

The surveyed critical screens use dialogs and dropdown menus, but no consistent shared bottom-sheet pattern is present.

Action: UX03 introduces `MuslimBottomSheet` and `MuslimActionSheet`, then later phases move secondary/advanced controls into them.

### AUDIT-DS-03 — Responsive policy is only partially centralized

`MuslimContentFrame` provides a width cap, but feature code still makes local width/height decisions. Prayer Home and Qibla use `BoxWithConstraints` with screen-local thresholds.

Action: UX04 establishes semantic Compact / Medium / Expanded window policy.

### AUDIT-DS-04 — Typography is Material-role based, not task-semantic

The current typography scale is complete at Material-role level, but there are no explicit semantic styles for:
- hero time/countdown,
- religious display,
- Quran Arabic,
- Quran translation,
- tafsir,
- reader metadata.

`MuslimFonts` also still contains historical TODO comments and currently resolves its Arabic/Latin/Quran families to `FontFamily.Default`.

Action: UX02 adds semantic typography tokens without changing Quran content or reader behavior.

### AUDIT-DS-05 — Shared settings row is missing

Settings-related screens repeatedly use raw Material `ListItem`, switches and local section wrappers.

Action: UX03 introduces `MuslimSettingsItem` while keeping specialized controls (Switch, slider, text fields) local.

## Complexity hotspots

The following files are the highest-priority presentation hot spots because of size, local geometry, or raw component density.

| Surface | Approx. LOC | Raw dp literals | Key issue |
|---|---:|---:|---|
| Quran Reader | 2328 | 62 | Very large reader surface; toolbar/actions/highlight/audio geometry coexist in one file |
| Prayer Settings | 1585 | 49 | Large settings surface with many composables and dialogs |
| Funeral / Will | 1489 | 44 | Large multi-workflow surface with local cards/buttons |
| Islamic History | 1383 | 62 | Heavy local Material actions/cards and local spacing |
| Reference | 1329 | 55 | Large reader/library surface with many local actions |
| Hadith | 1225 | 26 | Large but already substantially tokenized |
| Family Guide Hub | 1165 | 41 | Large feature hub with local geometry |
| Scholar Library Screens | 1159 | 58 | High raw Card/Button density |
| Adhkar Settings | 1146 | 7 | Large settings surface, mostly tokenized |
| Hajj & Umrah | 1101 | 83 | Highest local geometry count; nine raw Cards |
| Family Life | 956 | 33 | Large hub/reader combination |
| Quran Downloads | 958 | 12 | Large transfer-management surface, mostly tokenized |
| Adhkar | 923 | 8 | Large but already uses shared spacing/actions |
| Notification Settings | 891 | 23 | Many settings controls and local action patterns |
| Qibla | 834 | 61 | Specialized compass geometry mixed with layout geometry |
| Settings | 773 | 2 | Tokenized spacing but repeated raw ListItem hierarchy |
| Learning Lesson Reader | 726 | 25 | Card-heavy learning reader |
| Prayer Home | 696 | 9 | Good token adoption; specific hierarchy/space problems remain |
| Learn | 692 | 15 | Hub needs semantic regrouping |
| Tasbih | 613 | 9 | Good token adoption; interaction hierarchy can be simplified |
| Traveler / Expat | 600 | 55 | Heavy local geometry |
| Islamic Finance | 596 | 33 | Local layout values and hub density |
| Noorani / New Muslim | 582 | 50 | Raw cards + local geometry |
| Scholar Curriculum | 557 | 30 | Raw cards/actions |
| Names of Allah | 490 | 43 | Raw card/layout patterns |
| Zakat | 462 | 37 | Wizard opportunity; local layout values |

Raw `dp` counts include specialized geometry as well as layout values; they are therefore a **migration signal, not an automatic defect count**. Specialized Quran/compass/drawing geometry remains feature-owned when it is semantically correct.

## App shell and navigation

### Current state
- Primary bottom destinations are already limited to Prayer, Quran, Qibla and More.
- Ramadan is conditionally promoted using the existing adjusted-Hijri rule.
- Root shell already preserves tab state with `saveState` / `restoreState`.
- Bottom navigation performs local slot-width calculations inside `MuslimApp.kt`.

### Required V2 work
- Move visual navigation sizing into the shared adaptive shell.
- Add rail behavior for wider windows.
- Preserve every UX00 route and deep link.
- Do not expose more top-level destinations merely because more features exist.

Priority: UX05.

## Prayer Home

### Existing positives
- Strong shared-spacing adoption.
- `MuslimContentFrame` is already used.
- Shared secondary actions already exist.
- Next-prayer content already has a distinct surface.

### UX issues confirmed
- Upper screen hierarchy wastes vertical space before/around the date/header.
- Gregorian date is repeated again below “Today’s Prayer Times”.
- Per-prayer alert presentation exposes volume percentage, adding technical noise.
- Prayer symbols are visually inconsistent as a family.
- Daily schedule and primary hero compete for space more than necessary.
- Local compact-height/width thresholds remain in the screen.
- Monthly mode is a dense calendar grid rather than a true all-prayer timetable.

### Required result
- Compact date/location header.
- One dominant next-prayer hero.
- No duplicate date.
- Clean prayer rows.
- State-based alert/customization indicator with no exposed percentage.
- Advanced sound/volume/vibration controls move into a structured sheet.
- Imsakiyah-style monthly table is the default monthly schedule.

Priority: UX06.

## Prayer Settings

The file is one of the largest UI surfaces (about 1.5k LOC, 34 composables). The design-system adoption is already better than many older features, but interaction density remains high.

Required work:
- group settings by user goal,
- keep calculation choices separate from alert/audio choices,
- use shared settings rows and expandable advanced sections,
- preserve every prayer preference frozen by UX00.

Priority: UX06 / UX21.

## Quran Home

Current Surah list is relatively clean and already avoids raw ordinary Material cards/actions.

Required work:
- Continue Reading hero,
- unified search,
- Surah/Juz/Bookmarks segmentation,
- stronger information hierarchy without adding visual containers.

Priority: UX07.

## Quran Reader

Largest single presentation hotspot.

### Confirmed architecture problem
Reading content, contextual actions, reciter controls, settings, dropdowns, dialogs, audio state and highlight geometry coexist in a very large screen file.

### User-visible defect frozen by UX00
Multi-line highlight backgrounds can visually overlap neighboring Quran lines/glyphs.

### Required work
- reading-first scaffold,
- contextual ayah action sheet,
- less permanent chrome,
- compact mini-player,
- structured reader settings sheet,
- extract highlight geometry into a dedicated line-aware renderer based on actual text layout,
- preserve reader session/audio/download contracts.

Priority: UX08.

## Quran Downloads / Bookmarks

Downloads is large but already uses shared spacing heavily. Its main problem is information architecture rather than basic visual primitives.

Required:
- Downloaded / Available / Active segmentation,
- visible storage summary,
- reduce repeated controls,
- keep progress geometry feature-specific.

Bookmarks is already simple and should remain lightweight.

Priority: UX09.

## Qibla / Nearby Mosques

Qibla contains much specialized geometry, which is expected, but ordinary page spacing and adaptive thresholds are mixed with compass geometry.

Required:
- isolate compass geometry from responsive page policy,
- make compass the visual center,
- collapse scientific metadata into details,
- integrate mosque map/list through a consistent sheet/list model.

Priority: UX10.

## More

Current More is already sectioned and uses `IslamicListItem`, which is a good base.

Required:
- feature search,
- user-chosen quick access,
- collapsible sections,
- retain existing hidden/reordered sections,
- migrate MoreOrder raw spacing/action to shared tokens/components.

Priority: UX11.

## Adhkar / Tasbih

Adhkar main/settings screens already have good token adoption. `AdhkarCustomizeScreen` is an outlier with local raw card/spacing.

Required:
- reading-first dhikr flow,
- count/progress hierarchy,
- keep customization out of the reader,
- simplify Tasbih into a large primary count/tap surface,
- move history/settings into secondary surfaces.

Priority: UX12.

## Ramadan / Habit Tracker

Both already use centralized spacing well.

Required:
- Ramadan countdown hero,
- compact daily dashboard,
- move long-form content deeper,
- keep habits calm and non-gamified.

Priority: UX13.

## Hadith

Large surface but comparatively mature in design-system adoption.

Required:
- Collections → Chapters → Hadith hierarchy,
- sticky/clear search,
- filters in one secondary surface,
- contextual hadith actions,
- skeleton/loading polish.

Priority: UX14.

## Learning / Hajj / Names / Noorani / Traveler

This is the largest inconsistent design cluster.

### High debt
- Hajj & Umrah: 83 raw dp literals and nine raw Cards.
- Noorani/New Muslim: 50 raw dp literals and five raw Cards.
- Names of Allah: 43 raw dp literals and three raw Cards.
- Traveler/Expat: 55 raw dp literals.
- Learning reader/hub also carries local geometry.

Required:
- migrate to shared tokens/components before visual polish,
- reorganize Learning as paths rather than a flat feature collection,
- Hajj as journey/timeline,
- Names as grid → detail,
- Noorani as lesson flow,
- Traveler as compact dashboard + expandable guidance.

Priority: UX15–UX16.

## Family / Funeral / Will

Large and capability-rich; no feature should be removed.

Required:
- separate learn/prepare/draft/checklist/reference tasks,
- preserve draft protection,
- make Family a focused hub feeding a shared reader/search experience.

Priority: UX17.

## Zakat / Finance

Both still have substantial local spacing.

Required:
- Zakat becomes a short staged wizard with a clear result summary,
- Finance separates education, tools and tracking.

Priority: UX18.

## Reference / Islamic History

Major migration hotspot.

Reference has about 55 raw dp literals and multiple local action/list patterns. Islamic History has about 62 raw dp literals, four raw Cards and many raw buttons.

Required:
- shared library/reader patterns,
- Timeline / People / Places organization for History,
- unified search/filter semantics,
- reduce permanently exposed actions.

Priority: UX19.

## Scholar Library

One of the highest raw-component clusters:
- `ScholarLibraryScreens.kt` contains multiple raw Cards and a high number of raw buttons.
- curriculum/review/session surfaces also reimplement local patterns.
- Data Manager is already much closer to the shared system and should be the migration reference.

Required structure:
- Library
- Study
- Notes
- Manage

Priority: UX20.

## Settings

Settings is already well-tokenized for spacing, but it uses a local accordion architecture and repeated Material `ListItem` rows.

Required:
- semantic sections by user goal,
- settings search,
- shared `MuslimSettingsItem`,
- reduce the amount of nested configuration visible at once,
- preserve every preference frozen by UX00.

Priority: UX21–UX23.

## Wear OS

Wear remains intentionally separate from phone UI. It should not inherit phone cards/layouts.

Required:
- glanceable next prayer,
- countdown,
- tasbih,
- minimal text and large touch targets.

Priority: UX24.

## Cross-app migration rules from UX01

1. **Do not blanket-replace raw dp.** First classify values as layout spacing vs specialized geometry.
2. **Do not force specialized controls into wrappers.** Compass drawing, Quran text metrics, progress strokes, sliders, maps and similar controls remain feature-owned.
3. **Shared patterns win for repeated semantics.** Ordinary cards, rows, primary/secondary actions, settings rows, screen shells, sheets and states belong to core UI.
4. **Hierarchy work precedes decoration.** Remove duplicate information and permanently exposed secondary actions before adding polish.
5. **One main focus per screen.**
6. **Responsive policy moves upward** into the shared screen/layout layer.
7. **Feature parity verifier remains mandatory** throughout all migration phases.

## UX01 exit criteria

- [x] Route/deep-link/settings baseline exists from UX00.
- [x] Major routed UI surfaces audited.
- [x] Existing design-system foundation audited.
- [x] Highest complexity/design-debt hotspots identified.
- [x] User-reported Prayer and Quran defects mapped to explicit phases.
- [x] Migration order remains UX02 → UX03 → UX04 → UX05 → feature phases.
- [ ] CI must be green for this audit commit before UX02 starts.
