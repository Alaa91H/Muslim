package org.muslim.app.feature.learn.data

import org.muslim.app.feature.learn.domain.WillDraft
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Stable, versioned encoding for the encrypted will-draft payload.
 *
 * Each value is Base64-encoded before joining so user-entered newlines and
 * separators cannot corrupt the stored structure. Encryption is applied by
 * [WillDraftCrypto] after this encoding step.
 */
internal object WillDraftStorageCodec {
    private const val VERSION = "1"
    private const val FIELD_COUNT = 14

    fun encode(draft: WillDraft): String = buildList {
        add(VERSION)
        add(draft.fullName)
        add(draft.documentLocation)
        add(draft.executorName)
        add(draft.executorContact)
        add(draft.trustedContacts)
        add(draft.debtsAndRights)
        add(draft.assetsAndAccounts)
        add(draft.entrustedProperty)
        add(draft.digitalAccessInstructions)
        add(draft.funeralWishes)
        add(draft.guardianshipNotes)
        add(draft.charitableBequests)
        add(draft.lastReviewDate)
        add(draft.additionalNotes)
    }.joinToString(separator = "\n", transform = ::encodeValue)

    fun decode(payload: String): WillDraft? {
        val values = payload
            .split('\n')
            .mapNotNull(::decodeValue)

        if (values.size != FIELD_COUNT + 1 || values.first() != VERSION) {
            return null
        }

        return WillDraft(
            fullName = values[1],
            documentLocation = values[2],
            executorName = values[3],
            executorContact = values[4],
            trustedContacts = values[5],
            debtsAndRights = values[6],
            assetsAndAccounts = values[7],
            entrustedProperty = values[8],
            digitalAccessInstructions = values[9],
            funeralWishes = values[10],
            guardianshipNotes = values[11],
            charitableBequests = values[12],
            lastReviewDate = values[13],
            additionalNotes = values[14],
        )
    }

    private fun encodeValue(value: String): String =
        Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    private fun decodeValue(value: String): String? = runCatching {
        String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8)
    }.getOrNull()
}
