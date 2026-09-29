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

    print("Quran Reader V2 line-aware highlight contract verified.")


if __name__ == "__main__":
    main()
