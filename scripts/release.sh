#!/usr/bin/env bash
# Creates a release the "tag-first" way so the APK version is derived from the
# tag, never hardcoded: app/build.gradle.kts reads `git describe` at build time.
#
# Flow:
#   1. Tag the release commit and push the tag (and main).
#   2. The tag push triggers the `release-apk` CI job, which builds with that
#      exact versionName/versionCode (stable signature).
#   3. Download the CI artifact and publish a GitHub Release with it attached.
#
# Usage:
#   ./scripts/release.sh            # bump patch: v1.2.0 -> v1.3.0
#   ./scripts/release.sh 2.0.0      # explicit version
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

if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Working tree is dirty — commit or stash changes before releasing." >&2
    exit 1
fi

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

echo "Latest: v$latest -> releasing: $tag"

git tag -a "$tag" -m "Release $tag"
# Push both so CI checks out the tagged commit with the tag reachable.
git push origin HEAD:main
git push origin "$tag"

sha="$(git rev-parse HEAD)"
echo "Waiting for the release build of $tag ($sha) ..."
gh run watch --exit-status "$(gh run list --workflow=ci.yml --branch main --limit 1 --json databaseId --jq '.[0].databaseId')"

rm -rf /tmp/muslim-release
gh run download --name muslim-release-apk --dir /tmp/muslim-release
apk="$(find /tmp/muslim-release -name '*.apk' | head -1)"
[ -n "$apk" ] || { echo "No APK artifact found." >&2; exit 1; }

gh release create "$tag" "$apk" \
    --title "Muslim $tag" \
    --notes "Release $tag — built from the tag, so the in-app version matches exactly and updates install over existing installs with the stable signing key."

echo "Published release $tag: https://github.com/$REPO/releases/tag/$tag"
