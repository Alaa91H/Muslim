package org.muslim.app.core.notifications

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FeatureNotificationCoordinatorTest {

    @Test
    fun enabledChange_isDispatchedOnlyToMatchingCategory() = runTest {
        val hajj = RecordingToggleHandler(NotificationCategory.Hajj)
        val hadith = RecordingToggleHandler(NotificationCategory.HadithDaily)
        val coordinator = FeatureNotificationCoordinator(
            toggleHandlers = setOf(hajj, hadith),
            timedSettings = emptySet(),
        )

        coordinator.onEnabledChanged(NotificationCategory.Hajj, enabled = true)

        assertThat(hajj.events).containsExactly(true)
        assertThat(hadith.events).isEmpty()
    }

    @Test
    fun timedSettings_returnsTheUniqueProviderForCategory() {
        val hadith = FakeTimedSettings(NotificationCategory.HadithDaily, 8 * 60)
        val coordinator = FeatureNotificationCoordinator(
            toggleHandlers = emptySet(),
            timedSettings = setOf(hadith),
        )

        assertThat(coordinator.requireTimedSettings(NotificationCategory.HadithDaily))
            .isSameInstanceAs(hadith)
    }

    @Test(expected = IllegalStateException::class)
    fun timedSettings_rejectsMissingProvider() {
        FeatureNotificationCoordinator(
            toggleHandlers = emptySet(),
            timedSettings = emptySet(),
        ).requireTimedSettings(NotificationCategory.HadithDaily)
    }

    private class RecordingToggleHandler(
        override val category: NotificationCategory,
    ) : FeatureNotificationToggleHandler {
        val events = mutableListOf<Boolean>()

        override suspend fun onEnabledChanged(enabled: Boolean) {
            events += enabled
        }
    }

    private class FakeTimedSettings(
        override val category: NotificationCategory,
        override val defaultTimeMinutes: Int,
    ) : TimedFeatureNotificationSettings {
        override val timeMinutes: Flow<Int> = flowOf(defaultTimeMinutes)

        override suspend fun setTimeMinutes(minutes: Int) = Unit
    }
}
