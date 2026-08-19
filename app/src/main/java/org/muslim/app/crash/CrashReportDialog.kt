package org.muslim.app.crash

import android.content.Context
import android.content.Intent
import android.os.Process
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.muslim.app.R

/**
 * The single, elegant error surface for the whole app. It observes
 * [AppErrorBus] and therefore reacts to both paths:
 *
 *  - recoverable errors emitted by the app-wide coroutine handler (Continue
 *    keeps the app running), and
 *  - a fatal crash re-read from [CrashLogStore] after the auto-relaunch
 *    (Restart / Close).
 */
@Composable
fun CrashReportDialog() {
    val error by AppErrorBus.current.collectAsState()
    val current = error ?: return
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!current.fatal) AppErrorBus.dismiss() },
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text(stringResource(R.string.crash_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 360.dp),
            ) {
                Text(
                    text = stringResource(
                        if (current.fatal) R.string.crash_fatal_message
                        else R.string.crash_recoverable_message,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (current.detail.isNotBlank()) {
                    Text(
                        text = current.detail,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        },
        confirmButton = {
            if (current.fatal) {
                TextButton(onClick = { restartApp(context) }) {
                    Text(stringResource(R.string.crash_restart))
                }
            } else {
                TextButton(onClick = { AppErrorBus.dismiss() }) {
                    Text(stringResource(R.string.crash_dismiss))
                }
            }
        },
        dismissButton = {
            if (current.fatal) {
                TextButton(onClick = { closeApp() }) {
                    Text(stringResource(R.string.crash_close))
                }
            }
        },
    )
}

private fun restartApp(context: Context) {
    CrashLogStore.clear(context)
    AppErrorBus.dismiss()
    runCatching {
        val intent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ?: return
        context.startActivity(intent)
    }
}

private fun closeApp() {
    Process.killProcess(Process.myPid())
    Runtime.getRuntime().exit(0)
}
