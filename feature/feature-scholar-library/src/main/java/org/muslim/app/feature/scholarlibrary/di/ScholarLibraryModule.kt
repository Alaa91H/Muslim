package org.muslim.app.feature.scholarlibrary.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryDao
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryDatabase
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryFtsDao
import org.muslim.app.feature.scholarlibrary.data.ScholarLibraryRepository

@Module
@InstallIn(SingletonComponent::class)
object ScholarLibraryModule {
    @Provides
    @Singleton
    fun provideScholarLibraryDatabase(@ApplicationContext context: Context): ScholarLibraryDatabase =
        ScholarLibraryDatabase.getInstance(context)

    @Provides
    fun provideScholarLibraryDao(database: ScholarLibraryDatabase): ScholarLibraryDao = database.libraryDao()

    @Provides
    fun provideScholarLibraryFtsDao(database: ScholarLibraryDatabase): ScholarLibraryFtsDao = database.ftsDao()

    @Provides
    @Singleton
    fun provideScholarLibraryRepository(
        @ApplicationContext context: Context,
        libraryDao: ScholarLibraryDao,
        ftsDao: ScholarLibraryFtsDao,
        json: Json,
    ): ScholarLibraryRepository = ScholarLibraryRepository(context, libraryDao, ftsDao, json)
}
