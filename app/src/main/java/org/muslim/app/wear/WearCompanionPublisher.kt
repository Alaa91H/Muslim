package org.muslim.app.wear

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import org.muslim.app.core.common.wear.WearPrayerSnapshot
import org.muslim.app.core.common.wear.WearSyncContract
import org.muslim.app.core.datastore.AppPreferences
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.core.datastore.prayer.PrayerSettings
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository
import org.muslim.app.feature.prayertimes.domain.PrayerCountdownData
import org.muslim.app.feature.tasbih.data.TasbihRepository
import org.muslim.app.feature.tasbih.domain.TasbihState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Publishes a minimal paired-watch snapshot. No location, calculation method,
 * prayer history, audio, account data, or home-automation configuration leaves
 * the phone. Data Layer handles encrypted delivery to the same signed app.
 */
@Singleton
class WearCompanionPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val prayerSettingsRepository: PrayerSettingsRepository,
    private val calculator: PrayerTimesCalculator,
    private val tasbihRepository: TasbihRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(
                appPreferencesRepository.preferences,
                prayerSettingsRepository.settings,
                tasbihRepository.state,
                minuteTicker(),
            ) { preferences, prayerSettings, tasbih, nowMillis ->
                WearSyncInputs(preferences, prayerSettings, tasbih, nowMillis)
            }.collect { inputs ->
                if (inputs.preferences.wearCompanionEnabled) publish(inputs)
            }
        }
    }

    private fun publish(inputs: WearSyncInputs) {
        val countdown = PrayerCountdownData.compute(
            settings = inputs.prayerSettings,
            calculator = calculator,
            nowMillis = inputs.nowMillis,
        )
        val snapshot = WearPrayerSnapshot(
            nextPrayerName = countdown.nextPrayer?.watchLabel(),
            nextPrayerAtEpochMillis = countdown.nextPrayerAt?.let { nextTime ->
                inputs.nowMillis + countdown.remainingSeconds * 1_000L
            },
            tasbihPhrase = inputs.tasbih.phrase.text,
            tasbihCount = inputs.tasbih.count,
            tasbihTarget = inputs.tasbih.target,
            syncedAtEpochMillis = inputs.nowMillis,
        )
        if (!snapshot.isValid()) return

        val request = PutDataMapRequest.create(WearSyncContract.DATA_PATH).apply {
            dataMap.putString(WearSyncContract.KEY_NEXT_PRAYER, snapshot.nextPrayerName.orEmpty())
            dataMap.putLong(WearSyncContract.KEY_NEXT_PRAYER_AT, snapshot.nextPrayerAtEpochMillis ?: 0L)
            dataMap.putString(WearSyncContract.KEY_TASBIH_PHRASE, snapshot.tasbihPhrase)
            dataMap.putInt(WearSyncContract.KEY_TASBIH_COUNT, snapshot.tasbihCount)
            dataMap.putInt(WearSyncContract.KEY_TASBIH_TARGET, snapshot.tasbihTarget)
            dataMap.putLong(WearSyncContract.KEY_SYNCED_AT, snapshot.syncedAtEpochMillis)
        }.asPutDataRequest()
        Wearable.getDataClient(context).putDataItem(request)
    }

    private fun minuteTicker(): Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(MINUTE_MILLIS)
        }
    }

    private fun Prayer.watchLabel(): String = when (this) {
        Prayer.Fajr -> "الفجر"
        Prayer.Sunrise -> "الشروق"
        Prayer.Dhuhr -> "الظهر"
        Prayer.Asr -> "العصر"
        Prayer.Maghrib -> "المغرب"
        Prayer.Isha -> "العشاء"
    }

    private data class WearSyncInputs(
        val preferences: AppPreferences,
        val prayerSettings: PrayerSettings,
        val tasbih: TasbihState,
        val nowMillis: Long,
    )

    private companion object {
        const val MINUTE_MILLIS = 60_000L
    }
}
