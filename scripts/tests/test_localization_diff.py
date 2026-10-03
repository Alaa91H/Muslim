from __future__ import annotations

import unittest

from scripts.check_localization_diff import parse_xml, validate_translation


class LocalizationDiffTests(unittest.TestCase):
    def test_parse_xml_rejects_malformed_xml(self) -> None:
        _, issues = parse_xml("<resources><string>", "values-fr/strings.xml")
        self.assertTrue(any("Invalid XML" in issue for issue in issues))

    def test_parse_xml_rejects_duplicate_translatable_keys(self) -> None:
        _, issues = parse_xml(
            '<resources><string name="title">A</string><string name="title">B</string></resources>',
            "values-fr/strings.xml",
        )
        self.assertTrue(any("Duplicate string" in issue for issue in issues))

    def test_translation_requires_matching_format_tokens(self) -> None:
        issues = validate_translation("values-fr/strings.xml", "count", "%s livres", "%1$d books", "كتب")
        self.assertTrue(any("Placeholder mismatch" in issue for issue in issues))

    def test_translation_rejects_english_source_copy(self) -> None:
        issues = validate_translation("values-fr/strings.xml", "title", "Prayer Times", "Prayer Times", "مواقيت الصلاة")
        self.assertTrue(any("Untranslated English source" in issue for issue in issues))

    def test_translation_accepts_translated_value_and_reordered_tokens(self) -> None:
        issues = validate_translation("values-fr/strings.xml", "count", "%1$d livres", "%1$d books", "عدد الكتب %1$d")
        self.assertEqual([], issues)


if __name__ == "__main__":
    unittest.main()
