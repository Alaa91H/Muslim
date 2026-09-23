package org.muslim.app.feature.reference.domain

enum class HistorySearchType {
    Era,
    State,
    Event,
    Person,
    Place,
    CivilizationTopic,
}

data class HistorySearchResult(
    val type: HistorySearchType,
    val id: String,
    val title: HistoryText,
    val summary: HistoryText,
    val score: Int,
)

/**
 * Offline unified search across the curated history catalogue.
 *
 * Search intentionally works without network access and normalizes common Arabic orthographic
 * variants and diacritics so the same catalogue can later back Room/FTS without changing UI.
 */
object IslamicHistorySearch {
    fun search(
        query: String,
        type: HistorySearchType? = null,
    ): List<HistorySearchResult> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()

        return buildList {
            if (type == null || type == HistorySearchType.Era) addAll(searchEras(normalizedQuery))
            if (type == null || type == HistorySearchType.State) addAll(searchStates(normalizedQuery))
            if (type == null || type == HistorySearchType.Event) addAll(searchEvents(normalizedQuery))
            if (type == null || type == HistorySearchType.Person) addAll(searchPeople(normalizedQuery))
            if (type == null || type == HistorySearchType.Place) addAll(searchPlaces(normalizedQuery))
            if (type == null || type == HistorySearchType.CivilizationTopic) {
                addAll(searchCivilization(normalizedQuery))
            }
        }.sortedWith(
            compareByDescending<HistorySearchResult> { it.score }
                .thenBy { normalize(it.title.english) },
        )
    }

    private fun searchEras(query: String): List<HistorySearchResult> =
        IslamicHistoryContent.timeline.mapNotNull { era ->
            result(
                type = HistorySearchType.Era,
                id = era.id,
                title = era.title,
                summary = era.summary,
                query = query,
                searchable = listOf(era.title, era.summary) + era.highlights,
            )
        }

    private fun searchStates(query: String): List<HistorySearchResult> =
        IslamicHistoryStates.states.mapNotNull { state ->
            result(
                type = HistorySearchType.State,
                id = state.id,
                title = state.title,
                summary = state.summary,
                query = query,
                searchable = listOf(state.title, state.summary),
            )
        }

    private fun searchEvents(query: String): List<HistorySearchResult> =
        IslamicHistoricalEvents.events.mapNotNull { event ->
            result(
                type = HistorySearchType.Event,
                id = event.id,
                title = event.title,
                summary = event.summary,
                query = query,
                searchable = listOf(
                    event.title,
                    event.summary,
                    event.context,
                    event.significance,
                ),
            )
        }

    private fun searchPeople(query: String): List<HistorySearchResult> =
        IslamicHistoryContent.personalities.mapNotNull { person ->
            val profile = IslamicHistoryProfiles.personById(person.id)
            result(
                type = HistorySearchType.Person,
                id = person.id,
                title = person.name,
                summary = profile?.overview ?: person.summary,
                query = query,
                searchable = buildList {
                    add(person.name)
                    add(person.field)
                    add(person.summary)
                    add(person.contribution)
                    profile?.let {
                        add(it.overview)
                        it.sections.forEach { section ->
                            add(section.title)
                            addAll(section.paragraphs)
                        }
                    }
                },
            )
        }

    private fun searchPlaces(query: String): List<HistorySearchResult> =
        IslamicHistoryContent.atlasLayers
            .flatMap { it.places }
            .distinctBy { it.id }
            .mapNotNull { place ->
                val profile = IslamicHistoryProfiles.placeById(place.id)
                result(
                    type = HistorySearchType.Place,
                    id = place.id,
                    title = place.title,
                    summary = profile?.overview ?: place.note,
                    query = query,
                    searchable = buildList {
                        add(place.title)
                        add(place.note)
                        profile?.let {
                            add(it.overview)
                            it.sections.forEach { section ->
                                add(section.title)
                                addAll(section.paragraphs)
                            }
                        }
                    },
                )
            }

    private fun searchCivilization(query: String): List<HistorySearchResult> =
        IslamicCivilizationContent.topics.mapNotNull { topic ->
            result(
                type = HistorySearchType.CivilizationTopic,
                id = topic.id,
                title = topic.title,
                summary = topic.summary,
                query = query,
                searchable = buildList {
                    add(topic.title)
                    add(topic.summary)
                    topic.sections.forEach { section ->
                        add(section.title)
                        addAll(section.paragraphs)
                    }
                },
            )
        }

    private fun result(
        type: HistorySearchType,
        id: String,
        title: HistoryText,
        summary: HistoryText,
        query: String,
        searchable: List<HistoryText>,
    ): HistorySearchResult? {
        val titleScore = maxOf(
            fieldScore(normalize(title.arabic), query),
            fieldScore(normalize(title.english), query),
        )
        val bodyScore = searchable.maxOfOrNull { text ->
            maxOf(
                fieldScore(normalize(text.arabic), query),
                fieldScore(normalize(text.english), query),
            )
        } ?: 0
        val score = maxOf(titleScore, bodyScore.coerceAtMost(BODY_SCORE_CAP))
        return if (score == 0) null else HistorySearchResult(type, id, title, summary, score)
    }

    private fun fieldScore(text: String, query: String): Int = when {
        text == query -> 100
        text.startsWith(query) -> 85
        text.contains(query) -> 65
        query.split(' ').filter { it.isNotBlank() }.all { term -> text.contains(term) } -> 40
        else -> 0
    }

    internal fun normalize(value: String): String =
        value.lowercase()
            .replace(ARABIC_DIACRITICS, "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace('ة', 'ه')
            .replace(WHITESPACE, " ")
            .trim()

    private val ARABIC_DIACRITICS = Regex("[\\u0640\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val WHITESPACE = Regex("\\s+")
    private const val BODY_SCORE_CAP = 70
}
