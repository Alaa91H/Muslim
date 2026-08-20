package org.muslim.app.feature.tasbih.domain

import java.time.LocalDate
import kotlin.math.max

/** Broad grouping of dhikr phrases for the professional selector UI. */
enum class TasbihCategory(val id: String, val label: String) {
    Tasbeeh("tasbeeh", "التسبيح"),
    Tahmeed("tahmeed", "التحميد"),
    TakbeerTahleel("takbeer_tahleel", "التكبير والتهليل"),
    Istighfar("istighfar", "الاستغفار"),
    Salawat("salawat", "الصلاة على النبي ﷺ"),
    General("general", "أذكار جامعة"),
}

/**
 * The dhikr phrase currently being counted, with its transliteration and the
 * authentic virtue (فضل) behind it. Each phrase keeps an independent daily
 * counter, so switching phrases never loses progress.
 */
enum class TasbihPhrase(
    val text: String,
    val transliteration: String,
    val virtue: String,
    val category: TasbihCategory,
) {
    SubhanAllah(
        "سُبْحَانَ اللَّهِ",
        "Subhan Allah",
        "«سبحان الله والحمد لله تملآن ما بين السماء والأرض» (رواه مسلم)",
        TasbihCategory.Tasbeeh,
    ),
    SubhanAllahiWaBihamdihi(
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
        "Subhan Allahi wa bihamdihi",
        "«من قالها مئة مرة حُطَّت خطاياه وإن كانت مثل زبد البحر» (متفق عليه)",
        TasbihCategory.Tasbeeh,
    ),
    SubhanAllahilAzimWaBihamdihi(
        "سُبْحَانَ اللَّهِ الْعَظِيمِ وَبِحَمْدِهِ",
        "Subhan Allahil-'Azim wa bihamdihi",
        "«كلمتان حبيبتان إلى الرحمن، خفيفتان على اللسان، ثقيلتان في الميزان» (متفق عليه)",
        TasbihCategory.Tasbeeh,
    ),
    SubhanAllahiAdadaKhalqihi(
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ وَرِضَا نَفْسِهِ وَزِنَةَ عَرْشِهِ وَمِدَادَ كَلِمَاتِهِ",
        "Subhan Allahi wa bihamdihi 'adada khalqihi wa rida nafsihi wa zinata 'arshihi wa midada kalimatihi",
        "«لقد قلتُ بعدكِ كلمات لو وُزنت بما قلتِ لوزنتهن» (رواه مسلم)",
        TasbihCategory.Tasbeeh,
    ),
    Alhamdulillah(
        "الْحَمْدُ لِلَّهِ",
        "Alhamdulillah",
        "«الحمد لله تملأ الميزان» (رواه مسلم)",
        TasbihCategory.Tahmeed,
    ),
    AlhamdulillahiHamdanKathiran(
        "الْحَمْدُ لِلَّهِ حَمْدًا كَثِيرًا طَيِّبًا مُبَارَكًا فِيهِ",
        "Alhamdulillahi hamdan kathiran tayyiban mubarakan fihi",
        "«رأيتُ بضعة عشر ملكًا يبتدرونها أيهم يرفعها» (رواه الترمذي)",
        TasbihCategory.Tahmeed,
    ),
    AlhamdulillahiBinimatihi(
        "الْحَمْدُ لِلَّهِ الَّذِي بِنِعْمَتِهِ تَتِمُّ الصَّالِحَاتُ",
        "Alhamdulillahil-ladhi bini'matihi tatimmu-s-salihat",
        "«كان النبي ﷺ إذا أراد أن يقوم من المجلس قالها» (رواه ابن ماجه)",
        TasbihCategory.Tahmeed,
    ),
    AlhamdulillahiHamdanYuwafi(
        "الْحَمْدُ لِلَّهِ حَمْدًا يُوَافِي نِعَمَهُ وَيُكَافِئُ مَزِيدَهُ",
        "Alhamdulillahi hamdan yuwafi ni'amahu wa yukafi'u mazidahu",
        "من أذكار الحمد الجامعة الثابتة عن السلف",
        TasbihCategory.Tahmeed,
    ),
    AllahuAkbar(
        "اللَّهُ أَكْبَرُ",
        "Allahu akbar",
        "من تكبير الله وتعظيمه؛ «اللَّهُ أَكْبَرُ كَبِيرًا»",
        TasbihCategory.TakbeerTahleel,
    ),
    LaIlahaIllaAllah(
        "لَا إِلَهَ إِلَّا اللَّهُ",
        "La ilaha illa Allah",
        "«أفضل الذكر» (رواه الترمذي وحسنه)",
        TasbihCategory.TakbeerTahleel,
    ),
    LaIlahaIllaAllahWahdahu(
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        "La ilaha illa Allahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadir",
        "«من قالها عشرًا كان كمن أعتق أربعة أنفس من ولد إسماعيل» (متفق عليه)",
        TasbihCategory.TakbeerTahleel,
    ),
    LaIlahaIllaAllahYuhyiWaYumit(
        "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، يُحْيِي وَيُمِيتُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
        "La ilaha illa Allahu wahdahu la sharika lahu, lahul-mulku wa lahul-hamdu, yuhyi wa yumitu wa huwa 'ala kulli shay'in qadir",
        "«من قالها عشرًا كتب الله له بها عشر حسنات» (رواه الترمذي)",
        TasbihCategory.TakbeerTahleel,
    ),
    Astaghfirullah(
        "أَسْتَغْفِرُ اللَّهَ",
        "Astaghfirullah",
        "«من لزم الاستغفار جعل الله له من كل همٍّ فرجًا ومن كل ضيقٍ مخرجًا» (رواه أبو داود)",
        TasbihCategory.Istighfar,
    ),
    AstaghfirullahaWaAtubuIlaih(
        "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
        "Astaghfirullaha wa atubu ilayhi",
        "«كان النبي ﷺ يتوب إلى الله في اليوم مئة مرة» (رواه مسلم)",
        TasbihCategory.Istighfar,
    ),
    RabbiGhfirliWaTubAlayya(
        "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ",
        "Rabbi-ghfir li wa tub 'alayya innaka anta-t-Tawwabu-r-Rahim",
        "«كان ﷺ يقولها في المجلس الواحد مئة مرة» (رواه الترمذي وصححه)",
        TasbihCategory.Istighfar,
    ),
    AllahummaGhfirliWarhamni(
        "اللَّهُمَّ اغْفِرْ لِي وَارْحَمْنِي وَتُبْ عَلَيَّ",
        "Allahumma-ghfir li wa-rhamni wa tub 'alayya",
        "دعاء جامع من جوامع الأدعية",
        TasbihCategory.Istighfar,
    ),
    AllahummaSalliAlaMuhammad(
        "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
        "Allahumma salli wa sallim 'ala nabiyyina Muhammad",
        "«من صلى عليَّ صلاةً صلى الله عليه بها عشرًا» (رواه مسلم)",
        TasbihCategory.Salawat,
    ),
    AllahummaSalliAlaMuhammadWaAlaAlihi(
        "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ",
        "Allahumma salli 'ala Muhammadin wa 'ala ali Muhammad",
        "«كما صليت على إبراهيم وعلى آل إبراهيم» (متفق عليه)",
        TasbihCategory.Salawat,
    ),
    SalawatJamiah(
        "اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ عَلَى نَبِيِّنَا مُحَمَّدٍ وَعَلَى آلِهِ وَصَحْبِهِ أَجْمَعِينَ",
        "Allahumma salli wa sallim wa barik 'ala nabiyyina Muhammadin wa 'ala alihi wa sahbihi ajma'in",
        "صيغة جامعة في الصلاة والسلام على النبي ﷺ وآله وصحبه",
        TasbihCategory.Salawat,
    ),
    LaHawlaWalaQuwwata(
        "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
        "La hawla wa la quwwata illa billah",
        "«كنز من كنوز الجنة» (متفق عليه)",
        TasbihCategory.General,
    ),
    Hasbiyallahu(
        "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
        "Hasbiyallahu la ilaha illa huwa 'alayhi tawakkaltu wa huwa Rabbul-'arshil-'azim",
        "«من قالها سبع مرات كفاه الله ما أهمه من أمر الدنيا والآخرة» (رواه أبو داود)",
        TasbihCategory.General,
    ),
    AudhuBikalimatillah(
        "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
        "A'udhu bikalimatillahi-t-tammati min sharri ma khalaq",
        "«من قالها ثلاث مرات لم تضره حمة تلك الليلة» (رواه مسلم)",
        TasbihCategory.General,
    ),
    BismillahilladhiLaYadurru(
        "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
        "Bismillahil-ladhi la yadurru ma'a-smihi shay'un fil-ardi wa la fis-sama'i wa huwa-s-Sami'ul-'Alim",
        "«من قالها ثلاثًا صباحًا ومساءً لم يضره شيء» (رواه الترمذي وحسنه)",
        TasbihCategory.General,
    ),
    YaHayyuYaQayyum(
        "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ",
        "Ya Hayyu ya Qayyumu bi-rahmatika astaghith",
        "«كان ﷺ يقولها عند الكرب» (رواه الترمذي وحسنه)",
        TasbihCategory.General,
    ),
}

/** One day's total count, for the history chart. */
data class DailyCount(
    val date: LocalDate,
    val count: Int,
)

/**
 * Sound playback preferences when a full round of tasbih is completed:
 * a toggle plus which of the system tones to play.
 */
data class TargetSoundSettings(
    val enabled: Boolean = false,
    /** Kept for backward compatibility with older persisted data. */
    val tone: String = TONE_NOTIFICATION,
) {
    companion object {
        const val TONE_NOTIFICATION = "notification"
    }
}

/** Persisted misbaha state with an independent counter per phrase. */
data class TasbihState(
    val counts: Map<TasbihPhrase, Int>,
    val target: Int,
    val phrase: TasbihPhrase,
    val history: List<DailyCount>,
) {
    /** Count for the currently selected phrase. */
    val count: Int get() = counts[phrase] ?: 0

    /** Sum of every phrase's counter today. */
    val totalToday: Int get() = counts.values.sum()

    /** Number of completed full rounds for the active phrase. */
    val rounds: Int get() = if (target > 0) count / target else 0

    /** Whether the current target has been reached. */
    val targetReached: Boolean get() = target > 0 && count >= target
}

/**
 * Pure counter logic (PROJECT_PROMPT.md §6 Phase 4): tap-to-count with a
 * configurable target and a daily/weekly history. Counters are independent
 * per phrase, so switching dhikr does not lose progress.
 *
 * Kept as pure functions so the daily roll-over and undo behaviour are
 * unit-testable without Android dependencies.
 */
object TasbihCounter {

    /** Counts to display for [today]: the stored map, or empty after a day change. */
    fun effectiveCounts(
        storedCounts: Map<TasbihPhrase, Int>,
        storedDate: LocalDate,
        today: LocalDate,
    ): Map<TasbihPhrase, Int> = if (storedDate == today) storedCounts else emptyMap()

    /**
     * Applies one tap on [phrase]. When the date changed, the previous day's
     * total (across all phrases) is rolled into [history] (newest first,
     * trimmed to [historyLimit]) and a fresh day starts.
     */
    fun increment(
        storedCounts: Map<TasbihPhrase, Int>,
        storedDate: LocalDate,
        today: LocalDate,
        history: List<DailyCount>,
        phrase: TasbihPhrase,
        historyLimit: Int = 30,
    ): IncrementResult {
        return if (storedDate == today) {
            IncrementResult(
                counts = storedCounts + (phrase to (storedCounts[phrase] ?: 0) + 1),
                date = today,
                history = history,
            )
        } else {
            val total = storedCounts.values.sum()
            val rolled = if (total > 0) {
                (listOf(DailyCount(storedDate, total)) + history).take(historyLimit)
            } else {
                history
            }
            IncrementResult(counts = mapOf(phrase to 1), date = today, history = rolled)
        }
    }

    /** Removes one accidental tap (never goes below zero). */
    fun decrement(counts: Map<TasbihPhrase, Int>, phrase: TasbihPhrase): Map<TasbihPhrase, Int> =
        counts + (phrase to max(0, (counts[phrase] ?: 0) - 1))

    /** Zeroes only the active phrase's counter. */
    fun resetPhrase(counts: Map<TasbihPhrase, Int>, phrase: TasbihPhrase): Map<TasbihPhrase, Int> =
        counts - phrase

    /**
     * True when [newCount] lands exactly on a multiple of [target] — i.e. a
     * full round was just completed (33, 66, 99… for target 33; 99, 198… for
     * target 99; 100, 200… for target 100).
     */
    fun completesRound(newCount: Int, target: Int): Boolean =
        target > 0 && newCount > 0 && newCount % target == 0

    /** The number of full rounds completed at [newCount] with [target]. */
    fun roundNumberAt(newCount: Int, target: Int): Int =
        if (target > 0) newCount / target else 0

    data class IncrementResult(
        val counts: Map<TasbihPhrase, Int>,
        val date: LocalDate,
        val history: List<DailyCount>,
    )
}
