package org.muslim.app.feature.settings.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches the latest published release of the Muslim app from the GitHub
 * Releases API. Optional feature: the caller decides when to run it, every
 * network failure just yields null (offline-first, never blocks the UI).
 */
@Singleton
class GithubReleasesClient @Inject constructor(
    private val client: OkHttpClient,
) {

    private val apiUrl = "https://api.github.com/repos/Alaa91H/Muslim/releases/latest"

    /** Returns the latest release, or null when the request fails / no APK is attached. */
    suspend fun latestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Muslim-Android")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(response.body?.string().orEmpty())
                val assets = json.optJSONArray("assets")
                var selectedApk: JSONObject? = null
                if (assets != null) {
                    // Prefer the phone/tablet production artifact explicitly.
                    // Release workflows may also attach wear-release.apk; never
                    // select it just because it happens to be the first APK.
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.optString("name").equals("app-release.apk", ignoreCase = true)) {
                            selectedApk = asset
                            break
                        }
                    }
                    if (selectedApk == null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name")
                            if (
                                name.endsWith(".apk", ignoreCase = true) &&
                                !name.contains("wear", ignoreCase = true)
                            ) {
                                selectedApk = asset
                                break
                            }
                        }
                    }
                }
                val apkUrl = selectedApk
                    ?.optString("browser_download_url")
                    ?.takeIf { it.isNotBlank() }
                val apkSize = selectedApk?.optLong("size", 0L) ?: 0L
                ReleaseInfo(
                    version = json.optString("tag_name").removePrefix("v"),
                    tagName = json.optString("tag_name"),
                    name = json.optString("name"),
                    body = json.optString("body"),
                    apkUrl = apkUrl,
                    apkSizeBytes = apkSize,
                )
            }
        }.getOrNull()
    }
}
