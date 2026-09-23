package org.muslim.app.feature.reference.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.muslim.app.feature.reference.domain.HistorySearchType
import org.muslim.app.feature.reference.domain.IslamicHistorySearch

@Serializable
internal data class HistorySearchAsset(
    val schemaVersion: Int,
    val contentVersion: Int,
    val documents: List<HistorySearchAssetDocument>,
)

@Serializable
internal data class HistorySearchAssetDocument(
    val entityType: String,
    val entityId: String,
    val titleArabic: String,
    val titleEnglish: String,
    val summaryArabic: String,
    val summaryEnglish: String,
    val body: String,
) {
    fun typeOrNull(): HistorySearchType? =
        runCatching { HistorySearchType.valueOf(entityType) }.getOrNull()

    fun toEntity(): HistorySearchFtsEntity {
        val normalized = IslamicHistorySearch.normalize(
            listOf(
                titleArabic,
                titleEnglish,
                summaryArabic,
                summaryEnglish,
                body,
            ).joinToString(" "),
        )
        return HistorySearchFtsEntity(
            entityId = entityId,
            entityType = entityType,
            titleArabic = titleArabic,
            titleEnglish = titleEnglish,
            summaryArabic = summaryArabic,
            summaryEnglish = summaryEnglish,
            normalizedText = normalized,
        )
    }
}

internal class HistorySearchAssetLoader(
    private val context: Context,
) {
    fun load(): HistorySearchAsset {
        val payload = context.assets
            .open(ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }
        return JSON.decodeFromString(HistorySearchAsset.serializer(), payload)
    }

    companion object {
        private const val ASSET_PATH = "history/search_index.json"
        private val JSON = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
