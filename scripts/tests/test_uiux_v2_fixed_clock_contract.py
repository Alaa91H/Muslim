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
        self.assertIn("actualEpoch in expectedEpoch until (expectedEpoch + 24 * 60 * 60 * 1_000L)", capture)
        self.assertIn("fixed_clock_original_device_epoch=", exporter)
        self.assertIn("restore_fixed_clock", exporter)
        self.assertIn("uiux.fixedClockManaged=true", exporter)
        self.assertEqual(exporter.count("093015002026.00"), 1)

    def test_each_screenshot_batch_revalidates_managed_clock_before_gradle(self):
        exporter = (ROOT / "scripts/capture_uiux_v2_artifacts.sh").read_text(encoding="utf-8")
        run_batch = exporter.split("run_batch() {", 1)[1].split("\n}", 1)[0]

        self.assertIn("ensure_fixed_clock", run_batch)
        self.assertLess(run_batch.index("ensure_fixed_clock"), run_batch.index("./gradlew"))

    def test_screenshot_matrix_reboots_and_reinitializes_emulator_between_batches(self):
        exporter = (ROOT / "scripts/capture_uiux_v2_artifacts.sh").read_text(encoding="utf-8")
        reboot = exporter.split("reboot_emulator_between_batches() {", 1)[1].split("\n}", 1)[0]
        matrix = exporter.split("for screens in prayer-home,prayer-monthly", 1)[1]

        self.assertIn("adb reboot", reboot)
        self.assertIn("adb wait-for-device", reboot)
        self.assertIn("sys.boot_completed", reboot)
        self.assertIn("adb root", reboot)
        self.assertIn("ensure_fixed_clock", reboot)
        self.assertIn("reboot_emulator_between_batches", matrix)
        self.assertLess(matrix.index("reboot_emulator_between_batches"), matrix.index("run_batch"))


if __name__ == "__main__":
    unittest.main()
