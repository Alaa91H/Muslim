package org.muslim.app.feature.learn.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.muslim.app.core.notifications.FeatureNotificationToggleHandler
import org.muslim.app.core.notifications.NotificationCategory
import org.muslim.app.feature.learn.data.HajjCompanionScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HajjNotificationToggleHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) : FeatureNotificationToggleHandler {

    override val category: NotificationCategory = NotificationCategory.Hajj

    override suspend fun onEnabledChanged(enabled: Boolean) {
        if (enabled) {
            HajjCompanionScheduler.schedule(context)
        } else {
            HajjCompanionScheduler.cancel(context)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LearnNotificationIntegrationModule {
    @Binds
    @IntoSet
    abstract fun bindHajjNotificationToggleHandler(
        implementation: HajjNotificationToggleHandler,
    ): FeatureNotificationToggleHandler
}
