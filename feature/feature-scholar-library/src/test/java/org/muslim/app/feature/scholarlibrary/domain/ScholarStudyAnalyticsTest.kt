package org.muslim.app.feature.scholarlibrary.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScholarStudyAnalyticsTest {
    @Test
    fun activitySummaryCountsTodayAndRollingSevenDays() {
        val now = 10L * DAY_MILLIS
        val todayStart = 10L * DAY_MILLIS
        val sevenDaysStart = 3L * DAY_MILLIS
        val events = listOf(
            event(1, now + 1_000L, ScholarReviewRating.Good),
            event(2, 9L * DAY_MILLIS, ScholarReviewRating.Again),
            event(3, 2L * DAY_MILLIS, ScholarReviewRating.Easy),
        )
        val sessions = listOf(
            session(1, 8L * DAY_MILLIS, listOf("p1", "p2")),
            session(2, 1L * DAY_MILLIS, listOf("p3")),
        )
        val cards = listOf(
            card(1, dueAt = now - 1),
            card(2, dueAt = now + DAY_MILLIS),
        )

        val summary = ScholarStudyAnalytics.activitySummary(
            reviewEvents = events,
            sessions = sessions,
            cards = cards,
            nowEpochMillis = now,
            todayStartEpochMillis = todayStart,
            sevenDaysStartEpochMillis = sevenDaysStart,
        )

        assertThat(summary.reviewsToday).isEqualTo(1)
        assertThat(summary.reviewsLast7Days).isEqualTo(2)
        assertThat(summary.cardsReviewedLast7Days).isEqualTo(2)
        assertThat(summary.againLast7Days).isEqualTo(1)
        assertThat(summary.goodLast7Days).isEqualTo(1)
        assertThat(summary.completedSessionsLast7Days).isEqualTo(1)
        assertThat(summary.studiedPassagesLast7Days).isEqualTo(2)
        assertThat(summary.dueCards).isEqualTo(1)
    }

    @Test
    fun dueQueueFiltersByCategoryPathAndBook() {
        val cards = listOf(
            cardWithCitation(1, "book-a", ScholarCategory.Hadith),
            cardWithCitation(2, "book-b", ScholarCategory.Hadith),
            cardWithCitation(3, "book-c", ScholarCategory.Fiqh),
        )
        val path = ScholarStudyPath(
            id = "path",
            title = "مسار",
            summary = "وصف",
            category = ScholarCategory.Hadith,
            level = ScholarDifficulty.Foundation,
            stages = listOf(
                ScholarStudyStage("stage", "مرحلة", "وصف", listOf("book-b", "book-c")),
            ),
        )

        val result = ScholarStudyAnalytics.dueCards(
            cards = cards,
            nowEpochMillis = 100L,
            category = ScholarCategory.Hadith,
            path = path,
            bookId = "book-b",
        )

        assertThat(result.map { it.card.id }).containsExactly(2L)
    }

    private fun event(
        id: Long,
        reviewedAt: Long,
        rating: ScholarReviewRating,
    ) = ScholarReviewEvent(
        id = id,
        flashcardId = id,
        passageId = "passage-$id",
        bookId = "book",
        category = ScholarCategory.Hadith,
        reviewedAtEpochMillis = reviewedAt,
        outcome = ScholarReviewOutcome(
            rating = rating,
            scheduledIntervalDays = 3,
            lapseCountAfterReview = if (rating == ScholarReviewRating.Again) 1 else 0,
            easeFactorAfterReview = 2.5,
        ),
    )

    private fun session(
        id: Long,
        completedAt: Long,
        passages: List<String>,
    ) = ScholarStudySession(
        id = id,
        pathId = "path",
        planId = 1,
        bookId = "book",
        targetPassageIds = passages,
        completedPassageIds = passages,
        plannedMinutes = 20,
        status = ScholarStudySessionStatus.Completed,
        startedAtEpochMillis = completedAt - 10_000L,
        completedAtEpochMillis = completedAt,
    )

    private fun card(
        id: Long,
        dueAt: Long,
    ) = StudyFlashcard(
        id = id,
        passageId = "passage-$id",
        front = "سؤال",
        back = "جواب",
        createdAtEpochMillis = 0L,
        reviewState = ScholarFlashcardReviewState(dueAtEpochMillis = dueAt),
    )

    private fun cardWithCitation(
        id: Long,
        bookId: String,
        category: ScholarCategory,
    ) = FlashcardWithCitation(
        card = card(id, dueAt = 0L),
        citation = Citation(
            bookTitle = bookId,
            author = "مؤلف",
            chapter = "باب",
            volume = null,
            page = null,
        ),
        bookId = bookId,
        category = category,
    )

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}
