package org.muslim.app.feature.settings.update

/**
 * Pure policy for update-alert deduplication.
 *
 * A release should alert only when its version is non-blank and differs from
 * the last version for which Android actually accepted a notification.
 */
internal object UpdateNotificationPolicy {
    fun shouldNotify(releaseVersion: String, lastNotifiedVersion: String): Boolean {
        val latest = releaseVersion.trim().trimStart('v')
        val last = lastNotifiedVersion.trim().trimStart('v')
        return latest.isNotEmpty() && !latest.equals(last, ignoreCase = true)
    }
}
