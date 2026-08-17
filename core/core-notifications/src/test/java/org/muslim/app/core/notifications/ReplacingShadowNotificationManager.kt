package org.muslim.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowNotificationManager
import java.lang.reflect.Field

/**
 * Robolectric's built-in [ShadowNotificationManager] restores a "deleted"
 * channel on re-creation and only merges name / description / importance
 * lowering — so delete+recreate, the standard way apps force-apply channel
 * presentation on real Android, cannot be exercised through it.
 *
 * This shadow mimics the platform instead: a channel created after a delete
 * fully replaces the old one, while an existing (never-deleted) channel keeps
 * real Android's merge semantics (name, description, importance lowering only).
 * Used by the repository tests via `@Config(shadows = [...])`.
 */
@Implements(NotificationManager::class)
class ReplacingShadowNotificationManager : ShadowNotificationManager() {

    @Implementation
    protected override fun createNotificationChannel(channel: NotificationChannel) {
        val channels = channelsMap()
        val deleted = deletedMap()
        val id = channel.id
        if (deleted.containsKey(id)) {
            // Re-created after a delete: wholesale replacement, like the platform.
            deleted.remove(id)
            channels[id] = channel
            return
        }
        val existing = channels[id]
        if (existing == null) {
            channels[id] = channel
            return
        }
        // Existing (never-deleted) channel: real Android only accepts the name,
        // description and an importance *lowering*; everything else is ignored.
        existing.name = channel.name
        existing.description = channel.description
        if (channel.importance < existing.importance) {
            existing.importance = channel.importance
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun channelsMap(): MutableMap<String, NotificationChannel> =
        shadowField("notificationChannels")

    @Suppress("UNCHECKED_CAST")
    private fun deletedMap(): MutableMap<String, NotificationChannel> =
        shadowField("deletedNotificationChannels")

    private fun shadowField(name: String): MutableMap<String, NotificationChannel> {
        val field: Field = ShadowNotificationManager::class.java.getDeclaredField(name)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(null) as MutableMap<String, NotificationChannel>
    }
}
