package org.muslim.app.feature.reference.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.muslim.app.feature.reference.domain.HistorySearchResult
import org.muslim.app.feature.reference.domain.HistorySearchType
import org.muslim.app.feature.reference.domain.HistoryText
import org.muslim.app.feature.reference.domain.IslamicHistorySearch

internal class IslamicHistorySearchRepository private constructor(
    private val context: Context,
) {
    private val database by lazy { IslamicHistorySearchDatabase.get(context) }
    private val dao by lazy { database.searchDao() }
    private val assetLoader by lazy { HistorySearchAssetLoader(context.applicationContext) }
    private val searchAsset by lazy { assetLoader.load() }
    private val seedMutex = Mutex()

    suspend fun search(
        query: String,
        type: HistorySearchType? = null,
        limit: Int = DEFAULT_RESULT_LIMIT,
    ): List<HistorySearchResult> {
        if (query.isBlank()) return emptyList()

        return runCatching {
            ensureSeeded()
            val normalizedQuery = IslamicHistorySearch.normalize(query)
            val matchQuery = buildMatchQuery(normalizedQuery)
            if (matchQuery.isBlank()) return@runCatching emptyList()

            val rows = if (type == null) {
                dao.search(matchQuery = matchQuery, limit = limit)
            } else {
                dao.searchByType(
                    matchQuery = matchQuery,
                    entityType = type.name,
                    limit = limit,
                )
            }
            rows.mapNotNull { row -> row.toSearchResult(normalizedQuery) }
                .sortedWith(
                    compareByDescending<HistorySearchResult> { it.score }
                        .thenBy { IslamicHistorySearch.normalize(it.title.english) },
                )
        }.getOrElse {
            IslamicHistorySearch.search(query = query, type = type)
        }
    }

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            val asset = searchAsset
            val metadata = dao.metadata()
            if (
                metadata?.contentVersion == asset.contentVersion &&
                metadata.documentCount == asset.documents.size
            ) {
                return@withLock
            }

            val rows = asset.documents.map { it.toEntity() }
            database.withTransaction {
                dao.clearIndex()
                dao.insertAll(rows)
                dao.upsertMetadata(
                    HistorySearchMetaEntity(
                        contentVersion = asset.contentVersion,
                        documentCount = rows.size,
                    ),
                )
            }
        }
    }

    private fun HistorySearchFtsEntity.toSearchResult(
        normalizedQuery: String,
    ): HistorySearchResult? {
        val type = runCatching { HistorySearchType.valueOf(entityType) }.getOrNull() ?: return null
        val score = scoreSearchRow(
            query = normalizedQuery,
            titleArabic = titleArabic,
            titleEnglish = titleEnglish,
            normalizedText = normalizedText,
        )
        return HistorySearchResult(
            type = type,
            id = entityId,
            title = HistoryText(titleArabic, titleEnglish),
            summary = HistoryText(summaryArabic, summaryEnglish),
            score = score,
        )
    }

    companion object {
        @Volatile
        private var instance: IslamicHistorySearchRepository? = null

        fun get(context: Context): IslamicHistorySearchRepository =
            instance ?: synchronized(this) {
                instance ?: IslamicHistorySearchRepository(
                    context.applicationContext,
                ).also { instance = it }
            }

        internal fun buildMatchQuery(normalizedQuery: String): String =
            normalizedQuery
                .split(NON_SEARCH_CHARACTER)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" AND ") { token -> "$token*" }

        private val NON_SEARCH_CHARACTER = Regex("[^\\p{L}\\p{N}]+")
        private const val DEFAULT_RESULT_LIMIT = 120
    }
}

private fun scoreSearchRow(
    query: String,
    titleArabic: String,
    titleEnglish: String,
    normalizedText: String,
): Int {
    val titleScore = maxOf(
        scoreField(IslamicHistorySearch.normalize(titleArabic), query),
        scoreField(IslamicHistorySearch.normalize(titleEnglish), query),
    )
    val bodyScore = scoreField(normalizedText, query).coerceAtMost(BODY_SCORE_CAP)
    return maxOf(titleScore, bodyScore)
}

private fun scoreField(
    text: String,
    query: String,
): Int = when {
    text == query -> 100
    text.startsWith(query) -> 85
    text.contains(query) -> 65
    query.split(' ').filter { it.isNotBlank() }.all { token -> text.contains(token) } -> 40
    else -> 1
}

private const val BODY_SCORE_CAP = 70
