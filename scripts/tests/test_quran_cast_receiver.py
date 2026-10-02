"""Verify the Android sender and custom Cast receiver share one safe contract."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class QuranCastReceiverContractTest(unittest.TestCase):
    def test_namespace_and_version_match_android_sender(self):
        sender = (ROOT / "app/src/main/java/org/muslim/app/cast/QuranCastPlayback.kt").read_text(encoding="utf-8")
        receiver = (ROOT / "receiver/index.html").read_text(encoding="utf-8")
        payload = (ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/domain/QuranCastPayload.kt").read_text(encoding="utf-8")
        namespace = re.search(r'const val CUSTOM_NAMESPACE = "([^"]+)"', sender).group(1)
        self.assertIn(f"const NAMESPACE = '{namespace}'", receiver)
        self.assertIn("CURRENT_SCHEMA_VERSION = 1", payload)
        self.assertIn("payload.schemaVersion !== 1", receiver)

    def test_receiver_renders_content_without_interpreting_streamed_markup(self):
        receiver = (ROOT / "receiver/index.html").read_text(encoding="utf-8")
        for field in ("arabicAyah", "translation", "tafsir", "prayerLocation", "prayerTimes"):
            self.assertIn(f"payload.{field}", receiver)
        self.assertIn("textContent", receiver)
        self.assertNotIn("innerHTML", receiver)


if __name__ == "__main__":
    unittest.main()
