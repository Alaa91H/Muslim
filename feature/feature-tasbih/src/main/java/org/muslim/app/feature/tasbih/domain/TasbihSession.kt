package org.muslim.app.feature.tasbih.domain

/**
 * Counting modes supported by the shared session engine.
 *
 * The UI, widgets and Wear OS can all drive the same pure engine, preventing
 * each surface from developing slightly different counting behaviour.
 */
enum class TasbihSessionMode(val storageId: String) {
    Free("free"),
    Target("target"),
    Rounds("rounds");

    companion object {
        fun fromStorageId(value: String?): TasbihSessionMode =
            entries.firstOrNull { it.storageId == value } ?: Free
    }
}

enum class TasbihSessionStatus {
    Active,
    Completed,
}

enum class TasbihSessionEndReason(val storageId: String) {
    GoalReached("goal_reached"),
    ContextChanged("context_changed"),
    Reset("reset"),
    ResetAll("reset_all"),
    Manual("manual");

    companion object {
        fun fromStorageId(value: String?): TasbihSessionEndReason? =
            entries.firstOrNull { it.storageId == value }
    }
}

data class TasbihSessionHistoryItem(
    val id: Long,
    val phraseId: String,
    val mode: TasbihSessionMode,
    val target: Int,
    val roundsGoal: Int,
    val count: Long,
    val startedAtEpochMillis: Long,
    val lastUpdatedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val endReason: TasbihSessionEndReason?,
) {
    val isActive: Boolean get() = endedAtEpochMillis == null

    val durationMillis: Long
        get() = ((endedAtEpochMillis ?: lastUpdatedAtEpochMillis) - startedAtEpochMillis)
            .coerceAtLeast(0L)
}

data class TasbihSessionConfig(
    val phraseId: String,
    val mode: TasbihSessionMode = TasbihSessionMode.Target,
    val target: Int = 33,
    val roundsGoal: Int = 1,
) {
    init {
        require(phraseId.isNotBlank()) { "phraseId must not be blank" }
        require(target > 0) { "target must be greater than zero" }
        require(roundsGoal > 0) { "roundsGoal must be greater than zero" }
    }

    val goalCount: Long?
        get() = when (mode) {
            TasbihSessionMode.Free -> null
            TasbihSessionMode.Target -> target.toLong()
            TasbihSessionMode.Rounds -> target.toLong() * roundsGoal.toLong()
        }
}

data class TasbihSessionState(
    val config: TasbihSessionConfig,
    val count: Long,
    val startedAtEpochMillis: Long,
    val lastUpdatedAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null,
) {
    val status: TasbihSessionStatus
        get() = if (completedAtEpochMillis == null) TasbihSessionStatus.Active else TasbihSessionStatus.Completed

    val completedRounds: Long
        get() = count / config.target

    val goalReached: Boolean
        get() = config.goalCount?.let { count >= it } ?: false

    val progress: Float
        get() {
            val goal = config.goalCount ?: return 0f
            if (goal <= 0L) return 0f
            return (count.toDouble() / goal.toDouble()).coerceIn(0.0, 1.0).toFloat()
        }
}

data class TasbihSessionTransition(
    val state: TasbihSessionState,
    val roundCompleted: Boolean = false,
    val goalCompleted: Boolean = false,
)

/**
 * Pure counting state machine. Keeping this Android-free makes the exact same
 * rules reusable by Compose, Glance and Wear OS and makes fast-tap behaviour
 * straightforward to unit test.
 */
object TasbihSessionEngine {

    fun start(config: TasbihSessionConfig, nowEpochMillis: Long): TasbihSessionState =
        TasbihSessionState(
            config = config,
            count = 0L,
            startedAtEpochMillis = nowEpochMillis,
            lastUpdatedAtEpochMillis = nowEpochMillis,
        )

    fun increment(session: TasbihSessionState, nowEpochMillis: Long): TasbihSessionTransition {
        if (session.status == TasbihSessionStatus.Completed) {
            return TasbihSessionTransition(session)
        }

        val newCount = session.count + 1L
        val completedRound = newCount % session.config.target.toLong() == 0L
        val goalCompleted = session.config.goalCount?.let { newCount >= it } ?: false
        val next = session.copy(
            count = newCount,
            lastUpdatedAtEpochMillis = nowEpochMillis,
            completedAtEpochMillis = if (goalCompleted) nowEpochMillis else null,
        )
        return TasbihSessionTransition(
            state = next,
            roundCompleted = completedRound,
            goalCompleted = goalCompleted,
        )
    }

    fun decrement(session: TasbihSessionState, nowEpochMillis: Long): TasbihSessionState =
        session.copy(
            count = (session.count - 1L).coerceAtLeast(0L),
            lastUpdatedAtEpochMillis = nowEpochMillis,
            completedAtEpochMillis = null,
        )
}
