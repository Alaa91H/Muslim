#!/usr/bin/env bash
# Uploads the project signing key to GitHub Actions secrets so the CI release
# workflow signs release APKs with the same stable key as local builds. This is
# what lets users install updates on top of each other without uninstalling.
#
# Usage:
#   ./scripts/setup-github-signing.sh
#
# Requires the GitHub CLI (https://cli.github.com) authenticated to the repo.
set -euo pipefail

REPO="${GITHUB_REPOSITORY:-Alaa91H/Muslim}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if ! command -v gh >/dev/null 2>&1; then
    echo "gh CLI not found. Install it from https://cli.github.com" >&2
    exit 1
fi
gh auth status >/dev/null 2>&1 || {
    echo "gh is not authenticated. Run: gh auth login" >&2
    exit 1
}

if [ ! -f "$ROOT/keystore.properties" ]; then
    echo "keystore.properties not found. Generate it first with:" >&2
    echo "  ./scripts/create-signing-keystore.sh" >&2
    exit 1
fi

# Read the local keystore.properties (kept out of git on purpose).
STORE_FILE="$(grep -E '^storeFile=' "$ROOT/keystore.properties" | cut -d= -f2-)"
STORE_PASS="$(grep -E '^storePassword=' "$ROOT/keystore.properties" | cut -d= -f2-)"
KEY_ALIAS="$(grep -E '^keyAlias=' "$ROOT/keystore.properties" | cut -d= -f2-)"
KEY_PASS="$(grep -E '^keyPassword=' "$ROOT/keystore.properties" | cut -d= -f2-)"

if [ ! -f "$ROOT/$STORE_FILE" ]; then
    echo "Keystore file not found: $ROOT/$STORE_FILE" >&2
    exit 1
fi

base64 "$ROOT/$STORE_FILE" | tr -d '\n' > /tmp/keystore.b64

gh secret set SIGNING_KEYSTORE --repo "$REPO" < /tmp/keystore.b64
gh secret set SIGNING_STORE_PASSWORD --repo "$REPO" --body "$STORE_PASS"
gh secret set SIGNING_KEY_ALIAS --repo "$REPO" --body "$KEY_ALIAS"
gh secret set SIGNING_KEY_PASSWORD --repo "$REPO" --body "$KEY_PASS"

rm -f /tmp/keystore.b64
echo "Signing secrets configured for $REPO"
echo "CI will now build release APKs with the same signature as local builds."
