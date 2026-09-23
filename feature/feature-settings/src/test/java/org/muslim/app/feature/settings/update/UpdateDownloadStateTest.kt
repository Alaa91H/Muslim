package org.muslim.app.feature.settings.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateDownloadStateTest {

    @Test
    fun `progress is calculated from downloaded and total bytes`() {
        assertEquals(25, downloadProgress(25L, 100L))
        assertEquals(50, downloadProgress(512L, 1024L))
    }

    @Test
    fun `progress is clamped to one hundred percent`() {
        assertEquals(100, downloadProgress(150L, 100L))
    }

    @Test
    fun `unknown total produces indeterminate progress`() {
        assertNull(downloadProgress(10L, 0L))
        assertNull(downloadProgress(10L, -1L))
    }

    @Test
    fun `negative downloaded bytes produce indeterminate progress`() {
        assertNull(downloadProgress(-1L, 100L))
    }
}
