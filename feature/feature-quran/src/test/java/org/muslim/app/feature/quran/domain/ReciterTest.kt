package org.muslim.app.feature.quran.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReciterTest {

    @Test
    fun urlFor_padsNumbersToThreeDigits() {
        val reciter = Reciter("test", "Test", "style", "https://example.com/{surah}{ayah}.mp3")
        assertThat(reciter.urlFor(1, 7)).isEqualTo("https://example.com/001007.mp3")
        assertThat(reciter.urlFor(2, 1)).isEqualTo("https://example.com/002001.mp3")
        assertThat(reciter.urlFor(114, 6)).isEqualTo("https://example.com/114006.mp3")
    }

    @Test
    fun bundledReciters_areWellFormed() {
        Reciter.Bundled.forEach { reciter ->
            assertThat(reciter.id).isNotEmpty()
            assertThat(reciter.name).isNotEmpty()
            assertThat(reciter.urlTemplate).contains("{surah}")
            assertThat(reciter.urlTemplate).contains("{ayah}")
            assertThat(reciter.urlFor(1, 1)).startsWith("https://")
        }
    }

    @Test
    fun selectedReciterFallback_usesBundledFirst() {
        assertThat(Reciter.Bundled).isNotEmpty()
        assertThat(Reciter.Bundled.first().id).isEqualTo("abdul_basit_murattal_192kbps")
    }

    @Test
    fun bundledReciters_havePositiveBitrate() {
        Reciter.Bundled.forEach { reciter ->
            assertThat(reciter.bitrateKbps).isAtLeast(1)
            assertThat(reciter.estimatedBytesPerAyah()).isAtLeast(1L)
        }
    }

    @Test
    fun bundledReciters_haveNoDuplicateIds() {
        val ids = Reciter.Bundled.map { it.id }
        assertThat(ids.distinct()).containsExactlyElementsIn(ids)
    }

    @Test
    fun bundledReciters_keepOnlyBestQualityPerReciterStyle() {
        // No two entries may share (name + style label): lower-bitrate
        // duplicates of the same recitation were removed in favour of the
        // best quality variant ("اختر ذو الجودة الافضل واحذف التكرار").
        val keys = Reciter.Bundled.map { it.name to it.style }
        assertThat(keys.distinct()).containsExactlyElementsIn(keys)
        Reciter.Bundled.groupBy { it.name to it.style }.values.forEach { group ->
            assertThat(group.maxOf { it.bitrateKbps }).isEqualTo(group.first().bitrateKbps)
        }
    }

    @Test
    fun bundledReciters_folderNamesFollowServerSpelling() {
        // Every bundled folder must follow the live server's naming; a typo
        // yields HTTP 404 and silently broken downloads. Guards known bad
        // spellings so they can never be reintroduced.
        val folders = Reciter.Bundled.map { reciter ->
            reciter.urlTemplate.removePrefix("https://everyayah.com/data/").removeSuffix("/{surah}{ayah}.mp3")
        }
        assertThat(folders).doesNotContain("Maher_AlMuaiqly_128kbps")
        assertThat(folders).doesNotContain("")
        assertThat(folders.map { it.lowercase() }.distinct().size).isEqualTo(folders.size)
    }
}
