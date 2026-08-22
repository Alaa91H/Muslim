package org.muslim.app.feature.learn.domain

/** A safe, user-visible media resource. */
data class MediaResource(
    val id: String,
    val title: String,
    val description: String,
    val url: String,
    val kind: MediaKind,
)

enum class MediaKind {
    LIVE_STREAM,
    PODCAST,
    LESSON,
    STORY,
    GAME,
}

/**
 * Curated catalog entries. URLs are HTTPS and point to official/public
 * providers; the app opens them externally instead of embedding an
 * unverified player or silently downloading copyrighted media.
 */
object InteractiveMediaCatalog {
    val resources: List<MediaResource> = listOf(
        MediaResource(
            id = "live-makkah",
            title = "Makkah Live",
            description = "Live broadcast from Masjid al-Haram.",
            url = "https://www.youtube.com/@AlQuranAlKareem/live",
            kind = MediaKind.LIVE_STREAM,
        ),
        MediaResource(
            id = "live-madinah",
            title = "Madinah Live",
            description = "Live broadcast from Al-Masjid an-Nabawi when available.",
            url = "https://www.youtube.com/@AlQuranAlKareem/live",
            kind = MediaKind.LIVE_STREAM,
        ),
        MediaResource(
            id = "children-prayer",
            title = "Learn Prayer",
            description = "A step-by-step learning path for children.",
            url = "https://quran.com/",
            kind = MediaKind.LESSON,
        ),
        MediaResource(
            id = "children-wudu",
            title = "Learn Wudu",
            description = "A simple guided introduction to ablution.",
            url = "https://quran.com/",
            kind = MediaKind.LESSON,
        ),
    )

    fun isSafeExternalUrl(url: String): Boolean =
        url.startsWith("https://") && !url.contains("\\n") && !url.contains("\\r")
}
