package org.muslim.app.feature.quran.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.net.URI

enum class QuranTextPackKind(val wireValue: String) {
    MeaningTranslation("meaning_translation"),
    TafsirTranslation("tafsir_translation"),
    OriginalTafsir("tafsir_original"),
}

@Serializable
data class QuranTextPackFile(
    val schemaVersion: Int,
    val manifest: QuranTextPackManifest,
    val entries: List<QuranTextPackEntry>,
)

@Serializable
data class QuranTextPackManifest(
    val id: String,
    val kind: String,
    val languageTag: String,
    val title: String,
    val work: String,
    val translator: String,
    val publisher: String,
    val sourceAttribution: String,
    val sourceUrl: String,
    val license: String,
    val version: String,
    val reviewer: String,
    val reviewReference: String,
    val expectedAyahCount: Int,
    val footnoteCount: Int,
    val sha256: String,
    /** A source review or editorial review label; this is provenance, not an automated quality score. */
    val reviewStatus: String,
)

@Serializable
data class QuranTextPackEntry(
    val globalNumber: Int,
    val text: String,
    /** Preserve every source footnote verbatim; empty is valid only when the source has none. */
    val footnotes: List<String>,
)

internal data class QuranTextPackValidationResult(
    val footnoteCount: Int,
)

internal object QuranTextPackValidator {
    fun validate(pack: QuranTextPackFile, expectedGlobalNumbers: Set<Int>): QuranTextPackValidationResult {
        require(pack.schemaVersion == SUPPORTED_SCHEMA_VERSION) { "Unsupported Quran text pack schema" }
        val manifest = pack.manifest
        require(manifest.id.matches(Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{1,127}"))) { "Invalid pack id" }
        require(manifest.kind in QuranTextPackKind.entries.map(QuranTextPackKind::wireValue)) {
            "Unknown Quran text pack kind"
        }
        require(runCatching { java.util.Locale.Builder().setLanguageTag(manifest.languageTag).build() }.isSuccess) {
            "Invalid language tag"
        }
        listOf(
            manifest.languageTag, manifest.title, manifest.work, manifest.translator,
            manifest.publisher, manifest.sourceAttribution, manifest.sourceUrl, manifest.license, manifest.version,
            manifest.reviewer, manifest.reviewReference,
        ).forEach { require(it.isNotBlank()) { "Missing Quran text pack provenance" } }
        val sourceUri = runCatching { URI(manifest.sourceUrl) }.getOrNull()
        val reviewUri = runCatching { URI(manifest.reviewReference) }.getOrNull()
        require(sourceUri?.scheme.equals("https", ignoreCase = true) && !sourceUri?.host.isNullOrBlank()) {
            "Pack source must use HTTPS"
        }
        require(reviewUri?.scheme.equals("https", ignoreCase = true) && !reviewUri?.host.isNullOrBlank()) {
            "Pack review reference must use HTTPS"
        }
        require(sourceUri?.host.equals(reviewUri?.host, ignoreCase = true)) {
            "Source and review evidence must belong to the same publisher domain"
        }
        require(manifest.reviewStatus in setOf("source_reviewed", "editor_reviewed")) {
            "Pack does not declare a recognized review status"
        }
        require(manifest.expectedAyahCount == FULL_QURAN_AYAH_COUNT) {
            "Full-Quran packs must declare all $FULL_QURAN_AYAH_COUNT ayahs"
        }
        require(manifest.sha256.matches(Regex("[a-fA-F0-9]{64}"))) { "Invalid pack SHA-256" }
        val actualSha256 = contentSha256(Json.encodeToString(pack.entries))
        require(actualSha256.equals(manifest.sha256, ignoreCase = true)) { "Pack content checksum mismatch" }
        require(expectedGlobalNumbers.size == FULL_QURAN_AYAH_COUNT) { "Local Quran index is incomplete" }
        val ids = pack.entries.map(QuranTextPackEntry::globalNumber)
        require(ids.size == ids.toSet().size) { "Duplicate ayah entries" }
        require(ids.toSet() == expectedGlobalNumbers) { "Pack does not cover exactly all Quran ayahs" }
        require(pack.entries.all { it.text.isNotBlank() }) { "Pack contains blank ayah text" }
        require(pack.entries.flatMap(QuranTextPackEntry::footnotes).all(String::isNotBlank)) {
            "Pack contains a blank footnote"
        }
        val footnoteCount = pack.entries.sumOf { it.footnotes.size }
        require(manifest.footnoteCount == footnoteCount) { "Footnote count does not match manifest" }
        return QuranTextPackValidationResult(footnoteCount)
    }

    const val SUPPORTED_SCHEMA_VERSION = 2
    const val FULL_QURAN_AYAH_COUNT = 6_236

    fun contentSha256(serializedEntries: String): String = MessageDigest.getInstance("SHA-256")
        .digest(serializedEntries.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
