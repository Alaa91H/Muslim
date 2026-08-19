# -*- coding: utf-8 -*-
"""Add a global per-reciter download summary flow to QuranDownloadsViewModel."""
import io

p = "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranDownloadsViewModel.kt"
s = io.open(p, encoding="utf-8").read()


def rep(old, new, count=1):
    global s
    n = s.count(old)
    if n < count:
        print("MISS (%d/%d): %r" % (n, count, old[:80]))
        return False
    s = s.replace(old, new, count)
    print("OK: %r" % old[:70])
    return True


ok = True

# Add totalSummary right after refreshReciterState().
ok &= rep(
    """    /** Force a rescan (after a delete or a finished download). */
    fun refreshReciterState() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }
""",
    """    /** Force a rescan (after a delete or a finished download). */
    fun refreshReciterState() {
        _refreshTrigger.value = _refreshTrigger.value + 1
    }

    /**
     * Totals across every reciter: how much audio is downloaded on disk.
     * Re-scanned whenever a download finishes/fails or a delete happens
     * (the same trigger that refreshes the per-reciter state).
     */
    val totalSummary: StateFlow<TotalDownloadSummary> = _refreshTrigger
        .flatMapLatest { _ ->
            flow { emit(summarizeAllReciters()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TotalDownloadSummary(0, 0, 0L))

    private suspend fun summarizeAllReciters(): TotalDownloadSummary {
        var ayahs = 0L
        val surahs = mutableSetOf<Int>()
        var bytes = 0L
        for (reciter in Reciter.Bundled) {
            val state = recitationRepository.downloadState(reciter.id)
            ayahs += state.downloadedAyahs
            surahs += state.surahCounts.keys
            bytes += state.totalBytes
        }
        return TotalDownloadSummary(ayahs, surahs.size, bytes)
    }
""",
)

# Append the data class at the end of the file (after the companion object's closing brace).
ok &= rep(
    """    private companion object {
        const val TOTAL_AYAHS = 6236L
    }
}
""",
    """    private companion object {
        const val TOTAL_AYAHS = 6236L
    }
}

/** How much recitation audio is downloaded across all reciters, at a glance. */
data class TotalDownloadSummary(
    val downloadedAyahs: Long,
    /** Distinct surahs covered by at least one reciter (union). */
    val downloadedSurahs: Int,
    val totalBytes: Long,
)
""",
)

io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("ALL_OK" if ok else "SOME_MISSED")
