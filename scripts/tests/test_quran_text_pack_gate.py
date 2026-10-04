import importlib.util
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("quran_pack_gate", ROOT / "scripts/verify_quran_text_packs.py")
GATE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(GATE)


class QuranTextPackGateTests(unittest.TestCase):
    def setUp(self):
        self.path = ROOT / "docs/quran/text_packs/_gate-test-pack.json"
        entries = [
            {"globalNumber": number, "text": f"Verified text {number}", "footnotes": ["Source footnote"] if number == 2 else []}
            for number in range(1, GATE.AYAH_COUNT + 1)
        ]
        self.pack = {
            "schemaVersion": 2,
            "manifest": {
                "id": "gate-test-en",
                "kind": GATE.MEANING,
                "languageTag": "en",
                "title": "Verified edition",
                "work": "Meaning translation",
                "translator": "Named translator",
                "publisher": "Named publisher",
                "sourceAttribution": "Source edition review and contributor details",
                "sourceUrl": "https://example.org/source",
                "license": "CC BY 4.0",
                "version": "1.0",
                "reviewer": "Named source reviewer",
                "reviewReference": "https://example.org/review",
                "expectedAyahCount": GATE.AYAH_COUNT,
                "footnoteCount": 1,
                "sha256": "",
                "reviewStatus": "source_reviewed",
            },
            "entries": entries,
        }
        self._set_digest()

    def tearDown(self):
        self.path.unlink(missing_ok=True)

    def _set_digest(self):
        import hashlib

        serialized = json.dumps(self.pack["entries"], ensure_ascii=False, separators=(",", ":"))
        self.pack["manifest"]["sha256"] = hashlib.sha256(serialized.encode("utf-8")).hexdigest()

    def _validate(self):
        self.path.write_text(json.dumps(self.pack, ensure_ascii=False), encoding="utf-8")
        return GATE.validate_pack("docs/quran/text_packs/_gate-test-pack.json", GATE.MEANING, "en")

    def test_complete_pack_and_footnotes_pass(self):
        manifest, errors = self._validate()
        self.assertEqual(manifest["id"], "gate-test-en")
        self.assertEqual(errors, [])

    def test_missing_ayah_fails(self):
        self.pack["entries"].pop()
        self._set_digest()
        _, errors = self._validate()
        self.assertTrue(any("exactly 6236" in error for error in errors))
        self.assertTrue(any("coverage" in error for error in errors))

    def test_checksum_and_footnote_count_mismatch_fail(self):
        self.pack["manifest"]["sha256"] = "0" * 64
        self.pack["manifest"]["footnoteCount"] = 0
        _, errors = self._validate()
        self.assertTrue(any("footnote count" in error for error in errors))
        self.assertTrue(any("checksum" in error for error in errors))

    def test_source_and_review_reference_must_share_a_domain(self):
        self.pack["manifest"]["reviewReference"] = "https://unrelated.org/review"
        _, errors = self._validate()
        self.assertTrue(any("share a publisher domain" in error for error in errors))

    def test_ui_locale_coverage_requires_meanings_and_translated_tafsir(self):
        self.assertEqual(GATE.required_kinds("fr"), {GATE.MEANING, GATE.TAFSIR})
        self.assertEqual(GATE.required_kinds("ar"), {GATE.TAFSIR_ORIGINAL})


if __name__ == "__main__":
    unittest.main()
