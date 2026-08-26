package org.muslim.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.muslim.app.R

private sealed interface VoiceButtonState {
    data object Idle : VoiceButtonState
    data object Listening : VoiceButtonState
    data class Message(val stringRes: Int) : VoiceButtonState
}

/**
 * A visible, one-shot voice-navigation control. It has no hot word and never
 * listens until the user presses it. Any recognizer lifecycle ends when this
 * composable leaves composition.
 */
@Composable
fun VoiceNavigationButton(
    onTarget: (VoiceNavigationTarget) -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: VoiceNavigationViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val recognizer = remember { VoiceCommandRecognizer(context) }
    var state by remember { mutableStateOf<VoiceButtonState>(VoiceButtonState.Idle) }

    fun startListening() {
        recognizer.start(
            onListening = { state = VoiceButtonState.Listening },
            onResults = { candidates ->
                when (val target = viewModel.match(candidates)) {
                    null -> state = VoiceButtonState.Message(R.string.voice_command_unrecognized)
                    else -> {
                        state = VoiceButtonState.Idle
                        onTarget(target)
                    }
                }
            },
            onRecognitionError = { error ->
                state = VoiceButtonState.Message(error.messageRes())
            },
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) startListening() else state = VoiceButtonState.Message(R.string.voice_microphone_denied)
    }

    DisposableEffect(recognizer) {
        onDispose { recognizer.destroy() }
    }

    val listening = state is VoiceButtonState.Listening
    val label = when (val current = state) {
        VoiceButtonState.Idle -> stringResource(R.string.voice_listen)
        VoiceButtonState.Listening -> stringResource(R.string.voice_listening)
        is VoiceButtonState.Message -> stringResource(current.stringRes)
    }
    ExtendedFloatingActionButton(
        onClick = {
            if (listening) {
                recognizer.destroy()
                state = VoiceButtonState.Idle
            } else if (viewModel.microphoneGranted()) {
                startListening()
            } else {
                viewModel.microphonePermission()?.let(permissionLauncher::launch)
                    ?: run { state = VoiceButtonState.Message(R.string.voice_service_unavailable) }
            }
        },
        modifier = modifier,
        icon = {
            Icon(
                imageVector = if (listening) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = null,
            )
        },
        text = {
            Text(
                text = label,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    )
}

private fun VoiceRecognitionError.messageRes(): Int = when (this) {
    VoiceRecognitionError.Unavailable -> R.string.voice_service_unavailable
    VoiceRecognitionError.NoMatch -> R.string.voice_command_unrecognized
    VoiceRecognitionError.TimedOut -> R.string.voice_command_timeout
    VoiceRecognitionError.Network -> R.string.voice_command_network
    VoiceRecognitionError.Other -> R.string.voice_command_error
}
