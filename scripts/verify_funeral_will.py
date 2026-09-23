#!/usr/bin/env python3
"""Static verification for the Funerals & Islamic Will feature resources."""

from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APP_RESOURCE_DIRS = (
    ROOT / "app/src/main/res/values",
    ROOT / "app/src/main/res/values-en",
)
FEATURE_RESOURCE_DIRS = (
    ROOT / "feature/feature-learn/src/main/res/values",
    ROOT / "feature/feature-learn/src/main/res/values-en",
)
SCREEN = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/FuneralWillScreen.kt"
DRAFT = ROOT / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/domain/WillDraft.kt"
DRAFT_REPOSITORY = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/WillDraftRepository.kt"
)
DRAFT_CRYPTO = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/WillDraftCrypto.kt"
)
PDF_EXPORTER = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/WillDraftPdfExporter.kt"
)
PRINT_ADAPTER = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/WillDraftPrintAdapter.kt"
)
DRAFT_AUTHENTICATOR = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/WillDraftAuthenticator.kt"
)
PROTECTION_UI = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/WillDraftProtectionUi.kt"
)
PROTECTION_SESSION = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/ui/WillDraftProtectionSession.kt"
)
APP_MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
BACKUP_RULES = ROOT / "app/src/main/res/xml/backup_rules.xml"
DATA_EXTRACTION_RULES = ROOT / "app/src/main/res/xml/data_extraction_rules.xml"
PREFERENCES = (
    ROOT
    / "feature/feature-learn/src/main/java/org/muslim/app/feature/learn/data/FuneralWillPreferencesRepository.kt"
)


def read_string_names(path: Path) -> set[str]:
    ET.parse(path)
    return set(re.findall(r'<string\s+name="([^"]+)"', path.read_text(encoding="utf-8")))


def read_resource_dir(directory: Path) -> set[str]:
    names: set[str] = set()
    for resource_file in sorted(directory.glob("*.xml")):
        file_names = read_string_names(resource_file)
        duplicates = sorted(names & file_names)
        if duplicates:
            print(
                f"Duplicate string keys in {directory.relative_to(ROOT)}: {', '.join(duplicates)}",
                file=sys.stderr,
            )
            raise ValueError("duplicate string resources")
        names.update(file_names)
        print(f"XML valid: {resource_file.relative_to(ROOT)}")
    return names


def main() -> int:
    try:
        app_arabic = read_resource_dir(APP_RESOURCE_DIRS[0])
        app_english = read_resource_dir(APP_RESOURCE_DIRS[1])
        feature_arabic = read_resource_dir(FEATURE_RESOURCE_DIRS[0])
        feature_english = read_resource_dir(FEATURE_RESOURCE_DIRS[1])
    except (ET.ParseError, ValueError) as error:
        print(f"Resource verification failed: {error}", file=sys.stderr)
        return 1

    ui_resource_text = "\n".join(
        path.read_text(encoding="utf-8")
        for path in (SCREEN, PROTECTION_UI, PROTECTION_SESSION)
    )
    used_names = set(
        re.findall(
            r"R\.string\.(funeral_will_[A-Za-z0-9_]+)",
            ui_resource_text,
        )
    )
    missing_arabic = sorted(used_names - feature_arabic)
    missing_english = sorted(used_names - feature_english)
    if missing_arabic or missing_english:
        if missing_arabic:
            print("Missing Arabic keys:", ", ".join(missing_arabic), file=sys.stderr)
        if missing_english:
            print("Missing English keys:", ", ".join(missing_english), file=sys.stderr)
        return 1

    more_keys = {"more_funeral_will", "more_funeral_will_desc"}
    missing_more = sorted((more_keys - app_arabic) | (more_keys - app_english))
    if missing_more:
        print("Missing More menu keys:", ", ".join(missing_more), file=sys.stderr)
        return 1

    preferences_text = PREFERENCES.read_text(encoding="utf-8")
    preference_contract = {
        "hide_draft_intro",
        "hide_legal_notice",
        "hide_privacy_notice",
        "intro_content_version",
        "protect_draft_with_device_auth",
    }
    missing_preferences = sorted(
        key for key in preference_contract if key not in preferences_text
    )
    if missing_preferences:
        print(
            "Missing persisted intro preferences:",
            ", ".join(missing_preferences),
            file=sys.stderr,
        )
        return 1

    screen_text = SCREEN.read_text(encoding="utf-8")
    protection_ui_text = PROTECTION_UI.read_text(encoding="utf-8")
    protection_session_text = PROTECTION_SESSION.read_text(encoding="utf-8")
    required_ui_contract = {
        "funeral_will_restore_intro_cards",
        "dismissDraftIntro",
        "dismissLegalNotice",
        "dismissPrivacyNotice",
        "willEducationSections",
        "quickActionSteps",
        "funeral_will_search",
        "searchGuideSections",
        "searchWillEducationSections",
        "funeral_will_encrypted_notice",
        "funeral_will_export_pdf",
        "funeral_will_print",
        "printWillDraft",
        "widthIn(max = 900.dp)",
        "ActivityResultContracts.CreateDocument",
        "rememberWillDraftProtectionSession",
        "WillDraftLockedContent",
    }
    missing_ui_contract = sorted(
        token for token in required_ui_contract if token not in screen_text
    )
    protection_ui_contract = {
        "funeral_will_protection_enable": protection_ui_text,
        "funeral_will_protection_lock_now": protection_ui_text,
        "WillDraftProtectionTestTags": protection_ui_text,
        "Lifecycle.Event.ON_STOP": protection_session_text,
    }
    missing_ui_contract += sorted(
        token
        for token, source in protection_ui_contract.items()
        if token not in source
    )
    if missing_ui_contract:
        print(
            "Missing funeral/will UI contract:",
            ", ".join(missing_ui_contract),
            file=sys.stderr,
        )
        return 1

    expanded_fields = {
        "documentLocation",
        "trustedContacts",
        "assetsAndAccounts",
        "entrustedProperty",
        "digitalAccessInstructions",
        "lastReviewDate",
    }
    draft_text = DRAFT.read_text(encoding="utf-8")
    repository_text = DRAFT_REPOSITORY.read_text(encoding="utf-8")
    missing_draft_fields = sorted(
        field
        for field in expanded_fields
        if field not in draft_text or field not in repository_text or field not in screen_text
    )
    if missing_draft_fields:
        print(
            "Missing expanded will-draft contract:",
            ", ".join(missing_draft_fields),
            file=sys.stderr,
        )
        return 1

    security_contract = {
        "will_encrypted_payload_v1": repository_text,
        "migrateLegacyIfNeeded": repository_text,
        "AndroidKeyStore": DRAFT_CRYPTO.read_text(encoding="utf-8"),
        "AES/GCM/NoPadding": DRAFT_CRYPTO.read_text(encoding="utf-8"),
        "PdfDocument": PDF_EXPORTER.read_text(encoding="utf-8"),
        "Private organisational copy": PDF_EXPORTER.read_text(encoding="utf-8"),
        "PrintDocumentAdapter": PRINT_ADAPTER.read_text(encoding="utf-8"),
        "PrintManager": PRINT_ADAPTER.read_text(encoding="utf-8"),
        "BiometricPrompt": DRAFT_AUTHENTICATOR.read_text(encoding="utf-8"),
        "DEVICE_CREDENTIAL": DRAFT_AUTHENTICATOR.read_text(encoding="utf-8"),
    }
    missing_security = sorted(
        token for token, source in security_contract.items() if token not in source
    )
    if missing_security:
        print(
            "Missing encrypted-storage/PDF contract:",
            ", ".join(missing_security),
            file=sys.stderr,
        )
        return 1

    backup_path = "datastore/will_draft_prefs.preferences_pb"
    backup_contract = {
        "android:dataExtractionRules": APP_MANIFEST.read_text(encoding="utf-8"),
        "android:fullBackupContent": APP_MANIFEST.read_text(encoding="utf-8"),
        backup_path: BACKUP_RULES.read_text(encoding="utf-8"),
        f"transfer:{backup_path}": DATA_EXTRACTION_RULES.read_text(encoding="utf-8"),
    }
    missing_backup = [
        token
        for token, source in backup_contract.items()
        if (backup_path if token.startswith("transfer:") else token) not in source
    ]
    if missing_backup:
        print(
            "Missing private-draft backup exclusion:",
            ", ".join(missing_backup),
            file=sys.stderr,
        )
        return 1

    print("All Funerals & Islamic Will resources and UI contracts are present.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
