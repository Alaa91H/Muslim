package org.muslim.app.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class PlaybackDeactivationReason {
    Completed,
    Stopped,
    Failed,
}

/**
 * Keeps the recitation's [QuranAudioPlayer] alive in the background by running
 * it under a foreground [RecitationPlaybackService].
 */
fun interface RecitationPlaybackBridge {
    fun onPlaybackActiveChanged(
        active: Boolean,
        reason: PlaybackDeactivationReason?,
    )
}

/** Real bridge: starts/stops the service and closes durable sessions deliberately. */
@Singleton
class RecitationPlaybackServiceBridge @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRuntime: RecitationSessionRuntime,
) : RecitationPlaybackBridge {

    override fun onPlaybackActiveChanged(
        active: Boolean,
        reason: PlaybackDeactivationReason?,
    ) {
        if (active) {
            RecitationPlaybackService.start(context)
            return
        }

        if (reason != PlaybackDeactivationReason.Failed) {
            sessionRuntime.clear()
        }
        RecitationPlaybackService.stop(context)
    }
}
