package org.muslim.app.feature.tasbih.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.muslim.app.core.database.AppDatabase
import org.muslim.app.core.database.entity.TasbihSessionEntity
import org.muslim.app.feature.tasbih.domain.TasbihPhrase
import org.muslim.app.feature.tasbih.domain.TasbihSessionConfig
import org.muslim.app.feature.tasbih.domain.TasbihSessionEndReason
import org.muslim.app.feature.tasbih.domain.TasbihSessionEngine
import org.muslim.app.feature.tasbih.domain.TasbihSessionHistoryItem
import org.muslim.app.feature.tasbih.domain.TasbihSessionMode
import org.muslim.app.feature.tasbih.domain.TasbihSessionState
import org.muslim.app.feature.tasbih.domain.TasbihSessionTransition
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Durable session-history store for Tasbih.
 *
 * The current UI continues to expose the lightweight daily counter from
 * DataStore, while every continuous counting run is mirrored into Room for
 * statistics, recovery and future phone/Wear synchronization.
 */
@Singleton
class TasbihSessionRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dao = AppDatabase.getInstance(context).tasbihSessionDao()
    private val mutationMutex = Mutex()

    fun observeRecent(limit: Int = DEFAULT_HISTORY_LIMIT): Flow<List<TasbihSessionHistoryItem>> =
        dao.observeRecent(limit.coerceIn(1, MAX_HISTORY_LIMIT))
            .map { sessions -> sessions.map { it.toHistoryItem() } }

    suspend fun increment(
        phrase: TasbihPhrase,
        target: Int,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): TasbihSessionTransition = mutationMutex.withLock {
        val config = currentUiConfig(phrase, target)
        val active = dao.getActive()
        val crossedDayBoundary = active?.let {
            !isSameLocalDay(it.lastUpdatedAtEpochMillis, nowEpochMillis)
        } ?: false

        val sessionWithId = if (active == null || crossedDayBoundary || !active.matches(config)) {
            if (active != null) {
                val reason = if (crossedDayBoundary) {
                    TasbihSessionEndReason.DayChanged
                } else {
                    TasbihSessionEndReason.ContextChanged
                }
                val endAt = if (crossedDayBoundary) {
                    active.lastUpdatedAtEpochMillis
                } else {
                    nowEpochMillis
                }
                dao.endActive(endAt, reason.storageId)
            }
            val fresh = TasbihSessionEngine.start(config, nowEpochMillis)
            val id = dao.insert(fresh.toEntity())
            SessionWithId(id = id, state = fresh)
        } else {
            SessionWithId(id = active.id, state = active.toDomainState())
        }

        val transition = TasbihSessionEngine.increment(sessionWithId.state, nowEpochMillis)
        dao.update(
            transition.state.toEntity(
                id = sessionWithId.id,
                endReason = if (transition.goalCompleted) {
                    TasbihSessionEndReason.GoalReached
                } else {
                    null
                },
            )
        )
        transition
    }

    suspend fun decrement(
        phrase: TasbihPhrase,
        target: Int,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ) = mutationMutex.withLock {
        val active = dao.getActive() ?: return@withLock
        val config = currentUiConfig(phrase, target)
        if (!active.matches(config)) return@withLock

        val next = TasbihSessionEngine.decrement(active.toDomainState(), nowEpochMillis)
        dao.update(next.toEntity(id = active.id))
    }

    suspend fun endActive(
        reason: TasbihSessionEndReason,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ) = mutationMutex.withLock {
        dao.endActive(nowEpochMillis, reason.storageId)
    }

    private fun isSameLocalDay(firstEpochMillis: Long, secondEpochMillis: Long): Boolean {
        val zone = ZoneId.systemDefault()
        val first = Instant.ofEpochMilli(firstEpochMillis).atZone(zone).toLocalDate()
        val second = Instant.ofEpochMilli(secondEpochMillis).atZone(zone).toLocalDate()
        return first == second
    }

    private fun currentUiConfig(phrase: TasbihPhrase, target: Int) =
        TasbihSessionConfig(
            phraseId = phrase.storageId,
            // The existing screen intentionally keeps counting after each
            // target. Free mode preserves that behaviour while the engine still
            // reports every completed round based on [target].
            mode = TasbihSessionMode.Free,
            target = target.coerceIn(1, 100_000),
            roundsGoal = 1,
        )

    private fun TasbihSessionEntity.matches(config: TasbihSessionConfig): Boolean =
        phraseId == config.phraseId &&
            mode == config.mode.storageId &&
            target == config.target &&
            roundsGoal == config.roundsGoal &&
            endedAtEpochMillis == null

    private fun TasbihSessionEntity.toDomainState(): TasbihSessionState =
        TasbihSessionState(
            config = TasbihSessionConfig(
                phraseId = phraseId,
                mode = TasbihSessionMode.fromStorageId(mode),
                target = target.coerceAtLeast(1),
                roundsGoal = roundsGoal.coerceAtLeast(1),
            ),
            count = count.coerceAtLeast(0L),
            startedAtEpochMillis = startedAtEpochMillis,
            lastUpdatedAtEpochMillis = lastUpdatedAtEpochMillis,
            completedAtEpochMillis = if (endReason == TasbihSessionEndReason.GoalReached.storageId) {
                endedAtEpochMillis
            } else {
                null
            },
        )

    private fun TasbihSessionState.toEntity(
        id: Long = 0L,
        endReason: TasbihSessionEndReason? = null,
    ): TasbihSessionEntity =
        TasbihSessionEntity(
            id = id,
            phraseId = config.phraseId,
            mode = config.mode.storageId,
            target = config.target,
            roundsGoal = config.roundsGoal,
            count = count,
            startedAtEpochMillis = startedAtEpochMillis,
            lastUpdatedAtEpochMillis = lastUpdatedAtEpochMillis,
            endedAtEpochMillis = completedAtEpochMillis,
            endReason = endReason?.storageId,
        )

    private fun TasbihSessionEntity.toHistoryItem(): TasbihSessionHistoryItem =
        TasbihSessionHistoryItem(
            id = id,
            phraseId = phraseId,
            mode = TasbihSessionMode.fromStorageId(mode),
            target = target,
            roundsGoal = roundsGoal,
            count = count.coerceAtLeast(0L),
            startedAtEpochMillis = startedAtEpochMillis,
            lastUpdatedAtEpochMillis = lastUpdatedAtEpochMillis,
            endedAtEpochMillis = endedAtEpochMillis,
            endReason = TasbihSessionEndReason.fromStorageId(endReason),
        )

    private data class SessionWithId(
        val id: Long,
        val state: TasbihSessionState,
    )

    companion object {
        private const val DEFAULT_HISTORY_LIMIT = 50
        private const val MAX_HISTORY_LIMIT = 500
    }
}
