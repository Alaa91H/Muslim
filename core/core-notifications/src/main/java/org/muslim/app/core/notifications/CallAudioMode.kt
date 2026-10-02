package org.muslim.app.core.notifications

import android.content.Context
import android.media.AudioManager
import android.os.Build

/** Permission-free detection for calls and VoIP sessions using Android's audio mode API. */
object CallAudioMode {
    fun isActive(context: Context): Boolean {
        val manager = context.getSystemService(AudioManager::class.java) ?: return false
        return isCallAudioMode(manager.mode, Build.VERSION.SDK_INT)
    }

    internal fun isCallAudioMode(mode: Int, sdkInt: Int): Boolean = when {
        mode == AudioManager.MODE_RINGTONE ||
            mode == AudioManager.MODE_IN_CALL ||
            mode == AudioManager.MODE_IN_COMMUNICATION -> true
        sdkInt >= Build.VERSION_CODES.R && mode == AudioManager.MODE_CALL_SCREENING -> true
        sdkInt >= Build.VERSION_CODES.TIRAMISU &&
            (mode == AudioManager.MODE_CALL_REDIRECT || mode == AudioManager.MODE_COMMUNICATION_REDIRECT) -> true
        else -> false
    }
}
