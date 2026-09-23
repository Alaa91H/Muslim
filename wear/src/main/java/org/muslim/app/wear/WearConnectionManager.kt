package org.muslim.app.wear

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.muslim.app.core.common.wear.WearPrayerSnapshot
import org.muslim.app.core.common.wear.WearSyncContract

/**
 * NexaFlow-style Wear Data Layer connection helper.
 *
 * The watch restores the newest durable DataItem first, resolves the phone by
 * its advertised capability, falls back to any connected node during upgrades,
 * and performs bounded retries only for the idempotent sync request.
 */
internal object WearConnectionManager {

    suspend fun refreshFromPhone(context: Context): Boolean = withContext(Dispatchers.IO) {
        refreshCachedSnapshot(context)
        val baselineSync = WearSnapshotStore.read(context)?.syncedAtEpochMillis ?: 0L
        var retryDelay = 0L

        repeat(MAX_SYNC_ATTEMPTS) {
            if (retryDelay > 0L) delay(retryDelay)

            if (sendMessage(context, WearSyncContract.SYNC_REQUEST_PATH)) {
                repeat(RESPONSE_POLLS) {
                    delay(RESPONSE_POLL_MS)
                    refreshCachedSnapshot(context)
                    val latest = WearSnapshotStore.read(context)?.syncedAtEpochMillis ?: 0L
                    if (latest > baselineSync) return@withContext true
                }
            }

            retryDelay = when (retryDelay) {
                0L -> INITIAL_RETRY_DELAY_MS
                else -> (retryDelay * 2L).coerceAtMost(MAX_RETRY_DELAY_MS)
            }
        }

        Log.d(TAG, "Fresh Wear sync not confirmed; keeping last cached snapshot")
        false
    }

    /**
     * Tasbih increment is intentionally never retried because repeating a
     * delivered command would increment the counter twice.
     */
    suspend fun sendTasbihIncrement(context: Context): Boolean =
        withContext(Dispatchers.IO) {
            sendMessage(context, WearSyncContract.TASBIH_INCREMENT_PATH)
        }

    private suspend fun refreshCachedSnapshot(context: Context): WearPrayerSnapshot? {
        val dataClient = Wearable.getDataClient(context)
        val uri = Uri.parse("wear://*${WearSyncContract.DATA_PATH}")
        val buffer = runCatching {
            dataClient.getDataItems(uri, DataClient.FILTER_LITERAL).await()
        }.getOrElse { error ->
            Log.w(TAG, "Unable to read cached Wear snapshot", error)
            return null
        }

        return try {
            buffer.mapNotNull { item ->
                runCatching {
                    wearSnapshotFrom(DataMapItem.fromDataItem(item).dataMap)
                }.getOrNull()
            }
                .filter(WearPrayerSnapshot::isValid)
                .maxByOrNull { it.syncedAtEpochMillis }
                ?.also { WearSnapshotStore.save(context, it) }
        } finally {
            buffer.release()
        }
    }

    private suspend fun sendMessage(context: Context, path: String): Boolean {
        val nodeId = resolvePhoneNodeId(context) ?: return false
        return runCatching {
            Wearable.getMessageClient(context)
                .sendMessage(nodeId, path, ByteArray(0))
                .await()
            true
        }.getOrElse { error ->
            Log.w(TAG, "Wear message failed on $path for node $nodeId", error)
            false
        }
    }

    /**
     * Prefer the explicit phone capability; fall back to connected nodes so an
     * older phone build can still recover while capability metadata propagates.
     */
    private suspend fun resolvePhoneNodeId(context: Context): String? {
        val capabilityNodes = runCatching {
            Wearable.getCapabilityClient(context)
                .getCapability(
                    WearSyncContract.CAPABILITY_PHONE_APP,
                    CapabilityClient.FILTER_REACHABLE,
                )
                .await()
                .nodes
        }.getOrDefault(emptySet())

        capabilityNodes.firstOrNull { it.isNearby }?.let { return it.id }
        capabilityNodes.firstOrNull()?.let { return it.id }

        return runCatching {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            nodes.firstOrNull { it.isNearby }?.id ?: nodes.firstOrNull()?.id
        }.getOrNull()
    }

    private const val TAG = "WearConnectionManager"
    private const val MAX_SYNC_ATTEMPTS = 4
    private const val RESPONSE_POLLS = 5
    private const val RESPONSE_POLL_MS = 300L
    private const val INITIAL_RETRY_DELAY_MS = 500L
    private const val MAX_RETRY_DELAY_MS = 2_000L
}
