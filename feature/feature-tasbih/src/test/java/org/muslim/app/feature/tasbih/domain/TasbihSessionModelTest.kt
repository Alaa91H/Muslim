package org.muslim.app.feature.tasbih.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TasbihSessionModelTest {

    @Test
    fun `session mode storage ids are stable and unique`() {
        val ids = TasbihSessionMode.entries.map { it.storageId }

        assertThat(ids).containsExactly("free", "target", "rounds")
        assertThat(ids.distinct()).hasSize(TasbihSessionMode.entries.size)
    }

    @Test
    fun `unknown stored mode falls back to free mode`() {
        assertThat(TasbihSessionMode.fromStorageId("future-mode"))
            .isEqualTo(TasbihSessionMode.Free)
    }

    @Test
    fun `history duration uses end time when session is closed`() {
        val item = TasbihSessionHistoryItem(
            id = 1L,
            phraseId = TasbihPhrase.SubhanAllah.storageId,
            mode = TasbihSessionMode.Free,
            target = 33,
            roundsGoal = 1,
            count = 12,
            startedAtEpochMillis = 1_000L,
            lastUpdatedAtEpochMillis = 4_000L,
            endedAtEpochMillis = 5_000L,
            endReason = TasbihSessionEndReason.Manual,
        )

        assertThat(item.durationMillis).isEqualTo(4_000L)
        assertThat(item.isActive).isFalse()
    }

    @Test
    fun `active history duration uses last update and never becomes negative`() {
        val item = TasbihSessionHistoryItem(
            id = 2L,
            phraseId = TasbihPhrase.Alhamdulillah.storageId,
            mode = TasbihSessionMode.Free,
            target = 33,
            roundsGoal = 1,
            count = 2,
            startedAtEpochMillis = 5_000L,
            lastUpdatedAtEpochMillis = 4_000L,
            endedAtEpochMillis = null,
            endReason = null,
        )

        assertThat(item.durationMillis).isEqualTo(0L)
        assertThat(item.isActive).isTrue()
    }
}
