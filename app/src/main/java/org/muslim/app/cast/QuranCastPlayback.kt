package org.muslim.app.cast

import android.content.Context
import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.muslim.app.feature.quran.domain.QuranCastPayload

/** Cast sender that keeps media transport and Quran screen data in sync. */
class QuranCastPlayback(
    context: Context,
    private val onSessionChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
) : SessionManagerListener<CastSession> {
    private val appContext = context.applicationContext
    private val castContext = CastContext.getSharedInstance(appContext)
    private var session = castContext.sessionManager.currentCastSession
    private var listening = false

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
        val mediaMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK).apply {
            putString(MediaMetadata.KEY_TITLE, "${payload.surahNumber}:${payload.ayahNumber}")
            putString(MediaMetadata.KEY_ARTIST, payload.reciterName)
            payload.translation?.let { putString(MediaMetadata.KEY_ALBUM_TITLE, it.text) }
            putString("quranAyahText", payload.arabicAyah)
            payload.translation?.let {
                putString("quranTranslation", it.text)
                putString("quranTranslationLanguage", it.languageTag)
            }
            putString("quranTafsir", Json.encodeToString(payload.tafsir))
            putString("quranPrayerLocation", Json.encodeToString(payload.prayerLocation))
            putString("quranPrayerTimes", Json.encodeToString(payload.prayerTimes))
            addImage(WebImage(Uri.parse(ARTWORK_URL)))
        }
        val mediaInfo = MediaInfo.Builder(payload.audioUrl)
            .setContentType("audio/mpeg")
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setMetadata(mediaMetadata)
            .build()
        client.load(
            MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(autoplay)
                .setCurrentTime(0L)
                .build(),
        )

        val configuredReceiver = CastReceiverConfig.configuredApplicationId(appContext)
        if (configuredReceiver == currentSession.applicationMetadata?.applicationId) {
            val payloadJson = Json.encodeToString(payload)
            currentSession.setMessageReceivedCallbacks(CUSTOM_NAMESPACE, NOOP_RECEIVER_MESSAGE_CALLBACK)
            currentSession.sendMessage(CUSTOM_NAMESPACE, payloadJson)
        } else {
            onError("Audio is playing. Register the Quran Web Receiver to show the Quran text and prayer times.")
        }
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
        session = castSession
        onSessionChanged(true)
    }

    companion object {
        const val CUSTOM_NAMESPACE = "urn:x-cast:org.muslim.quran"
        private const val ARTWORK_URL = "https://raw.githubusercontent.com/Alaa91H/Muslim/main/app/src/main/res/mipmap-hdpi/ic_muslim_launcher_v2028.png"
        private val NOOP_RECEIVER_MESSAGE_CALLBACK = com.google.android.gms.cast.Cast.MessageReceivedCallback { _, _, _ -> }
    }
}
