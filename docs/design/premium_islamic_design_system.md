# Premium Islamic Design System

> **Purpose.** This system gives the Android and Wear experiences one restrained visual language: calm hierarchy, readable religious content, precise interaction feedback, and subtle geometric identity. It preserves feature ownership; it does not move prayer calculation, location, Quran, Hadith, notification, or persistence logic into the visual layer.

## Product principles

The interface uses **modern Islamic minimalism** rather than decorative symbolism. Islamic character comes from proportion, warm material-like surfaces, conservative use of the existing geometric ornament, Arabic-first reading rhythm, and disciplined color roles. Sacred text is never used as decorative background material.

| Principle | Applied rule |
|---|---|
| One clear focus | The next prayer is the primary focal point on Home; supporting schedule, location, and configuration stay secondary. |
| Content before container | Reading surfaces rely on typography, spacing, dividers, and tonal layers; cards are used only for meaningful grouping. |
| Local trust | Permissions, location, downloaded content, and optional networking use contextual explanations and recoverable states rather than technical exceptions. |
| Accessible restraint | Touch targets remain Material-sized, icons have meaningful labels where they are actions, and visual status is not communicated by color alone. |
| Responsive clarity | Ordinary task content is centered and bounded on wider layouts, while specialist readers retain control of their own reading width. |

## Central tokens and primitives

The existing `core-design-system` tokens remain the canonical source for palette, typography, shapes, spacing, icon sizes, and motion. `core-ui` supplies presentation primitives on top of those tokens, avoiding feature-specific replacements for theme values.

| Primitive | Responsibility | Current adoption |
|---|---|---|
| `AppTheme` | Material color scheme, typography, shapes, high contrast, reading mode, and stored reduced-motion preference. | Root application theme. |
| `MuslimAppScaffold` | Deliberate background/content colors and standard top, bottom, snackbar, and floating-action slots. | Root application, More, Hadith, Location, and Settings. |
| `MuslimContentFrame` | Centers task content and applies a 760dp readable maximum on large screens and foldables. | Home, Location, Hadith, More, and Settings. |
| `IslamicCard` / `MuslimStateSurface` | Tonal grouping and calm, actionable loading/error/information states. | Shared across feature screens. |
| `MuslimSectionHeader` | Hierarchy for grouped information without excess card nesting. | Home, More, and feature sections. |
| `LocalMuslimMotionPreferences` | Makes motion durations immediate when the persisted reduced-animation preference is enabled. | Available to all Compose modules through the theme. |

## Color and surface language

The palette is semantic rather than decorative. Botanical primary tones establish identity; warm parchment-like light surfaces and layered charcoal dark surfaces establish reading comfort. The tertiary role is reserved for meaningful emphasis, such as the next prayer. Information, warning, error, and success states use semantic roles through Material surfaces rather than locally invented colors.

Dynamic wallpaper color remains an explicit opt-in. High-contrast mode takes priority over dynamic color so that accessibility intent is not diluted.

## Typography, Arabic, and RTL

Arabic reading, Quranic text, Hadith text, date strings, and time numerals use the existing specialized typography rather than raw screen-level sizes. Layouts use directional Material icons and start/end padding rather than manual mirroring. Mixed Arabic and Latin metadata remains secondary to the reading content and is truncated or wrapped intentionally rather than forced into narrow labels.

## Motion and state feedback

Motion is purposeful and short. Expand/collapse, selection, and state changes may use the shared motion durations; religious text itself is not animated for ornament. When **Reduce animations** is enabled in Settings, consumers of `LocalMuslimMotionPreferences` receive zero-duration values. Loading states expose contextual progress when available, and error states explain the next action without exposing paths, stack traces, or platform implementation terms.

## Surface-specific rules

| Surface | Design intent | Contract preserved |
|---|---|---|
| Prayer Home | Prominent next-prayer card with calm countdown, schedule hierarchy, location context, and direct per-prayer customization. | Calculation, Hijri adjustment, alert persistence, and the in-place customization dialog. |
| Location | Contextual GPS action, clear manual coordinate fallback, and recoverable error feedback. | Fine/coarse permission flow, coordinate IANA resolution, and alarm/widget rescheduling. |
| Hadith | Scholarly collection identity, real-book imagery with provenance, progress during local preparation, chapter index, and Paging. | Selected-book-only bounded import and local search. |
| More | Sectioned discovery rather than an icon grid, honoring hidden/reordered sections. | Existing feature routes and user-managed section order. |
| Settings | Grouped choices, explanations, and single expanded section. | Persisted preferences, update actions, permissions, and downstream routes. |
| Wear OS | Glanceable prayer/tasbih information following Wear Material conventions, rather than a compressed phone layout. | Paired-phone snapshot and tasbih increment boundaries. |

## Verification discipline

The static `scripts/verify_design_system_adoption.py` guard verifies central theme binding and adoption in the principal phone journeys. CI combines it with accessibility, visual identity, lifecycle, content, location, Hadith paging, responsive customization, unit, lint, Detekt, and emulator gates. A local Android SDK is required for device compilation; CI remains the build and emulator authority when that SDK is not installed in the workspace.
