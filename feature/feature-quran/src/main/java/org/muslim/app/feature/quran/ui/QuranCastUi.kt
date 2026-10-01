package org.muslim.app.feature.quran.ui

import androidx.compose.runtime.Composable
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
