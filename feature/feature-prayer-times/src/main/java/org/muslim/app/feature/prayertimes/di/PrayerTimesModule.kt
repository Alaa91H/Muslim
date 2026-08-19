package org.muslim.app.feature.prayertimes.di

import android.content.Context
import android.location.Geocoder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.muslim.app.core.location.FusedLocationProvider
import org.muslim.app.core.location.GeocoderRegionNameResolver
import org.muslim.app.core.location.LocationProvider
import org.muslim.app.core.location.RegionNameResolver
import org.muslim.app.core.common.prayer.PrayerTimesCalculator
import java.util.Locale
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

    @Provides
    @Singleton
    fun provideRegionNameResolver(
        @ApplicationContext context: Context,
    ): RegionNameResolver = GeocoderRegionNameResolver(Geocoder(context, Locale.getDefault()))
}
