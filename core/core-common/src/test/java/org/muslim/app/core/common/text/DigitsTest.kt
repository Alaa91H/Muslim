package org.muslim.app.core.common.text

import org.junit.Assert.assertEquals
import org.junit.Test

class DigitsTest {

    @Test
    fun `western digits pass through unchanged`() {
        assertEquals("12345.67", Digits.toWesternDigits("12345.67"))
        assertEquals("0", Digits.toWesternDigits("0"))
    }

    @Test
    fun `arabic-indic digits convert to western`() {
        assertEquals("1234567890", Digits.toWesternDigits("\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669\u0660"))
    }

    @Test
    fun `persian digits convert to western`() {
        assertEquals("1234567890", Digits.toWesternDigits("\u06F1\u06F2\u06F3\u06F4\u06F5\u06F6\u06F7\u06F8\u06F9\u06F0"))
    }

    @Test
    fun `arabic decimal separator becomes dot`() {
        assertEquals("12.5", Digits.toWesternDigits("12\u066B5"))
    }

    @Test
    fun `arabic thousands separator becomes comma`() {
        assertEquals("1,000", Digits.toWesternDigits("1\u066C000"))
    }

    @Test
    fun `mixed input converts every non-western digit`() {
        // "١2٣" (arabic 1, western 2, arabic 3)
        assertEquals("123", Digits.toWesternDigits("\u06612\u0663"))
    }

    @Test
    fun `non-digit characters are untouched`() {
        assertEquals("abc-+()", Digits.toWesternDigits("abc-+()"))
    }
}
