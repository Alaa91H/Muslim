package org.muslim.app.core.common.wear

import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity

/**
 * Versioned, minimal payload contract shared by the phone and paired Wear OS
 * app. The contract contains no location, prayer-calculation settings, audio,
 * account identifiers, or home-automation credentials.
 */
object WearSyncContract {
    /** Capability advertised by the Android phone app. */
    const val CAPABILITY_PHONE_APP = "muslim_phone_companion_v1"

    /** Capability advertised by the Wear OS companion app. */
    const val CAPABILITY_WATCH_APP = "muslim_watch_companion_v1"

    const val DATA_PATH = "/muslim/wear/state/v1"
    const val SYNC_REQUEST_PATH = "/muslim/wear/sync-request/v1"
    const val TASBIH_INCREMENT_PATH = "/muslim/wear/tasbih/increment/v1"

    const val KEY_NEXT_PRAYER = "next_prayer"
    const val KEY_NEXT_PRAYER_AT = "next_prayer_at"
    const val KEY_TASBIH_PHRASE = "tasbih_phrase"
    const val KEY_TASBIH_COUNT = "tasbih_count"
    const val KEY_TASBIH_TARGET = "tasbih_target"
    const val KEY_SYNCED_AT = "synced_at"
    const val KEY_ORNAMENT_STYLE = "ornament_style"
    const val KEY_ORNAMENT_INTENSITY = "ornament_intensity"

    fun isSupportedIncrementPath(path: String): Boolean = path == TASBIH_INCREMENT_PATH

    fun isSupportedSyncRequestPath(path: String): Boolean = path == SYNC_REQUEST_PATH
}

/** A privacy-minimal snapshot rendered on the watch. */
data class WearPrayerSnapshot(
    val nextPrayerName: String?,
    val nextPrayerAtEpochMillis: Long?,
    val tasbihPhrase: String,
    val tasbihCount: Int,
    val tasbihTarget: Int,
    val syncedAtEpochMillis: Long,
    val ornamentStyle: AppOrnamentStyle = AppOrnamentStyle.Geometry,
    val ornamentIntensity: OrnamentIntensity = OrnamentIntensity.Balanced,
) {
    fun isValid(): Boolean =
        tasbihPhrase.isNotBlank() &&
            tasbihCount >= 0 &&
            tasbihTarget in 1..100_000 &&
            syncedAtEpochMillis > 0
}
