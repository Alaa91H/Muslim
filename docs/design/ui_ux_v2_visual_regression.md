# UI/UX V2 visual regression

## Capture contract

`UiUxV2MatrixInstrumentedTest` enumerates 192 cases per emulator API:
eight critical screens × Arabic/English × light/dark × font scales 1.0/1.5/2.0
× compact/expanded. Each case launches the real Hilt-backed app with a saved
Makkah location, system permission onboarding handled and dynamic colors disabled.
Expanded cases use 1280×800 pixels and assert a window width of at least 840dp.
This is window-resize coverage, not physical fold/unfold validation.

The export script pulls atomically published PNGs while AGP instrumentation is
running. `validate_uiux_v2_screenshots.py` rejects missing variants, invalid PNG
headers and invalid window dimensions. Content readiness and actual system font
scale are checked on the emulator. Quran captures also require loaded Quran text.

## Reviewed baseline contract

The comparison runner is available, but a baseline must be reviewed before it
can be used as an acceptance gate. There is no automatic approval/update mode.
Keep API 26 and API 36 in separate baseline directories, each containing 192 PNGs
and `review.json`. The manifest has an `images` object keyed by the exact capture
filename. Each entry must contain:

```json
{
  "reviewed": true,
  "reviewed_by": "named reviewer",
  "source_run": "verified GitHub Actions run ID",
  "channel_tolerance": 16,
  "max_changed_fraction": 0.003,
  "masks": []
}
```

Masks are explicitly reviewed pixel rectangles `[left, top, right, bottom]`, with
right/bottom exclusive. They must not hide clipping, text geometry, focus targets
or other requirements being accepted. Clock/date/countdown and sensor-dependent
content need a deterministic fixture or a documented, narrowly reviewed mask
before comparisons are enabled in CI. The current real-app captures use the
emulator's clock; dates/countdowns are not deterministic goldens yet.

```powershell
python -m pip install -r scripts/requirements-uiux-visual.txt
python -m unittest discover -s scripts/tests -p test_uiux_v2_visual_comparison.py -v
python scripts/compare_uiux_v2_screenshots.py BASELINE ACTUAL --output COMPARISON
```

The runner rejects missing reviews, missing images, dimension changes and masks
that exclude the entire image. It writes machine-readable `comparison.json` and
RGB difference PNGs for changed variants. A comparison failure requires visual
review and a specific fix or intentional baseline update.

## Remaining acceptance evidence

- Review every full-matrix capture and fix defects before accepting goldens.
- Establish deterministic dynamic content or reviewed masks, then enable the
  comparison gate on the same emulator configuration.
- Check TalkBack traversal and actions on representative devices.
- Measure frame time, recomposition and scrolling on a representative device;
  successful emulator instrumentation is not a performance result.
- Check actual fold/unfold continuity and the final installed build.
