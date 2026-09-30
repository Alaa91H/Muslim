#!/usr/bin/env python3
"""Guard UX12 Adhkar/Tasbih simplification and feature parity."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ADHKAR = ROOT / "feature/feature-adhkar/src/main/java/org/muslim/app/feature/adhkar/ui/AdhkarScreen.kt"
ADHKAR_SETTINGS = ROOT / "feature/feature-adhkar/src/main/java/org/muslim/app/feature/adhkar/ui/AdhkarSettingsScreen.kt"
ADHKAR_CUSTOMIZE = ROOT / "feature/feature-adhkar/src/main/java/org/muslim/app/feature/adhkar/ui/AdhkarCustomizeScreen.kt"
TASBIH = ROOT / "feature/feature-tasbih/src/main/java/org/muslim/app/feature/tasbih/ui/TasbihScreen.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def require_in(source: str, snippets: tuple[str, ...], label: str) -> None:
    for snippet in snippets:
        require(snippet in source, f"{label} contract missing: {snippet}")


def main() -> None:
    adhkar = ADHKAR.read_text(encoding="utf-8")
    settings = ADHKAR_SETTINGS.read_text(encoding="utf-8")
    customize = ADHKAR_CUSTOMIZE.read_text(encoding="utf-8")
    tasbih = TASBIH.read_text(encoding="utf-8")

    require_in(
        adhkar,
        (
            "MuslimScreen(",
            "MuslimTopBar(",
            "MuslimSearchBar(",
            "MuslimFilterBar(",
            "MuslimSettingsItem(",
            "MuslimOverflowMenu(",
            "MuslimProgressHeader(",
            "AdhkarRoute.Reader",
            "AdhkarRoute.Settings",
            "AdhkarRoute.Customize",
            "viewModel::setSearchQuery",
            "viewModel::setFavoritesOnly",
            "viewModel::setMorningEveningReminderEnabled",
            "viewModel.toggleFavorite",
            "viewModel.increment",
            "viewModel.reset",
            "viewModel.toggleSpeech",
            "R.string.adhkar_copy",
            "R.string.adhkar_share",
        ),
        "Adhkar V2",
    )
    require(
        "DropdownMenu(" not in adhkar and "DropdownMenuItem(" not in adhkar,
        "Adhkar card actions must not regress to local popup menus",
    )
    require_in(
        settings,
        (
            "MuslimScreen(",
            "MuslimTopBar(",
            "MuslimExpandableSection(",
            "onOpenCustomize",
        ),
        "Adhkar settings V2",
    )
    require_in(
        customize,
        (
            "MuslimScreen(",
            "MuslimTopBar(",
            "LazyColumn(",
        ),
        "Adhkar customize V2",
    )

    require_in(
        tasbih,
        (
            "MuslimScreen(",
            "MuslimTopBar(",
            "TasbihCounterSection(",
            "MuslimExpandableSection(",
            "MuslimFilterBar(",
            "MuslimOverflowMenu(",
            "TasbihSessionMode.Free",
            "TasbihSessionMode.Target",
            "TasbihSessionMode.Rounds",
            "viewModel::increment",
            "viewModel::decrement",
            "viewModel::reset",
            "viewModel::resetAll",
            "viewModel::setTarget",
            "viewModel::setSessionMode",
            "viewModel::setRoundsGoal",
            "viewModel::setTargetSoundEnabled",
            "RecentTasbihSessions(",
        ),
        "Tasbih V2",
    )
    require(
        "DropdownMenu(" not in tasbih and "DropdownMenuItem(" not in tasbih,
        "Tasbih secondary actions must stay on shared V2 disclosure patterns",
    )

    print("Adhkar and Tasbih UX12 contract verified.")


if __name__ == "__main__":
    main()
