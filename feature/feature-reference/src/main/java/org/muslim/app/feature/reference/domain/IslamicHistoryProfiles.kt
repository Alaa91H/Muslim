package org.muslim.app.feature.reference.domain

/**
 * Stable facade for people and place profiles.
 *
 * Content is split into dedicated catalogues to keep each source file within static-analysis
 * complexity limits while preserving one lookup surface for UI, validation, and search.
 */
object IslamicHistoryProfiles {
    val people: List<HistoryPersonProfile>
        get() = IslamicHistoryPeopleProfiles.people

    val places: List<HistoricalPlaceProfile>
        get() = IslamicHistoryPlaceProfiles.places

    fun personById(id: String): HistoryPersonProfile? = IslamicHistoryPeopleProfiles.byId(id)

    fun placeById(id: String): HistoricalPlaceProfile? = IslamicHistoryPlaceProfiles.byId(id)

    fun atlasPlaceById(id: String): HistoricalPlace? =
        IslamicHistoryContent.atlasLayers
            .asSequence()
            .flatMap { it.places.asSequence() }
            .firstOrNull { it.id == id }
}
