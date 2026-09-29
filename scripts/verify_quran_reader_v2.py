#!/usr/bin/env python3
"""Guard the Quran Reader V2 highlight contract."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
READER = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt"
RENDERER = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranTextHighlightRenderer.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    reader = READER.read_text(encoding="utf-8")
    renderer = RENDERER.read_text(encoding="utf-8")

    require(
        "SpanStyle(background =" not in reader and "SpanStyle(background=" not in reader,
        "Quran ayah states must not use hard SpanStyle background blocks",
    )
    require(
        "drawQuranTextHighlights(" in reader,
        "reader must route all ayah highlighting through the line-aware renderer",
    )
    require(
        "QuranHighlightKind.Tapped" in reader
        and "QuranHighlightKind.Playback" in reader
        and "QuranHighlightKind.Opened" in reader
        and "QuranHighlightKind.Selected" in reader,
        "all reader highlight states must use the shared line-aware path",
    )
    require(
        "insetHighlightVerticalBounds(" in renderer,
        "renderer must keep per-line highlights vertically inset",
    )
    require(
        "MuslimActionSheet(" in reader and "showAyahActions = true" in reader,
        "tapping an ayah must open the shared contextual action sheet",
    )
    require(
        "ReaderSettingsSheet(" in reader
        and "MuslimBottomSheet(" in reader
        and "showReaderSettings = true" in reader,
        "secondary reader settings must use the shared bottom sheet",
    )
    require(
        "RecitationSettingsSheet(" in reader
        and "quran_playback_settings" in reader,
        "advanced recitation controls must move out of the permanent player row",
    )
    require(
        "var repeatMenu by remember" not in reader
        and "var rangeMenu by remember" not in reader
        and "var reciterMenu by remember" not in reader,
        "legacy reciter/repeat/range popup menus must not return to the permanent player",
    )
    require(
        "onClick = { viewModel.setReaderTheme(theme.next) }" not in reader,
        "reader theme cycling must not remain a permanently exposed top-bar action",
    )
    require(
        'id = "play"' in reader
        and 'id = "bookmark"' in reader
        and 'id = "supplement"' in reader
        and 'id = "share"' in reader
        and 'id = "copy"' in reader,
        "reader ayah sheet must preserve core contextual actions",
    )
    require(
        "IconButton(\n                        onClick = viewModel::toggleBookmark" not in reader,
        "bookmarking must not return as a permanently exposed top-bar action",
    )
    require(
        "getBoundingBox(offset)" in renderer,
        "renderer must derive visual horizontal edges from laid-out glyph boxes",
    )
    require(
        "getLineStart(line)" in renderer and "getLineEnd(line, visibleEnd = true)" in renderer,
        "renderer must split wrapped ayat into actual line-local segments",
    )
    require(
        "lineTop + safeInset" in renderer and "lineBottom - safeInset" in renderer,
        "renderer must shrink rather than expand line backgrounds",
    )

    marker_literal = 'append("\\uFD3F${ayah.numberInSurah.toString()}\\uFD3E")'
    marker_index = reader.find(marker_literal)
    require(marker_index >= 0, "ayah marker ornament append contract is missing")
    marker_context = reader[max(0, marker_index - 900): marker_index + len(marker_literal) + 200]
    require(
        "color = scheme.tertiary" in marker_context,
        "ayah-number ornament must use the stable gold/bronze tertiary tone",
    )
    require(
        "selectedAyahGlobal -> scheme.primary" not in marker_context
        and "playingAyahGlobal -> scheme.primary" not in marker_context,
        "selection/playback must not recolor an ayah-number ornament green",
    )

    print("Quran Reader V2 line-aware highlight contract verified.")


if __name__ == "__main__":
    main()
