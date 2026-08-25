package org.muslim.app.feature.prayertimes.notifications

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small façade for UI-only playback controls and delivery evidence.
 *
 * Keeping these closely related responsibilities together avoids exposing a
 * raw player and a separate diagnostic store to each screen model.
 */
@Singleton
class AdhanPlaybackDiagnostics @Inject constructor(
    private val soundPlayer: AdhanSoundPlayer,
    private val deliveryJournal: AdhanDeliveryJournal,
) {
    val lastProbe: StateFlow<AdhanDeliveryStatus> = deliveryJournal.lastProbe

    fun setPreviewVolume(volumePercent: Int) {
        soundPlayer.setVolume(volumePercent)
    }
}
