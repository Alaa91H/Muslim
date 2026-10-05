import importlib.util
import unittest

ROOT = __import__("pathlib").Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "quran_foundation_catalog_audit",
    ROOT / "scripts/audit_quran_foundation_catalog.py",
)
AUDIT = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(AUDIT)


class QuranFoundationCatalogAuditTests(unittest.TestCase):
    def test_normalizes_translation_metadata_without_retaining_content(self):
        payload = {
            "translations": [
                {
                    "id": 19,
                    "name": "Verified meanings",
                    "author_name": "Named translator",
                    "slug": "verified-meanings",
                    "language_name": "english",
                    "translated_name": {"name": "Verified meanings", "language_name": "english"},
                    "text": "Sensitive Quran content must not be included in this audit.",
                }
            ]
        }

        records = AUDIT.normalize_resources(payload, "translations")

        self.assertEqual(records, [{
            "id": 19,
            "name": "Verified meanings",
            "author_name": "Named translator",
            "slug": "verified-meanings",
            "language_name": "english",
            "translated_name": {"name": "Verified meanings", "language_name": "english"},
        }])

    def test_rejects_duplicate_resource_ids(self):
        row = {"id": 1, "name": "Work", "language_name": "english"}
        with self.assertRaisesRegex(ValueError, "duplicate"):
            AUDIT.normalize_resources({"tafsirs": [row, row]}, "tafsirs")

    def test_rejects_unexpected_catalogue_shape(self):
        with self.assertRaisesRegex(ValueError, "translations"):
            AUDIT.normalize_resources({"items": []}, "translations")


if __name__ == "__main__":
    unittest.main()
