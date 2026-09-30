package org.muslim.app.feature.quran.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.muslim.app.feature.quran.data.QuranPrefsRepository
import org.muslim.app.feature.quran.domain.Ayah
import org.muslim.app.feature.quran.domain.QuranRepository

/** Independent explanation cursor: audio scrolling cannot overwrite manual tafsir selection. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class QuranReaderSupplementCursor(
    private val repository: QuranRepository,
    private val preferences: QuranPrefsRepository,
    private val scope: CoroutineScope,
) {
    private val reading = MutableStateFlow<Ayah?>(null)
    val followPlayback = preferences.supplementFollowPlayback
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), true)

    fun recordReading(ayah: Ayah) { reading.value = ayah }

    fun ayah(playing: Flow<Int?>) = combine(reading, playing, preferences.supplementFollowPlayback) { manual, audio, follow ->
        supplementTargetGlobal(manual?.globalNumber, audio, follow)
    }.flatMapLatest { global -> flow { emit(global?.let { repository.ayahByGlobal(it) }) } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    fun setFollow(enabled: Boolean, playing: Int?, current: Ayah?) = scope.launch {
        if (!enabled) reading.value = playing?.let { repository.ayahByGlobal(it) } ?: current
        preferences.setSupplementFollowPlayback(enabled)
    }
}

internal fun supplementTargetGlobal(manual: Int?, playing: Int?, follow: Boolean): Int? =
    if (follow) playing ?: manual else manual
