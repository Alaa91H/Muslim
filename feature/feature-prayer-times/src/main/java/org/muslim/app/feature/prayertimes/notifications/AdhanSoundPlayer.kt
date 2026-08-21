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
    private var audioTrack: AudioTrack? = null
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
                audioTrack?.let { if (it.playState == AudioTrack.PLAYSTATE_PLAYING) it.pause() }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.let { if (!it.isPlaying) runCatching { it.start() } }
                audioTrack?.let { if (it.playState == AudioTrack.PLAYSTATE_PAUSED) it.play() }
            }
        }
    }

    /** Requests transient audio focus (best-effort; never blocks playback). */
    private fun requestFocus() {
        val manager = audioManager ?: return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
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
        val resId = bundledSoundRes(sound)
        if (resId == 0) {
            // Resource missing (should never happen — ships with the app).
            playSynthesized(volumePercent, onFinished)
            return
        }
        stop()
        requestFocus()
        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        player.setDataSource(
            context,
            "android.resource://${context.packageName}/$resId".toUri(),
        )
        player.setOnPreparedListener { p ->
            if (mediaPlayer !== p) return@setOnPreparedListener
            val target = (volumePercent / 100f).coerceIn(0f, 1f)
            p.setVolume(target, target)
            p.start()
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
                playSynthesized(volumePercent, onFinished)
            }
            true
        }
        runCatching { player.prepareAsync() }
            .onFailure { onFinished() }
    }

    /** Plays [file] (user-picked or downloaded). */
    fun playFile(file: File, volumePercent: Int, onFinished: () -> Unit) {
        stop()
        requestFocus()
        val player = MediaPlayer()
        mediaPlayer = player
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
        )
        player.setDataSource(file.absolutePath)
        player.setOnPreparedListener { p ->
            if (mediaPlayer !== p) return@setOnPreparedListener
            val target = (volumePercent / 100f).coerceIn(0f, 1f)
            p.setVolume(target, target)
            p.start()
        }
        player.setOnCompletionListener {
            if (mediaPlayer === it) onFinished()
        }
        player.setOnErrorListener { p, _, _ ->
            if (mediaPlayer === p) {
                runCatching { p.release() }
                mediaPlayer = null
                abandonFocus()
                playSynthesized(volumePercent, onFinished)
            }
            true
        }
        runCatching { player.prepareAsync() }
            .onFailure { onFinished() }
    }

    /** Plays the synthesised default tone. */
    fun playSynthesized(volumePercent: Int, onFinished: () -> Unit) {
        stop()
        requestFocus()
        val samples = runCatching { AdhanSynthesizer.generate() }.getOrNull()
            ?: ShortArray(0)
        val minBuffer = AudioTrack.getMinBufferSize(
            AdhanSynthesizer.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(AdhanSynthesizer.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build(),
            )
            .setBufferSizeInBytes(maxOf(minBuffer, samples.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        audioTrack = track
        val buffer = ShortArray(samples.size)
        samples.copyInto(buffer)
        track.write(buffer, 0, buffer.size)
        val target = (volumePercent / 100f).coerceIn(0f, 1f)
        track.setVolume(target)
        track.play()

        // The track knows its own duration; schedule the completion callback.
        val durationMs = (samples.size.toDouble() / AdhanSynthesizer.SAMPLE_RATE * 1000).toLong()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(onFinished, durationMs + 200)
    }

    /**
     * Applies [volumePercent] (0..100) to whatever is currently playing so the
     * preview's volume slider is live — no need to restart the preview. Safe to
     * call when nothing is playing (no-op).
     */
    fun setVolume(volumePercent: Int) {
        val target = (volumePercent / 100f).coerceIn(0f, 1f)
        mediaPlayer?.let { runCatching { it.setVolume(target, target) } }
        audioTrack?.let { runCatching { it.setVolume(target) } }
    }

    fun stop() {
        mediaPlayer?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        mediaPlayer = null
        audioTrack?.let {
            runCatching { it.stop() }
            runCatching { it.release() }
        }
        audioTrack = null
        abandonFocus()
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
