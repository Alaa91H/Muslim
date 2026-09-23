package org.muslim.app.feature.reference.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReferenceReaderPreferencesTest {

    @Test
    fun topicKey_isStableAcrossReaderFeatures() {
        assertThat(ReferenceReaderKeyCodec.topicKey("islam", "tawhid"))
            .isEqualTo("islam/tawhid")
    }

    @Test
    fun location_roundTrips() {
        val location = ReferenceReaderLocation(
            bookId = "history_civilization",
            topicId = "baghdad_foundation",
            scrollIndex = 7,
        )

        assertThat(
            ReferenceReaderKeyCodec.decodeLocation(
                ReferenceReaderKeyCodec.encodeLocation(location),
            ),
        ).isEqualTo(location)
    }

    @Test
    fun malformedLocation_isRejected() {
        assertThat(ReferenceReaderKeyCodec.decodeLocation("islam|tawhid")).isNull()
        assertThat(ReferenceReaderKeyCodec.decodeLocation("||4")).isNull()
        assertThat(ReferenceReaderKeyCodec.decodeLocation("islam|tawhid|not-a-number")).isNull()
    }

    @Test
    fun negativeLocationIndex_isClamped() {
        assertThat(ReferenceReaderKeyCodec.decodeLocation("islam|tawhid|-5"))
            .isEqualTo(ReferenceReaderLocation("islam", "tawhid", 0))
    }

    @Test
    fun fontStep_isClampedToSupportedRange() {
        assertThat(ReferenceReaderKeyCodec.clampFontStep(-10)).isEqualTo(0)
        assertThat(ReferenceReaderKeyCodec.clampFontStep(2)).isEqualTo(2)
        assertThat(ReferenceReaderKeyCodec.clampFontStep(99)).isEqualTo(3)
    }
}
