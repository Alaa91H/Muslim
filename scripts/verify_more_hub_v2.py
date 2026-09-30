#!/usr/bin/env python3
"""Guard UX11 More hub hierarchy, customization and route parity."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MORE = ROOT / "app/src/main/java/org/muslim/app/ui/MoreScreen.kt"
ORDER = ROOT / "app/src/main/java/org/muslim/app/ui/MoreOrderScreen.kt"
APP = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    more = MORE.read_text(encoding="utf-8")
    order = ORDER.read_text(encoding="utf-8")
    app = APP.read_text(encoding="utf-8")

    for component in (
        "MuslimScreen(",
        "MuslimTopBar(",
        "MuslimSearchBar(",
        "MuslimExpandableSection(",
        "MuslimSettingsItem(",
    ):
        require(component in more, f"More hub must use shared V2 component: {component}")

    require(
        "TopAppBar(" not in more
        and "MuslimAppScaffold(" not in more
        and "IslamicListItem(" not in more,
        "More hub must not regress to its legacy local shell/list pattern",
    )
    require(
        "sectionOrder" in more
        and "hiddenSections" in more
        and "DEFAULT_MORE_SECTION_ORDER" in more,
        "persisted More section ordering/hiding contract must remain represented",
    )
    for section_id in (
        "MORE_SECTION_WORSHIP",
        "MORE_SECTION_KNOWLEDGE",
        "MORE_SECTION_TOOLS",
        "MORE_SECTION_APP",
    ):
        require(section_id in more, f"More hub section missing: {section_id}")

    for callback in (
        "onOpenSettings",
        "onOpenHadith",
        "onOpenAdhkar",
        "onOpenTasbih",
        "onOpenRamadan",
        "onOpenHabits",
        "onOpenZakat",
        "onOpenIslamicFinance",
        "onOpenLearn",
        "onOpenReference",
        "onOpenIslamicHistory",
        "onOpenScholarLibrary",
        "onOpenAccessibility",
        "onOpenDownloads",
        "onOpenFamily",
        "onOpenFuneralWill",
        "onOpenNoorani",
        "onOpenTraveler",
        "onOpenMoreOrder",
    ):
        require(callback in more, f"More hub destination callback missing: {callback}")

    require(
        "if (showRamadanShortcut)" in more,
        "seasonal Ramadan shortcut behavior must remain explicit",
    )
    require(
        "searchQuery.isNotBlank()" in more,
        "active search must expand matching sections instead of hiding matches",
    )

    for component in (
        "MuslimScreen(",
        "MuslimTopBar(",
        "MuslimInlineMessage(",
        "IslamicSecondaryButton(",
    ):
        require(component in order, f"More customization must use shared V2 component: {component}")

    for capability in (
        "detectDragGesturesAfterLongPress",
        "viewModel.setSectionHidden",
        "viewModel::setOrder",
        "viewModel::reset",
        "Switch(",
        "MoreReorderState",
    ):
        require(capability in order, f"More customization capability missing: {capability}")

    require(
        "TopAppBar(" not in order and "OutlinedButton(" not in order,
        "More customization must not regress to legacy Material controls",
    )
    require(
        "onOpenMoreOrder = { navController.navigate(MORE_ORDER_ROUTE) }" in app,
        "More hub must expose direct navigation to its customization screen",
    )
    require(
        "composable(MORE_ORDER_ROUTE)" in app,
        "More customization route must remain registered",
    )

    print("More hub UX11 contract verified.")


if __name__ == "__main__":
    main()
