package org.muslim.app.feature.settings.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.muslim.app.core.datastore.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches published Muslim releases from GitHub.
 *
 * Stable uses GitHub's /releases/latest endpoint, which excludes prereleases.
 * Beta uses the releases collection and accepts the most recently published
 * non-draft release, whether stable or prerelease.
 *
 * New releases may attach update-manifest.json. When present, its
 * version/package/APK asset are matched before SHA-256/versionCode metadata is
 * trusted. Older releases remain supported as a compatibility fallback.
 */
@Singleton
class GithubReleasesClient @Inject constructor(
    private val client: OkHttpClient,
) {

    suspend fun latestRelease(
        channel: String = AppPreferences.UPDATE_CHANNEL_STABLE,
    ): ReleaseInfo? = withContext(Dispatchers.IO) {
        runCatching {
            if (channel == AppPreferences.UPDATE_CHANNEL_BETA) {
                val releases = fetchReleaseList() ?: return@withContext null
                for (i in 0 until releases.length()) {
                    val release = releases.getJSONObject(i)
                    if (release.optBoolean("draft", false)) continue
                    parseRelease(release)?.let { return@withContext it }
                }
                return@withContext null
            }

            fetchObject(LATEST_RELEASE_URL)?.let(::parseRelease)
        }.getOrNull()
    }

    private fun fetchReleaseList(): JSONArray? {
        val request = githubRequest(RELEASES_URL)
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            JSONArray(response.body?.string().orEmpty())
        }
    }

    private fun fetchObject(url: String): JSONObject? {
        val request = githubRequest(url)
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            JSONObject(response.body?.string().orEmpty())
        }
    }

    private fun parseRelease(json: JSONObject): ReleaseInfo? {
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

        val selected = selectedApk ?: return null
        val apkUrl = selected.optString("browser_download_url").takeIf(String::isNotBlank)
            ?: return null
        val tagName = json.optString("tag_name")
        val version = tagName.removePrefix("v")
        if (version.isBlank()) return null
        val selectedApkName = selected.optString("name")
        val manifest = manifestAsset
            ?.optString("browser_download_url")
            ?.takeIf(String::isNotBlank)
            ?.let(::fetchManifest)
            ?.takeIf { metadata ->
                metadata.versionName == version &&
                    metadata.packageName == APP_PACKAGE &&
                    metadata.apkAssetName.equals(selectedApkName, ignoreCase = true)
            }

        return ReleaseInfo(
            version = version,
            tagName = tagName,
            name = json.optString("name"),
            body = json.optString("body"),
            apkUrl = apkUrl,
            apkSizeBytes = selected.optLong("size", 0L),
            versionCode = manifest?.versionCode,
            apkSha256 = manifest?.apkSha256,
            minSdk = manifest?.minSdk?.takeIf { it > 0 },
            isPrerelease = json.optBoolean("prerelease", false),
            hasVerifiedMetadata = manifest != null,
        )
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
        const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/Alaa91H/Muslim/releases/latest"
        const val RELEASES_URL =
            "https://api.github.com/repos/Alaa91H/Muslim/releases?per_page=20"
    }
}
