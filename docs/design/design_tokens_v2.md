# UI/UX V2 Design Tokens

Date: 2026-09-29  
Phase: UX02

## Purpose

UI/UX V2 extends the existing Modern Islamic Minimalism token layer instead of replacing it. The goal is to stop feature screens from inventing repeated layout and typography decisions while keeping specialized geometry feature-owned.

## Spacing

`IslamicSpacing` remains the canonical spacing scale.

New semantic aliases:
- `HeroToContent` — spacing between a hero region and ordinary content.
- `ControlGap` — spacing between closely related controls.

Raw dp values remain allowed for true geometry such as Quran glyph/highlight measurements, compass drawing, progress strokes, map geometry and other feature-specific rendering.

## Adaptive layout

`IslamicLayout` is the single source of truth for common width classes:

- Compact: 0–599dp
- Medium: 600–839dp
- Expanded: 840dp+

Shared content bounds:
- readable/task content: 760dp
- wide/multi-pane content: 1200dp

Feature screens must not introduce competing phone/tablet breakpoints for ordinary page layout.

## Strokes

`IslamicStroke` defines:
- Standard: 1dp
- Selected: 2dp
- Emphasis: 3dp

## Content emphasis

`IslamicContentAlpha` defines:
- Full: 1.00
- Secondary: 0.78
- Muted: 0.60
- Disabled: 0.38
- SubtleDecoration: 0.12

Opacity is a hierarchy aid only; state meaning must never rely on opacity or color alone.

## Semantic typography

Material typography remains the application baseline. `MuslimSemanticTypography` gives repeated responsibilities stable names:

- ReligiousDisplay
- Hero
- ScreenTitle
- SectionTitle
- ItemTitle
- Body
- Supporting
- Metadata
- QuranArabic
- QuranTranslation
- QuranTafsir
- QuranMetadata

Reader user preferences may scale/adjust these base roles. The semantic token does not replace user-controlled Quran font sizing.

## Font asset policy

`MuslimFonts` continues to use platform default families until separately licensed font assets are reviewed and deliberately bundled. UI/UX V2 does not introduce unreviewed font binaries as part of a visual refactor.

## Migration contract

1. New ordinary screen spacing uses `IslamicSpacing`.
2. New responsive page behavior uses `IslamicLayout`.
3. Shared surfaces use `IslamicStroke` rather than unrelated border literals where semantics match.
4. Repeated text responsibilities use semantic typography roles.
5. Specialized drawing/reader geometry remains feature-owned.
6. Existing behavior and persisted settings are unchanged by UX02.

## Verification

`IslamicDesignSystemTest` covers:
- palette identity,
- shape/motion/touch targets,
- adaptive breakpoint boundaries,
- readable/wide content bounds,
- semantic Quran typography,
- semantic emphasis ordering.

`verify_design_system_adoption.py` requires the shared app content frame to consume the central readable-width token.
