package org.muslim.app.feature.prayertimes.notifications

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.muslim.app.core.datastore.prayer.PrayerSettingsRepository

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
    fun soundPlayer(): AdhanSoundPlayer
    fun dndManager(): PrayerDndManager
}
