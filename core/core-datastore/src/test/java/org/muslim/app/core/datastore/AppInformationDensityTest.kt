package org.muslim.app.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppInformationDensityTest {

    @Test
    fun newOrMigratedPreferences_defaultToComfortableDensity() {
        assertEquals(AppInformationDensity.Comfortable, AppPreferences().informationDensity)
    }

    @Test
    fun compactDensity_remainsAnExplicitDistinctUserChoice() {
        assertNotEquals(AppInformationDensity.Comfortable, AppInformationDensity.Compact)
        assertEquals(AppInformationDensity.Compact, AppPreferences(
            informationDensity = AppInformationDensity.Compact,
        ).informationDensity)
    }
}
