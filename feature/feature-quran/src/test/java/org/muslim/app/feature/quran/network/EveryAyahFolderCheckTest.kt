package org.muslim.app.feature.quran.network

import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.muslim.app.feature.quran.data.Mp3QuranAudioProbe
import org.muslim.app.feature.quran.domain.Reciter
import java.net.HttpURLConnection
import java.net.URL

/**
 * LIVE network audit — deliberately separate from offline unit tests.
 *
 * Checks the generated start, longest-surah end, and final-surah end URLs for
 * every bundled EveryAyah reciter. Each sample must return a one-byte MP3 range,
 * which verifies both source availability and seek-capable HTTP behavior.
 *
 * Run explicitly with:
 *   ./gradlew :feature:feature-quran:testDebugUnitTest \
 *       -DnetworkTests=true --tests "org.muslim.app.feature.quran.network.*"
 * (Skipped by default so normal unit test runs stay offline and fast.)
 */
class EveryAyahFolderCheckTest {

    @Test
    fun everyAyahFolderParserPreservesNestedRecitationPaths() {
        assertThat(
            everyAyahFolderOf(
                "https://everyayah.com/data/warsh/warsh_ibrahim_aldosary_128kbps/{surah}{ayah}.mp3",
            ),
        ).isEqualTo("warsh/warsh_ibrahim_aldosary_128kbps")
    }

    @Test
    fun auditUrlsCoverStartLongSurahEndAndFinalSurah() {
        val urls = everyAyahAuditUrls(
            "https://everyayah.com/data/Alafasy_128kbps/{surah}{ayah}.mp3",
        )

        assertThat(urls).containsExactly(
            "https://everyayah.com/data/Alafasy_128kbps/001001.mp3",
            "https://everyayah.com/data/Alafasy_128kbps/002286.mp3",
            "https://everyayah.com/data/Alafasy_128kbps/114006.mp3",
        ).inOrder()
    }

    @Test
    fun everyBundledReciterSamplesAreAvailableSeekableMp3s() {
        assumeTrue(
            "Skipped: run with -DnetworkTests=true to hit the live server",
            System.getProperty("networkTests") == "true",
        )

        val failures = Reciter.Bundled.flatMap { reciter ->
            val urls = everyAyahAuditUrls(reciter.urlTemplate)
            if (urls.isEmpty()) return@flatMap listOf("${reciter.id}: invalid source URL template")
            urls.mapNotNull { url -> rangeFailure(url)?.let { "${reciter.id}: $url: $it" } }
        }

        assertThat(failures).isEmpty()
    }

    private fun rangeFailure(url: String): String? {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Range", "bytes=0-0")
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = true
            val status = connection.responseCode
            val verified = Mp3QuranAudioProbe.inspectRange(
                statusCode = status,
                contentType = connection.contentType,
                contentRange = connection.getHeaderField("Content-Range"),
            )
            when {
                verified == null -> listOf(
                    "unverified range response",
                    "status=$status",
                    "type=${connection.contentType}",
                    "range=${connection.getHeaderField("Content-Range")}",
                ).joinToString(", ")
                connection.getHeaderField("Content-Length")?.toLongOrNull()?.let { it != 1L } == true ->
                    "range response has unexpected length ${connection.getHeaderField("Content-Length")}"
                connection.inputStream.use { it.read() } < 0 -> "range response had no audio byte"
                else -> null
            }
        } catch (error: Exception) {
            "${error.javaClass.simpleName}: ${error.message}"
        } finally {
            connection.disconnect()
        }
    }
}

internal fun everyAyahAuditUrls(template: String): List<String> {
    val folder = everyAyahFolderOf(template) ?: return emptyList()
    return listOf("001001", "002286", "114006").map { ayah ->
        "https://everyayah.com/data/$folder/$ayah.mp3"
    }
}

internal fun everyAyahFolderOf(template: String): String? {
    val prefix = "https://everyayah.com/data/"
    if (!template.startsWith(prefix)) return null
    val suffix = "/{surah}{ayah}.mp3"
    return template.removePrefix(prefix)
        .removeSuffix(suffix)
        .takeIf { it.isNotBlank() && it != template.removePrefix(prefix) }
}
