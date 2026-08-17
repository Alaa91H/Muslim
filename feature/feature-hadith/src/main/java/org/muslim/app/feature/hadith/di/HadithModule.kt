package org.muslim.app.feature.hadith.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import org.muslim.app.feature.hadith.data.HadithDao
import org.muslim.app.feature.hadith.data.HadithDatabase
import org.muslim.app.feature.hadith.data.HadithFtsDao
import org.muslim.app.feature.hadith.data.HadithPrefsRepository
import org.muslim.app.feature.hadith.data.HadithRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HadithModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HadithDatabase =
        HadithDatabase.getInstance(context)

    @Provides
    fun provideHadithDao(database: HadithDatabase): HadithDao = database.hadithDao()

    @Provides
    fun provideHadithFtsDao(database: HadithDatabase): HadithFtsDao = database.hadithFtsDao()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRepository(
        @ApplicationContext context: Context,
        hadithDao: HadithDao,
        hadithFtsDao: HadithFtsDao,
        prefsRepository: HadithPrefsRepository,
        json: Json,
    ): HadithRepository = HadithRepository(context, hadithDao, hadithFtsDao, prefsRepository, json)
}
