package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val FUNERAL_WILL_INTRO_CONTENT_VERSION = 1
private val Context.funeralWillUiDataStore by preferencesDataStore(name = "funeral_will_ui_prefs")

data class FuneralWillIntroVisibility(
    val draftIntroVisible: Boolean = true,
    val legalNoticeVisible: Boolean = true,
    val privacyNoticeVisible: Boolean = true,
) {
    val hasDismissedCards: Boolean
        get() = !draftIntroVisible || !legalNoticeVisible || !privacyNoticeVisible
}

/**
 * Stores only UI acknowledgement state for the funerals and Islamic-will screen.
 *
 * The acknowledgement flags are deliberately separated from the private will draft,
 * so clearing or restoring these cards never changes the user's will text.
 */
@Singleton
class FuneralWillPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val introVisibility: Flow<FuneralWillIntroVisibility> =
        context.funeralWillUiDataStore.data.map { preferences ->
            val storedVersion = preferences[Keys.CONTENT_VERSION]
            if (storedVersion != null && storedVersion != FUNERAL_WILL_INTRO_CONTENT_VERSION) {
                FuneralWillIntroVisibility()
            } else {
                FuneralWillIntroVisibility(
                    draftIntroVisible = !(preferences[Keys.HIDE_DRAFT_INTRO] ?: false),
                    legalNoticeVisible = !(preferences[Keys.HIDE_LEGAL_NOTICE] ?: false),
                    privacyNoticeVisible = !(preferences[Keys.HIDE_PRIVACY_NOTICE] ?: false),
                )
            }
        }

    suspend fun dismissDraftIntro() = updateHiddenFlag(Keys.HIDE_DRAFT_INTRO, hidden = true)

    suspend fun dismissLegalNotice() = updateHiddenFlag(Keys.HIDE_LEGAL_NOTICE, hidden = true)

    suspend fun dismissPrivacyNotice() = updateHiddenFlag(Keys.HIDE_PRIVACY_NOTICE, hidden = true)

    suspend fun restoreIntroCards() {
        context.funeralWillUiDataStore.edit { preferences ->
            preferences[Keys.CONTENT_VERSION] = FUNERAL_WILL_INTRO_CONTENT_VERSION
            preferences[Keys.HIDE_DRAFT_INTRO] = false
            preferences[Keys.HIDE_LEGAL_NOTICE] = false
            preferences[Keys.HIDE_PRIVACY_NOTICE] = false
        }
    }

    private suspend fun updateHiddenFlag(
        key: androidx.datastore.preferences.core.Preferences.Key<Boolean>,
        hidden: Boolean,
    ) {
        context.funeralWillUiDataStore.edit { preferences ->
            preferences[Keys.CONTENT_VERSION] = FUNERAL_WILL_INTRO_CONTENT_VERSION
            preferences[key] = hidden
        }
    }

    private object Keys {
        val CONTENT_VERSION = intPreferencesKey("intro_content_version")
        val HIDE_DRAFT_INTRO = booleanPreferencesKey("hide_draft_intro")
        val HIDE_LEGAL_NOTICE = booleanPreferencesKey("hide_legal_notice")
        val HIDE_PRIVACY_NOTICE = booleanPreferencesKey("hide_privacy_notice")
    }
}
