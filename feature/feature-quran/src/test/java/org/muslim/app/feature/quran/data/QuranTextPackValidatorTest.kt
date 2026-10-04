package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlinx.serialization.encodeToString

class QuranTextPackValidatorTest {
    private val globalNumbers = (1..QuranTextPackValidator.FULL_QURAN_AYAH_COUNT).toSet()

    @Test
    fun completePackWithFootnotesPassesAndPreservesFootnoteCount() {
        val entries = fullEntries()
        val pack = validPack(entries)

        assertThat(QuranTextPackValidator.validate(pack, globalNumbers).footnoteCount).isEqualTo(2)
    }

    @Test
    fun missingAyahIsRejected() {
        val entries = fullEntries().dropLast(1)

        assertFailure(validPack(entries), "exactly all Quran ayahs")
    }

    @Test
    fun duplicateAyahIsRejected() {
        val entries = fullEntries().toMutableList().apply { this[lastIndex] = this[0] }

        assertFailure(validPack(entries), "Duplicate ayah")
    }

    @Test
    fun incompleteProvenanceIsRejected() {
        val pack = validPack(fullEntries()).copy(
            manifest = validPack(fullEntries()).manifest.copy(translator = " "),
        )

        assertFailure(pack, "provenance")
    }

    @Test
    fun checksumMismatchIsRejected() {
        val pack = validPack(fullEntries()).copy(
            manifest = validPack(fullEntries()).manifest.copy(sha256 = "0".repeat(64)),
        )

        assertFailure(pack, "checksum")
    }

    @Test
    fun reviewReferenceFromAnotherDomainIsRejected() {
        val pack = validPack(fullEntries()).copy(
            manifest = validPack(fullEntries()).manifest.copy(reviewReference = "https://unrelated.org/review"),
        )
        assertFailure(pack, "same publisher domain")
    }

    private fun fullEntries() = (1..QuranTextPackValidator.FULL_QURAN_AYAH_COUNT).map { number ->
        QuranTextPackEntry(
            globalNumber = number,
            text = "Verified source text $number",
            footnotes = if (number == 2) listOf("Source footnote 1", "Source footnote 2") else emptyList(),
        )
    }

    private fun validPack(entries: List<QuranTextPackEntry>): QuranTextPackFile {
        val serialized = kotlinx.serialization.json.Json.encodeToString(entries)
        return QuranTextPackFile(
            schemaVersion = QuranTextPackValidator.SUPPORTED_SCHEMA_VERSION,
            manifest = QuranTextPackManifest(
                id = "verified-en",
                kind = QuranTextPackKind.MeaningTranslation.wireValue,
                languageTag = "en",
                title = "Verified edition",
                work = "Quran meaning translation",
                translator = "Named translator",
                publisher = "Named publisher",
                sourceAttribution = "Source edition and contributor details",
                sourceUrl = "https://example.org/source",
                license = "CC BY 4.0",
                version = "1.0",
                reviewer = "Named source reviewer",
                reviewReference = "https://example.org/review",
                expectedAyahCount = QuranTextPackValidator.FULL_QURAN_AYAH_COUNT,
                footnoteCount = entries.sumOf { it.footnotes.size },
                sha256 = QuranTextPackValidator.contentSha256(serialized),
                reviewStatus = "source_reviewed",
            ),
            entries = entries,
        )
    }

    private fun assertFailure(pack: QuranTextPackFile, message: String) {
        val error = runCatching { QuranTextPackValidator.validate(pack, globalNumbers) }.exceptionOrNull()
        assertThat(error).isNotNull()
        assertThat(error?.message).contains(message)
    }
}
