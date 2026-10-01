package org.muslim.app.core.cast

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class LocalCastMediaServerTest {
    @Test fun `serves private media with head and range and rejects missing token`() {
        val file = File.createTempFile("cast-test", ".mp3")
        file.writeBytes("0123456789".toByteArray())
        val server = LocalCastMediaServer(file)
        try {
            val uri = URL(server.start())
            val connection = uri.openConnection() as HttpURLConnection
            connection.setRequestProperty("Range", "bytes=3-6")
            assertThat(connection.responseCode).isEqualTo(206)
            assertThat(connection.getHeaderField("Content-Range")).isEqualTo("bytes 3-6/10")
            assertThat(connection.inputStream.readBytes().decodeToString()).isEqualTo("3456")

            val head = (uri.openConnection() as HttpURLConnection).apply { requestMethod = "HEAD" }
            assertThat(head.responseCode).isEqualTo(200)
            assertThat(head.getHeaderField("Content-Length")).isEqualTo("10")

            val protected = URL(uri.protocol, uri.host, uri.port, "/wrong/audio.mp3").openConnection() as HttpURLConnection
            assertThat(protected.responseCode).isEqualTo(404)
        } finally {
            server.close()
            file.delete()
        }
    }
}
