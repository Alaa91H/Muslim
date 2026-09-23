package org.muslim.app.wear

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val started = AtomicBoolean(false)

    fun start() {
        if (!started.compareAndSet(false, true)) return

        // Match NexaFlow's reconnect behavior: whenever the remote watch
        // capability becomes reachable, immediately re-publish a fresh state.
        scope.launch {
            watchReconnectEvents().collect {
                runCatching { pushNow() }
                    .onFailure { Log.w(TAG, "Wear reconnect push failed", it) }
            }
        }

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

    /** Builds and publishes a fresh snapshot in response to the watch. */
    suspend fun pushNow() {
        val preferences = appPreferencesRepository.preferences.first()
        if (!preferences.wearCompanionEnabled) return
        publish(
            WearSyncInputs(
                preferences = preferences,
                prayerSettings = prayerSettingsRepository.settings.first(),
                tasbih = tasbihRepository.state.first(),
                nowMillis = System.currentTimeMillis(),
            ),
        )
    }

    private suspend fun publish(inputs: WearSyncInputs) {
        val countdown = PrayerCountdownData.compute(
            settings = inputs.prayerSettings,
            calculator = calculator,
            nowMillis = inputs.nowMillis,
        )
        val snapshot = WearPrayerSnapshot(
            nextPrayerName = countdown.nextPrayer?.watchLabel(),
            nextPrayerId = countdown.nextPrayer?.watchId(),
            nextPrayerAtEpochMillis = countdown.nextPrayerAt?.let {
                inputs.nowMillis + countdown.remainingSeconds * 1_000L
            },
            tasbihPhrase = inputs.tasbih.phrase.text,
            tasbihCount = inputs.tasbih.count,
            tasbihTarget = inputs.tasbih.target,
            syncedAtEpochMillis = inputs.nowMillis,
            ornamentStyle = inputs.preferences.ornamentStyle,
            ornamentIntensity = inputs.preferences.ornamentIntensity,
            languageTag = resolvedLanguageTag(inputs.preferences.languageCode),
        )
        if (!snapshot.isValid()) return

        val request = PutDataMapRequest.create(WearSyncContract.DATA_PATH).apply {
            dataMap.putString(WearSyncContract.KEY_NEXT_PRAYER, snapshot.nextPrayerName.orEmpty())
            dataMap.putString(WearSyncContract.KEY_NEXT_PRAYER_ID, snapshot.nextPrayerId.orEmpty())
            dataMap.putLong(WearSyncContract.KEY_NEXT_PRAYER_AT, snapshot.nextPrayerAtEpochMillis ?: 0L)
            dataMap.putString(WearSyncContract.KEY_TASBIH_PHRASE, snapshot.tasbihPhrase)
            dataMap.putInt(WearSyncContract.KEY_TASBIH_COUNT, snapshot.tasbihCount)
            dataMap.putInt(WearSyncContract.KEY_TASBIH_TARGET, snapshot.tasbihTarget)
            dataMap.putLong(WearSyncContract.KEY_SYNCED_AT, snapshot.syncedAtEpochMillis)
            dataMap.putString(WearSyncContract.KEY_ORNAMENT_STYLE, snapshot.ornamentStyle.name)
            dataMap.putString(WearSyncContract.KEY_ORNAMENT_INTENSITY, snapshot.ornamentIntensity.name)
            dataMap.putString(WearSyncContract.KEY_LANGUAGE_TAG, snapshot.languageTag.orEmpty())
        }.asPutDataRequest().setUrgent()

        runCatching {
            Wearable.getDataClient(context).putDataItem(request).await()
        }.onFailure {
            Log.w(TAG, "Failed to publish Wear snapshot", it)
        }
    }

    /**
     * Emits for an already-connected watch at startup and again when the watch
     * capability returns after Bluetooth/Wi-Fi/cloud Data Layer reconnects.
     */
    private fun watchReconnectEvents(): Flow<Unit> = callbackFlow {
        val capabilityClient = Wearable.getCapabilityClient(context)
        val listener = CapabilityClient.OnCapabilityChangedListener { capability ->
            if (capability.nodes.isNotEmpty()) trySend(Unit)
        }

        val listenerRegistered = runCatching {
            capabilityClient.addListener(
                listener,
                WearSyncContract.CAPABILITY_WATCH_APP,
            ).await()
            true
        }.getOrElse {
            Log.w(TAG, "Failed to register Wear capability listener", it)
            false
        }

        val reachable = runCatching {
            capabilityClient.getCapability(
                WearSyncContract.CAPABILITY_WATCH_APP,
                CapabilityClient.FILTER_REACHABLE,
            ).await().nodes.isNotEmpty()
        }.getOrElse {
            Log.w(TAG, "Failed to query Wear capability", it)
            false
        }
        if (reachable) trySend(Unit)

        awaitClose {
            if (listenerRegistered) {
                capabilityClient.removeListener(listener, WearSyncContract.CAPABILITY_WATCH_APP)
            }
        }
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

    private fun Prayer.watchId(): String = when (this) {
        Prayer.Fajr -> "fajr"
        Prayer.Sunrise -> "sunrise"
        Prayer.Dhuhr -> "dhuhr"
        Prayer.Asr -> "asr"
        Prayer.Maghrib -> "maghrib"
        Prayer.Isha -> "isha"
    }

    /**
     * Resolve "system" on the phone before transport so the watch follows the
     * phone app's effective language even when the watch OS uses another locale.
     */
    private fun resolvedLanguageTag(languageCode: String): String =
        if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
            Resources.getSystem().configuration.locales[0].toLanguageTag()
        } else {
            languageCode
        }

    private data class WearSyncInputs(
        val preferences: AppPreferences,
        val prayerSettings: PrayerSettings,
        val tasbih: TasbihState,
        val nowMillis: Long,
    )

    private companion object {
        const val TAG = "WearCompanionPublisher"
        const val MINUTE_MILLIS = 60_000L
    }
}
