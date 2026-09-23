package org.muslim.app.feature.learn.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class WillDraftAuthenticationAvailability {
    Available,
    NotConfigured,
    Unavailable,
}

/**
 * Small UI-layer wrapper around AndroidX BiometricPrompt.
 *
 * Authentication accepts either an enrolled biometric or the device credential
 * (PIN, pattern, or password), so the feature does not invent or persist its own
 * app-specific PIN.
 */
class WillDraftAuthenticator(
    private val activity: FragmentActivity,
) {
    fun availability(): WillDraftAuthenticationAvailability = when (
        BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS)
    ) {
        BiometricManager.BIOMETRIC_SUCCESS -> WillDraftAuthenticationAvailability.Available
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            WillDraftAuthenticationAvailability.NotConfigured

        else -> WillDraftAuthenticationAvailability.Unavailable
    }

    fun authenticate(
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {},
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence,
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode in CANCEL_ERROR_CODES) {
                        onCancelled()
                    } else {
                        onError(errString.toString())
                    }
                }
            },
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        prompt.authenticate(promptInfo)
    }

    private companion object {
        const val ALLOWED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val CANCEL_ERROR_CODES = setOf(
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
        )
    }
}
