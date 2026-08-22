package org.muslim.app.feature.learn.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Uses the device's installed Arabic text-to-speech voice for isolated letter
 * prompts. This avoids bundling unlicensed Qaida recordings; it does not
 * replace teacher-led pronunciation feedback.
 */
class ArabicSpeechController(context: Context) : TextToSpeech.OnInitListener {
    private val textToSpeech = TextToSpeech(context.applicationContext, this)

    var isReady by mutableStateOf(false)
        private set

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val languageResult = textToSpeech.setLanguage(Locale("ar"))
        isReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
            languageResult != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String) {
        if (!isReady) return
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "noorani-$text")
    }

    fun release() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}
