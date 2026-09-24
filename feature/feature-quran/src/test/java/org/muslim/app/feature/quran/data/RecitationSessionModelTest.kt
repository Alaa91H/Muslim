package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class RecitationSessionModelTest {

    private fun session(
        globals: List<Int> = listOf(10, 11, 12),
        current: Int = 11,
        positionMs: Long = 4_200L,
    ) = PersistedRecitationSession(
        intent = RecitationSessionIntent(
            reciterId = "reciter",
            surahNumber = 2,
            globalNumbers = globals,
            repeatCount = 2,
            continuous = false,
            advanceToNext = true,
            toEndOfQuran = true,
        ),
        currentGlobalNumber = current,
        positionMs = positionMs,
        remainingRepeats = 2,
        wasPlaying = true,
        savedAtEpochMs = 123L,
    )

    @Test
    fun `valid persisted session is restorable`() {
        assertThat(session().asRestorableOrNull()).isNotNull()
    }

    @Test
    fun `session rejects current ayah outside its queue`() {
        assertThat(session(current = 99).asRestorableOrNull()).isNull()
    }

    @Test
    fun `session rejects invalid surah empty queue and negative position`() {
        assertThat(
            session().copy(intent = session().intent.copy(surahNumber = 0)).asRestorableOrNull(),
        ).isNull()
        assertThat(session(globals = emptyList(), current = 1).asRestorableOrNull()).isNull()
        assertThat(session(positionMs = -1L).asRestorableOrNull()).isNull()
        assertThat(session().copy(remainingRepeats = 0).asRestorableOrNull()).isNull()
    }

    @Test
    fun `remaining queue begins at the persisted current ayah`() {
        assertThat(session().remainingGlobalNumbers())
            .containsExactly(11, 12)
            .inOrder()
    }

    @Test
    fun `persisted session round trips through json`() {
        val json = Json { encodeDefaults = true }
        val original = session()

        val restored = json.decodeFromString<PersistedRecitationSession>(
            json.encodeToString(PersistedRecitationSession.serializer(), original),
        )

        assertThat(restored).isEqualTo(original)
    }
}
