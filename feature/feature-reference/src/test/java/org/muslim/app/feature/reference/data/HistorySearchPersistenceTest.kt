package org.muslim.app.feature.reference.data

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test
import org.muslim.app.feature.reference.domain.HistorySearchType

class HistorySearchPersistenceTest {
    @Test
    fun `match query uses prefix tokens and strips punctuation`() {
        val query = IslamicHistorySearchRepository.buildMatchQuery("بغداد، العلم 1258")

        assertThat(query).isEqualTo("بغداد* AND العلم* AND 1258*")
    }

    @Test
    fun `asset document maps to normalized FTS entity`() {
        val document = HistorySearchAssetDocument(
            entityType = HistorySearchType.Place.name,
            entityId = "test_place",
            titleArabic = "القَاهِرَة",
            titleEnglish = "Cairo",
            summaryArabic = "مدينة",
            summaryEnglish = "City",
            body = "الأَزهر والعلم",
        )

        val entity = document.toEntity()

        assertThat(entity.entityId).isEqualTo("test_place")
        assertThat(entity.entityType).isEqualTo("Place")
        assertThat(entity.normalizedText).contains("القاهره")
        assertThat(entity.normalizedText).contains("الازهر")
    }

    @Test
    fun `versioned search asset schema decodes deterministically`() {
        val payload = """
            {
              "schemaVersion": 1,
              "contentVersion": 7,
              "documents": [
                {
                  "entityType": "Era",
                  "entityId": "sample",
                  "titleArabic": "حقبة",
                  "titleEnglish": "Era",
                  "summaryArabic": "ملخص",
                  "summaryEnglish": "Summary",
                  "body": "نص"
                }
              ]
            }
        """.trimIndent()

        val asset = Json.decodeFromString(HistorySearchAsset.serializer(), payload)

        assertThat(asset.schemaVersion).isEqualTo(1)
        assertThat(asset.contentVersion).isEqualTo(7)
        assertThat(asset.documents).hasSize(1)
        assertThat(asset.documents.single().typeOrNull()).isEqualTo(HistorySearchType.Era)
    }
}
