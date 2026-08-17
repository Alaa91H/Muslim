package org.muslim.app.core.notifications

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt does not support injection into BroadcastReceivers (and notifier
 * objects are called from receivers); they obtain the unified
 * [NotificationPrefsRepository] through this entry point so every notifier
 * can check the master per-category switch before posting.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationEntryPoint {
    fun notificationPrefs(): NotificationPrefsRepository
}
