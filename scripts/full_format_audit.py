# -*- coding: utf-8 -*-
"""Full-repo audit: every stringResource(id, ...) call where the arg type
(String vs Int) mismatches the format specifier type (%s vs %d) in the default
locale. String->%d raises IllegalFormatConversionException at runtime."""
import io
import os
import re

MODULES = ["app", "feature", "core"]
problems = []


def spec_types(text):
    out = set()
    for m in re.finditer(r"%(\d+)\$([ds])", text):
        out.add(m.group(2))
    stripped = re.sub(r"%\d+\$[ds]", "", text)
    if re.search(r"%d", stripped):
        out.add("d")
    if re.search(r"%s", stripped):
        out.add("s")
    return out


for mod_dir in MODULES:
    if not os.path.isdir(mod_dir):
        continue
    for root, dirs, files in os.walk(mod_dir):
        dirs[:] = [d for d in dirs if d != "build"]
        if not root.endswith(os.path.join("src", "main", "res")):
            continue
        values = os.path.join(root, "values", "strings.xml")
        if not os.path.exists(values):
            continue
        default_types = {}
        txt = io.open(values, encoding="utf-8").read()
        for m in re.finditer(r'<string name="([^"]+)">(.*?)</string>', txt, re.S):
            default_types[m.group(1)] = spec_types(m.group(2))
        java_dir = root.replace(os.path.join("src", "main", "res"), os.path.join("src", "main", "java"))
        if not os.path.isdir(java_dir):
            continue
        for jroot, _, jfiles in os.walk(java_dir):
            for f in jfiles:
                if not f.endswith(".kt"):
                    continue
                src = io.open(os.path.join(jroot, f), encoding="utf-8").read()
                # match stringResource(R.string.X, args...) with balanced parens
                for m in re.finditer(r"stringResource\(\s*R\.string\.([A-Za-z0-9_]+)\s*,", src):
                    key = m.group(1)
                    types = default_types.get(key)
                    if not types:
                        continue
                    # capture until the matching close paren
                    i = m.end()
                    depth = 1
                    while i < len(src) and depth > 0:
                        if src[i] == "(":
                            depth += 1
                        elif src[i] == ")":
                            depth -= 1
                        i += 1
                    args = src[m.end(): i - 1]
                    for a in [x.strip() for x in args.split(",") if x.strip()]:
                        is_int = bool(re.search(r"\.count\b|\.size\b|\.number\b|\.value\b|\.length\b|\b\d+\b", a)) and ".toString()" not in a
                        is_str = (re.search(r"\.toString\(\)", a) or re.search(r'"[^"]*"', a)) and not is_int
                        if is_str and "d" in types and "s" not in types:
                            problems.append((os.path.relpath(os.path.join(jroot, f)), key, a[:50], types, "String->%d"))
                        if is_int and "s" in types and "d" not in types:
                            problems.append((os.path.relpath(os.path.join(jroot, f)), key, a[:50], types, "Int->%s"))

print("PROBLEMS:", len(problems))
for p in sorted(set((a,b,c,tuple(sorted(d)),e) for a,b,c,d,e in problems)):
    print("  %s | %s(%s) %s -> %s" % (p[0], p[1], p[2], set(p[3]), p[4]))
