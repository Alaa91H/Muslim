package org.muslim.app.feature.learn.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InteractiveMediaCatalogTest {
    @Test
    fun `catalog contains live and children learning resources`() {
        assertThat(InteractiveMediaCatalog.resources.map { it.kind })
            .containsAtLeast(MediaKind.LIVE_STREAM, MediaKind.LESSON)
    }

    @Test
    fun `catalog urls are safe external https urls`() {
        assertThat(InteractiveMediaCatalog.resources).isNotEmpty()
        InteractiveMediaCatalog.resources.forEach {
            assertThat(InteractiveMediaCatalog.isSafeExternalUrl(it.url)).isTrue()
        }
        assertThat(InteractiveMediaCatalog.isSafeExternalUrl("http://example.com")).isFalse()
        assertThat(InteractiveMediaCatalog.isSafeExternalUrl("javascript:alert(1)")).isFalse()
    }
}
