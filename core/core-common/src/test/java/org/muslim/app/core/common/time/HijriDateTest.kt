package org.muslim.app.core.common.time

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Anchors are published Umm al-Qura dates (Saudi authorities):
 *  - 1 Muharram 1446        = 7 July 2024
 *  - 10 Dhul-Hijjah 1445    = 16 June 2024 (Eid al-Adha)
 *  - 1 Ramadan 1446         = 1 March 2025
 *  - 1 Shawwal 1446         = 30 March 2025 (Eid al-Fitr)
 *  - 1 Muharram 1447        = 26 June 2025
 */
class HijriDateTest {

    @Test
    fun `gregorian to hijri - known anchors`() {
        assertThat(HijriDate.from(LocalDate.of(2024, 7, 7))).isEqualTo(HijriDate.of(1446, 1, 1))
        assertThat(HijriDate.from(LocalDate.of(2024, 6, 16))).isEqualTo(HijriDate.of(1445, 12, 10))
        assertThat(HijriDate.from(LocalDate.of(2025, 3, 1))).isEqualTo(HijriDate.of(1446, 9, 1))
        assertThat(HijriDate.from(LocalDate.of(2025, 3, 30))).isEqualTo(HijriDate.of(1446, 10, 1))
        assertThat(HijriDate.from(LocalDate.of(2025, 6, 26))).isEqualTo(HijriDate.of(1447, 1, 1))
    }

    @Test
    fun `hijri to gregorian - round trip`() {
        val hijri = HijriDate.of(1447, 1, 1)
        assertThat(hijri.gregorian).isEqualTo(LocalDate.of(2025, 6, 26))
        assertThat(HijriDate.from(hijri.gregorian)).isEqualTo(hijri)
    }

    @Test
    fun `manual day adjustment shifts the displayed date`() {
        // The adjustment simply shifts the shown date by whole days, so it must
        // equal converting the shifted Gregorian date directly.
        assertThat(HijriDate.from(LocalDate.of(2025, 3, 1), adjustment = 1))
            .isEqualTo(HijriDate.from(LocalDate.of(2025, 3, 2)))
        assertThat(HijriDate.from(LocalDate.of(2025, 3, 1), adjustment = -1))
            .isEqualTo(HijriDate.from(LocalDate.of(2025, 2, 28)))
    }

    @Test
    fun `month names are Arabic`() {
        assertThat(HijriDate.of(1446, 9, 1).monthName).isEqualTo("رمضان")
        assertThat(HijriDate.of(1446, 1, 1).monthName).isEqualTo("محرم")
        assertThat(HijriDate.of(1446, 12, 1).monthName).isEqualTo("ذو الحجة")
    }

    @Test
    fun `formatting`() {
        assertThat(HijriDate.of(1446, 9, 1).formatArabic()).isEqualTo("1 رمضان 1446")
    }

    @Test
    fun `plusDays rolls across months`() {
        val endOfMuharram = HijriDate.of(1446, 1, 29)
        assertThat(endOfMuharram.plusDays(1)).isEqualTo(HijriDate.of(1446, 2, 1))
    }
}
