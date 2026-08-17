package org.muslim.app.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.core.content.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPreferencesDataStore by preferencesDataStore(name = "app_preferences")

/**
 * Persists the app-wide preferences ([AppPreferences]) in a DataStore on the
 * device. This is the single source for the UI theme, the app language and
 * accessibility-related toggles (PROJECT_PROMPT.md §4/§5/§6).
 */
@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val preferences: Flow<AppPreferences> = context.appPreferencesDataStore.data.map { prefs ->
        AppPreferences(
            themeMode = runCatching { AppThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: AppThemeMode.System.name) }
                .getOrDefault(AppThemeMode.System),
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            languageCode = prefs[Keys.LANGUAGE] ?: AppPreferences.SYSTEM_LANGUAGE,
            reduceAnimations = prefs[Keys.REDUCE_ANIMATIONS] ?: false,
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) = edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = edit { prefs -> prefs[Keys.DYNAMIC_COLOR] = enabled }

    suspend fun setLanguage(languageCode: String) {
        edit { prefs -> prefs[Keys.LANGUAGE] = languageCode }
        // Synchronous mirror so attachBaseContext can apply the locale without
        // an async read (DataStore cannot be read synchronously).
        languageMirror.edit { putString(Keys.LANGUAGE.name, languageCode) }
    }

    suspend fun setReduceAnimations(enabled: Boolean) = edit { prefs -> prefs[Keys.REDUCE_ANIMATIONS] = enabled }

    /** Blocking read of the persisted language, safe for [android.app.Activity.attachBaseContext]. */
    fun readLanguageSync(): String =
        languageMirror.getString(Keys.LANGUAGE.name, AppPreferences.SYSTEM_LANGUAGE)
            ?: AppPreferences.SYSTEM_LANGUAGE

    private suspend fun edit(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.appPreferencesDataStore.edit { prefs -> transform(prefs) }
    }

    private val languageMirror: SharedPreferences
        get() = context.getSharedPreferences("app_locale", Context.MODE_PRIVATE)

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val LANGUAGE = stringPreferencesKey("language")
        val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
    }
}
