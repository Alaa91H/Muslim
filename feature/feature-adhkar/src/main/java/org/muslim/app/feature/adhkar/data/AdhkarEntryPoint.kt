package org.muslim.app.feature.adhkar.data

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt does not support injection into BroadcastReceivers; receivers use this
 * entry point to obtain singletons from the app's component (same pattern as
 * the prayer-times module).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdhkarEntryPoint {
    fun prefsRepository(): AdhkarPrefsRepository
    fun adhkarRepository(): AdhkarRepository
    fun reminderScheduler(): AdhkarReminderScheduler
    fun periodicReminderScheduler(): PeriodicAdhkarReminderScheduler
}
