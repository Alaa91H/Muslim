#!/usr/bin/env python3
"""Verify that feature modules do not introduce direct feature-to-feature dependencies.

Feature modules are intended to depend on core modules only. A very small allowlist
captures the legacy edges that already exist so this verifier can be enabled without
forcing a risky, all-at-once refactor. The allowlist must shrink as those edges are
removed and may not grow without an explicit architecture decision.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FEATURE_ROOT = ROOT / "feature"

PROJECT_DEPENDENCY = re.compile(
    r"""project\(\s*(?:path\s*=\s*)?["'](:feature:[^"']+)["']\s*\)"""
)

# Temporary legacy edges. Do not add new entries here as a shortcut.
ALLOWED_FEATURE_EDGES: set[tuple[str, str]] = {
    (":feature:feature-learn", ":feature:feature-qibla"),
    (":feature:feature-settings", ":feature:feature-hadith"),
    (":feature:feature-settings", ":feature:feature-learn"),
}


def discover_feature_edges() -> set[tuple[str, str]]:
    edges: set[tuple[str, str]] = set()

    if not FEATURE_ROOT.is_dir():
        raise RuntimeError(f"Feature root not found: {FEATURE_ROOT}")

    for module_dir in sorted(path for path in FEATURE_ROOT.iterdir() if path.is_dir()):
        build_file = module_dir / "build.gradle.kts"
        if not build_file.is_file():
            continue

        source = f":feature:{module_dir.name}"
        content = build_file.read_text(encoding="utf-8")
        for target in PROJECT_DEPENDENCY.findall(content):
            edges.add((source, target))

    return edges


def format_edge(edge: tuple[str, str]) -> str:
    return f"{edge[0]} -> {edge[1]}"


def main() -> int:
    discovered = discover_feature_edges()
    unexpected = sorted(discovered - ALLOWED_FEATURE_EDGES)
    stale_allowlist = sorted(ALLOWED_FEATURE_EDGES - discovered)

    if unexpected:
        print("ERROR: new direct feature-to-feature dependencies were found:", file=sys.stderr)
        for edge in unexpected:
            print(f"  - {format_edge(edge)}", file=sys.stderr)
        print(
            "\nMove shared contracts/logic into an appropriate core module or compose "
            "the features from the app module instead of adding another feature edge.",
            file=sys.stderr,
        )

    if stale_allowlist:
        print("ERROR: stale architecture allowlist entries were found:", file=sys.stderr)
        for edge in stale_allowlist:
            print(f"  - {format_edge(edge)}", file=sys.stderr)
        print(
            "\nRemove stale entries from ALLOWED_FEATURE_EDGES so the technical-debt "
            "baseline only contains dependencies that still exist.",
            file=sys.stderr,
        )

    if unexpected or stale_allowlist:
        return 1

    print(
        "Feature module boundary check passed "
        f"({len(discovered)} temporary legacy edge(s), 0 new edge(s))."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
