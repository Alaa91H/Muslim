#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Checks that no locale string has a broken placeholder (raw %N without a
valid %N$s / %N$d specifier)."""
import glob
import re
import sys
import xml.etree.ElementTree as ET

SPEC = re.compile(r"%\d+\$[ds]")
BAD = re.compile(r"%\d+")

issues = 0
for f in glob.glob("app/src/main/res/values-*/strings.xml") + glob.glob("feature/feature-settings/src/main/res/values-*/strings.xml"):
    try:
        root = ET.parse(f).getroot()
    except ET.ParseError as e:
        print("XML ERROR:", f, e)
        issues += 1
        continue
    for el in root.iter("string"):
        t = el.text or ""
        if BAD.search(t) and not SPEC.search(t):
            print(f, el.get("name"), repr(t[:60]))
            issues += 1
print("placeholder issues:", issues)
sys.exit(1 if issues else 0)