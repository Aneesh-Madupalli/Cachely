#!/usr/bin/env sh
# Cachely – local build script. Run: ./build.sh   or   sh build.sh

set -e
cd "$(dirname "$0")"

if [ -x ./gradlew ]; then
  ./gradlew assembleDebug --no-daemon
elif command -v gradle >/dev/null 2>&1; then
  gradle assembleDebug --no-daemon
else
  echo "No Gradle found. Run 'gradle wrapper' first or build from Android Studio."
  exit 1
fi

echo ""
echo "Build succeeded. APK: app/build/outputs/apk/debug/app-debug.apk"
