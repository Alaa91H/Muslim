package org.muslim.app.crash

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CrashLogStoreTest {

    @Test
    fun format_contains_exception_class_message_and_stack() {
        val error = IllegalStateException("boom")
        val report = CrashLogStore.format(error, timestamp = 1_700_000_000_000L)

        assertThat(report).contains("exception=java.lang.IllegalStateException")
        assertThat(report).contains("message=boom")
        assertThat(report).contains("java.lang.IllegalStateException: boom")
    }

    @Test
    fun format_handles_null_message() {
        val error = NullPointerException()
        val report = CrashLogStore.format(error, timestamp = 1_700_000_000_000L)

        assertThat(report).contains("message=<none>")
    }

    @Test
    fun format_is_deterministic_for_a_given_timestamp() {
        val error = RuntimeException("x")
        assertThat(CrashLogStore.format(error, 42L))
            .isEqualTo(CrashLogStore.format(error, 42L))
    }
}
