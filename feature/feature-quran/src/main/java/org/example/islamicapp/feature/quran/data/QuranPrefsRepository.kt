package org.example.islamicapp.feature.quran.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.islamicapp.feature.quran.domain.LastRead
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quranPrefsDataStore by preferencesDataStore(name = "quran_prefs")

/** Device-local reader preferences (PROJECT_PROMPT.md §6: متابعة آخر قراءة). */
@Singleton
class QuranPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val lastRead: Flow<LastRead?> = context.quranPrefsDataStore.data.map { prefs ->
        val surah = prefs[Keys.LAST_SURAH] ?: return@map null
        val global = prefs[Keys.LAST_GLOBAL] ?: return@map null
        val inSurah = prefs[Keys.LAST_IN_SURAH] ?: return@map null
        LastRead(surahNumber = surah, globalNumber = global, numberInSurah = inSurah)
    }

    suspend fun saveLastRead(lastRead: LastRead) {
        context.quranPrefsDataStore.edit { prefs ->
            prefs[Keys.LAST_SURAH] = lastRead.surahNumber
            prefs[Keys.LAST_GLOBAL] = lastRead.globalNumber
            prefs[Keys.LAST_IN_SURAH] = lastRead.numberInSurah
        }
    }

    private object Keys {
        val LAST_SURAH = intPreferencesKey("last_surah")
        val LAST_GLOBAL = intPreferencesKey("last_global")
        val LAST_IN_SURAH = intPreferencesKey("last_in_surah")
    }
}
