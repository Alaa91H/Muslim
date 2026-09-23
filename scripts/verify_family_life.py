#!/usr/bin/env python3
"""Static verification for the Family Life feature module and content contract."""

from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
MODULE = ROOT / "feature/feature-family-life"
SCREEN = MODULE / "src/main/java/org/muslim/app/feature/family/ui/FamilyLifeScreen.kt"
CONTENT = MODULE / "src/main/java/org/muslim/app/feature/family/domain/FamilyLifeContent.kt"
AR_STRINGS = MODULE / "src/main/res/values/strings.xml"
EN_STRINGS = MODULE / "src/main/res/values-en/strings.xml"
SETTINGS = ROOT / "settings.gradle.kts"
APP_BUILD = ROOT / "app/build.gradle.kts"
APP_NAV = ROOT / "app/src/main/java/org/muslim/app/ui/MuslimApp.kt"
OLD_ROOT = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn"


def string_names(path: Path) -> set[str]:
    ET.parse(path)
    return set(re.findall(r'<string\s+name="([^"]+)"', path.read_text(encoding="utf-8")))


def fail(message: str) -> int:
    print(message, file=sys.stderr)
    return 1


def main() -> int:
    required = (
        MODULE / "build.gradle.kts",
        MODULE / "src/main/AndroidManifest.xml",
        SCREEN,
        CONTENT,
        MODULE / "src/main/java/org/muslim/app/feature/family/ui/FamilyLifeViewModel.kt",
        MODULE / "src/main/java/org/muslim/app/feature/family/domain/AqiqahCalculator.kt",
        MODULE / "src/main/java/org/muslim/app/feature/family/data/AqiqahPrefsRepository.kt",
        MODULE / "src/main/java/org/muslim/app/feature/family/data/AqiqahReminderScheduler.kt",
        MODULE / "src/main/java/org/muslim/app/feature/family/data/AqiqahReminderWorker.kt",
        AR_STRINGS,
        EN_STRINGS,
    )
    missing = [str(path.relative_to(ROOT)) for path in required if not path.is_file()]
    if missing:
        return fail("Missing family-life files: " + ", ".join(missing))

    settings = SETTINGS.read_text(encoding="utf-8")
    app_build = APP_BUILD.read_text(encoding="utf-8")
    app_nav = APP_NAV.read_text(encoding="utf-8")
    if 'include(":feature:feature-family-life")' not in settings:
        return fail("feature-family-life is not registered in settings.gradle.kts")
    if 'implementation(project(":feature:feature-family-life"))' not in app_build:
        return fail("app does not depend on feature-family-life")
    if "org.muslim.app.feature.family.ui.FamilyLifeScreen" not in app_nav:
        return fail("app navigation does not use the extracted FamilyLifeScreen")

    obsolete = (
        OLD_ROOT / "ui/FamilyLifeScreen.kt",
        OLD_ROOT / "ui/FamilyLifeViewModel.kt",
        OLD_ROOT / "domain/FamilyLifeContent.kt",
        OLD_ROOT / "domain/AqiqahCalculator.kt",
        OLD_ROOT / "data/AqiqahPrefsRepository.kt",
        OLD_ROOT / "data/AqiqahReminderScheduler.kt",
        OLD_ROOT / "data/AqiqahReminderWorker.kt",
    )
    leftovers = [str(path.relative_to(ROOT)) for path in obsolete if path.exists()]
    if leftovers:
        return fail("Migrated family sources still exist in feature-learn: " + ", ".join(leftovers))

    arabic = string_names(AR_STRINGS)
    english = string_names(EN_STRINGS)
    screen_text = SCREEN.read_text(encoding="utf-8")
    worker_text = (MODULE / "src/main/java/org/muslim/app/feature/family/data/AqiqahReminderWorker.kt").read_text(encoding="utf-8")
    used = set(re.findall(r"R\.string\.([A-Za-z0-9_]+)", screen_text + "\n" + worker_text))
    missing_ar = sorted(used - arabic)
    missing_en = sorted(used - english)
    if missing_ar or missing_en:
        if missing_ar:
            print("Missing Arabic family strings: " + ", ".join(missing_ar), file=sys.stderr)
        if missing_en:
            print("Missing English family strings: " + ", ".join(missing_en), file=sys.stderr)
        return 1

    content = CONTENT.read_text(encoding="utf-8")
    article_ids = re.findall(r'FamilyGuideArticle\(\s*id\s*=\s*"([^"]+)"', content)
    if len(article_ids) != len(set(article_ids)):
        return fail("Duplicate family article IDs detected")
    required_articles = {"engagement", "nikah", "marital_rights", "parenting"}
    if not required_articles.issubset(article_ids):
        return fail("Core family guide articles are missing")

    audio_urls = re.findall(r'https://everyayah\.com/data/[^"\s]+\.mp3', content)
    if len(audio_urls) < 3:
        return fail("Expected curated EveryAyah audio references are missing")
    if 'preferencesDataStore(name = "family_life_prefs")' not in (
        MODULE / "src/main/java/org/muslim/app/feature/family/data/AqiqahPrefsRepository.kt"
    ).read_text(encoding="utf-8"):
        return fail("Family DataStore name changed; this would break preference migration")

    print("Family Life module contract verified.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
