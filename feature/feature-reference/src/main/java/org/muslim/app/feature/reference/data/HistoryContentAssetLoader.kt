package org.muslim.app.feature.reference.data

import android.content.Context
import kotlinx.serialization.json.Json

internal class HistoryContentAssetLoader(
    private val context: Context,
) {
    fun load(): HistoryContentAsset {
        val payload = context.assets
            .open(ASSET_PATH)
            .bufferedReader()
            .use { it.readText() }
        return HistoryContentJson.decodeFromString(
            HistoryContentAsset.serializer(),
            payload,
        ).validated()
    }

    companion object {
        private const val ASSET_PATH = "history/content_v1.json"
    }
}

internal val HistoryContentJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}
