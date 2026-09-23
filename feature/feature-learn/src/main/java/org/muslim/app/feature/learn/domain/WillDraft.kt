package org.muslim.app.feature.learn.domain

/**
 * A private, on-device draft for organising a Muslim's will-related notes.
 *
 * This model is intentionally not a legal instrument. It helps the user record
 * personal information and then share a copy through Android's system chooser.
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

    /** A readable plain-text copy for the user's intentional, system-mediated sharing. */
    fun toShareText(isArabic: Boolean): String = if (isArabic) {
        buildString {
            appendLine("مسودة وصية شرعية")
            appendLine("للاستخدام الشخصي والتنظيمي — راجع عالمًا موثوقًا ومحاميًا/كاتب عدل قبل اعتمادها.")
            appendLine()

            appendSection("البيانات والوصول")
            appendField("الاسم الكامل", fullName)
            appendField("مكان حفظ الوثائق المهمة", documentLocation)
            appendField("المنفذ أو الشخص الموثوق", executorName)
            appendField("وسيلة تواصل المنفذ", executorContact)
            appendField("جهات اتصال موثوقة", trustedContacts)

            appendSection("الحقوق والأموال والأمانات")
            appendField("الديون والحقوق والالتزامات", debtsAndRights)
            appendField("الأصول والحسابات ومكان مستنداتها", assetsAndAccounts)
            appendField("الأمانات وممتلكات الغير", entrustedProperty)
            appendField("تعليمات الوصول الرقمي الآمن", digitalAccessInstructions)

            appendSection("الأسرة والجنازة والوصايا")
            appendField("وصايا التجهيز والجنازة", funeralWishes)
            appendField("ملاحظات الوصاية على القُصَّر", guardianshipNotes)
            appendField("الوصايا الخيرية", charitableBequests)

            appendSection("المراجعة والملاحظات")
            appendField("تاريخ آخر مراجعة", lastReviewDate)
            appendField("ملاحظات إضافية", additionalNotes)

            appendLine("تنبيه للخصوصية: لا تُدرج كلمات مرور أو رموز PIN أو عبارات استرداد أو مفاتيح خاصة داخل هذه المسودة.")
            appendLine("أُنشئت هذه النسخة للمشاركة باختيار صاحبها. تحقّق من الجهة المستلمة ووسيلة الإرسال قبل المشاركة.")
        }
    } else {
        buildString {
            appendLine("Islamic Will Draft")
            appendLine("For personal and organisational use — consult a qualified scholar and a lawyer/notary before relying on it.")
            appendLine()

            appendSection("Identity and access")
            appendField("Full name", fullName)
            appendField("Important document location", documentLocation)
            appendField("Executor or trusted person", executorName)
            appendField("Executor contact", executorContact)
            appendField("Trusted contacts", trustedContacts)

            appendSection("Rights, finances, and entrusted property")
            appendField("Debts, rights, and obligations", debtsAndRights)
            appendField("Assets, accounts, and document locations", assetsAndAccounts)
            appendField("Entrusted or third-party property", entrustedProperty)
            appendField("Secure digital-access instructions", digitalAccessInstructions)

            appendSection("Family, funeral, and bequests")
            appendField("Funeral wishes", funeralWishes)
            appendField("Guardianship notes for minors", guardianshipNotes)
            appendField("Charitable bequests", charitableBequests)

            appendSection("Review and notes")
            appendField("Last review date", lastReviewDate)
            appendField("Additional notes", additionalNotes)

            appendLine("Privacy reminder: do not include passwords, PINs, recovery phrases, or private keys in this draft.")
            appendLine("This copy was created for its owner's deliberate sharing. Verify the recipient and sharing method before sending.")
        }
    }

    private fun StringBuilder.appendSection(title: String) {
        appendLine("— $title —")
    }

    private fun StringBuilder.appendField(label: String, value: String) {
        if (value.isNotBlank()) {
            appendLine("$label:")
            appendLine(value.trim())
            appendLine()
        }
    }
}
