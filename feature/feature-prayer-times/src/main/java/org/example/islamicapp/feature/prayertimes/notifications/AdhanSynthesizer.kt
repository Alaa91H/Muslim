package org.example.islamicapp.feature.prayertimes.notifications

import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates a dignified melodic call-to-prayer as raw 16-bit PCM — the app's
 * bundled default adhan sound (PROJECT_PROMPT.md §6: مكتبة أصوات الأذان).
 *
 * Shipping real recordings would require licensed assets; instead the default
 * is synthesised in code (public domain) and users can replace it per-prayer
 * with their own audio file or an optional download (see [AdhanSoundRepository]).
 *
 * The melody is a simple maqam-flavoured rendering of the adhan phrases:
 * takbir ×4, the two shahadas ×2, hayya 'ala-s-salah ×2, hayya 'ala-l-falah ×2,
 * takbir ×2, and the tahlil — keeping the traditional rhythm and phrasing.
 */
object AdhanSynthesizer {

    const val SAMPLE_RATE = 44_100
    private const val ATTACK_MS = 15.0
    private const val RELEASE_MS = 60.0

    private data class Note(val frequency: Double, val durationMs: Double)

    private fun n(freq: Double, ms: Int) = Note(freq, ms.toDouble())

    private val TAKBIR = listOf(
        n(440.0, 260), n(440.0, 260), n(523.25, 260), n(587.33, 520),
        n(659.25, 260), n(587.33, 260), n(523.25, 520),
    )
    private val SHAHADA_1 = listOf(
        n(440.0, 240), n(440.0, 240), n(523.25, 240), n(659.25, 400),
        n(659.25, 240), n(587.33, 240), n(523.25, 360), n(440.0, 480),
    )
    private val SHAHADA_2 = listOf(
        n(440.0, 240), n(440.0, 240), n(523.25, 240), n(659.25, 400),
        n(659.25, 240), n(783.99, 240), n(659.25, 360), n(587.33, 480),
    )
    private val HAYYA_SALAH = listOf(
        n(587.33, 300), n(659.25, 300), n(587.33, 300), n(659.25, 600),
    )
    private val HAYYA_FALAH = listOf(
        n(659.25, 300), n(783.99, 300), n(659.25, 300), n(783.99, 600),
    )
    private val TAHLIL = listOf(
        n(440.0, 300), n(440.0, 300), n(523.25, 520), n(587.33, 700),
    )

    private val GAP_MS = 140.0

    private val PHRASES = buildList {
        repeat(4) { add(TAKBIR) }
        repeat(2) { add(SHAHADA_1) }
        repeat(2) { add(SHAHADA_2) }
        repeat(2) { add(HAYYA_SALAH) }
        repeat(2) { add(HAYYA_FALAH) }
        repeat(2) { add(TAKBIR) }
        add(TAHLIL)
    }

    /** Total duration of the generated melody in milliseconds. */
    val durationMs: Long = PHRASES.sumOf { phrase ->
        phrase.sumOf { it.durationMs } + GAP_MS
    }.toLong()

    /**
     * Renders the full melody into a 16-bit signed little-endian mono PCM
     * buffer at [SAMPLE_RATE]. Returns `ShortArray` of samples.
     */
    fun generate(): ShortArray {
        val totalSamples = (durationMs / 1000.0 * SAMPLE_RATE).toInt()
        val samples = ShortArray(totalSamples)
        var cursor = 0

        PHRASES.forEach { phrase ->
            phrase.forEach { note ->
                cursor = renderNote(note, samples, cursor)
                cursor += (GAP_MS / 1000.0 * SAMPLE_RATE).toInt()
            }
        }
        return samples
    }

    private fun renderNote(note: Note, samples: ShortArray, start: Int): Int {
        val length = (note.durationMs / 1000.0 * SAMPLE_RATE).toInt()
        val attackSamples = (ATTACK_MS / 1000.0 * SAMPLE_RATE).toInt()
        val releaseSamples = (RELEASE_MS / 1000.0 * SAMPLE_RATE).toInt()
        val omega = 2.0 * PI * note.frequency / SAMPLE_RATE
        val omega2 = 2.0 * PI * note.frequency * 2 / SAMPLE_RATE

        for (i in 0 until length) {
            val index = start + i
            if (index >= samples.size) break
            val t = i.toDouble() / length
            val envelope = when {
                i < attackSamples -> i.toDouble() / attackSamples
                i >= length - releaseSamples -> (length - i).toDouble() / releaseSamples
                else -> 1.0
            }
            val fundamental = sin(omega * i)
            val harmonic = 0.25 * sin(omega2 * i)
            val value = (fundamental + harmonic) * envelope * 0.55
            samples[index] = (value * Short.MAX_VALUE).toInt().toShort()
        }
        return start + length
    }
}
