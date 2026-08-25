package org.muslim.app.feature.prayertimes.notifications

/**
 * Separates the visible prayer alert from audible delivery.
 *
 * Android notification permission and a user-selected presentation category
 * control only the visible alert. They must not silently disable the user's
 * independently enabled Adhan audio choice.
 */
internal data class AdhanAlarmDeliveryPolicy(
    val postVisibleNotification: Boolean,
    val startAudio: Boolean,
) {
    companion object {
        fun resolve(
            adhanEnabled: Boolean,
            presentationAllowed: Boolean,
        ): AdhanAlarmDeliveryPolicy = AdhanAlarmDeliveryPolicy(
            postVisibleNotification = adhanEnabled && presentationAllowed,
            startAudio = adhanEnabled,
        )
    }
}
