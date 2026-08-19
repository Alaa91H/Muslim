# -*- coding: utf-8 -*-
"""Find stringResource(id, arg) calls where the arg type does not match the
format specifier type (String passed to %d -> IllegalFormatConversionException
at runtime, crashing the screen on composition)."""
import glob
import io
import os
import re

MODULES = ["feature/feature-adhkar", "feature/feature-tasbih"]

# Gather string -> specifier types from default (values) and values-en.
def specifier_types(text):
    out = set()
    for m in re.finditer(r"%(\d+)\$([ds])", text):
        out.add(m.group(2))
    # raw %s / %d (non-positional)
    stripped = re.sub(r"%\d+\$[ds]", "", text)
    if re.search(r"%d", stripped):
        out.add("d")
    if re.search(r"%s", stripped):
        out.add("s")
    return out

problems = []
for module in MODULES:
    res = os.path.join(module, "src", "main", "res")
    default_text = io.open(os.path.join(res, "values", "strings.xml"), encoding="utf-8").read()
    default_types = {}
    for m in re.finditer(r'<string name="([^"]+)">(.*?)</string>', default_text, re.S):
        default_types[m.group(1)] = specifier_types(m.group(2))

    # collect all format calls in ui code
    for root, _, files in os.walk(os.path.join(module, "src", "main", "java")):
        for f in files:
            if not f.endswith(".kt"):
                continue
            path = os.path.join(root, f)
            src = io.open(path, encoding="utf-8").read()
            # stringResource(R.string.X, arg1, arg2...)
            for m in re.finditer(
                r"stringResource\(\s*R\.string\.([A-Za-z0-9_]+)\s*,\s*([^)]+)\)",
                src,
            ):
                key = m.group(1)
                args = m.group(2)
                types = default_types.get(key, set())
                arg_parts = [a.strip() for a in args.split(",") if a.strip()]
                # classify each arg: String-ish vs Int-ish
                for a in arg_parts:
                    is_str = bool(
                        re.search(r"\.toString\(\)", a)
                        or re.search(r'"[^"]*"', a)
                        or re.search(r"stringResource\(", a)
                    )
                    is_int = bool(
                        re.search(r"\b\d+\b", a)
                        and not re.search(r'"[^"]*"', a)
                        or re.search(r"\.count\b|\.size\b|\.value\b|\.number\b|\.length\b", a)
                    )
                    if is_str and "d" in types and "s" not in types:
                        problems.append((module, os.path.relpath(path, module), key, a, types, "String->%d"))
                    if is_int and "s" in types and "d" not in types:
                        problems.append((module, os.path.relpath(path, module), key, a, types, "Int->%s"))

print("PROBLEMS:", len(problems))
for p in problems:
    print("  %s | %s | %s(%s) types=%s -> %s" % (p[0], p[1], p[2], p[3][:40], p[4], p[5]))
