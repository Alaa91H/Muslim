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

    // Instance-local counter keeps generated ids deterministic whenever the
    // repository is recreated (tests, process recreation, or future scopes).
    private var allDuasIndex = 0L

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
            "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى",
            "O Allah, I ask You for guidance, piety, chastity, and contentment.",
            "دعاء جامع للهداية والعفة", "رواه مسلم",
            DhikrCategory.DuaDaily,
        ),
        item(
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْبُخْلِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ",
            "O Allah, I seek refuge in You from anxiety and sorrow, incapacity and laziness, cowardice and miserliness, the burden of debt, and being overpowered by people.",
            "دعاء الاستعاذة من الهم والدَّين", "رواه البخاري",
            DhikrCategory.DuaDaily,
        ),
        item(
            "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ، اللَّهُمَّ إِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الْأَمْرَ خَيْرٌ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي، فَاقْدُرْهُ لِي وَيَسِّرْهُ لِي، ثُمَّ بَارِكْ لِي فِيهِ، وَإِنْ كُنْتَ تَعْلَمُ أَنَّ هَذَا الْأَمْرَ شَرٌّ لِي فِي دِينِي وَمَعَاشِي وَعَاقِبَةِ أَمْرِي، فَاصْرِفْهُ عَنِّي وَاصْرِفْنِي عَنْهُ، وَاقْدُرْ لِيَ الْخَيْرَ حَيْثُ كَانَ، ثُمَّ أَرْضِنِي بِهِ",
            "O Allah, I seek the better choice by Your knowledge and seek ability by Your power, and I ask You from Your immense bounty. You are able and I am not; You know and I do not; You are the Knower of the unseen. If You know this matter is good for me in my religion, livelihood, and outcome, decree it for me, make it easy for me, and bless it for me. If You know it is bad for me in my religion, livelihood, and outcome, turn it away from me and turn me away from it; decree good for me wherever it may be, then make me content with it.",
            "دعاء الاستخارة", "رواه البخاري",
            DhikrCategory.DuaDaily,
        ),
        item(
            "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عِلْمٍ لَا يَنْفَعُ، وَمِنْ قَلْبٍ لَا يَخْشَعُ، وَمِنْ نَفْسٍ لَا تَشْبَعُ، وَمِنْ دَعْوَةٍ لَا يُسْتَجَابُ لَهَا",
            "O Allah, I seek refuge in You from knowledge that does not benefit, a heart that is not humble, a soul that is not satisfied, and a supplication that is not answered.",
            "الاستعاذة من العلم غير النافع", "رواه مسلم",
            DhikrCategory.DuaDaily,
        ),
        item(
            "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
            "O Turner of hearts, keep my heart firm upon Your religion.",
            "دعاء الثبات", "رواه الترمذي",
            DhikrCategory.DuaDaily,
        ),
        item(
            "اللَّهُمَّ مُصَرِّفَ الْقُلُوبِ صَرِّفْ قُلُوبَنَا عَلَى طَاعَتِكَ",
            "O Allah, Director of the hearts, direct our hearts to Your obedience.",
            "دعاء توجيه القلب إلى الطاعة", "رواه مسلم",
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
    }
}
