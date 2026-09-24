package org.muslim.app.feature.prayertimes.ui.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.IslamicListItem
import org.muslim.app.core.ui.theme.IslamicPrimaryButton
import org.muslim.app.core.ui.theme.IslamicSecondaryButton
import org.muslim.app.core.ui.theme.MuslimAppScaffold
import org.muslim.app.core.ui.theme.MuslimContentFrame
import org.muslim.app.feature.prayertimes.R
import org.muslim.app.feature.prayertimes.domain.City

@Composable
fun LocationScreen(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = hiltViewModel(),
) {
    val results by viewModel.results.collectAsStateWithLifecycle()
    val message by viewModel.messages.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }

    val invalidText = stringResource(R.string.location_invalid)
    val gpsDeniedText = stringResource(R.string.location_gps_denied)
    val gpsFailedText = stringResource(R.string.location_gps_failed)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.useGps() else viewModel.gpsDenied()
    }

    LaunchedEffect(message) {
        val current = message ?: return@LaunchedEffect
        when (current) {
            is LocationViewModel.Message.Saved -> {
                viewModel.consumeMessage()
                onSaved()
            }
            is LocationViewModel.Message.Error -> {
                val text = when (current.text) {
                    "invalid" -> invalidText
                    "gps_denied" -> gpsDeniedText
                    else -> gpsFailedText
                }
                snackbarHostState.showSnackbar(text)
                viewModel.consumeMessage()
            }
        }
    }

    MuslimAppScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        MuslimContentFrame(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().padding(IslamicSpacing.Medium)) {
            OutlinedTextField(
                value = viewModel.searchQuery.collectAsStateWithLifecycle().value,
                onValueChange = { viewModel.searchQuery.value = it },
                label = { Text(stringResource(R.string.location_search_hint)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(IslamicSpacing.Small))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(results, key = { it.name }) { city ->
                    CityRow(city) { viewModel.selectCity(city) }
                }
            }

            IslamicSecondaryButton(
                onClick = {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted || coarseGranted) {
                        viewModel.useGps()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(Modifier.padding(start = IslamicSpacing.Small))
                Text(stringResource(R.string.location_use_gps))
            }

            Spacer(Modifier.height(IslamicSpacing.Small))
            Text(
                text = stringResource(R.string.location_manual),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = latitudeText,
                    // Normalize digits so Arabic-Indic/Persian keyboard digits
                    // never get rejected by toDoubleOrNull later.
                    onValueChange = { latitudeText = org.muslim.app.core.common.text.Digits.toWesternDigits(it) },
                    label = { Text(stringResource(R.string.location_latitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.padding(start = IslamicSpacing.Small))
                OutlinedTextField(
                    value = longitudeText,
                    // Normalize digits so Arabic-Indic/Persian keyboard digits
                    // never get rejected by toDoubleOrNull later.
                    onValueChange = { longitudeText = org.muslim.app.core.common.text.Digits.toWesternDigits(it) },
                    label = { Text(stringResource(R.string.location_longitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(IslamicSpacing.Small))
                IslamicPrimaryButton(
                    onClick = { viewModel.saveManual(latitudeText, longitudeText) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.location_save))
                }
            }
        }
    }
}

@Composable
private fun CityRow(city: City, onClick: () -> Unit) {
    IslamicListItem(
        title = city.displayName,
        subtitle = city.country,
        onClick = onClick,
        modifier = Modifier.padding(vertical = IslamicSpacing.XSmall),
    )
}
