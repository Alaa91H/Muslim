package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RitualCounterTest {
    @Test
    fun `tawaf and sai counters are seven and bounded`() {
        var progress = RitualProgress()
        repeat(9) { progress = progress.increment(RitualKind.TAWAF) }
        assertThat(progress.tawaf.completed).isEqualTo(7)
        assertThat(progress.tawaf.isComplete).isTrue()
        repeat(2) { progress = progress.decrement(RitualKind.TAWAF) }
        assertThat(progress.tawaf.completed).isEqualTo(5)
        assertThat(progress.tawaf.reset().completed).isEqualTo(0)
    }

    @Test
    fun `coordinates outside makkah do not select a sacred site`() {
        assertThat(HajjLocationGuide.siteAt(40.0, -74.0)).isNull()
        assertThat(HajjLocationGuide.guidanceFor(SacredSite.ARAFAH).supplication).isNotEmpty()
    }

    @Test
    fun `nearby makkah coordinates select offline guidance`() {
        assertThat(HajjLocationGuide.siteAt(21.4225, 39.8262)).isEqualTo(SacredSite.KAABA)
    }
}
