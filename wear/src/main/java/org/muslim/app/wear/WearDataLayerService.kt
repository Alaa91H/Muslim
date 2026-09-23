package org.muslim.app.wear

import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import org.muslim.app.core.common.appearance.AppOrnamentStyle
import org.muslim.app.core.common.appearance.OrnamentIntensity
import org.muslim.app.core.common.wear.WearPrayerSnapshot
import org.muslim.app.core.common.wear.WearSyncContract

/**
 * Receives the privacy-minimal state published by the paired phone. The
 * service accepts only the versioned state path and persists it locally so the
 * watch remains useful between Data Layer deliveries.
 */
class WearDataLayerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            dataEvents
                .filter { event -> event.type == DataEvent.TYPE_CHANGED }
                .filter { event -> event.dataItem.uri.path == WearSyncContract.DATA_PATH }
                .forEach { event ->
                    val data = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val snapshot = wearSnapshotFrom(data)
                    if (snapshot.isValid()) WearSnapshotStore.save(applicationContext, snapshot)
                }
        } finally {
            dataEvents.release()
        }
    }
}

internal fun wearSnapshotFrom(data: DataMap): WearPrayerSnapshot =
    WearPrayerSnapshot(
        nextPrayerName = data.getString(WearSyncContract.KEY_NEXT_PRAYER),
        nextPrayerAtEpochMillis = data.getLong(WearSyncContract.KEY_NEXT_PRAYER_AT, 0L)
            .takeIf { value -> value > 0L },
        tasbihPhrase = data.getString(WearSyncContract.KEY_TASBIH_PHRASE).orEmpty(),
        tasbihCount = data.getInt(WearSyncContract.KEY_TASBIH_COUNT, 0),
        tasbihTarget = data.getInt(WearSyncContract.KEY_TASBIH_TARGET, 33),
        syncedAtEpochMillis = data.getLong(WearSyncContract.KEY_SYNCED_AT, 0L),
        ornamentStyle = enumOr(
            data.getString(WearSyncContract.KEY_ORNAMENT_STYLE),
            AppOrnamentStyle.Geometry,
        ),
        ornamentIntensity = enumOr(
            data.getString(WearSyncContract.KEY_ORNAMENT_INTENSITY),
            OrnamentIntensity.Balanced,
        ),
    )

/** Local storage for the non-sensitive state rendered by [WearMainActivity]. */
internal object WearSnapshotStore {
    private const val FILE_NAME = "wear_companion_snapshot"
    private const val NEXT_PRAYER = "next_prayer"
    private const val NEXT_PRAYER_AT = "next_prayer_at"
    private const val TASBIH_PHRASE = "tasbih_phrase"
    private const val TASBIH_COUNT = "tasbih_count"
    private const val TASBIH_TARGET = "tasbih_target"
    private const val SYNCED_AT = "synced_at"
    private const val ORNAMENT_STYLE = "ornament_style"
    private const val ORNAMENT_INTENSITY = "ornament_intensity"

    fun save(context: Context, snapshot: WearPrayerSnapshot) {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit {
            putString(NEXT_PRAYER, snapshot.nextPrayerName)
            putLong(NEXT_PRAYER_AT, snapshot.nextPrayerAtEpochMillis ?: 0L)
            putString(TASBIH_PHRASE, snapshot.tasbihPhrase)
            putInt(TASBIH_COUNT, snapshot.tasbihCount)
            putInt(TASBIH_TARGET, snapshot.tasbihTarget)
            putLong(SYNCED_AT, snapshot.syncedAtEpochMillis)
            putString(ORNAMENT_STYLE, snapshot.ornamentStyle.name)
            putString(ORNAMENT_INTENSITY, snapshot.ornamentIntensity.name)
        }
    }

    fun read(context: Context): WearPrayerSnapshot? {
        val prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val snapshot = WearPrayerSnapshot(
            nextPrayerName = prefs.getString(NEXT_PRAYER, null),
            nextPrayerAtEpochMillis = prefs.getLong(NEXT_PRAYER_AT, 0L).takeIf { it > 0L },
            tasbihPhrase = prefs.getString(TASBIH_PHRASE, "").orEmpty(),
            tasbihCount = prefs.getInt(TASBIH_COUNT, 0),
            tasbihTarget = prefs.getInt(TASBIH_TARGET, 33),
            syncedAtEpochMillis = prefs.getLong(SYNCED_AT, 0L),
            ornamentStyle = enumOr(
                prefs.getString(ORNAMENT_STYLE, null),
                AppOrnamentStyle.Geometry,
            ),
            ornamentIntensity = enumOr(
                prefs.getString(ORNAMENT_INTENSITY, null),
                OrnamentIntensity.Balanced,
            ),
        )
        return snapshot.takeIf(WearPrayerSnapshot::isValid)
    }
}

private fun <T : Enum<T>> enumOr(value: String?, default: T): T =
    value?.let { raw -> default::class.java.enumConstants?.firstOrNull { it.name == raw } } ?: default
