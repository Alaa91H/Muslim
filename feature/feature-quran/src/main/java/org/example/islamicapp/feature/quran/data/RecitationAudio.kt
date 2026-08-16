package org.example.islamicapp.feature.quran.data

import org.example.islamicapp.feature.quran.domain.Reciter

/**
 * Pure naming/URL rules for recitation audio — JVM-testable
 * (see `RecitationAudioTest`).
 *
 * Downloads are stored per reciter under the app's internal storage:
 * `filesDir/quran_audio/<reciter-id>/<globalAyahNumber>.mp3`
 * (privacy-first: everything stays on the device, and the directory is
 * cleared when the app is uninstalled).
 */
object RecitationAudio {

    /** CDN base for per-ayah MP3s (islamic.network, 128 kbps). */
    private const val CDN_BASE = "https://cdn.islamic.network/quran/audio/128"

    /** Root dir name under [android.content.Context.filesDir]. */
    private const val ROOT_DIR = "quran_audio"

    /** Remote URL of one ayah for [reciter]. */
    fun url(reciter: Reciter, globalAyahNumber: Int): String =
        "$CDN_BASE/${reciter.id}/$globalAyahNumber.mp3"

    /** Local file name of one downloaded ayah. */
    fun fileName(globalAyahNumber: Int): String = "$globalAyahNumber.mp3"

    /** Directory holding a reciter's downloads. */
    fun reciterDir(filesDir: java.io.File, reciter: Reciter): java.io.File =
        java.io.File(java.io.File(filesDir, ROOT_DIR), reciter.id)

    /** Local file for one ayah, or null when [reciterDir] isn't created yet. */
    fun fileFor(filesDir: java.io.File, reciter: Reciter, globalAyahNumber: Int): java.io.File =
        java.io.File(reciterDir(filesDir, reciter), fileName(globalAyahNumber))
}
