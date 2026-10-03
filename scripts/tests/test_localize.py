from __future__ import annotations

import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

from scripts import localize
from scripts import sync_duplicate_localizations


class LocalizationQualityTests(unittest.TestCase):
    def test_duplicate_translation_reuse_requires_one_exact_source_match(self) -> None:
        index: dict[tuple[str, str], set[str]] = {}
        sync_duplicate_localizations.add_candidate(index, "fr", "Open Quran", "Ouvrir le Coran")
        self.assertEqual(
            sync_duplicate_localizations.resolve_candidate(index, "fr", "Open Quran"),
            "Ouvrir le Coran",
        )

        sync_duplicate_localizations.add_candidate(index, "fr", "Open Quran", "Ouvrir le livre")
        self.assertIsNone(
            sync_duplicate_localizations.resolve_candidate(index, "fr", "Open Quran"),
        )

    def test_duplicate_translation_reuse_rejects_source_and_placeholder_mismatches(self) -> None:
        index: dict[tuple[str, str], set[str]] = {}
        sync_duplicate_localizations.add_candidate(index, "de", "Open Quran", "Open Quran")
        sync_duplicate_localizations.add_candidate(index, "de", "Page %1$d", "Seite %1$s")
        self.assertIsNone(sync_duplicate_localizations.resolve_candidate(index, "de", "Open Quran"))
        self.assertIsNone(sync_duplicate_localizations.resolve_candidate(index, "de", "Page %1$d"))

    def test_duplicate_sync_fills_only_missing_unambiguous_values(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            donor = root / "donor"
            target = root / "target"
            for module in (donor, target):
                (module / "values-en").mkdir(parents=True)
                (module / "values-fr").mkdir()

            (donor / "values-en/strings.xml").write_text(
                '<resources><string name="source">Open Quran</string></resources>', encoding="utf-8",
            )
            (donor / "values-fr/strings.xml").write_text(
                '<resources><string name="source">Ouvrir le Coran</string></resources>', encoding="utf-8",
            )
            (target / "values-en/strings.xml").write_text(
                '<resources><string name="action">Open Quran</string>'
                '<string name="copy">Open Quran</string>'
                '<string name="existing">Save changes</string></resources>', encoding="utf-8",
            )
            (target / "values-fr/strings.xml").write_text(
                '<resources><string name="action">Open Quran</string>'
                '<string name="existing">Enregistrer</string></resources>', encoding="utf-8",
            )

            with patch("builtins.print"):
                sync_duplicate_localizations.synchronize([str(donor), str(target)])
            translated = localize.read_locale_strings(str(target / "values-fr/strings.xml"))

        self.assertEqual(translated["action"], "Ouvrir le Coran")
        self.assertEqual(translated["copy"], "Ouvrir le Coran")
        self.assertEqual(translated["existing"], "Enregistrer")

    def test_scholar_library_has_complete_english_source(self) -> None:
        resources = Path(localize.PROJECT_ROOT) / "feature/feature-scholar-library/src/main/res"
        arabic = localize.read_locale_strings(str(resources / "values/strings.xml"))
        english_path = resources / "values-en/strings.xml"

        self.assertTrue(english_path.is_file(), "Scholar Library needs an English UI source")
        english = localize.read_locale_strings(str(english_path))
        self.assertEqual(set(english), set(arabic))
        for key, source in arabic.items():
            self.assertEqual(localize.format_signature(source), localize.format_signature(english[key]), key)

    def test_android_format_tokens_are_counted_without_treating_quotes_as_tokens(self) -> None:
        source = r"Don't remove %1$s, %2$d, 100%%, or the line break\n"
        self.assertEqual(
            localize.format_signature(source),
            localize.collections.Counter(["%1$s", "%2$d", "%%", r"\n"]),
        )
        self.assertNotIn(r"\'", localize.format_signature(source))

    def test_restore_keeps_placeholder_identity_when_translation_reorders_markers(self) -> None:
        protected, tokens = localize.protect("From %1$s to %2$d")
        reordered = protected.replace("zxqmuslimfmt0zxq", "TEMP").replace(
            "zxqmuslimfmt1zxq", "zxqmuslimfmt0zxq",
        ).replace("TEMP", "zxqmuslimfmt1zxq")
        self.assertEqual(localize.restore(reordered, tokens), "From %2$d to %1$s")

    def test_provider_rate_limit_fails_without_returning_source_as_translation(self) -> None:
        error = urllib.error.HTTPError("https://translate.invalid", 429, "rate limited", {}, None)
        try:
            with patch.object(localize.urllib.request, "urlopen", side_effect=error), patch.object(localize.time, "sleep") as sleep:
                with self.assertRaisesRegex(RuntimeError, "HTTP 429 rate limited.*no source fallback"):
                    localize.translate_batch(["Start playback"], "fr")
                self.assertEqual([call.args[0] for call in sleep.call_args_list], [10, 20])
        finally:
            error.close()

    def test_provider_retry_after_header_is_respected(self) -> None:
        error = urllib.error.HTTPError(
            "https://translate.invalid", 429, "rate limited", {"Retry-After": "7"}, None,
        )
        try:
            with patch.object(localize.urllib.request, "urlopen", side_effect=error), patch.object(localize.time, "sleep") as sleep:
                with self.assertRaisesRegex(RuntimeError, "HTTP 429 rate limited"):
                    localize.translate_batch(["Start playback"], "fr")
                self.assertEqual([call.args[0] for call in sleep.call_args_list], [7.0, 7.0])
        finally:
            error.close()

    def test_cached_source_fallback_is_rejected(self) -> None:
        source = "Start Quran playback"
        protected, _ = localize.protect(source)
        with self.assertRaisesRegex(RuntimeError, "Untranslated source found in cache"):
            localize.process_lang("res", "fr", {"play": source}, {f"fr|{protected}": source}, None)

    def test_provider_returning_source_text_is_rejected(self) -> None:
        source = "Start Quran playback"
        protected, _ = localize.protect(source)
        with patch.object(localize, "translate_batch", return_value=[protected]):
            with self.assertRaisesRegex(RuntimeError, "untranslated source"):
                localize.process_lang("res", "fr", {"play": source}, {}, None)

    def test_fill_mode_keeps_safe_values_when_one_translation_is_rejected(self) -> None:
        first = "Start Quran playback"
        second = "Open prayer settings"
        first_protected, _ = localize.protect(first)
        with patch.object(localize, "translate_batch", return_value=[first_protected, "Ouvrir les paramètres de prière"]):
            translated = localize.process_lang(
                "res", "fr", {"bad": first, "good": second}, {}, None, allow_incomplete=True,
            )
        self.assertEqual(translated, {"good": "Ouvrir les paramètres de prière"})

    def test_quality_gate_rejects_copied_source_sentences(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            res = Path(temporary_directory)
            (res / "values").mkdir()
            (res / "values-fr").mkdir()
            sentence = "Start the prayer audio now"
            source_xml = f'<resources><string name="action">{sentence}</string></resources>'
            (res / "values" / "strings.xml").write_text(source_xml, encoding="utf-8")
            (res / "values-fr" / "strings.xml").write_text(source_xml, encoding="utf-8")

            with patch("builtins.print") as output:
                problems = localize.check_locales([str(res)])

        self.assertGreater(problems, 0)
        self.assertTrue(any("UNTRANSLATED" in str(call) for call in output.call_args_list))

    def test_mixed_language_detector_finds_a_copied_source_phrase(self) -> None:
        source = "Coordinates are never sent to any external server that stores user data."
        translated = "[translated sentence] external server that stores user data [translated ending]"

        self.assertEqual(localize.untranslated_source_phrase(source, translated), "external server that stores user")

    def test_mixed_language_detector_ignores_a_single_borrowed_technical_term(self) -> None:
        self.assertIsNone(
            localize.untranslated_source_phrase("Use an external server", "Traduction: server"),
        )

    def test_mixed_language_detector_ignores_official_product_name(self) -> None:
        self.assertIsNone(
            localize.untranslated_source_phrase(
                "Use your wallpaper colors (Material You) on Android",
                "Utilisez les couleurs du fond d'écran (Material You) sur Android",
            ),
        )

    def test_mixed_language_detector_ignores_islamic_terms_and_media_control_labels(self) -> None:
        self.assertIsNone(
            localize.untranslated_source_phrase(
                "Adhkar, tasbih, Ramadan and Zakat with reminders",
                "Adhkar, tasbih, Ramadan en Zakat met aanmaningen",
            ),
        )

    def test_mixed_language_detector_matches_adjacent_words_not_gapped_words(self) -> None:
        self.assertIsNone(
            localize.untranslated_source_phrase(
                "Free forever · Privacy first · Open source app",
                "Gratis para siempre · Privacy first · Aplicacion de codigo abierto",
            ),
        )

    def test_mixed_language_detector_still_finds_a_real_copied_clause(self) -> None:
        self.assertEqual(
            localize.untranslated_source_phrase(
                "New releases require confirmation before silent installation",
                "Nuevas versiones require confirmation before silent installation",
            ),
            "require confirmation before silent installation",
        )
        self.assertIsNone(
            localize.untranslated_source_phrase(
                "Play pause stop controls while reciting Quran",
                "Afspeel pauze stop bediening tijdens recitatie van Quran",
            ),
        )

    def test_quality_gate_reports_an_untranslated_source_phrase(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            res = Path(temporary_directory)
            (res / "values").mkdir()
            for locale in ("values-en", "values-fr"):
                (res / locale).mkdir()
            (res / "values-en/strings.xml").write_text(
                '<resources><string name="privacy">Coordinates are never sent to any external server that stores user data.</string></resources>',
                encoding="utf-8",
            )
            (res / "values-fr/strings.xml").write_text(
                '<resources><string name="privacy">[translated] external server that stores user data [translated]</string></resources>',
                encoding="utf-8",
            )

            with patch("builtins.print") as output:
                problems = localize.check_locales([str(res)])

        self.assertEqual(problems, 1)
        self.assertTrue(any("MIXED_LANGUAGE" in str(call) for call in output.call_args_list))

    def test_quality_gate_does_not_double_report_a_wholly_untranslated_value(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            res = Path(temporary_directory)
            (res / "values").mkdir()
            for locale in ("values-en", "values-fr"):
                (res / locale).mkdir()
            shared = '<resources><string name="label">Card corner softness</string></resources>'
            (res / "values-en/strings.xml").write_text(shared, encoding="utf-8")
            (res / "values-fr/strings.xml").write_text(shared, encoding="utf-8")

            with patch("builtins.print") as output:
                problems = localize.check_locales([str(res)])

        self.assertEqual(problems, 1)
        self.assertTrue(any("UNTRANSLATED" in str(call) for call in output.call_args_list))
        self.assertFalse(any("MIXED_LANGUAGE" in str(call) for call in output.call_args_list))

    def test_appending_resources_preserves_existing_translations_and_escapes_xml(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            path = Path(temporary_directory) / "strings.xml"
            original = '<resources>\n    <string name="old">Already curated</string>\n</resources>\n'
            path.write_text(original, encoding="utf-8")

            localize.append_locale_strings(str(path), {"new": "Qur'an & <meaning>"})

            content = path.read_text(encoding="utf-8")
            self.assertIn('<string name="old">Already curated</string>', content)
            self.assertIn("Qur\\'an &amp; &lt;meaning&gt;", content)
            self.assertEqual(localize.read_locale_strings(str(path)), {
                "old": "Already curated",
                "new": "Qur\\'an & <meaning>",
            })

    def test_fill_missing_continues_other_locales_after_provider_rejects_one(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            res = Path(temporary_directory)
            for locale in ("values-en", "values-aa", "values-ab"):
                (res / locale).mkdir()
            (res / "values-en/strings.xml").write_text(
                '<resources><string name="action">Open Quran</string></resources>', encoding="utf-8",
            )
            for locale in ("values-aa", "values-ab"):
                (res / locale / "strings.xml").write_text("<resources></resources>", encoding="utf-8")

            def translate(_res_dir: str, lang: str, strings: dict[str, str], _cache: dict[str, str], _app_name: str | None, **_kwargs: object) -> dict[str, str]:
                if lang == "aa":
                    raise RuntimeError("provider returned untranslated source")
                return {"action": "Abrir el Corán"}

            with patch.object(localize, "process_lang", side_effect=translate), patch("builtins.print"):
                filled, failures = localize.fill_missing_locales([str(res)], {})

            self.assertEqual(filled, 1)
            self.assertEqual(len(failures), 1)
            self.assertIn("aa", failures[0])
            self.assertEqual(localize.read_locale_strings(str(res / "values-ab/strings.xml")), {"action": "Abrir el Corán"})


if __name__ == "__main__":
    unittest.main()
