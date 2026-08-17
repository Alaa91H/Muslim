package org.muslim.app.feature.prayertimes.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the adhan for [AdhanPlaybackService]: either the bundled synthesised
 * tone ([AdhanSynthesizer]) or a user-provided/downloaded audio file
 * ([AdhanSoundRepository]). Both honour the configured master volume with a
 * gradual fade-in (PROJECT_PROMPT.md §6: Fade-in تدريجي).
 */
@Singleton
class AdhanSoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var mediaPlayer: MediaPlayer? = null
    private var audioTrack: AudioTrack? = null

    /** Plays [file] (user-picked or downloaded). */
    fun playFile(file: File, volumePercent: Int, onFinished: () -> Unit) {
        stop()
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
            p.setVolume(0f, 0f)
            p.start()
            fadeIn(p, volumePercent / 100f)
        }
        player.setOnCompletionListener { onFinished() }
        player.setOnErrorListener { _, _, _ -> onFinished(); true }
        runCatching { player.prepare() }
    }

    /** Plays the synthesised default tone. */
    fun playSynthesized(volumePercent: Int, onFinished: () -> Unit) {
        stop()
        val samples = AdhanSynthesizer.generate()
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
        track.setVolume(0f)
        track.play()
        fadeInTrack(track, volumePercent / 100f)

        // The track knows its own duration; schedule the completion callback.
        val durationMs = (samples.size.toDouble() / AdhanSynthesizer.SAMPLE_RATE * 1000).toLong()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(onFinished, durationMs + 200)
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
    }

    private fun fadeIn(player: MediaPlayer, target: Float) {
        val start = System.currentTimeMillis()
        val runnable = object : Runnable {
            override fun run() {
                if (mediaPlayer !== player) return
                val progress = ((System.currentTimeMillis() - start).toFloat() / FADE_IN_MS).coerceAtMost(1f)
                val volume = target * progress
                player.setVolume(volume, volume)
                if (progress < 1f) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, FADE_STEP_MS)
                }
            }
        }
        android.os.Handler(android.os.Looper.getMainLooper()).post(runnable)
    }

    private fun fadeInTrack(track: AudioTrack, target: Float) {
        val start = System.currentTimeMillis()
        val runnable = object : Runnable {
            override fun run() {
                if (audioTrack !== track) return
                val progress = ((System.currentTimeMillis() - start).toFloat() / FADE_IN_MS).coerceAtMost(1f)
                track.setVolume(target * progress)
                if (progress < 1f) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, FADE_STEP_MS)
                }
            }
        }
        android.os.Handler(android.os.Looper.getMainLooper()).post(runnable)
    }

    private companion object {
        const val FADE_IN_MS = 4_000L
        const val FADE_STEP_MS = 50L
    }
}
