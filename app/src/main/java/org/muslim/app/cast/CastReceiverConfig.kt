package org.muslim.app.cast

import android.content.Context
import androidx.annotation.VisibleForTesting
import org.muslim.app.BuildConfig

/** Build-time receiver ID: custom display features stay disabled until registered by the owner. */
object CastReceiverConfig {
    private const val DEBUG_DEFAULT_RECEIVER = "CC1AD845"

    fun applicationId(context: Context): String? =
        configuredApplicationId(context) ?: if (BuildConfig.DEBUG) DEBUG_DEFAULT_RECEIVER else null

    @VisibleForTesting
    internal fun configuredApplicationId(context: Context): String? {
        val id = context.resources.getIdentifier("muslim_cast_receiver_application_id", "string", context.packageName)
        if (id == 0) return null
        return context.getString(id).takeIf(::isValidCustomApplicationId)
    }

    @VisibleForTesting
    internal fun isValidCustomApplicationId(value: String): Boolean =
        value.matches(Regex("[A-Fa-f0-9]{8}")) && value != DEBUG_DEFAULT_RECEIVER
}
