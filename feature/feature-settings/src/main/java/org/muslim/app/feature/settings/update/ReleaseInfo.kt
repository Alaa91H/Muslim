package org.muslim.app.feature.settings.update

/**
 * The latest published release of the Muslim app, parsed from the GitHub
 * Releases API (https://api.github.com/repos/Alaa91H/Muslim/releases/latest).
 * Only the fields the update screen and the checker need are kept.
 */
data class ReleaseInfo(
    /** Version without the leading "v" (e.g. "1.5.0"). */
    val version: String,
    /** Raw tag name as published (e.g. "v1.5.0"). */
    val tagName: String,
    /** Release title. */
    val name: String,
    /** Markdown changelog body. */
    val body: String,
    /** Direct download URL of the signed release APK, if attached. */
    val apkUrl: String?,
    /** Size of the APK in bytes (0 when unknown). */
    val apkSizeBytes: Long,
)

/** Compares dotted version strings ("1.5.0" > "1.4.9"). Purely numeric. */
object VersionCompare {
    fun isNewer(latest: String, installed: String): Boolean {
        val a = latest.trim().trimStart('v').split('.').map { it.toIntOrNull() ?: 0 }
        val b = installed.trim().trimStart('v').split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
