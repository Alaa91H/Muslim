package org.muslim.app.core.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the notification-prefs [DataStore] so [NotificationPrefsRepository]
 * can be constructed cleanly through Hilt — and injected with a temp-file
 * store in unit tests.
 */
private val Context.notificationPrefsDataStore by preferencesDataStore(name = "notification_prefs")

@Module
@InstallIn(SingletonComponent::class)
object NotificationDataStoreModule {

    @Provides
    @Singleton
    fun provideNotificationPrefsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.notificationPrefsDataStore
}
