package org.muslim.app.feature.settings.update

import org.json.JSONObject

/**
 * Machine-readable metadata attached to a GitHub Release as update-manifest.json.
 *
 * The APK remains authenticated by Android signing verification; this manifest
 * adds an independent SHA-256 integrity check plus an authoritative versionCode.
 */
internal data class UpdateReleaseManifest(
    val versionName: String,
    val versionCode: Long,
    val packageName: String,
    val minSdk: Int,
    val apkAssetName: String,
    val apkSha256: String,
) {
    companion object {
        fun parse(raw: String): UpdateReleaseManifest? = runCatching {
            val json = JSONObject(raw)
            require(json.optInt("schemaVersion", 0) == SUPPORTED_SCHEMA)
            val apk = json.getJSONObject("apk")
            val versionName = json.getString("versionName").trim().trimStart('v')
            val versionCode = json.getLong("versionCode")
            val packageName = json.getString("packageName").trim()
            val minSdk = json.optInt("minSdk", 0)
            val apkAssetName = apk.getString("asset").trim()
            val apkSha256 = apk.getString("sha256").trim().lowercase()

            require(versionName.isNotEmpty())
            require(versionCode > 0L)
            require(packageName.isNotEmpty())
            require(apkAssetName.endsWith(".apk", ignoreCase = true))
            require(SHA_256.matches(apkSha256))

            UpdateReleaseManifest(
                versionName = versionName,
                versionCode = versionCode,
                packageName = packageName,
                minSdk = minSdk,
                apkAssetName = apkAssetName,
                apkSha256 = apkSha256,
            )
        }.getOrNull()

        private const val SUPPORTED_SCHEMA = 1
        private val SHA_256 = Regex("^[0-9a-f]{64}$")
    }
}
