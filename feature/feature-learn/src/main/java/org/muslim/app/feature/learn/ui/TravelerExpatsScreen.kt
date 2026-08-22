package org.muslim.app.feature.learn.ui

import android.hardware.SensorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.location.MagneticDeclination
import org.muslim.app.core.permissions.AppPermission
import org.muslim.app.core.permissions.PermissionEntryPoint
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.HighLatitudeRuleInfo
import org.muslim.app.feature.learn.domain.TravelContent
import org.muslim.app.feature.learn.domain.TravelDistanceStatus
import org.muslim.app.feature.learn.domain.TravelDistanceThreshold
import org.muslim.app.feature.learn.domain.TravelGuideSection
import org.muslim.app.feature.learn.domain.TravelPoint
import org.muslim.app.feature.qibla.domain.QiblaCalculator
import org.muslim.app.feature.qibla.ui.rememberCompassHeading
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Education and local technical aids for travellers and expats. It deliberately
 * distinguishes GPS/calculation output from individual fiqh rulings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelerExpatsScreen(
    onBack: () -> Unit,
    onOpenPrayerSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TravelerExpatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.any { it }) viewModel.refreshGps()
    }
    fun requestGps() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PermissionEntryPoint::class.java,
        )
        val manager = entryPoint.permissionManager()
        if (manager.isGranted(AppPermission.Location)) {
            viewModel.refreshGps()
        } else {
            permissionLauncher.launch(manager.runtimeRequestArray(AppPermission.Location) ?: arrayOf())
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.traveler_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.learn_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TravelerTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
            when (selectedTab) {
                0 -> TravelDistanceTab(
                    state = state,
                    onRefresh = ::requestGps,
                    onSetOrigin = viewModel::setCurrentAsOrigin,
                    onClearOrigin = viewModel::clearOrigin,
                    onSelectThreshold = viewModel::selectThreshold,
                )
                1 -> TransportPrayerTab(state = state, onRefresh = ::requestGps)
                else -> HighLatitudeTab(state = state, onOpenPrayerSettings = onOpenPrayerSettings)
            }
        }
    }
}

@Composable
private fun TravelerTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.traveler_tab_distance),
        stringResource(R.string.traveler_tab_transport),
        stringResource(R.string.traveler_tab_high_latitude),
    )
    PrimaryTabRow(selectedTabIndex = selectedTab) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelect(index) },
                text = { Text(label, maxLines = 2, textAlign = TextAlign.Center) },
            )
        }
    }
}

@Composable
private fun TravelDistanceTab(
    state: TravelerUiState,
    onRefresh: () -> Unit,
    onSetOrigin: () -> Unit,
    onClearOrigin: () -> Unit,
    onSelectThreshold: (TravelDistanceThreshold) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { TravelNotice() }
        item { GpsControls(state, onRefresh, onSetOrigin, onClearOrigin) }
        item { ThresholdSelector(selected = state.threshold, onSelect = onSelectThreshold) }
        item { DistanceAssessmentCard(state) }
        item { TravelGuides(TravelContent.travelSections) }
    }
}

@Composable
private fun TravelNotice() {
    InfoCard(
        text = stringResource(R.string.traveler_distance_notice),
        icon = Icons.Filled.Info,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

@Composable
private fun GpsControls(
    state: TravelerUiState,
    onRefresh: () -> Unit,
    onSetOrigin: () -> Unit,
    onClearOrigin: () -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(stringResource(R.string.traveler_gps_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.traveler_gps_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 5.dp),
            )
            OutlinedButton(
                onClick = onRefresh,
                enabled = state.gpsState != TravelerGpsState.Requesting,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.traveler_refresh_gps))
            }
            GpsStatus(state.gpsState)
            OriginStatus(origin = state.origin)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                Button(
                    onClick = onSetOrigin,
                    enabled = state.gpsState is TravelerGpsState.Fix,
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.traveler_set_departure))
                }
                OutlinedButton(onClick = onClearOrigin, enabled = state.origin != null) {
                    Text(stringResource(R.string.traveler_clear_departure))
                }
            }
        }
    }
}

@Composable
private fun GpsStatus(gpsState: TravelerGpsState) {
    val message = when (gpsState) {
        TravelerGpsState.Idle -> stringResource(R.string.traveler_gps_idle)
        TravelerGpsState.Requesting -> stringResource(R.string.traveler_gps_requesting)
        is TravelerGpsState.Fix -> stringResource(R.string.traveler_gps_ready)
        TravelerGpsState.Error -> stringResource(R.string.traveler_gps_error)
    }
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = if (gpsState is TravelerGpsState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun OriginStatus(origin: TravelPoint?) {
    val text = if (origin == null) {
        stringResource(R.string.traveler_departure_missing)
    } else {
        stringResource(R.string.traveler_departure_ready, origin.latitude, origin.longitude)
    }
    Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
}

@Composable
private fun ThresholdSelector(
    selected: TravelDistanceThreshold,
    onSelect: (TravelDistanceThreshold) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(stringResource(R.string.traveler_threshold_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.traveler_threshold_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                TravelDistanceThreshold.entries.forEach { threshold ->
                    AssistChip(
                        onClick = { onSelect(threshold) },
                        label = { Text("${threshold.kilometres.toInt()} km") },
                        leadingIcon = if (threshold == selected) {
                            { Icon(Icons.Filled.LocationOn, contentDescription = null) }
                        } else {
                            null
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (threshold == selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceAssessmentCard(state: TravelerUiState) {
    val assessment = state.distanceAssessment
    if (assessment == null) {
        InfoCard(
            text = stringResource(R.string.traveler_distance_waiting),
            icon = Icons.Filled.LocationOn,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val statusText = when (assessment.status) {
        TravelDistanceStatus.BELOW_REFERENCE -> stringResource(
            R.string.traveler_distance_below,
            assessment.threshold.kilometres.toInt(),
        )
        TravelDistanceStatus.AT_OR_ABOVE_REFERENCE -> stringResource(
            R.string.traveler_distance_above,
            assessment.threshold.kilometres.toInt(),
        )
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.traveler_distance_label, assessment.distanceKm),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = stringResource(R.string.traveler_distance_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun TravelGuides(sections: List<TravelGuideSection>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sections.forEach { section -> TravelGuideCard(section, isArabicLocale()) }
    }
}

@Composable
private fun TravelGuideCard(section: TravelGuideSection, isArabic: Boolean) {
    Card {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(section.title.resolve(isArabic), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            section.paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph.resolve(isArabic),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun TransportPrayerTab(state: TravelerUiState, onRefresh: () -> Unit) {
    val currentPoint = (state.gpsState as? TravelerGpsState.Fix)?.point ?: state.origin
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            InfoCard(
                text = stringResource(R.string.traveler_transport_notice),
                icon = Icons.Filled.Flight,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        item { OfflineQiblaCompass(point = currentPoint, onRefresh = onRefresh, gpsState = state.gpsState) }
        item { TransportHeader() }
        item { TravelGuides(TravelContent.transportSections) }
    }
}

@Composable
private fun TransportHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Flight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.traveler_plane), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Filled.Train, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.traveler_train), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OfflineQiblaCompass(
    point: TravelPoint?,
    onRefresh: () -> Unit,
    gpsState: TravelerGpsState,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.traveler_offline_compass_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.traveler_offline_compass_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (point == null) {
                OutlinedButton(onClick = onRefresh, enabled = gpsState != TravelerGpsState.Requesting, modifier = Modifier.padding(top = 12.dp)) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.traveler_refresh_gps))
                }
            } else {
                CompassDial(point)
            }
        }
    }
}

@Composable
private fun CompassDial(point: TravelPoint) {
    val context = LocalContext.current
    val heading by rememberCompassHeading()
    val declination = remember(point) {
        MagneticDeclination.declinationDegrees(context, point.latitude, point.longitude)
    }
    val trueHeading = (heading.heading + declination + 360f) % 360f
    val bearing = QiblaCalculator.direction(point.latitude, point.longitude)
    val turn = QiblaCalculator.relativeToTrueNorth(bearing, trueHeading.toDouble())
    val outlineColor = MaterialTheme.colorScheme.outline
    val northColor = MaterialTheme.colorScheme.error
    val qiblaColor = Color(0xFFD4A017)
    val centerColor = MaterialTheme.colorScheme.primary
    val directionText = when {
        turn < 4.0 || turn > 356.0 -> stringResource(R.string.traveler_compass_facing)
        turn <= 180.0 -> stringResource(R.string.traveler_compass_turn_right, turn.roundToInt())
        else -> stringResource(R.string.traveler_compass_turn_left, (360 - turn).roundToInt())
    }
    Canvas(modifier = Modifier.size(190.dp).padding(top = 14.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension * 0.40f
        drawCircle(outlineColor, radius, center, style = Stroke(width = 3.dp.toPx()))
        drawLine(
            color = northColor,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y - radius * 0.64f),
            strokeWidth = 6.dp.toPx(),
        )
        val relativeRadians = (turn - 0.0) * PI / 180.0
        val qiblaEnd = Offset(
            center.x + (sin(relativeRadians) * radius * 0.84f).toFloat(),
            center.y - (cos(relativeRadians) * radius * 0.84f).toFloat(),
        )
        drawLine(
            color = qiblaColor,
            start = center,
            end = qiblaEnd,
            strokeWidth = 8.dp.toPx(),
        )
        drawCircle(centerColor, radius = 7.dp.toPx(), center = center)
    }
    Text(
        text = stringResource(R.string.traveler_compass_bearing, bearing),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Text(directionText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
    if (heading.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
        Text(
            text = stringResource(R.string.traveler_compass_calibrate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun HighLatitudeTab(state: TravelerUiState, onOpenPrayerSettings: () -> Unit) {
    val preview = state.highLatitudePreview
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item { HighLatitudeNotice() }
        item { HighLatitudePreviewCard(preview = preview, onOpenPrayerSettings = onOpenPrayerSettings) }
        item { HighLatitudeBandCard(preview?.latitude) }
        items(TravelContent.highLatitudeRules, key = { it.rule.name }) { rule ->
            HighLatitudeRuleCard(rule = rule, isSelected = preview?.rule == rule.rule)
        }
    }
}

@Composable
private fun HighLatitudeNotice() {
    InfoCard(
        text = stringResource(R.string.traveler_high_latitude_notice),
        icon = Icons.Filled.Info,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

@Composable
private fun HighLatitudePreviewCard(preview: HighLatitudePreview?, onOpenPrayerSettings: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(stringResource(R.string.traveler_high_latitude_preview), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (preview == null) {
                Text(stringResource(R.string.traveler_high_latitude_location_missing), modifier = Modifier.padding(top = 8.dp))
            } else if (!preview.calculationAvailable) {
                Text(stringResource(R.string.traveler_high_latitude_unavailable), modifier = Modifier.padding(top = 8.dp))
            } else {
                Text(
                    stringResource(R.string.traveler_high_latitude_location, preview.latitude),
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(stringResource(R.string.traveler_high_latitude_fajr, preview.fajr.orEmpty()), modifier = Modifier.padding(top = 4.dp))
                Text(stringResource(R.string.traveler_high_latitude_isha, preview.isha.orEmpty()), modifier = Modifier.padding(top = 2.dp))
            }
            OutlinedButton(onClick = onOpenPrayerSettings, modifier = Modifier.padding(top = 12.dp)) {
                Icon(Icons.Filled.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.traveler_high_latitude_settings))
            }
        }
    }
}

@Composable
private fun HighLatitudeBandCard(latitude: Double?) {
    val text = when {
        latitude == null -> stringResource(R.string.traveler_high_latitude_band_missing)
        abs(latitude) < 48.0 -> stringResource(R.string.traveler_high_latitude_band_normal)
        abs(latitude) < 66.0 -> stringResource(R.string.traveler_high_latitude_band_second)
        else -> stringResource(R.string.traveler_high_latitude_band_third)
    }
    InfoCard(
        text = text,
        icon = Icons.Filled.Info,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun HighLatitudeRuleCard(rule: HighLatitudeRuleInfo, isSelected: Boolean) {
    Card(colors = CardDefaults.cardColors(
        containerColor = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
    )) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(rule.title.resolve(isArabicLocale()), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(rule.description.resolve(isArabicLocale()), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
            if (isSelected) {
                Text(
                    stringResource(R.string.traveler_high_latitude_selected),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    contentColor: Color,
) {
    Card(colors = CardDefaults.cardColors(containerColor = color), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun isArabicLocale(): Boolean = LocalConfiguration.current.locales[0]?.language == "ar"
