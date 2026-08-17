package org.muslim.app.feature.quran.domain

/**
 * Curated surah-details data shown by the reader's Details button
 * (PROJECT_PROMPT.md §6 Phase 2): the chronological order of revelation and
 * well-known reasons for revelation (asbab an-nuzul).
 *
 * The order is the traditional chronological order as published by Tanzil
 * (https://tanzil.net/docs/revelation_order), based on narrations of Ibn
 * Abbas; the order reflects when the *first* ayahs of the surah were revealed.
 * Reasons are short, widely-cited summaries for famous suras only.
 */
object SurahRevelationData {

    /** Chronological order of revelation: surah number -> position (1..114). */
    val revelationOrder: Map<Int, Int> = mapOf(
        96 to 1, 68 to 2, 73 to 3, 74 to 4, 1 to 5, 111 to 6, 81 to 7, 87 to 8,
        92 to 9, 89 to 10, 93 to 11, 94 to 12, 103 to 13, 100 to 14, 108 to 15,
        102 to 16, 107 to 17, 109 to 18, 105 to 19, 113 to 20, 114 to 21,
        112 to 22, 53 to 23, 80 to 24, 97 to 25, 91 to 26, 85 to 27, 95 to 28,
        106 to 29, 101 to 30, 75 to 31, 104 to 32, 77 to 33, 50 to 34, 90 to 35,
        86 to 36, 54 to 37, 38 to 38, 7 to 39, 72 to 40, 36 to 41, 25 to 42,
        35 to 43, 19 to 44, 20 to 45, 56 to 46, 26 to 47, 27 to 48, 28 to 49,
        17 to 50, 10 to 51, 11 to 52, 12 to 53, 15 to 54, 6 to 55, 37 to 56,
        31 to 57, 34 to 58, 39 to 59, 40 to 60, 41 to 61, 42 to 62, 43 to 63,
        44 to 64, 45 to 65, 46 to 66, 51 to 67, 88 to 68, 18 to 69, 16 to 70,
        71 to 71, 14 to 72, 21 to 73, 23 to 74, 32 to 75, 52 to 76, 67 to 77,
        69 to 78, 70 to 79, 78 to 80, 79 to 81, 82 to 82, 84 to 83, 30 to 84,
        29 to 85, 83 to 86, 2 to 87, 8 to 88, 3 to 89, 33 to 90, 60 to 91,
        4 to 92, 99 to 93, 57 to 94, 47 to 95, 13 to 96, 55 to 97, 76 to 98,
        65 to 99, 98 to 100, 59 to 101, 24 to 102, 22 to 103, 63 to 104,
        58 to 105, 49 to 106, 66 to 107, 64 to 108, 61 to 109, 62 to 110,
        48 to 111, 5 to 112, 9 to 113, 110 to 114,
    )

    /**
     * Curated reasons for revelation for well-known suras.
     * Each value is (Arabic, English) — the UI picks the app language.
     */
    val revelationReason: Map<Int, Pair<String, String>> = mapOf(
        1 to Pair(
            "أول سورة نزلت كاملة؛ وهي أم الكتاب، وتُقرأ في كل ركعة.",
            "The first surah revealed in full; it is the Mother of the Book, recited in every rak'ah."
        ),
        2 to Pair(
            "أطول سورة في القرآن، نزلت في المدينة في أوقات مختلفة.",
            "The longest surah in the Quran, revealed in Medina over different periods."
        ),
        9 to Pair(
            "آخر سورة نزلت (في معظمها)، وهي الوحيدة التي لا تبدأ بالبسملة.",
            "One of the last revealed suras; it is the only one that does not begin with the Bismillah."
        ),
        19 to Pair(
            "نزلت بعد سورة آل عمران، وفيها قصة مريم وعيسى عليهما السلام.",
            "Revealed after Aal-i-Imran; it tells the story of Maryam and 'Isa (peace be upon them)."
        ),
        20 to Pair(
            "نزلت لتثبيت النبي ﷺ بقصة موسى عليه السلام، وفيها التخفيف من قيام الليل.",
            "Revealed to strengthen the Prophet (peace be upon him) with the story of Musa; it eases the night prayer."
        ),
        25 to Pair(
            "نزلت جوابًا على شبهات المشركين في القرآن والرسول.",
            "Revealed in response to the disbelievers' doubts about the Quran and the Messenger."
        ),
        36 to Pair(
            "«قلب القرآن»؛ تُقرأ للمحتضرين والموتى، وفيها دلائل البعث.",
            "'The heart of the Quran', recited for the dying and the deceased; it presents proofs of the Resurrection."
        ),
        53 to Pair(
            "نزلت عند نزولها سجد النبي ﷺ وسجد معه المشركون؛ أول سورة سُجد فيها.",
            "When it was revealed the Prophet (peace be upon him) prostrated and the disbelievers prostrated with him; the first surah with a prostration."
        ),
        55 to Pair(
            "نزلت في بيان نعم الله على الإنس والجن، تكرر فيها (فبأي آلاء ربكما تكذبان).",
            "Revealed about Allah's favours to mankind and the jinn, repeating 'Which of the favours of your Lord will you deny?'"
        ),
        68 to Pair(
            "ثاني سورة نزولًا؛ نزلت جوابًا على اتهام المشركين للنبي ﷺ بالجنون، وفيها وصف خلقه العظيم.",
            "The second revealed surah; it answers the disbelievers' charge of madness against the Prophet and praises his great character."
        ),
        73 to Pair(
            "نزلت في أول الأمر بقيام الليل، ثم خُفف بأواخرها.",
            "Revealed commanding the night prayer at the start, later eased by its final verses."
        ),
        74 to Pair(
            "نزلت بعد فترة فتور الوحي: (يا أيها المدثر، قم فأنذر).",
            "Revealed after a pause in revelation: 'O you wrapped in your cloak, arise and warn.'"
        ),
        80 to Pair(
            "نزلت في ابن أم مكتوم الأعمى حين أعرض عنه النبي ﷺ لانشغاله بدعوة كبار قريش.",
            "Revealed about Ibn Umm Maktum, the blind man, when the Prophet turned away while inviting the leaders of Quraysh."
        ),
        93 to Pair(
            "نزلت تطمينًا للنبي ﷺ بعد انقطاع الوحي: (ما ودعك ربك وما قلى).",
            "Revealed to reassure the Prophet after revelation paused: 'Your Lord has not forsaken you, nor is He displeased.'"
        ),
        94 to Pair(
            "نزلت بعد سورة الضحى: (ألم نشرح لك صدرك).",
            "Revealed after Ad-Duha: 'Did We not expand for you your breast?'"
        ),
        96 to Pair(
            "أول ما نزل من القرآن: (اقرأ باسم ربك) في غار حراء.",
            "The first revelation of the Quran: 'Read in the name of your Lord' in the Cave of Hira."
        ),
        105 to Pair(
            "نزلت في قصة أصحاب الفيل وحماية الكعبة من أبرهة.",
            "Revealed about the People of the Elephant and the protection of the Kaaba from Abraha."
        ),
        108 to Pair(
            "نزلت ردًا على من قال إن النبي ﷺ أبتر: (إنا أعطيناك الكوثر).",
            "Revealed in reply to those who called the Prophet cut off: 'Indeed, We have granted you al-Kawthar.'"
        ),
        110 to Pair(
            "آخر سورة نزلت كاملة؛ نزلت في حجة الوداع (إذا جاء نصر الله والفتح).",
            "The last surah revealed in full, during the Farewell Pilgrimage: 'When the help of Allah comes and the conquest.'"
        ),
        111 to Pair(
            "نزلت في أبي لهب وزوجته حمالة الحطب.",
            "Revealed about Abu Lahab and his wife, the carrier of firewood."
        ),
        112 to Pair(
            "نزلت جوابًا عن سؤال المشركين عن صفة الله: (قل هو الله أحد).",
            "Revealed in answer to the disbelievers' question about Allah's nature: 'Say: He is Allah, the One.'"
        ),
        113 to Pair(
            "إحدى المعوذتين؛ نزلت للاستعاذة بالله من شر المخلوقات.",
            "One of the two Mu'awwidhatayn, revealed for seeking refuge in Allah from the evil of creation."
        ),
        114 to Pair(
            "إحدى المعوذتين؛ نزلت للاستعاذة من وسوسة الشيطان.",
            "One of the two Mu'awwidhatayn, revealed for seeking refuge from the whisperings of Satan."
        ),
    )

    /** Chronological position (1..114) of a surah, or null if unknown. */
    fun orderOf(surahNumber: Int): Int? = revelationOrder[surahNumber]

    /** Reason for revelation as (Arabic, English), or null when not curated. */
    fun reasonOf(surahNumber: Int): Pair<String, String>? = revelationReason[surahNumber]
}
