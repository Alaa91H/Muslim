package org.muslim.app.feature.scholarlibrary.domain

import kotlin.math.roundToInt

data class ScholarReviewSchedule(
    val reviewCount: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val lapseCount: Int,
    val dueAtEpochMillis: Long,
    val lastReviewedAtEpochMillis: Long,
    val lastRating: ScholarReviewRating,
)

object ScholarReviewScheduler {
    fun schedule(
        card: StudyFlashcard,
        rating: ScholarReviewRating,
        reviewedAtEpochMillis: Long,
    ): ScholarReviewSchedule {
        val nextEase = when (rating) {
            ScholarReviewRating.Again -> (card.easeFactor - AGAIN_EASE_PENALTY).coerceAtLeast(MIN_EASE)
            ScholarReviewRating.Hard -> (card.easeFactor - HARD_EASE_PENALTY).coerceAtLeast(MIN_EASE)
            ScholarReviewRating.Good -> card.easeFactor.coerceIn(MIN_EASE, MAX_EASE)
            ScholarReviewRating.Easy -> (card.easeFactor + EASY_EASE_BONUS).coerceAtMost(MAX_EASE)
        }
        val nextInterval = when (rating) {
            ScholarReviewRating.Again -> 0
            ScholarReviewRating.Hard -> hardInterval(card)
            ScholarReviewRating.Good -> goodInterval(card)
            ScholarReviewRating.Easy -> easyInterval(card)
        }
        val dueAt = when (rating) {
            ScholarReviewRating.Again -> reviewedAtEpochMillis + AGAIN_DELAY_MILLIS
            else -> reviewedAtEpochMillis + nextInterval.toLong() * DAY_MILLIS
        }
        return ScholarReviewSchedule(
            reviewCount = card.reviewCount + 1,
            intervalDays = nextInterval,
            easeFactor = nextEase,
            lapseCount = card.lapseCount + if (rating == ScholarReviewRating.Again) 1 else 0,
            dueAtEpochMillis = dueAt,
            lastReviewedAtEpochMillis = reviewedAtEpochMillis,
            lastRating = rating,
        )
    }

    fun reviewSummary(
        cards: List<StudyFlashcard>,
        nowEpochMillis: Long,
    ): ScholarReviewSummary {
        if (cards.isEmpty()) {
            return ScholarReviewSummary(
                totalCards = 0,
                dueCards = 0,
                learningCards = 0,
                matureCards = 0,
                estimatedMasteryPercent = 0,
            )
        }
        return ScholarReviewSummary(
            totalCards = cards.size,
            dueCards = cards.count { it.dueAtEpochMillis <= nowEpochMillis },
            learningCards = cards.count { it.intervalDays in 0 until MATURE_INTERVAL_DAYS },
            matureCards = cards.count { it.intervalDays >= MATURE_INTERVAL_DAYS },
            estimatedMasteryPercent = cards.map(::masteryPercent).average().roundToInt().coerceIn(0, 100),
        )
    }

    fun categoryMastery(
        cards: List<FlashcardWithCitation>,
        nowEpochMillis: Long,
    ): List<ScholarCategoryMastery> =
        cards.groupBy { it.category }
            .map { (category, categoryCards) ->
                ScholarCategoryMastery(
                    category = category,
                    totalCards = categoryCards.size,
                    dueCards = categoryCards.count { it.card.dueAtEpochMillis <= nowEpochMillis },
                    estimatedMasteryPercent = categoryCards
                        .map { masteryPercent(it.card) }
                        .average()
                        .roundToInt()
                        .coerceIn(0, 100),
                )
            }
            .sortedWith(
                compareByDescending<ScholarCategoryMastery> { it.totalCards }
                    .thenBy { it.category.name },
            )

    fun masteryPercent(card: StudyFlashcard): Int {
        val intervalScore = when {
            card.intervalDays >= 60 -> 100
            card.intervalDays >= 30 -> 90
            card.intervalDays >= 21 -> 80
            card.intervalDays >= 14 -> 70
            card.intervalDays >= 7 -> 60
            card.intervalDays >= 3 -> 40
            card.intervalDays >= 1 -> 20
            else -> 0
        }
        return (intervalScore - card.lapseCount * LAPSE_MASTERY_PENALTY).coerceIn(0, 100)
    }

    private fun hardInterval(card: StudyFlashcard): Int = when {
        card.intervalDays <= 0 -> 1
        else -> (card.intervalDays * HARD_MULTIPLIER).roundToInt().coerceAtLeast(1)
    }

    private fun goodInterval(card: StudyFlashcard): Int = when {
        card.intervalDays <= 0 -> 1
        card.intervalDays == 1 -> 3
        else -> (card.intervalDays * card.easeFactor.coerceIn(MIN_EASE, MAX_EASE))
            .roundToInt()
            .coerceAtLeast(card.intervalDays + 1)
    }

    private fun easyInterval(card: StudyFlashcard): Int = when {
        card.intervalDays <= 0 -> 4
        else -> (
            card.intervalDays *
                card.easeFactor.coerceIn(MIN_EASE, MAX_EASE) *
                EASY_MULTIPLIER
            ).roundToInt().coerceAtLeast(card.intervalDays + 2)
    }

    private const val MIN_EASE = 1.3
    private const val MAX_EASE = 3.0
    private const val AGAIN_EASE_PENALTY = 0.2
    private const val HARD_EASE_PENALTY = 0.15
    private const val EASY_EASE_BONUS = 0.15
    private const val HARD_MULTIPLIER = 1.2
    private const val EASY_MULTIPLIER = 1.3
    private const val LAPSE_MASTERY_PENALTY = 5
    private const val MATURE_INTERVAL_DAYS = 21
    private const val DAY_MILLIS = 24L * 60 * 60 * 1000
    private const val AGAIN_DELAY_MILLIS = 10L * 60 * 1000
}
