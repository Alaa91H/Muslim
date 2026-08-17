package org.muslim.app.feature.prayertimes.domain

import com.google.common.truth.Truth.assertThat
import org.muslim.app.core.common.prayer.AdhanPlaybackPlan
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.junit.Test

class AdhanPlaybackPlanTest {

    @Test
    fun `silent never plays nor vibrates`() {
        val plan = AdhanPlaybackPlan.plan(AdhanSoundOption.Silent, hasBundledSound = true, vibrationEnabled = true)
        assertThat(plan.playSound).isFalse()
        assertThat(plan.vibrate).isFalse()
    }

    @Test
    fun `default plays sound when a bundled sound exists`() {
        val plan = AdhanPlaybackPlan.plan(AdhanSoundOption.Default, hasBundledSound = true, vibrationEnabled = true)
        assertThat(plan.playSound).isTrue()
        assertThat(plan.vibrate).isFalse()
    }

    @Test
    fun `default falls back to vibration when no sound ships`() {
        val plan = AdhanPlaybackPlan.plan(AdhanSoundOption.Default, hasBundledSound = false, vibrationEnabled = true)
        assertThat(plan.playSound).isFalse()
        assertThat(plan.vibrate).isTrue()
    }

    @Test
    fun `default fallback respects the vibration master switch`() {
        val plan = AdhanPlaybackPlan.plan(AdhanSoundOption.Default, hasBundledSound = false, vibrationEnabled = false)
        assertThat(plan.playSound).isFalse()
        assertThat(plan.vibrate).isFalse()
    }

    @Test
    fun `vibrate-only vibrates when enabled`() {
        val plan = AdhanPlaybackPlan.plan(AdhanSoundOption.VibrateOnly, hasBundledSound = true, vibrationEnabled = true)
        assertThat(plan.playSound).isFalse()
        assertThat(plan.vibrate).isTrue()
    }

    @Test
    fun `vibrate-only is quiet when the master switch is off`() {
        val plan = AdhanPlaybackPlan.plan(AdhanSoundOption.VibrateOnly, hasBundledSound = true, vibrationEnabled = false)
        assertThat(plan.playSound).isFalse()
        assertThat(plan.vibrate).isFalse()
    }
}
