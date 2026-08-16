package org.example.islamicapp.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RepeatPlanTest {

    @Test
    fun `single ayah repeats the requested number of times`() {
        assertThat(RepeatPlan.sequence(start = 10, end = 10, count = 3))
            .containsExactly(10, 10, 10)
            .inOrder()
    }

    @Test
    fun `passage loops in mushaf order for each pass`() {
        assertThat(RepeatPlan.sequence(start = 1, end = 3, count = 2))
            .containsExactly(1, 2, 3, 1, 2, 3)
            .inOrder()
    }

    @Test
    fun `larger passage keeps order across passes`() {
        assertThat(RepeatPlan.sequence(start = 7, end = 9, count = 2))
            .containsExactly(7, 8, 9, 7, 8, 9)
            .inOrder()
    }

    @Test
    fun `count of one plays the passage exactly once`() {
        assertThat(RepeatPlan.sequence(start = 4, end = 6, count = 1))
            .containsExactly(4, 5, 6)
            .inOrder()
    }

    @Test
    fun `invalid inputs produce an empty sequence`() {
        assertThat(RepeatPlan.sequence(start = 0, end = 1, count = 1)).isEmpty()
        assertThat(RepeatPlan.sequence(start = 5, end = 2, count = 1)).isEmpty()
        assertThat(RepeatPlan.sequence(start = 1, end = 3, count = 0)).isEmpty()
        assertThat(RepeatPlan.sequence(start = 1, end = 3, count = -2)).isEmpty()
    }
}
