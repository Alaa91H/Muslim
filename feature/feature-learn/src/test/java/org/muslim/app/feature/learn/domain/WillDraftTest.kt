package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WillDraftTest {
    @Test
    fun `empty draft is recognised across expanded fields`() {
        assertThat(WillDraft().isEmpty()).isTrue()
        assertThat(WillDraft(fullName = "أحمد").isEmpty()).isFalse()
        assertThat(WillDraft(entrustedProperty = "أمانة لصديق").isEmpty()).isFalse()
        assertThat(WillDraft(lastReviewDate = "2026-09-23").isEmpty()).isFalse()
    }

    @Test
    fun `arabic share text groups supplied fields and privacy reminder`() {
        val draft = WillDraft(
            fullName = "أحمد محمد",
            documentLocation = "الخزنة المنزلية",
            executorName = "خالد علي",
            debtsAndRights = "دين موثق",
            entrustedProperty = "أمانة يجب ردها",
            lastReviewDate = "2026-09-23",
        )

        val text = draft.toShareText(isArabic = true)

        assertThat(text).contains("مسودة وصية شرعية")
        assertThat(text).contains("— البيانات والوصول —")
        assertThat(text).contains("أحمد محمد")
        assertThat(text).contains("الخزنة المنزلية")
        assertThat(text).contains("خالد علي")
        assertThat(text).contains("دين موثق")
        assertThat(text).contains("أمانة يجب ردها")
        assertThat(text).contains("2026-09-23")
        assertThat(text).contains("تحقّق من الجهة المستلمة")
        assertThat(text).doesNotContain("ملاحظات إضافية:")
    }

    @Test
    fun `english share text includes expanded structured fields`() {
        val text = WillDraft(
            fullName = "Amina Ahmad",
            trustedContacts = "Family contact: Sara",
            assetsAndAccounts = "Bank documents are in the home file.",
            digitalAccessInstructions = "Access plan is stored with the notary.",
            funeralWishes = "Contact the local mosque.",
        ).toShareText(isArabic = false)

        assertThat(text).contains("Islamic Will Draft")
        assertThat(text).contains("Identity and access")
        assertThat(text).contains("Rights, finances, and entrusted property")
        assertThat(text).contains("Full name:")
        assertThat(text).contains("Amina Ahmad")
        assertThat(text).contains("Family contact: Sara")
        assertThat(text).contains("Bank documents are in the home file.")
        assertThat(text).contains("Access plan is stored with the notary.")
        assertThat(text).contains("Contact the local mosque.")
        assertThat(text).contains("Verify the recipient")
    }

    @Test
    fun `blank expanded fields are omitted from shared copy`() {
        val text = WillDraft(fullName = "Amina Ahmad").toShareText(isArabic = false)

        assertThat(text).contains("Full name:")
        assertThat(text).doesNotContain("Trusted contacts:")
        assertThat(text).doesNotContain("Entrusted or third-party property:")
        assertThat(text).doesNotContain("Last review date:")
    }
}
