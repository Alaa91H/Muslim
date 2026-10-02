package org.muslim.app.cast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.CastOptions.Builder
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

/** Custom Quran receiver only; discovery stays disabled until a real owner-registered ID is configured. */
class MuslimCastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions = CastReceiverConfig.applicationId(context)
        ?.let { id ->
            Builder()
                .setReceiverApplicationId(id)
                .setSupportedNamespaces(listOf(QuranCastPlayback.CUSTOM_NAMESPACE))
                .build()
        }
        ?: Builder().setReceiverApplicationId("CC1AD845").build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? = null
}
