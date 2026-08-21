package org.muslim.app.feature.qibla.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MosqueFinderRepositoryTest {

    @Test
    fun `nearbyNearest expands radius and stops at first nonempty result ordered by distance`() = runTest {
        val repository = MosqueFinderRepository()
        val searched = mutableListOf<Int>()
        val result = repository.nearbyNearestWith(
            fetch = { radiusKm ->
                searched += radiusKm
                if (radiusKm < 10) emptyList() else listOf(
                    Mosque("far", 0.0, 0.0, 900, 0.0),
                    Mosque("near", 0.0, 0.0, 120, 0.0),
                )
            },
        )

        assertThat(searched).containsExactly(1, 3, 5, 10).inOrder()
        assertThat(result.map { it.name }).containsExactly("near", "far").inOrder()
    }

    @Test
    fun `nearbyNearest returns empty after exhausting all radii`() = runTest {
        val repository = MosqueFinderRepository()
        val searched = mutableListOf<Int>()
        val result = repository.nearbyNearestWith(
            fetch = { radiusKm -> searched += radiusKm; emptyList() },
        )

        assertThat(result).isEmpty()
        assertThat(searched).containsExactly(1, 3, 5, 10, 25, 50, 100, 250, 500, 1000).inOrder()
    }
}
