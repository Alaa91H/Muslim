package org.muslim.app.core.permissions

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Lets non-Compose, non-injected call sites (receivers, workers, services)
 * reach the [PermissionManager] from the application graph.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PermissionEntryPoint {
    fun permissionManager(): PermissionManager
}
