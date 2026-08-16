package org.example.islamicapp.feature.quran.domain

/**
 * A Quran reciter whose per-ayah audio can be downloaded on demand
 * (PROJECT_PROMPT.md §6 Phase 2: قرّاء متعددون + تنزيل).
 *
 * [urlTemplate] uses `{surah}` and `{ayah}` placeholders, e.g.
 * `https://everyayah.com/data/{reciterId}/...`. The template is resolved to a
 * URL by [urlFor].
 *
 * The built-in entries point at EveryAyah — a public, free archive of Quran
 * recitations (see README for licensing notes). Any reciter can be added by
 * providing a template; downloads are always user-initiated.
 */
data class Reciter(
    val id: String,
    val name: String,
    val style: String,
    val urlTemplate: String,
) {
    /** Resolves the audio URL for a specific ayah. */
    fun urlFor(surahNumber: Int, ayahNumberInSurah: Int): String =
        urlTemplate
            .replace("{surah}", surahNumber.toString().padStart(3, '0'))
            .replace("{ayah}", ayahNumberInSurah.toString().padStart(3, '0'))

    companion object {
        val Bundled = listOf(
            Reciter(
                id = "abdulbasit_murattal",
                name = "عبد الباسط عبد الصمد",
                style = "مرتّل (192k)",
                urlTemplate = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/{surah}{ayah}.mp3",
            ),
            Reciter(
                id = "husary_muallim",
                name = "محمود خليل الحصري",
                style = "معلّم (64k)",
                urlTemplate = "https://everyayah.com/data/Husary_Muallim_64kbps/{surah}{ayah}.mp3",
            ),
            Reciter(
                id = "mishaari_raashid",
                name = "مشاري راشد العفاسي",
                style = "مرتّل (128k)",
                urlTemplate = "https://everyayah.com/data/Mishaari_Raashid_Al_3fasee_128kbps/{surah}{ayah}.mp3",
            ),
        )
    }
}
