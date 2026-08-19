package org.muslim.app.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Test

class AppPreferencesTest {

    private val default = AppPreferences.DEFAULT_MORE_SECTION_ORDER

    @Test
    fun `null or blank raw order falls back to default`() {
        assertEquals(default, AppPreferences.decodeSectionOrder(null))
        assertEquals(default, AppPreferences.decodeSectionOrder(""))
        assertEquals(default, AppPreferences.decodeSectionOrder("   "))
    }

    @Test
    fun `full valid order is preserved`() {
        val custom = listOf(
            AppPreferences.MORE_SECTION_TOOLS,
            AppPreferences.MORE_SECTION_WORSHIP,
            AppPreferences.MORE_SECTION_APP,
            AppPreferences.MORE_SECTION_KNOWLEDGE,
        )
        assertEquals(custom, AppPreferences.decodeSectionOrder(custom.joinToString(",")))
    }

    @Test
    fun `unknown ids are dropped`() {
        val raw = "tools,mystery,worship,app,knowledge"
        val expected = listOf(
            AppPreferences.MORE_SECTION_TOOLS,
            AppPreferences.MORE_SECTION_WORSHIP,
            AppPreferences.MORE_SECTION_APP,
            AppPreferences.MORE_SECTION_KNOWLEDGE,
        )
        assertEquals(expected, AppPreferences.decodeSectionOrder(raw))
    }

    @Test
    fun `missing sections are appended in default order`() {
        val raw = "tools"
        val expected = listOf(
            AppPreferences.MORE_SECTION_TOOLS,
            AppPreferences.MORE_SECTION_WORSHIP,
            AppPreferences.MORE_SECTION_KNOWLEDGE,
            AppPreferences.MORE_SECTION_APP,
        )
        assertEquals(expected, AppPreferences.decodeSectionOrder(raw))
    }

    @Test
    fun `duplicates are deduplicated keeping first position`() {
        val raw = "app,worship,app,tools,worship,knowledge"
        val expected = listOf(
            AppPreferences.MORE_SECTION_APP,
            AppPreferences.MORE_SECTION_WORSHIP,
            AppPreferences.MORE_SECTION_TOOLS,
            AppPreferences.MORE_SECTION_KNOWLEDGE,
        )
        assertEquals(expected, AppPreferences.decodeSectionOrder(raw))
    }
}