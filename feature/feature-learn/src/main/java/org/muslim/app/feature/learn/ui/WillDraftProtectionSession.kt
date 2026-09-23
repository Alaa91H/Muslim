package org.muslim.app.feature.learn.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.muslim.app.feature.learn.R

data class WillDraftProtectionActions(
    val unlock: () -> Unit,
    val enable: () -> Unit,
    val disable: () -> Unit,
    val lockNow: () -> Unit,
)

data class WillDraftProtectionSession(
    val loaded: Boolean,
    val enabled: Boolean,
    val unlocked: Boolean,
    val availability: WillDraftAuthenticationAvailability,
    val errorMessage: String?,
    val actions: WillDraftProtectionActions,
)

private data class ProtectionAuthMessages(
    val title: String,
    val subtitle: String,
    val notConfigured: String,
    val unavailable: String,
)

/**
 * Keeps device-auth state in memory only. Leaving the activity/process never
 * persists an unlocked session; when protection is enabled, ON_STOP locks the
 * private draft again.
 */
@Composable
fun rememberWillDraftProtectionSession(
    state: WillDraftProtectionState,
    onSetEnabled: (Boolean) -> Unit,
): WillDraftProtectionSession {
    val activity = LocalContext.current.findFragmentActivity()
    val authenticator = remember(activity) {
        activity?.let(::WillDraftAuthenticator)
    }
    val messages = ProtectionAuthMessages(
        title = stringResource(R.string.funeral_will_auth_prompt_title),
        subtitle = stringResource(R.string.funeral_will_auth_prompt_subtitle),
        notConfigured = stringResource(R.string.funeral_will_auth_not_configured),
        unavailable = stringResource(R.string.funeral_will_auth_unavailable),
    )
    var unlocked by remember { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, state.enabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && state.enabled) {
                unlocked = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val availability = authenticator?.availability()
        ?: WillDraftAuthenticationAvailability.Unavailable
    val authenticate: ((() -> Unit) -> Unit) = { afterSuccess ->
        requestDraftAuthentication(
            authenticator = authenticator,
            availability = availability,
            messages = messages,
            onError = { errorMessage = it },
            onSuccess = {
                errorMessage = null
                afterSuccess()
            },
        )
    }

    return WillDraftProtectionSession(
        loaded = state.loaded,
        enabled = state.enabled,
        unlocked = state.loaded && (!state.enabled || unlocked),
        availability = availability,
        errorMessage = errorMessage,
        actions = WillDraftProtectionActions(
            unlock = {
                authenticate {
                    unlocked = true
                }
            },
            enable = {
                authenticate {
                    unlocked = true
                    onSetEnabled(true)
                }
            },
            disable = {
                authenticate {
                    unlocked = true
                    onSetEnabled(false)
                }
            },
            lockNow = {
                errorMessage = null
                unlocked = false
            },
        ),
    )
}

private fun requestDraftAuthentication(
    authenticator: WillDraftAuthenticator?,
    availability: WillDraftAuthenticationAvailability,
    messages: ProtectionAuthMessages,
    onError: (String) -> Unit,
    onSuccess: () -> Unit,
) {
    when (availability) {
        WillDraftAuthenticationAvailability.Available -> {
            authenticator?.authenticate(
                title = messages.title,
                subtitle = messages.subtitle,
                onSuccess = onSuccess,
                onError = onError,
            )
        }

        WillDraftAuthenticationAvailability.NotConfigured -> {
            onError(messages.notConfigured)
        }

        WillDraftAuthenticationAvailability.Unavailable -> {
            onError(messages.unavailable)
        }
    }
}


private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
