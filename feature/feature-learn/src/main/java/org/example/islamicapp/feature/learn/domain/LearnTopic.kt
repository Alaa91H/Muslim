package org.example.islamicapp.feature.learn.domain

/**
 * A learn guide (PROJECT_PROMPT.md §6 Phase 5): ordered steps plus neutral
 * notes on known juristic differences — presented without preferring any
 * school (§10: تعرض الخلافات الفقهية بحياد دون ترجيح).
 */
data class LearnTopic(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    /** Ordered steps of the guide. */
    val steps: List<LearnStep>,
    /** Neutral notes on madhhab differences, keyed by topic. */
    val differences: List<MadhhabNote> = emptyList(),
)

data class LearnStep(
    val titleAr: String,
    val titleEn: String,
    val detailAr: String,
    val detailEn: String,
)

/** A neutral presentation of a known juristic difference. */
data class MadhhabNote(
    val pointAr: String,
    val pointEn: String,
)
