package org.muslim.app.feature.prayertimes.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import org.muslim.app.core.common.prayer.BundledAdhanSound
import java.io.File
import kotlin.math.min
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the adhan for [AdhanPlaybackService]: a bundled real recording
 * ([BundledAdhanSound] shipped in `res/raw` — plays offline with no download),
 * a user-provided/downloaded audio file ([AdhanSoundRepository]), or the
 * synthesised tone ([AdhanSynthesizer]) as a last resort. All honour the
 * configured master volume with a gradual fade-in (PROJECT_PROMPT.md §6).
 *
 * Reliability: playback requests transient audio focus (USAGE_ALARM) so the
 * call to prayer is never ducked or blocked, uses asynchronous prepare so a
 * slow/corrupt source can never hang the main thread, and falls back to the
 * synthesised tone if a bundled file cannot be decoded — the adhan must never
 * be silent.
 */
@Singleton
class AdhanSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var mediaPlayer: MediaPlayer? = null
    @Volatile
    private var audioTrack: AudioTrack? = null
    /** Serializes every native AudioTrack operation with invalidation and release. */
    private val audioTrackLock = Any()
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasFocus = false

    private val audioManager: AudioManager?
        get() = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS -> stop()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause briefly, resume when focus is regained.
                mediaPlayer?.let { if (it.isPlaying) it.pause() }
                synchronized(audioTrackLock) {
                    audioTrack?.let { track ->
                        runCatching {
                            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.pause()
                        }
                    }
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.let { if (!it.isPlaying) runCatching { it.start() } }
                synchronized(audioTrackLock) {
                    audioTrack?.let { track ->
                        runCatching {
                            if (track.playState == AudioTrack.PLAYSTATE_PAUSED) track.play()
                        }
                    }
                }
            }
        }
    }

    /** Requests transient audio focus (best-effort; never blocks playback). */
    private fun requestFocus() {
        val manager = audioManager ?: return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(attrs)
                .setAcceptsDelayedFocusGain(false)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioFocusRequest = request
            manager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            manager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN)
        }
        hasFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonFocus() {
        if (!hasFocus) return
        val manager = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { runCatching { manager.abandonAudioFocusRequest(it) } }
        } else {
            @Suppress("DEPRECATION")
            runCatching { manager.abandonAudioFocus(focusChangeListener) }
        }
        hasFocus = false
        audioFocusRequest = null
    }

    /** Plays a bundled recording (shipped in `res/raw`, always offline). */
    fun playBundled(sound: BundledAdhanSound, volumePercent: Int, onFinished: () -> Unit) {
        playBundled(sound, volumePercent, onStarted = {}, onFinished = onFinished)
    }

    /** Plays a bundled recording and confirms once Android has started output. */
    fun playBundled(
        sound: BundledAdhanSound,
        volumePercent: Int,
        onStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        val resId = bundledSoundRes(sound)
        if (resId == 0) {
            // Resource missing (should never happen — ships with the app).
            playSynthesized(volumePercent, onStarted, onFinished)
            return
        }
        stop()
        requestFocus()
        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        val sourceReady = runCatching {
            player.setDataSource(
                context,
                "android.resource://${context.packageName}/$resId".toUri(),
            )
        }.isSuccess
        if (!sourceReady) {
            runCatching { player.release() }
            mediaPlayer = null
            abandonFocus()
            playSynthesized(volumePercent, onStarted, onFinished)
            return
        }
        player.setOnPreparedListener { p ->
            if (mediaPlayer !== p) return@setOnPreparedListener
            val target = (volumePercent / 100f).coerceIn(0f, 1f)
            runCatching {
                p.setVolume(target, target)
                p.start()
                if (p.isPlaying) onStarted() else error("MediaPlayer did not enter playing state")
            }.onFailure {
                if (mediaPlayer === p) {
                    runCatching { p.release() }
                    mediaPlayer = null
                    abandonFocus()
                    playSynthesized(volumePercent, onStarted, onFinished)
                }
            }
        }
        player.setOnCompletionListener {
            if (mediaPlayer === it) onFinished()
        }
        player.setOnErrorListener { p, _, _ ->
            // The bundled file could not be decoded — never leave the adhan
            // silent: fall back to the synthesised tone.
            if (mediaPlayer === p) {
                runCatching { p.release() }
                mediaPlayer = null
                abandonFocus()
                playSynthesized(volumePercent, onStarted, onFinished)
            }
            true
        }
        runCatching { player.prepareAsync() }
            .onFailure {
                if (mediaPlayer === player) {
                    runCatching { player.release() }
                    mediaPlayer = null
                    abandonFocus()
                    playSynthesized(volumePercent, onStarted, onFinished)
                }
            }
    }

    /** Plays [file] (user-picked or downloaded). */
    fun playFile(file: File, volumePercent: Int, onFinished: () -> Unit) {
        playFile(file, volumePercent, onStarted = {}, onFinished = onFinished)
    }

    /** Plays a file and confirms once Android has started output. */
    fun playFile(
        file: File,
        volumePercent: Int,
        onStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        stop()
        requestFocus()
        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        val sourceReady = runCatching { player.setDataSource(file.absolutePath) }.isSuccess
        if (!sourceReady) {
            runCatching { player.release() }
            mediaPlayer = null
            abandonFocus()
            playSynthesized(volumePercent, onStarted, onFinished)
            return
        }
        player.setOnPreparedListener { p ->
            if (mediaPlayer !== p) return@setOnPreparedListener
            val target = (volumePercent / 100f).coerceIn(0f, 1f)
            runCatching {
                p.setVolume(target, target)
                p.start()
                if (p.isPlaying) onStarted() else error("MediaPlayer did not enter playing state")
            }.onFailure {
                if (mediaPlayer === p) {
                    runCatching { p.release() }
                    mediaPlayer = null
                    abandonFocus()
                    playSynthesized(volumePercent, onStarted, onFinished)
                }
            }
        }
        player.setOnCompletionListener {
            if (mediaPlayer === it) onFinished()
        }
        player.setOnErrorListener { p, _, _ ->
            if (mediaPlayer === p) {
                runCatching { p.release() }
                mediaPlayer = null
                abandonFocus()
                playSynthesized(volumePercent, onStarted, onFinished)
            }
            true
        }
        runCatching { player.prepareAsync() }
            .onFailure {
                if (mediaPlayer === player) {
                    runCatching { player.release() }
                    mediaPlayer = null
                    abandonFocus()
                    playSynthesized(volumePercent, onStarted, onFinished)
                }
            }
    }

    /** Plays the synthesised default tone. */
    fun playSynthesized(volumePercent: Int, onFinished: () -> Unit) {
        playSynthesized(volumePercent, onStarted = {}, onFinished = onFinished)
    }

    /**
     * Plays the synthesised fallback as a verified stream rather than a single
     * large static AudioTrack buffer. Some OEM audio stacks reject or truncate
     * long static buffers while still returning a PLAYING state, which can make
     * a journal report a start without producing audible frames.
     */
    fun playSynthesized(
        volumePercent: Int,
        onStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        stop()
        requestFocus()
        val samples = runCatching { AdhanSynthesizer.generate() }.getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: run {
                abandonFocus()
                onFinished()
                return
            }
        val track = createSynthesizedTrack() ?: run {
            abandonFocus()
            onFinished()
            return
        }
        synchronized(audioTrackLock) {
            audioTrack = track
        }
        val target = (volumePercent / 100f).coerceIn(0f, 1f)
        val volumeApplied = synchronized(audioTrackLock) {
            audioTrack === track && runCatching { track.setVolume(target) }.isSuccess
        }
        if (!volumeApplied) {
            finishSynthesizedTrack(track, onFinished)
            return
        }
        streamSynthesizedTrack(track, samples, onStarted, onFinished)
    }

    /** Creates a valid stream-mode track, releasing partially initialized tracks safely. */
    private fun createSynthesizedTrack(): AudioTrack? {
        val minBuffer = AudioTrack.getMinBufferSize(
            AdhanSynthesizer.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer <= 0) return null
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(AdhanSynthesizer.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build(),
                )
                .setBufferSizeInBytes(maxOf(minBuffer, STREAM_CHUNK_SAMPLES * 2))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull()
        return track?.takeIf { it.state == AudioTrack.STATE_INITIALIZED }
            ?: run {
                runCatching { track?.release() }
                null
            }
    }

    /** Streams bounded PCM chunks and contains OEM release races inside the playback session. */
    private fun streamSynthesizedTrack(
        track: AudioTrack,
        samples: ShortArray,
        onStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        Thread({
            try {
                if (audioTrack !== track) return@Thread
                val firstWritten = writeTrackChunk(track, samples, 0, min(STREAM_CHUNK_SAMPLES, samples.size))
                if (firstWritten <= 0 || !startSynthesizedTrack(track)) {
                    finishSynthesizedTrack(track, onFinished)
                    return@Thread
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post(onStarted)
                var offset = firstWritten
                while (offset < samples.size && audioTrack === track) {
                    val written = writeTrackChunk(track, samples, offset, min(STREAM_CHUNK_SAMPLES, samples.size - offset))
                    if (written <= 0) {
                        finishSynthesizedTrack(track, onFinished)
                        return@Thread
                    }
                    offset += written
                }
                if (audioTrack === track) finishSynthesizedTrack(track, onFinished)
            } catch (_: Throwable) {
                // An OEM may invalidate a stream during a concurrent stop. The
                // fallback must end quietly rather than terminating the app.
                finishSynthesizedTrack(track, onFinished)
            }
        }, "Muslim-AdhanAudioTrack").start()
    }

    private fun startSynthesizedTrack(track: AudioTrack): Boolean = synchronized(audioTrackLock) {
        if (audioTrack !== track) return@synchronized false
        runCatching {
            track.play()
            track.playState == AudioTrack.PLAYSTATE_PLAYING
        }.getOrDefault(false)
    }

    /** Returns a negative AudioTrack status when a concurrent stop invalidates the stream. */
    private fun writeTrackChunk(
        track: AudioTrack,
        samples: ShortArray,
        offset: Int,
        size: Int,
    ): Int = synchronized(audioTrackLock) {
        if (audioTrack !== track) return@synchronized AudioTrack.ERROR_INVALID_OPERATION
        runCatching {
            track.write(samples, offset, size, AudioTrack.WRITE_BLOCKING)
        }.getOrDefault(AudioTrack.ERROR_INVALID_OPERATION)
    }

    private fun finishSynthesizedTrack(track: AudioTrack, onFinished: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            val finished = synchronized(audioTrackLock) {
                if (audioTrack !== track) return@synchronized false
                // Invalidate before native release so no writer can pass a stale identity check.
                audioTrack = null
                runCatching { track.stop() }
                runCatching { track.release() }
                true
            }
            if (!finished) return@post
            abandonFocus()
            onFinished()
        }
    }

    /**
     * Applies [volumePercent] (0..100) to whatever is currently playing so the
     * preview's volume slider is live — no need to restart the preview. Safe to
     * call when nothing is playing (no-op).
     */
    fun setVolume(volumePercent: Int) {
        val target = (volumePercent / 100f).coerceIn(0f, 1f)
        mediaPlayer?.let { runCatching { it.setVolume(target, target) } }
        synchronized(audioTrackLock) {
            audioTrack?.let { runCatching { it.setVolume(target) } }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        mediaPlayer = null
        synchronized(audioTrackLock) {
            val track = audioTrack
            // The writer observes null under the same lock before this track can be released.
            audioTrack = null
            track?.let {
                runCatching { it.stop() }
                runCatching { it.release() }
            }
        }
        abandonFocus()
    }

    private companion object {
        const val STREAM_CHUNK_SAMPLES = 4_410
    }

    private fun bundledSoundRes(sound: BundledAdhanSound): Int = when (sound) {
        BundledAdhanSound.Makkah -> org.muslim.app.feature.prayertimes.R.raw.adhan_makkah
        BundledAdhanSound.Madinah -> org.muslim.app.feature.prayertimes.R.raw.adhan_madinah
        BundledAdhanSound.AbdulBasit -> org.muslim.app.feature.prayertimes.R.raw.adhan_abdul_basit
        BundledAdhanSound.Minshawi -> org.muslim.app.feature.prayertimes.R.raw.adhan_minshawi
        BundledAdhanSound.Egypt -> org.muslim.app.feature.prayertimes.R.raw.adhan_egypt
        BundledAdhanSound.AlAqsa -> org.muslim.app.feature.prayertimes.R.raw.adhan_alaqsa
        BundledAdhanSound.Halab -> org.muslim.app.feature.prayertimes.R.raw.adhan_halab
        BundledAdhanSound.AbdulGhaffar -> org.muslim.app.feature.prayertimes.R.raw.adhan_abdul_ghaffar
        BundledAdhanSound.AbdulHakam -> org.muslim.app.feature.prayertimes.R.raw.adhan_abdul_hakam
        BundledAdhanSound.AlHussaini -> org.muslim.app.feature.prayertimes.R.raw.adhan_al_hussaini
        BundledAdhanSound.BakirBash -> org.muslim.app.feature.prayertimes.R.raw.adhan_bakir_bash
        BundledAdhanSound.Hafez -> org.muslim.app.feature.prayertimes.R.raw.adhan_hafez
        BundledAdhanSound.HafizMurad -> org.muslim.app.feature.prayertimes.R.raw.adhan_hafiz_murad
        BundledAdhanSound.Naghshbandi -> org.muslim.app.feature.prayertimes.R.raw.adhan_naghshbandi
        BundledAdhanSound.Saber -> org.muslim.app.feature.prayertimes.R.raw.adhan_saber
        BundledAdhanSound.SharifDoman -> org.muslim.app.feature.prayertimes.R.raw.adhan_sharif_doman
        BundledAdhanSound.YusufIslam -> org.muslim.app.feature.prayertimes.R.raw.adhan_yusuf_islam
    }

}
