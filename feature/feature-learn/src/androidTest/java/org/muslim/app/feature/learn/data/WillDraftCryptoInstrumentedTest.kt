package org.muslim.app.feature.learn.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WillDraftCryptoInstrumentedTest {
    @Test
    fun encryptedPayloadRoundTripsThroughAndroidKeystore() {
        val crypto = WillDraftCrypto()
        val original = "وصية خاصة\nPrivate will draft\n123"

        val encrypted = crypto.encrypt(original)
        val decrypted = crypto.decrypt(encrypted)

        assertThat(encrypted).isNotEqualTo(original)
        assertThat(encrypted).startsWith("v1:")
        assertThat(decrypted).isEqualTo(original)
    }

    @Test
    fun malformedPayloadIsRejectedWithoutThrowing() {
        val crypto = WillDraftCrypto()

        assertThat(crypto.decrypt("not-valid")).isNull()
    }
}
