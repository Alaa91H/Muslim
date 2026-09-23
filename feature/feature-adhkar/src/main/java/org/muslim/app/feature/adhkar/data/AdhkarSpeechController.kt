package org.muslim.app.feature.adhkar.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A user-selectable local Arabic voice exposed by the installed Android TTS engine. */
data class AdhkarSpeechVoiceOption(
    val name: String,
    val label: String,
)

/**
 * Owns the optional adhkar read-aloud engine.
 *
 * Only Arabic voices that do not require a network connection are exposed or
 * used, keeping adhkar text on the device. The feature itself is disabled by
 * default in [AdhkarPrefs].
 */
@Singleton
class AdhkarSpeechController @Inject constructor(
    @ApplicationContext context: Context,
) : TextToSpeech.OnInitListener {

    private val textToSpeech = TextToSpeech(context.applicationContext, this)

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val _initializationFailed = MutableStateFlow(false)
    val initializationFailed: StateFlow<Boolean> = _initializationFailed.asStateFlow()

    private val _voices = MutableStateFlow<List<AdhkarSpeechVoiceOption>>(emptyList())
    val voices: StateFlow<List<AdhkarSpeechVoiceOption>> = _voices.asStateFlow()

    private val _activeUtteranceId = MutableStateFlow<String?>(null)
    val activeUtteranceId: StateFlow<String?> = _activeUtteranceId.asStateFlow()

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            markUnavailable()
            return
        }

        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _activeUtteranceId.value = utteranceId
                }

                override fun onDone(utteranceId: String?) {
                    clearIfCurrent(utteranceId)
                }

                @Deprecated("Deprecated in Android SDK")
                override fun onError(utteranceId: String?) {
                    clearIfCurrent(utteranceId)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    clearIfCurrent(utteranceId)
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    clearIfCurrent(utteranceId)
                }
            },
        )

        val languageResult = textToSpeech.setLanguage(ARABIC_LOCALE)
        if (
            languageResult == TextToSpeech.LANG_MISSING_DATA ||
            languageResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            markUnavailable()
            return
        }

        refreshVoices()
        _ready.value = localArabicVoices().isNotEmpty()
        _initializationFailed.value = !_ready.value
    }

    fun refreshVoices() {
        val available = localArabicVoices()
        _voices.value = available.map { voice ->
            val localeLabel = voice.locale.getDisplayName(Locale.getDefault())
                .ifBlank { voice.locale.toLanguageTag() }
            AdhkarSpeechVoiceOption(
                name = voice.name,
                label = "$localeLabel — ${voice.name}",
            )
        }
    }

    fun speak(
        text: String,
        voiceName: String?,
        rate: Float,
        utteranceId: String,
    ): Boolean {
        if (!_ready.value || text.isBlank()) return false

        val available = localArabicVoices()
        val voice = available.firstOrNull { it.name == voiceName } ?: available.firstOrNull() ?: return false
        textToSpeech.voice = voice
        textToSpeech.setSpeechRate(rate.coerceIn(MIN_RATE, MAX_RATE))

        val result = textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId,
        )
        if (result == TextToSpeech.SUCCESS) {
            _activeUtteranceId.value = utteranceId
            return true
        }
        return false
    }

    fun stop() {
        textToSpeech.stop()
        _activeUtteranceId.value = null
    }

    private fun localArabicVoices(): List<Voice> = textToSpeech.voices
        .orEmpty()
        .asSequence()
        .filter { voice ->
            voice.locale.language.equals("ar", ignoreCase = true) &&
                !voice.isNetworkConnectionRequired
        }
        .sortedWith(compareBy({ it.locale.toLanguageTag() }, { it.name }))
        .toList()

    private fun clearIfCurrent(utteranceId: String?) {
        if (_activeUtteranceId.value == utteranceId) {
            _activeUtteranceId.value = null
        }
    }

    private fun markUnavailable() {
        _ready.value = false
        _initializationFailed.value = true
        _voices.value = emptyList()
        _activeUtteranceId.value = null
    }

    private companion object {
        val ARABIC_LOCALE: Locale = Locale.forLanguageTag("ar")
        const val MIN_RATE = 0.5f
        const val MAX_RATE = 2.0f
    }
}
