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

private val Context.learnPrefsDataStore by preferencesDataStore(name = "learn_prefs")

/** Persists the ids of the learning topics the user saved as favourites. */
@Singleton
class LearnPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val favoriteIds: Flow<Set<String>> =
        context.learnPrefsDataStore.data.map { it[Keys.FAVORITES] ?: emptySet() }

    suspend fun setFavorite(id: String, favorite: Boolean) {
        context.learnPrefsDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf()
            if (favorite) current.add(id) else current.remove(id)
            prefs[Keys.FAVORITES] = current
        }
    }

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorite_topic_ids")
    }
}
