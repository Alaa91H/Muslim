#!/usr/bin/env bash
# Production release: verifies content/signing gates, commits everything, tags,
# pushes, waits for the tag-triggered CI build, verifies APK + AAB artifacts,
# and publishes a GitHub Release with a generated changelog.
#
# The APK version is derived from the tag (app/build.gradle.kts reads
# `git describe` at build time), never hardcoded. The tag push triggers the
# `release-apk` CI job, which signs with the same stable key as local builds
# (secrets from scripts/setup-github-signing.sh), so users install updates
# over existing installs without uninstalling.
#
# Usage:
#   ./scripts/release.sh            # commit all changes, bump patch: v1.2.0 -> v1.3.0
#   ./scripts/release.sh 2.0.0      # commit all changes, release v2.0.0
#
# Requires the GitHub CLI (https://cli.github.com) authenticated to the repo.
set -euo pipefail

REPO="${GITHUB_REPOSITORY:-Alaa91H/Muslim}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if ! command -v gh >/dev/null 2>&1; then
    echo "gh CLI not found. Install it from https://cli.github.com" >&2
    exit 1
fi
gh auth status >/dev/null 2>&1 || { echo "gh is not authenticated. Run: gh auth login" >&2; exit 1; }

git fetch --tags --quiet

latest="$(git describe --tags --match 'v*' --abbrev=0 2>/dev/null || echo "v0.0.0")"
latest="${latest#v}"

next="${1:-}"
if [ -z "$next" ]; then
    IFS=. read -r major minor patch <<< "$latest"
    next="$major.$minor.$((patch + 1))"
fi

tag="v$next"
if git rev-parse -q --verify "refs/tags/$tag" >/dev/null 2>&1; then
    echo "Tag $tag already exists." >&2
    exit 1
fi

# --- 1. Stop before creating a tag unless production gates are truly ready. ---
# This command intentionally fails while content approvals or the stable signing
# identity are incomplete; a tag must never be used as a speculative test.
./gradlew :app:verifyProductionRelease --stacktrace

# --- 2. Commit everything (so the release captures the full working tree). ---
if ! git diff --quiet || ! git diff --cached --quiet || [ -n "$(git ls-files --others --exclude-standard)" ]; then
    git add -A
    git -c core.hooksPath=/dev/null commit -m "Release $tag

$(git log --oneline -1 --no-decorate 2>/dev/null || true)" >/dev/null 2>&1 || true
    echo "Committed all working-tree changes for $tag."
else
    echo "Working tree is clean — nothing extra to commit."
fi

# --- 3. Generate the changelog from commits since the previous tag. ---
changelog="$(git log --oneline --no-decorate "v$latest"..HEAD 2>/dev/null \
    | sed 's/^/- /' | head -80 || true)"
if [ -z "$changelog" ]; then
    changelog="- Initial release."
fi

# --- 4. Tag and push. The tag push triggers the release-apk CI job. ---
echo "Latest: v$latest -> releasing: $tag"
git tag -a "$tag" -m "Release $tag"
git push origin HEAD:main
git push origin "$tag"

sha="$(git rev-parse HEAD)"

# --- 5. Wait for the build of THIS commit (the tag push). ---
# The tag-triggered run appears with the tag as its ref; filter by head sha so
# we never watch a stale/parallel run.
echo "Waiting for the release build of $tag ($sha) ..."
run_id=""
for _ in $(seq 1 60); do
    run_id="$(gh run list --workflow=ci.yml --limit 50 --json databaseId,headSha,event --jq ".[] | select(.headSha == \"$sha\") | .databaseId" | head -1)"
    [ -n "$run_id" ] && break
    sleep 10
done
if [ -z "$run_id" ]; then
    echo "No CI run found for $sha. Check https://github.com/$REPO/actions" >&2
    exit 1
fi
gh run watch --exit-status "$run_id"

# --- 6. Download and verify the signed APK and production App Bundle. ---
rm -rf /tmp/muslim-release
gh run download --repo "$REPO" --name muslim-release-apk --dir /tmp/muslim-release "$run_id"
apk="$(find /tmp/muslim-release -name '*.apk' | head -1)"
aab="$(find /tmp/muslim-release -name '*.aab' | head -1)"
[ -n "$apk" ] || { echo "No APK artifact found." >&2; exit 1; }
[ -n "$aab" ] || { echo "No AAB artifact found." >&2; exit 1; }
printf 'Verified release artifacts:\n- APK: %s\n- AAB: %s\n' "$apk" "$aab"

echo "Verifying APK signature: $apk"
bt="$(ls -d "$LOCALAPPDATA/Android/Sdk/build-tools/"* 2>/dev/null | sort -V | tail -1 || true)"
apksigner=""
for cand in "$bt/apksigner.bat" "$bt/apksigner" $(command -v apksigner 2>/dev/null); do
    [ -n "$cand" ] && [ -x "$cand" ] && apksigner="$cand" && break
done
if [ -n "$apksigner" ]; then
    certs="$("$apksigner" verify --print-certs "$apk" 2>/dev/null || true)"
    if echo "$certs" | grep -qi "Android Debug"; then
        echo "ERROR: Production artifact is debug-signed; refusing to publish." >&2
        exit 1
    else
        echo "APK signature verified (release key):"
        echo "$certs" | grep -i "DN:" | head -2
    fi
else
    echo "apksigner not found — skipping signature check." >&2
fi

# --- 7. Publish the GitHub Release with the changelog. ---
# Two-phase so a slow ~77 MB APK upload can NEVER leave a release without
# its asset: create the release first (fast), then upload + verify + publish
# through scripts/upload_release_asset.sh (retries + byte-size verification).
gh release create "$tag" \
    --repo "$REPO" \
    --draft \
    --title "Muslim $tag" \
    --notes "## Muslim $tag

Built from the $tag tag, so the in-app version matches exactly and updates install over existing installs (stable signing key).

### Changes since v$latest
$changelog

### Install
- Download the APK and open it (allow \"install from unknown sources\" if prompted).
- The app stays signed with the same key across releases, so updates install directly over previous versions.

### Support
- GitHub: https://github.com/Alaa91H
- Email: alahus2591@gmail.com
- Telegram: https://t.me/Alaa91h
- Ko-fi: https://ko-fi.com/alaa91h"

"$ROOT/scripts/upload_release_asset.sh" "$tag" "$apk" --publish

echo "Published release $tag: https://github.com/$REPO/releases/tag/$tag"
