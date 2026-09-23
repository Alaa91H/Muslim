package org.muslim.app.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.common.wear.WearSyncContract
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.feature.tasbih.data.TasbihRepository

/**
 * Receives versioned commands from the same signed Wear companion. Traffic is
 * ignored until the user explicitly enables companion synchronization.
 */
@AndroidEntryPoint
class WearCompanionDataService : WearableListenerService() {

    @Inject lateinit var appPreferencesRepository: AppPreferencesRepository
    @Inject lateinit var tasbihRepository: TasbihRepository
    @Inject lateinit var wearCompanionPublisher: WearCompanionPublisher

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearSyncContract.TASBIH_INCREMENT_PATH,
            WearSyncContract.SYNC_REQUEST_PATH -> Unit
            else -> return
        }

        scope.launch {
            val preferences = appPreferencesRepository.preferences.first()
            if (!preferences.wearCompanionEnabled) return@launch

            when (messageEvent.path) {
                WearSyncContract.TASBIH_INCREMENT_PATH -> {
                    val state = tasbihRepository.state.first()
                    tasbihRepository.increment(state.phrase)
                }
                WearSyncContract.SYNC_REQUEST_PATH -> wearCompanionPublisher.pushNow()
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
