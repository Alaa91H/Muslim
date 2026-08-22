package org.muslim.app.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/** Error group surfaced by the one-shot accessibility voice-navigation control. */
enum class VoiceRecognitionError {
    Unavailable,
    NoMatch,
    TimedOut,
    Network,
    Other,
}

/**
 * Creates one Android speech-recognition session on an explicit user request.
 * It prefers an on-device recognizer where the platform exposes one, destroys
 * the recognizer after every terminal callback, and never stores audio or text.
 */
class VoiceCommandRecognizer(context: Context) {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null

    fun start(
        onListening: () -> Unit,
        onResults: (List<String>) -> Unit,
        onRecognitionError: (VoiceRecognitionError) -> Unit,
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onRecognitionError(VoiceRecognitionError.Unavailable)
            return
        }
        destroy()
        recognizer = createRecognizer()
        val activeRecognizer = recognizer ?: run {
            onRecognitionError(VoiceRecognitionError.Unavailable)
            return
        }
        activeRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = onListening()
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit

            override fun onError(error: Int) {
                destroy()
                onRecognitionError(error.toVoiceError())
            }

            override fun onResults(results: Bundle?) {
                val candidates = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    .orEmpty()
                destroy()
                if (candidates.isEmpty()) onRecognitionError(VoiceRecognitionError.NoMatch) else onResults(candidates)
            }

            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        activeRecognizer.startListening(recognitionIntent())
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
        } else {
            SpeechRecognizer.createSpeechRecognizer(appContext)
        }

    private fun recognitionIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    }
}

private fun Int.toVoiceError(): VoiceRecognitionError = when (this) {
    SpeechRecognizer.ERROR_NO_MATCH -> VoiceRecognitionError.NoMatch
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceRecognitionError.TimedOut
    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceRecognitionError.Network
    SpeechRecognizer.ERROR_CLIENT, SpeechRecognizer.ERROR_SERVER, SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY, SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
    SpeechRecognizer.ERROR_AUDIO -> VoiceRecognitionError.Other
    else -> VoiceRecognitionError.Unavailable
}
