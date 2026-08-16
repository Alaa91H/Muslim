package org.example.islamicapp.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Core networking (PROJECT_PROMPT.md §3.2: `core-network`).
 *
 * The app is offline-first: every network capability is optional and the UI
 * never blocks on it. This module owns the single OkHttp client used by all
 * optional downloads (adhan sounds, Quran recitations, tafsir packs, zakat
 * nisab updates). Data is fetched on demand and cached on-device.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}
