package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.learn.domain.WillDraft
import javax.inject.Inject
import javax.inject.Singleton

private val Context.willDraftDataStore by preferencesDataStore(name = "will_draft_prefs")

/**
 * Persists the user's will draft on the device only. The app never uploads or
 * shares this text unless the user explicitly taps the share action.
 */
@Singleton
class WillDraftRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val draft: Flow<WillDraft> = context.willDraftDataStore.data.map { preferences ->
        WillDraft(
            fullName = preferences[Keys.FULL_NAME].orEmpty(),
            executorName = preferences[Keys.EXECUTOR_NAME].orEmpty(),
            executorContact = preferences[Keys.EXECUTOR_CONTACT].orEmpty(),
            debtsAndRights = preferences[Keys.DEBTS_AND_RIGHTS].orEmpty(),
            funeralWishes = preferences[Keys.FUNERAL_WISHES].orEmpty(),
            guardianshipNotes = preferences[Keys.GUARDIANSHIP_NOTES].orEmpty(),
            charitableBequests = preferences[Keys.CHARITABLE_BEQUESTS].orEmpty(),
            additionalNotes = preferences[Keys.ADDITIONAL_NOTES].orEmpty(),
        )
    }

    suspend fun save(draft: WillDraft) {
        context.willDraftDataStore.edit { preferences ->
            preferences[Keys.FULL_NAME] = draft.fullName.trim()
            preferences[Keys.EXECUTOR_NAME] = draft.executorName.trim()
            preferences[Keys.EXECUTOR_CONTACT] = draft.executorContact.trim()
            preferences[Keys.DEBTS_AND_RIGHTS] = draft.debtsAndRights.trim()
            preferences[Keys.FUNERAL_WISHES] = draft.funeralWishes.trim()
            preferences[Keys.GUARDIANSHIP_NOTES] = draft.guardianshipNotes.trim()
            preferences[Keys.CHARITABLE_BEQUESTS] = draft.charitableBequests.trim()
            preferences[Keys.ADDITIONAL_NOTES] = draft.additionalNotes.trim()
        }
    }

    suspend fun clear() {
        context.willDraftDataStore.edit { it.clear() }
    }

    private object Keys {
        val FULL_NAME = stringPreferencesKey("will_full_name")
        val EXECUTOR_NAME = stringPreferencesKey("will_executor_name")
        val EXECUTOR_CONTACT = stringPreferencesKey("will_executor_contact")
        val DEBTS_AND_RIGHTS = stringPreferencesKey("will_debts_and_rights")
        val FUNERAL_WISHES = stringPreferencesKey("will_funeral_wishes")
        val GUARDIANSHIP_NOTES = stringPreferencesKey("will_guardianship_notes")
        val CHARITABLE_BEQUESTS = stringPreferencesKey("will_charitable_bequests")
        val ADDITIONAL_NOTES = stringPreferencesKey("will_additional_notes")
    }
}
