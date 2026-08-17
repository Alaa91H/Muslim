package org.muslim.app.feature.quran.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Receives the play / pause / stop actions tapped on the recitation control
 * notification and routes them to the app-wide [QuranAudioPlayer]. Hilt does
 * not inject into BroadcastReceivers directly, so the singleton player is
 * resolved through an [EntryPoint] (same pattern as the other notifiers).
 */
class RecitationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val player = EntryPointAccessors
            .fromApplication(context.applicationContext, RecitationEntryPoint::class.java)
            .audioPlayer()
        when (intent.action) {
            ACTION_PLAY_PAUSE -> if (player.isPlaying) player.pause() else player.resume()
            ACTION_STOP -> player.stop()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RecitationEntryPoint {
        fun audioPlayer(): QuranAudioPlayer
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "org.muslim.app.quran.action.PLAY_PAUSE"
        const val ACTION_STOP = "org.muslim.app.quran.action.STOP"
    }
}
