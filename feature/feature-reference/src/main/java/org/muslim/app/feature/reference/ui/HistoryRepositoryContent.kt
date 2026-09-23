package org.muslim.app.feature.reference.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.muslim.app.feature.reference.data.IslamicHistoryContentRepository
import org.muslim.app.feature.reference.domain.CivilizationTopic
import org.muslim.app.feature.reference.domain.HistoricalEvent
import org.muslim.app.feature.reference.domain.HistoricalPlaceProfile
import org.muslim.app.feature.reference.domain.HistoricalState
import org.muslim.app.feature.reference.domain.HistoryArticle
import org.muslim.app.feature.reference.domain.HistoryPersonProfile

@Composable
internal fun rememberHistoryArticle(eraId: String?): State<HistoryArticle?> =
    rememberRepositoryContent(eraId) { repository, id ->
        repository.articleForEra(id)
    }

@Composable
internal fun rememberHistoricalState(stateId: String?): State<HistoricalState?> =
    rememberRepositoryContent(stateId) { repository, id ->
        repository.stateById(id)
    }

@Composable
internal fun rememberHistoricalEvent(eventId: String?): State<HistoricalEvent?> =
    rememberRepositoryContent(eventId) { repository, id ->
        repository.eventById(id)
    }

@Composable
internal fun rememberCivilizationTopic(topicId: String?): State<CivilizationTopic?> =
    rememberRepositoryContent(topicId) { repository, id ->
        repository.topicById(id)
    }

@Composable
internal fun rememberHistoryPersonProfile(personId: String?): State<HistoryPersonProfile?> =
    rememberRepositoryContent(personId) { repository, id ->
        repository.personProfile(id)
    }

@Composable
internal fun rememberHistoricalPlaceProfile(placeId: String?): State<HistoricalPlaceProfile?> =
    rememberRepositoryContent(placeId) { repository, id ->
        repository.placeProfile(id)
    }

@Composable
private fun <T> rememberRepositoryContent(
    id: String?,
    loader: suspend (IslamicHistoryContentRepository, String) -> T?,
): State<T?> {
    val context = LocalContext.current
    val repository = remember(context) {
        IslamicHistoryContentRepository.get(context.applicationContext)
    }
    return produceState<T?>(
        initialValue = null,
        key1 = id,
        key2 = repository,
    ) {
        value = id?.let { loader(repository, it) }
    }
}

@Composable
internal fun HistoryContentLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
