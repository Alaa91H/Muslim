package org.muslim.app.feature.adhkar.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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

/**
 * A user-selectable Arabic voice exposed by the installed Android TTS engine.
 *
 * [label] is deliberately user-facing and never contains the engine's opaque
 * internal name (for example `ar-xa-x-are-local`). [name] remains the stable
 * engine identifier persisted by the app.
 */
data class AdhkarSpeechVoiceOption(
    val name: String,
    val label: String,
    val localeTag: String,
    val requiresNetwork: Boolean,
    val quality: Int,
)

/**
 * Owns adhkar text-to-speech.
 *
 * Local Arabic voices are always preferred. Network-backed voices are exposed
 * as optional choices and are only used when the user explicitly enables them.
 * Speech also requests transient audio focus so recitation does not compete
 * with other media at full volume.
 */
@Singleton
class AdhkarSpeechController @Inject constructor(
    @ApplicationContext context: Context,
) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private val textToSpeech = TextToSpeech(appContext, this)
    private val audioManager = appContext.getSystemService(AudioManager::class.java)

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val _initializationFailed = MutableStateFlow(false)
    val initializationFailed: StateFlow<Boolean> = _initializationFailed.asStateFlow()

    private val _voices = MutableStateFlow<List<AdhkarSpeechVoiceOption>>(emptyList())
    val voices: StateFlow<List<AdhkarSpeechVoiceOption>> = _voices.asStateFlow()

    private val _activeUtteranceId = MutableStateFlow<String?>(null)
    val activeUtteranceId: StateFlow<String?> = _activeUtteranceId.asStateFlow()

    private val audioFocusRequest: AudioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setOnAudioFocusChangeListener { change ->
                if (
                    change == AudioManager.AUDIOFOCUS_LOSS ||
                    change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                ) {
                    stop()
                }
            }
            .build()
    }

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
    }

    /**
     * Refreshes the engine's Arabic voice inventory and assigns stable,
     * human-readable labels such as "العربية (السعودية) · 1".
     */
    fun refreshVoices() {
        val counters = mutableMapOf<String, Int>()
        val available = allArabicVoices()
        _voices.value = available.map { voice ->
            val localeTag = voice.locale.toLanguageTag()
            val ordinal = (counters[localeTag] ?: 0) + 1
            counters[localeTag] = ordinal

            val localeLabel = voice.locale
                .getDisplayName(Locale.getDefault())
                .ifBlank { localeTag }

            AdhkarSpeechVoiceOption(
                name = voice.name,
                label = "$localeLabel · $ordinal",
                localeTag = localeTag,
                requiresNetwork = voice.isNetworkConnectionRequired,
                quality = voice.quality,
            )
        }

        _ready.value = available.isNotEmpty()
        _initializationFailed.value = available.isEmpty()
    }

    fun speak(
        text: String,
        voiceName: String?,
        rate: Float,
        allowNetworkVoices: Boolean = false,
        utteranceId: String,
    ): Boolean {
        if (!_ready.value || text.isBlank()) return false

        val available = allArabicVoices()
            .filter { allowNetworkVoices || !it.isNetworkConnectionRequired }

        val voice = available.firstOrNull { it.name == voiceName }
            ?: available.firstOrNull { !it.isNetworkConnectionRequired }
            ?: available.firstOrNull()
            ?: return false

        val normalizedText = normalizeAdhkarForSpeech(text)
        if (normalizedText.isBlank()) return false

        if (audioManager.requestAudioFocus(audioFocusRequest) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return false
        }

        textToSpeech.voice = voice
        textToSpeech.setSpeechRate(rate.coerceIn(MIN_RATE, MAX_RATE))

        val result = textToSpeech.speak(
            normalizedText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId,
        )
        if (result == TextToSpeech.SUCCESS) {
            _activeUtteranceId.value = utteranceId
            return true
        }

        abandonAudioFocus()
        return false
    }

    /**
     * Stops speech only when [utteranceId] still owns the engine. Passing null
     * keeps the explicit global-stop behaviour used by the master toggle.
     */
    fun stop(utteranceId: String? = null) {
        if (utteranceId != null && _activeUtteranceId.value != utteranceId) return
        textToSpeech.stop()
        _activeUtteranceId.value = null
        abandonAudioFocus()
    }

    private fun allArabicVoices(): List<Voice> = textToSpeech.voices
        .orEmpty()
        .asSequence()
        .filter { voice -> voice.locale.language.equals("ar", ignoreCase = true) }
        .sortedWith(
            compareBy<Voice>(
                { it.isNetworkConnectionRequired },
                { -it.quality },
                { it.locale.toLanguageTag() },
                { it.name },
            ),
        )
        .toList()

    private fun clearIfCurrent(utteranceId: String?) {
        if (_activeUtteranceId.value == utteranceId) {
            _activeUtteranceId.value = null
            abandonAudioFocus()
        }
    }

    private fun abandonAudioFocus() {
        runCatching { audioManager.abandonAudioFocusRequest(audioFocusRequest) }
    }

    private fun markUnavailable() {
        _ready.value = false
        _initializationFailed.value = true
        _voices.value = emptyList()
        _activeUtteranceId.value = null
        abandonAudioFocus()
    }

    private companion object {
        val ARABIC_LOCALE: Locale = Locale.forLanguageTag("ar")
        const val MIN_RATE = 0.5f
        const val MAX_RATE = 2.0f
    }
}

/**
 * Removes decorative Qur'anic/typographic marks that TTS engines often try to
 * pronounce, while preserving the actual Arabic words and diacritics.
 */
internal fun normalizeAdhkarForSpeech(text: String): String = text
    .replace('۝', ' ')
    .replace('۞', ' ')
    .replace('…', ' ')
    .replace("...", " ")
    .replace('—', '،')
    .replace(Regex("""\s+"""), " ")
    .trim()
