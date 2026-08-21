#!/usr/bin/env bash
# Generates CHANGELOG.md from git tags and commit messages.
#
#   ./scripts/generate_changelog.sh [repo_path]
#
# Every v* release tag becomes a section; the commits reachable from that tag
# (but not from the previous tag) are listed underneath. Untagged work on the
# current branch is appended as an "Unreleased" section.
set -euo pipefail

REPO="${1:-$(git rev-parse --show-toplevel)}"
cd "$REPO"

OUT="$REPO/CHANGELOG.md"
TAGS=$(git tag --list 'v*' --sort=-v:refname || true)

if [ -z "$TAGS" ]; then
  echo "No v* tags found — nothing to generate." >&2
  exit 1
fi

cat > "$OUT" <<'EOF'
# Changelog

All notable changes to this project are documented here, grouped by release
tag. Generated automatically from the commit history by
`scripts/generate_changelog.sh`.

The format is based on the project's release tags; each entry lists the
commits that landed in that release.

EOF

PREV=""
for TAG in $TAGS; do
  {
    echo "## $TAG"
    echo ""
    if [ -n "$PREV" ]; then
      git log --oneline --no-merges "$TAG" "^$PREV"
    else
      git log --oneline --no-merges "$TAG"
    fi
    echo ""
  } >> "$OUT"
  PREV="$TAG"
done

# Unreleased: commits on the current branch after the newest tag.
HEAD_TAG=$(git tag --list 'v*' --sort=-v:refname | head -1)
if [ -n "$HEAD_TAG" ]; then
  UNRELEASED=$(git log --oneline --no-merges HEAD "^$HEAD_TAG" 2>/dev/null || true)
  if [ -n "$UNRELEASED" ]; then
    {
      echo "## Unreleased"
      echo ""
      echo "$UNRELEASED"
      echo ""
    } >> "$OUT"
  fi
fi

echo "Wrote $OUT ($(wc -l < "$OUT") lines)"
