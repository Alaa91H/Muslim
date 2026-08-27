#!/usr/bin/env python3
"""Static guardrails for GPS, prayer-time Isha and live notification semantics."""

from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOCATION_SCREEN = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/location/LocationScreen.kt"
LOCATION_VM = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/location/LocationViewModel.kt"
FUSED_PROVIDER = ROOT / "core/core-location/src/main/java/org/muslim/app/core/location/FusedLocationProvider.kt"
GEOCODER_RESOLVER = ROOT / "core/core-location/src/main/java/org/muslim/app/core/location/GeocoderRegionNameResolver.kt"
ADHAN_SOUND_PLAYER = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanSoundPlayer.kt"
SETTINGS = ROOT / "core/core-datastore/src/main/java/org/muslim/app/core/datastore/prayer/PrayerSettings.kt"
SETTINGS_PARAMETERS = ROOT / "core/core-datastore/src/main/java/org/muslim/app/core/datastore/prayer/PrayerSettingsParameters.kt"
METHOD = ROOT / "core/core-common/src/main/java/org/muslim/app/core/common/prayer/CalculationMethod.kt"
CALCULATOR_TEST = ROOT / "feature/feature-prayer-times/src/test/java/org/muslim/app/feature/prayertimes/domain/PrayerTimesCalculatorTest.kt"
NOTIFICATIONS = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/NextAdhanNotifications.kt"
NOTIFICATION_SERVICE = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/NextAdhanService.kt"
HOME = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeScreen.kt"
HOME_DIALOG = ROOT / "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeAdhanCustomizationDialog.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    location_screen = LOCATION_SCREEN.read_text(encoding="utf-8")
    location_vm = LOCATION_VM.read_text(encoding="utf-8")
    fused = FUSED_PROVIDER.read_text(encoding="utf-8")
    geocoder_resolver = GEOCODER_RESOLVER.read_text(encoding="utf-8")
    adhan_sound_player = ADHAN_SOUND_PLAYER.read_text(encoding="utf-8")
    settings = SETTINGS.read_text(encoding="utf-8")
    settings_parameters = SETTINGS_PARAMETERS.read_text(encoding="utf-8")
    method = METHOD.read_text(encoding="utf-8")
    calculator_test = CALCULATOR_TEST.read_text(encoding="utf-8")
    notifications = NOTIFICATIONS.read_text(encoding="utf-8")
    service = NOTIFICATION_SERVICE.read_text(encoding="utf-8")
    home = HOME.read_text(encoding="utf-8")
    home_dialog = HOME_DIALOG.read_text(encoding="utf-8")

    require("RequestMultiplePermissions" in location_screen, "GPS must request Android location permission pair")
    require("ACCESS_FINE_LOCATION" in location_screen and "ACCESS_COARSE_LOCATION" in location_screen, "GPS UI must accept precise or approximate permission")
    require("fineGranted || coarseGranted" in location_screen, "GPS entry cannot reject an approximate location grant")
    require("hasFine && !hasCoarse" in fused, "provider must only reject GPS when neither permission is present")
    require("Priority.PRIORITY_HIGH_ACCURACY" in fused, "precise GPS must request a high-accuracy current fix")
    require("awaitLastKnownLocation()" in fused and "mostRecentPlatformLocation()" in fused, "GPS must retain local fallback paths")
    require("private val client: FusedLocationProviderClient? by lazy" in fused, "fused client construction must remain recoverable")
    require("runCatching { LocationServices.getFusedLocationProviderClient(appContext) }.getOrNull()" in fused, "fused client construction must not escape the GPS failure boundary")
    require("private val platformLocationManager: LocationManager?" in fused and "getOrNull()" in fused, "platform location manager lookup must remain recoverable")
    require("TimeZone.getDefault" not in location_vm, "GPS must not assign the device timezone to coordinates")
    require("coordinateTimeZoneResolver.resolve(geo.latitude, geo.longitude)" in location_vm, "GPS must resolve a coordinate IANA zone")
    require("catch (error: CancellationException)" in location_vm, "GPS cancellation must propagate without treating it as an app failure")
    require("messages.value = Message.Error(\"gps_failed\")" in location_vm, "GPS provider failures must become a recoverable UI state")
    require("private suspend fun persistNow" in location_vm, "location persistence must remain behind one guarded path")
    require("runCatching" in geocoder_resolver and "geocoder.getFromLocation" in geocoder_resolver, "reverse geocoding must contain platform failures")
    require("writeTrackChunk" in adhan_sound_player, "synthesized Adhan must guard AudioTrack writes")
    require("AudioTrack.ERROR_INVALID_OPERATION" in adhan_sound_player, "invalidated AudioTrack writes must end safely")
    require("@Volatile" in adhan_sound_player, "AudioTrack session ownership must be visible to its writer thread")
    require("audioTrackLock" in adhan_sound_player, "AudioTrack native operations must share an invalidation lock")
    require("synchronized(audioTrackLock)" in adhan_sound_player, "AudioTrack write and release must be serialized")
    require("audioTrack = null\n            track?.let" in adhan_sound_player, "AudioTrack must invalidate ownership before native release")

    require("CalculationMethod.MuslimWorldLeague" in settings, "global prayer default must remain MWL")
    require("customIshaAngle: Double = 17.0" in settings, "custom Isha fallback must remain 17 degrees")
    require("ishaAngle = 17.0" in method, "MWL Isha must remain 17 degrees")
    require("PrayerParameters.of(method)" in settings_parameters, "all non-custom methods must use official parameter profiles")
    require("defaultProfile.ishaAngle).isEqualTo(17.0)" in calculator_test, "MWL Isha angle must have an explicit regression assertion")
    require("defaultProfile.userAdjustments[Prayer.Isha]).isEqualTo(0)" in calculator_test, "Isha must have no hidden user offset")
    require("GLOBAL_MWL_REFERENCE_CASES" in calculator_test and "Berlin late summer" in calculator_test, "Isha requires cross-region reference coverage")

    require("ForegroundColorSpan(upcomingTimeColor)" in notifications, "next prayer time must receive a foreground colour span")
    require("context.getColor(R.color.adhan_accent)" in notifications, "next prayer time must use the green Adhan accent")
    require("durationColor = MissedAdhanColors.DEFAULT" in notifications, "remaining and elapsed durations must use the red semantic colour")
    require("missedAdhanColor" not in service, "service must not pass a user-selected duration colour")
    require("HomeAdhanCustomizationDialog" in home, "home alert icon must open the modal customizer")
    require("onClick = { customizingPrayer = prayer }" in home, "home alert icon must not navigate to prayer settings")
    require("PrayerSettingsViewModel" in home_dialog and "saveAdhanCustomization" in home_dialog, "home modal must share the persisted customisation flow")

    print("Prayer GPS, Isha, AudioTrack, notification colour and direct-customisation contract verified.")


if __name__ == "__main__":
    main()
