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
ADVANCED_CONTENT = MODULE / "src/main/java/org/muslim/app/feature/family/domain/FamilyAdvancedContent.kt"
PARENTING_CONTENT = MODULE / "src/main/java/org/muslim/app/feature/family/domain/FamilyParentingContent.kt"
DAILY_KINSHIP_CONTENT = MODULE / "src/main/java/org/muslim/app/feature/family/domain/FamilyDailyKinshipContent.kt"
UTILITY_CONTENT = MODULE / "src/main/java/org/muslim/app/feature/family/domain/FamilyUtilityContent.kt"
LIBRARY_PREFS = MODULE / "src/main/java/org/muslim/app/feature/family/data/FamilyLibraryPrefsRepository.kt"
NAMES_EXPANSION = MODULE / "src/main/java/org/muslim/app/feature/family/domain/FamilyNamesExpansion.kt"
GUIDE_HUB = MODULE / "src/main/java/org/muslim/app/feature/family/ui/FamilyGuideHub.kt"
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
        ADVANCED_CONTENT,
        PARENTING_CONTENT,
        DAILY_KINSHIP_CONTENT,
        UTILITY_CONTENT,
        LIBRARY_PREFS,
        NAMES_EXPANSION,
        GUIDE_HUB,
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
    guide_text = GUIDE_HUB.read_text(encoding="utf-8")
    worker_text = (MODULE / "src/main/java/org/muslim/app/feature/family/data/AqiqahReminderWorker.kt").read_text(encoding="utf-8")
    used = set(re.findall(r"R\.string\.([A-Za-z0-9_]+)", screen_text + "\n" + guide_text + "\n" + worker_text))
    missing_ar = sorted(used - arabic)
    missing_en = sorted(used - english)
    if missing_ar or missing_en:
        if missing_ar:
            print("Missing Arabic family strings: " + ", ".join(missing_ar), file=sys.stderr)
        if missing_en:
            print("Missing English family strings: " + ", ".join(missing_en), file=sys.stderr)
        return 1

    content = CONTENT.read_text(encoding="utf-8")
    advanced_content = ADVANCED_CONTENT.read_text(encoding="utf-8")
    parenting_content = PARENTING_CONTENT.read_text(encoding="utf-8")
    daily_kinship_content = DAILY_KINSHIP_CONTENT.read_text(encoding="utf-8")
    utility_content = UTILITY_CONTENT.read_text(encoding="utf-8")
    library_prefs = LIBRARY_PREFS.read_text(encoding="utf-8")
    names_expansion = NAMES_EXPANSION.read_text(encoding="utf-8")
    article_ids = re.findall(
        r'FamilyGuideArticle\(\s*id\s*=\s*"([^"]+)"',
        content + "\n" + advanced_content + "\n" + parenting_content + "\n" + daily_kinship_content,
    )
    if len(article_ids) != len(set(article_ids)):
        return fail("Duplicate family article IDs detected")
    required_articles = {
        "engagement",
        "choosing_spouse",
        "premarital_conversations",
        "nikah",
        "mahr_financial_agreements",
        "marriage_documentation",
        "marital_rights",
        "marital_communication",
        "household_finances",
        "parenting",
        "conflict_resolution",
        "mediation_reconciliation",
        "abuse_safety",
        "separation_divorce",
        "divorce_general_principles",
        "khul_annulment",
        "pregnancy_preparation",
        "postpartum_family_support",
        "newborn_sunnahs_evidence",
        "breastfeeding_child_care",
        "aqiqah_complete_guide",
        "choosing_child_name",
        "parenting_early_years",
        "parenting_school_age",
        "parenting_teens",
        "children_prayer_quran",
        "discipline_without_harm",
        "child_digital_safety",
        "body_privacy_safeguarding",
        "sibling_fairness",
        "children_faith_questions",
        "parents_kindness_boundaries",
        "elder_parent_care",
        "supporting_parents_financially",
        "maintaining_kinship",
        "harmful_relatives_boundaries",
        "inlaws_household_boundaries",
        "family_reconciliation_after_distance",
        "household_worship_routine",
        "family_shura_decisions",
        "family_budget_moderation",
        "household_privacy_devices",
        "family_work_study_balance",
        "guests_neighbours_home",
        "family_healthcare_planning",
        "family_weekly_meeting",
    }
    if not required_articles.issubset(article_ids):
        return fail("Expanded family guide articles are missing")
    if len(article_ids) < 49:
        return fail(f"Family guide unexpectedly small: {len(article_ids)} articles")
    expanded_names = re.findall(r'(?:prophet|arabicBoy|arabicGirl)\("([^"]+)"', names_expansion)
    if len(expanded_names) < 60:
        return fail(f"Baby-name expansion unexpectedly small: {len(expanded_names)} names")
    ruqyah_duas = re.findall(r'RuqyahSupplication\(\s*id\s*=\s*"([^"]+)"', parenting_content)
    if len(ruqyah_duas) < 5:
        return fail("Ruqyah supplication catalogue is incomplete")
    checklist_ids = re.findall(r'FamilyChecklist\(\s*id\s*=\s*"([^"]+)"', utility_content)
    if len(checklist_ids) < 5 or len(checklist_ids) != len(set(checklist_ids)):
        return fail("Family checklist catalogue is incomplete or has duplicate IDs")
    if 'preferencesDataStore(name = "family_library_prefs")' not in library_prefs:
        return fail("Family library preferences DataStore is missing or renamed")
    if "MAX_RECENT_ARTICLES = 20" not in library_prefs:
        return fail("Family reading history must remain bounded")
    required_ui = (
        "FamilyArticleDetailContent",
        "FamilyHubContent",
        "FamilySavedContent",
        "FamilyToolsContent",
    )
    if not all(symbol in screen_text + "\n" + guide_text for symbol in required_ui):
        return fail("Family Life UI is missing hub, saved library or checklist wiring")

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
