package org.muslim.app.feature.quran.network

import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.muslim.app.feature.quran.domain.Reciter
import java.net.HttpURLConnection
import java.net.URL

/**
 * LIVE network check — deliberately separate from the offline unit tests.
 *
 * Verifies that every bundled reciter's folder actually exists on the
 * EveryAyah server by issuing a real HTTP HEAD request for the first ayah of
 * the mushaf (001001.mp3) inside each folder. A wrong folder name would yield
 * HTTP 404 and break downloads, so this guards the curated list.
 *
 * Run explicitly with:
 *   ./gradlew :feature:feature-quran:testDebugUnitTest \
 *       -DnetworkTests=true --tests "org.muslim.app.feature.quran.network.*"
 * (Skipped by default via the `networkTests` system property, so normal unit
 * test runs stay offline and fast.)
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
    fun everyBundledReciterFolderExistsOnEveryAyah() {
        assumeTrue(
            "Skipped: run with -DnetworkTests=true to hit the live server",
            System.getProperty("networkTests") == "true",
        )

        val missing = Reciter.Bundled.mapNotNull { reciter ->
            val folder = everyAyahFolderOf(reciter.urlTemplate)
            if (folder == null) {
                "reciter ${reciter.id}: cannot parse folder from template"
            } else {
                val status = headStatus("https://everyayah.com/data/$folder/001001.mp3")
                if (status == HttpURLConnection.HTTP_OK) null else "reciter ${reciter.id} ($folder): HTTP $status"
            }
        }

        assertThat(missing).isEmpty()
    }

    private fun headStatus(url: String): Int {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        return try {
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = true
            connection.responseCode
        } finally {
            connection.disconnect()
        }
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
