package org.muslim.app.feature.scholarlibrary.domain

object ScholarStudyAnalytics {
    fun activitySummary(
        reviewEvents: List<ScholarReviewEvent>,
        sessions: List<ScholarStudySession>,
        cards: List<StudyFlashcard>,
        nowEpochMillis: Long,
        todayStartEpochMillis: Long,
        sevenDaysStartEpochMillis: Long,
    ): ScholarStudyActivitySummary {
        val recentReviews = reviewEvents.filter { it.reviewedAtEpochMillis >= sevenDaysStartEpochMillis }
        val recentSessions = sessions.filter {
            it.status == ScholarStudySessionStatus.Completed &&
                (it.completedAtEpochMillis ?: Long.MIN_VALUE) >= sevenDaysStartEpochMillis
        }
        return ScholarStudyActivitySummary(
            review = ScholarReviewActivity(
                reviewsToday = reviewEvents.count { it.reviewedAtEpochMillis >= todayStartEpochMillis },
                reviewsLast7Days = recentReviews.size,
                cardsReviewedLast7Days = recentReviews.map { it.flashcardId }.distinct().size,
                againLast7Days = recentReviews.count { it.outcome.rating == ScholarReviewRating.Again },
                hardLast7Days = recentReviews.count { it.outcome.rating == ScholarReviewRating.Hard },
                goodLast7Days = recentReviews.count { it.outcome.rating == ScholarReviewRating.Good },
                easyLast7Days = recentReviews.count { it.outcome.rating == ScholarReviewRating.Easy },
            ),
            study = ScholarStudyActivity(
                completedSessionsLast7Days = recentSessions.size,
                studiedPassagesLast7Days = recentSessions.sumOf { it.completedPassageIds.size },
                dueCards = cards.count { it.dueAtEpochMillis <= nowEpochMillis },
            ),
        )
    }

    fun dueCards(
        cards: List<FlashcardWithCitation>,
        nowEpochMillis: Long,
        category: ScholarCategory? = null,
        path: ScholarStudyPath? = null,
        bookId: String? = null,
    ): List<FlashcardWithCitation> {
        val pathBookIds = path?.stages?.flatMap { it.bookIds }?.toSet()
        return cards.asSequence()
            .filter { it.card.dueAtEpochMillis <= nowEpochMillis }
            .filter { category == null || it.category == category }
            .filter { pathBookIds == null || it.bookId in pathBookIds }
            .filter { bookId == null || it.bookId == bookId }
            .sortedWith(
                compareBy<FlashcardWithCitation> { it.card.dueAtEpochMillis }
                    .thenBy { it.card.intervalDays }
                    .thenBy { it.card.id },
            )
            .toList()
    }
}
