package org.example.islamicapp.feature.adhkar.data

import org.example.islamicapp.feature.adhkar.domain.DhikrCategory
import org.example.islamicapp.feature.adhkar.domain.DhikrItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Curated, fully-sourced adhkar collection modelled on "حصن المسلم"
 * (Hisn al-Muslim) — PROJECT_PROMPT.md §6 Phase 4 requires documented
 * sources for every religious text. All texts below are the well-known
 * authentic wording from the source books cited per item.
 *
 * Content lives in code (not assets) so it is compile-time checked,
 * instantly available offline, and reviewable in pull requests.
 */
@Singleton
class AdhkarRepository @Inject constructor() {

    val categories: List<DhikrCategory> by lazy {
        listOf(
        morning,
        evening,
        sleep,
        waking,
        afterPrayer,
        mosque,
        food,
        travel,
        distress,
        general,
        )
    }

    fun category(id: String): DhikrCategory? = categories.firstOrNull { it.id == id }

    /** Short single-line adhkar used by the periodic reminder notification. */
    val shortReminders: List<DhikrItem> by lazy {
        general.items.filter { it.text.length <= 60 } + listOf(
            DhikrItem(
                id = "short_istighfar",
                text = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                translationEn = "I seek Allah's forgiveness and repent to Him.",
                count = 1,
                reference = "رواه البخاري ومسلم",
            ),
            DhikrItem(
                id = "short_hawqala",
                text = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                translationEn = "There is no power and no strength except by Allah.",
                count = 1,
                reference = "رواه البخاري",
            ),
            DhikrItem(
                id = "short_salawat",
                text = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
                translationEn = "O Allah, send blessings and peace upon our Prophet Muhammad.",
                count = 1,
                reference = "رواه مسلم",
            ),
        )
    }

    private val ayatAlKursi = DhikrItem(
        id = "ayat_al_kursi_m",
        text = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
        translationEn = "Ayat al-Kursi (Al-Baqarah 255).",
        count = 1,
        reference = "رواه الحاكم وصححه الألباني",
        virtue = "من قالها حين يصبح أُجير من الجن حتى يمسي",
    )

    private val muawwidhat = DhikrItem(
        id = "muawwidhat_m",
        text = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ\n\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ\n\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
        translationEn = "Surah al-Ikhlas, al-Falaq and an-Nas — recited three times each.",
        count = 3,
        reference = "رواه أبو داود والترمذي وصححه الألباني",
        virtue = "تكفيك من كل شيء",
    )

    private val morning = DhikrCategory(
        id = "morning",
        titleAr = "أذكار الصباح",
        titleEn = "Morning adhkar",
        items = listOf(
            ayatAlKursi,
            muawwidhat,
            DhikrItem(
                id = "asbahna_almulk",
                text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَٰذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَٰذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
                translationEn = "We have entered a new morning and with it all dominion belongs to Allah…",
                count = 1,
                reference = "رواه مسلم",
            ),
            DhikrItem(
                id = "allahumma_bika_asbahna",
                text = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ",
                translationEn = "O Allah, by You we enter the morning and the evening, by You we live and die, and to You is the resurrection.",
                count = 1,
                reference = "رواه الترمذي وصححه الألباني",
            ),
            DhikrItem(
                id = "sayyid_istighfar",
                text = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                translationEn = "O Allah, You are my Lord, there is no god but You… (Sayyid al-Istighfar — the master supplication for forgiveness).",
                count = 1,
                reference = "رواه البخاري",
                virtue = "من قالها موقنًا بها حين يصبح فمات دخل الجنة",
            ),
            DhikrItem(
                id = "allahumma_asbahtu_ushhiduka",
                text = "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَٰهَ إِلَّا أَنْتَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
                translationEn = "O Allah, I bear witness — as do the bearers of Your Throne, Your angels and all creation — that You are Allah, there is no god but You, and that Muhammad is Your servant and Messenger.",
                count = 4,
                reference = "رواه أبو داود والترمذي",
                virtue = "من قالها أعتقه الله من النار",
            ),
            DhikrItem(
                id = "allahumma_afini",
                text = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَٰهَ إِلَّا أَنْتَ",
                translationEn = "O Allah, grant my body health, my hearing health, and my sight health; there is no god but You.",
                count = 3,
                reference = "رواه أبو داود وحسنه الألباني",
            ),
            DhikrItem(
                id = "hasbi_allah",
                text = "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                translationEn = "Allah is sufficient for me; there is no god but Him. In Him I trust, and He is the Lord of the Mighty Throne.",
                count = 7,
                reference = "رواه أبو داود",
            ),
            DhikrItem(
                id = "bismillah_la_yadurr",
                text = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                translationEn = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm; He is the All-Hearing, All-Knowing.",
                count = 3,
                reference = "رواه أبو داود والترمذي وصححه الألباني",
                virtue = "لم تضره فجأة بلاء حتى يمسي",
            ),
            DhikrItem(
                id = "radhitu_billah",
                text = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
                translationEn = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
                count = 3,
                reference = "رواه أبو داود والترمذي وحسنه الألباني",
                virtue = "كان حقًا على الله أن يرضيه يوم القيامة",
            ),
            DhikrItem(
                id = "ya_hayyu_ya_qayyum_asbah",
                text = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
                translationEn = "O Ever-Living, O Sustainer, by Your mercy I seek help; set right all my affairs and do not leave me to myself for the blink of an eye.",
                count = 1,
                reference = "رواه الحاكم وحسنه الألباني",
            ),
            DhikrItem(
                id = "asbahna_fitrat",
                text = "أَصْبَحْنَا عَلَى فِطْرَةِ الْإِسْلَامِ، وَعَلَى كَلِمَةِ الْإِخْلَاصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ ﷺ، وَعَلَى مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
                translationEn = "We have entered the morning upon the fitrah of Islam, the word of sincerity, the religion of our Prophet Muhammad ﷺ and the millah of our father Ibrahim, inclining to truth and being of those who submit.",
                count = 1,
                reference = "رواه أحمد وصححه الألباني",
            ),
            DhikrItem(
                id = "subhanallah_wabihamdi_100",
                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                translationEn = "Glory be to Allah and praise be to Him.",
                count = 100,
                reference = "رواه مسلم",
                virtue = "حُطَّتْ خطاياه وإن كانت مثل زبد البحر",
            ),
            DhikrItem(
                id = "la_ilaha_illa_allah_100",
                text = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                translationEn = "There is no god but Allah alone, without partner; to Him belongs dominion and praise, and He is able to do all things.",
                count = 10,
                reference = "رواه البخاري ومسلم",
                virtue = "كانت له عدل عشر رقاب وكُتبت له مئة حسنة",
            ),
            DhikrItem(
                id = "subhanallah_adad_khalqih",
                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
                translationEn = "Glory and praise be to Allah, as many as His creation, as pleases Him, as weighs His Throne, and as abundant as the ink of His words.",
                count = 3,
                reference = "رواه مسلم",
            ),
            DhikrItem(
                id = "a_udhu_bikalimat_allah",
                text = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                translationEn = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
                count = 3,
                reference = "رواه مسلم",
                virtue = "لم تضره حُمَةٌ تلك الليلة",
            ),
            DhikrItem(
                id = "allahumma_salli_muhammad_10",
                text = "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
                translationEn = "O Allah, send blessings and peace upon our Prophet Muhammad.",
                count = 10,
                reference = "رواه مسلم",
                virtue = "من صلى عليّ صلاة صلى الله عليه بها عشرًا",
            ),
        ),
    )

    private val evening = DhikrCategory(
        id = "evening",
        titleAr = "أذكار المساء",
        titleEn = "Evening adhkar",
        items = listOf(
            ayatAlKursi.copy(id = "ayat_al_kursi_e", virtue = "من قالها حين يمسي أُجير من الجن حتى يصبح"),
            muawwidhat.copy(id = "muawwidhat_e"),
            DhikrItem(
                id = "amsayna_almulk",
                text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَٰذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَٰذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
                translationEn = "We have entered the evening and with it all dominion belongs to Allah…",
                count = 1,
                reference = "رواه مسلم",
            ),
            DhikrItem(
                id = "allahumma_bika_amsayna",
                text = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
                translationEn = "O Allah, by You we enter the evening and the morning, by You we live and die, and to You is the destination.",
                count = 1,
                reference = "رواه الترمذي وصححه الألباني",
            ),
            DhikrItem(
                id = "sayyid_istighfar_e",
                text = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                translationEn = "Sayyid al-Istighfar (see morning adhkar).",
                count = 1,
                reference = "رواه البخاري",
                virtue = "من قالها موقنًا بها حين يمسي فمات دخل الجنة",
            ),
            DhikrItem(
                id = "allahumma_amsaytu_ushhiduka",
                text = "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلَٰهَ إِلَّا أَنْتَ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
                translationEn = "Evening version: O Allah, I bear witness that You are Allah… and that Muhammad is Your servant and Messenger.",
                count = 4,
                reference = "رواه أبو داود والترمذي",
            ),
            DhikrItem(
                id = "bismillah_la_yadurr_e",
                text = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
                translationEn = "In the name of Allah, with whose name nothing can cause harm…",
                count = 3,
                reference = "رواه أبو داود والترمذي وصححه الألباني",
            ),
            DhikrItem(
                id = "a_udhu_bikalimat_allah_e",
                text = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                translationEn = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
                count = 3,
                reference = "رواه مسلم",
            ),
            DhikrItem(
                id = "radhitu_billah_e",
                text = "رَضِيتُ بِاللَّهِ رَبًّا، وَبِالْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ ﷺ نَبِيًّا",
                translationEn = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
                count = 3,
                reference = "رواه أبو داود والترمذي وحسنه الألباني",
            ),
        ),
    )

    private val sleep = DhikrCategory(
        id = "sleep",
        titleAr = "أذكار النوم",
        titleEn = "Sleep adhkar",
        items = listOf(
            DhikrItem(
                id = "bismika_allahumma_amutu",
                text = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
                translationEn = "In Your name, O Allah, I die and I live.",
                count = 1,
                reference = "رواه البخاري",
            ),
            DhikrItem(
                id = "ayat_al_kursi_sleep",
                text = "قراءة آية الكرسي: اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ… (البقرة: 255)",
                translationEn = "Recite Ayat al-Kursi (al-Baqarah 255).",
                count = 1,
                reference = "رواه البخاري",
                virtue = "لن يزال عليك من الله حافظ ولا يقربك شيطان حتى تصبح",
            ),
            DhikrItem(
                id = "muawwidhat_nafth",
                text = "النفث في الكفين بقراءة: قُلْ هُوَ اللَّهُ أَحَدٌ… وَقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ… وَقُلْ أَعُوذُ بِرَبِّ النَّاسِ… ثم مسح ما أقبل من الجسد والرأس والوجه، ثلاث مرات",
                translationEn = "Join your palms, recite al-Ikhlas, al-Falaq and an-Nas, blow into your hands and wipe over your body — three times.",
                count = 3,
                reference = "رواه البخاري ومسلم",
            ),
            DhikrItem(
                id = "allahumma_aslamtu",
                text = "اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ، وَفَوَّضْتُ أَمْرِي إِلَيْكَ، وَأَلْجَأْتُ ظَهْرِي إِلَيْكَ، وَرَغْبَةً وَرَهْبَةً إِلَيْكَ، لَا مَلْجَأَ وَلَا مَنْجَا مِنْكَ إِلَّا إِلَيْكَ، آمَنْتُ بِكِتَابِكَ الَّذِي أَنْزَلْتَ، وَبِنَبِيِّكَ الَّذِي أَرْسَلْتَ",
                translationEn = "O Allah, I submit myself to You, entrust my affairs to You… I believe in Your Book which You revealed and in Your Prophet whom You sent.",
                count = 1,
                reference = "رواه البخاري ومسلم",
                virtue = "من قالها ومات من ليلته مات على الفطرة",
            ),
            DhikrItem(
                id = "tasbih_sleep_33",
                text = "سُبْحَانَ اللَّهِ (٣٣)، الْحَمْدُ لِلَّهِ (٣٣)، اللَّهُ أَكْبَرُ (٣٤)",
                translationEn = "Subhan Allah 33×, al-hamdu lillah 33×, Allahu akbar 34×.",
                count = 100,
                reference = "رواه البخاري ومسلم",
                virtue = "خير لكما من خادم",
            ),
            DhikrItem(
                id = "allahumma_qini_adhabak",
                text = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
                translationEn = "O Allah, protect me from Your punishment on the day You resurrect Your servants.",
                count = 3,
                reference = "رواه أبو داود والترمذي",
            ),
            DhikrItem(
                id = "bismika_rabbi_wada_tu",
                text = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
                translationEn = "In Your name, my Lord, I lie down and by You I rise…",
                count = 1,
                reference = "رواه أبو داود والترمذي",
            ),
        ),
    )

    private val waking = DhikrCategory(
        id = "waking",
        titleAr = "أذكار الاستيقاظ",
        titleEn = "Waking up adhkar",
        items = listOf(
            DhikrItem(
                id = "alhamdulillah_ahyana",
                text = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
                translationEn = "All praise is for Allah who gave us life after having taken it from us, and unto Him is the resurrection.",
                count = 1,
                reference = "رواه البخاري",
            ),
            DhikrItem(
                id = "la_ilaha_radda_roohi",
                text = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ… الْحَمْدُ لِلَّهِ الَّذِي رَدَّ عَلَيَّ رُوحِي، وَعَافَانِي فِي جَسَدِي، وَأَذِنَ لِي بِذِكْرِهِ",
                translationEn = "There is no god but Allah alone… praise is for Allah who restored my soul, kept my body healthy and allowed me to remember Him.",
                count = 1,
                reference = "رواه الترمذي وحسنه الألباني",
            ),
        ),
    )

    private val afterPrayer = DhikrCategory(
        id = "after_prayer",
        titleAr = "أذكار بعد الصلاة",
        titleEn = "Post-prayer adhkar",
        items = listOf(
            DhikrItem(
                id = "astaghfirullah_3",
                text = "أَسْتَغْفِرُ اللَّهَ (٣ مرات)، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
                translationEn = "Astaghfirullah ×3, then: O Allah, You are Peace and from You comes peace; blessed are You, Owner of Majesty and Honour.",
                count = 1,
                reference = "رواه مسلم",
            ),
            DhikrItem(
                id = "la_ilaha_wahdahu_la_shareeka",
                text = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، اللَّهُمَّ لَا مَانِعَ لِمَا أَعْطَيْتَ، وَلَا مُعْطِيَ لِمَا مَنَعْتَ، وَلَا يَنْفَعُ ذَا الْجَدِّ مِنْكَ الْجَدُّ",
                translationEn = "There is no god but Allah alone… O Allah, none can withhold what You give…",
                count = 1,
                reference = "رواه البخاري ومسلم",
            ),
            DhikrItem(
                id = "tasbih_33_33_33",
                text = "سُبْحَانَ اللَّهِ (٣٣)، الْحَمْدُ لِلَّهِ (٣٣)، اللَّهُ أَكْبَرُ (٣٣)\nثم تمام المئة: لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                translationEn = "Subhan Allah 33×, al-hamdu lillah 33×, Allahu akbar 33×, then complete 100 with: la ilaha illa Allah wahdahu…",
                count = 100,
                reference = "رواه مسلم",
                virtue = "غُفرت خطاياه وإن كانت مثل زبد البحر",
            ),
            DhikrItem(
                id = "ayat_al_kursi_after",
                text = "قراءة آية الكرسي بعد كل صلاة: اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ… (البقرة: 255)",
                translationEn = "Ayat al-Kursi after every prayer.",
                count = 1,
                reference = "رواه النسائي وصححه الألباني",
                virtue = "لم يمنعه من دخول الجنة إلا أن يموت",
            ),
            DhikrItem(
                id = "muawwidhat_after",
                text = "قراءة: قُلْ هُوَ اللَّهُ أَحَدٌ، قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ، قُلْ أَعُوذُ بِرَبِّ النَّاسِ بعد كل صلاة",
                translationEn = "Recite al-Ikhlas, al-Falaq and an-Nas after every prayer.",
                count = 1,
                reference = "رواه أبو داود والنسائي",
            ),
            DhikrItem(
                id = "allahumma_ainni_dhikrak",
                text = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ",
                translationEn = "O Allah, help me to remember You, thank You, and worship You well.",
                count = 1,
                reference = "رواه أبو داود والنسائي وصححه الألباني",
            ),
        ),
    )

    private val mosque = DhikrCategory(
        id = "mosque",
        titleAr = "أذكار المسجد",
        titleEn = "Mosque adhkar",
        items = listOf(
            DhikrItem(
                id = "entering_mosque",
                text = "عند دخول المسجد: اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
                translationEn = "Entering the mosque: O Allah, open for me the doors of Your mercy.",
                count = 1,
                reference = "رواه مسلم",
            ),
            DhikrItem(
                id = "leaving_mosque",
                text = "عند الخروج من المسجد: اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
                translationEn = "Leaving the mosque: O Allah, I ask You from Your bounty.",
                count = 1,
                reference = "رواه مسلم",
            ),
        ),
    )

    private val food = DhikrCategory(
        id = "food",
        titleAr = "أذكار الطعام والشراب",
        titleEn = "Food & drink adhkar",
        items = listOf(
            DhikrItem(
                id = "bismillah_food",
                text = "قبل الطعام: بِسْمِ اللَّهِ\nوإذا نسي في أوله: بِسْمِ اللَّهِ أَوَّلَهُ وَآخِرَهُ",
                translationEn = "Before eating: Bismillah. If forgotten at the start: Bismillahi awwalahu wa akhirahu.",
                count = 1,
                reference = "رواه أبو داود والترمذي وصححه الألباني",
            ),
            DhikrItem(
                id = "alhamdulillah_at_amani",
                text = "بعد الطعام: الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَٰذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
                translationEn = "After eating: All praise is for Allah who fed me this and provided it for me without any effort or power on my part.",
                count = 1,
                reference = "رواه الترمذي وحسنه الألباني",
                virtue = "غُفر له ما تقدم من ذنبه",
            ),
            DhikrItem(
                id = "allahumma_barik_lana",
                text = "إذا أفطر عند أهل بيت: أَفْطَرَ عِنْدَكُمُ الصَّائِمُونَ، وَأَكَلَ طَعَامَكُمُ الْأَبْرَارُ، وَصَلَّتْ عَلَيْكُمُ الْمَلَائِكَةُ",
                translationEn = "When breaking fast at someone's home: May the fasting break their fast with you, may the righteous eat your food, and may the angels pray for you.",
                count = 1,
                reference = "رواه أبو داود وابن ماجه وصححه الألباني",
            ),
        ),
    )

    private val travel = DhikrCategory(
        id = "travel",
        titleAr = "أذكار السفر",
        titleEn = "Travel adhkar",
        items = listOf(
            DhikrItem(
                id = "travel_takbir",
                text = "عند السفر: اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ، اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَٰذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى، اللَّهُمَّ هَوِّنْ عَلَيْنَا سَفَرَنَا هَٰذَا، وَاطْوِ عَنَّا بُعْدَهُ، اللَّهُمَّ أَنْتَ الصَّاحِبُ فِي السَّفَرِ، وَالْخَلِيفَةُ فِي الْأَهْلِ",
                translationEn = "When travelling: Allahu akbar ×3, then: Glory to Him who has subjected this to us… O Allah, we ask You for righteousness and piety in this journey…",
                count = 1,
                reference = "رواه مسلم",
            ),
            DhikrItem(
                id = "travel_nuzul",
                text = "عند النزول في مكان: أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
                translationEn = "Upon arriving at a place: I seek refuge in the perfect words of Allah from the evil of what He created.",
                count = 1,
                reference = "رواه مسلم",
                virtue = "لم يضره شيء حتى يرتحل",
            ),
            DhikrItem(
                id = "travel_return",
                text = "عند الرجوع من السفر: اللَّهُ أَكْبَرُ (٣)، آيِبُونَ تَائِبُونَ عَابِدُونَ لِرَبِّنَا حَامِدُونَ",
                translationEn = "Returning from travel: Allahu akbar ×3 — returning, repenting, worshipping and praising our Lord.",
                count = 1,
                reference = "رواه مسلم",
            ),
        ),
    )

    private val distress = DhikrCategory(
        id = "distress",
        titleAr = "أذكار الهم والكرب",
        titleEn = "Distress & anxiety adhkar",
        items = listOf(
            DhikrItem(
                id = "dua_yunus",
                text = "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                translationEn = "There is no god but You; glory be to You; indeed I was among the wrongdoers (dua of Yunus).",
                count = 1,
                reference = "رواه الترمذي وصححه الألباني",
                virtue = "ما دعا بها مسلم في شيء إلا استجاب الله له",
            ),
            DhikrItem(
                id = "dua_karb",
                text = "لَا إِلَٰهَ إِلَّا اللَّهُ الْعَظِيمُ الْحَلِيمُ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ الْعَرْشِ الْعَظِيمِ، لَا إِلَٰهَ إِلَّا اللَّهُ رَبُّ السَّمَاوَاتِ وَرَبُّ الْأَرْضِ وَرَبُّ الْعَرْشِ الْكَرِيمِ",
                translationEn = "There is no god but Allah, the Most Great, the Forbearing… (dua of distress).",
                count = 1,
                reference = "رواه البخاري ومسلم",
            ),
            DhikrItem(
                id = "hasbuna_allah",
                text = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                translationEn = "Sufficient for us is Allah, and He is the best Disposer of affairs.",
                count = 1,
                reference = "رواه البخاري",
            ),
            DhikrItem(
                id = "allahumma_abduka",
                text = "اللَّهُمَّ إِنِّي عَبْدُكَ، ابْنُ عَبْدِكَ، ابْنُ أَمَتِكَ، نَاصِيَتِي بِيَدِكَ، مَاضٍ فِيَّ حُكْمُكَ، عَدْلٌ فِيَّ قَضَاؤُكَ، أَسْأَلُكَ بِكُلِّ اسْمٍ هُوَ لَكَ سَمَّيْتَ بِهِ نَفْسَكَ… أَنْ تَجْعَلَ الْقُرْآنَ رَبِيعَ قَلْبِي، وَنُورَ صَدْرِي، وَجِلَاءَ حُزْنِي، وَذَهَابَ هَمِّي",
                translationEn = "O Allah, I am Your servant, son of Your servant… I ask You by every name of Yours to make the Qur'an the spring of my heart and the removal of my anxiety and grief.",
                count = 1,
                reference = "رواه أحمد وصححه الألباني",
            ),
            DhikrItem(
                id = "allahumma_rahmataka_arju",
                text = "اللَّهُمَّ رَحْمَتَكَ أَرْجُو فَلَا تَكِلْنِي إِلَى نَفْسِي، وَأَصْلِحْ لِي شَأْنِي كُلَّهُ، لَا إِلَٰهَ إِلَّا أَنْتَ",
                translationEn = "O Allah, I hope for Your mercy; do not leave me to myself, and set right all my affairs; there is no god but You.",
                count = 1,
                reference = "رواه أبو داود وحسنه الألباني",
            ),
        ),
    )

    private val general = DhikrCategory(
        id = "general",
        titleAr = "تسابيح وأذكار عامة",
        titleEn = "General tasbih & adhkar",
        items = listOf(
            DhikrItem(
                id = "subhanallah_wabihamdi_100_g",
                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                translationEn = "Glory be to Allah and praise be to Him.",
                count = 100,
                reference = "رواه البخاري ومسلم",
                virtue = "من قالها مئة مرة حُطَّت خطاياه وإن كانت مثل زبد البحر",
            ),
            DhikrItem(
                id = "la_hawla",
                text = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                translationEn = "There is no power and no strength except by Allah.",
                count = 10,
                reference = "رواه البخاري",
                virtue = "كنز من كنوز الجنة",
            ),
            DhikrItem(
                id = "the_four_words",
                text = "سُبْحَانَ اللَّهِ، وَالْحَمْدُ لِلَّهِ، وَلَا إِلَٰهَ إِلَّا اللَّهُ، وَاللَّهُ أَكْبَرُ",
                translationEn = "Subhan Allah, al-hamdu lillah, la ilaha illa Allah, Allahu akbar.",
                count = 10,
                reference = "رواه مسلم",
                virtue = "أحب الكلام إلى الله",
            ),
            DhikrItem(
                id = "istighfar_100",
                text = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                translationEn = "I seek Allah's forgiveness and turn to Him in repentance.",
                count = 100,
                reference = "رواه البخاري ومسلم",
            ),
            DhikrItem(
                id = "subhanallah_adad_g",
                text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
                translationEn = "Glory and praise be to Allah, as many as His creation…",
                count = 3,
                reference = "رواه مسلم",
            ),
        ),
    )
}
