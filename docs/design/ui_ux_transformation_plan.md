# UI/UX Transformation Plan

> **Status:** The core `ui/premium-islamic-experience` workstream was merged into `main` and released in `v1.25.0`. A follow-up consistency pass for selected secondary product surfaces is in progress and remains untagged until validation completes. This document describes presentation and interaction improvements only. It does not change religious content, prayer calculations, offline-first behaviour, notification delivery, or data contracts.

## Product intent

Muslim should feel quiet, precise, trustworthy and unmistakably Islamic without relying on religious clip-art or decorative excess. The visual language is **Modern Islamic Minimalism**: warm reading surfaces, botanical green for meaningful actions and state, restrained bronze as an accent, clear Arabic and Latin hierarchy, low-elevation surfaces, and geometry that supports orientation rather than competes with content.

> The design system serves worship and study content. It must not turn sacred text into decoration, gamify worship, or create stronger religious, privacy, provider, or delivery claims than the existing implementation supports.

## Audit snapshot

The application already has a sound architectural foundation: a Compose `AppTheme`, Material 3 colour schemes, shared geometry and spacing tokens, an Islamic-ornament layer, four stable primary destinations, DataStore-backed appearance/accessibility preferences, feature modules, and real application instrumentation coverage. The principal UX contracts are the existing routes, optional More-section order/visibility, local-first storage, Arabic/RTL support, notification identity separation, Quran reader state, Hadith Paging, and paired Wear behaviour.

| Area | Existing strength to preserve | Design debt to address first |
|---|---|---|
| Theme and preferences | Light, dark, high-contrast, reading, palette, corner and ornament preferences are already centralised. | Many feature screens still use local sizes, cards, and state surfaces rather than semantic shared primitives. |
| App shell | The four-tab model is small, familiar and functionally stable. | The shell provides limited visual continuity between tabs and secondary routes. |
| Prayer Times | The screen already exposes date, location, next prayer, live countdown, timetable, navigation and optional tracker. | The primary hierarchy is spread across several local cards and raw spacings; state cues need stronger semantic treatment. |
| Quran | Reader supports reading themes, Arabic sizing, bookmarks, playback, paging, tafsir, tajweed and wide-screen spreads. | The entry list and media controls use screen-local surfaces; any reader work must stay presentation-only because its state machine is intentionally dense. |
| Hadith | Offline preparation, Paging, filters, daily item, bookmarks, copy/share and retry paths are explicit. | Loading, error and empty states are functional but inconsistent and visually minimal. |
| More and Settings | More groups are persisted/reorderable/hideable; Settings exposes rich user choices. | Both use repeated local cards and accordion/list patterns that can be made clearer without altering routes or persistence. |
| Secondary features and Wear | Qibla, Adhkar, Tasbih, Learning and Wear have independent focused surfaces. | Qibla, Tasbih, Learning, Islamic Finance, and Wear are the selected follow-up scope for shared hierarchy, state treatment, responsive behaviour, and accessibility consistency. |

## Design decisions

| Decision | Rationale | Non-negotiable boundary |
|---|---|---|
| Keep the `v2028` geometric app identity as the visual anchor. | It creates recognisable continuity across launcher, app and product details. | The full-colour launcher identity and monochrome Android status-bar glyph remain distinct. |
| Extend, rather than replace, the existing token system. | Existing themes and user preferences already depend on it. | Do not hard-code feature-local palettes or silently discard a user-selected palette/shape/ornament option. |
| Introduce semantic shared UI primitives only where patterns recur. | This reduces visual drift without producing unnecessary abstraction. | Avoid wrappers that conceal accessibility or make feature state harder to reason about. |
| Make Prayer Times the first flagship redesign. | It is the app’s main emotional and functional anchor and already has a contained state model. | Preserve calculation, date navigation, use-24-hour, location, share, monthly view, tracker, alarms and notification behaviour. |
| Treat the Quran reader as a reading environment. | Mushaf content must remain primary and the reader state is specialised. | Do not alter ayah paging, audio state, tafsir/tajweed behaviour, bookmarks, or reading progress for visual convenience. |
| Use semantic state surfaces. | Loading, empty, retry, unavailable, permission and offline states need clear next actions. | Never expose raw exceptions or imply a network feature is required when it is optional. |
| Make accessibility and RTL implementation constraints, not a final polish layer. | Arabic, TalkBack, touch ergonomics, large text and layout direction are core product quality. | Use logical start/end, semantics, minimum touch targets, Material state layers and reduced motion. |

## Implementation sequence

The work is deliberately incremental so each commit remains reviewable and each visual change can be isolated from application logic.

| Workstream | Primary outputs | Acceptance evidence |
|---|---|---|
| Tokens and primitives | Expanded semantic spacing, type, state, surface and motion roles; reusable state and section primitives. | Token documentation, previews, static visual verifier and focused unit/Compose checks. |
| App shell and More | Premium bottom navigation treatment, app scaffold conventions, clearer More hierarchy while retaining persisted order and visibility. | Stable existing routes, RTL-aware icons, semantics and navigation tests. |
| Prayer Times | Refined hierarchy for date/location/next prayer/countdown/schedule; semantic current-next-upcoming cues. | Existing ViewModel contracts and prayer instrumentation tests remain unchanged and green. |
| Quran | Reader-entry and recitation-control visual refinement; focused reading surfaces. | Bookmarks, paging, audio, downloads, tafsir and tajweed behaviour retain their tests and routes. |
| Knowledge, worship and settings | Consistent scholarly reading, calm devotional controls, clear settings sections and contextual explanations. | Paging and state tests, preference persistence and accessibility checks. |
| Responsive, accessibility and Wear | Large-text/RTL validation, adaptive content widths and concise Wear consistency. | Accessibility verifier, representative previews/tests and existing Wear checks. |
| Final verification | Visual/debt audit, documentation and full CI. | Gradle unit tests, Lint, Detekt, relevant static verifiers and application emulator coverage. |

## Secondary-surface consistency follow-up

The post-`v1.25.0` pass applies the established system to genuinely remaining gaps rather than repeating the completed flagship navigation, Prayer Times, Quran, Hadith, and Settings work. Qibla gains a semantic hierarchy and a device-independent geometric direction marker; Tasbih gains a screen-reader description for its primary counter and semantic progress colours; Learning and Islamic Finance adopt the shared section, state, card, and action primitives; Wear improves its glanceable prayer and Tasbih hierarchy. The paired-phone contract, local finance ledger, Qibla calculation, sensor handling, haptic setting, devotional content, and all navigation destinations remain unchanged.

## Component policy

The following primitives are candidates for implementation because they recur across modules: `MuslimAppScaffold`, `MuslimSectionHeader`, `MuslimListSurface`, `MuslimStateSurface`, `MuslimInlineStatus`, `MuslimPrimaryAction`, `MuslimSecondaryAction`, and a semantic navigation-item treatment. Prayer, Quran, Hadith, Adhkar and Tasbih retain feature-owned components when their behaviour is specialised.

Every new shared component must expose a meaningful label/description path, preserve a minimum 48dp interactive target where it is actionable, use Material semantic colours, work under RTL, and avoid animation when the user has requested reduced motion.

## Verification requirements

No workstream may claim completion until the relevant source checks pass. The release gate remains the established main/tag CI pipeline. Local validation should include the project’s visual and accessibility verifiers together with the relevant Gradle unit, lint and Detekt tasks when an Android SDK is available.

| Scope | Required checks |
|---|---|
| Theme/design-system work | `python3 scripts/verify_islamic_visual_identity.py`, unit tests, Lint and Detekt. |
| Accessibility work | `python3 scripts/verify_accessibility.py`, semantic/touch-target review and relevant instrumentation. |
| Navigation/feature work | Existing navigation and feature verification scripts plus application instrumentation where applicable. |
| Before merge | `git diff --check`, focused tests, full CI on the work branch, then a deliberate review before `main` is changed. |

## References

[1]: https://developer.android.com/develop/ui/compose/designsystems "Android Developers — Design systems in Compose"
[2]: https://developer.android.com/develop/ui/compose/accessibility "Android Developers — Accessibility in Compose"
[3]: https://m3.material.io/ "Material Design 3"
