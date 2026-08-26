package org.muslim.app.feature.settings

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * User-controlled cross-platform settings. Neither watch sync nor the home
 * bridge is enabled by default; the latter needs a complete HTTPS endpoint and
 * sends only a minimal adhan-start event when local audible playback begins.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartDevicesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    var endpoint by remember(preferences.smartHomeBridgeEndpoint) {
        mutableStateOf(preferences.smartHomeBridgeEndpoint)
    }
    var token by rememberSaveable { mutableStateOf("") }
    val endpointValid = isCompleteHttpsEndpoint(endpoint)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.smart_devices_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.smart_devices_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.smart_devices_wear_title)) },
                    supportingContent = { Text(stringResource(R.string.smart_devices_wear_desc)) },
                    trailingContent = {
                        Switch(
                            checked = preferences.wearCompanionEnabled,
                            onCheckedChange = viewModel::setWearCompanionEnabled,
                        )
                    },
                )
            }
            item {
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(R.string.smart_devices_auto_title)) },
                    supportingContent = { Text(stringResource(R.string.smart_devices_auto_desc)) },
                )
            }
            item {
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(stringResource(R.string.smart_devices_home_title)) },
                    supportingContent = { Text(stringResource(R.string.smart_devices_home_desc)) },
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { endpoint = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.smart_devices_home_endpoint)) },
                        isError = endpoint.isNotBlank() && !endpointValid,
                    )
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.smart_devices_home_token)) },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    Button(
                        onClick = {
                            viewModel.setSmartHomeBridgeEndpoint(endpoint)
                            viewModel.saveSmartHomeBridgeToken(token)
                            endpoint = endpoint.trim()
                            token = ""
                        },
                        enabled = endpointValid,
                    ) {
                        Text(stringResource(R.string.smart_devices_home_save))
                    }
                    TextButton(onClick = viewModel::clearSmartHomeBridgeToken) {
                        Text(stringResource(R.string.smart_devices_home_clear_token))
                    }
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.smart_devices_home_privacy)) },
                        supportingContent = { Text(stringResource(R.string.smart_devices_home_https_hint)) },
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.smart_devices_home_title)) },
                        supportingContent = { Text(stringResource(R.string.smart_devices_home_desc)) },
                        trailingContent = {
                            Switch(
                                checked = preferences.smartHomeBridgeEnabled,
                                enabled = endpointValid,
                                onCheckedChange = viewModel::setSmartHomeBridgeEnabled,
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun isCompleteHttpsEndpoint(raw: String): Boolean {
    val uri = runCatching { Uri.parse(raw.trim()) }.getOrNull() ?: return false
    return uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
}
