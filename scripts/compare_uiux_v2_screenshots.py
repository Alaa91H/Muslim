#!/usr/bin/env python3
"""Compare captures against explicitly reviewed baselines; never approve images automatically."""
from __future__ import annotations

import argparse
import json
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw

from validate_uiux_v2_screenshots import expected_names


def compare(baseline: Image.Image, actual: Image.Image, masks: list[list[int]], tolerance: int) -> tuple[float, Image.Image]:
    if baseline.size != actual.size:
        raise ValueError(f"Dimensions changed: {baseline.size} -> {actual.size}")
    difference = ImageChops.difference(baseline.convert("RGB"), actual.convert("RGB"))
    mask = Image.new("L", baseline.size, 255)
    draw = ImageDraw.Draw(mask)
    for rectangle in masks:
        if len(rectangle) != 4 or not (0 <= rectangle[0] < rectangle[2] <= baseline.width and 0 <= rectangle[1] < rectangle[3] <= baseline.height):
            raise ValueError(f"Invalid reviewed mask: {rectangle}")
        draw.rectangle((rectangle[0], rectangle[1], rectangle[2] - 1, rectangle[3] - 1), fill=0)
    red, green, blue = difference.split()
    maximum = ImageChops.lighter(ImageChops.lighter(red, green), blue)
    changed = maximum.point(lambda value: 255 if value > tolerance else 0)
    changed = ImageChops.multiply(changed, mask)
    compared_pixels = mask.histogram()[255]
    if not compared_pixels:
        raise ValueError("Reviewed masks exclude the entire image")
    return changed.histogram()[255] / compared_pixels, difference


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("baseline", type=Path)
    parser.add_argument("actual", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    manifest = json.loads((args.baseline / "review.json").read_text(encoding="utf-8"))
    reviews = manifest["images"]
    if set(reviews) != expected_names():
        raise SystemExit("Baseline review must enumerate all 192 variants")
    args.output.mkdir(parents=True, exist_ok=True)
    results = []
    for name in sorted(expected_names()):
        review = reviews[name]
        if not review.get("reviewed") or not review.get("reviewed_by") or not review.get("source_run"):
            raise SystemExit(f"Baseline has no explicit reviewer and CI provenance: {name}")
        try:
            with Image.open(args.baseline / name) as baseline, Image.open(args.actual / name) as actual:
                fraction, difference = compare(baseline, actual, review.get("masks", []), review.get("channel_tolerance", 16))
                passed = fraction <= review.get("max_changed_fraction", 0.003)
                if not passed:
                    difference.save(args.output / name)
                results.append({"name": name, "changed_fraction": fraction, "passed": passed})
        except (OSError, ValueError) as error:
            results.append({"name": name, "passed": False, "error": str(error)})
    (args.output / "comparison.json").write_text(json.dumps(results, indent=2), encoding="utf-8")
    failures = sum(not item["passed"] for item in results)
    print(f"Compared {len(results)} reviewed variants; {failures} differences require review.")
    raise SystemExit(bool(failures))


if __name__ == "__main__":
    main()
