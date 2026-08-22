package org.muslim.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.muslim.app.core.permissions.AppPermission
import org.muslim.app.core.permissions.PermissionManager
import org.muslim.app.feature.quran.domain.QuranRepository
import org.muslim.app.feature.quran.domain.Surah
import java.text.Normalizer
import javax.inject.Inject

/** A destination returned from one user-triggered spoken navigation command. */
sealed interface VoiceNavigationTarget {
    data class Route(val route: String) : VoiceNavigationTarget
    data class Reader(val surahNumber: Int) : VoiceNavigationTarget
}

/**
 * Converts a small, documented Arabic/English command vocabulary into navigation targets.
 * The recognizer candidates are never persisted; this class retains only locally bundled
 * surah metadata so commands such as "اقرأ سورة الكهف" can resolve to the reader.
 */
class VoiceCommandMatcher {

    fun match(candidates: List<String>, surahs: List<Surah>): VoiceNavigationTarget? =
        candidates.firstNotNullOfOrNull { candidate -> matchOne(candidate, surahs) }

    private fun matchOne(raw: String, surahs: List<Surah>): VoiceNavigationTarget? {
        val command = normalize(raw)
        val surah = surahTarget(command, surahs)
        if (surah != null) return VoiceNavigationTarget.Reader(surah)
        return destinations.firstOrNull { phrase -> command.contains(phrase.token) }
            ?.let { VoiceNavigationTarget.Route(it.route) }
    }

    private fun surahTarget(command: String, surahs: List<Surah>): Int? {
        val asksForQuran = listOf(
            "سورة", "اقرا", "القران", "quran", "surah", "read",
        ).any(command::contains)
        if (!asksForQuran) return null
        val spokenNumber = numberPattern.find(command)?.value?.toIntOrNull()
        spokenNumber?.takeIf { it in 1..114 }?.let { return it }
        return surahs.firstOrNull { surah ->
            command.contains(normalize(surah.arabicName)) ||
                command.contains(normalize(surah.englishName)) ||
                command.contains(normalize(surah.translation))
        }?.number
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace(diacritics, "")
            .replace(arabicDiacritics, "")
            .replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace(nonWord, " ")
            .replace(multipleSpaces, " ")
            .trim()

    private data class Phrase(val token: String, val route: String)

    private companion object {
        val diacritics = Regex("\\p{M}+")
        val arabicDiacritics = Regex("[\\u064B-\\u065F\\u0670]")
        val nonWord = Regex("[^\\p{L}\\p{N}]+")
        val multipleSpaces = Regex("\\s+")
        val numberPattern = Regex("\\b(\\d{1,3})\\b")
        val destinations = listOf(
            Phrase("الرئيسية", "home"),
            Phrase("home", "home"),
            Phrase("اوقات الصلاة", "home"),
            Phrase("prayer times", "home"),
            Phrase("القران", "quran"),
            Phrase("quran", "quran"),
            Phrase("القبلة", "qibla"),
            Phrase("qibla", "qibla"),
            Phrase("المزيد", "more"),
            Phrase("more", "more"),
            Phrase("الاذكار", "adhkar"),
            Phrase("adhkar", "adhkar"),
            Phrase("المسبحة", "tasbih"),
            Phrase("tasbih", "tasbih"),
            Phrase("التعلم", "learn"),
            Phrase("learn", "learn"),
            Phrase("الاعدادات", "settings"),
            Phrase("settings", "settings"),
            Phrase("الوصول", "accessibility"),
            Phrase("accessibility", "accessibility"),
            Phrase("الحديث", "hadith"),
            Phrase("hadith", "hadith"),
            Phrase("رمضان", "ramadan"),
            Phrase("ramadan", "ramadan"),
            Phrase("الزكاة", "zakat"),
            Phrase("zakat", "zakat"),
            Phrase("المالية", "finance"),
            Phrase("finance", "finance"),
            Phrase("النورانية", "learn/noorani-new-muslim"),
            Phrase("noorani", "learn/noorani-new-muslim"),
            Phrase("المسافر", "learn/traveler-expat"),
            Phrase("traveler", "learn/traveler-expat"),
            Phrase("التاريخ", "history"),
            Phrase("history", "history"),
            Phrase("المراجع", "reference"),
            Phrase("reference", "reference"),
            Phrase("الحج", "learn/hajj-umrah"),
            Phrase("hajj", "learn/hajj-umrah"),
            Phrase("اسماء الله", "learn/names-of-allah"),
            Phrase("names of allah", "learn/names-of-allah"),
            Phrase("الجنائز", "learn/funeral-will"),
            Phrase("funerals", "learn/funeral-will"),
            Phrase("محاسبة النفس", "habits"),
            Phrase("habits", "habits"),
        )
    }
}

@HiltViewModel
class VoiceNavigationViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    quranRepository: QuranRepository,
) : ViewModel() {

    private val matcher = VoiceCommandMatcher()

    val surahs: StateFlow<List<Surah>> = quranRepository.observeSurahs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun microphonePermission(): String? = permissionManager.runtimeRequestArray(AppPermission.Microphone)?.firstOrNull()

    fun microphoneGranted(): Boolean = permissionManager.isGranted(AppPermission.Microphone)

    fun match(candidates: List<String>): VoiceNavigationTarget? = matcher.match(candidates, surahs.value)
}
