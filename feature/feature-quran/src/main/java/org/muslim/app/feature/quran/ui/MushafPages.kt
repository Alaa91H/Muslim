package org.muslim.app.feature.quran.ui

import org.muslim.app.feature.quran.domain.Ayah

/** Canonical pages include all surahs sharing a page; never slice at a surah boundary. */
internal fun mushafPages(ayahs: List<Ayah>): List<Map.Entry<Int, List<Ayah>>> =
    ayahs.sortedBy { it.globalNumber }.groupBy { it.page }.toSortedMap().entries.toList()

internal fun initialMushafPageIndex(
    pages: List<Map.Entry<Int, List<Ayah>>>,
    surah: Int,
    targetGlobal: Int,
): Int = pages.indexOfFirst { (_, ayahs) ->
    if (targetGlobal > 0) ayahs.any { it.globalNumber == targetGlobal }
    else ayahs.any { it.surahNumber == surah }
}.coerceAtLeast(0)
