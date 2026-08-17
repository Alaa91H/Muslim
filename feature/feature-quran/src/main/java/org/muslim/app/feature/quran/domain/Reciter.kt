package org.muslim.app.feature.quran.domain

/**
 * A Quran reciter whose per-ayah audio can be downloaded on demand
 * (PROJECT_PROMPT.md §6 Phase 2: قرّاء متعددون + تنزيل).
 *
 * [urlTemplate] uses `{surah}` and `{ayah}` placeholders; both are zero-padded
 * to 3 digits. The built-in entries point at EveryAyah — a public, free
 * archive of Quran recitations (folder names verified against the live
 * directory listing; see README for licensing notes). [bitrateKbps] drives the
 * approximate download-size estimates shown in the downloads screen.
 */
data class Reciter(
    val id: String,
    val name: String,
    val style: String,
    val urlTemplate: String,
    val bitrateKbps: Int = 128,
) {
    /** Resolves the audio URL for a specific ayah. */
    fun urlFor(surahNumber: Int, ayahNumberInSurah: Int): String =
        urlTemplate
            .replace("{surah}", surahNumber.toString().padStart(3, '0'))
            .replace("{ayah}", ayahNumberInSurah.toString().padStart(3, '0'))

    /** Rough per-ayah byte estimate used for download sizes (typical ayah ≈ 12s). */
    fun estimatedBytesPerAyah(): Long = (bitrateKbps * 125L) * 12L

    companion object {
        private fun entry(
            folder: String,
            name: String,
            style: String,
            bitrate: Int,
        ) = Reciter(
            id = folder.lowercase(),
            name = name,
            style = style,
            urlTemplate = "https://everyayah.com/data/$folder/{surah}{ayah}.mp3",
            bitrateKbps = bitrate,
        )

        val Bundled = listOf(
            entry("Abdul_Basit_Murattal_192kbps", "عبد الباسط عبد الصمد", "مرتّل · 192k", 192),
            entry("Abdul_Basit_Murattal_64kbps", "عبد الباسط عبد الصمد", "مرتّل · 64k", 64),
            entry("Abdul_Basit_Mujawwad_128kbps", "عبد الباسط عبد الصمد", "مجوّد · 128k", 128),
            entry("Husary_128kbps", "محمود خليل الحصري", "مرتّل · 128k", 128),
            entry("Husary_64kbps", "محمود خليل الحصري", "مرتّل · 64k", 64),
            entry("Husary_Muallim_128kbps", "محمود خليل الحصري", "معلّم · 128k", 128),
            entry("Husary_Muallim_64kbps", "محمود خليل الحصري", "معلّم · 64k", 64),
            entry("Alafasy_128kbps", "مشاري راشد العفاسي", "مرتّل · 128k", 128),
            entry("Alafasy_64kbps", "مشاري راشد العفاسي", "مرتّل · 64k", 64),
            entry("Abdurrahmaan_As-Sudais_192kbps", "عبد الرحمن السديس", "مرتّل · 192k", 192),
            entry("Saood_ash-Shuraym_128kbps", "سعود الشريم", "مرتّل · 128k", 128),
            entry("Minshawy_Murattal_128kbps", "محمد صديق المنشاوي", "مرتّل · 128k", 128),
            entry("Minshawy_Mujawwad_192kbps", "محمد صديق المنشاوي", "مجوّد · 192k", 192),
            entry("Muhammad_Ayyoub_128kbps", "محمد أيوب", "مرتّل · 128k", 128),
            entry("Abu_Bakr_Ash-Shaatree_128kbps", "أبو بكر الشاطري", "مرتّل · 128k", 128),
            entry("Maher_AlMuaiqly_128kbps", "ماهر المعيقلي", "مرتّل · 128k", 128),
            entry("Yasser_Ad-Dussary_128kbps", "ياسر الدوسري", "مرتّل · 128k", 128),
            entry("Nasser_Alqatami_128kbps", "ناصر القطامي", "مرتّل · 128k", 128),
            entry("Sahl_Yassin_128kbps", "سهل ياسين", "مرتّل · 128k", 128),
            entry("Salah_Al_Budair_128kbps", "صلاح البدير", "مرتّل · 128k", 128),
            entry("Hani_Rifai_192kbps", "هاني الرفاعي", "مرتّل · 192k", 192),
            entry("Hudhaify_128kbps", "علي الحذيفي", "مرتّل · 128k", 128),
            entry("Ibrahim_Akhdar_64kbps", "إبراهيم الأخضر", "مرتّل · 64k", 64),
            entry("Muhammad_Jibreel_128kbps", "محمد جبريل", "مرتّل · 128k", 128),
        )
    }
}
