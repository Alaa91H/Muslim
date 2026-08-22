package org.muslim.app.feature.reference.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IslamicHistoryContentTest {
    @Test
    fun `timeline is chronological and bilingual`() {
        val eras = IslamicHistoryContent.timeline

        assertThat(eras.map { it.id }.toSet()).hasSize(eras.size)
        assertThat(eras.map { it.startCe }).isInOrder()
        eras.forEach { era ->
            assertThat(era.title.arabic).isNotEmpty()
            assertThat(era.title.english).isNotEmpty()
            assertThat(era.summary.arabic).isNotEmpty()
            assertThat(era.summary.english).isNotEmpty()
            assertThat(era.highlights).isNotEmpty()
        }
    }

    @Test
    fun `atlas layers contain usable schematic shapes routes and places`() {
        val layers = IslamicHistoryContent.atlasLayers

        assertThat(layers.map { it.id }.toSet()).hasSize(layers.size)
        layers.forEach { layer ->
            assertThat(layer.schematicArea.size).isAtLeast(3)
            assertThat(layer.places).isNotEmpty()
            layer.routes.forEach { route ->
                assertThat(route.coordinates.size).isAtLeast(2)
                assertThat(route.note.english).contains("simplified")
            }
        }
    }

    @Test
    fun `personality summaries retain context and avoid exaggerated claims`() {
        val people = IslamicHistoryContent.personalities
        val text = people.joinToString(" ") { "${it.summary.english} ${it.contribution.english}" }

        assertThat(people.map { it.id }.toSet()).hasSize(people.size)
        assertThat(text).doesNotContain("invented")
        assertThat(text).contains("history of science")
        assertThat(IslamicHistoryContent.eraById("abbasid")?.startCe).isEqualTo(750)
        assertThat(IslamicHistoryContent.mapLayerById("abbasid_networks")?.routes).isNotEmpty()
    }
}
