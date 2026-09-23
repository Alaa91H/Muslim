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
) {
    fun validated(): HistorySearchAsset {
        require(schemaVersion == SUPPORTED_SCHEMA_VERSION) {
            "Unsupported history search asset schema: $schemaVersion"
        }
        require(contentVersion > 0) {
            "History search asset contentVersion must be positive"
        }
        require(documents.isNotEmpty()) {
            "History search asset must contain documents"
        }
        val keys = documents.map { "${it.entityType}:${it.entityId}" }
        require(keys.size == keys.toSet().size) {
            "History search asset contains duplicate entity keys"
        }
        documents.forEach { document ->
            require(document.typeOrNull() != null) {
                "Unknown history search entity type: ${document.entityType}"
            }
            require(document.entityId.isNotBlank()) {
                "History search asset contains a blank entity id"
            }
            require(document.titleArabic.isNotBlank() && document.titleEnglish.isNotBlank()) {
                "History search asset has an incomplete bilingual title for ${document.entityId}"
            }
        }
        return this
    }

    companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1
    }
}

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
        return JSON.decodeFromString(HistorySearchAsset.serializer(), payload).validated()
    }

    companion object {
        private const val ASSET_PATH = "history/search_index.json"
        private val JSON = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
