package org.muslim.app.feature.learn.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.learn.domain.WillDraft

class WillDraftStorageCodecTest {
    @Test
    fun `round trip preserves every structured draft field including newlines`() {
        val original = WillDraft(
            fullName = "أحمد محمد",
            documentLocation = "الخزنة\nالرف العلوي",
            executorName = "خالد علي",
            executorContact = "+49 123 456",
            trustedContacts = "سارة\nليلى",
            debtsAndRights = "دين موثق\nحق مستحق",
            assetsAndAccounts = "حساب مصرفي ووثائق عقار",
            entrustedProperty = "أمانة لصديق",
            digitalAccessInstructions = "خطة الوصول عند كاتب العدل",
            funeralWishes = "التواصل مع المسجد",
            guardianshipNotes = "ملاحظات الأسرة",
            charitableBequests = "جهة خيرية مقترحة",
            lastReviewDate = "2026-09-23",
            additionalNotes = "ملاحظة ختامية",
        )

        val decoded = WillDraftStorageCodec.decode(
            WillDraftStorageCodec.encode(original),
        )

        assertThat(decoded).isEqualTo(original)
    }

    @Test
    fun `unsupported or corrupt payload is rejected`() {
        assertThat(WillDraftStorageCodec.decode("not-a-valid-payload")).isNull()
    }
}
