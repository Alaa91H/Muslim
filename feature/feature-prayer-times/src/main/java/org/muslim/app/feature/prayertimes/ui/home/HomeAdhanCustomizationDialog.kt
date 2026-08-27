package org.muslim.app.feature.prayertimes.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.muslim.app.core.common.prayer.AdhanSoundOption
import org.muslim.app.core.common.prayer.BundledAdhanSound
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.feature.prayertimes.ui.settings.AdhanCustomization
import org.muslim.app.feature.prayertimes.ui.settings.AdhanCustomizationActions
import org.muslim.app.feature.prayertimes.ui.settings.AdhanCustomizeDialog
import org.muslim.app.feature.prayertimes.ui.settings.PrayerSettingsViewModel

/**
 * Direct, modal entry point for one prayer's Adhan controls on the home screen.
 * It shares the settings ViewModel's atomic save and preview APIs but never
 * navigates away from the prayer-times screen.
 */
@Composable
internal fun HomeAdhanCustomizationDialog(
    prayer: Prayer,
    onDismiss: () -> Unit,
    viewModel: PrayerSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val density by viewModel.informationDensity.collectAsStateWithLifecycle()
    AdhanCustomizeDialog(
        prayer = prayer,
        density = density,
        onDensityChange = viewModel::setInformationDensity,
        initial = AdhanCustomization(
            option = settings.adhanSounds[prayer] ?: AdhanSoundOption.Default,
            sound = BundledAdhanSound.fromId(
                settings.bundledAdhanSounds[prayer] ?: BundledAdhanSound.DEFAULT_ID,
            ),
            volume = settings.adhanVolumeFor(prayer),
            vibrate = settings.vibrateFor(prayer),
            adjustmentMinutes = settings.adjustments[prayer],
            useGlobalVolume = settings.useGlobalAdhanVolume,
        ),
        actions = AdhanCustomizationActions(
            onPreview = { sound, volume -> viewModel.previewBundled(prayer, sound, volume) },
            onLiveVolume = viewModel::setLivePreviewVolume,
            onDismiss = {
                viewModel.stopPreview()
                onDismiss()
            },
            onConfirm = { selected ->
                viewModel.saveAdhanCustomization(
                    prayer = prayer,
                    option = selected.option,
                    bundledSound = selected.sound,
                    volume = selected.volume,
                    vibrate = selected.vibrate,
                    adjustmentMinutes = selected.adjustmentMinutes,
                    useGlobalVolume = selected.useGlobalVolume,
                )
                viewModel.stopPreview()
                onDismiss()
            },
        ),
    )
}
