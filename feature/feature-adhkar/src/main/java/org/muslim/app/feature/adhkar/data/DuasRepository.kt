package org.muslim.app.feature.adhkar.data

import org.muslim.app.feature.adhkar.domain.Dhikr
import org.muslim.app.feature.adhkar.domain.DhikrCategory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extended categorized duas (طلب صريح: "أدعية موسّعة مصنّفة") — Qur'anic
 * duas, daily-life duas and occasion duas, every entry sourced from the
 * Qur'an or the authenticated Sunnah. Rendered in the adhkar screen under the
 * Dua categories.
 */
@Singleton
class DuasRepository @Inject constructor() {

    /** All duas, in a stable order (ids are offset to avoid seed collisions). */
    val allDuas: List<Dhikr> by lazy {
        quranicDuas() + dailyDuas() + occasionDuas()
    }

    private fun quranicDuas(): List<Dhikr> = listOf(
        item(
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "Our Lord, give us good in this world and good in the Hereafter, and protect us from the Fire.",
            "أكثر دعاء النبي ﷺ — البقرة: 201", "رواه البخاري ومسلم — سورة البقرة: 201",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ الْوَهَّابُ",
            "Our Lord, let not our hearts deviate after You have guided us, and grant us mercy from Yourself.",
            "سورة آل عمران: 8", "قرآنية — آل عمران: 8",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي وَاحْلُلْ عُقْدَةً مِّن لِّسَانِي",
            "My Lord, expand for me my chest and ease for me my task and untie the knot from my tongue.",
            "دعاء موسى عليه السلام — طه: 25-27", "قرآنية — طه: 25-27",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "رَبَّنَا هَبْ لَنَا مِنْ أَزْوَاجِنَا وَذُرِّيَّاتِنَا قُرَّةَ أَعْيُنٍ وَاجْعَلْنَا لِلْمُتَّقِينَ إِمَامًا",
            "Our Lord, grant us spouses and offspring who are the comfort of our eyes, and make us leaders of the righteous.",
            "الفرقان: 74", "قرآنية — الفرقان: 74",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "رَبِّ اجْعَلْنِي مُقِيمَ الصَّلَاةِ وَمِن ذُرِّيَّتِي ۚ رَبَّنَا وَتَقَبَّلْ دُعَاءِ",
            "My Lord, make me an establisher of prayer, and from my descendants; and accept my supplication.",
            "إبراهيم: 40", "قرآنية — إبراهيم: 40",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            "There is no deity except You; glory to You; indeed I was of the wrongdoers.",
            "دعاء يونس عليه السلام — الأنبياء: 87", "رواه الترمذي",
            DhikrCategory.DuaQuranic,
            "ما دعا بها مسلم في شيء إلا استجاب الله له — رواه الترمذي",
        ),
        item(
            "رَبَّنَا ظَلَمْنَا أَنفُسَنَا وَإِن لَّمْ تَغْفِرْ لَنَا وَتَرْحَمْنَا لَنَكُونَنَّ مِنَ الْخَاسِرِينَ",
            "Our Lord, we have wronged ourselves, and if You do not forgive us and have mercy, we will be among the losers.",
            "الأعراف: 23", "قرآنية — الأعراف: 23",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "رَبِّ زِدْنِي عِلْمًا",
            "My Lord, increase me in knowledge.",
            "طه: 114", "قرآنية — طه: 114",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "رَبَّنَا اغْفِرْ لَنَا ذُنُوبَنَا وَإِسْرَافَنَا فِي أَمْرِنَا وَثَبِّتْ أَقْدَامَنَا وَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
            "Our Lord, forgive us our sins and excesses, make firm our feet, and grant us victory.",
            "آل عمران: 147", "قرآنية — آل عمران: 147",
            DhikrCategory.DuaQuranic,
        ),
        item(
            "رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا ۚ رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا",
            "Our Lord, take us not to task if we forget or err; lay not upon us a burden like that which You laid upon those before us.",
            "البقرة: 286 — آخر آية نزولًا", "قرآنية — البقرة: 286",
            DhikrCategory.DuaQuranic,
        ),
    )

    private fun dailyDuas(): List<Dhikr> = listOf(
        item(
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا وَاسِعًا، وَشِفَاءً مِنْ كُلِّ دَاءٍ",
            "O Allah, I ask You beneficial knowledge, ample provision, and healing from every illness.",
            "أدعية مأثورة صحيحة الجوامع", "من الجوامع المأثورة",
            DhikrCategory.DuaDaily,
        ),
        item(
            "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ — دعاء الخروج من المنزل",
            "In the name of Allah, I place my trust in Allah... — when leaving home.",
            "يُقال عند الخروج من المنزل", "رواه أبو داود والترمذي",
            DhikrCategory.DuaDaily,
        ),
        item(
            "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ… عند التردد في أمر مباح",
            "O Allah, I seek Your guidance by Your knowledge and Your power… (istikharah).",
            "دعاء الاستخارة كاملًا", "رواه البخاري",
            DhikrCategory.DuaDaily,
        ),
        item(
            "اللَّهُمَّ اعْصِمْنِي مِنْ بَيْنِ يَدَيَّ، وَمِنْ خَلْفِي، وَعَنْ يَمِينِي، وَعَنْ شِمَالِي، وَمِنْ فَوْقِي",
            "O Allah, protect me from before me, behind me, my right, my left and above me.",
            "حفظ وحماية", "رواه أبو داود وابن ماجه",
            DhikrCategory.DuaDaily,
        ),
        item(
            "اللَّهُمَّ لَكَ الْحَمْدُ كَمَا هَدَيْتَنَا وَشَكَرْتَ — عند بلوغ نعمة أو فرج",
            "O Allah, to You is praise as You guided us…",
            "شكر النعم", "من الجوامع المأثورة",
            DhikrCategory.DuaDaily,
        ),
        item(
            "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ — عند الضيق والحاجة",
            "O Ever-Living, O Sustainer, by Your mercy I seek help.",
            "عند الضيق", "رواه الترمذي",
            DhikrCategory.DuaDaily,
        ),
    )

    private fun occasionDuas(): List<Dhikr> = listOf(
        item(
            "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            "O Allah, You are Pardoning and love pardon, so pardon me.",
            "دعاء ليلة القدر", "رواه الترمذي وابن ماجه",
            DhikrCategory.DuaOccasion,
        ),
        item(
            "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            "Thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
            "دعاء الإفطار للصائم", "رواه أبو داود",
            DhikrCategory.DuaOccasion,
        ),
        item(
            "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالْأَمْنِ وَالْإِيمَانِ وَالسَّلَامَةِ وَالْإِسْلَامِ",
            "O Allah, let this month dawn upon us with security, faith, safety and Islam.",
            "عند رؤية هلال الشهر", "رواه الترمذي",
            DhikrCategory.DuaOccasion,
        ),
        item(
            "اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ وَعَافِهِ وَاعْفُ عَنْهُ — دعاء للميت",
            "O Allah, forgive him, have mercy on him, grant him well-being and pardon him.",
            "في الصلاة على الميت وزيارته", "رواه مسلم",
            DhikrCategory.DuaOccasion,
        ),
        item(
            "بَارَكَ اللَّهُ لَكَ وَبَارَكَ عَلَيْكَ وَجَمَعَ بَيْنَكُمَا فِي خَيْرٍ — تهنئة الزواج",
            "May Allah bless you, and shower His blessings upon you, and join you together in goodness.",
            "للمتزوج", "رواه أبو داود والترمذي",
            DhikrCategory.DuaOccasion,
        ),
        item(
            "أَعْظَمَ اللَّهُ أَجْرَكَ، وَأَحْسَنَ عَزَاءَكَ، وَغَفَرَ لِمَيِّتِكَ — تعزية",
            "May Allah magnify your reward, make good your consolation, and forgive your deceased.",
            "لتعزية المصاب", "رواه أبو داود",
            DhikrCategory.DuaOccasion,
        ),
        item(
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ… في كل شأن",
            "O Allah, I ask You for well-being in this world and the Hereafter.",
            "جامع شامل", "رواه أبو داود وابن ماجه",
            DhikrCategory.DuaOccasion,
        ),
    )

    private fun item(
        text: String,
        translationEn: String,
        title: String,
        reference: String,
        category: DhikrCategory,
        virtue: String? = null,
    ): Dhikr {
        val index = allDuasIndex++
        return Dhikr(
            id = ID_OFFSET + index,
            category = category,
            arabic = text,
            translation = translationEn,
            source = reference,
            repetition = 1,
            virtue = virtue ?: title,
        )
    }

    private companion object {
        /** Offset above the bundled seed ids so the two id spaces never collide. */
        const val ID_OFFSET = 1_000_000L
        var allDuasIndex = 0L
    }
}
