package org.muslim.app.core.notifications

import android.media.AudioManager
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CallAudioModeTest {
    @Test
    fun `recognizes ringtone cellular and voip communication modes`() {
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_RINGTONE, Build.VERSION_CODES.O)).isTrue()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_IN_CALL, Build.VERSION_CODES.O)).isTrue()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_IN_COMMUNICATION, Build.VERSION_CODES.O)).isTrue()
    }

    @Test
    fun `recognizes newer call routing modes only on supported api levels`() {
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_CALL_SCREENING, Build.VERSION_CODES.Q)).isFalse()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_CALL_SCREENING, Build.VERSION_CODES.R)).isTrue()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_CALL_REDIRECT, Build.VERSION_CODES.S)).isFalse()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_CALL_REDIRECT, Build.VERSION_CODES.TIRAMISU)).isTrue()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_COMMUNICATION_REDIRECT, Build.VERSION_CODES.S)).isFalse()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_COMMUNICATION_REDIRECT, Build.VERSION_CODES.TIRAMISU)).isTrue()
    }

    @Test
    fun `normal and unrelated audio modes are not treated as calls`() {
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_NORMAL, Build.VERSION_CODES.UPSIDE_DOWN_CAKE)).isFalse()
        assertThat(CallAudioMode.isCallAudioMode(AudioManager.MODE_CURRENT, Build.VERSION_CODES.UPSIDE_DOWN_CAKE)).isFalse()
    }
}
