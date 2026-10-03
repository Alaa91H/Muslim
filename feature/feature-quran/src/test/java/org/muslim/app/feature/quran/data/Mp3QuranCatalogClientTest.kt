package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class Mp3QuranCatalogClientTest {
    @Test
    fun fetchUsesRequestedLanguageAndParsesRecordingVariants() = runBlocking {
        val call = mockk<Call>()
        val capturedRequest = slot<Request>()
        val client = mockk<OkHttpClient>()
        every { client.newCall(capture(capturedRequest)) } returns call
        every { call.execute() } returns response(
            body = """
                {"reciters":[{"id":1,"name":"Ibrahim Al-Akdar","moshaf":[
                  {"id":2,"name":"Hafs - Murattal","server":"https://server6.mp3quran.net/akdr/",
                   "surah_total":114,"surah_list":"1,2,114"}
                ]}]}
            """.trimIndent(),
            contentType = "application/json; charset=utf-8",
        )

        val recordings = Mp3QuranCatalogClient(client).fetch("eng")

        assertThat(capturedRequest.captured.method).isEqualTo("GET")
        assertThat(capturedRequest.captured.url.queryParameter("language")).isEqualTo("eng")
        assertThat(recordings.single().audioUrl(2)).isEqualTo("https://server6.mp3quran.net/akdr/002.mp3")
    }

    @Test
    fun verifyAudioSourceMakesHeadRequestAndReturnsTransportFacts() = runBlocking {
        val call = mockk<Call>()
        val capturedRequest = slot<Request>()
        val client = mockk<OkHttpClient>()
        every { client.newCall(capture(capturedRequest)) } returns call
        every { call.execute() } returns response(
            body = "",
            contentType = "audio/mpeg",
            extraHeaders = mapOf("Content-Length" to "48000", "Accept-Ranges" to "bytes"),
        )
        val recording = Mp3QuranCatalogParser.parse(
            """{"reciters":[{"id":7,"name":"Reader","moshaf":[{"id":8,"name":"Hafs","server":"https://server9.mp3quran.net/r/","surah_list":"1"}]}]}""",
        ).single()

        val verified = Mp3QuranCatalogClient(client).verifyAudioSource(recording, 1)

        assertThat(capturedRequest.captured.method).isEqualTo("HEAD")
        assertThat(capturedRequest.captured.url.encodedPath).isEqualTo("/r/001.mp3")
        assertThat(verified?.contentLengthBytes).isEqualTo(48_000L)
        assertThat(verified?.supportsRangeRequests).isTrue()
    }

    private fun response(
        body: String,
        contentType: String,
        extraHeaders: Map<String, String> = emptyMap(),
    ): Response {
        val headers = Headers.Builder()
            .add("Content-Type", contentType)
            .apply { extraHeaders.forEach { (name, value) -> add(name, value) } }
            .build()
        return Response.Builder()
            .request(Request.Builder().url("https://mp3quran.net/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .headers(headers)
            .body(body.toResponseBody())
            .build()
    }
}
