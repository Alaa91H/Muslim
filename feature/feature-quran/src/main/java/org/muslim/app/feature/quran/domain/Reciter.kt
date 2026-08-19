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

        // Curated, duplicate-free list verified against the LIVE EveryAyah
        // directory (https://everyayah.com/data/) on 2026-08-19. Every folder
        // name below exists; the highest-quality variant of each
        // (reciter + style) is kept and lower-bitrate duplicates dropped.
        // Folder names that are NOT on the server (e.g. the old
        // "Maher_AlMuaiqly_128kbps") are absent — a wrong name yields HTTP 404
        // and broke downloads.
        val Bundled = listOf(
            // --- The most requested reciters (best bitrate kept) ---
            entry("Abdul_Basit_Murattal_192kbps", "عبد الباسط عبد الصمد", "مرتّل · 192k", 192),
            entry("Abdul_Basit_Mujawwad_128kbps", "عبد الباسط عبد الصمد", "مجوّد · 128k", 128),
            entry("Husary_128kbps", "محمود خليل الحصري", "مرتّل · 128k", 128),
            entry("Husary_Muallim_128kbps", "محمود خليل الحصري", "معلّم · 128k", 128),
            entry("Husary_128kbps_Mujawwad", "محمود خليل الحصري", "مجوّد · 128k", 128),
            entry("Alafasy_128kbps", "مشاري راشد العفاسي", "مرتّل · 128k", 128),
            entry("Abdurrahmaan_As-Sudais_192kbps", "عبد الرحمن السديس", "مرتّل · 192k", 192),
            entry("Saood_ash-Shuraym_128kbps", "سعود الشريم", "مرتّل · 128k", 128),
            entry("Minshawy_Murattal_128kbps", "محمد صديق المنشاوي", "مرتّل · 128k", 128),
            entry("Minshawy_Mujawwad_192kbps", "محمد صديق المنشاوي", "مجوّد · 192k", 192),
            entry("Minshawy_Teacher_128kbps", "محمد صديق المنشاوي", "معلّم · 128k", 128),
            entry("MaherAlMuaiqly128kbps", "ماهر المعيقلي", "مرتّل · 128k", 128),
            entry("Yasser_Ad-Dussary_128kbps", "ياسر الدوسري", "مرتّل · 128k", 128),
            entry("Hani_Rifai_192kbps", "هاني الرفاعي", "مرتّل · 192k", 192),
            entry("Hudhaify_128kbps", "علي الحذيفي", "مرتّل · 128k", 128),
            entry("Muhammad_Ayyoub_128kbps", "محمد أيوب", "مرتّل · 128k", 128),
            entry("Abu_Bakr_Ash-Shaatree_128kbps", "أبو بكر الشاطري", "مرتّل · 128k", 128),
            entry("Nasser_Alqatami_128kbps", "ناصر القطامي", "مرتّل · 128k", 128),
            entry("Sahl_Yassin_128kbps", "سهل ياسين", "مرتّل · 128k", 128),
            entry("Salah_Al_Budair_128kbps", "صلاح البدير", "مرتّل · 128k", 128),
            entry("Ibrahim_Akhdar_64kbps", "إبراهيم الأخضر", "مرتّل · 64k", 64),
            entry("Muhammad_Jibreel_128kbps", "محمد جبريل", "مرتّل · 128k", 128),
            // --- More well-known reciters from the same licensed source ---
            entry("Abdullah_Basfar_192kbps", "عبد الله بصفر", "مرتّل · 192k", 192),
            entry("Abdullaah_3awwaad_Al-Juhaynee_128kbps", "عواد الجهني", "مرتّل · 128k", 128),
            entry("Ahmed_Neana_128kbps", "أحمد نعينع", "مرتّل · 128k", 128),
            entry("ahmed_ibn_ali_al_ajamy_128kbps", "أحمد بن علي العجمي", "مرتّل · 128k", 128),
            entry("Akram_AlAlaqimy_128kbps", "أكرم العلاقمي", "مرتّل · 128k", 128),
            entry("Ali_Hajjaj_AlSuesy_128kbps", "علي الحجاج السويسي", "مرتّل · 128k", 128),
            entry("Ali_Jaber_64kbps", "علي جابر", "مرتّل · 64k", 64),
            entry("Ayman_Sowaid_64kbps", "أيمن سويد", "مرتّل · 64k", 64),
            entry("aziz_alili_128kbps", "عزيز عليلي", "مرتّل · 128k", 128),
            entry("Fares_Abbad_64kbps", "فارس عباد", "مرتّل · 64k", 64),
            entry("Ghamadi_40kbps", "سعد الغامدي", "مرتّل · 40k", 40),
            entry("Karim_Mansoori_40kbps", "كريم منصوري", "مرتّل · 40k", 40),
            entry("Khaalid_Abdullaah_al-Qahtaanee_192kbps", "خالد القحطاني", "مرتّل · 192k", 192),
            entry("khalefa_al_tunaiji_64kbps", "خليفة الطنيجي", "مرتّل · 64k", 64),
            entry("mahmoud_ali_al_banna_32kbps", "محمود علي البنا", "مرتّل · 32k", 32),
            entry("Mohammad_al_Tablaway_128kbps", "محمد الطبلاوي", "مرتّل · 128k", 128),
            entry("Muhammad_AbdulKareem_128kbps", "محمد عبد الكريم", "مرتّل · 128k", 128),
            entry("Muhsin_Al_Qasim_192kbps", "محسن القاسم", "مرتّل · 192k", 192),
            entry("Mustafa_Ismail_48kbps", "مصطفى إسماعيل", "مرتّل · 48k", 48),
            entry("Nabil_Rifa3i_48kbps", "نبيل الرفاعي", "مرتّل · 48k", 48),
            entry("Salaah_AbdulRahman_Bukhatir_128kbps", "صلاح بوخاطر", "مرتّل · 128k", 128),
            entry("Yaser_Salamah_128kbps", "ياسر سلامة", "مرتّل · 128k", 128),
        )
    }
}
