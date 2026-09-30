#!/usr/bin/env python3
"""Guard the UX09 Quran downloads/bookmarks simplification contract."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOWNLOADS = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranDownloadsScreen.kt"
CONFIG = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranDownloadConfigurationV2.kt"
BOOKMARKS = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/BookmarksScreen.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    downloads = DOWNLOADS.read_text(encoding="utf-8")
    config = CONFIG.read_text(encoding="utf-8")
    bookmarks = BOOKMARKS.read_text(encoding="utf-8")

    require(
        "MuslimAdaptiveScreen(" in downloads and "MuslimTopBar(" in downloads,
        "downloads hub must use the shared adaptive screen shell",
    )
    require(
        "DownloadConfigurationPanel(" in downloads,
        "dense download setup controls must stay extracted from the hub",
    )
    require(
        downloads.count("MuslimExpandableSection(") >= 2,
        "coverage and downloaded-library details must use progressive disclosure",
    )
    require(
        "MuslimOverflowMenu(" in downloads and "MuslimMenuAction(" in downloads,
        "destructive library actions must use contextual overflow menus",
    )
    require(
        "TopAppBar(" not in downloads and "FilterChip(" not in downloads,
        "UX09 must not regress to raw top bars or scattered filter chips",
    )

    for needle in (
        "viewModel.pause(task.id)",
        "viewModel.resume(task.id)",
        "viewModel.cancel(task.id)",
        "viewModel.deleteSurah(surahNumber)",
        "viewModel.deleteReciter()",
    ):
        require(needle in downloads, f"download capability missing after UX09: {needle}")

    require(
        "MuslimSection(" in config
        and "MuslimSegmentedControl(" in config
        and "MuslimExpandableSection(" in config,
        "new-download configuration must use shared V2 hierarchy controls",
    )
    for scope in ("DownloadScope.Ayah", "DownloadScope.Surah", "DownloadScope.FullQuran"):
        require(scope in config, f"download scope missing after UX09: {scope}")
    for action in (
        "onNightOnlyChanged",
        "onNightWindowStartChanged",
        "onNightWindowEndChanged",
        "onStartDownload",
    ):
        require(action in config, f"download configuration action missing: {action}")

    require(
        "MuslimScreen(" in bookmarks and "MuslimTopBar(" in bookmarks,
        "bookmarks must use the shared V2 screen shell",
    )
    require(
        "BookmarkCard(" in bookmarks and "onOpenAyah(" in bookmarks,
        "bookmark navigation must remain available",
    )
    require(
        "TopAppBar(" not in bookmarks,
        "bookmarks must not regress to a raw local top app bar",
    )

    print("Quran library UX09 contract verified.")


if __name__ == "__main__":
    main()
