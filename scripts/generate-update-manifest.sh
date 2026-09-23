#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:?APK path is required}"
OUTPUT_PATH="${2:?Output path is required}"
CHANNEL="${3:-stable}"
TAG="${4:-}"

if [ ! -f "$APK_PATH" ]; then
  echo "APK not found: $APK_PATH" >&2
  exit 1
fi

AAPT="$(find "${ANDROID_HOME:-$ANDROID_SDK_ROOT}/build-tools" -type f -name aapt 2>/dev/null | sort -V | tail -1)"
if [ -z "$AAPT" ] || [ ! -x "$AAPT" ]; then
  echo "Unable to locate Android aapt in the configured SDK." >&2
  exit 1
fi

BADGING="$("$AAPT" dump badging "$APK_PATH")"
PACKAGE_LINE="$(printf '%s\n' "$BADGING" | grep -m1 "^package:")"
PACKAGE_NAME="$(printf '%s\n' "$PACKAGE_LINE" | sed -n "s/.*name='\([^']*\)'.*/\1/p")"
VERSION_CODE="$(printf '%s\n' "$PACKAGE_LINE" | sed -n "s/.*versionCode='\([^']*\)'.*/\1/p")"
VERSION_NAME="$(printf '%s\n' "$PACKAGE_LINE" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p")"
MIN_SDK="$(printf '%s\n' "$BADGING" | sed -n "s/^sdkVersion:'\([^']*\)'.*/\1/p" | head -1)"
SHA256="$(sha256sum "$APK_PATH" | awk '{print $1}')"
ASSET_NAME="$(basename "$APK_PATH")"
SIZE_BYTES="$(stat -c '%s' "$APK_PATH")"

for required in PACKAGE_NAME VERSION_CODE VERSION_NAME SHA256 ASSET_NAME SIZE_BYTES; do
  if [ -z "${!required}" ]; then
    echo "Unable to derive $required from $APK_PATH" >&2
    exit 1
  fi
done

mkdir -p "$(dirname "$OUTPUT_PATH")"

PACKAGE_NAME="$PACKAGE_NAME" \
VERSION_CODE="$VERSION_CODE" \
VERSION_NAME="$VERSION_NAME" \
MIN_SDK="$MIN_SDK" \
SHA256="$SHA256" \
ASSET_NAME="$ASSET_NAME" \
SIZE_BYTES="$SIZE_BYTES" \
CHANNEL="$CHANNEL" \
TAG="$TAG" \
OUTPUT_PATH="$OUTPUT_PATH" \
python3 - <<'PY'
import json
import os
from pathlib import Path

payload = {
    "schemaVersion": 1,
    "packageName": os.environ["PACKAGE_NAME"],
    "versionName": os.environ["VERSION_NAME"],
    "versionCode": int(os.environ["VERSION_CODE"]),
    "minSdk": int(os.environ["MIN_SDK"]) if os.environ.get("MIN_SDK", "").isdigit() else 0,
    "channel": os.environ["CHANNEL"],
    "tag": os.environ.get("TAG", ""),
    "apk": {
        "asset": os.environ["ASSET_NAME"],
        "sizeBytes": int(os.environ["SIZE_BYTES"]),
        "sha256": os.environ["SHA256"].lower(),
    },
}
Path(os.environ["OUTPUT_PATH"]).write_text(
    json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
    encoding="utf-8",
)
PY

echo "Generated $OUTPUT_PATH"
cat "$OUTPUT_PATH"
