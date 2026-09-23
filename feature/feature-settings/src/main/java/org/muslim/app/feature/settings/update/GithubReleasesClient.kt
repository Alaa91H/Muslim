package org.muslim.app.feature.settings.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches the latest published release of the Muslim app from GitHub Releases.
 *
 * New releases may attach update-manifest.json. When it is present, this client
 * validates that its version/package/APK asset match the GitHub release before
 * exposing the SHA-256 + versionCode to the rest of the update pipeline.
 * Older releases remain supported without metadata as a compatibility fallback.
 */
@Singleton
class GithubReleasesClient @Inject constructor(
    private val client: OkHttpClient,
) {

    private val apiUrl = "https://api.github.com/repos/Alaa91H/Muslim/releases/latest"

    /** Returns the latest release, or null when the GitHub request itself fails. */
    suspend fun latestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val request = githubRequest(apiUrl)
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(response.body?.string().orEmpty())
                val assets = json.optJSONArray("assets")
                var selectedApk: JSONObject? = null
                var manifestAsset: JSONObject? = null

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        when {
                            asset.optString("name").equals("app-release.apk", ignoreCase = true) -> {
                                selectedApk = asset
                            }
                            asset.optString("name").equals(MANIFEST_ASSET, ignoreCase = true) -> {
                                manifestAsset = asset
                            }
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

                val tagName = json.optString("tag_name")
                val version = tagName.removePrefix("v")
                val selectedApkName = selectedApk?.optString("name").orEmpty()
                val manifest = manifestAsset
                    ?.optString("browser_download_url")
                    ?.takeIf(String::isNotBlank)
                    ?.let(::fetchManifest)
                    ?.takeIf { metadata ->
                        metadata.versionName == version &&
                            metadata.packageName == APP_PACKAGE &&
                            metadata.apkAssetName.equals(selectedApkName, ignoreCase = true)
                    }

                ReleaseInfo(
                    version = version,
                    tagName = tagName,
                    name = json.optString("name"),
                    body = json.optString("body"),
                    apkUrl = selectedApk
                        ?.optString("browser_download_url")
                        ?.takeIf(String::isNotBlank),
                    apkSizeBytes = selectedApk?.optLong("size", 0L) ?: 0L,
                    versionCode = manifest?.versionCode,
                    apkSha256 = manifest?.apkSha256,
                    minSdk = manifest?.minSdk?.takeIf { it > 0 },
                    hasVerifiedMetadata = manifest != null,
                )
            }
        }.getOrNull()
    }

    private fun fetchManifest(url: String): UpdateReleaseManifest? {
        val request = githubRequest(url)
        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                UpdateReleaseManifest.parse(response.body?.string().orEmpty())
            }
        }.getOrNull()
    }

    private fun githubRequest(url: String): Request =
        Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Muslim-Android")
            .get()
            .build()

    private companion object {
        const val APP_PACKAGE = "org.muslim.app"
        const val MANIFEST_ASSET = "update-manifest.json"
    }
}
