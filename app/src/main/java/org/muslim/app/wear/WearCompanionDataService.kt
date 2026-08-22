package org.muslim.app.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.muslim.app.core.common.wear.WearSyncContract
import org.muslim.app.core.datastore.AppPreferencesRepository
import org.muslim.app.feature.tasbih.data.TasbihRepository
import javax.inject.Inject

/**
 * Accepts only the versioned tasbih-increment event from the same signed Wear
 * companion. It ignores all traffic until the user enables companion sync.
 */
@AndroidEntryPoint
class WearCompanionDataService : WearableListenerService() {

    @Inject lateinit var appPreferencesRepository: AppPreferencesRepository
    @Inject lateinit var tasbihRepository: TasbihRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (!WearSyncContract.isSupportedIncrementPath(messageEvent.path)) return
        scope.launch {
            val preferences = appPreferencesRepository.preferences.first()
            if (!preferences.wearCompanionEnabled) return@launch
            val state = tasbihRepository.state.first()
            tasbihRepository.increment(state.phrase)
        }
    }
}
