package org.muslim.app.feature.reference.domain

/**
 * Structural validation for curated history content.
 *
 * The validator is deliberately dependency-free so it can run both in JVM unit tests and
 * future import/build tooling when the catalogue moves to JSON/Room.
 */
object HistoryContentValidator {
    fun validate(
        eras: List<HistoryEra> = IslamicHistoryContent.timeline,
        articles: List<HistoryArticle> = IslamicHistoryArticles.articles,
        states: List<HistoricalState> = IslamicHistoryStates.states,
        civilizationTopics: List<CivilizationTopic> = IslamicCivilizationContent.topics,
        sources: List<HistorySource> = IslamicHistorySources.all,
    ): List<String> {
        val errors = mutableListOf<String>()
        val eraIds = eras.map { it.id }.toSet()
        val sourceIds = sources.map { it.id }.toSet()
        val topicIds = civilizationTopics.map { it.id }.toSet()
        val personIds = IslamicHistoryContent.personalities.map { it.id }.toSet()

        duplicateIds("era", eras.map { it.id }, errors)
        duplicateIds("article", articles.map { it.id }, errors)
        duplicateIds("state", states.map { it.id }, errors)
        duplicateIds("civilization topic", civilizationTopics.map { it.id }, errors)
        duplicateIds("source", sources.map { it.id }, errors)

        validateEras(eras, errors)
        validateArticles(articles, eraIds, sourceIds, errors)
        validateStates(states, eraIds, sourceIds, errors)
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
}
