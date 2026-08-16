package org.example.islamicapp.feature.prayertimes.notifications

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.example.islamicapp.core.datastore.prayer.PrayerSettingsRepository

/**
 * Hilt does not support injection into BroadcastReceivers; receivers use this
 * entry point to obtain singletons from the app's component.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdhanEntryPoint {
    fun scheduler(): AdhanScheduler
    fun settingsRepository(): PrayerSettingsRepository
    fun soundRepository(): AdhanSoundRepository
}
