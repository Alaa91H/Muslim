package org.muslim.app.feature.qibla.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HeadingSmootherTest {

    @Test
    fun `first reading is passed through unchanged`() {
        val smoother = HeadingSmoother()
        assertThat(smoother.update(123f)).isEqualTo(123f)
    }

    @Test
    fun `smooths consecutive readings toward the target`() {
        val smoother = HeadingSmoother(alpha = 0.5)
        smoother.update(0f)
        val next = smoother.update(10f)
        assertThat(next).isEqualTo(5f) // 0 + 0.5 * (10 - 0)
    }

    @Test
    fun `handles wrap around 360 without a long jump`() {
        val smoother = HeadingSmoother(alpha = 0.5)
        smoother.update(359f)
        // Shortest arc from 359 to 1 is +2°, not -358°.
        val next = smoother.update(1f)
        assertThat(next).isEqualTo(0f)
    }

    @Test
    fun `output stays within 0 to 360 range`() {
        val smoother = HeadingSmoother(alpha = 0.9)
        var value = 0f
        repeat(200) { i ->
            val raw = (i * 37 % 360).toFloat()
            value = smoother.update(raw)
            assertThat(value).isAtLeast(0f)
            assertThat(value).isLessThan(360f)
        }
    }

    @Test
    fun `reset clears the filter state`() {
        val smoother = HeadingSmoother(alpha = 0.5)
        smoother.update(0f)
        smoother.update(10f)
        smoother.reset()
        assertThat(smoother.update(350f)).isEqualTo(350f) // no smoothing
    }

    @Test
    fun `low alpha is calmer than high alpha`() {
        val calm = HeadingSmoother(alpha = 0.1)
        val fast = HeadingSmoother(alpha = 0.9)
        calm.update(0f)
        fast.update(0f)
        val calmNext = calm.update(100f)
        val fastNext = fast.update(100f)
        assertThat(calmNext).isLessThan(fastNext)
    }
}