#!/usr/bin/env python3
"""Static checks for the Islamic economy and finance feature."""

from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
FINANCE_RESOURCES = (
    ROOT / "feature/feature-finance/src/main/res/values/strings.xml",
    ROOT / "feature/feature-finance/src/main/res/values-en/strings.xml",
)
FINANCE_SCREEN = ROOT / "feature/feature-finance/src/main/java/org/muslim/app/feature/finance/ui/IslamicFinanceScreen.kt"
REQUIRED_SOURCES = (
    ROOT / "core/core-notifications/src/main/java/org/muslim/app/core/notifications/NotificationCategory.kt",
    ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/NotificationSettingsScreen.kt",
    ROOT / "feature/feature-settings/src/main/java/org/muslim/app/feature/settings/NotificationSettingsViewModel.kt",
    ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt",
    ROOT / "app/src/main/java/org/muslim/app/MainActivity.kt",
)


def names(path: Path) -> set[str]:
    ET.parse(path)
    return set(re.findall(r'<string\s+name="([^"]+)"', path.read_text(encoding="utf-8")))


def main() -> int:
    for resource in FINANCE_RESOURCES:
        names(resource)
        print(f"XML valid: {resource.relative_to(ROOT)}")

    used = set(re.findall(r"R\.string\.(finance_[A-Za-z0-9_]+)", FINANCE_SCREEN.read_text(encoding="utf-8")))
    missing = {resource.relative_to(ROOT): sorted(used - names(resource)) for resource in FINANCE_RESOURCES}
    missing = {path: values for path, values in missing.items() if values}
    if missing:
        for path, values in missing.items():
            print(f"Missing finance strings in {path}: {', '.join(values)}", file=sys.stderr)
        return 1

    checks = {
        REQUIRED_SOURCES[0]: "    Finance(",
        REQUIRED_SOURCES[1]: "NotificationCategory.Finance",
        REQUIRED_SOURCES[2]: "NotificationCategory.Finance",
        REQUIRED_SOURCES[3]: "ISLAMIC_FINANCE_ROUTE",
        REQUIRED_SOURCES[4]: 'data.startsWith("muslim://finance")',
    }
    failed = [path.relative_to(ROOT) for path, needle in checks.items() if needle not in path.read_text(encoding="utf-8")]
    if failed:
        print(f"Missing finance integration in: {', '.join(map(str, failed))}", file=sys.stderr)
        return 1

    print("Islamic finance resources and integration references are complete.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
