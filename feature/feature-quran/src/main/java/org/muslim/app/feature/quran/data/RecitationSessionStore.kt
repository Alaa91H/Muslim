package org.muslim.app.feature.quran.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.quranRecitationSessionDataStore by
    preferencesDataStore(name = "quran_recitation_session")

@Serializable
data class RecitationSessionIntent(
    val reciterId: String,
    val surahNumber: Int,
    val globalNumbers: List<Int>,
    val repeatCount: Int,
    val continuous: Boolean,
    val advanceToNext: Boolean,
    val toEndOfQuran: Boolean,
)

@Serializable
data class PersistedRecitationSession(
    val intent: RecitationSessionIntent,
    val currentGlobalNumber: Int,
    val positionMs: Long,
    val wasPlaying: Boolean,
    val savedAtEpochMs: Long,
)

internal fun PersistedRecitationSession.asRestorableOrNull(): PersistedRecitationSession? {
    val request = intent
    if (request.reciterId.isBlank()) return null
    if (request.surahNumber !in 1..114) return null
    if (request.globalNumbers.isEmpty()) return null
    if (currentGlobalNumber !in request.globalNumbers) return null
    if (positionMs < 0L) return null
    return this
}

internal fun PersistedRecitationSession.remainingGlobalNumbers(): List<Int> {
    val index = intent.globalNumbers.indexOf(currentGlobalNumber)
    return if (index >= 0) intent.globalNumbers.drop(index) else intent.globalNumbers
}

@Singleton
class RecitationSessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val session: Flow<PersistedRecitationSession?> =
        context.quranRecitationSessionDataStore.data.map { prefs ->
            prefs[Keys.SESSION]
                ?.let { raw ->
                    runCatching {
                        json.decodeFromString<PersistedRecitationSession>(raw)
                    }.getOrNull()
                }
                ?.asRestorableOrNull()
        }

    suspend fun save(session: PersistedRecitationSession) {
        context.quranRecitationSessionDataStore.edit { prefs ->
            prefs[Keys.SESSION] = json.encodeToString(
                PersistedRecitationSession.serializer(),
                session,
            )
        }
    }

    suspend fun clear() {
        context.quranRecitationSessionDataStore.edit { prefs ->
            prefs.remove(Keys.SESSION)
        }
    }

    private object Keys {
        val SESSION = stringPreferencesKey("active_session")
    }
}

/**
 * Process-local owner of the active recitation intent.
 *
 * The durable snapshot lives in [RecitationSessionStore]. This runtime holder
 * intentionally does not auto-start playback after process recreation: a new
 * process has no active intent, while the persisted snapshot remains available
 * for an explicit user resume from the Quran reader.
 */
@Singleton
class RecitationSessionRuntime @Inject constructor(
    private val store: RecitationSessionStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val persistMutex = Mutex()

    @Volatile
    private var activeIntent: RecitationSessionIntent? = null

    @Volatile
    private var generation = 0L

    @Volatile
    private var operationSequence = 0L

    @Synchronized
    fun begin(intent: RecitationSessionIntent) {
        activeIntent = intent
        generation += 1L
        operationSequence += 1L
        val writeGeneration = generation
        val operation = operationSequence
        val firstGlobal = intent.globalNumbers.firstOrNull() ?: return
        val snapshot = PersistedRecitationSession(
            intent = intent,
            currentGlobalNumber = firstGlobal,
            positionMs = 0L,
            wasPlaying = false,
            savedAtEpochMs = System.currentTimeMillis(),
        )
        scope.launch {
            persistMutex.withLock {
                if (
                    writeGeneration == generation &&
                    operation == operationSequence &&
                    activeIntent == intent
                ) {
                    store.save(snapshot)
                }
            }
        }
    }

    @Synchronized
    fun persist(
        currentGlobalNumber: Int?,
        positionMs: Long,
        state: PlaybackState,
    ) {
        val intent = activeIntent ?: return
        val global = currentGlobalNumber ?: return
        if (global !in intent.globalNumbers || state == PlaybackState.Idle) return

        operationSequence += 1L
        val writeGeneration = generation
        val operation = operationSequence
        val snapshot = PersistedRecitationSession(
            intent = intent,
            currentGlobalNumber = global,
            positionMs = positionMs.coerceAtLeast(0L),
            wasPlaying = state == PlaybackState.Playing,
            savedAtEpochMs = System.currentTimeMillis(),
        )
        scope.launch {
            persistMutex.withLock {
                if (
                    writeGeneration == generation &&
                    operation == operationSequence &&
                    activeIntent == intent
                ) {
                    store.save(snapshot)
                }
            }
        }
    }

    @Synchronized
    fun clear() {
        activeIntent = null
        generation += 1L
        operationSequence += 1L
        val clearGeneration = generation
        val operation = operationSequence
        scope.launch {
            persistMutex.withLock {
                if (
                    clearGeneration == generation &&
                    operation == operationSequence &&
                    activeIntent == null
                ) {
                    store.clear()
                }
            }
        }
    }
}
