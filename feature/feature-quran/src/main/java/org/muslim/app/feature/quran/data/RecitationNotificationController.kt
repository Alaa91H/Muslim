package org.muslim.app.feature.quran.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.core.notifications.notificationCategoryEnabled
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.QuranAyahIndex
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts a media-style control notification while a Quran recitation is playing
 * (or paused): play/pause + stop actions, plus the current surah/ayah as the
 * title. Owned by the unified notification manager — it is gated on the
 * [NotificationCategory.Recitation] master switch, so disabling that category
 * in settings hides it everywhere.
 *
 * Started once from MainActivity; collects the app-wide [QuranAudioPlayer]
 * state so it appears and disappears with playback.
 */
@Singleton
class RecitationNotificationController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioPlayer: QuranAudioPlayer,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    /** Starts observing the player; safe to call multiple times. */
    fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(
                audioPlayer.playbackState,
                audioPlayer.currentAyah,
            ) { state, globalNumber -> state to globalNumber }
                .collect { (state, globalNumber) ->
                    when (state) {
                        PlaybackState.Idle -> hide()
                        PlaybackState.Playing, PlaybackState.Paused -> show(state, globalNumber)
                    }
                }
        }
    }

    private suspend fun show(state: PlaybackState, globalNumber: Int?) {
        if (!context.notificationCategoryEnabled(NotificationCategory.Recitation)) return
        val reference = globalNumber?.let { QuranAyahIndex.referenceOf(it) }
        val title = if (reference != null) {
            context.getString(
                R.string.quran_recitation_notif_title,
                reference.first,
                reference.second,
            )
        } else {
            context.getString(R.string.quran_recitation_notif_title_unknown)
        }
        val text = context.getString(
            if (state == PlaybackState.Playing) {
                R.string.quran_recitation_notif_playing
            } else {
                R.string.quran_recitation_notif_paused
            },
        )

        val toggleIcon = if (state == PlaybackState.Playing) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val toggleLabel = if (state == PlaybackState.Playing) {
            context.getString(R.string.quran_recitation_notif_pause)
        } else {
            context.getString(R.string.quran_recitation_notif_play)
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.RECITATION)
            .setSmallIcon(R.drawable.ic_recitation_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setContentIntent(openAppPendingIntent())
            .addAction(toggleIcon, toggleLabel, actionPendingIntent(RecitationActionReceiver.ACTION_PLAY_PAUSE))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.quran_recitation_notif_stop), actionPendingIntent(RecitationActionReceiver.ACTION_STOP))
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun hide() {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    private fun actionPendingIntent(action: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            Intent(context, RecitationActionReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun openAppPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** Idempotent teardown (used by tests / process recreation paths). */
    fun stop() {
        if (!started) return
        started = false
        scope.cancel()
        hide()
    }

    private companion object {
        const val NOTIFICATION_ID = 7005
        const val OPEN_APP_REQUEST_CODE = 70051
    }
}
