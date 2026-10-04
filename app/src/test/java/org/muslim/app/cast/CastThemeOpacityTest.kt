package org.muslim.app.cast

import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class CastThemeOpacityTest {
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
}
