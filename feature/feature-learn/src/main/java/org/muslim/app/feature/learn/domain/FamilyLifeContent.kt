package org.muslim.app.feature.learn.domain

/** A piece of content available in the two curated app languages. */
data class LocalizedFamilyText(
    val arabic: String,
    val english: String,
)

data class RuqyahPassage(
    val id: String,
    val title: LocalizedFamilyText,
    val text: LocalizedFamilyText,
    val reference: LocalizedFamilyText,
    val audioUrl: String,
)

data class RuqyahAudioTrack(
    val id: String,
    val title: LocalizedFamilyText,
    val description: LocalizedFamilyText,
    val url: String,
)

enum class BabyNameGender { Boy, Girl }

data class IslamicBabyName(
    val id: String,
    val nameArabic: String,
    val transliteration: String,
    val gender: BabyNameGender,
    val meaningArabic: String,
    val meaningEnglish: String,
)

data class FamilyGuideSection(
    val title: LocalizedFamilyText,
    val paragraphs: List<LocalizedFamilyText>,
)

data class FamilyGuideArticle(
    val id: String,
    val title: LocalizedFamilyText,
    val summary: LocalizedFamilyText,
    val sections: List<FamilyGuideSection>,
)

/**
 * Offline, conservative family-life reference content.
 *
 * Ruqyah is presented as worship based on the Quran and established
 * supplications, not as a guaranteed medical cure. The app explicitly directs
 * users to qualified medical care for physical or mental-health symptoms.
 * Marriage and parenting material is educational rather than a personal fatwa.
 */
object FamilyLifeContent {
    val ruqyahGuidance = listOf(
        LocalizedFamilyText(
            arabic = "تكون الرقية بتوحيد الله وإخلاص الدعاء له، وبآيات القرآن والأدعية الصحيحة، مع اعتقاد أن الشفاء بيد الله وحده وأن الرقية سبب مشروع لا تستقل بالتأثير.",
            english = "Ruqyah is performed with sincere worship and supplication to Allah, Quranic verses and sound supplications. Healing belongs to Allah alone; ruqyah is a lawful means, not an independent power.",
        ),
        LocalizedFamilyText(
            arabic = "يقرأ المسلم على نفسه أو على المريض بهدوء وتدبر، ويمكن أن ينفث نفثًا خفيفًا بعد القراءة ويمسح موضع الألم بيده من غير اعتداء أو مبالغة.",
            english = "Read calmly and thoughtfully over yourself or the person who asks for help. A gentle blow after recitation and placing the hand on the painful area are permitted without exaggeration or harm.",
        ),
        LocalizedFamilyText(
            arabic = "تُترك التمائم والطلاسم والألفاظ المجهولة، وطلب الاستعانة بالجن، والعزائم التي تخالف التوحيد، واستغلال المريض ماليًا أو تخويفه.",
            english = "Avoid amulets, talismans, unknown formulas, seeking help from jinn, practices that contradict monotheism, and exploiting or frightening a vulnerable person.",
        ),
        LocalizedFamilyText(
            arabic = "الرقية لا تمنع مراجعة الطبيب أو المختص النفسي. الأعراض المستمرة أو الطارئة تحتاج إلى تقييم طبي عاجل، ولا يجوز إيقاف علاج موصوف اعتمادًا على الرقية وحدها.",
            english = "Ruqyah does not replace a doctor or mental-health professional. Persistent or urgent symptoms need medical assessment, and prescribed treatment must not be stopped because of ruqyah.",
        ),
    )

    val ruqyahPassages = listOf(
        RuqyahPassage(
            id = "al_fatihah",
            title = LocalizedFamilyText("سورة الفاتحة", "Surah Al-Fatihah"),
            text = LocalizedFamilyText(
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ۝ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمَٰنِ الرَّحِيمِ ۝ مَالِكِ يَوْمِ الدِّينِ ۝ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                "In the name of Allah, the Most Merciful, the Especially Merciful. All praise belongs to Allah, Lord of the worlds, the Most Merciful, the Especially Merciful, Master of the Day of Judgment. You alone we worship and You alone we ask for help. Guide us to the straight path, the path of those You have blessed, not those who incurred anger nor those who went astray.",
            ),
            reference = LocalizedFamilyText("الفاتحة: 1–7", "Al-Fatihah 1–7"),
            audioUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/001001.mp3",
        ),
        RuqyahPassage(
            id = "ayat_al_kursi",
            title = LocalizedFamilyText("آية الكرسي", "Ayat al-Kursi"),
            text = LocalizedFamilyText(
                "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                "Allah—there is no deity except Him, the Ever-Living, the Sustainer of all. Neither drowsiness nor sleep overtakes Him. To Him belongs whatever is in the heavens and the earth. Who can intercede with Him except by His permission? He knows what is before them and what is behind them, and they encompass nothing of His knowledge except what He wills. His Kursi extends over the heavens and the earth, and preserving them does not tire Him. He is the Most High, the Most Great.",
            ),
            reference = LocalizedFamilyText("البقرة: 255", "Al-Baqarah 2:255"),
            audioUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/002255.mp3",
        ),
        RuqyahPassage(
            id = "al_ikhlas",
            title = LocalizedFamilyText("سورة الإخلاص", "Surah Al-Ikhlas"),
            text = LocalizedFamilyText(
                "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
                "Say: He is Allah, the One. Allah, the Self-Sufficient. He neither begets nor is born, and there is none comparable to Him.",
            ),
            reference = LocalizedFamilyText("الإخلاص: 1–4", "Al-Ikhlas 112:1–4"),
            audioUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/112001.mp3",
        ),
        RuqyahPassage(
            id = "al_falaq",
            title = LocalizedFamilyText("سورة الفلق", "Surah Al-Falaq"),
            text = LocalizedFamilyText(
                "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                "Say: I seek refuge in the Lord of daybreak, from the evil of what He created, from the evil of darkness when it settles, from the evil of those who blow on knots, and from the evil of an envier when he envies.",
            ),
            reference = LocalizedFamilyText("الفلق: 1–5", "Al-Falaq 113:1–5"),
            audioUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/113001.mp3",
        ),
        RuqyahPassage(
            id = "an_nas",
            title = LocalizedFamilyText("سورة الناس", "Surah An-Nas"),
            text = LocalizedFamilyText(
                "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
                "Say: I seek refuge in the Lord of mankind, the King of mankind, the God of mankind, from the evil of the retreating whisperer who whispers in the hearts of mankind, from among jinn and mankind.",
            ),
            reference = LocalizedFamilyText("الناس: 1–6", "An-Nas 114:1–6"),
            audioUrl = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/114001.mp3",
        ),
    )

    val ruqyahAudio = listOf(
        RuqyahAudioTrack(
            id = "ruqyah_short_surahs",
            title = LocalizedFamilyText("المعوذات الثلاث", "The three protective surahs"),
            description = LocalizedFamilyText("الفلق والناس والإخلاص — تلاوة قرآنية من مصدر EveryAyah.", "Al-Falaq, An-Nas and Al-Ikhlas — Quran recitation from EveryAyah."),
            url = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/113001.mp3",
        ),
        RuqyahAudioTrack(
            id = "ruqyah_fatihah",
            title = LocalizedFamilyText("تلاوة الفاتحة", "Al-Fatihah recitation"),
            description = LocalizedFamilyText("سورة الفاتحة بصوت قارئ موثق من أرشيف EveryAyah.", "Al-Fatihah recited by a listed EveryAyah reciter."),
            url = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/001001.mp3",
        ),
        RuqyahAudioTrack(
            id = "ruqyah_kursi",
            title = LocalizedFamilyText("آية الكرسي صوتيًا", "Ayat al-Kursi audio"),
            description = LocalizedFamilyText("تلاوة الآية 255 من سورة البقرة.", "Recitation of verse 255 from Al-Baqarah."),
            url = "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/002255.mp3",
        ),
    )

    val babyNames: List<IslamicBabyName> = listOf(
        name("adam", "آدم", "Adam", BabyNameGender.Boy, "أبو البشر وأول الأنبياء", "The first human and a prophet"),
        name("ibrahim", "إبراهيم", "Ibrahim", BabyNameGender.Boy, "أبو الأنبياء وخليل الرحمن", "A prophet and the close friend of the Most Merciful"),
        name("ismail", "إسماعيل", "Ismail", BabyNameGender.Boy, "المطيع لله والصابر", "A prophet known for obedience and patience"),
        name("yusuf", "يوسف", "Yusuf", BabyNameGender.Boy, "اسم نبي، ويُذكر بالجمال والعفة", "A prophet associated with beauty and chastity"),
        name("yunus", "يونس", "Yunus", BabyNameGender.Boy, "اسم نبي الله صاحب الحوت", "The prophet known as the companion of the whale"),
        name("musa", "موسى", "Musa", BabyNameGender.Boy, "اسم نبي الله الكليم", "The prophet who spoke with Allah"),
        name("muhammad", "محمد", "Muhammad", BabyNameGender.Boy, "كثير الخصال المحمودة", "One who is greatly praised"),
        name("ahmad", "أحمد", "Ahmad", BabyNameGender.Boy, "الأكثر حمدًا", "The most praiseworthy"),
        name("abdullah", "عبد الله", "Abdullah", BabyNameGender.Boy, "العبد المتذلل لله", "The servant of Allah"),
        name("abdurrahman", "عبد الرحمن", "Abdurrahman", BabyNameGender.Boy, "عبد الرحمن سبحانه", "The servant of the Most Merciful"),
        name("ali", "علي", "Ali", BabyNameGender.Boy, "الرفيع القدر والشريف", "Noble and exalted"),
        name("umar", "عمر", "Umar", BabyNameGender.Boy, "الحياة والعمر الطويل", "Life and long-lived"),
        name("uthman", "عثمان", "Uthman", BabyNameGender.Boy, "اسم الصحابي والخليفة الراشد", "The name of a companion and rightly guided caliph"),
        name("hamza", "حمزة", "Hamza", BabyNameGender.Boy, "القوي الشجاع والأسد", "Strong, brave and lion-like"),
        name("bilal", "بلال", "Bilal", BabyNameGender.Boy, "الماء والندى، واسم مؤذن النبي", "Water and freshness; the Prophet's muadhin"),
        name("zayd", "زيد", "Zayd", BabyNameGender.Boy, "النماء والزيادة", "Growth and increase"),
        name("anas", "أنس", "Anas", BabyNameGender.Boy, "الألفة والطمأنينة", "Affection and companionship"),
        name("saad", "سعد", "Saad", BabyNameGender.Boy, "السعادة واليُمن", "Happiness and good fortune"),
        name("khalid", "خالد", "Khalid", BabyNameGender.Boy, "الباقي والدائم", "Everlasting and enduring"),
        name("rayyan", "ريان", "Rayyan", BabyNameGender.Boy, "المرتوي، وباب من أبواب الجنة للصائمين", "Well-watered; a gate of Paradise for those who fast"),
        name("karim", "كريم", "Karim", BabyNameGender.Boy, "الجواد كثير العطاء", "Generous and noble"),
        name("hadi", "هادي", "Hadi", BabyNameGender.Boy, "المرشد إلى الخير", "A guide to what is good"),
        name("sami", "سامي", "Sami", BabyNameGender.Boy, "العالي الرفيع", "High and elevated"),
        name("nabil", "نبيل", "Nabil", BabyNameGender.Boy, "الشريف ذو الخلق", "Noble and virtuous"),
        name("hasan", "حسن", "Hasan", BabyNameGender.Boy, "الجميل الطيب", "Good and beautiful"),
        name("husayn", "حسين", "Husayn", BabyNameGender.Boy, "الحسن الجميل", "Little good and beautiful one"),
        name("salman", "سلمان", "Salman", BabyNameGender.Boy, "السالم المعافى", "Safe and sound"),
        name("ammar", "عمار", "Ammar", BabyNameGender.Boy, "كثير العبادة والبناء", "One who builds and worships abundantly"),
        name("muadh", "معاذ", "Muadh", BabyNameGender.Boy, "المحفوظ والمعتصم بالله", "Protected and seeking refuge in Allah"),
        name("salah", "صلاح", "Salah", BabyNameGender.Boy, "الاستقامة والخير", "Righteousness and goodness"),
        name("maryam", "مريم", "Maryam", BabyNameGender.Girl, "العابدة الطاهرة، أم عيسى", "The pure worshipper and mother of Isa"),
        name("khadijah", "خديجة", "Khadijah", BabyNameGender.Girl, "اسم أم المؤمنين الأولى", "The name of the first Mother of the Believers"),
        name("aisha", "عائشة", "Aisha", BabyNameGender.Girl, "الحية ذات الحياة الطيبة", "Living and full of good life"),
        name("fatimah", "فاطمة", "Fatimah", BabyNameGender.Girl, "الطاهرة، واسم بنت النبي", "Pure; the name of the Prophet's daughter"),
        name("hafsah", "حفصة", "Hafsah", BabyNameGender.Girl, "اسم أم المؤمنين وحفصة الأسد", "The name of a Mother of the Believers"),
        name("zaynab", "زينب", "Zaynab", BabyNameGender.Girl, "اسم عدد من أمهات المؤمنين والصحابيات", "The name of several Mothers of the Believers and companions"),
        name("ruqayyah", "رقية", "Ruqayyah", BabyNameGender.Girl, "الرفعة والرقي", "Elevation and refinement"),
        name("safiyyah", "صفية", "Safiyyah", BabyNameGender.Girl, "النقية المختارة", "Pure and chosen"),
        name("sumayyah", "سمية", "Sumayyah", BabyNameGender.Girl, "اسم أول شهيدة في الإسلام", "The name of the first martyr in Islam"),
        name("asma", "أسماء", "Asma", BabyNameGender.Girl, "جمع اسم، واسم صحابية جليلة", "Plural of name; the name of a noble companion"),
        name("sarah", "سارة", "Sarah", BabyNameGender.Girl, "اسم زوجة إبراهيم وأم إسحاق", "The wife of Ibrahim and mother of Ishaq"),
        name("hajar", "هاجر", "Hajar", BabyNameGender.Girl, "اسم أم إسماعيل الصابرة", "The patient mother of Ismail"),
        name("huda", "هدى", "Huda", BabyNameGender.Girl, "الرشاد والدلالة إلى الحق", "Guidance to the truth"),
        name("iman", "إيمان", "Iman", BabyNameGender.Girl, "التصديق بالقلب والعمل الصالح", "Faith expressed in belief and good works"),
        name("nur", "نور", "Nur", BabyNameGender.Girl, "الضياء والهداية", "Light and guidance"),
        name("rahmah", "رحمة", "Rahmah", BabyNameGender.Girl, "الرقة والإحسان", "Mercy and kindness"),
        name("amani", "أماني", "Amani", BabyNameGender.Girl, "الأمنيات والآمال الطيبة", "Good hopes and aspirations"),
        name("ayah", "آية", "Ayah", BabyNameGender.Girl, "العلامة والدليل", "A sign and proof"),
        name("bushra", "بشرى", "Bushra", BabyNameGender.Girl, "الخبر السار", "Glad tidings"),
        name("jannah", "جنة", "Jannah", BabyNameGender.Girl, "دار النعيم", "The garden of Paradise"),
        name("tasneem", "تسنيم", "Tasneem", BabyNameGender.Girl, "عين في الجنة", "A spring in Paradise"),
        name("sidra", "سدرة", "Sidra", BabyNameGender.Girl, "شجرة سدرة المنتهى", "The lote tree of the utmost boundary"),
        name("lina", "لينا", "Lina", BabyNameGender.Girl, "النخلة الصغيرة واللين", "A young palm and gentleness"),
        name("salma", "سلمى", "Salma", BabyNameGender.Girl, "السالمة والآمنة", "Safe and peaceful"),
        name("muna", "منى", "Muna", BabyNameGender.Girl, "الأماني والرغبات الطيبة", "Good hopes and wishes"),
        name("maha", "مها", "Maha", BabyNameGender.Girl, "البياض والجمال", "Brightness and beauty"),
        name("yasmin", "ياسمين", "Yasmin", BabyNameGender.Girl, "اسم الزهرة الطيبة الرائحة", "A fragrant flower"),
        name("zahra", "زهراء", "Zahra", BabyNameGender.Girl, "المضيئة والمشرقة", "Radiant and shining"),
        name("salsabil", "سلسبيل", "Salsabil", BabyNameGender.Girl, "عين في الجنة عذبة سهلة الشرب", "A pure, easy-drinking spring in Paradise"),
        name("haneen", "حنين", "Haneen", BabyNameGender.Girl, "الشوق والرقة", "Longing and tenderness"),
    )

    val familyArticles = listOf(
        FamilyGuideArticle(
            id = "engagement",
            title = LocalizedFamilyText("الخطبة والتعارف", "Engagement and getting to know one another"),
            summary = LocalizedFamilyText("الخطبة وعد بالزواج وليست عقدًا، ولها آداب تحفظ الدين والكرامة وحقوق الطرفين.", "An engagement is a promise to marry, not a marriage contract; its etiquette protects faith, dignity and both families."),
            sections = listOf(
                FamilyGuideSection(
                    LocalizedFamilyText("الخطبة ليست زواجًا", "An engagement is not a marriage"),
                    listOf(
                        LocalizedFamilyText("تبقى المرأة أجنبية عن الخاطب حتى يتم عقد النكاح الصحيح؛ فلا خلوة ولا لمس ولا علاقة زوجية، وتكون الزيارات بعلم الأسرة وفي حدود الأدب.", "The prospective couple remain non-mahram until a valid marriage contract. There is no seclusion, touching or marital relationship; meetings should be transparent and respectful."),
                        LocalizedFamilyText("يجوز لكل من الطرفين العدول عن الخطبة بلا ظلم أو تشهير، وتُرد الهدايا والمهر بحسب العرف والاتفاق والقانون المحلي، ويُستفتى أهل العلم عند النزاع.", "Either person may withdraw without harm or public humiliation. Gifts and any payment are handled according to local custom, agreement and law; consult qualified scholars in a dispute."),
                    ),
                ),
                FamilyGuideSection(
                    LocalizedFamilyText("التحقق والاختيار", "Responsible discernment"),
                    listOf(
                        LocalizedFamilyText("يُنظر إلى الدين والخلق والمسؤولية والقدرة على التواصل، ولا يكفي المال أو المظهر. يسأل الطرفان عن التوقعات المتعلقة بالسكن والعمل والإنفاق والأبناء.", "Consider faith, character, responsibility and communication, not wealth or appearance alone. Discuss expectations about housing, work, finances and children."),
                        LocalizedFamilyText("الاستخارة استشارة لله بعد بذل الأسباب، وليست حلمًا لازمًا أو بديلًا عن السؤال والتحقق والاستشارة.", "Istikhara is asking Allah for what is best after taking practical means; it is not a required dream and does not replace checking facts and seeking advice."),
                    ),
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "nikah",
            title = LocalizedFamilyText("عقد الزواج", "The marriage contract"),
            summary = LocalizedFamilyText("النكاح ميثاق غليظ يقوم على الرضا والإيجاب والقبول والمهر والشهود وسائر الشروط المعتبرة.", "Nikah is a serious covenant built on consent, offer and acceptance, mahr, witnesses and the other valid conditions."),
            sections = listOf(
                FamilyGuideSection(
                    LocalizedFamilyText("الأساس والشروط", "Core conditions"),
                    listOf(
                        LocalizedFamilyText("لا يصح إجبار الرجل أو المرأة على الزواج. من أهم ما يذكره الفقهاء: تعيين الزوجين، والرضا، والإيجاب والقبول، والولي عند جمهور أهل السنة، وشاهدا عدل، والمهر، والإعلان وانتفاء الموانع.", "A man or woman must not be forced into marriage. Common Sunni fiqh discussions include identifying the spouses, consent, offer and acceptance, a wali according to the majority, two witnesses, mahr, public announcement and absence of impediments."),
                        LocalizedFamilyText("تختلف بعض التفاصيل بين المذاهب والقوانين، لذلك ينبغي توثيق العقد رسميًا ومراجعة جهة شرعية وقانونية موثوقة في البلد.", "Some details differ between schools and civil laws. Register the marriage officially and consult a trusted scholarly and legal authority in the country."),
                    ),
                ),
                FamilyGuideSection(
                    LocalizedFamilyText("المهر والحقوق المالية", "Mahr and financial rights"),
                    listOf(
                        LocalizedFamilyText("المهر حق خالص للزوجة، يُسمى أو يثبت بمهر المثل، ولا يجوز لوليها أو غيره أخذه بغير رضاها. يتفق الطرفان بوضوح على المعجل والمؤجل.", "Mahr belongs exclusively to the bride. It is agreed or established according to local norms, and no guardian may take it without her consent. Clarify any immediate and deferred amount."),
                        LocalizedFamilyText("النفقة والسكن والكسوة بحسب القدرة والعرف الصحيح، مع التعاون في شؤون البيت دون إسقاط حق أو تحميل طرف ما لا يطيق.", "Maintenance, housing and clothing are provided according to ability and sound custom, while household responsibilities are handled cooperatively without denying either spouse's rights."),
                    ),
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "marital_rights",
            title = LocalizedFamilyText("الحقوق والحياة الزوجية", "Marital rights and life together"),
            summary = LocalizedFamilyText("المعاشرة بالمعروف رحمة وعدل وتعاون، وليست تسلطًا أو إهانة أو أذى.", "Living together in kindness means mercy, justice and cooperation—not control, humiliation or harm."),
            sections = listOf(
                FamilyGuideSection(
                    LocalizedFamilyText("المعاشرة بالمعروف", "Kind companionship"),
                    listOf(
                        LocalizedFamilyText("يحفظ كل زوج كرامة الآخر وخصوصيته، ويشكره على المعروف، ويتجنب نشر أسرار البيت والسخرية والسباب والعنف بكل صوره.", "Each spouse protects the other's dignity and privacy, appreciates good, and avoids exposing household secrets, mockery, insults and every form of violence."),
                        LocalizedFamilyText("التشاور في القرارات الكبرى، والوضوح في المال، وتقسيم المسؤوليات بما يناسب الحال، من أسباب السكينة والاستمرار.", "Consultation about major decisions, financial clarity and a fair division of responsibilities help create tranquility and stability."),
                    ),
                ),
                FamilyGuideSection(
                    LocalizedFamilyText("عند الخلاف", "When conflict occurs"),
                    listOf(
                        LocalizedFamilyText("يبدأ الإصلاح بالهدوء وسماع الطرفين وتحديد المشكلة، ثم الاستعانة بحكمين أو مستشار أسري موثوق. لا يجوز استعمال العنف أو التهديد أو حبس المال للإكراه.", "Begin with calm conversation and hearing both sides, then seek two trusted mediators or a qualified family counselor. Violence, threats and financial coercion are never acceptable."),
                        LocalizedFamilyText("إذا وُجد خطر أو إساءة، فالأولوية للأمان وطلب المساعدة من الجهات المختصة. مسائل الطلاق والخلع والحقوق تحتاج فتوى وحماية قانونية خاصة وليست جوابًا آليًا.", "When abuse or danger exists, safety and qualified support come first. Divorce, khulʿ and legal rights need case-specific scholarly and legal guidance, not an automated answer."),
                    ),
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "parenting",
            title = LocalizedFamilyText("تربية الأبناء", "Raising children"),
            summary = LocalizedFamilyText("التربية أمانة تبدأ بالرحمة والقدوة والحوار، وتجمع بين الحزم العادل وحفظ كرامة الطفل.", "Parenting is a trust built on mercy, example and dialogue, combining fair boundaries with the child's dignity."),
            sections = listOf(
                FamilyGuideSection(
                    LocalizedFamilyText("القدوة والرحمة", "Example and mercy"),
                    listOf(
                        LocalizedFamilyText("يرى الطفل الصدق والصلاة والرحمة في سلوك والديه قبل أن يسمع المواعظ. يُعلّم بالتدرج، ويُشكر على المحاولة، ويُصلح الخطأ دون تحقير أو مقارنة.", "Children see honesty, prayer and mercy in their parents before they hear advice. Teach gradually, praise effort, and correct mistakes without humiliation or comparison."),
                        LocalizedFamilyText("تُراعى مراحل العمر والقدرة، ويُفتح باب السؤال عن العقيدة والجسد والمشاعر بأمان ووضوح مناسبين للسن.", "Respect age and capacity, and make questions about faith, the body and emotions safe and age-appropriate."),
                    ),
                ),
                FamilyGuideSection(
                    LocalizedFamilyText("العدل والحماية", "Justice and protection"),
                    listOf(
                        LocalizedFamilyText("العدل بين الأبناء في العطاء والاهتمام لا يعني التسوية في كل شيء، بل إعطاء كل طفل حاجته دون تفضيل مؤذٍ.", "Justice between children does not mean identical treatment; it means meeting each child's needs without harmful favoritism."),
                        LocalizedFamilyText("حماية الطفل من الاستغلال والتنمر والمحتوى المؤذي واجب مشترك، مع تعليم الخصوصية والاستئذان والحدود وطلب النجدة.", "Protecting children from exploitation, bullying and harmful content is a shared duty. Teach privacy, consent, boundaries and how to seek help."),
                    ),
                ),
            ),
        ),
    )

    fun searchNames(query: String, gender: BabyNameGender? = null): List<IslamicBabyName> {
        val normalized = query.trim().lowercase()
        return babyNames.filter { item ->
            (gender == null || item.gender == gender) &&
                (normalized.isEmpty() || listOf(
                    item.nameArabic,
                    item.transliteration.lowercase(),
                    item.meaningArabic,
                    item.meaningEnglish.lowercase(),
                ).any { it.contains(normalized) })
        }
    }

    fun isSafeAudioUrl(url: String): Boolean =
        url.startsWith("https://everyayah.com/data/") &&
            url.endsWith(".mp3") &&
            !url.contains("..") &&
            !url.contains('\n') &&
            !url.contains('\r')

    private fun name(
        id: String,
        arabic: String,
        transliteration: String,
        gender: BabyNameGender,
        meaningArabic: String,
        meaningEnglish: String,
    ) = IslamicBabyName(id, arabic, transliteration, gender, meaningArabic, meaningEnglish)
}
