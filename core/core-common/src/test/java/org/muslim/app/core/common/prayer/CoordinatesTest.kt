package org.muslim.app.core.common.prayer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CoordinatesTest {

    @Test
    fun `default elevation is zero`() {
        val c = Coordinates(21.4225, 39.8262)
        assertThat(c.elevation).isEqualTo(0.0)
    }

    @Test
    fun `accepts valid elevation`() {
        val c = Coordinates(21.4225, 39.8262, 277.0)
        assertThat(c.elevation).isEqualTo(277.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects negative elevation`() {
        Coordinates(21.4225, 39.8262, -5.0)
    }

    @Test
    fun `rejects invalid latitude and longitude`() {
        try {
            Coordinates(91.0, 0.0)
            throw AssertionError("latitude 91 should be rejected")
        } catch (e: IllegalArgumentException) {
            // expected
        }
        try {
            Coordinates(0.0, 181.0)
            throw AssertionError("longitude 181 should be rejected")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }
}