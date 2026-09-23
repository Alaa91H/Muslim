package org.muslim.app.feature.reference.domain

/**
 * Structural validation for curated history content.
 *
 * The validator is deliberately dependency-free so it can run both in JVM unit tests and
 * future import/build tooling when the catalogue moves to JSON/Room.
 */
object HistoryContentValidator {
    @Suppress("LongParameterList")
    fun validate(
        eras: List<HistoryEra> = IslamicHistoryContent.timeline,
        articles: List<HistoryArticle> = IslamicHistoryArticles.articles,
        states: List<HistoricalState> = IslamicHistoryStates.states,
        events: List<HistoricalEvent> = IslamicHistoricalEvents.events,
        atlasLayers: List<HistoricalMapLayer> = IslamicHistoryContent.atlasLayers,
        personProfiles: List<HistoryPersonProfile> = IslamicHistoryProfiles.people,
        placeProfiles: List<HistoricalPlaceProfile> = IslamicHistoryProfiles.places,
        civilizationTopics: List<CivilizationTopic> = IslamicCivilizationContent.topics,
        sources: List<HistorySource> = IslamicHistorySources.all,
    ): List<String> {
        val errors = mutableListOf<String>()
        val eraIds = eras.map { it.id }.toSet()
        val sourceIds = sources.map { it.id }.toSet()
        val topicIds = civilizationTopics.map { it.id }.toSet()
        val personIds = IslamicHistoryContent.personalities.map { it.id }.toSet()
        val stateIds = states.map { it.id }.toSet()
        val placeIds = atlasLayers
            .flatMap { it.places }
            .map { it.id }
            .toSet()
        val eventIds = events.map { it.id }.toSet()
        val profileReferences = ProfileReferences(
            eraIds = eraIds,
            stateIds = stateIds,
            placeIds = placeIds,
            eventIds = eventIds,
            topicIds = topicIds,
            personIds = personIds,
            sourceIds = sourceIds,
        )

        duplicateIds("era", eras.map { it.id }, errors)
        duplicateIds("article", articles.map { it.id }, errors)
        duplicateIds("state", states.map { it.id }, errors)
        duplicateIds("event", events.map { it.id }, errors)
        duplicateIds("atlas layer", atlasLayers.map { it.id }, errors)
        duplicateIds("person profile", personProfiles.map { it.personId }, errors)
        duplicateIds("place profile", placeProfiles.map { it.placeId }, errors)
        duplicateIds("civilization topic", civilizationTopics.map { it.id }, errors)
        duplicateIds("source", sources.map { it.id }, errors)

        validateEras(eras, errors)
        validateArticles(articles, eraIds, sourceIds, errors)
        validateStates(states, eraIds, sourceIds, errors)
        validateAtlasLayers(atlasLayers, eraIds, errors)
        validateEvents(
            events,
            eraIds,
            stateIds,
            placeIds,
            personIds,
            topicIds,
            sourceIds,
            errors,
        )
        validatePersonProfiles(personProfiles, profileReferences, errors)
        validatePlaceProfiles(placeProfiles, profileReferences, errors)
        validateCivilizationTopics(
            civilizationTopics,
            topicIds,
            personIds,
            sourceIds,
            errors,
        )
        validateSources(sources, errors)
        return errors
    }

}

private fun validateEras(
    eras: List<HistoryEra>,
    errors: MutableList<String>,
    ) {
    eras.forEach { era ->
        if (era.title.arabic.isBlank() || era.title.english.isBlank()) {
            errors += "Era ${era.id} has an incomplete bilingual title"
        }
        if (era.summary.arabic.isBlank() || era.summary.english.isBlank()) {
            errors += "Era ${era.id} has an incomplete bilingual summary"
        }
        if (era.endCe != null && era.endCe < era.startCe) {
            errors += "Era ${era.id} ends before it starts"
        }
    }
    }

private fun validateArticles(
    articles: List<HistoryArticle>,
    eraIds: Set<String>,
    sourceIds: Set<String>,
    errors: MutableList<String>,
    ) {
    articles.forEach { article ->
        if (article.eraId !in eraIds) {
            errors += "Article ${article.id} references unknown era ${article.eraId}"
        }
        if (article.title.arabic.isBlank() || article.title.english.isBlank()) {
            errors += "Article ${article.id} has an incomplete bilingual title"
        }
        if (article.lead.arabic.isBlank() || article.lead.english.isBlank()) {
            errors += "Article ${article.id} has an incomplete bilingual lead"
        }
        if (article.sections.isEmpty()) {
            errors += "Article ${article.id} has no sections"
        }

        duplicateIds(
            "section in article ${article.id}",
            article.sections.map { it.id },
            errors,
        )
        validateArticleSections(article, errors)
        validateArticleReferences(article, eraIds, sourceIds, errors)
    }
    }

private fun validateArticleSections(
    article: HistoryArticle,
    errors: MutableList<String>,
    ) {
    article.sections.forEach { section ->
        if (section.title.arabic.isBlank() || section.title.english.isBlank()) {
            errors += "Section ${article.id}/${section.id} has an incomplete bilingual title"
        }
        if (section.paragraphs.isEmpty()) {
            errors += "Section ${article.id}/${section.id} has no paragraphs"
        }
        section.paragraphs.forEachIndexed { index, paragraph ->
            if (paragraph.arabic.isBlank() || paragraph.english.isBlank()) {
                errors += "Paragraph ${index + 1} in ${article.id}/${section.id} is not bilingual"
            }
        }
    }
    }

private fun validateArticleReferences(
    article: HistoryArticle,
    eraIds: Set<String>,
    sourceIds: Set<String>,
    errors: MutableList<String>,
    ) {
    article.sourceIds.forEach { sourceId ->
        if (sourceId !in sourceIds) {
            errors += "Article ${article.id} references unknown source $sourceId"
        }
    }
    article.relatedEraIds.forEach { eraId ->
        if (eraId !in eraIds) {
            errors += "Article ${article.id} references unknown related era $eraId"
        }
    }
    }

private fun validateStates(
    states: List<HistoricalState>,
    eraIds: Set<String>,
    sourceIds: Set<String>,
    errors: MutableList<String>,
    ) {
    states.forEach { state ->
        if (state.title.arabic.isBlank() || state.title.english.isBlank()) {
            errors += "State ${state.id} has an incomplete bilingual title"
        }
        if (state.summary.arabic.isBlank() || state.summary.english.isBlank()) {
            errors += "State ${state.id} has an incomplete bilingual summary"
        }
        val start = state.period.startCe
        val end = state.period.endCe
        if (start != null && end != null && end < start) {
            errors += "State ${state.id} ends before it starts"
        }
        state.eraIds.forEach { eraId ->
            if (eraId !in eraIds) {
                errors += "State ${state.id} references unknown era $eraId"
            }
        }
        state.sourceIds.forEach { sourceId ->
            if (sourceId !in sourceIds) {
                errors += "State ${state.id} references unknown source $sourceId"
            }
        }
    }
    }

private fun validateAtlasLayers(
    layers: List<HistoricalMapLayer>,
    eraIds: Set<String>,
    errors: MutableList<String>,
) {
    val placeIds = mutableSetOf<String>()
    layers.forEach { layer ->
        if (layer.eraId !in eraIds) {
            errors += "Atlas layer ${layer.id} references unknown era ${layer.eraId}"
        }
        if (layer.endCe != null && layer.endCe < layer.startCe) {
            errors += "Atlas layer ${layer.id} ends before it starts"
        }
        if (layer.title.arabic.isBlank() || layer.title.english.isBlank()) {
            errors += "Atlas layer ${layer.id} has an incomplete bilingual title"
        }
        layer.places.forEach { place ->
            if (!placeIds.add(place.id)) {
                errors += "Duplicate atlas place id: ${place.id}"
            }
        }
    }
}

@Suppress("LongParameterList")
private fun validateEvents(
    events: List<HistoricalEvent>,
    eraIds: Set<String>,
    stateIds: Set<String>,
    placeIds: Set<String>,
    personIds: Set<String>,
    topicIds: Set<String>,
    sourceIds: Set<String>,
    errors: MutableList<String>,
    ) {
    events.forEach { event ->
        validateEventText(event, errors)
        val start = event.date.startCe
        val end = event.date.endCe
        if (start != null && end != null && end < start) {
            errors += "Event ${event.id} ends before it starts"
        }
        event.eraIds.forEach { eraId ->
            if (eraId !in eraIds) errors += "Event ${event.id} references unknown era $eraId"
        }
        event.stateIds.forEach { stateId ->
            if (stateId !in stateIds) errors += "Event ${event.id} references unknown state $stateId"
        }
        event.placeIds.forEach { placeId ->
            if (placeId !in placeIds) errors += "Event ${event.id} references unknown place $placeId"
        }
        event.personIds.forEach { personId ->
            if (personId !in personIds) errors += "Event ${event.id} references unknown person $personId"
        }
        event.relatedTopicIds.forEach { topicId ->
            if (topicId !in topicIds) errors += "Event ${event.id} references unknown topic $topicId"
        }
        event.sourceIds.forEach { sourceId ->
            if (sourceId !in sourceIds) errors += "Event ${event.id} references unknown source $sourceId"
        }
    }
    }

private fun validateEventText(
    event: HistoricalEvent,
    errors: MutableList<String>,
    ) {
    val fields = listOf(
        "title" to event.title,
        "summary" to event.summary,
        "context" to event.context,
        "significance" to event.significance,
    )
    fields.forEach { (label, text) ->
        if (text.arabic.isBlank() || text.english.isBlank()) {
            errors += "Event ${event.id} has incomplete bilingual $label"
        }
    }
    if (event.eraIds.isEmpty()) errors += "Event ${event.id} has no era links"
    if (event.sourceIds.isEmpty()) errors += "Event ${event.id} has no sources"
    }

private data class ProfileReferences(
    val eraIds: Set<String>,
    val stateIds: Set<String>,
    val placeIds: Set<String>,
    val eventIds: Set<String>,
    val topicIds: Set<String>,
    val personIds: Set<String>,
    val sourceIds: Set<String>,
    )

private fun validatePersonProfiles(
    profiles: List<HistoryPersonProfile>,
    refs: ProfileReferences,
    errors: MutableList<String>,
    ) {
    profiles.forEach { profile ->
        if (profile.personId !in refs.personIds) {
            errors += "Person profile references unknown person ${profile.personId}"
        }
        validateProfileText(
            label = "Person profile ${profile.personId}",
            overview = profile.overview,
            sections = profile.sections,
            errors = errors,
        )
        validateProfileReferences(
            label = "Person profile ${profile.personId}",
            eraIds = profile.eraIds,
            stateIds = profile.stateIds,
            placeIds = profile.placeIds,
            eventIds = profile.eventIds,
            topicIds = profile.relatedTopicIds,
            personIds = emptyList(),
            sourceIds = profile.sourceIds,
            refs = refs,
            errors = errors,
        )
    }
    }

private fun validatePlaceProfiles(
    profiles: List<HistoricalPlaceProfile>,
    refs: ProfileReferences,
    errors: MutableList<String>,
    ) {
    profiles.forEach { profile ->
        if (profile.placeId !in refs.placeIds) {
            errors += "Place profile references unknown place ${profile.placeId}"
        }
        validateProfileText(
            label = "Place profile ${profile.placeId}",
            overview = profile.overview,
            sections = profile.sections,
            errors = errors,
        )
        validateProfileReferences(
            label = "Place profile ${profile.placeId}",
            eraIds = profile.eraIds,
            stateIds = profile.stateIds,
            placeIds = emptyList(),
            eventIds = profile.eventIds,
            topicIds = profile.relatedTopicIds,
            personIds = profile.relatedPersonIds,
            sourceIds = profile.sourceIds,
            refs = refs,
            errors = errors,
        )
    }
    }

private fun validateProfileText(
    label: String,
    overview: HistoryText,
    sections: List<HistoryArticleSection>,
    errors: MutableList<String>,
    ) {
    if (overview.arabic.isBlank() || overview.english.isBlank()) {
        errors += "$label has an incomplete bilingual overview"
    }
    if (sections.isEmpty()) errors += "$label has no sections"
    duplicateIds("section in $label", sections.map { it.id }, errors)
    sections.forEach { section ->
        if (section.title.arabic.isBlank() || section.title.english.isBlank()) {
            errors += "$label has an incomplete bilingual section title"
        }
        if (section.paragraphs.isEmpty()) errors += "$label has an empty section"
        section.paragraphs.forEach { paragraph ->
            if (paragraph.arabic.isBlank() || paragraph.english.isBlank()) {
                errors += "$label contains a non-bilingual paragraph"
            }
        }
    }
    }

@Suppress("LongParameterList")
private fun validateProfileReferences(
    label: String,
    eraIds: List<String>,
    stateIds: List<String>,
    placeIds: List<String>,
    eventIds: List<String>,
    topicIds: List<String>,
    personIds: List<String>,
    sourceIds: List<String>,
    refs: ProfileReferences,
    errors: MutableList<String>,
    ) {
    validateIds(label, "era", eraIds, refs.eraIds, errors)
    validateIds(label, "state", stateIds, refs.stateIds, errors)
    validateIds(label, "place", placeIds, refs.placeIds, errors)
    validateIds(label, "event", eventIds, refs.eventIds, errors)
    validateIds(label, "topic", topicIds, refs.topicIds, errors)
    validateIds(label, "person", personIds, refs.personIds, errors)
    validateIds(label, "source", sourceIds, refs.sourceIds, errors)
    if (sourceIds.isEmpty()) errors += "$label has no sources"
    }

private fun validateIds(
    label: String,
    kind: String,
    ids: List<String>,
    knownIds: Set<String>,
    errors: MutableList<String>,
    ) {
    ids.forEach { id ->
        if (id !in knownIds) errors += "$label references unknown $kind $id"
    }
    }

private fun validateCivilizationTopics(
    topics: List<CivilizationTopic>,
    topicIds: Set<String>,
    personIds: Set<String>,
    sourceIds: Set<String>,
    errors: MutableList<String>,
    ) {
    topics.forEach { topic ->
        if (topic.title.arabic.isBlank() || topic.title.english.isBlank()) {
            errors += "Civilization topic ${topic.id} has an incomplete bilingual title"
        }
        if (topic.summary.arabic.isBlank() || topic.summary.english.isBlank()) {
            errors += "Civilization topic ${topic.id} has an incomplete bilingual summary"
        }
        if (topic.sections.isEmpty()) {
            errors += "Civilization topic ${topic.id} has no sections"
        }
        duplicateIds(
            "section in civilization topic ${topic.id}",
            topic.sections.map { it.id },
            errors,
        )
        topic.sections.forEach { section ->
            if (section.title.arabic.isBlank() || section.title.english.isBlank()) {
                errors += "Section ${topic.id}/${section.id} has an incomplete bilingual title"
            }
            if (section.paragraphs.isEmpty()) {
                errors += "Section ${topic.id}/${section.id} has no paragraphs"
            }
            section.paragraphs.forEach { paragraph ->
                if (paragraph.arabic.isBlank() || paragraph.english.isBlank()) {
                    errors += "Civilization topic ${topic.id} contains a non-bilingual paragraph"
                }
            }
        }
        topic.sourceIds.forEach { sourceId ->
            if (sourceId !in sourceIds) {
                errors += "Civilization topic ${topic.id} references unknown source $sourceId"
            }
        }
        topic.personIds.forEach { personId ->
            if (personId !in personIds) {
                errors += "Civilization topic ${topic.id} references unknown person $personId"
            }
        }
        topic.relatedTopicIds.forEach { relatedId ->
            if (relatedId !in topicIds) {
                errors += "Civilization topic ${topic.id} references unknown topic $relatedId"
            }
        }
    }
    }

private fun validateSources(
    sources: List<HistorySource>,
    errors: MutableList<String>,
    ) {
    sources.forEach { source ->
        if (source.title.arabic.isBlank() || source.title.english.isBlank()) {
            errors += "Source ${source.id} has an incomplete bilingual title"
        }
    }
    }

private fun duplicateIds(
    label: String,
    ids: List<String>,
    errors: MutableList<String>,
    ) {
    ids.groupingBy { it }
        .eachCount()
        .filterValues { it > 1 }
        .keys
        .forEach { id -> errors += "Duplicate $label id: $id" }
    }
