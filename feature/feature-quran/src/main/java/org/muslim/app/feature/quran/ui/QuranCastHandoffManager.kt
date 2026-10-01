package org.muslim.app.feature.quran.ui

import org.muslim.app.core.cast.LocalCastMediaServer
import org.muslim.app.feature.quran.data.QuranAudioPlayer
import org.muslim.app.feature.quran.data.RemotePlaybackCommand
import org.muslim.app.feature.quran.data.RecitationRepository
import org.muslim.app.feature.quran.domain.Reciter
import org.muslim.app.feature.quran.domain.QuranCastPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranCastHandoffManager @Inject constructor(
    private val player: QuranAudioPlayer,
    private val repository: RecitationRepository,
) {
    private var mediaServer: LocalCastMediaServer? = null
    var onRemoteCommand: ((RemotePlaybackCommand) -> Unit)? = null
        set(value) { field = value; player.onRemoteCommand = value }

    fun localMediaUrl(reciter: Reciter, surahNumber: Int, globalNumber: Int): String? = runCatching {
        mediaServer?.close()
        LocalCastMediaServer(repository.fileFor(reciter.id, surahNumber, globalNumber)).also { mediaServer = it }.start()
    }.getOrNull()

    fun mediaUrl(payload: QuranCastPayload): String? {
        val file = repository.fileFor(payload.reciterId, payload.surahNumber, payload.globalAyahNumber)
        if (file.isFile && file.length() > 0L) {
            return runCatching {
                mediaServer?.close()
                LocalCastMediaServer(file).also { mediaServer = it }.start()
            }.getOrNull()
        }
        return payload.audioUrl.takeIf { it.startsWith("https://") }
    }

    fun handoffToRemote(positionMs: Long) = player.handoffToRemote(positionMs)

    fun acceptRemotePosition(positionMs: Long, durationMs: Long, playing: Boolean) =
        player.acceptRemotePosition(positionMs, durationMs, playing)

    fun onRemoteMediaEnded() = player.onRemoteMediaEnded()

    fun handoffToLocal(positionMs: Long, playing: Boolean) {
        mediaServer?.close()
        mediaServer = null
        player.leaveRemote(positionMs, playing)
    }

    fun endSession() {
        mediaServer?.close()
        mediaServer = null
    }

    fun isDownloaded(reciter: Reciter, surahNumber: Int, globalNumber: Int): Boolean =
        repository.fileFor(reciter.id, surahNumber, globalNumber).let { it.isFile && it.length() > 0L }
}
