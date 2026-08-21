package org.muslim.app.core.common.prayer

/**
 * The adhan recordings bundled inside the APK (feature-prayer-times `res/raw`).
 *
 * These ship with every build, so the call to prayer plays offline with no
 * download ever needed. The recordings are the freely-distributed adhan clips
 * published by the open-source PrayTimes project (praytimes.org) for use in
 * prayer applications — MIT-licensed project, recordings provided on the site
 * for this purpose.
 */
enum class BundledAdhanSound(val id: String) {
    Makkah("makkah"),
    Madinah("madinah"),
    AbdulBasit("abdul_basit"),
    Minshawi("minshawi"),
    AbdulGhaffar("abdul_ghaffar"),
    AbdulHakam("abdul_hakam"),
    AlAqsa("alaqsa"),
    Egypt("egypt"),
    Halab("halab"),
    AlHussaini("al_hussaini"),
    BakirBash("bakir_bash"),
    Hafez("hafez"),
    HafizMurad("hafiz_murad"),
    Naghshbandi("naghshbandi"),
    Saber("saber"),
    SharifDoman("sharif_doman"),
    YusufIslam("yusuf_islam"),
    ;

    companion object {
        /** The default bundled recording. */
        const val DEFAULT_ID = "makkah"

        fun fromId(id: String?): BundledAdhanSound =
            entries.firstOrNull { it.id == id } ?: Makkah
    }
}
