# Modern Islamic Minimalism

**Modern Islamic Minimalism** is the app-wide visual language introduced for the Muslim Android application. It is intentionally calm, readable, and contemporary: deep green and warm ivory establish the primary atmosphere, while antique gold is reserved for meaningful accents rather than dominant decoration. The implementation refines the existing experience; it does not rebuild navigation, replace established Quran-reader workflows, or alter Quran content.

> The design system treats readability and spiritual focus as primary product requirements. Ornament supports hierarchy and orientation; it is never permitted to compete with Quran text, recitation controls, or assistive technology.

## Design principles

The visual identity uses restrained surfaces, clear type hierarchy, modest elevation, and vector-based Islamic geometry. Cards and buttons feel rounded and approachable without exaggerated shadows. Icons retain the established Material icon set so their interaction meaning remains familiar, while colour and outlines bring them into the shared identity.

| Principle | Product decision | Guardrail |
|---|---|---|
| Calm hierarchy | Green establishes primary actions and active states. | Do not introduce bright gold, glow, or large decorative panels. |
| Focused Quran reading | Reader paper, night, and light modes use dedicated Material schemes. | Never place a background ornament behind the Quran body text. |
| Subtle heritage | Geometry, stars, arabesque, and Mushaf dividers are reusable vector drawables. | Do not use Unicode ornamental glyphs as interface decoration. |
| Stable interaction | Existing search, bookmarking, reading progress, audio, and RTL workflows remain in place. | Visual work must not modify Quran text, ayah data, or recitation logic. |
| Accessible restraint | Colour is paired with labels, outlines, state changes, and 48dp touch targets. | Do not rely on colour alone to communicate a state. |

## Central tokens

The source of truth is `core:core-design-system`. `IslamicPalette` holds semantic raw palette values, while `MuslimLightColors`, `MuslimDarkColors`, and `MuslimSepiaColors` expose Material 3 schemes. New UI should use `MaterialTheme.colorScheme` roles rather than hard-coded colour literals.

| Context | Background and surface | Primary | Accent usage |
|---|---|---|---|
| Dark | `#0D1110`, `#121816`, `#151C19`, `#1C2923` | `#527A68` | Antique gold `#B49A62` is a tertiary detail, not a surface. |
| Light | `#F5F1E7`, `#FAF8F1` | Muted green Material role | Antique gold is reserved for metadata, dividers, and selected details. |
| Reader Sepia | `#E8DEC7`, `#EFE6D3` | Reader green role | Gold remains a restrained secondary accent. |

`IslamicSpacing`, `IslamicRadius`, `IslamicElevation`, and `IslamicMotion` establish the shared layout language. The default card radius is 20dp, large containers are 24dp, elevation is kept low, standard motion is 150ms to 250ms, and the minimum interactive target remains 48dp. `IslamicShapes` applies the radius system to the Material theme.

## Ornament catalogue and opacity

All ornaments are Android vector XML resources in `core:core-ui`. `IslamicOrnament` maps a semantic name to its resource and `IslamicOrnamentImage` renders it consistently with a tint and explicitly supplied alpha. This provides a reusable vector equivalent of an SVG-style design asset without text symbols or font-dependent decoration.

| Semantic ornament | Intended placement | Opacity range |
|---|---|---|
| `Geometric8` / `Geometric12` | Quiet page or card background, away from primary text. | 3–6% background |
| `Star8` / `Star12` | Small section marker or active visual detail. | 6–10% section; up to 15% on a dark active state |
| `Arabesque` | Narrow separator above a compact control area. | 6–10% section |
| `MushafDivider` | Basmala and compact content transitions. | 6–10% section |
| `SurahHeader` | Surah or Quran-list header band. | 6–10% section |
| `Corner` | Reserved for small non-reader card accents. | 3–6% background |

The token names `LightBackground`, `LightSection`, and `LightActive` identify the intended visual intensity. They are deliberately low enough to remain non-essential; assistive users must receive the same information through text, semantic labels, and control state.

## Quran-reader quietness

The Quran reader preserves its existing light, Sepia, and night modes, Arabic-reading controls, per-ayah bookmarking, last-read progress, download status, and recitation playback. Sepia now resolves from the shared `MuslimSepiaColors` scheme, while the night reader retains a calm deep-green palette. Quran text continues to use the selected Arabic reading font and is not edited by this visual change.

Surah headers and Basmala transitions use slim vector bands that sit outside the Quran body. Current-ayah and audio-follow state use a modest green playback treatment and a secondary gold marker where appropriate. The bottom recitation player uses a stable reader surface, a thin vector separator, and Material roles for contrast. No ornament is rendered as a repeating background beneath verses.

## Reusable components and preview

`IslamicCard`, `IslamicPrimaryButton`, and `IslamicSecondaryButton` provide safe defaults for restrained borders, Material semantic colours, and shared shape tokens. They should be preferred for new non-specialised surfaces. Existing Material components continue to inherit the global shape and palette configuration, so migrations can be incremental and avoid UX regressions.

`IslamicDesignShowcase` is an internal Compose preview rather than a production destination. It renders light, dark, and Mushaf-paper examples of a Surah header, ayah marker, card, buttons, and dividers. It helps developers review token coherence without exposing a developer-only screen to end users or changing app navigation.

## Accessibility and responsive behavior

The system supports small phones, large phones, tablets, and landscape through responsive Compose layout primitives rather than fixed screen dimensions. Decorative images use layout-only modifiers and do not receive interaction handlers. Cards and controls retain at least the shared touch-target guidance, while text wraps and grows with user font scaling. RTL remains controlled by the existing Compose layout direction and Arabic typography paths.

| Accessibility requirement | Implementation approach |
|---|---|
| Contrast and high readability | Use Material `on*` roles against their paired surfaces; preserve outlines on calm cards and controls. |
| Dynamic colour choice | The app’s Islamic identity is the new-install default; users’ existing preference persists, and wallpaper colour remains an explicit opt-in. |
| Screen readers | Decorative imagery conveys no unique information, while controls retain descriptions and textual labels. |
| Large text and locale direction | Avoid narrow fixed-width text containers; preserve existing Arabic font, RTL, and reading accessibility settings. |
| Motion sensitivity | Playback feedback is a short, low-amplitude alpha transition; no looping decorative animation is introduced. |

## Android launcher and notification identity

The system-facing application identity is intentionally split into two assets. The approved full-colour geometric Islamic mark is supplied through the `v2027` adaptive launcher and round-launcher resources. A separate `ic_muslim_status_bar_v2027` vector is used for every Android notification small icon. It is monochrome by design because the operating system applies the final status-bar tint and may render it at very small sizes.

| Surface | Asset class | Design rule |
|---|---|---|
| App launcher and themed launcher | Full-colour adaptive foreground, navy background, monochrome themed layer | Preserve the eight-point geometry, mihrab and crescent silhouette within adaptive-icon safe zones. |
| Status bar | Dedicated monochrome small-icon vector | Keep a strong, centred silhouette; do not use a raster launcher image, a multicolour asset, or text. |
| Adhan, countdown and Quran playback cards | Android-owned notification and media templates | Do not provide a custom large app image merely to force branding; the current application identity may be rendered by Android itself. |

The repair uses fresh resource names and fresh current notification IDs, then cancels cards known to be retained from earlier versions. This is a migration mechanism rather than a new visual style. The exact migration IDs, test coverage, user upgrade note, and platform limits are recorded in [`qa/notification_identity_repair.md`](qa/notification_identity_repair.md).

## Verification

Run the static verifier after making design-system changes:

```bash
python3 scripts/verify_islamic_visual_identity.py
```

The verifier confirms the required palette values, shape and motion tokens, central Sepia scheme, vector assets, reader integrations, preview, documentation, and the absence of the former Unicode ornament constant or bright-gold `#FFD700` equivalent in the primary visual sources. It complements Kotlin compilation, Detekt, Android lint, unit tests, and the repository CI workflow; it is not a substitute for screenshot or device-based visual review.
