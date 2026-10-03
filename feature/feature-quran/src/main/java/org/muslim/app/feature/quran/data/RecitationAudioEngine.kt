package org.muslim.app.feature.quran.data

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Abstraction over the Android [MediaPlayer] used by [QuranAudioPlayer].
 *
 * The real implementation is [MediaPlayerAudioEngine]; tests substitute a fake
 * so the queue / repeat / state-transition logic can be unit-tested on the JVM
 * without the Android framework.
 */
interface RecitationAudioEngine {
    val durationMs: Int
    val positionMs: Int
    fun prepareAsync()
    fun start()
    fun pause()
    fun seekTo(msec: Int)
    fun stop()
    fun release()
    fun setOnPreparedListener(listener: () -> Unit)
    fun setOnCompletionListener(listener: () -> Unit)
    fun setOnErrorListener(listener: () -> Unit)
}

/** Creates an engine for one audio file; returns null when the file can't open. */
fun interface RecitationEngineFactory {
    fun create(file: File): RecitationAudioEngine?

    /** Keeps file-backed callers compatible while allowing network-backed queue entries. */
    fun create(item: RecitationQueueItem): RecitationAudioEngine? = create(item.file)
}

/** Real [RecitationAudioEngine] backed by a [MediaPlayer]. */
class MediaPlayerAudioEngine private constructor(
    private val player: MediaPlayer,
) : RecitationAudioEngine {

    override val durationMs: Int get() = runCatching { player.duration }.getOrDefault(0)
    override val positionMs: Int get() = runCatching { player.currentPosition }.getOrDefault(0)

    override fun prepareAsync() = player.prepareAsync()
    override fun start() = player.start()
    override fun pause() = player.pause()
    override fun seekTo(msec: Int) = player.seekTo(msec)
    override fun stop() = player.stop()
    override fun release() = player.release()

    override fun setOnPreparedListener(listener: () -> Unit) {
        player.setOnPreparedListener { listener() }
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        player.setOnCompletionListener { listener() }
    }

    override fun setOnErrorListener(listener: () -> Unit) {
        player.setOnErrorListener { _, _, _ ->
            listener()
            true
        }
    }

    class Factory @Inject constructor(
        @ApplicationContext private val context: Context,
    ) : RecitationEngineFactory {
        override fun create(file: File): RecitationAudioEngine? = runCatching {
            createPlayer { setDataSource(file.absolutePath) }
        }.getOrNull()

        override fun create(item: RecitationQueueItem): RecitationAudioEngine? = runCatching {
            val url = item.streamUrl
            if (url.isNullOrBlank()) return@runCatching create(item.file)
            val uri = Uri.parse(url)
            require(uri.scheme == "https" && uri.host == "everyayah.com")
            createPlayer { setDataSource(context, uri) }
        }.getOrNull()

        private fun createPlayer(setDataSource: MediaPlayer.() -> Unit): MediaPlayerAudioEngine =
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource()
            }.let(::MediaPlayerAudioEngine)
    }
}
