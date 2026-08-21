package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [RecitationFocusPolicy]: whether the recitation auto-resumes
 * after an audio-focus loss, depending on the loss type and the player state
 * at the moment the focus was taken.
 */
class RecitationFocusPolicyTest {

    @Test
    fun `transient loss while playing resumes on gain`() {
        val policy = RecitationFocusPolicy()

        policy.onTransientLoss(wasPlaying = true)
        assertThat(policy.onGain()).isTrue()
    }

    @Test
    fun `transient loss while paused does not auto-resume`() {
        // The user had already paused the recitation — a navigation/alert sound
        // taking focus must not silently restart it.
        val policy = RecitationFocusPolicy()

        policy.onTransientLoss(wasPlaying = false)
        assertThat(policy.onGain()).isFalse()
    }

    @Test
    fun `permanent loss never auto-resumes`() {
        // Phone call / another app took over: pause and stay paused.
        val policy = RecitationFocusPolicy()
        policy.onTransientLoss(wasPlaying = true)

        policy.onPermanentLoss()
        assertThat(policy.onGain()).isFalse()
    }

    @Test
    fun `a second gain without a fresh loss does not resume again`() {
        val policy = RecitationFocusPolicy()

        policy.onTransientLoss(wasPlaying = true)
        assertThat(policy.onGain()).isTrue()
        // The flag is consumed by the first gain.
        assertThat(policy.onGain()).isFalse()
    }

    @Test
    fun `gain with no loss at all does not resume`() {
        val policy = RecitationFocusPolicy()

        assertThat(policy.onGain()).isFalse()
    }

    @Test
    fun `fresh transient loss after a consumed gain re-arms resume`() {
        val policy = RecitationFocusPolicy()

        policy.onTransientLoss(wasPlaying = true)
        assertThat(policy.onGain()).isTrue()
        assertThat(policy.onGain()).isFalse()

        // Another sound interrupts the (now resumed) recitation.
        policy.onTransientLoss(wasPlaying = true)
        assertThat(policy.onGain()).isTrue()
    }
}
