package org.muslim.app.cast

import android.content.Context
import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.muslim.app.feature.quran.domain.QuranCastPayload
import org.muslim.app.feature.quran.data.RecitationPlaybackSnapshot
import org.muslim.app.feature.quran.data.RecitationQueueItem
import org.muslim.app.feature.quran.domain.CastPlaybackState
import org.muslim.app.feature.quran.data.RemotePlaybackCommand

data class QuranCastPlaybackCallbacks(
    val onSessionChanged: (Boolean) -> Unit,
    val onError: (String) -> Unit,
    val mediaUrlFor: (QuranCastPayload) -> String?,
    val onRemoteEnded: () -> Unit,
    val onRemoteAccepted: (Long) -> Unit,
    val onRemoteProgress: (Long, Long, Boolean) -> Unit,
    val onRemoteItemEnded: () -> Unit,
    val customReceiverEnabled: Boolean,
)

/** Cast sender that keeps media transport and Quran screen data in sync. */
class QuranCastPlayback(
    context: Context,
    callbacks: QuranCastPlaybackCallbacks,
) : SessionManagerListener<CastSession> {
    private val onSessionChanged = callbacks.onSessionChanged
    private val onError = callbacks.onError
    private val mediaUrlFor = callbacks.mediaUrlFor
    private val onRemoteEnded = callbacks.onRemoteEnded
    private val onRemoteAccepted = callbacks.onRemoteAccepted
    private val onRemoteProgress = callbacks.onRemoteProgress
    private val onRemoteItemEnded = callbacks.onRemoteItemEnded
    private val customReceiverEnabled = callbacks.customReceiverEnabled
    private val appContext = context.applicationContext
    private val castContext = CastContext.getSharedInstance(appContext)
    private var session = castContext.sessionManager.currentCastSession
    private var listening = false
    private var lastPayload: QuranCastPayload? = null
    private var loadedGlobalAyah: Int? = null
    private var completionHandled = false
    private val progressListener = RemoteMediaClient.ProgressListener { position, duration ->
        val playing = remoteIsPlaying()
        lastRemotePosition = position to playing
        onRemoteProgress(position, duration, playing)
    }
    private val mediaCallback = object : RemoteMediaClient.Callback() {
        override fun onStatusUpdated() {
            val client = session?.remoteMediaClient ?: return
            val mediaStatus = client.mediaStatus
            if (mediaStatus?.playerState == MediaStatus.PLAYER_STATE_PLAYING) completionHandled = false
            if (mediaStatus?.playerState == MediaStatus.PLAYER_STATE_IDLE &&
                mediaStatus.idleReason == MediaStatus.IDLE_REASON_FINISHED && !completionHandled
            ) {
                completionHandled = true
                onRemoteItemEnded()
            }
            val position = client.approximateStreamPosition.coerceAtLeast(0L)
            val duration = client.streamDuration.coerceAtLeast(0L)
            val playing = client.isPlaying
            lastRemotePosition = position to playing
            onRemoteProgress(position, duration, playing)
        }
    }

    fun start() {
        if (listening) return
        castContext.sessionManager.addSessionManagerListener(this, CastSession::class.java)
        listening = true
        session?.let { onSessionChanged(it.isConnected) }
    }

    fun end() {
        if (!listening) return
        castContext.sessionManager.removeSessionManagerListener(this, CastSession::class.java)
        listening = false
        castContext.sessionManager.endCurrentSession(true)
        session = null
        onSessionChanged(false)
    }

    fun release() {
        if (!listening) return
        castContext.sessionManager.removeSessionManagerListener(this, CastSession::class.java)
        listening = false
    }

    fun isConnected(): Boolean = session?.isConnected == true
    fun deviceName(): String? = session?.castDevice?.friendlyName

    fun load(payload: QuranCastPayload, autoplay: Boolean = true) {
        val currentSession = session
        if (currentSession?.isConnected != true) {
            onError("Connect to a Cast device first.")
            return
        }
        val client = currentSession.remoteMediaClient
        if (client == null) {
            onError("The Cast receiver did not provide a media client.")
            return
        }
        val contentUrl = mediaUrlFor(payload)
        if (contentUrl == null) { onError("Could not serve this downloaded ayah to the Cast device."); return }
        lastPayload = payload
        completionHandled = false
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, "${payload.surahNumber}:${payload.ayahNumber}")
            putString(MediaMetadata.KEY_ARTIST, payload.reciterName)
            payload.translation?.let { putString(MediaMetadata.KEY_ALBUM_TITLE, it.text) }
            putString("quranAyahText", payload.arabicAyah)
            payload.translation?.let {
                putString("quranTranslation", it.text)
                putString("quranTranslationLanguage", it.languageTag)
            }
            putString("quranPayload", Json.encodeToString(payload))
            addImage(WebImage(Uri.parse(ARTWORK_URL)))
        }
        val mediaInfo = MediaInfo.Builder(contentUrl)
            .setContentType("audio/mpeg")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(mediaMetadata)
            .build()
        client.load(
            MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(autoplay)
                .setCurrentTime(payload.positionMs)
                .build(),
        ).addStatusListener { status ->
            if (status.status.isSuccess) onRemoteAccepted(payload.positionMs)
            else onError("Cast media failed to load (${status.status.statusCode}).")
        }

        sendFullState(currentSession, payload)
    }

    private fun sendFullState(target: CastSession, payload: QuranCastPayload) {
        if (!customReceiverEnabled) return
        val callback = com.google.android.gms.cast.Cast.MessageReceivedCallback { _, _, message ->
            val type = runCatching { Json.parseToJsonElement(message).jsonObject["type"]?.jsonPrimitive?.content }.getOrNull()
            when (type) {
                "REQUEST_FULL_STATE", "UI_READY" -> lastPayload?.let { sendFullState(target, it) }
                "MEDIA_ERROR" -> onError("The Cast receiver could not load Quran audio.")
                "UNSUPPORTED_SCHEMA" -> onError("The Cast receiver does not support this Quran state version.")
            }
        }
        runCatching { target.removeMessageReceivedCallbacks(CUSTOM_NAMESPACE) }
        target.setMessageReceivedCallbacks(CUSTOM_NAMESPACE, callback)
        target.sendMessage(CUSTOM_NAMESPACE, "{\"type\":\"FULL_STATE\",\"payload\":${Json.encodeToString(payload)}}")
    }

    fun togglePlayback() {
        val client = session?.remoteMediaClient ?: return
        if (client.isPlaying) client.pause() else client.play()
    }

    fun syncPlayback(shouldPlay: Boolean) {
        val client = session?.remoteMediaClient ?: return
        if (client.isPlaying != shouldPlay) {
            if (shouldPlay) client.play() else client.pause()
        }
    }

    fun seekTo(positionMs: Long) {
        session?.remoteMediaClient?.seek(
            MediaSeekOptions.Builder().setPosition(positionMs.coerceAtLeast(0L)).build(),
        )
    }

    fun execute(command: RemotePlaybackCommand) {
        when (command) {
            RemotePlaybackCommand.Play -> session?.remoteMediaClient?.play()
            RemotePlaybackCommand.Pause -> session?.remoteMediaClient?.pause()
            RemotePlaybackCommand.Stop -> session?.remoteMediaClient?.stop()
            RemotePlaybackCommand.Next, RemotePlaybackCommand.Previous -> Unit
            is RemotePlaybackCommand.Seek -> seekTo(command.positionMs)
        }
    }

    fun loadSnapshot(payload: QuranCastPayload, snapshot: RecitationPlaybackSnapshot) {
        val current = payload.copy(
            queueGlobalNumbers = snapshot.queue.map(RecitationQueueItem::globalNumber),
            queueIndex = snapshot.queueIndex,
            repeatCount = snapshot.repeatCount,
            remainingRepeats = snapshot.remainingRepeats,
            positionMs = snapshot.positionMs,
            durationMs = snapshot.durationMs.takeIf { it > 0 },
            playbackState = when (snapshot.state) {
                org.muslim.app.feature.quran.data.PlaybackState.Playing -> CastPlaybackState.PLAYING
                org.muslim.app.feature.quran.data.PlaybackState.Paused -> CastPlaybackState.PAUSED
                org.muslim.app.feature.quran.data.PlaybackState.Idle -> CastPlaybackState.IDLE
            },
        )
        if (loadedGlobalAyah != current.globalAyahNumber) {
            load(current, autoplay = snapshot.state == org.muslim.app.feature.quran.data.PlaybackState.Playing)
            loadedGlobalAyah = current.globalAyahNumber
        } else {
            lastPayload = current
            session?.let { sendFullState(it, current) }
            syncPlayback(snapshot.state == org.muslim.app.feature.quran.data.PlaybackState.Playing)
        }
    }

    fun sendCurrentState(payload: QuranCastPayload) {
        session?.let { sendFullState(it, payload) }
    }


    fun remotePositionMs(): Long = session?.remoteMediaClient?.approximateStreamPosition ?: 0L
    fun remoteDurationMs(): Long = session?.remoteMediaClient?.streamDuration?.toLong() ?: 0L
    fun remoteIsPlaying(): Boolean = session?.remoteMediaClient?.isPlaying == true
    @Volatile var lastRemotePosition: Pair<Long, Boolean> = 0L to false
        private set

    fun addMediaCallback(callback: RemoteMediaClient.Callback) {
        session?.remoteMediaClient?.registerCallback(callback)
    }

    fun removeMediaCallback(callback: RemoteMediaClient.Callback) {
        session?.remoteMediaClient?.unregisterCallback(callback)
    }

    fun addProgressListener(listener: RemoteMediaClient.ProgressListener) {
        session?.remoteMediaClient?.addProgressListener(listener, 1_000)
    }

    fun removeProgressListener(listener: RemoteMediaClient.ProgressListener) {
        session?.remoteMediaClient?.removeProgressListener(listener)
    }

    override fun onSessionStarted(castSession: CastSession, sessionId: String) = updateSession(castSession)
    override fun onSessionResumed(castSession: CastSession, wasSuspended: Boolean) = updateSession(castSession)
    override fun onSessionEnded(castSession: CastSession, error: Int) {
        lastRemotePosition = remotePositionMs() to remoteIsPlaying()
        castSession.remoteMediaClient?.unregisterCallback(mediaCallback)
        castSession.remoteMediaClient?.removeProgressListener(progressListener)
        onRemoteEnded()
        session = null
        onSessionChanged(false)
    }

    override fun onSessionStartFailed(castSession: CastSession, error: Int) = onError("Cast could not connect ($error).")
    override fun onSessionResumeFailed(castSession: CastSession, error: Int) = onError("Cast could not resume ($error).")
    override fun onSessionStarting(castSession: CastSession) = Unit
    override fun onSessionResuming(castSession: CastSession, sessionId: String) = Unit
    override fun onSessionEnding(castSession: CastSession) = Unit
    override fun onSessionSuspended(castSession: CastSession, reason: Int) = Unit

    private fun updateSession(castSession: CastSession) {
        session?.remoteMediaClient?.unregisterCallback(mediaCallback)
        session?.remoteMediaClient?.removeProgressListener(progressListener)
        session = castSession
        castSession.remoteMediaClient?.registerCallback(mediaCallback)
        castSession.remoteMediaClient?.addProgressListener(progressListener, 1_000)
        onSessionChanged(true)
        lastPayload?.let { sendFullState(castSession, it) }
    }

    companion object {
        const val CUSTOM_NAMESPACE = "urn:x-cast:org.muslim.quran"
        private const val ARTWORK_URL = "https://raw.githubusercontent.com/Alaa91H/Muslim/main/app/src/main/res/mipmap-hdpi/ic_muslim_launcher_v2028.png"
    }
}
