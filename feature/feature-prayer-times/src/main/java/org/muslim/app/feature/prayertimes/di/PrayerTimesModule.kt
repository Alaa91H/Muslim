package org.muslim.app.feature.prayertimes.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.muslim.app.core.location.FusedLocationProvider
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrayerTimesModule {

    @Provides
    @Singleton
    fun provideCalculator(): PrayerTimesCalculator = PrayerTimesCalculator()

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context,
    ): LocationProvider = FusedLocationProvider(context)
}
