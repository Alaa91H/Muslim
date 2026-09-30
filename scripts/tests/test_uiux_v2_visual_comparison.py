"""Exercise regressions, reviewed dynamic regions and invalid baseline policies."""
import sys
import unittest
from pathlib import Path

from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from compare_uiux_v2_screenshots import compare


class VisualComparisonTest(unittest.TestCase):
    def test_layout_shift_fails_while_small_rendering_noise_is_tolerated(self):
        baseline = Image.new("RGB", (100, 100), "white")
        ImageDraw.Draw(baseline).rectangle((10, 10, 49, 49), fill="black")
        shifted = Image.new("RGB", baseline.size, "white")
        ImageDraw.Draw(shifted).rectangle((20, 10, 59, 49), fill="black")
        fraction, _ = compare(baseline, shifted, [], 16)
        self.assertGreater(fraction, 0.003)
        fraction, _ = compare(Image.new("RGB", (100, 100), (240, 240, 240)), Image.new("RGB", (100, 100), (243, 243, 243)), [], 16)
        self.assertEqual(fraction, 0)

    def test_mask_excludes_only_its_reviewed_rectangle(self):
        baseline = Image.new("RGB", (100, 100), "white")
        actual = baseline.copy()
        ImageDraw.Draw(actual).rectangle((0, 0, 9, 9), fill="black")
        self.assertEqual(compare(baseline, actual, [[0, 0, 10, 10]], 16)[0], 0)
        ImageDraw.Draw(actual).rectangle((20, 20, 29, 29), fill="black")
        self.assertGreater(compare(baseline, actual, [[0, 0, 10, 10]], 16)[0], 0)

    def test_dimension_changes_and_full_image_masks_are_rejected(self):
        baseline = Image.new("RGB", (100, 100))
        with self.assertRaises(ValueError):
            compare(baseline, Image.new("RGB", (101, 100)), [], 16)
        with self.assertRaises(ValueError):
            compare(baseline, baseline, [[0, 0, 100, 100]], 16)
        with self.assertRaises(ValueError):
            compare(baseline, baseline, [[-1, 0, 10, 10]], 16)


if __name__ == "__main__":
    unittest.main()
