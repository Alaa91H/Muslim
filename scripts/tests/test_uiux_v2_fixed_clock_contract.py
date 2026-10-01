"""Guard against changing Android system time once per screenshot test."""
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]


class FixedClockContractTest(unittest.TestCase):
    def test_ci_matrix_manages_one_fixed_clock_per_instrumentation_batch(self):
        capture = (ROOT / "app/src/androidTest/java/org/muslim/app/UiUxV2ScreenshotInstrumentedTest.kt").read_text(encoding="utf-8")
        exporter = (ROOT / "scripts/capture_uiux_v2_artifacts.sh").read_text(encoding="utf-8")

        self.assertIn('getString("uiux.fixedClockManaged") == "true"', capture)
        self.assertIn("if (manageScreenshotClock)", capture)
        self.assertIn("fixed_clock_original_device_epoch=", exporter)
        self.assertIn("restore_fixed_clock", exporter)
        self.assertIn("uiux.fixedClockManaged=true", exporter)
        self.assertEqual(exporter.count("093015002026.00"), 1)


if __name__ == "__main__":
    unittest.main()
