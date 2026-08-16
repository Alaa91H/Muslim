package org.example.islamicapp.feature.quran.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.example.islamicapp.core.database.AppDatabase
import org.example.islamicapp.core.database.dao.AyahDao
import org.example.islamicapp.core.database.dao.AyahFtsDao
import org.example.islamicapp.core.database.dao.BookmarkDao
import org.example.islamicapp.core.database.dao.SurahDao
import org.example.islamicapp.feature.quran.data.QuranRepositoryImpl
import org.example.islamicapp.feature.quran.domain.QuranRepository
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
    @Singleton
    fun provideQuranRepository(
        @ApplicationContext context: Context,
        database: AppDatabase,
        surahDao: SurahDao,
        ayahDao: AyahDao,
        ayahFtsDao: AyahFtsDao,
        bookmarkDao: BookmarkDao,
    ): QuranRepository = QuranRepositoryImpl(
        context = context,
        database = database,
        surahDao = surahDao,
        ayahDao = ayahDao,
        ayahFtsDao = ayahFtsDao,
        bookmarkDao = bookmarkDao,
    )
}
