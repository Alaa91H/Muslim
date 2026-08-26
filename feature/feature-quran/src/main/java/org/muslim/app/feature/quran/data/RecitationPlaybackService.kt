package org.muslim.app.feature.quran.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.QuranAyahIndex
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Surah
import javax.inject.Inject

/**
 * Foreground service (type `mediaPlayback`) that keeps Quran recitation alive
 * in the background and exposes it to the system as **media**:
 *
 * - A [MediaSessionCompat] + [MediaStyle] notification make the recitation
 *   appear on the lock screen and in the notification shade with standard
 *   prev / play-pause / next / stop controls (same look as any music app).
 * - Running as a foreground service is what stops Android from killing the
 *   process after a few minutes of background playback.
 *
 * The playback itself stays in the app-wide [QuranAudioPlayer] singleton;
 * this service merely hosts the session, posts the notification and reacts to
 * its controls by calling back into the player. It is started by
 * [RecitationPlaybackServiceBridge] when playback becomes active and stops
 * itself as soon as the player goes Idle.
 */
@AndroidEntryPoint
class RecitationPlaybackService : MediaBrowserServiceCompat() {

    @Inject lateinit var player: QuranAudioPlayer
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var recitationRepository: RecitationRepository

    private var session: MediaSessionCompat? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var collecting = false

    // --- Notification-driven pause (see [RecitationPauseOnNotifications]) ---
    // Notifications do not request audio focus, so a soundful notification
    // would overlap the recitation without this. Pause while it shows, resume
    // a moment after it disappears — only if the user hadn't paused manually.
    private var resumeAfterNotification = false

    // --- Audio focus ---
    // Quran recitation should pause whenever another sound takes over
    // (navigation voice, another media app, an alert) and automatically
    // resume once that sound finishes. A permanent loss (e.g. a phone call)
    // pauses without auto-resume. The decision logic lives in
    // [RecitationFocusPolicy] so it can be unit-tested on the JVM.
    private val focusPolicy = RecitationFocusPolicy()
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss (phone call, another app took over): pause
                // and never auto-resume.
                focusPolicy.onPermanentLoss()
                hasAudioFocus = false
                player.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            -> {
                // Another sound started (navigation, alerts, other media):
                // pause; remember to resume once focus comes back, but only if
                // the user hadn't already paused manually.
                focusPolicy.onTransientLoss(
                    player.playbackState.value == PlaybackState.Playing,
                )
                player.pause()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                if (focusPolicy.onGain()) player.resume()
            }
        }
    }

    private fun requestAudioFocus() {
        val request = audioFocusRequest ?: return
        hasAudioFocus = audioManager?.requestAudioFocus(request) ==
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        hasAudioFocus = false
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: android.os.Bundle?,
    ): BrowserRoot = BrowserRoot(MEDIA_ROOT_ID, null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        result.detach()
        scope.launch {
            result.sendResult(buildBrowseChildren(parentId).toMutableList())
        }
    }

    private fun onNotificationPause() {
        resumeAfterNotification = player.playbackState.value == PlaybackState.Playing
        if (resumeAfterNotification) player.pause()
    }

    private fun onNotificationResume() {
        if (resumeAfterNotification) {
            resumeAfterNotification = false
            player.resume()
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AudioManager::class.java)
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setOnAudioFocusChangeListener(focusListener)
            .build()
        session = MediaSessionCompat(this, MEDIA_SESSION_TAG).apply {
            setCallback(PlayerSessionCallback())
            isActive = true
        }
        session?.let { mediaSession -> setSessionToken(mediaSession.sessionToken) }
        RecitationPauseController.onPauseRequested = ::onNotificationPause
        RecitationPauseController.onResumeRequested = ::onNotificationResume
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Route headphone / external media-button presses (ACTION_MEDIA_BUTTON)
        // to the active media session so the play/pause/next keys on headsets
        // and Bluetooth devices control the recitation.
        session?.let { MediaButtonReceiver.handleIntent(it, intent) }
        if (player.playbackState.value == PlaybackState.Idle) {
            // The process was recreated (START_STICKY) but nothing is playing —
            // nothing to show, so shut down immediately.
            stopSelf()
            return START_NOT_STICKY
        }
        if (!collecting) {
            collecting = true
            scope.launch {
                combine(player.playbackState, player.currentAyah) { state, ayah -> state to ayah }
                    .collect { (state, ayah) ->
                        when (state) {
                            PlaybackState.Idle -> stopSelf()
                            PlaybackState.Playing, PlaybackState.Paused ->
                                runCatching {
                                    publish(state, ayah)
                                }
                        }
                    }
            }
        }
        return START_STICKY
    }

    private fun publish(state: PlaybackState, globalNumber: Int?) {
        lastGlobalAyah = globalNumber
        val reference = globalNumber?.let { QuranAyahIndex.referenceOf(it) }
        val title = if (reference != null) {
            getString(R.string.quran_recitation_notif_title, reference.first, reference.second)
        } else {
            getString(R.string.quran_recitation_notif_title_unknown)
        }
        val text = getString(
            if (state == PlaybackState.Playing) {
                R.string.quran_recitation_notif_playing
            } else {
                R.string.quran_recitation_notif_paused
            },
        )

        session?.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, text)
                .build(),
        )
        session?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_STOP,
                )
                .setState(
                    if (state == PlaybackState.Playing) {
                        PlaybackStateCompat.STATE_PLAYING
                    } else {
                        PlaybackStateCompat.STATE_PAUSED
                    },
                    player.positionMs.value,
                    1f,
                )
                .build(),
        )

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(state, title, text),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            },
        )
    }

    private fun buildNotification(
        state: PlaybackState,
        title: String,
        text: String,
    ): android.app.Notification {
        val toggleIcon = if (state == PlaybackState.Playing) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val toggleLabel = if (state == PlaybackState.Playing) {
            getString(R.string.quran_recitation_notif_pause)
        } else {
            getString(R.string.quran_recitation_notif_play)
        }

        return NotificationCompat.Builder(this, NotificationChannels.RECITATION)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v1251)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent())
            .setStyle(
                MediaStyle()
                    .setMediaSession(session?.sessionToken)
                    .setShowActionsInCompactView(1, 2),
            )
            .addAction(
                android.R.drawable.ic_media_previous,
                getString(R.string.quran_recitation_notif_previous),
                actionPendingIntent(RecitationActionReceiver.ACTION_PREVIOUS),
            )
            .addAction(toggleIcon, toggleLabel, actionPendingIntent(RecitationActionReceiver.ACTION_PLAY_PAUSE))
            .addAction(
                android.R.drawable.ic_media_next,
                getString(R.string.quran_recitation_notif_next),
                actionPendingIntent(RecitationActionReceiver.ACTION_NEXT),
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.quran_recitation_notif_stop),
                actionPendingIntent(RecitationActionReceiver.ACTION_STOP),
            )
            .build()
    }

    private fun actionPendingIntent(action: String): PendingIntent =
        PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            Intent(this, RecitationActionReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /**
     * Tapping the notification opens the Quran reader scrolled to the ayah
     * that is currently being recited (instead of the plain launcher screen):
     * the reader route + ayah travel through [EXTRA_ROUTE], the same channel
     * the App Shortcuts use, so [MainActivity] navigates there on tap.
     */
    private fun openAppPendingIntent(): PendingIntent {
        val launch = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        lastGlobalAyah?.let { global ->
            val surah = QuranAyahIndex.surahOf(global)
            if (surah >= 1) {
                // Same route shape as "quran/reader/{surahNumber}?ayah={ayah}".
                launch?.putExtra(EXTRA_ROUTE, "quran/reader/$surah?ayah=$global")
            }
        }
        return PendingIntent.getActivity(
            this,
            OPEN_APP_REQUEST_CODE,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Global ayah number of the ayah currently being recited (null when idle). */
    private var lastGlobalAyah: Int? = null

    private suspend fun buildBrowseChildren(parentId: String): List<MediaBrowserCompat.MediaItem> = when (parentId) {
        MEDIA_ROOT_ID -> listOf(
            browseFolder(
                id = RECITATIONS_FOLDER_ID,
                title = getString(R.string.quran_car_recitations),
                subtitle = getString(R.string.quran_car_recitations_subtitle),
            ),
        )

        RECITATIONS_FOLDER_ID -> downloadedSurahs().map(::surahItem)
        else -> emptyList()
    }

    private suspend fun downloadedSurahs(): List<Surah> {
        val selectedReciter = recitationRepository.selectedReciter()
        return quranRepository.observeSurahs().first().filter { surah ->
            recitationRepository.isSurahComplete(
                reciterId = selectedReciter.id,
                surahNumber = surah.number,
                expectedAyahs = surah.ayahCount,
            )
        }
    }

    private fun browseFolder(
        id: String,
        title: String,
        subtitle: String,
    ): MediaBrowserCompat.MediaItem = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE,
    )

    private fun surahItem(surah: Surah): MediaBrowserCompat.MediaItem = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId("$SURAH_MEDIA_PREFIX${surah.number}")
            .setTitle(surah.arabicName)
            .setSubtitle(surah.englishName)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE,
    )

    private fun playMediaId(mediaId: String) {
        val surahNumber = mediaId.removePrefix(SURAH_MEDIA_PREFIX).toIntOrNull() ?: return
        scope.launch { playDownloadedSurah(surahNumber) }
    }

    private suspend fun playDownloadedSurah(surahNumber: Int) {
        val surah = quranRepository.observeSurahs().first().firstOrNull { it.number == surahNumber } ?: return
        val reciter = recitationRepository.selectedReciter()
        if (!recitationRepository.isSurahComplete(reciter.id, surah.number, surah.ayahCount)) {
            publishPlaybackError(getString(R.string.quran_car_not_downloaded))
            return
        }
        val queue = quranRepository.observeSurah(surah.number).first().map { ayah ->
            RecitationQueueItem(
                file = recitationRepository.fileFor(reciter.id, surah.number, ayah.globalNumber),
                globalNumber = ayah.globalNumber,
            )
        }
        if (queue.isEmpty()) return
        requestAudioFocus()
        player.playQueue(queue, startIndex = 0, repeatCount = 1)
    }

    private fun playSearch(query: String?) {
        val normalized = query.orEmpty().trim().lowercase()
        if (normalized.isEmpty()) return
        scope.launch {
            val matched = quranRepository.observeSurahs().first().firstOrNull { surah ->
                normalized.contains(surah.arabicName.lowercase()) ||
                    normalized.contains(surah.englishName.lowercase()) ||
                    normalized == surah.number.toString()
            }
            matched?.let { surah -> playDownloadedSurah(surah.number) }
                ?: publishPlaybackError(getString(R.string.quran_car_search_unavailable))
        }
    }

    private fun publishPlaybackError(message: String) {
        session?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY)
                .setState(PlaybackStateCompat.STATE_ERROR, 0L, 0f)
                .setErrorMessage(PlaybackStateCompat.ERROR_CODE_NOT_AVAILABLE_IN_REGION, message)
                .build(),
        )
    }

    override fun onDestroy() {
        abandonAudioFocus()
        RecitationPauseController.onPauseRequested = null
        RecitationPauseController.onResumeRequested = null
        scope.cancel()
        session?.isActive = false
        session?.release()
        session = null
        super.onDestroy()
    }

    /**
     * Bridges the media-session controls back to the shared player. Play
     * requests audio focus (so other apps duck/pause for us); pause/stop
     * release it (so other apps can play again).
     */
    private inner class PlayerSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            requestAudioFocus()
            player.resume()
        }

        override fun onPause() {
            abandonAudioFocus()
            player.pause()
        }

        override fun onStop() {
            abandonAudioFocus()
            player.stop()
        }

        override fun onSkipToNext() = player.next()
        override fun onSkipToPrevious() = player.previous()

        override fun onPlayFromMediaId(mediaId: String?, extras: android.os.Bundle?) {
            mediaId?.let(::playMediaId)
        }

        override fun onPlayFromSearch(query: String?, extras: android.os.Bundle?) {
            playSearch(query)
        }
    }

    companion object {
        private const val MEDIA_SESSION_TAG = "org.muslim.app.quran.RecitationPlayback"
        private const val MEDIA_ROOT_ID = "muslim_recitation_root"
        private const val RECITATIONS_FOLDER_ID = "muslim_recitations"
        private const val SURAH_MEDIA_PREFIX = "muslim_surah_"
        private const val NOTIFICATION_ID = 7006
        private const val OPEN_APP_REQUEST_CODE = 70061
        /** Same extra key [org.muslim.app.MainActivity] reads for deep links. */
        private const val EXTRA_ROUTE = "org.muslim.app.extra.ROUTE"

        fun start(context: Context) {
            val intent = Intent(context, RecitationPlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, RecitationPlaybackService::class.java))
        }
    }
}
