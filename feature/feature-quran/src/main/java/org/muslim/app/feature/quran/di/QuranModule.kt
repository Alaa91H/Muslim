package org.muslim.app.feature.quran.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.muslim.app.core.database.AppDatabase
import org.muslim.app.core.database.dao.AyahDao
import org.muslim.app.core.database.dao.AyahFtsDao
import org.muslim.app.core.database.dao.BookmarkDao
import org.muslim.app.core.database.dao.SurahDao
import org.muslim.app.core.database.dao.TafsirDao
import org.muslim.app.core.database.dao.TranslationDao
import org.muslim.app.feature.quran.data.QuranRepositoryImpl
import org.muslim.app.feature.quran.domain.QuranRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuranModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideSurahDao(database: AppDatabase): SurahDao = database.surahDao()

    @Provides
    fun provideAyahDao(database: AppDatabase): AyahDao = database.ayahDao()

    @Provides
    fun provideAyahFtsDao(database: AppDatabase): AyahFtsDao = database.ayahFtsDao()

    @Provides
    fun provideBookmarkDao(database: AppDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideTranslationDao(database: AppDatabase): TranslationDao = database.translationDao()

    @Provides
    fun provideTafsirDao(database: AppDatabase): TafsirDao = database.tafsirDao()

    @Provides
    @Singleton
    fun provideQuranRepository(
        @ApplicationContext context: Context,
        surahDao: SurahDao,
        ayahDao: AyahDao,
        ayahFtsDao: AyahFtsDao,
        bookmarkDao: BookmarkDao,
    ): QuranRepository = QuranRepositoryImpl(context, surahDao, ayahDao, ayahFtsDao, bookmarkDao)
}
