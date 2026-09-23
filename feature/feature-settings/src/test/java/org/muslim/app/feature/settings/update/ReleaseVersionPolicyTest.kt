package org.muslim.app.feature.settings.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseVersionPolicyTest {

    @Test
    fun `manifest versionCode takes precedence over versionName`() {
        val release = release(version = "1.0.0", versionCode = 20000L)
        assertTrue(
            ReleaseVersionPolicy.isNewer(
                release = release,
                installedVersionCode = 19999L,
                installedVersion = "99.0.0",
            ),
        )
    }

    @Test
    fun `equal manifest versionCode is not an update`() {
        val release = release(version = "9.9.9", versionCode = 20000L)
        assertFalse(
            ReleaseVersionPolicy.isNewer(
                release = release,
                installedVersionCode = 20000L,
                installedVersion = "1.0.0",
            ),
        )
    }

    @Test
    fun `legacy release falls back to semantic numeric comparison`() {
        val release = release(version = "1.25.36", versionCode = null)
        assertTrue(
            ReleaseVersionPolicy.isNewer(
                release = release,
                installedVersionCode = 12535L,
                installedVersion = "1.25.35",
            ),
        )
    }

    private fun release(version: String, versionCode: Long?) = ReleaseInfo(
        version = version,
        tagName = "v$version",
        name = "Muslim $version",
        body = "",
        apkUrl = "https://example.invalid/app-release.apk",
        apkSizeBytes = 1L,
        versionCode = versionCode,
    )
}
