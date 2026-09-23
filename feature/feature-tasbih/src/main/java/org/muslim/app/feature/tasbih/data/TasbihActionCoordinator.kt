package org.muslim.app.feature.tasbih.data

import android.util.Log
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihSessionEndReason
import org.muslim.app.feature.tasbih.domain.TasbihSessionTransition
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single write path for every Tasbih surface.
 *
 * Phone UI and Glance both call this coordinator so daily counters and durable
 * Room session history stay aligned. Session-history failures are deliberately
 * non-fatal: a persistence issue must never discard the user's visible tap.
 */
@Singleton
class TasbihActionCoordinator @Inject constructor(
    private val counterRepository: TasbihRepository,
    private val sessionRepository: TasbihSessionRepository,
) {

    suspend fun increment(
        phrase: TasbihPhrase,
        target: Int,
    ): TasbihSessionTransition? {
        counterRepository.increment(phrase)
        return runCatching { sessionRepository.increment(phrase, target) }
            .onFailure { Log.w(TAG, "Could not persist tasbih session increment", it) }
            .getOrNull()
    }

    suspend fun decrement(phrase: TasbihPhrase, target: Int) {
        counterRepository.decrement(phrase)
        runCatching { sessionRepository.decrement(phrase, target) }
            .onFailure { Log.w(TAG, "Could not persist tasbih session decrement", it) }
    }

    suspend fun reset(phrase: TasbihPhrase) {
        closeSessionBestEffort(TasbihSessionEndReason.Reset)
        counterRepository.reset(phrase)
    }

    suspend fun resetAll() {
        closeSessionBestEffort(TasbihSessionEndReason.ResetAll)
        counterRepository.resetAll()
    }

    suspend fun setTarget(target: Int) {
        closeSessionBestEffort(TasbihSessionEndReason.ContextChanged)
        counterRepository.setTarget(target)
    }

    suspend fun setPhrase(phrase: TasbihPhrase) {
        closeSessionBestEffort(TasbihSessionEndReason.ContextChanged)
        counterRepository.setPhrase(phrase)
    }

    private suspend fun closeSessionBestEffort(reason: TasbihSessionEndReason) {
        runCatching { sessionRepository.endActive(reason) }
            .onFailure { Log.w(TAG, "Could not close active tasbih session", it) }
    }

    companion object {
        private const val TAG = "TasbihCoordinator"
    }
}
