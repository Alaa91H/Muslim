#!/usr/bin/env bash
# Export atomic screenshots before AGP uninstalls each bounded instrumentation batch.
set -u
mkdir -p artifacts/uiux-v2 artifacts/emulator-diagnostics
app_id="$(sed -n 's/^muslim.applicationId=//p' gradle.properties | tr -d '\r')"
adb root
adb wait-for-device
[ "$(adb shell id -u | tr -d '\r')" = "0" ] || { echo "CI visual fixture needs a root-capable emulator"; exit 1; }
adb shell settings put global auto_time 0
adb shell settings put global auto_time_zone 0
adb shell setprop persist.sys.timezone UTC
adb logcat -b all -v threadtime '*:E' > artifacts/emulator-diagnostics/logcat.txt 2>&1 &
logcat_pid=$!
gradle_pid=""
trap 'kill "$logcat_pid" ${gradle_pid:+"$gradle_pid"} 2>/dev/null || true' EXIT INT TERM
status=0
run_batch() {
    local batch="$1"
    shift
    free -m >> artifacts/emulator-diagnostics/host-memory.txt
    ./gradlew :app:connectedDebugAndroidTest --max-workers=2 \
        '-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US' \
        '-Pkotlin.daemon.jvmargs=-Xmx1024m -Duser.language=en -Duser.country=US' \
        -Pandroid.testInstrumentationRunnerArguments.uiux.fixedClock=true "$@" \
        > "artifacts/emulator-diagnostics/$batch.txt" 2>&1 &
    gradle_pid=$!
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
    local result=$?
    gradle_pid=""
    cat "artifacts/emulator-diagnostics/$batch.txt"
    mkdir -p "artifacts/emulator-diagnostics/test-results/$batch"
    cp -a app/build/outputs/androidTest-results/connected/debug/. \
        "artifacts/emulator-diagnostics/test-results/$batch/" 2>/dev/null || true
    free -m >> artifacts/emulator-diagnostics/host-memory.txt
    [ "$result" = 0 ] || status="$result"
}
# Reinstall between 48-case groups to bound retained Activity/graphics state.
# Every configured case still runs; failures remain failures and are not retried away.
for screens in prayer-home,prayer-monthly quran-home,quran-reader qibla,more hadith,settings; do
    run_batch "matrix-$screens" \
        -Pandroid.testInstrumentationRunnerArguments.class=org.muslim.app.UiUxV2MatrixInstrumentedTest \
        "-Pandroid.testInstrumentationRunnerArguments.uiux.screens=$screens"
    [ "$(adb get-state 2>/dev/null)" = device ] || break
done
if [ "$(adb get-state 2>/dev/null)" = device ]; then
    run_batch app-regression \
        -Pandroid.testInstrumentationRunnerArguments.notClass=org.muslim.app.UiUxV2MatrixInstrumentedTest
fi
sudo dmesg -T > artifacts/emulator-diagnostics/kernel.txt 2>&1 || true
exit "$status"
