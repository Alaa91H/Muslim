package org.muslim.app.core.cast

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ByteRangeTest {
    @Test fun `parses bounded and open ended ranges`() {
        assertThat(ByteRange.parse("bytes=5-9", 20)).isEqualTo(ByteRange(5, 9, 20))
        assertThat(ByteRange.parse("bytes=10-", 20)).isEqualTo(ByteRange(10, 19, 20))
    }

    @Test fun `parses suffix ranges and clamps oversized suffix`() {
        assertThat(ByteRange.parse("bytes=-4", 20)).isEqualTo(ByteRange(16, 19, 20))
        assertThat(ByteRange.parse("Bytes=-40", 20)).isEqualTo(ByteRange(0, 19, 20))
    }

    @Test fun `rejects invalid and unsatisfiable ranges`() {
        assertThat(ByteRange.parse("bytes=20-", 20)).isNull()
        assertThat(ByteRange.parse("bytes=8-3", 20)).isNull()
        assertThat(ByteRange.parse("bytes=1-2,4-5", 20)).isNull()
    }
}
