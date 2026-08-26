package org.muslim.app.feature.quran.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.muslim.app.feature.quran.domain.TajweedAnnotation
import org.muslim.app.feature.quran.domain.TajweedRule
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class TajweedPackItem(
    val surah: Int,
    val ayah: Int,
    val annotations: List<TajweedPackAnnotation> = emptyList(),
)

@Serializable
private data class TajweedPackAnnotation(
    val start: Int,
    val end: Int,
    val rule: String,
)

/**
 * Offline Hafs tajweed annotations for the bundled Uthmani text. The source
 * spans are loaded once and validated again against each displayed ayah before
 * they are exposed to the UI; no tajweed rule is guessed from text heuristics.
 */
@Singleton
class QuranTajweedRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    @Volatile
    private var cached: Map<Int, Map<Int, List<TajweedAnnotation>>>? = null

    suspend fun annotationsForSurah(surahNumber: Int): Map<Int, List<TajweedAnnotation>> =
        load().getOrElse(surahNumber) { emptyMap() }

    private suspend fun load(): Map<Int, Map<Int, List<TajweedAnnotation>>> {
        cached?.let { return it }
        return withContext(Dispatchers.IO) {
            cached ?: context.assets.open(ASSET).bufferedReader(Charsets.UTF_8).use { reader ->
                val parsed = json.decodeFromString<List<TajweedPackItem>>(reader.readText())
                parsed.groupBy { it.surah }.mapValues { (_, ayahs) ->
                    ayahs.associate { item ->
                        item.ayah to item.annotations.mapNotNull { annotation ->
                            annotation.rule.toTajweedRule()?.let { rule ->
                                TajweedAnnotation(
                                    start = annotation.start,
                                    endExclusive = annotation.end,
                                    rule = rule,
                                )
                            }
                        }
                    }
                }.also { loaded -> cached = loaded }
            }
        }
    }

    private fun String.toTajweedRule(): TajweedRule? = when {
        this == "ghunnah" -> TajweedRule.Ghunnah
        this.startsWith("idghaam") -> TajweedRule.Idghaam
        this.startsWith("ikhfa") -> TajweedRule.Ikhfa
        this == "iqlab" -> TajweedRule.Iqlab
        this.startsWith("madd") -> TajweedRule.Madd
        this == "qalqalah" -> TajweedRule.Qalqalah
        this == "hamzat_wasl" -> TajweedRule.HamzatWasl
        this == "lam_shamsiyyah" -> TajweedRule.LamShamsiyyah
        this == "silent" -> TajweedRule.Silent
        else -> null
    }

    private companion object {
        const val ASSET = "quran_tajweed_hafs_annotations.json"
    }
}
