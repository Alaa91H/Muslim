package org.muslim.app.feature.reference.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.muslim.app.feature.reference.domain.CivilizationTopic
import org.muslim.app.feature.reference.domain.HistoricalEvent
import org.muslim.app.feature.reference.domain.HistoricalPlaceProfile
import org.muslim.app.feature.reference.domain.HistoricalState
import org.muslim.app.feature.reference.domain.HistoryArticle
import org.muslim.app.feature.reference.domain.HistoryPersonProfile
import org.muslim.app.feature.reference.domain.IslamicCivilizationContent
import org.muslim.app.feature.reference.domain.IslamicHistoricalEvents
import org.muslim.app.feature.reference.domain.IslamicHistoryArticles
import org.muslim.app.feature.reference.domain.IslamicHistoryProfiles
import org.muslim.app.feature.reference.domain.IslamicHistoryStates

internal class IslamicHistoryContentRepository private constructor(
    private val context: Context,
) {
    private val database by lazy { IslamicHistorySearchDatabase.get(context) }
    private val dao by lazy { database.contentDao() }
    private val loader by lazy { HistoryContentAssetLoader(context.applicationContext) }
    private val asset by lazy { loader.load() }
    private val seedMutex = Mutex()

    suspend fun articleForEra(eraId: String): HistoryArticle? =
        load(
            type = StoredHistoryContentType.Article,
            id = eraId,
            serializer = HistoryArticleDto.serializer(),
            fallback = {
                IslamicHistoryArticles.articleForEra(eraId)?.let { HistoryArticleDto.fromDomain(it) }
            },
        )?.toDomain()

    suspend fun stateById(id: String): HistoricalState? =
        load(
            type = StoredHistoryContentType.State,
            id = id,
            serializer = HistoricalStateDto.serializer(),
            fallback = { IslamicHistoryStates.byId(id)?.let { HistoricalStateDto.fromDomain(it) } },
        )?.toDomain()

    suspend fun eventById(id: String): HistoricalEvent? =
        load(
            type = StoredHistoryContentType.Event,
            id = id,
            serializer = HistoricalEventDto.serializer(),
            fallback = { IslamicHistoricalEvents.byId(id)?.let { HistoricalEventDto.fromDomain(it) } },
        )?.toDomain()

    suspend fun topicById(id: String): CivilizationTopic? =
        load(
            type = StoredHistoryContentType.CivilizationTopic,
            id = id,
            serializer = CivilizationTopicDto.serializer(),
            fallback = { IslamicCivilizationContent.byId(id)?.let { CivilizationTopicDto.fromDomain(it) } },
        )?.toDomain()

    suspend fun personProfile(id: String): HistoryPersonProfile? =
        load(
            type = StoredHistoryContentType.PersonProfile,
            id = id,
            serializer = HistoryPersonProfileDto.serializer(),
            fallback = { IslamicHistoryProfiles.personById(id)?.let { HistoryPersonProfileDto.fromDomain(it) } },
        )?.toDomain()

    suspend fun placeProfile(id: String): HistoricalPlaceProfile? =
        load(
            type = StoredHistoryContentType.PlaceProfile,
            id = id,
            serializer = HistoricalPlaceProfileDto.serializer(),
            fallback = { IslamicHistoryProfiles.placeById(id)?.let { HistoricalPlaceProfileDto.fromDomain(it) } },
        )?.toDomain()

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            val loadedAsset = asset
            val metadata = dao.metadata()
            if (
                metadata?.contentVersion == loadedAsset.contentVersion &&
                metadata.recordCount == loadedAsset.recordCount
            ) {
                return@withLock
            }

            val rows = loadedAsset.toEntities()
            database.withTransaction {
                dao.clearContent()
                dao.insertContent(rows)
                dao.upsertMetadata(
                    HistoryContentMetaEntity(
                        contentVersion = loadedAsset.contentVersion,
                        recordCount = rows.size,
                    ),
                )
            }
        }
    }

    private suspend fun <T> load(
        type: StoredHistoryContentType,
        id: String,
        serializer: DeserializationStrategy<T>,
        fallback: () -> T?,
    ): T? =
        try {
            ensureSeeded()
            dao.content(type.name, id)?.payloadJson?.let { payload ->
                HistoryContentJson.decodeFromString(serializer, payload)
            } ?: fallback()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            fallback()
        }

    companion object {
        @Volatile
        private var instance: IslamicHistoryContentRepository? = null

        fun get(context: Context): IslamicHistoryContentRepository =
            instance ?: synchronized(this) {
                instance ?: IslamicHistoryContentRepository(
                    context.applicationContext,
                ).also { instance = it }
            }
    }
}

private enum class StoredHistoryContentType {
    Article,
    State,
    Event,
    CivilizationTopic,
    PersonProfile,
    PlaceProfile,
}

private fun HistoryContentAsset.toEntities(): List<HistoryContentEntity> =
    buildList {
        articles.forEach { article ->
            add(
                article.toEntity(
                    type = StoredHistoryContentType.Article,
                    id = article.eraId,
                    serializer = HistoryArticleDto.serializer(),
                ),
            )
        }
        states.forEach { state ->
            add(
                state.toEntity(
                    type = StoredHistoryContentType.State,
                    id = state.id,
                    serializer = HistoricalStateDto.serializer(),
                ),
            )
        }
        events.forEach { event ->
            add(
                event.toEntity(
                    type = StoredHistoryContentType.Event,
                    id = event.id,
                    serializer = HistoricalEventDto.serializer(),
                ),
            )
        }
        civilizationTopics.forEach { topic ->
            add(
                topic.toEntity(
                    type = StoredHistoryContentType.CivilizationTopic,
                    id = topic.id,
                    serializer = CivilizationTopicDto.serializer(),
                ),
            )
        }
        peopleProfiles.forEach { profile ->
            add(
                profile.toEntity(
                    type = StoredHistoryContentType.PersonProfile,
                    id = profile.personId,
                    serializer = HistoryPersonProfileDto.serializer(),
                ),
            )
        }
        placeProfiles.forEach { profile ->
            add(
                profile.toEntity(
                    type = StoredHistoryContentType.PlaceProfile,
                    id = profile.placeId,
                    serializer = HistoricalPlaceProfileDto.serializer(),
                ),
            )
        }
    }

private fun <T> T.toEntity(
    type: StoredHistoryContentType,
    id: String,
    serializer: SerializationStrategy<T>,
): HistoryContentEntity =
    HistoryContentEntity(
        entityType = type.name,
        entityId = id,
        payloadJson = HistoryContentJson.encodeToString(serializer, this),
    )
