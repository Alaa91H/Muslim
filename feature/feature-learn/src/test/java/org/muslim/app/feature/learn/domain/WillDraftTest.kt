package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WillDraftTest {
    @Test
    fun `empty draft is recognised`() {
        assertThat(WillDraft().isEmpty()).isTrue()
        assertThat(WillDraft(fullName = "أحمد").isEmpty()).isFalse()
    }

    @Test
    fun `arabic share text includes supplied fields and privacy reminder`() {
        val draft = WillDraft(
            fullName = "أحمد محمد",
            executorName = "خالد علي",
            debtsAndRights = "دين موثق",
        )

        val text = draft.toShareText(isArabic = true)

        assertThat(text).contains("مسودة وصية شرعية")
        assertThat(text).contains("أحمد محمد")
        assertThat(text).contains("خالد علي")
        assertThat(text).contains("دين موثق")
        assertThat(text).contains("تحقّق من الجهة المستلمة")
        assertThat(text).doesNotContain("ملاحظات إضافية:")
    }

    @Test
    fun `english share text includes supplied fields`() {
        val text = WillDraft(
            fullName = "Amina Ahmad",
            funeralWishes = "Contact the local mosque.",
        ).toShareText(isArabic = false)

        assertThat(text).contains("Islamic Will Draft")
        assertThat(text).contains("Full name:")
        assertThat(text).contains("Amina Ahmad")
        assertThat(text).contains("Contact the local mosque.")
        assertThat(text).contains("Verify the recipient")
    }
}
