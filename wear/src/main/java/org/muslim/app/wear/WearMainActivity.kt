package org.muslim.app.wear

import android.app.Activity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.core.content.edit
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import org.muslim.app.core.common.wear.WearPrayerSnapshot
import org.muslim.app.core.common.wear.WearSyncContract
import java.text.DateFormat
import java.util.Date

/**
 * A compact, paired-phone Wear OS companion. It never calculates prayer times
 * or stores location: it renders the latest snapshot the phone explicitly
 * synchronized and relays a single tasbih increment when tapped.
 */
class WearMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearCompanionApp() }
    }
}

@Suppress("LongMethod")
@Composable
private fun WearCompanionApp() {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf(WearSnapshotStore.read(context)) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var hapticsEnabled by remember {
        mutableStateOf(
            context.getSharedPreferences(HAPTICS_FILE, Activity.MODE_PRIVATE)
                .getBoolean(HAPTICS_ENABLED, true),
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            snapshot = WearSnapshotStore.read(context)
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(org.muslim.app.wear.R.string.wear_next_prayer),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            PrayerOverview(snapshot = snapshot, nowMillis = nowMillis)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(org.muslim.app.wear.R.string.wear_tasbih),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            snapshot?.let { state ->
                Text(
                    text = state.tasbihPhrase,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(
                        org.muslim.app.wear.R.string.wear_tasbih_count,
                        state.tasbihCount,
                        state.tasbihTarget,
                    ),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = {
                        if (hapticsEnabled) {
                            (context as? Activity)?.window?.decorView?.performHapticFeedback(
                                HapticFeedbackConstants.CONFIRM,
                            )
                        }
                        requestTasbihIncrement(context)
                    },
                ) {
                    Text(text = stringResource(org.muslim.app.wear.R.string.wear_increment))
                }
            } ?: Text(
                text = stringResource(org.muslim.app.wear.R.string.wear_sync_waiting),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    hapticsEnabled = !hapticsEnabled
                    context.getSharedPreferences(HAPTICS_FILE, Activity.MODE_PRIVATE).edit {
                        putBoolean(HAPTICS_ENABLED, hapticsEnabled)
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        if (hapticsEnabled) {
                            org.muslim.app.wear.R.string.wear_vibration_on
                        } else {
                            org.muslim.app.wear.R.string.wear_vibration_off
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun PrayerOverview(snapshot: WearPrayerSnapshot?, nowMillis: Long) {
    val nextPrayerName = snapshot?.nextPrayerName
    val nextPrayerAt = snapshot?.nextPrayerAtEpochMillis
    if (nextPrayerName == null || nextPrayerAt == null) {
        Text(text = stringResource(org.muslim.app.wear.R.string.wear_no_prayer))
        return
    }
    Text(
        text = nextPrayerName,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(nextPrayerAt)),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        text = stringResource(
            org.muslim.app.wear.R.string.wear_countdown,
            formatCountdown((nextPrayerAt - nowMillis).coerceAtLeast(0L)),
        ),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

private fun requestTasbihIncrement(context: android.content.Context) {
    Wearable.getCapabilityClient(context)
        .getCapability(WearSyncContract.CAPABILITY, CapabilityClient.FILTER_REACHABLE)
        .addOnSuccessListener { capability ->
            capability.nodes.forEach { node ->
                Wearable.getMessageClient(context).sendMessage(
                    node.id,
                    WearSyncContract.TASBIH_INCREMENT_PATH,
                    byteArrayOf(),
                )
            }
        }
}

private fun formatCountdown(millis: Long): String {
    val totalSeconds = millis / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    return if (hours > 0) "%d:%02d".format(hours, minutes) else "%d:%02d".format(minutes, totalSeconds % 60L)
}

private const val HAPTICS_FILE = "wear_haptics"
private const val HAPTICS_ENABLED = "enabled"
