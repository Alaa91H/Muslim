package org.example.islamicapp.feature.prayertimes.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AdhanSynthesizerTest {

    @Test
    fun duration_isReasonableCallToPrayerLength() {
        // A full adhan is typically 1.5–4 minutes; our melody is a compact
        // rendering, so a 15–120s window is the correct sanity range.
        assertThat(AdhanSynthesizer.durationMs).isAtLeast(15_000)
        assertThat(AdhanSynthesizer.durationMs).isAtMost(120_000)
    }

    @Test
    fun generate_producesExpectedSampleCount() {
        val samples = AdhanSynthesizer.generate()
        val expected = (AdhanSynthesizer.durationMs / 1000.0 * AdhanSynthesizer.SAMPLE_RATE).toInt()
        assertThat(samples.size).isAtLeast(expected - AdhanSynthesizer.SAMPLE_RATE)
        assertThat(samples.size).isAtMost(expected + AdhanSynthesizer.SAMPLE_RATE)
    }

    @Test
    fun generate_hasAudibleNonSilentContent() {
        val samples = AdhanSynthesizer.generate()
        // The melody must actually contain sound — at least 20% of samples
        // above a low amplitude threshold (notes + envelopes).
        val loud = samples.count { kotlin.math.abs(it.toInt()) > 4_000 }
        assertThat(loud.toDouble() / samples.size).isAtLeast(0.2)
    }

    @Test
    fun generate_isClippingFree() {
        val samples = AdhanSynthesizer.generate()
        var max = 0
        for (sample in samples) {
            val value = kotlin.math.abs(sample.toInt())
            if (value > max) max = value
        }
        assertThat(max).isLessThan(Short.MAX_VALUE.toInt())
    }
}
