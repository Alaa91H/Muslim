package org.muslim.app.core.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.muslim.app.core.designsystem.IslamicRadius
import org.muslim.app.core.designsystem.MuslimDarkColors
import org.muslim.app.core.designsystem.MuslimLightColors
import org.muslim.app.core.designsystem.MuslimSepiaColors

/**
 * Internal Compose showcase for reviewing the Islamic design system in light,
 * dark and reader-paper contexts. It is deliberately not registered as an app
 * destination, so it cannot disrupt the established navigation or reader UX.
 */
@Composable
fun IslamicDesignShowcase(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Modern Islamic Minimalism",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        DesignShowcaseSurface(title = "Light", colorScheme = MuslimLightColors)
        DesignShowcaseSurface(title = "Dark", colorScheme = MuslimDarkColors)
        DesignShowcaseSurface(title = "Mushaf paper", colorScheme = MuslimSepiaColors)
    }
}

@Composable
private fun DesignShowcaseSurface(title: String, colorScheme: ColorScheme) {
    MaterialTheme(colorScheme = colorScheme, shapes = IslamicShapes) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IslamicOrnamentImage(
                    ornament = IslamicOrnament.SurahHeader,
                    tint = MaterialTheme.colorScheme.tertiary,
                    alpha = IslamicOrnamentOpacity.LightSection,
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                )
                IslamicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Surah header",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Quiet surface • clear hierarchy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = IslamicRadius.AyahMarker,
                        ) {
                            Text(
                                text = "١",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IslamicPrimaryButton(onClick = {}) { Text("Continue") }
                    IslamicSecondaryButton(onClick = {}) { Text("Settings") }
                }
                IslamicOrnamentImage(
                    ornament = IslamicOrnament.MushafDivider,
                    tint = MaterialTheme.colorScheme.tertiary,
                    alpha = IslamicOrnamentOpacity.LightBackground,
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun IslamicDesignShowcasePreview() {
    AppTheme(dynamicColor = false) {
        IslamicDesignShowcase()
    }
}
