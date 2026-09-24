package org.muslim.app.core.notifications

import dagger.Module
import dagger.multibindings.Multibinds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Feature-owned side effects that must run when a global notification category
 * is enabled or disabled from Settings.
 *
 * Implementations stay inside their owning feature module. Settings only talks
 * to this core contract, which prevents feature-to-feature dependencies.
 */
interface FeatureNotificationToggleHandler {
    val category: NotificationCategory

    suspend fun onEnabledChanged(enabled: Boolean)
}

/**
 * Feature-owned notification schedule preference exposed to the global Settings
 * screen without exposing the feature's repository or worker implementation.
 */
interface TimedFeatureNotificationSettings {
    val category: NotificationCategory
    val timeMinutes: Flow<Int>
    val defaultTimeMinutes: Int

    suspend fun setTimeMinutes(minutes: Int)
}

/**
 * Registry for optional feature notification integrations contributed through
 * Dagger/Hilt multibindings.
 */
@Singleton
class FeatureNotificationCoordinator @Inject constructor(
    private val toggleHandlers: Set<@JvmSuppressWildcards FeatureNotificationToggleHandler>,
    private val timedSettings: Set<@JvmSuppressWildcards TimedFeatureNotificationSettings>,
) {
    suspend fun onEnabledChanged(category: NotificationCategory, enabled: Boolean) {
        toggleHandlers
            .filter { it.category == category }
            .forEach { it.onEnabledChanged(enabled) }
    }

    fun requireTimedSettings(category: NotificationCategory): TimedFeatureNotificationSettings {
        val providers = timedSettings.filter { it.category == category }
        check(providers.size == 1) {
            "Expected exactly one timed notification settings provider for $category, found ${providers.size}"
        }
        return providers.single()
    }
}

/**
 * Declares empty sets so feature contributions are optional and the core graph
 * remains valid even when a consuming build does not include every feature.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureNotificationMultibindingsModule {
    @Multibinds
    abstract fun toggleHandlers(): Set<FeatureNotificationToggleHandler>

    @Multibinds
    abstract fun timedSettings(): Set<TimedFeatureNotificationSettings>
}
