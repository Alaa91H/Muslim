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
fixed_clock_original_device_epoch=""
fixed_clock_started_host_epoch=""
fixed_screenshot_epoch=""
fixed_screenshot_window_end=""
fixed_screenshot_date='093015002026.00'
restore_fixed_clock() {
    [ -n "$fixed_clock_original_device_epoch" ] || return 0
    local now_host_epoch restore_epoch restore_date
    now_host_epoch="$(date -u +%s)"
    restore_epoch=$((fixed_clock_original_device_epoch + now_host_epoch - fixed_clock_started_host_epoch))
    restore_date="$(date -u -d "@$restore_epoch" +%m%d%H%M%Y.%S)"
    adb shell "su 0 date -u $restore_date" >/dev/null 2>&1 || true
    fixed_clock_original_device_epoch=""
    fixed_clock_started_host_epoch=""
}
start_fixed_clock() {
    fixed_clock_original_device_epoch="$(adb shell date -u +%s | tr -d '\r')"
    fixed_clock_started_host_epoch="$(date -u +%s)"
    [[ "$fixed_clock_original_device_epoch" =~ ^[0-9]+$ ]] || return 1
    fixed_screenshot_epoch="$(date -u -d '2026-09-30T15:00:00Z' +%s)"
    fixed_screenshot_window_end=$((fixed_screenshot_epoch + 24 * 60 * 60))
    adb shell su 0 date -u "$fixed_screenshot_date"
}
ensure_fixed_clock() {
    [ -n "$fixed_screenshot_epoch" ] || return 0
    local device_epoch
    device_epoch="$(adb shell date -u +%s | tr -d '\r')"
    if [[ "$device_epoch" =~ ^[0-9]+$ ]] &&
        ((device_epoch >= fixed_screenshot_epoch && device_epoch < fixed_screenshot_window_end)); then
        return 0
    fi
    adb shell su 0 date -u "$fixed_screenshot_date" || return 1
    device_epoch="$(adb shell date -u +%s | tr -d '\r')"
    if [[ ! "$device_epoch" =~ ^[0-9]+$ ]] ||
        ((device_epoch < fixed_screenshot_epoch || device_epoch >= fixed_screenshot_window_end)); then
        echo "Screenshot clock revalidation failed: expected $fixed_screenshot_epoch..$fixed_screenshot_window_end, observed $device_epoch"
        return 1
    fi
    return 0
}
reboot_emulator_between_batches() {
    echo "Rebooting the emulator to release AndroidTest and graphics state between screenshot groups."
    adb reboot || return 1
    timeout 180 adb wait-for-device || return 1
    local attempt boot_state=""
    for attempt in $(seq 1 60); do
        boot_state="$(timeout 10 adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
        [ "$boot_state" = 1 ] && break
        sleep 2
    done
    [ "$boot_state" = 1 ] || { echo "Emulator did not finish booting between screenshot groups."; return 1; }
    local root_state=""
    for attempt in $(seq 1 15); do
        timeout 5 adb root >/dev/null 2>&1 || true
        timeout 5 adb wait-for-device || true
        root_state="$(timeout 5 adb shell id -u 2>/dev/null | tr -d '\r')"
        [ "$root_state" = 0 ] && break
        sleep 1
    done
    [ "$root_state" = 0 ] || { echo "Emulator root was not restored after reboot."; return 1; }
    adb shell settings put global auto_time 0 || return 1
    adb shell settings put global auto_time_zone 0 || return 1
    adb shell setprop persist.sys.timezone UTC || return 1
    ensure_fixed_clock || return 1
    kill "$logcat_pid" 2>/dev/null || true
    adb logcat -b all -v threadtime '*:E' >> artifacts/emulator-diagnostics/logcat.txt 2>&1 &
    logcat_pid=$!
}
trap 'restore_fixed_clock; kill "$logcat_pid" ${gradle_pid:+"$gradle_pid"} 2>/dev/null || true' EXIT INT TERM
status=0
device_ready() {
    [ "$(adb get-state 2>/dev/null)" = device ]
}
pull_screenshots() {
    local files device_screenshot name
    files="$(adb shell "find /sdcard/Android/data/$app_id/files/uiux-v2 -name '*.png' 2>/dev/null" | tr -d '\r')"
    while IFS= read -r device_screenshot; do
        [ -n "$device_screenshot" ] || continue
        name="${device_screenshot##*/}"
        if [ -s "artifacts/uiux-v2/$name" ] || adb pull "$device_screenshot" "artifacts/uiux-v2/$name"; then
            [ -s "artifacts/uiux-v2/$name" ] || continue
        fi
    done <<< "$files"
}
run_batch() {
    local batch="$1"
    shift
    if ! ensure_fixed_clock; then
        status=1
        return 1
    fi
    free -m >> artifacts/emulator-diagnostics/host-memory.txt
    ./gradlew :app:connectedDebugAndroidTest --max-workers=2 \
        '-Dorg.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US' \
        '-Pkotlin.daemon.jvmargs=-Xmx1024m -Duser.language=en -Duser.country=US' \
        "$@" \
        > "artifacts/emulator-diagnostics/$batch.txt" 2>&1 &
    gradle_pid=$!
    while kill -0 "$gradle_pid" 2>/dev/null; do
        pull_screenshots
        sleep 1
    done
    wait "$gradle_pid"
    local result=$?
    gradle_pid=""
    cat "artifacts/emulator-diagnostics/$batch.txt"
    mkdir -p "artifacts/emulator-diagnostics/test-results/$batch"
    cp -a app/build/outputs/androidTest-results/connected/debug/. \
        "artifacts/emulator-diagnostics/test-results/$batch/" 2>/dev/null || true
    device_ready && pull_screenshots
    free -m >> artifacts/emulator-diagnostics/host-memory.txt
    [ "$result" = 0 ] || status="$result"
    device_ready || status=1
}
set_display_variant() {
    local expanded="$1"
    if [ "$expanded" = true ]; then
        # API 26's CI AVD is capped at 640x1280 pixels. At 120 dpi this is
        # still 853dp wide, enough to exercise the expanded-layout breakpoint.
        adb shell wm size 640x1280 >/dev/null
        adb shell wm density 120 >/dev/null
        local applied_size applied_density
        applied_size="$(adb shell wm size | tr -d '\r')"
        applied_density="$(adb shell wm density | tr -d '\r')"
        if [[ "$applied_size" != *"Override size: 640x1280"* ]] ||
            [[ "$applied_density" != *"Override density: 120"* ]]; then
            echo "Could not configure expanded emulator display: size=$applied_size density=$applied_density"
            return 1
        fi
    else
        adb shell wm size reset >/dev/null
        adb shell wm density reset >/dev/null
    fi
}
# Behavioral regressions target the compact phone layout. Run them before the
# screenshot matrix because screenshot variants temporarily change wall clock
# and can invalidate later RTC_WAKEUP delivery probes.
if set_display_variant false; then
    run_batch app-regression \
        -Pandroid.testInstrumentationRunnerArguments.notClass=org.muslim.app.UiUxV2MatrixInstrumentedTest
else
    status=1
fi

# Set the deterministic date before the matrix and revalidate it once per
# Gradle batch. Avoid per-capture jumps, which trigger Android 16 time-usage
# churn and can destabilize the ADB transport.
# Reinstall between 24-case width/screen groups to bound retained Activity/graphics state.
# Every configured case still runs; failures remain failures and are not retried away.
if [ "$status" = 0 ] && device_ready; then
    start_fixed_clock || status=1
    if [ "$status" = 0 ]; then
        screenshot_batch_started=false
        for screens in prayer-home,prayer-monthly quran-home,quran-reader qibla,more hadith,settings; do
            for expanded in false true; do
                if [ "$screenshot_batch_started" = true ]; then
                    reboot_emulator_between_batches || { status=1; break 2; }
                fi
                screenshot_batch_started=true
                set_display_variant "$expanded" || { status=1; break 2; }
                run_batch "matrix-$screens-$expanded" \
                    -Pandroid.testInstrumentationRunnerArguments.class=org.muslim.app.UiUxV2MatrixInstrumentedTest \
                    "-Pandroid.testInstrumentationRunnerArguments.uiux.screens=$screens" \
                    "-Pandroid.testInstrumentationRunnerArguments.uiux.expanded=$expanded" \
                    -Pandroid.testInstrumentationRunnerArguments.uiux.fixedClock=true \
                    -Pandroid.testInstrumentationRunnerArguments.uiux.fixedClockManaged=true
                device_ready || break 2
            done
        done
    fi
    restore_fixed_clock
fi
sudo dmesg -T > artifacts/emulator-diagnostics/kernel.txt 2>&1 || true
exit "$status"
