package org.muslim.app.feature.quran.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.muslim.app.feature.quran.domain.FullSurahRecitation
import java.net.URI

/** Defensive parser for the public MP3Quran v3 reciters endpoint. */
object Mp3QuranCatalogParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(payload: String): List<FullSurahRecitation> = runCatching {
        val root = json.parseToJsonElement(payload).jsonObject
        val reciters = root["reciters"] as? JsonArray ?: return emptyList()
        buildList {
            reciters.forEach reciterLoop@{ reciterElement ->
                val reciter = reciterElement as? JsonObject ?: return@reciterLoop
                val reciterId = reciter.int("id") ?: return@reciterLoop
                val reciterName = reciter.string("name")?.trim()?.takeIf(String::isNotEmpty)
                    ?: return@reciterLoop
                val recordings = reciter["moshaf"] as? JsonArray ?: return@reciterLoop
                recordings.forEach recordingLoop@{ recordingElement ->
                    val recording = recordingElement as? JsonObject ?: return@recordingLoop
                    val recordingId = recording.int("id") ?: return@recordingLoop
                    val rewayaName = recording.string("name")?.trim()?.takeIf(String::isNotEmpty)
                        ?: return@recordingLoop
                    val server = recording.string("server")?.let(::normalizedHttpsBaseUrl)
                        ?: return@recordingLoop
                    val availableSurahs = recording.string("surah_list")
                        ?.split(',')
                        ?.mapNotNull(String::toIntOrNull)
                        ?.filter { it in 1..114 }
                        ?.distinct()
                        ?.sorted()
                        .orEmpty()
                    if (availableSurahs.isEmpty()) return@recordingLoop
                    add(
                        FullSurahRecitation(
                            id = "mp3quran:$reciterId:$recordingId",
                            reciterId = reciterId,
                            reciterName = reciterName,
                            recordingId = recordingId,
                            rewayaName = rewayaName,
                            sourceBaseUrl = server,
                            availableSurahs = availableSurahs,
                            declaredSurahCount = recording.int("surah_total")?.takeIf { it in 1..114 },
                        ),
                    )
                }
            }
        }
    }.getOrDefault(emptyList())

    private fun JsonObject.string(name: String): String? =
        this[name]?.jsonPrimitive?.contentOrNull

    private fun JsonObject.int(name: String): Int? =
        this[name]?.jsonPrimitive?.intOrNull

    private fun normalizedHttpsBaseUrl(value: String): String? = runCatching {
        val uri = URI(value.trim())
        if (!FullSurahRecitation.isTrustedBaseUrl(uri.toASCIIString())) return null
        uri.toASCIIString().let { if (it.endsWith('/')) it else "$it/" }
    }.getOrNull()
}
