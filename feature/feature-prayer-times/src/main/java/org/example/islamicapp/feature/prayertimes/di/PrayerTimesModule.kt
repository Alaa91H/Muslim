package org.example.islamicapp.feature.prayertimes.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.example.islamicapp.core.location.FusedLocationProvider
import org.example.islamicapp.core.location.LocationProvider
import org.example.islamicapp.feature.prayertimes.domain.PrayerTimesCalculator
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
