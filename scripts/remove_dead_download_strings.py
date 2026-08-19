# -*- coding: utf-8 -*-
"""Remove the 5 strings that were only used by the removed reader dialog."""
import glob
import os
import re

DEAD = {
    "quran_download_reciter_title",
    "quran_download_options_title",
    "quran_download_current_ayah",
    "quran_download_current_surah",
    "quran_downloaded",
}

root = os.path.join("feature", "feature-quran", "src", "main", "res")
paths = sorted(glob.glob(os.path.join(root, "values*", "strings.xml")))
total_files = 0
total_removed = 0
for p in paths:
    with open(p, "r", encoding="utf-8", newline="") as f:
        text = f.read()
    newline = "\r\n" if "\r\n" in text else "\n"
    lines = text.split(newline)
    out = []
    removed = 0
    for line in lines:
        m = re.search(r'<string name="([^"]+)"', line)
        if m and m.group(1) in DEAD:
            removed += 1
            continue
        out.append(line)
    if removed:
        with open(p, "w", encoding="utf-8", newline="") as f:
            f.write(newline.join(out))
        total_files += 1
        total_removed += removed
print("files=%d removed=%d" % (total_files, total_removed))
