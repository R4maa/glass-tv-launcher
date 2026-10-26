#!/bin/sh
# Build, check and publish a Glass release (maintainer only; see RELEASING.md).
#   ./release.sh             build glass.apk, check its signature and the changelog, then publish
#   ./release.sh --dry-run   everything except publishing
# Needs: the release key (~/.glass-tv/keystore.properties), the Android SDK, and gh signed in.
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$DIR/.." && pwd)"
REPO=R4maa/glass-tv-launcher
# SHA-256 of the release certificate. Every release must match it, or users can't update.
CERT=660e149eb48e2b309434cea43f06b51daa9705cff149ac7b0fb846bcd9c6deb8

VERSION=$(sed -n "s/^ *versionName '\(.*\)'.*/\1/p" "$DIR/launcher/app/build.gradle")
[ -n "$VERSION" ] || { echo "No versionName in build.gradle"; exit 1; }
echo "== Glass $VERSION"

# Publish only what's committed and pushed, and tag exactly that commit
if [ "$1" != --dry-run ]; then
  [ -z "$(git -C "$ROOT" status --porcelain)" ] || { echo "Uncommitted changes: commit and push first."; exit 1; }
  git -C "$ROOT" fetch -q origin main
  [ "$(git -C "$ROOT" rev-parse HEAD)" = "$(git -C "$ROOT" rev-parse origin/main)" ]     || { echo "HEAD isn't origin/main: push (or pull) first."; exit 1; }
fi
COMMIT=$(git -C "$ROOT" rev-parse HEAD)

# The changelog section for this version, without its own heading (the release title says it)
NOTES="$ROOT/build-release-notes.md"
trap 'rm -f "$NOTES"' EXIT
awk -v v="$VERSION" '/^## / { on = index($0, "## " v " ") == 1; next } on' "$ROOT/CHANGELOG.md" > "$NOTES"
[ -s "$NOTES" ] || { echo "CHANGELOG.md has no '## $VERSION · <date>' section"; exit 1; }

echo "== Building"
cd "$DIR/launcher"
if [ -f gradlew.bat ] && [ "$OS" = Windows_NT ]; then ./gradlew.bat -q assembleRelease; else ./gradlew -q assembleRelease; fi
cp app/build/outputs/apk/release/app-release.apk "$ROOT/glass.apk"

echo "== Checking the signature"
SDK=$(sed -n 's/^sdk.dir=//p' local.properties)
APKSIGNER=$(ls -d "$SDK"/build-tools/*/ | sort -V | tail -1)apksigner
[ -f "$APKSIGNER" ] || APKSIGNER="$APKSIGNER.bat"
"$APKSIGNER" verify --print-certs "$ROOT/glass.apk" | grep -qi "SHA-256 digest: $CERT" \
  || { echo "glass.apk is NOT signed with the release key. Not publishing."; exit 1; }
echo "Signed with the release key"

if [ "$1" = --dry-run ]; then
  echo "Dry run: would publish v$VERSION with $ROOT/glass.apk and these notes:"; cat "$NOTES"
  exit 0
fi

echo "== Publishing v$VERSION"
gh release create "v$VERSION" "$ROOT/glass.apk" --repo "$REPO" --target "$COMMIT"   --title "Glass $VERSION" --notes-file "$NOTES"
