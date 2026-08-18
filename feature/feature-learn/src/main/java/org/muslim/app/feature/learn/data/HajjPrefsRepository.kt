package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.hajjPrefsDataStore by preferencesDataStore(name = "hajj_prefs")

/**
 * Persists the pilgrim's interactive checklist progress through the Hajj/Umrah
 * rites. Keys are "topicId:stepIndex"; a present key means that step is done.
 */
@Singleton
class HajjPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val checkedStepKeys: Flow<Set<String>> =
        context.hajjPrefsDataStore.data.map { it[Keys.CHECKED_STEPS] ?: emptySet() }

    suspend fun setStepChecked(key: String, checked: Boolean) {
        context.hajjPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.CHECKED_STEPS]?.toMutableSet() ?: mutableSetOf()
            if (checked) current.add(key) else current.remove(key)
            prefs[Keys.CHECKED_STEPS] = current
        }
    }

    private object Keys {
        val CHECKED_STEPS = stringSetPreferencesKey("checked_hajj_steps")
    }
}
