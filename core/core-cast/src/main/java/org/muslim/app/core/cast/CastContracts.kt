package org.muslim.app.core.cast

import android.net.Uri
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.cast.framework.SessionManager

/** Narrow Cast framework boundary; features depend on this module, not the SDK scattered through UI. */
class CastContextFacade(private val context: CastContext) {
    val currentSession: CastSession? get() = context.sessionManager.currentCastSession
    val sessionManager: SessionManager get() = context.sessionManager
}

interface CastSessionHandle {
    val deviceName: String?
    val mediaClient: RemoteMediaClient?
    fun send(namespace: String, message: String)
    fun stop()
}

interface RemoteMediaHandle {
    val positionMs: Long
    val durationMs: Long
    val isPlaying: Boolean
    fun play()
    fun pause()
    fun stop()
    fun seek(positionMs: Long)
    fun load(contentUri: Uri, contentType: String, autoplay: Boolean, positionMs: Long)
}
