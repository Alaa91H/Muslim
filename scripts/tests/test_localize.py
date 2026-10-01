from __future__ import annotations

import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

from scripts import localize


class LocalizationQualityTests(unittest.TestCase):
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
        with patch.object(localize.urllib.request, "urlopen", side_effect=error), patch.object(localize.time, "sleep"):
            with self.assertRaisesRegex(RuntimeError, "no source fallback"):
                localize.translate_batch(["Start playback"], "fr")

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


if __name__ == "__main__":
    unittest.main()
