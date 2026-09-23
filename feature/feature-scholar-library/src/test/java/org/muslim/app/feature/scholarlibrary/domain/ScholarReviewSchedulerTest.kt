package org.muslim.app.feature.scholarlibrary.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScholarReviewSchedulerTest {
    @Test
    fun againReturnsSoonAndRecordsLapse() {
        val result = ScholarReviewScheduler.schedule(
            card = card(intervalDays = 14, easeFactor = 2.5, lapseCount = 1),
            rating = ScholarReviewRating.Again,
            reviewedAtEpochMillis = 1_000_000L,
        )

        assertThat(result.intervalDays).isEqualTo(0)
        assertThat(result.lapseCount).isEqualTo(2)
        assertThat(result.easeFactor).isWithin(0.0001).of(2.3)
        assertThat(result.dueAtEpochMillis).isEqualTo(1_600_000L)
    }

    @Test
    fun goodExpandsIntervalUsingCurrentEase() {
        val result = ScholarReviewScheduler.schedule(
            card = card(intervalDays = 7, easeFactor = 2.5),
            rating = ScholarReviewRating.Good,
            reviewedAtEpochMillis = 0L,
        )

        assertThat(result.intervalDays).isEqualTo(18)
        assertThat(result.reviewCount).isEqualTo(3)
        assertThat(result.lastRating).isEqualTo(ScholarReviewRating.Good)
    }

    @Test
    fun easyExpandsMoreThanGood() {
        val source = card(intervalDays = 7, easeFactor = 2.5)

        val good = ScholarReviewScheduler.schedule(source, ScholarReviewRating.Good, 0L)
        val easy = ScholarReviewScheduler.schedule(source, ScholarReviewRating.Easy, 0L)

        assertThat(easy.intervalDays).isGreaterThan(good.intervalDays)
        assertThat(easy.easeFactor).isGreaterThan(good.easeFactor)
    }

    @Test
    fun summaryCountsDueLearningAndMatureCards() {
        val cards = listOf(
            card(id = 1, intervalDays = 0, dueAtEpochMillis = 0L),
            card(id = 2, intervalDays = 7, dueAtEpochMillis = 10_000L),
            card(id = 3, intervalDays = 30, dueAtEpochMillis = 10_000L),
        )

        val summary = ScholarReviewScheduler.reviewSummary(cards, nowEpochMillis = 1_000L)

        assertThat(summary.totalCards).isEqualTo(3)
        assertThat(summary.dueCards).isEqualTo(1)
        assertThat(summary.learningCards).isEqualTo(2)
        assertThat(summary.matureCards).isEqualTo(1)
        assertThat(summary.estimatedMasteryPercent).isGreaterThan(0)
    }

    private fun card(
        id: Long = 1,
        intervalDays: Int = 0,
        easeFactor: Double = 2.5,
        lapseCount: Int = 0,
        dueAtEpochMillis: Long = 0L,
    ) = StudyFlashcard(
        id = id,
        passageId = "passage-$id",
        front = "سؤال",
        back = "جواب",
        reviewCount = 2,
        dueAtEpochMillis = dueAtEpochMillis,
        createdAtEpochMillis = 0L,
        intervalDays = intervalDays,
        easeFactor = easeFactor,
        lapseCount = lapseCount,
    )
}
