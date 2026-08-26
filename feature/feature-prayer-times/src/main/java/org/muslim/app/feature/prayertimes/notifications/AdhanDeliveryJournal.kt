package org.muslim.app.feature.prayertimes.notifications

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.muslim.app.core.common.prayer.Prayer
import javax.inject.Inject
import javax.inject.Singleton

/** A concrete checkpoint in the scheduled Adhan delivery path. */
enum class AdhanDeliveryStage {
    NotRun,
    ProbeScheduled,
    ReceiverReached,
    VisibleNotificationPosted,
    VisibleNotificationBlocked,
    ServiceStartRequested,
    ServiceStarted,
    AudioFallbackStarted,
    AudioStarted,
    Failed,
}

/** Result of the active Adhan alert notification, independent of audio playback. */
enum class AdhanVisibleNotificationResult {
    NotAttempted,
    Posted,
    Blocked,
}

/**
 * Local, diagnostic-only record of the most recent Adhan delivery attempt.
 *
 * A successful permission/configuration check is not evidence of audible
 * delivery. This journal records each hand-off in the actual Android path,
 * including the point where MediaPlayer/AudioTrack has started output.
 */
data class AdhanDeliveryStatus(
    val stage: AdhanDeliveryStage = AdhanDeliveryStage.NotRun,
    val prayer: Prayer? = null,
    val isProbe: Boolean = false,
    val atMillis: Long = 0L,
    val detail: String? = null,
    val visibleNotificationResult: AdhanVisibleNotificationResult = AdhanVisibleNotificationResult.NotAttempted,
) {
    val audioStarted: Boolean get() = stage == AdhanDeliveryStage.AudioStarted
}

@Singleton
class AdhanDeliveryJournal @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _lastDelivery = MutableStateFlow(read(PREFIX_DELIVERY))
    val lastDelivery: StateFlow<AdhanDeliveryStatus> = _lastDelivery.asStateFlow()

    private val _lastProbe = MutableStateFlow(read(PREFIX_PROBE))
    val lastProbe: StateFlow<AdhanDeliveryStatus> = _lastProbe.asStateFlow()

    fun probeScheduled(prayer: Prayer) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.ProbeScheduled,
            prayer = prayer,
            isProbe = true,
            atMillis = System.currentTimeMillis(),
        ),
    )

    fun receiverReached(prayer: Prayer, isProbe: Boolean) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.ReceiverReached,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
        ),
    )

    fun visibleNotificationPosted(prayer: Prayer, isProbe: Boolean) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.VisibleNotificationPosted,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
            visibleNotificationResult = AdhanVisibleNotificationResult.Posted,
        ),
    )

    fun visibleNotificationBlocked(prayer: Prayer, isProbe: Boolean, detail: String) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.VisibleNotificationBlocked,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
            detail = detail.take(MAX_DETAIL_LENGTH),
            visibleNotificationResult = AdhanVisibleNotificationResult.Blocked,
        ),
    )

    fun serviceStartRequested(prayer: Prayer, isProbe: Boolean) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.ServiceStartRequested,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
        ),
    )

    fun serviceStarted(prayer: Prayer, isProbe: Boolean) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.ServiceStarted,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
        ),
    )

    fun audioFallbackStarted(prayer: Prayer, isProbe: Boolean, detail: String) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.AudioFallbackStarted,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
            detail = detail.take(MAX_DETAIL_LENGTH),
        ),
    )

    fun audioStarted(prayer: Prayer, isProbe: Boolean) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.AudioStarted,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
        ),
    )

    fun failed(prayer: Prayer, isProbe: Boolean, detail: String) = record(
        AdhanDeliveryStatus(
            stage = AdhanDeliveryStage.Failed,
            prayer = prayer,
            isProbe = isProbe,
            atMillis = System.currentTimeMillis(),
            detail = detail.take(MAX_DETAIL_LENGTH),
        ),
    )

    private fun record(status: AdhanDeliveryStatus) {
        val previous = if (status.isProbe) _lastProbe.value else _lastDelivery.value
        val carried = if (
            status.stage != AdhanDeliveryStage.ReceiverReached &&
            status.visibleNotificationResult == AdhanVisibleNotificationResult.NotAttempted &&
            previous.prayer == status.prayer &&
            previous.isProbe == status.isProbe
        ) {
            status.copy(visibleNotificationResult = previous.visibleNotificationResult)
        } else {
            status
        }
        if (carried.isProbe) {
            write(PREFIX_PROBE, carried)
            _lastProbe.value = carried
        } else {
            write(PREFIX_DELIVERY, carried)
            _lastDelivery.value = carried
        }
    }

    private fun read(prefix: String): AdhanDeliveryStatus {
        val stage = preferences.getString("${prefix}_stage", null)
            ?.let { runCatching { AdhanDeliveryStage.valueOf(it) }.getOrNull() }
            ?: AdhanDeliveryStage.NotRun
        val prayer = preferences.getString("${prefix}_prayer", null)
            ?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
        return AdhanDeliveryStatus(
            stage = stage,
            prayer = prayer,
            isProbe = preferences.getBoolean("${prefix}_probe", false),
            atMillis = preferences.getLong("${prefix}_at", 0L),
            detail = preferences.getString("${prefix}_detail", null),
            visibleNotificationResult = preferences
                .getString("${prefix}_visible_notification", null)
                ?.let { runCatching { AdhanVisibleNotificationResult.valueOf(it) }.getOrNull() }
                ?: AdhanVisibleNotificationResult.NotAttempted,
        )
    }

    private fun write(prefix: String, status: AdhanDeliveryStatus) {
        preferences.edit()
            .putString("${prefix}_stage", status.stage.name)
            .putString("${prefix}_prayer", status.prayer?.name)
            .putBoolean("${prefix}_probe", status.isProbe)
            .putLong("${prefix}_at", status.atMillis)
            .putString("${prefix}_detail", status.detail)
            .putString("${prefix}_visible_notification", status.visibleNotificationResult.name)
            .apply()
    }

    private companion object {
        const val PREFS = "adhan_delivery_journal"
        const val PREFIX_DELIVERY = "delivery"
        const val PREFIX_PROBE = "probe"
        const val MAX_DETAIL_LENGTH = 160
    }
}
