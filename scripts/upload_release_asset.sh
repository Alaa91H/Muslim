#!/usr/bin/env bash
# Upload a release asset to a GitHub Release with automatic retries and a
# byte-size verification, then optionally publish the release.
#
# The APK is ~77 MB; on slow connections a single-shot `gh release create`
# or `gh release upload` can be interrupted (timeouts, network drops) and
# leave the release WITHOUT its asset — exactly why the APK kept "not
# appearing". This helper never declares success until the uploaded asset
# matches the local file's size, and retries a few times before giving up.
#
# Usage:
#   upload_release_asset.sh <tag> <file.apk> [--publish]
#
#   --publish  also flips the (draft) release to public after the asset
#              is verified, so users see the APK the moment it's live.
set -euo pipefail

REPO="${GITHUB_REPOSITORY:-Alaa91H/Muslim}"
TAG="$1"
FILE="$2"
PUBLISH="${3:-}"

[ -f "$FILE" ] || { echo "File not found: $FILE" >&2; exit 1; }
SIZE="$(stat -c %s "$FILE")"
BASE="$(basename "$FILE")"

echo "[upload] target: $REPO release $TAG"
echo "[upload] asset:  $BASE ($SIZE bytes)"

for attempt in 1 2 3 4 5; do
    echo "[upload] attempt $attempt/5: uploading $BASE ..."
    if gh release upload "$TAG" "$FILE" --repo "$REPO" --clobber; then
        # Verify: the remote asset must exist AND match the local byte size.
        # Note: `gh release view` (not the by-tag REST endpoint) is used
        # because the by-tag endpoint 404s while the release is a draft.
        got="$(gh release view "$TAG" --repo "$REPO" --json assets \
            --jq ".assets[] | select(.name == \"$BASE\") | .size" 2>/dev/null || true)"
        if [ -n "$got" ] && [ "$got" = "$SIZE" ]; then
            echo "[upload] OK: asset verified on GitHub ($got bytes)"
            if [ "$PUBLISH" = "--publish" ]; then
                gh release edit "$TAG" --repo "$REPO" --draft=false
                echo "[upload] published release $TAG"
            fi
            echo "[upload] https://github.com/$REPO/releases/tag/$TAG"
            exit 0
        fi
        echo "[upload] size mismatch (remote='${got:-none}', local=$SIZE) — retrying" >&2
    else
        echo "[upload] upload failed — retrying in 20s" >&2
    fi
    sleep 20
done

echo "[upload] FAILED after 5 attempts. Run this script again to resume." >&2
exit 1
