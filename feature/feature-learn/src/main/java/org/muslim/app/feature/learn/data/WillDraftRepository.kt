package org.muslim.app.feature.learn.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.muslim.app.feature.learn.domain.WillDraft
import javax.inject.Inject
import javax.inject.Singleton

private val Context.willDraftDataStore by preferencesDataStore(name = "will_draft_prefs")

data class WillDraftStorageState(
    val draft: WillDraft = WillDraft(),
    val encryptionError: Boolean = false,
)

/**
 * Persists the user's private will draft on device.
 *
 * New saves are encoded as one versioned payload and encrypted with an
 * Android-Keystore-backed AES key. Existing plaintext preference keys remain
 * readable only long enough to migrate them, then they are removed.
 */
@Singleton
class WillDraftRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crypto: WillDraftCrypto,
) {
    val storageState: Flow<WillDraftStorageState> =
        context.willDraftDataStore.data.map(::readStorageState)

    val draft: Flow<WillDraft> = storageState.map { it.draft }

    /**
     * Converts an existing plaintext draft to encrypted storage in place.
     * This is idempotent and never overwrites an existing encrypted payload.
     */
    suspend fun migrateLegacyIfNeeded() {
        val preferences = context.willDraftDataStore.data.first()
        if (preferences[Keys.ENCRYPTED_PAYLOAD] != null) {
            return
        }

        val legacyDraft = readLegacyDraft(preferences)
        if (legacyDraft.isEmpty()) {
            return
        }

        val encrypted = encryptDraft(legacyDraft)
        context.willDraftDataStore.edit { mutablePreferences ->
            mutablePreferences[Keys.ENCRYPTED_PAYLOAD] = encrypted
            removeLegacyKeys(mutablePreferences)
        }
    }

    suspend fun save(draft: WillDraft) {
        val normalized = draft.normalized()
        val encrypted = encryptDraft(normalized)

        context.willDraftDataStore.edit { preferences ->
            preferences[Keys.ENCRYPTED_PAYLOAD] = encrypted
            removeLegacyKeys(preferences)
        }
    }

    suspend fun clear() {
        context.willDraftDataStore.edit { it.clear() }
    }

    private fun readStorageState(preferences: Preferences): WillDraftStorageState {
        val encryptedPayload = preferences[Keys.ENCRYPTED_PAYLOAD]
            ?: return WillDraftStorageState(draft = readLegacyDraft(preferences))

        val decodedDraft = crypto
            .decrypt(encryptedPayload)
            ?.let(WillDraftStorageCodec::decode)

        return if (decodedDraft != null) {
            WillDraftStorageState(draft = decodedDraft)
        } else {
            WillDraftStorageState(encryptionError = true)
        }
    }

    private fun encryptDraft(draft: WillDraft): String =
        crypto.encrypt(WillDraftStorageCodec.encode(draft))

    private fun readLegacyDraft(preferences: Preferences): WillDraft = WillDraft(
        fullName = preferences[Keys.FULL_NAME].orEmpty(),
        documentLocation = preferences[Keys.DOCUMENT_LOCATION].orEmpty(),
        executorName = preferences[Keys.EXECUTOR_NAME].orEmpty(),
        executorContact = preferences[Keys.EXECUTOR_CONTACT].orEmpty(),
        trustedContacts = preferences[Keys.TRUSTED_CONTACTS].orEmpty(),
        debtsAndRights = preferences[Keys.DEBTS_AND_RIGHTS].orEmpty(),
        assetsAndAccounts = preferences[Keys.ASSETS_AND_ACCOUNTS].orEmpty(),
        entrustedProperty = preferences[Keys.ENTRUSTED_PROPERTY].orEmpty(),
        digitalAccessInstructions = preferences[Keys.DIGITAL_ACCESS_INSTRUCTIONS].orEmpty(),
        funeralWishes = preferences[Keys.FUNERAL_WISHES].orEmpty(),
        guardianshipNotes = preferences[Keys.GUARDIANSHIP_NOTES].orEmpty(),
        charitableBequests = preferences[Keys.CHARITABLE_BEQUESTS].orEmpty(),
        lastReviewDate = preferences[Keys.LAST_REVIEW_DATE].orEmpty(),
        additionalNotes = preferences[Keys.ADDITIONAL_NOTES].orEmpty(),
    )

    private fun removeLegacyKeys(preferences: androidx.datastore.preferences.core.MutablePreferences) {
        preferences.remove(Keys.FULL_NAME)
        preferences.remove(Keys.DOCUMENT_LOCATION)
        preferences.remove(Keys.EXECUTOR_NAME)
        preferences.remove(Keys.EXECUTOR_CONTACT)
        preferences.remove(Keys.TRUSTED_CONTACTS)
        preferences.remove(Keys.DEBTS_AND_RIGHTS)
        preferences.remove(Keys.ASSETS_AND_ACCOUNTS)
        preferences.remove(Keys.ENTRUSTED_PROPERTY)
        preferences.remove(Keys.DIGITAL_ACCESS_INSTRUCTIONS)
        preferences.remove(Keys.FUNERAL_WISHES)
        preferences.remove(Keys.GUARDIANSHIP_NOTES)
        preferences.remove(Keys.CHARITABLE_BEQUESTS)
        preferences.remove(Keys.LAST_REVIEW_DATE)
        preferences.remove(Keys.ADDITIONAL_NOTES)
    }

    private fun WillDraft.normalized(): WillDraft = copy(
        fullName = fullName.trim(),
        documentLocation = documentLocation.trim(),
        executorName = executorName.trim(),
        executorContact = executorContact.trim(),
        trustedContacts = trustedContacts.trim(),
        debtsAndRights = debtsAndRights.trim(),
        assetsAndAccounts = assetsAndAccounts.trim(),
        entrustedProperty = entrustedProperty.trim(),
        digitalAccessInstructions = digitalAccessInstructions.trim(),
        funeralWishes = funeralWishes.trim(),
        guardianshipNotes = guardianshipNotes.trim(),
        charitableBequests = charitableBequests.trim(),
        lastReviewDate = lastReviewDate.trim(),
        additionalNotes = additionalNotes.trim(),
    )

    private object Keys {
        val ENCRYPTED_PAYLOAD = stringPreferencesKey("will_encrypted_payload_v1")

        // Legacy plaintext keys retained for one-way migration only.
        val FULL_NAME = stringPreferencesKey("will_full_name")
        val DOCUMENT_LOCATION = stringPreferencesKey("will_document_location")
        val EXECUTOR_NAME = stringPreferencesKey("will_executor_name")
        val EXECUTOR_CONTACT = stringPreferencesKey("will_executor_contact")
        val TRUSTED_CONTACTS = stringPreferencesKey("will_trusted_contacts")
        val DEBTS_AND_RIGHTS = stringPreferencesKey("will_debts_and_rights")
        val ASSETS_AND_ACCOUNTS = stringPreferencesKey("will_assets_and_accounts")
        val ENTRUSTED_PROPERTY = stringPreferencesKey("will_entrusted_property")
        val DIGITAL_ACCESS_INSTRUCTIONS = stringPreferencesKey("will_digital_access_instructions")
        val FUNERAL_WISHES = stringPreferencesKey("will_funeral_wishes")
        val GUARDIANSHIP_NOTES = stringPreferencesKey("will_guardianship_notes")
        val CHARITABLE_BEQUESTS = stringPreferencesKey("will_charitable_bequests")
        val LAST_REVIEW_DATE = stringPreferencesKey("will_last_review_date")
        val ADDITIONAL_NOTES = stringPreferencesKey("will_additional_notes")
    }
}
