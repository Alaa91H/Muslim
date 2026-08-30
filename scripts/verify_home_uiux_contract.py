"""Protect the Home screen's accessibility and responsive action layout."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REQUIRED = {
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeScreen.kt": [
        "Role.Button",
        "home_location_action",
        "home_next_prayer_accessibility",
        "modifier = Modifier.weight(1f)",
        ".semantics { contentDescription = nextPrayerDescription }",
    ],
    "feature/feature-prayer-times/src/main/res/values/strings.xml": [
        'name="home_location_action"',
        'name="home_next_prayer_accessibility"',
    ],
    "feature/feature-prayer-times/src/main/res/values-en/strings.xml": [
        'name="home_location_action"',
        'name="home_next_prayer_accessibility"',
    ],
}


def main() -> int:
    problems: list[str] = []
    for relative_path, snippets in REQUIRED.items():
        path = ROOT / relative_path
        if not path.exists():
            problems.append(f"{relative_path}: file is missing")
            continue
        content = path.read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet not in content:
                problems.append(f"{relative_path}: missing {snippet!r}")
    if problems:
        print("Home UI/UX contract checks failed:")
        print("\n".join(f"- {problem}" for problem in problems))
        return 1
    print("Home UI/UX contract checks passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
