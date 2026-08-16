package org.example.islamicapp.feature.quran.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.islamicapp.feature.quran.domain.LastRead
import org.example.islamicapp.feature.quran.domain.ReaderTheme
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

    /** Reader colour theme (light / sepia / dark), independent of the app theme. */
    val readerTheme: Flow<ReaderTheme> = context.quranPrefsDataStore.data.map { prefs ->
        runCatching { ReaderTheme.valueOf(prefs[Keys.READER_THEME] ?: ReaderTheme.Light.name) }
            .getOrDefault(ReaderTheme.Light)
    }

    suspend fun setReaderTheme(theme: ReaderTheme) {
        context.quranPrefsDataStore.edit { prefs -> prefs[Keys.READER_THEME] = theme.name }
    }

    /**
     * Farthest ayah the user has read (global number) — drives the khatma
     * progress (PROJECT_PROMPT.md §6 Phase 2: متابعة تقدّم الختمة).
     */
    val readThroughGlobal: Flow<Int> =
        context.quranPrefsDataStore.data.map { prefs -> prefs[Keys.READ_THROUGH] ?: 0 }

    /** Advances the khatma progress; never moves backwards. */
    suspend fun advanceReadThrough(globalNumber: Int) {
        context.quranPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.READ_THROUGH] ?: 0
            if (globalNumber > current) prefs[Keys.READ_THROUGH] = globalNumber
        }
    }

    /** Reader font size in sp. */
    val readerFontSize: Flow<Float> =
        context.quranPrefsDataStore.data.map { prefs -> prefs[Keys.FONT_SIZE] ?: 26f }

    suspend fun setReaderFontSize(sp: Float) {
        context.quranPrefsDataStore.edit { prefs -> prefs[Keys.FONT_SIZE] = sp }
    }

    /** Selected reciter id (defaults to the first bundled reciter). */
    val selectedReciterId: Flow<String> = context.quranPrefsDataStore.data.map { prefs ->
        prefs[Keys.RECITER] ?: "abdulbasit_murattal"
    }

    suspend fun setSelectedReciterId(id: String) {
        context.quranPrefsDataStore.edit { prefs -> prefs[Keys.RECITER] = id }
    }

    private object Keys {
        val LAST_SURAH = intPreferencesKey("last_surah")
        val LAST_GLOBAL = intPreferencesKey("last_global")
        val LAST_IN_SURAH = intPreferencesKey("last_in_surah")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val READ_THROUGH = intPreferencesKey("read_through_global")
        val FONT_SIZE = floatPreferencesKey("reader_font_size")
        val RECITER = stringPreferencesKey("reciter_id")
    }
}
