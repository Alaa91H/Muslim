package org.muslim.app.feature.learn.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.muslim.app.feature.learn.domain.WillDraft
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
class WillDraftPdfInstrumentedTest {
    @Test
    fun arabicPdfIsGeneratedForLongStructuredDraft() {
        val output = ByteArrayOutputStream()
        val draft = WillDraft(
            fullName = "أحمد محمد",
            debtsAndRights = "دين موثق ومستحقات مالية",
            funeralWishes = "التواصل مع المسجد والأسرة.",
            additionalNotes = buildString {
                repeat(180) {
                    append("ملاحظة تنظيمية طويلة لاختبار التفاف النص وتقسيم الصفحات. ")
                }
            },
        )

        writeWillDraftPdf(
            output = output,
            document = draft.toDocument(isArabic = true),
            isArabic = true,
        )

        assertValidPdf(output.toByteArray())
    }

    @Test
    fun englishPdfIsGeneratedForStructuredDraft() {
        val output = ByteArrayOutputStream()
        val draft = WillDraft(
            fullName = "Amina Ahmad",
            assetsAndAccounts = "Property and account document locations.",
            trustedContacts = "Sara and Yusuf",
            additionalNotes = "Review this copy with qualified advisers.",
        )

        writeWillDraftPdf(
            output = output,
            document = draft.toDocument(isArabic = false),
            isArabic = false,
        )

        assertValidPdf(output.toByteArray())
    }

    private fun assertValidPdf(bytes: ByteArray) {
        assertThat(bytes.size).isGreaterThan(2_000)
        val prefix = String(
            bytes.copyOfRange(0, 4),
            StandardCharsets.US_ASCII,
        )
        assertThat(prefix).isEqualTo("%PDF")
    }
}
