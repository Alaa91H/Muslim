package org.muslim.app.cast

import android.content.Context
import androidx.annotation.VisibleForTesting
import org.muslim.app.BuildConfig

/** Build-time receiver ID: custom display features stay disabled until registered by the owner. */
object CastReceiverConfig {
    fun applicationId(context: Context): String? = configuredApplicationId(context)

    @VisibleForTesting
    internal fun configuredApplicationId(context: Context): String? {
        val id = BuildConfig.CAST_RECEIVER_APP_ID.ifBlank {
            context.getSharedPreferences("cast_receiver", Context.MODE_PRIVATE)
                .getString("application_id", "").orEmpty()
        }
        return id.takeIf(::isValidCustomApplicationId)
    }

    @VisibleForTesting
    internal fun isValidCustomApplicationId(value: String): Boolean =
        value.matches(Regex("[A-Fa-f0-9]{8}")) && value != "CC1AD845"
}
