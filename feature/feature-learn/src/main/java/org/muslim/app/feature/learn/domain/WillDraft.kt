package org.muslim.app.feature.learn.domain

/**
 * A private, on-device draft for organising a Muslim's will-related notes.
 *
 * This model is intentionally not a legal instrument. It helps the user record
 * personal information and then export or share a copy through Android system
 * surfaces chosen by the user.
 *
 * Sensitive secrets such as passwords, recovery phrases, PINs, or private keys
 * should never be stored in this draft. [digitalAccessInstructions] is only for
 * recording where trusted people can find a separate secure access plan.
 */
data class WillDraft(
    val fullName: String = "",
    val documentLocation: String = "",
    val executorName: String = "",
    val executorContact: String = "",
    val trustedContacts: String = "",
    val debtsAndRights: String = "",
    val assetsAndAccounts: String = "",
    val entrustedProperty: String = "",
    val digitalAccessInstructions: String = "",
    val funeralWishes: String = "",
    val guardianshipNotes: String = "",
    val charitableBequests: String = "",
    val lastReviewDate: String = "",
    val additionalNotes: String = "",
) {
    fun isEmpty(): Boolean = allValues().all(String::isBlank)

    fun toDocument(isArabic: Boolean): WillDraftDocument =
        if (isArabic) arabicDocument() else englishDocument()

    private fun arabicDocument(): WillDraftDocument = WillDraftDocument(
        title = "مسودة وصية شرعية",
        subtitle = "نسخة شخصية تنظيمية — راجع عالمًا موثوقًا ومحاميًا/كاتب عدل قبل اعتمادها.",
        sections = listOf(
            section(
                title = "البيانات والوصول",
                "الاسم الكامل" to fullName,
                "مكان حفظ الوثائق المهمة" to documentLocation,
                "المنفذ أو الشخص الموثوق" to executorName,
                "وسيلة تواصل المنفذ" to executorContact,
                "جهات اتصال موثوقة" to trustedContacts,
            ),
            section(
                title = "الحقوق والأموال والأمانات",
                "الديون والحقوق والالتزامات" to debtsAndRights,
                "الأصول والحسابات ومكان مستنداتها" to assetsAndAccounts,
                "الأمانات وممتلكات الغير" to entrustedProperty,
                "تعليمات الوصول الرقمي الآمن" to digitalAccessInstructions,
            ),
            section(
                title = "الأسرة والجنازة والوصايا",
                "وصايا التجهيز والجنازة" to funeralWishes,
                "ملاحظات الوصاية على القُصَّر" to guardianshipNotes,
                "الوصايا الخيرية" to charitableBequests,
            ),
            section(
                title = "المراجعة والملاحظات",
                "تاريخ آخر مراجعة" to lastReviewDate,
                "ملاحظات إضافية" to additionalNotes,
            ),
        ),
        privacyReminder = "تنبيه للخصوصية: لا تُدرج كلمات مرور أو رموز PIN أو عبارات استرداد أو مفاتيح خاصة داخل هذه المسودة.",
        sharingReminder = "تحقّق من الجهة المستلمة ووسيلة الإرسال قبل مشاركة أي نسخة.",
    )

    private fun englishDocument(): WillDraftDocument = WillDraftDocument(
        title = "Islamic Will Draft",
        subtitle = "Personal organisational copy — consult a qualified scholar and a lawyer/notary before relying on it.",
        sections = listOf(
            section(
                title = "Identity and access",
                "Full name" to fullName,
                "Important document location" to documentLocation,
                "Executor or trusted person" to executorName,
                "Executor contact" to executorContact,
                "Trusted contacts" to trustedContacts,
            ),
            section(
                title = "Rights, finances, and entrusted property",
                "Debts, rights, and obligations" to debtsAndRights,
                "Assets, accounts, and document locations" to assetsAndAccounts,
                "Entrusted or third-party property" to entrustedProperty,
                "Secure digital-access instructions" to digitalAccessInstructions,
            ),
            section(
                title = "Family, funeral, and bequests",
                "Funeral wishes" to funeralWishes,
                "Guardianship notes for minors" to guardianshipNotes,
                "Charitable bequests" to charitableBequests,
            ),
            section(
                title = "Review and notes",
                "Last review date" to lastReviewDate,
                "Additional notes" to additionalNotes,
            ),
        ),
        privacyReminder = "Privacy reminder: do not include passwords, PINs, recovery phrases, or private keys in this draft.",
        sharingReminder = "Verify the recipient and sharing method before sending any copy.",
    )

    /** A readable plain-text copy for the user's intentional, system-mediated sharing. */
    fun toShareText(isArabic: Boolean): String {
        val document = toDocument(isArabic)
        return buildString {
            appendLine(document.title)
            appendLine(document.subtitle)
            appendLine()
            document.sections.forEach { section ->
                if (section.fields.isNotEmpty()) {
                    appendLine("— ${section.title} —")
                    section.fields.forEach { field ->
                        appendLine("${field.label}:")
                        appendLine(field.value)
                        appendLine()
                    }
                }
            }
            appendLine(document.privacyReminder)
            appendLine(document.sharingReminder)
        }
    }

    private fun allValues(): List<String> = listOf(
        fullName,
        documentLocation,
        executorName,
        executorContact,
        trustedContacts,
        debtsAndRights,
        assetsAndAccounts,
        entrustedProperty,
        digitalAccessInstructions,
        funeralWishes,
        guardianshipNotes,
        charitableBequests,
        lastReviewDate,
        additionalNotes,
    )

    private fun section(
        title: String,
        vararg fields: Pair<String, String>,
    ): WillDraftDocumentSection = WillDraftDocumentSection(
        title = title,
        fields = fields
            .filter { (_, value) -> value.isNotBlank() }
            .map { (label, value) ->
                WillDraftDocumentField(
                    label = label,
                    value = value.trim(),
                )
            },
    )
}

data class WillDraftDocument(
    val title: String,
    val subtitle: String,
    val sections: List<WillDraftDocumentSection>,
    val privacyReminder: String,
    val sharingReminder: String,
)

data class WillDraftDocumentSection(
    val title: String,
    val fields: List<WillDraftDocumentField>,
)

data class WillDraftDocumentField(
    val label: String,
    val value: String,
)
