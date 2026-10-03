package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Mp3QuranCatalogParserTest {
    @Test
    fun parsesMoshafAsFullSurahScopeAndPreservesAvailableSurahs() {
        val json = """
            {"reciters":[{"id":231,"name":"Hazza Al-Balushi","moshaf":[
              {"id":231,"name":"Hafs - Murattal","server":"https://server11.mp3quran.net/hazza/",
               "surah_total":3,"surah_list":"1,18,114"}
            ]}]}
        """.trimIndent()

        val variant = Mp3QuranCatalogParser.parse(json).single()

        assertThat(variant.id).isEqualTo("mp3quran:231:231")
        assertThat(variant.reciterName).isEqualTo("Hazza Al-Balushi")
        assertThat(variant.rewayaName).isEqualTo("Hafs - Murattal")
        assertThat(variant.availableSurahs).containsExactly(1, 18, 114).inOrder()
        assertThat(variant.audioUrl(18)).isEqualTo("https://server11.mp3quran.net/hazza/018.mp3")
        assertThat(variant.audioUrl(2)).isNull()
    }

    @Test
    fun rejectsInsecureOrMalformedSourcesAndInvalidSurahNumbers() {
        val json = """
            {"reciters":[
              {"id":1,"name":"Reader","moshaf":[
                {"id":1,"name":"Hafs","server":"http://server.example/audio/","surah_list":"1,2"},
                {"id":2,"name":"Bad host","server":"https://user:pass@example.com/audio/","surah_list":"1,2"},
              {"id":3,"name":"Impostor","server":"https://server.mp3quran.net.evil.example/audio/","surah_list":"1,2"},
              {"id":4,"name":"Good","server":"https://server9.mp3quran.net/audio/","surah_list":"0,1,115,2,broken,2"}
              ]},
              {"id":2,"name":"","moshaf":[{"id":4,"name":"Hafs","server":"https://server.example/","surah_list":"1"}]}
            ]}
        """.trimIndent()

        val variants = Mp3QuranCatalogParser.parse(json)

        assertThat(variants).hasSize(1)
        assertThat(variants.single().id).isEqualTo("mp3quran:1:4")
        assertThat(variants.single().availableSurahs).containsExactly(1, 2).inOrder()
    }

    @Test
    fun returnsAnEmptyListForNoRecitersAndRejectsMalformedPayload() {
        assertThat(Mp3QuranCatalogParser.parse("{\"reciters\":[]}")).isEmpty()
        assertThat(Mp3QuranCatalogParser.parse("not-json")).isEmpty()
    }

    @Test
    fun audioUrlCannotBeForgedWithAnUntrustedHost() {
        val forged = org.muslim.app.feature.quran.domain.FullSurahRecitation(
            id = "forged",
            reciterId = 1,
            reciterName = "Reader",
            recordingId = 1,
            rewayaName = "Hafs",
            sourceBaseUrl = "https://attacker.example/audio/",
            availableSurahs = listOf(1),
            declaredSurahCount = 1,
        )

        assertThat(forged.audioUrl(1)).isNull()
    }

    @Test
    fun audioProbeRequiresSuccessfulMpegResponseAndReportsSeekSupport() {
        val verified = Mp3QuranAudioProbe.inspect(
            statusCode = 200,
            contentType = "audio/mpeg",
            contentLength = 375_643,
            acceptRanges = "bytes",
        )

        assertThat(verified?.contentLengthBytes).isEqualTo(375_643)
        assertThat(verified?.supportsRangeRequests).isTrue()
    }

    @Test
    fun audioProbeRejectsErrorsWrongTypesAndUnknownSizes() {
        assertThat(Mp3QuranAudioProbe.inspect(404, "audio/mpeg", 100L, "bytes")).isNull()
        assertThat(Mp3QuranAudioProbe.inspect(200, "text/html", 100L, "bytes")).isNull()
        assertThat(Mp3QuranAudioProbe.inspect(200, "audio/mpeg", null, "bytes")).isNull()
        assertThat(Mp3QuranAudioProbe.inspect(200, "audio/mpeg", 0L, "bytes")).isNull()
    }
}
