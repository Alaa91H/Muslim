package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class QuranBackupTest {
    @Test
    fun `backup round trips bookmarks notes and last read`() {
        val original = QuranBackup(
            exportedAt = 123L,
            bookmarks = listOf(QuranBackupBookmark(10, 1, 10, "text", 100L)),
            notes = listOf(QuranBackupNote(10, "note", 101L)),
            lastReadGlobalNumber = 10,
        )

        val decoded = QuranBackupCodec.decode(QuranBackupCodec.encode(original))
        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `decoder accepts unknown fields for future backups`() {
        val decoded = QuranBackupCodec.decode("""{"version":1,"exportedAt":1,"futureField":true}""")
        assertThat(decoded.version).isEqualTo(1)
        assertThat(decoded.bookmarks).isEmpty()
    }
}
