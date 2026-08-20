package org.muslim.app.feature.settings.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCompareTest {

    @Test
    fun `newer major version wins`() {
        assertTrue(VersionCompare.isNewer("2.0.0", "1.9.9"))
        assertTrue(VersionCompare.isNewer("2.0.0", "1.5.0"))
    }

    @Test
    fun `newer minor version wins`() {
        assertTrue(VersionCompare.isNewer("1.6.0", "1.5.9"))
        assertTrue(VersionCompare.isNewer("1.5.1", "1.5.0"))
    }

    @Test
    fun `newer patch version wins`() {
        assertTrue(VersionCompare.isNewer("1.5.1", "1.5.0"))
    }

    @Test
    fun `leading v is ignored`() {
        assertTrue(VersionCompare.isNewer("v1.6.0", "1.5.9"))
        assertTrue(VersionCompare.isNewer("v2.0.0", "v1.9.9"))
        assertFalse(VersionCompare.isNewer("v1.5.0", "v1.5.0"))
    }

    @Test
    fun `equal versions are not newer`() {
        assertFalse(VersionCompare.isNewer("1.5.0", "1.5.0"))
    }

    @Test
    fun `older version is not newer`() {
        assertFalse(VersionCompare.isNewer("1.4.9", "1.5.0"))
        assertFalse(VersionCompare.isNewer("1.5.0", "1.5.1"))
    }

    @Test
    fun `shorter and longer version lists compare by missing segments as zero`() {
        assertTrue(VersionCompare.isNewer("1.5.1", "1.5"))
        assertFalse(VersionCompare.isNewer("1.5", "1.5.1"))
    }

    @Test
    fun `non-numeric segments fall back to zero`() {
        assertTrue(VersionCompare.isNewer("1.5.1", "1.5"))
        assertFalse(VersionCompare.isNewer("1.5.beta", "1.5.1"))
    }
}

class FormatSizeTest {

    @Test
    fun `zero bytes yields unknown marker`() {
        assertEquals("?", formatSize(0))
        assertEquals("?", formatSize(-5))
    }

    @Test
    fun `megabytes are formatted with one decimal`() {
        assertEquals("1.0 MB", formatSize((1024 * 1024).toLong()))
        assertEquals("12.3 MB", formatSize((12.3 * 1024 * 1024).toLong()))
    }
}
