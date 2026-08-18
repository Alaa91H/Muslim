package org.muslim.app.feature.quran.data

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt does not support injection into BroadcastReceivers; the boot receiver
 * uses this entry point to obtain the download manager singleton (same pattern
 * as the adhkar and prayer-times modules).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuranDownloadEntryPoint {
    fun downloadManager(): QuranDownloadManager
}
