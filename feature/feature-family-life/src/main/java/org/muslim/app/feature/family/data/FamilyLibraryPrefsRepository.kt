package org.muslim.app.feature.family.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.family.domain.FamilyUtilityContent

private val Context.familyLibraryDataStore by preferencesDataStore(name = "family_library_prefs")

@Singleton
class FamilyLibraryPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val favoriteArticleIds: Flow<Set<String>> =
        context.familyLibraryDataStore.data.map { prefs ->
            prefs[Keys.FAVORITE_ARTICLE_IDS] ?: emptySet()
        }

    val recentArticleIds: Flow<List<String>> =
        context.familyLibraryDataStore.data.map { prefs ->
            prefs[Keys.RECENT_ARTICLE_IDS]
                .orEmpty()
                .split(RECENT_SEPARATOR)
                .filter { it.isNotBlank() }
                .distinct()
                .take(MAX_RECENT_ARTICLES)
        }

    val completedChecklistItemIds: Flow<Set<String>> =
        context.familyLibraryDataStore.data.map { prefs ->
            prefs[Keys.COMPLETED_CHECKLIST_ITEM_IDS] ?: emptySet()
        }

    suspend fun setArticleFavorite(articleId: String, favorite: Boolean) {
        context.familyLibraryDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_ARTICLE_IDS] ?: emptySet()
            prefs[Keys.FAVORITE_ARTICLE_IDS] =
                if (favorite) current + articleId else current - articleId
        }
    }

    suspend fun recordArticleOpened(articleId: String) {
        context.familyLibraryDataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_ARTICLE_IDS]
                .orEmpty()
                .split(RECENT_SEPARATOR)
                .filter { it.isNotBlank() && it != articleId }
            prefs[Keys.RECENT_ARTICLE_IDS] =
                (listOf(articleId) + current)
                    .take(MAX_RECENT_ARTICLES)
                    .joinToString(RECENT_SEPARATOR)
        }
    }

    suspend fun clearRecentArticles() {
        context.familyLibraryDataStore.edit { prefs ->
            prefs.remove(Keys.RECENT_ARTICLE_IDS)
        }
    }

    suspend fun setChecklistItemCompleted(
        checklistId: String,
        itemId: String,
        completed: Boolean,
    ) {
        val key = FamilyUtilityContent.completionKey(checklistId, itemId)
        context.familyLibraryDataStore.edit { prefs ->
            val current = prefs[Keys.COMPLETED_CHECKLIST_ITEM_IDS] ?: emptySet()
            prefs[Keys.COMPLETED_CHECKLIST_ITEM_IDS] =
                if (completed) current + key else current - key
        }
    }

    private object Keys {
        val FAVORITE_ARTICLE_IDS = stringSetPreferencesKey("favorite_article_ids")
        val RECENT_ARTICLE_IDS = stringPreferencesKey("recent_article_ids")
        val COMPLETED_CHECKLIST_ITEM_IDS = stringSetPreferencesKey("completed_checklist_item_ids")
    }

    private companion object {
        const val RECENT_SEPARATOR = "|"
        const val MAX_RECENT_ARTICLES = 20
    }
}
