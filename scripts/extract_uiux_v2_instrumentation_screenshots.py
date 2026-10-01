#!/usr/bin/env python3
"""Restore screenshots emitted as Base64 chunks in Android test XML output."""

from __future__ import annotations

import base64
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

CHUNK_SIZE = 3000
MARKER = re.compile(r"uiux\.screenshot\.(name|chunks|chunk|data|complete)\s*[=:]\s*([^\s,]+)")


def recover(xml_path: Path, output: Path) -> list[Path]:
    events: dict[str, dict[str, object]] = {}
    root = ET.parse(xml_path).getroot()
    for node in root.iter():
        raw = " ".join([node.text or "", *node.attrib.values()])
        pairs = MARKER.findall(raw)
        current_name = next((value for key, value in pairs if key == "name"), None)
        for key, value in pairs:
            if key == "complete":
                current_name = value
        if not current_name:
            continue
        event = events.setdefault(current_name, {"chunks": {}})
        for key, value in pairs:
            if key == "chunks":
                event["count"] = int(value)
            elif key == "chunk":
                event["index"] = int(value)
            elif key == "data":
                index = int(event.get("index", -1))
                if index >= 0:
                    event["chunks"][index] = value
            elif key == "complete":
                event["complete"] = True

    written = []
    output.mkdir(parents=True, exist_ok=True)
    for name, event in events.items():
        chunks = event["chunks"]
        count = event.get("count")
        if not event.get("complete") or count is None or set(chunks) != set(range(count)):
            continue
        data = base64.b64decode("".join(chunks[index] for index in range(count)), validate=True)
        if not data.startswith(bytes.fromhex("89504e470d0a1a0a")):
            raise ValueError(f"Recovered image is not a PNG: {name}")
        path = output / name
        path.write_bytes(data)
        written.append(path)
    return written


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: extract_uiux_v2_instrumentation_screenshots.py TEST_RESULTS_DIR OUTPUT_DIR")
    source, output = map(Path, sys.argv[1:])
    written = []
    for xml_path in source.rglob("*.xml"):
        if xml_path.name.startswith("TEST-"):
            written.extend(recover(xml_path, output))
    for path in written:
        print(f"Recovered {path.name} ({path.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
