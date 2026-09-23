package org.muslim.app.feature.tasbih.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.muslim.app.feature.tasbih.domain.TasbihPhrase

class TasbihPersistenceCodecTest {

    @Test
    fun `stable ids are unique for every built in phrase`() {
        val ids = TasbihPhrase.entries.map { it.storageId }

        assertThat(ids).doesNotContain("")
        assertThat(ids.distinct()).hasSize(TasbihPhrase.entries.size)
    }

    @Test
    fun `v2 counts round trip by stable id`() {
        val counts = mapOf(
            TasbihPhrase.SubhanAllah to 33,
            TasbihPhrase.Alhamdulillah to 21,
        )

        val encoded = TasbihPersistenceCodec.encodeCounts(counts)
        val decoded = TasbihPersistenceCodec.decodeCounts(encoded)

        assertThat(encoded).contains("subhan_allah:33")
        assertThat(encoded).contains("alhamdulillah:21")
        assertThat(decoded).isEqualTo(counts)
    }

    @Test
    fun `legacy ordinal counts are migrated on read`() {
        val legacy = "${TasbihPhrase.SubhanAllah.ordinal}:33;" +
            "${TasbihPhrase.Alhamdulillah.ordinal}:12"

        val decoded = TasbihPersistenceCodec.decodeCounts(legacy)

        assertThat(decoded).containsExactly(
            TasbihPhrase.SubhanAllah, 33,
            TasbihPhrase.Alhamdulillah, 12,
        )
    }

    @Test
    fun `stable selected phrase wins over legacy ordinal`() {
        val selected = TasbihPersistenceCodec.resolveSelectedPhrase(
            stableId = TasbihPhrase.AllahuAkbar.storageId,
            legacyOrdinal = TasbihPhrase.SubhanAllah.ordinal,
        )

        assertThat(selected).isEqualTo(TasbihPhrase.AllahuAkbar)
    }

    @Test
    fun `legacy selected phrase remains readable before migration`() {
        val selected = TasbihPersistenceCodec.resolveSelectedPhrase(
            stableId = null,
            legacyOrdinal = TasbihPhrase.Astaghfirullah.ordinal,
        )

        assertThat(selected).isEqualTo(TasbihPhrase.Astaghfirullah)
    }

    @Test
    fun `corrupt persisted entries are ignored safely`() {
        val decoded = TasbihPersistenceCodec.decodeCounts(
            "bad;unknown_phrase:9;subhan_allah:-1;subhan_allah:not-a-number;alhamdulillah:7",
        )

        assertThat(decoded).containsExactly(TasbihPhrase.Alhamdulillah, 7)
    }
}
