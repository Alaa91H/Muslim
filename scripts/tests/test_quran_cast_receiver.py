"""Verify Muslim's sender contract and its external receiver documentation."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class QuranCastReceiverContractTest(unittest.TestCase):
    def test_sender_and_receiver_deployment_docs_use_the_same_protocol(self):
        sender = (ROOT / "app/src/main/java/org/muslim/app/cast/QuranCastPlayback.kt").read_text(encoding="utf-8")
        payload = (ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/domain/QuranCastPayload.kt").read_text(encoding="utf-8")
        receiver_docs = (ROOT / "receiver/README.md").read_text(encoding="utf-8")

        namespace = re.search(r'const val CUSTOM_NAMESPACE = "([^"]+)"', sender).group(1)
        version = int(re.search(r"CURRENT_SCHEMA_VERSION = (\d+)", payload).group(1))

        self.assertIn(namespace, receiver_docs)
        self.assertIn(f"schema v{version}", receiver_docs)
        self.assertIn("QuranLiveStream", receiver_docs)
        self.assertIn("web/cast/receiver.html", receiver_docs)
        self.assertIn("CAST_RECEIVER_APP_ID", receiver_docs)

    def test_internal_sample_is_not_documented_as_the_deployed_receiver(self):
        receiver_docs = (ROOT / "receiver/README.md").read_text(encoding="utf-8")
        self.assertNotIn("receiver/index.html", receiver_docs)
        self.assertIn("standard audio receiver", receiver_docs)
        self.assertFalse((ROOT / "receiver/index.html").exists())


if __name__ == "__main__":
    unittest.main()
