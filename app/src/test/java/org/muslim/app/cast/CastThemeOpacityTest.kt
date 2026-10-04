package org.muslim.app.cast

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.graphics.Color
import android.util.TypedValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.w3c.dom.Element
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
class CastThemeOpacityTest {
    @Test
    fun `MediaRouter button context resolves opaque colors at runtime`() {
        val appContext = RuntimeEnvironment.getApplication()
        val activityContext = ContextThemeWrapper(appContext, org.muslim.app.R.style.Theme_Muslim)
        val castContext = ContextThemeWrapper(
            activityContext,
            org.muslim.app.feature.quran.R.style.ThemeOverlay_Muslim_CastButton,
        )

        val primary = resolveColor(castContext, androidx.appcompat.R.attr.colorPrimary)
        val background = resolveColor(castContext, android.R.attr.colorBackground)
        assertEquals(Color.parseColor("#FF176B45"), primary)
        assertEquals(Color.WHITE, background)
        assertNotNull(androidx.mediarouter.app.MediaRouteButton(castContext) as View)
    }

    @Test
    fun `activity theme supplies opaque MediaRouter primary and background colors`() {
        val values = parseStyle("app/src/main/res/values/themes.xml", "Theme.Muslim")

        assertEquals("#FF176B45", values["colorPrimary"])
        assertEquals("#FFFFFFFF", values["android:colorBackground"])
    }

    @Test
    fun `Cast button wrapper keeps its own MediaRouter colors opaque`() {
        val values = parseStyle(
            "feature/feature-quran/src/main/res/values/themes.xml",
            "ThemeOverlay.Muslim.CastButton",
        )

        assertEquals("#FF176B45", values["colorPrimary"])
        assertEquals("#FFFFFFFF", values["android:colorBackground"])
    }

    private fun parseStyle(path: String, styleName: String): Map<String, String> {
        val candidates = listOf(File(path), File("../$path"))
        val resourceFile = candidates.firstOrNull(File::isFile)
            ?: error("Could not find $path from ${File(".").absolutePath}")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resourceFile)
        val styles = document.getElementsByTagName("style")
        val style = (0 until styles.length)
            .map { styles.item(it) as Element }
            .first { it.getAttribute("name") == styleName }
        val items = style.getElementsByTagName("item")
        return (0 until items.length).associate { index ->
            val item = items.item(index) as Element
            item.getAttribute("name") to item.textContent.trim()
        }
    }

    private fun resolveColor(context: Context, attribute: Int): Int {
        val value = TypedValue()
        check(context.theme.resolveAttribute(attribute, value, true)) {
            "Theme attribute $attribute was not resolved"
        }
        return if (value.resourceId != 0) {
            context.getColor(value.resourceId)
        } else {
            value.data
        }
    }
}
