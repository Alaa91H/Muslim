package org.muslim.app.core.ui.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineMapAreasTest {

    @Test
    fun `cities have valid non-empty names and bounds`() {
        assertTrue(OfflineMapAreas.CITIES.isNotEmpty())
        OfflineMapAreas.CITIES.forEach { area ->
            assertTrue("city name blank: ${area.name}", area.name.isNotBlank())
            assertEquals("city kind: ${area.name}", "city", area.kind)
            assertTrue("${area.name} north>=south", area.bounds.latitudeNorth >= area.bounds.latitudeSouth)
            assertTrue("${area.name} east>=west", area.bounds.longitudeEast >= area.bounds.longitudeWest)
        }
    }

    @Test
    fun `countries have valid non-empty names and bounds`() {
        assertTrue(OfflineMapAreas.COUNTRIES.isNotEmpty())
        OfflineMapAreas.COUNTRIES.forEach { area ->
            assertTrue("country name blank: ${area.name}", area.name.isNotBlank())
            assertEquals("country kind: ${area.name}", "country", area.kind)
            assertTrue("${area.name} north>=south", area.bounds.latitudeNorth >= area.bounds.latitudeSouth)
            assertTrue("${area.name} east>=west", area.bounds.longitudeEast >= area.bounds.longitudeWest)
        }
    }

    @Test
    fun `area names are unique`() {
        val cities = OfflineMapAreas.CITIES.map { it.name }
        assertEquals("duplicate city names", cities.size, cities.toSet().size)
        val countries = OfflineMapAreas.COUNTRIES.map { it.name }
        assertEquals("duplicate country names", countries.size, countries.toSet().size)
    }

    @Test
    fun `key cities are present`() {
        val names = OfflineMapAreas.CITIES.map { it.name }
        assertTrue("Mecca missing", names.contains("Mecca"))
        assertTrue("Medina missing", names.contains("Medina"))
        assertTrue("Cairo missing", names.contains("Cairo"))
    }

    @Test
    fun `key countries are present`() {
        val names = OfflineMapAreas.COUNTRIES.map { it.name }
        assertTrue("Saudi Arabia missing", names.contains("Saudi Arabia"))
        assertTrue("Egypt missing", names.contains("Egypt"))
        assertTrue("United States missing", names.contains("United States"))
    }
}
