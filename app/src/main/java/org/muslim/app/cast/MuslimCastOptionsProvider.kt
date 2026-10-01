package org.muslim.app.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.CastOptions.Builder
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/** Uses Cast's public Default Media Receiver until an owner-registered Quran receiver is configured. */
class MuslimCastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        Builder()
            .setReceiverApplicationId(CastReceiverConfig.applicationId(context) ?: "CC1AD845")
            .setSupportedNamespaces(listOf(QuranCastPlayback.CUSTOM_NAMESPACE))
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
