package org.muslim.app.feature.quran.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import android.view.ContextThemeWrapper
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import org.muslim.app.feature.quran.R
import org.muslim.app.feature.quran.domain.QuranCastPayload

/** Activity-owned Cast sender hook; keeps app navigation APIs independent of Cast. */
val LocalQuranCastPayloadSink = staticCompositionLocalOf<(QuranCastPayload?) -> Unit> { {} }
val LocalQuranCastPlaybackSink = staticCompositionLocalOf<(Boolean?) -> Unit> { {} }
val LocalQuranCastConnected = staticCompositionLocalOf { false }
val LocalQuranCastDeviceName = staticCompositionLocalOf<String?> { null }
val LocalQuranCastError = staticCompositionLocalOf<String?> { null }
val LocalQuranCastStartSink = staticCompositionLocalOf<(QuranCastPayload, org.muslim.app.feature.quran.data.RecitationPlaybackSnapshot) -> Unit> { { _, _ -> } }

@Composable
internal fun rememberCastSessionId(context: Context): String = remember(context) {
    java.util.UUID.randomUUID().toString()
}

@Composable
internal fun rememberCastSequence(sessionId: String, payload: QuranCastPayload?): Long {
    val sequence = remember(sessionId) { mutableLongStateOf(0L) }
    LaunchedEffect(sessionId, payload?.sequenceContentKey()) { sequence.longValue += 1L }
    return sequence.longValue
}

@Composable
internal fun QuranCastButton() {
    AndroidView(
        modifier = Modifier.size(48.dp),
        factory = { viewContext ->
            val castButtonContext = ContextThemeWrapper(viewContext, R.style.ThemeOverlay_Muslim_CastButton)
            MediaRouteButton(castButtonContext).apply {
                CastButtonFactory.setUpMediaRouteButton(castButtonContext, this)
            }
        },
    )
}
