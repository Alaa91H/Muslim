# -*- coding: utf-8 -*-
"""Clean re-apply of the downloads-screen string changes.

1. Reverts every feature-quran locale change (undoes collateral re-translations
   from the localize.py re-run).
2. Re-removes the 5 dead dialog strings from all locales.
3. Adds the 2 new summary strings (AR/EN sources + translated locales), taking
   translations ONLY from the localize cache with strict validation.
"""
import glob
import io
import json
import os
import re
import subprocess
import sys

RES = os.path.join("feature", "feature-quran", "src", "main", "res")
CACHE_PATH = os.path.join("app", "build", "localize_cache.json")

DEAD = {
    "quran_download_reciter_title",
    "quran_download_options_title",
    "quran_download_current_ayah",
    "quran_download_current_surah",
    "quran_downloaded",
}

TITLE_EN = "Total downloaded"
SUMMARY_EN = "%1$d surahs \u00b7 %2$d ayahs \u00b7 %3$s"

# The token-protected forms used as cache keys.
def protect(text):
    toks = []
    counter = 0

    def repl(m):
        nonlocal counter
        tok = "9970%04d" % counter
        toks.append(m.group(0))
        counter += 1
        return tok

    return re.sub(r"(%\d+\$[ds]|%%|%[ds]|\\n|\\'|\\\")", repl, text), toks

TITLE_PROTECTED, _ = protect(TITLE_EN)
SUMMARY_PROTECTED, _ = protect(SUMMARY_EN)

# --- 1) revert every locale change in feature-quran ---
subprocess.run(
    ["git", "checkout", "--", os.path.join(RES)],
    check=True, cwd=".",
)
print("reverted:", RES)

# --- 2) remove dead strings ---
for p in sorted(glob.glob(os.path.join(RES, "values*", "strings.xml"))):
    with open(p, "r", encoding="utf-8", newline="") as f:
        text = f.read()
    newline = "\r\n" if "\r\n" in text else "\n"
    lines = text.split(newline)
    out = [ln for ln in lines
           if not (re.search(r'<string name="([^"]+)"', ln) and re.search(r'<string name="([^"]+)"', ln).group(1) in DEAD)]
    if len(out) != len(lines):
        with open(p, "w", encoding="utf-8", newline="") as f:
            f.write(newline.join(out))
print("dead strings removed")

# --- 3) add AR + EN sources ---
def add_after(path, anchor, additions):
    with open(path, "r", encoding="utf-8", newline="") as f:
        text = f.read()
    newline = "\r\n" if "\r\n" in text else "\n"
    idx = text.find(anchor)
    if idx < 0:
        print("MISSING anchor in", path)
        return False
    text = text[: idx + len(anchor)] + newline + additions + text[idx + len(anchor):]
    with open(path, "w", encoding="utf-8", newline="") as f:
        f.write(text)
    return True

add_after(
    os.path.join(RES, "values", "strings.xml"),
    '    <string name="quran_downloads_active_title">\u0627\u0644\u062a\u0646\u0632\u064a\u0644\u0627\u062a \u0627\u0644\u062d\u0627\u0644\u064a\u0629 \u0648\u0627\u0644\u0633\u0627\u0628\u0642\u0629</string>',
    '    <string name="quran_downloads_summary_title">\u0625\u062c\u0645\u0627\u0644\u064a \u0627\u0644\u0645\u062d\u0645\u0651\u0644</string>\n'
    '    <string name="quran_downloads_summary">%1$d \u0633\u0648\u0631\u0629 \u00b7 %2$d \u0622\u064a\u0629 \u00b7 %3$s</string>',
)
add_after(
    os.path.join(RES, "values-en", "strings.xml"),
    '    <string name="quran_downloads_active_title">Current and past downloads</string>',
    '    <string name="quran_downloads_summary_title">Total downloaded</string>\n'
    '    <string name="quran_downloads_summary">%1$d surahs \u00b7 %2$d ayahs \u00b7 %3$s</string>',
)
print("sources added")

# --- 4) patch every locale from the cache ---
cache = json.load(open(CACHE_PATH, encoding="utf-8"))

def xml_escape(text):
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    text = text.replace("'", "\\'")
    text = text.replace("\n", "\\n")
    return text

def validate_title(v):
    return isinstance(v, str) and v.strip() and "9970" not in v

def validate_summary(v):
    if not isinstance(v, str) or not v.strip() or "9970" in v:
        return False
    toks = set(re.findall(r"%\d+\$[ds]", v))
    return toks == {"%1$d", "%2$d", "%3$s"}

AR_TITLE = "\u0625\u062c\u0645\u0627\u0644\u064a \u0627\u0644\u0645\u062d\u0645\u0651\u0644"
AR_SUMMARY = "%1$d \u0633\u0648\u0631\u0629 \u00b7 %2$d \u0622\u064a\u0629 \u00b7 %3$s"

patched = 0
fallback = []
for p in sorted(glob.glob(os.path.join(RES, "values-*", "strings.xml"))):
    lang = os.path.basename(os.path.dirname(p))[len("values-"):]
    if lang == "en":
        continue
    title_v = cache.get("%s|%s" % (lang, TITLE_PROTECTED))
    summary_v = cache.get("%s|%s" % (lang, SUMMARY_PROTECTED))
    title_ok = validate_title(title_v)
    summary_ok = validate_summary(summary_v)
    if not title_ok or not summary_ok:
        fallback.append(lang)
        title_v = AR_TITLE if not title_ok else title_v
        summary_v = AR_SUMMARY if not summary_ok else summary_v
    with open(p, "r", encoding="utf-8", newline="") as f:
        text = f.read()
    newline = "\r\n" if "\r\n" in text else "\n"
    anchor = '    <string name="quran_downloads_active_title">'
    idx = text.find(anchor)
    if idx < 0:
        print("anchor missing in", p)
        continue
    end = text.find("</string>", idx) + len("</string>")
    additions = (
        newline
        + '    <string name="quran_downloads_summary_title">%s</string>' % xml_escape(title_v)
        + newline
        + '    <string name="quran_downloads_summary">%s</string>' % xml_escape(summary_v)
    )
    text = text[:end] + additions + text[end:]
    with open(p, "w", encoding="utf-8", newline="") as f:
        f.write(text)
    patched += 1

print("patched locales:", patched)
print("fallback to Arabic:", len(fallback), fallback[:20])
