package org.muslim.app.feature.quran.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OfficialQuranTextSourceTest {
    @Test
    fun `maps official translation metadata without inventing attribution`() {
        val source = mapQuranEncMeaningSource(
            QuranEncCatalogEntry(
                key = "french_rashid",
                languageIsoCode = "fr",
                version = "1.0.3",
                title = "French Translation - Rachid Maach",
                description = "Translated by Rashid Ma’ash",
            ),
        )

        assertThat(source.kind).isEqualTo(OfficialQuranTextKind.Meaning)
        assertThat(source.translator).isEqualTo("Rashid Ma’ash")
        assertThat(source.reviewEvidence).isNull()
        assertThat(source.sourceAttribution).isEqualTo("Translated by Rashid Ma’ash")
    }

    @Test
    fun `requires explicit review or editorial supervision evidence`() {
        assertThat(extractReviewEvidence("Translated by A. Author. Reviewed by B. Scholar"))
            .contains("B. Scholar")
        assertThat(extractReviewEvidence("Developed under the supervision of Rowwad Translation Center"))
            .contains("Rowwad Translation Center")
        assertThat(extractReviewEvidence("Issued by Example Organization"))
            .isNull()
    }

    @Test
    fun `keeps QuranEnc translated tafsir separate and unavailable without complete provenance`() {
        val sources = quranEncTranslatedTafsirCandidates()

        assertThat(sources).hasSize(28)
        assertThat(sources.map { it.storageKey }).contains("uzbek_mokhtasar")
        assertThat(sources.map { it.languageTag }).contains("ja")
        assertThat(sources.map { it.storageKey }).containsAtLeast(
            "assamese_mokhtasar",
            "azeri_mokhtasar",
            "bosnian_mokhtasar",
            "chinese_mokhtasar",
            "french_mokhtasar",
            "italian_mokhtasar",
            "kyrgyz_mokhtasar",
            "pashto_mokhtasar",
            "serbian_mokhtasar",
            "sinhalese_mokhtasar",
            "spanish_mokhtasar",
            "tagalog_mokhtasar",
            "uyghur_mokhtasar",
            "vietnamese_mokhtasar",
        )
        assertThat(sources.all { it.kind == OfficialQuranTextKind.TranslatedTafsir }).isTrue()
        assertThat(sources.all { it.reviewEvidence == null }).isTrue()
        assertThat(sources.all { it.version == "Not specified by the source" }).isTrue()
        assertThat(sources.none { it.canDownload }).isTrue()
    }

    @Test
    fun `allows the QuranEnc Uzbek tafsir only with its indexed translator review and version`() {
        val source = quranEncUzbekMuyassarSource()

        assertThat(source.languageTag).isEqualTo("uz")
        assertThat(source.translator).isEqualTo("Ismail Yaqub")
        assertThat(source.version).isEqualTo("1.0.0")
        assertThat(source.reviewEvidence).contains("IxlosOrg")
        assertThat(source.canDownload).isTrue()
    }

    @Test
    fun `reads edition version from the matching QuranEnc source card only`() {
        val index = """
            <div class="tab_card"><div>01/01/2026 - V9.9.9</div>
              <a data-share-key="other_source"></a></div>
            <div class="tab_card"><div>28/09/2024 - V1.0.0</div>
              <h2>Uzbek Translation of At-Tafsir Al-Muyassar</h2>
              <small>Translated by Ismail Yaqub and reviewed by members of the Islamic Center IxlosOrg.</small>
              <a data-share-key="uzbek_moyassar"></a></div>
        """.trimIndent()

        assertThat(parseQuranEncEditionVersion(index, "uzbek_moyassar")).isEqualTo("1.0.0")
        assertThat(parseQuranEncEditionVersion(index, "missing_source")).isNull()
    }
}
