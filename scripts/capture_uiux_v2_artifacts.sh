#!/usr/bin/env bash
# Pull completed files while instrumentation runs: AGP uninstalls the app afterwards.
set -u
mkdir -p artifacts/uiux-v2
app_id="$(sed -n 's/^muslim.applicationId=//p' gradle.properties | tr -d '\r')"
# Clock control is confined to disposable, root-capable CI emulators.
adb root
adb wait-for-device
[ "$(adb shell id -u | tr -d '\r')" = "0" ] || { echo "CI visual fixture needs a root-capable emulator"; exit 1; }
adb shell settings put global auto_time 0
adb shell settings put global auto_time_zone 0
adb shell setprop persist.sys.timezone UTC
./gradlew :app:connectedDebugAndroidTest --max-workers=2 '-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8' -Pandroid.testInstrumentationRunnerArguments.uiux.fixedClock=true > /tmp/uiux-v2-connected-tests.log 2>&1 &
gradle_pid=$!
trap 'kill "$gradle_pid" 2>/dev/null || true' INT TERM
while kill -0 "$gradle_pid" 2>/dev/null; do
    files="$(adb shell "find /sdcard/Android/data/$app_id/files/uiux-v2 -name '*.png' 2>/dev/null" | tr -d '\r')"
    while IFS= read -r device_screenshot; do
        [ -n "$device_screenshot" ] || continue
        name="${device_screenshot##*/}"
        [ -s "artifacts/uiux-v2/$name" ] && continue
        adb pull "$device_screenshot" "artifacts/uiux-v2/$name" || true
    done <<< "$files"
    sleep 1
done
wait "$gradle_pid"
gradle_status=$?
cat /tmp/uiux-v2-connected-tests.log
exit "$gradle_status"
