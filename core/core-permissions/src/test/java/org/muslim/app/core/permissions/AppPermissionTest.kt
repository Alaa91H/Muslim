package org.muslim.app.core.permissions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Pure metadata invariants for the unified permission model. */
class AppPermissionTest {

    @Test
    fun `runtime permissions declare their manifest permission`() {
        AppPermission.entries
            .filter { it.kind == AppPermission.Kind.Runtime }
            .forEach { permission ->
                assertThat(permission.runtimePermission).isNotNull()
            }
    }

    @Test
    fun `normal and special-access permissions need no runtime string`() {
        AppPermission.entries
            .filter { it.kind != AppPermission.Kind.Runtime }
            .forEach { permission ->
                assertThat(permission.runtimePermission).isNull()
            }
    }

    @Test
    fun `all permissions carry labels and descriptions`() {
        AppPermission.entries.forEach { permission ->
            assertThat(permission.labelRes).isGreaterThan(0)
            assertThat(permission.descriptionRes).isGreaterThan(0)
        }
    }

    @Test
    fun `location requests coarse companion`() {
        assertThat(AppPermission.Location.companionRuntimePermission)
            .isEqualTo(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @Test
    fun `notifications gate exists only from android 13`() {
        assertThat(AppPermission.Notifications.minSdk)
            .isEqualTo(android.os.Build.VERSION_CODES.TIRAMISU)
    }
}
