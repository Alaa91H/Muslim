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

    @Test
    fun `query covers common mosque tags used across Europe and America`() {
        val query = MosqueFinderRepository().buildQuery(52.52, 13.405, 5_000)

        assertThat(query).contains("""["amenity"="mosque"]""")
        assertThat(query).contains("""["building"="mosque"]""")
        assertThat(query).contains("""["building"="masjid"]""")
        assertThat(query).contains("""["religion"="muslim"]""")
        assertThat(query).contains("""["religion"="islam"]""")
        assertThat(query).contains("out center qt;")
    }

    @Test
    fun `nearbyNearest stops expanding once the time budget is spent`() = runTest {
        val repository = MosqueFinderRepository()
        val searched = mutableListOf<Int>()
        // Budget already spent (-1 ms puts the deadline in the past): the
        // loop must not attempt any radius at all, so a total Overpass outage
        // cannot chain ten slow timeouts.
        val result = repository.nearbyNearestWith(
            fetch = { radiusKm -> searched += radiusKm; emptyList() },
            timeBudgetMillis = -1L,
        )

        assertThat(result).isEmpty()
        assertThat(searched).isEmpty()
    }
}
