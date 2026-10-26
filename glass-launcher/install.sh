#!/bin/sh
# Build the Glass launcher from source, install it on the Fire TV and make it the home screen.
# A copy built here is signed with your own debug key, so it can't install updates from GitHub
# Releases; for those, install glass.apk from the latest release instead (see the README).
#   ./install.sh             build + install + set as default home + disable Amazon launcher
#   ./install.sh --no-home   build + install + open it, leave the default home alone
#   --updates / --no-updates  turn Glass's daily update check on or off (asks if neither is given;
#                             without a terminal to ask in, it's turned off)
# Needs: JDK 17+, Android SDK (sdk.dir in launcher/local.properties), Python venv in .venv
# (see README). TV address: .tv-ip file or TV_IP env var.
set -e
HOME_MODE=1 UPDATES=
for arg in "$@"; do
  case "$arg" in
    --no-home) HOME_MODE= ;;
    --updates) UPDATES=true ;;
    --no-updates) UPDATES=false ;;
    *) echo "Unknown option: $arg"; exit 1 ;;
  esac
done
DIR="$(cd "$(dirname "$0")" && pwd)"
TV="$DIR/tools/tv"
PKG=com.ivantrj.glasslauncher
APK="$DIR/launcher/app/build/outputs/apk/debug/app-debug.apk"

echo "== Building"
(cd "$DIR/launcher" && ./gradlew -q assembleDebug)

echo "== Pushing APK (base64 chunks, md5 verified)"
"$TV" push "$APK" /data/local/tmp/glass.apk

echo "== Installing"
OUT=$("$TV" shell "pm install -r -g /data/local/tmp/glass.apk" 2>&1) || true
echo "$OUT"
case "$OUT" in
  *Success*) ;;
  *INSTALL_FAILED_UPDATE_INCOMPATIBLE*)
    echo "The Glass on the TV is signed with a different key (for example the glass.apk release)."
    echo "Uninstall it first ($TV shell 'pm uninstall $PKG'; its settings start fresh), then run this again."
    exit 1 ;;
  *) echo "Install failed."; exit 1 ;;
esac
"$TV" shell "rm /data/local/tmp/glass.apk"
# Android 11 only shows the Wi-Fi name to apps with location permission (-g grants it, this makes sure)
"$TV" shell "pm grant $PKG android.permission.ACCESS_FINE_LOCATION" || true

# Lets Glass install its own updates (Fire OS still asks before each one)
"$TV" shell "appops set $PKG REQUEST_INSTALL_PACKAGES allow" || true

if [ -n "$HOME_MODE" ]; then
  echo "== Setting default home"
  "$TV" shell "cmd package set-home-activity $PKG/.HomeActivity"
  # One pm command per shell call: chained pm commands can silently stop after the first.
  # Fire OS 8 refuses this ("protected package"); the accessibility service below covers it.
  if "$TV" shell "pm disable-user --user 0 com.amazon.tv.launcher 2>&1" | grep -q "new state: disabled"; then
    echo "Amazon launcher disabled"
  else
    echo "Amazon launcher is protected on this Fire OS build (expected); using the Home redirect"
  fi

  echo "== Enabling the Home button redirect (accessibility service)"
  SVC="$PKG/$PKG.HomeButtonService"
  CUR=$("$TV" shell "settings get secure enabled_accessibility_services" | tr -d '\r\n')
  case "$CUR" in
    *"$SVC"*) ;;
    null|"") "$TV" shell "settings put secure enabled_accessibility_services $SVC" ;;
    *) "$TV" shell "settings put secure enabled_accessibility_services '$CUR:$SVC'" ;;
  esac
  "$TV" shell "settings put secure accessibility_enabled 1"
fi

# This copy is signed with your own key, so it can't install releases from GitHub: checks only
# tell you a new version exists (then install glass.apk from the README). Off unless you say yes.
if [ -z "$UPDATES" ]; then
  UPDATES=false
  if [ -t 0 ]; then
    echo "Glass can check GitHub once a day and tell you when a new version is out. This copy was built"
    echo "here, so it can't install those itself: you'd install glass.apk from the README instead."
    printf "Turn on update checks? [y/N] "
    read -r answer || answer=
    case "$answer" in [yY]*) UPDATES=true ;; esac
  fi
fi
"$TV" shell "am start -n $PKG/.HomeActivity --ez updateChecks $UPDATES" >/dev/null
if [ "$UPDATES" = true ]; then
  echo "Update checks on (change in Glass: Settings > Updates)"
else
  echo "Update checks off (turn on in Glass: Settings > Updates, or run again with --updates)"
fi
echo "Done."
