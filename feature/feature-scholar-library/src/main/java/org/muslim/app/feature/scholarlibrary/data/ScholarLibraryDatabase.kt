package org.muslim.app.feature.scholarlibrary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Private on-device database for the study library. It deliberately contains
 * only content bundled with clear metadata or material the user imports.
 */
@Database(
    entities = [
        ScholarBookEntity::class,
        ScholarPassageEntity::class,
        ScholarPassageFtsEntity::class,
        ScholarNoteEntity::class,
        ScholarFlashcardEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ScholarLibraryDatabase : RoomDatabase() {
    abstract fun libraryDao(): ScholarLibraryDao
    abstract fun ftsDao(): ScholarLibraryFtsDao

    companion object {
        private const val DB_NAME = "scholar_library.db"

        @Volatile
        private var instance: ScholarLibraryDatabase? = null

        fun getInstance(context: Context): ScholarLibraryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScholarLibraryDatabase::class.java,
                    DB_NAME,
                ).build().also { instance = it }
            }
    }
}
