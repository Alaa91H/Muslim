package org.muslim.app.feature.settings.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateNotificationPolicyTest {

    @Test
    fun `new release is eligible for notification`() {
        assertTrue(UpdateNotificationPolicy.shouldNotify("1.25.36", "1.25.35"))
    }

    @Test
    fun `same release is not notified twice`() {
        assertFalse(UpdateNotificationPolicy.shouldNotify("1.25.36", "1.25.36"))
    }

    @Test
    fun `leading v does not defeat deduplication`() {
        assertFalse(UpdateNotificationPolicy.shouldNotify("v1.25.36", "1.25.36"))
    }

    @Test
    fun `blank release version is never notified`() {
        assertFalse(UpdateNotificationPolicy.shouldNotify("   ", "1.25.35"))
    }
}
