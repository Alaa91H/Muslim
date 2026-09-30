package org.muslim.app.feature.prayertimes.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.designsystem.IslamicSpacing
import org.muslim.app.core.ui.theme.MuslimScreen
import org.muslim.app.core.ui.theme.MuslimStateSurface
import org.muslim.app.core.ui.theme.MuslimTopBar
import org.muslim.app.feature.prayertimes.R

/** Independent monthly timetable: no today's schedule or next-prayer hero. */
@Composable
fun MonthlyPrayerScreen(
    onBack: () -> Unit,
    onSelectLocation: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val use24h by viewModel.use24h.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.setMonthly(true) }
    MuslimScreen(topBar = {
        MuslimTopBar(
            title = stringResource(R.string.times_monthly),
            onNavigateBack = onBack,
            navigationContentDescription = stringResource(R.string.prayer_navigate_back),
        )
    }) {
        Column(Modifier.fillMaxSize().padding(IslamicSpacing.Medium)) {
            val location = if (state.hasLocation) state.locationName else stringResource(R.string.home_select_location)
            PrayerDateHeader(
                state = state,
                locationLabel = location,
                locationDescription = stringResource(R.string.home_location_action, location),
                compact = false,
                onSelectLocation = onSelectLocation,
                onPrevious = viewModel::previousPeriod,
                onNext = viewModel::nextPeriod,
            )
            when {
                !state.hasLocation -> MuslimStateSurface(title = stringResource(R.string.home_location_unknown))
                !state.isValid -> MuslimStateSurface(title = stringResource(R.string.home_cannot_compute))
                state.monthly -> MonthlyTimetable(state, use24h)
            }
        }
    }
}
