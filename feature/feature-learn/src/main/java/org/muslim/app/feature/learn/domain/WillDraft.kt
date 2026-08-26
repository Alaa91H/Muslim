package org.muslim.app.feature.learn.domain

/**
 * A private, on-device draft for organising a Muslim's will-related notes.
 *
 * This model is intentionally not a legal instrument. It helps the user record
 * personal information and then share a copy through Android's system chooser.
 */
data class WillDraft(
    val fullName: String = "",
    val executorName: String = "",
    val executorContact: String = "",
    val debtsAndRights: String = "",
    val funeralWishes: String = "",
    val guardianshipNotes: String = "",
    val charitableBequests: String = "",
    val additionalNotes: String = "",
) {
    fun isEmpty(): Boolean = listOf(
        fullName,
        executorName,
        executorContact,
        debtsAndRights,
        funeralWishes,
        guardianshipNotes,
        charitableBequests,
        additionalNotes,
    ).all(String::isBlank)

    /** A readable plain-text copy for the user's intentional, system-mediated sharing. */
    fun toShareText(isArabic: Boolean): String = if (isArabic) {
        buildString {
            appendLine("مسودة وصية شرعية")
            appendLine("للاستخدام الشخصي والتعليمي — راجع عالمًا موثوقًا ومحاميًا/كاتب عدل قبل اعتمادها.")
            appendLine()
            appendField("الاسم الكامل", fullName)
            appendField("المنفذ أو الشخص الموثوق", executorName)
            appendField("وسيلة تواصل المنفذ", executorContact)
            appendField("الديون والحقوق والالتزامات", debtsAndRights)
            appendField("وصايا التجهيز والجنازة", funeralWishes)
            appendField("ملاحظات الوصاية على القُصَّر", guardianshipNotes)
            appendField("الوصايا الخيرية", charitableBequests)
            appendField("ملاحظات إضافية", additionalNotes)
            appendLine("تنبيه للخصوصية: أُنشئت هذه النسخة للمشاركة باختيار صاحبها. تحقّق من الجهة المستلمة قبل الإرسال.")
        }
    } else {
        buildString {
            appendLine("Islamic Will Draft")
            appendLine("For personal and educational use — consult a qualified scholar and a lawyer/notary before relying on it.")
            appendLine()
            appendField("Full name", fullName)
            appendField("Executor or trusted person", executorName)
            appendField("Executor contact", executorContact)
            appendField("Debts, rights, and obligations", debtsAndRights)
            appendField("Funeral wishes", funeralWishes)
            appendField("Guardianship notes for minors", guardianshipNotes)
            appendField("Charitable bequests", charitableBequests)
            appendField("Additional notes", additionalNotes)
            appendLine("Privacy reminder: this copy was created for its owner's deliberate sharing. Verify the recipient before sending.")
        }
    }

    private fun StringBuilder.appendField(label: String, value: String) {
        if (value.isNotBlank()) {
            appendLine("$label:")
            appendLine(value.trim())
            appendLine()
        }
    }
}
