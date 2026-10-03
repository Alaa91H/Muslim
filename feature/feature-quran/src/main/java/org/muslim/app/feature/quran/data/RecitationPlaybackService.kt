package org.muslim.app.feature.quran.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.createBitmap
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.QuranAyahIndex
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Reciter
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
    @Inject lateinit var sessionRuntime: RecitationSessionRuntime
    @Inject lateinit var sessionStore: RecitationSessionStore

    private var session: MediaSessionCompat? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var collecting = false
    private var sessionPersistenceStarted = false

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
        // The service can be recreated before another application-start path runs.
        // Clear its retained pre-branding card before publishing the current media card.
        cancelRetiredNotification(this)
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
                            PlaybackState.Playing, PlaybackState.Paused -> {
                                player.refreshPosition()
                                sessionRuntime.persist(
                                    currentGlobalNumber = ayah,
                                    positionMs = player.positionMs.value,
                                    remainingRepeats = player.remainingRepeats.value,
                                    state = state,
                                )
                                runCatching { publish(state, ayah) }
                            }
                        }
                    }
            }
        }
        ensureSessionPersistence()
        return START_STICKY
    }

    private fun ensureSessionPersistence() {
        if (sessionPersistenceStarted) return
        sessionPersistenceStarted = true
        scope.launch {
            while (true) {
                delay(SESSION_PERSIST_INTERVAL_MS)
                val state = player.playbackState.value
                if (state != PlaybackState.Idle) {
                    player.refreshPosition()
                    sessionRuntime.persist(
                        currentGlobalNumber = player.currentAyah.value,
                        positionMs = player.positionMs.value,
                        remainingRepeats = player.remainingRepeats.value,
                        state = state,
                    )
                }
            }
        }
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
            RECITATION_NOTIFICATION_ID,
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

        // Professional transport notification: unified small-icon identity (white, tinted
        // for status bar) + official gold largeIcon for the shade so the media card
        // matches the countdown card's gold emblem. The largeIcon is the app's
        // launcher icon (gold hollow star on navy) obtained via PackageManager so
        // no cross-module resource reference is needed.
        val largeIcon: Bitmap? = try {
            val drawable: Drawable = packageManager.getApplicationIcon(packageName)
            drawableToBitmap(drawable)
        } catch (_: Exception) { null }
        return NotificationCompat.Builder(this, NotificationChannels.RECITATION)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2029)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText(getString(R.string.quran_recitation_notif_subtext))
            .setContentInfo(if (state == PlaybackState.Playing) getString(R.string.quran_recitation_notif_playing) else getString(R.string.quran_recitation_notif_paused))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(false)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setContentIntent(openAppPendingIntent())
            .setStyle(
                MediaStyle()
                    .setMediaSession(session?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2),
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

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 256
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 256
        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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
        MEDIA_ROOT_ID -> buildList {
            androidAutoResumePlan()?.let { plan ->
                val ayah = quranRepository.ayahByGlobal(plan.intent.globalNumbers.first())
                val surah = ayah?.let { quranRepository.observeSurahMetadata(it.surahNumber).first() }
                if (ayah != null && surah != null) {
                    add(
                        playableItem(
                            id = RESUME_MEDIA_ID,
                            title = getString(R.string.quran_resume, surah.englishName, ayah.numberInSurah.toString()),
                            subtitle = Reciter.Bundled.first { it.id == plan.intent.reciterId }.name,
                        ),
                    )
                }
            }
            if (bookmarkedAyahItems().isNotEmpty()) {
                add(
                    browseFolder(
                        id = BOOKMARKS_FOLDER_ID,
                        title = getString(R.string.quran_bookmarks),
                        subtitle = null,
                    ),
                )
            }
            add(browseFolder(
                id = RECITATIONS_FOLDER_ID,
                title = getString(R.string.quran_car_recitations),
                subtitle = getString(R.string.quran_car_recitations_subtitle),
            ))
        }

        RECITATIONS_FOLDER_ID -> downloadedReciters().map(::reciterFolder)
        BOOKMARKS_FOLDER_ID -> bookmarkedAyahItems()
        else -> if (parentId.startsWith(RECITER_MEDIA_PREFIX)) {
            val reciterId = parentId.removePrefix(RECITER_MEDIA_PREFIX)
            downloadedSurahs(reciterId).map { surah -> surahItem(surah, reciterId) }
        } else emptyList()
    }

    private suspend fun downloadedReciters() = with(quranRepository.observeSurahs().first().associateBy { it.number }) {
        Reciter.Bundled.mapNotNull { reciter ->
            val hasCompleteSurah = recitationRepository.downloadState(reciter.id).surahCounts.any { (number, count) ->
                val expectedAyahs = this[number]?.ayahCount
                expectedAyahs != null && count >= expectedAyahs
            }
            reciter.takeIf { hasCompleteSurah }
        }
    }

    private suspend fun downloadedSurahs(reciterId: String): List<Surah> {
        val downloadedCounts = recitationRepository.downloadState(reciterId).surahCounts
        return quranRepository.observeSurahs().first().filter { surah ->
            (downloadedCounts[surah.number] ?: 0) >= surah.ayahCount
        }
    }

    private fun browseFolder(
        id: String,
        title: String,
        subtitle: String?,
    ): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
        subtitle?.let(description::setSubtitle)
        return MediaBrowserCompat.MediaItem(description.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun reciterFolder(reciter: Reciter): MediaBrowserCompat.MediaItem = browseFolder(
        id = "$RECITER_MEDIA_PREFIX${reciter.id}",
        title = reciter.name,
        subtitle = reciter.style,
    )

    private fun surahItem(surah: Surah, reciterId: String): MediaBrowserCompat.MediaItem = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId("$SURAH_MEDIA_PREFIX${surah.number}_$reciterId")
            .setTitle(surah.arabicName)
            .setSubtitle(surah.englishName)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE,
    )

    private fun playableItem(id: String, title: String, subtitle: String): MediaBrowserCompat.MediaItem =
        MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder().setMediaId(id).setTitle(title).setSubtitle(subtitle).build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE,
        )

    /** Surface saved ayah bookmarks only when the selected reader's audio is already local. */
    private suspend fun bookmarkedAyahItems(): List<MediaBrowserCompat.MediaItem> {
        val reciter = recitationRepository.selectedReciter()
        val bookmarks = quranRepository.observeBookmarks().first()
        val playable = withContext(Dispatchers.IO) {
            bookmarks.filter { bookmark ->
                val ayah = bookmark.ayah
                val file = recitationRepository.fileFor(reciter.id, ayah.surahNumber, ayah.globalNumber)
                file.isFile && file.length() > 0L
            }
        }
        return playable.map { bookmark ->
            playableItem(
                id = "muslim_ayah_${bookmark.ayah.globalNumber}",
                title = getString(
                    R.string.quran_bookmark_ref,
                    bookmark.surahName,
                    bookmark.ayah.numberInSurah.toString(),
                ),
                subtitle = reciter.name,
            )
        }
    }

    /** Only expose resume in the car when the rest of its queue is already offline-ready. */
    private suspend fun androidAutoResumePlan(): AndroidAutoResumePlan? {
        val saved = sessionStore.session.first() ?: return null
        val byGlobal = quranRepository.allAyahs().associateBy { it.globalNumber }
        val knownReciters = Reciter.Bundled.mapTo(mutableSetOf()) { it.id }
        return withContext(Dispatchers.IO) {
            buildAndroidAutoResumePlan(saved, knownReciters) { global ->
                val ayah = byGlobal[global] ?: return@buildAndroidAutoResumePlan false
                val file = recitationRepository.fileFor(saved.intent.reciterId, ayah.surahNumber, global)
                file.isFile && file.length() > 0L
            }
        }
    }

    private suspend fun resumeSavedSession() {
        val plan = androidAutoResumePlan() ?: run {
            publishPlaybackError(getString(R.string.quran_car_not_downloaded))
            return
        }
        val globals = plan.intent.globalNumbers
        val byGlobal = quranRepository.allAyahs().associateBy { it.globalNumber }
        val ayahs = globals.mapNotNull(byGlobal::get)
        if (ayahs.size != globals.size) return
        val intent = plan.intent
        val queue = ayahs.map { ayah ->
            RecitationQueueItem(
                file = recitationRepository.fileFor(intent.reciterId, ayah.surahNumber, ayah.globalNumber),
                globalNumber = ayah.globalNumber,
            )
        }
        requestAudioFocus()
        sessionRuntime.begin(intent, plan.positionMs, plan.remainingRepeats)
        player.playQueue(
            items = queue,
            startIndex = 0,
            repeatCount = intent.repeatCount.coerceAtLeast(1),
            continuous = intent.continuous,
            startPositionMs = plan.positionMs,
            remainingRepeatsForCurrent = plan.remainingRepeats,
        )
    }

    private fun playMediaId(mediaId: String) {
        if (mediaId == RESUME_MEDIA_ID) {
            scope.launch { resumeSavedSession() }
            return
        }
        RecitationMediaId.parseBookmarkedAyah(mediaId)?.let { globalNumber ->
            scope.launch { playBookmarkedAyah(globalNumber) }
            return
        }
        val target = RecitationMediaId.parse(mediaId, Reciter.Bundled.mapTo(mutableSetOf()) { it.id }) ?: return
        scope.launch { playDownloadedSurah(target.first, target.second) }
    }

    private suspend fun playBookmarkedAyah(globalNumber: Int) {
        val ayah = quranRepository.ayahByGlobal(globalNumber) ?: return
        val reciter = recitationRepository.selectedReciter()
        val file = recitationRepository.fileFor(reciter.id, ayah.surahNumber, globalNumber)
        if (!file.isFile || file.length() <= 0L) {
            publishPlaybackError(getString(R.string.quran_car_not_downloaded))
            return
        }
        requestAudioFocus()
        player.playQueue(
            items = listOf(RecitationQueueItem(file, globalNumber)),
            startIndex = 0,
            repeatCount = 1,
        )
    }

    private suspend fun playDownloadedSurah(
        surahNumber: Int,
        requestedReciterId: String? = null,
        startGlobalNumber: Int? = null,
    ) {
        val surah = quranRepository.observeSurahs().first().firstOrNull { it.number == surahNumber } ?: return
        val reciter = requestedReciterId?.let { id -> Reciter.Bundled.firstOrNull { it.id == id } }
            ?: recitationRepository.selectedReciter()
        if (!recitationRepository.isSurahComplete(reciter.id, surah.number, surah.ayahCount)) {
            publishPlaybackError(getString(R.string.quran_car_not_downloaded))
            return
        }
        val queue = quranRepository.observeSurah(surah.number).first()
            .filter { ayah -> startGlobalNumber == null || ayah.globalNumber >= startGlobalNumber }
            .map { ayah ->
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
        scope.launch {
            val surahs = quranRepository.observeSurahs().first()
            val ayahs = quranRepository.allAyahs()
            val target = resolveAndroidAutoSearchTarget(surahs, ayahs, query)
            val surah = target?.let { value -> surahs.firstOrNull { it.number == value.surahNumber } }
            if (target != null && surah != null) {
                val selectedReciter = recitationRepository.selectedReciter()
                val reciterId = if (recitationRepository.isSurahComplete(selectedReciter.id, surah.number, surah.ayahCount)) {
                    selectedReciter.id
                } else {
                    findDownloadedReciter(surah)
                }
                if (reciterId == null) {
                    publishPlaybackError(getString(R.string.quran_car_search_unavailable))
                } else {
                    playDownloadedSurah(surah.number, reciterId, target.startGlobalNumber)
                }
            } else {
                publishPlaybackError(getString(R.string.quran_car_search_unavailable))
            }
        }
    }

    private suspend fun findDownloadedReciter(surah: Surah): String? {
        for (reciter in downloadedReciters()) {
            if (recitationRepository.isSurahComplete(reciter.id, surah.number, surah.ayahCount)) return reciter.id
        }
        return null
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
        private const val BOOKMARKS_FOLDER_ID = "muslim_bookmarks"
        private const val RESUME_MEDIA_ID = "muslim_resume_recitation"
        private const val RECITER_MEDIA_PREFIX = "muslim_reciter_"
        private const val SURAH_MEDIA_PREFIX = "muslim_surah_"

        /** New identity ensures Android creates a fresh media card after the branding upgrade. */
        const val RECITATION_NOTIFICATION_ID = 7008
        /** Most recent foreground-card identity, retained for migration cleanup. */
        const val RETIRED_RECITATION_NOTIFICATION_ID = 7007
        private const val OLDER_RETIRED_RECITATION_NOTIFICATION_ID = 7006
        private const val OPEN_APP_REQUEST_CODE = 70061
        private const val SESSION_PERSIST_INTERVAL_MS = 2_000L
        /** Same extra key [org.muslim.app.MainActivity] reads for deep links. */
        private const val EXTRA_ROUTE = "org.muslim.app.extra.ROUTE"

        /** Clears the retained media card from the pre-branding APK before playback resumes. */
        fun cancelRetiredNotification(context: Context) {
            context.getSystemService(android.app.NotificationManager::class.java).apply {
                cancel(RETIRED_RECITATION_NOTIFICATION_ID)
                cancel(OLDER_RETIRED_RECITATION_NOTIFICATION_ID)
            }
        }

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
