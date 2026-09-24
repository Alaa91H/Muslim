package org.muslim.app.feature.hadith.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.core.notifications.TimedFeatureNotificationSettings
import org.muslim.app.feature.hadith.data.HadithOfTheDayScheduler
import org.muslim.app.feature.hadith.data.HadithPrefsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HadithDailyNotificationSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsRepository: HadithPrefsRepository,
) : TimedFeatureNotificationSettings {

    override val category: NotificationCategory = NotificationCategory.HadithDaily

    override val timeMinutes: Flow<Int> =
        prefsRepository.dailyNotificationTimeMinutes

    override val defaultTimeMinutes: Int =
        HadithPrefsRepository.DEFAULT_NOTIFICATION_TIME_MINUTES

    override suspend fun setTimeMinutes(minutes: Int) {
        prefsRepository.setDailyNotificationTimeMinutes(minutes)
        if (prefsRepository.dailyNotificationEnabled.first()) {
            HadithOfTheDayScheduler.schedule(context, minutes)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HadithNotificationIntegrationModule {
    @Binds
    @IntoSet
    abstract fun bindHadithDailyNotificationSettings(
        implementation: HadithDailyNotificationSettings,
    ): TimedFeatureNotificationSettings
}
