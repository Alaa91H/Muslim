package org.muslim.app.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the recitation's [QuranAudioPlayer] alive in the background by running
 * it under a foreground [RecitationPlaybackService]. The player calls
 * [onPlaybackActiveChanged] whenever it transitions to/from actual playback;
 * the bridge (wired through Hilt) starts/stops the foreground service so the
 * system never kills the process mid-recitation.
 */
fun interface RecitationPlaybackBridge {
    fun onPlaybackActiveChanged(active: Boolean)
}

/** Real [RecitationPlaybackBridge]: starts/stops [RecitationPlaybackService]. */
@Singleton
class RecitationPlaybackServiceBridge @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecitationPlaybackBridge {

    override fun onPlaybackActiveChanged(active: Boolean) {
        if (active) {
            RecitationPlaybackService.start(context)
        } else {
            RecitationPlaybackService.stop(context)
        }
    }
}
