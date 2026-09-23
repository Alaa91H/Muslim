package org.muslim.app.feature.family.domain

/**
 * Curated expansion for the baby-name catalogue.
 *
 * Meanings are concise language aids rather than religious rulings. Entries with
 * a historical note identify well-known Quranic or early-Muslim usage without
 * treating that association as a guarantee that every spelling or modern usage
 * is automatically recommended.
 */
object FamilyNamesExpansion {
    val names: List<IslamicBabyName> = listOf(
        prophet("zakariya", "زكريا", "Zakariya", "اسم نبي من أنبياء الله", "The name of a prophet", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("yahya", "يحيى", "Yahya", "اسم نبي من أنبياء الله", "The name of a prophet", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("isa", "عيسى", "Isa", "اسم نبي الله عيسى", "The name of Prophet Isa", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("dawud", "داود", "Dawud", "اسم نبي الله داود", "The name of Prophet Dawud", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("sulayman", "سليمان", "Sulayman", "اسم نبي الله سليمان", "The name of Prophet Sulayman", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("ayyub", "أيوب", "Ayyub", "اسم نبي ارتبط بالصبر", "The name of a prophet associated with patience", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("idris", "إدريس", "Idris", "اسم نبي من أنبياء الله", "The name of a prophet", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("harun", "هارون", "Harun", "اسم نبي الله هارون", "The name of Prophet Harun", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("ishaq", "إسحاق", "Ishaq", "اسم نبي الله إسحاق", "The name of Prophet Ishaq", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("yaqub", "يعقوب", "Yaqub", "اسم نبي الله يعقوب", "The name of Prophet Yaqub", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("nuh", "نوح", "Nuh", "اسم نبي الله نوح", "The name of Prophet Nuh", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("hud", "هود", "Hud", "اسم نبي الله هود", "The name of Prophet Hud", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("salih", "صالح", "Salih", "الصالح المستقيم، واسم نبي", "Righteous and upright; also the name of a prophet", "اسم عربي واسم نبي ورد في القرآن", "An Arabic name and a prophet named in the Quran"),
        prophet("shuayb", "شعيب", "Shuayb", "اسم نبي الله شعيب", "The name of Prophet Shuayb", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("ilyas", "إلياس", "Ilyas", "اسم نبي من أنبياء الله", "The name of a prophet", "اسم نبي ورد في القرآن", "A prophet named in the Quran"),
        prophet("luqman", "لقمان", "Luqman", "اسم لقمان الحكيم", "The name of Luqman the Wise", "ورد اسمه في القرآن", "His name appears in the Quran"),
        arabicBoy("imran", "عمران", "Imran", "العمران والبناء والازدهار", "Building, flourishing and prosperity", "ورد الاسم في القرآن", "The name appears in the Quran"),
        arabicBoy("talha", "طلحة", "Talha", "اسم شجرة، واسم صحابي مشهور", "A tree name and the name of a well-known companion", "اسم صحابي مشهور", "A well-known companion's name"),
        arabicBoy("zubayr", "الزبير", "Zubayr", "اسم عربي قديم", "A classical Arabic name", "اسم صحابي مشهور", "A well-known companion's name"),
        arabicBoy("musab", "مصعب", "Musab", "القوي أو المحتمل للشدة", "Strong or able to endure hardship", "اسم صحابي مشهور", "A well-known companion's name"),
        arabicBoy("jaafar", "جعفر", "Jaafar", "النهر الصغير أو كثير الماء", "A small river or abundant water", "اسم عربي قديم", "A classical Arabic name"),
        arabicBoy("tariq", "طارق", "Tariq", "الآتي ليلًا، والطارق اسم وارد في القرآن", "One who arrives by night; al-Tariq also appears in the Quran", "اسم عربي قرآني الاستعمال", "An Arabic name with Quranic usage"),
        arabicBoy("bashir", "بشير", "Bashir", "حامل البشارة والخبر السار", "Bearer of glad tidings", "اسم عربي حسن المعنى", "An Arabic name with a positive meaning"),
        arabicBoy("munir", "منير", "Munir", "المضيء والمشرق", "Luminous and bright", "اسم عربي", "Arabic"),
        arabicBoy("mahir", "ماهر", "Mahir", "المتقن والبارع", "Skilled and proficient", "اسم عربي", "Arabic"),
        arabicBoy("adil", "عادل", "Adil", "المتصف بالعدل", "Just and fair", "اسم عربي", "Arabic"),
        arabicBoy("amin", "أمين", "Amin", "الموثوق الصادق", "Trustworthy and honest", "اسم عربي", "Arabic"),
        arabicBoy("rafiq", "رفيق", "Rafiq", "الصاحب اللطيف", "A kind companion", "اسم عربي", "Arabic"),
        arabicBoy("hakim", "حكيم", "Hakim", "ذو الحكمة وحسن التقدير", "Wise and sound in judgment", "اسم عربي؛ ويُراعى عدم قصد الاسم الإلهي المختص", "Arabic; avoid intending a divine name in a uniquely divine sense"),
        arabicBoy("rashid", "راشد", "Rashid", "المهتدي إلى الصواب", "Rightly guided and sensible", "اسم عربي", "Arabic"),
        arabicBoy("faisal", "فيصل", "Faisal", "الفاصل والحاكم بين الأمور", "One who distinguishes or decides between matters", "اسم عربي", "Arabic"),
        arabicBoy("iyad", "إياد", "Iyad", "السند والقوة", "Support and strength", "اسم عربي", "Arabic"),
        arabicBoy("tamim", "تميم", "Tamim", "التام في صفاته", "Complete or well-formed", "اسم عربي قديم", "A classical Arabic name"),
        arabicBoy("thabit", "ثابت", "Thabit", "الراسخ المستقر", "Steadfast and firm", "اسم عربي", "Arabic"),
        arabicBoy("badr", "بدر", "Badr", "القمر ليلة اكتماله", "The full moon", "اسم عربي", "Arabic"),
        arabicBoy("fadl", "فضل", "Fadl", "الزيادة والخير والإحسان", "Grace, goodness and generosity", "اسم عربي", "Arabic"),
        arabicBoy("ridwan", "رضوان", "Ridwan", "الرضا والقبول", "Contentment and approval", "اسم عربي", "Arabic"),
        arabicBoy("suhayb", "صهيب", "Suhayb", "اسم عربي قديم", "A classical Arabic name", "اسم صحابي مشهور", "A well-known companion's name"),
        arabicBoy("hudhayfah", "حذيفة", "Hudhayfah", "اسم عربي قديم", "A classical Arabic name", "اسم صحابي مشهور", "A well-known companion's name"),
        arabicBoy("miqdad", "المقداد", "Miqdad", "اسم عربي قديم", "A classical Arabic name", "اسم صحابي مشهور", "A well-known companion's name"),

        arabicGirl("aminah", "آمنة", "Aminah", "الآمنة المطمئنة", "Safe and secure", "اسم عربي مشهور في السيرة", "A classical Arabic name known from the sira"),
        arabicGirl("halimah", "حليمة", "Halimah", "الحليمة الصبورة المتأنية", "Forbearing, patient and gentle", "اسم عربي مشهور في السيرة", "A classical Arabic name known from the sira"),
        arabicGirl("juwayriyah", "جويرية", "Juwayriyah", "اسم عربي قديم", "A classical Arabic name", "اسم أم من أمهات المؤمنين", "The name of a Mother of the Believers"),
        arabicGirl("maymunah", "ميمونة", "Maymunah", "المباركة والميمونة", "Blessed and fortunate", "اسم أم من أمهات المؤمنين", "The name of a Mother of the Believers"),
        arabicGirl("nusaybah", "نسيبة", "Nusaybah", "اسم عربي قديم", "A classical Arabic name", "اسم صحابية مشهورة", "A well-known companion woman's name"),
        arabicGirl("rufaida", "رفيدة", "Rufaida", "العون والدعم", "Aid and support", "اسم عربي متداول", "An Arabic name in common usage"),
        arabicGirl("shifa", "شفاء", "Shifa", "البرء والعافية", "Healing and recovery", "لفظ عربي ورد في القرآن", "An Arabic word used in the Quran"),
        arabicGirl("marwa", "مروة", "Marwa", "اسم جبل المروة من شعائر الحج", "The name of al-Marwah, one of the Hajj landmarks", "اسم مرتبط بشعيرة الصفا والمروة", "Associated with the rite of Safa and Marwah"),
        arabicGirl("safa", "صفا", "Safa", "الصفاء، واسم جبل الصفا", "Clarity and purity; also al-Safa", "اسم مرتبط بشعيرة الصفا والمروة", "Associated with the rite of Safa and Marwah"),
        arabicGirl("kawthar", "كوثر", "Kawthar", "الخير الكثير", "Abundant goodness", "لفظ قرآني", "A Quranic word"),
        arabicGirl("firdaus", "فردوس", "Firdaus", "الفردوس والجنة العالية", "Paradise and the highest gardens", "لفظ وارد في القرآن", "A word used in the Quran"),
        arabicGirl("duha", "ضحى", "Duha", "وقت ارتفاع النهار وإشراقه", "The bright forenoon", "لفظ قرآني واسم سورة", "A Quranic word and surah name"),
        arabicGirl("shuruq", "شروق", "Shuruq", "طلوع الشمس وإضاءتها", "Sunrise and first light", "اسم عربي", "Arabic"),
        arabicGirl("amal", "أمل", "Amal", "الرجاء والتطلع إلى الخير", "Hope and positive expectation", "اسم عربي", "Arabic"),
        arabicGirl("nada", "ندى", "Nada", "قطرات الندى والكرم", "Dewdrops and generosity", "اسم عربي", "Arabic"),
        arabicGirl("wafa", "وفاء", "Wafa", "الإخلاص والوفاء بالعهد", "Loyalty and faithfulness", "اسم عربي", "Arabic"),
        arabicGirl("najwa", "نجوى", "Najwa", "الحديث الخفي والمناجاة", "Private conversation or intimate supplication", "لفظ عربي ورد في القرآن", "An Arabic word used in the Quran"),
        arabicGirl("manal", "منال", "Manal", "ما يُنال ويُدرك", "That which is attained", "اسم عربي", "Arabic"),
        arabicGirl("reem", "ريم", "Reem", "الغزال الأبيض في الاستعمال العربي", "A white gazelle in Arabic usage", "اسم عربي", "Arabic"),
        arabicGirl("ghada", "غادة", "Ghada", "الرقيقة اللينة", "Graceful and gentle", "اسم عربي", "Arabic"),
        arabicGirl("lamia", "لمياء", "Lamia", "اسم عربي قديم من أوصاف الجمال", "A classical Arabic name associated with beauty", "اسم عربي", "Arabic"),
        arabicGirl("rania", "رانيا", "Rania", "الناظرة بإمعان", "One who looks attentively", "اسم عربي", "Arabic"),
        arabicGirl("samira", "سميرة", "Samira", "رفيقة السمر والحديث ليلًا", "A companion in evening conversation", "اسم عربي", "Arabic"),
        arabicGirl("suhaila", "سهيلة", "Suhaila", "السهلة اللطيفة", "Gentle and easy-going", "اسم عربي", "Arabic"),
        arabicGirl("karima", "كريمة", "Karima", "الكريمة الشريفة السخية", "Generous and noble", "اسم عربي", "Arabic"),
        arabicGirl("afaf", "عفاف", "Afaf", "العفة والنزاهة", "Chastity and integrity", "اسم عربي", "Arabic"),
        arabicGirl("ihsan", "إحسان", "Ihsan", "الإتقان وفعل الخير", "Excellence and doing good", "لفظ عربي قرآني", "An Arabic Quranic term"),
        arabicGirl("sakinah", "سكينة", "Sakinah", "الطمأنينة والهدوء", "Tranquillity and calm", "لفظ وارد في القرآن", "A word used in the Quran"),
        arabicGirl("mawaddah", "مودة", "Mawaddah", "المحبة الصادقة", "Deep affection", "لفظ وارد في القرآن", "A word used in the Quran"),
        arabicGirl("hikmah", "حكمة", "Hikmah", "الحكمة وحسن التقدير", "Wisdom and sound judgment", "لفظ عربي قرآني", "An Arabic Quranic term"),
    )

    private fun arabicBoy(
        id: String,
        arabic: String,
        transliteration: String,
        meaningArabic: String,
        meaningEnglish: String,
        noteArabic: String,
        noteEnglish: String,
    ) = arabicName(
        id,
        arabic,
        transliteration,
        BabyNameGender.Boy,
        meaningArabic,
        meaningEnglish,
        LocalizedFamilyText(noteArabic, noteEnglish),
    )

    private fun arabicGirl(
        id: String,
        arabic: String,
        transliteration: String,
        meaningArabic: String,
        meaningEnglish: String,
        noteArabic: String,
        noteEnglish: String,
    ) = arabicName(
        id,
        arabic,
        transliteration,
        BabyNameGender.Girl,
        meaningArabic,
        meaningEnglish,
        LocalizedFamilyText(noteArabic, noteEnglish),
    )

    private fun arabicName(
        id: String,
        arabic: String,
        transliteration: String,
        gender: BabyNameGender,
        meaningArabic: String,
        meaningEnglish: String,
        note: LocalizedFamilyText,
    ) = IslamicBabyName(
        id = id,
        nameArabic = arabic,
        transliteration = transliteration,
        gender = gender,
        meaningArabic = meaningArabic,
        meaningEnglish = meaningEnglish,
        origin = LocalizedFamilyText("عربي أو مستعمل عربيًا", "Arabic or established Arabic usage"),
        note = note,
    )

    private fun prophet(
        id: String,
        arabic: String,
        transliteration: String,
        meaningArabic: String,
        meaningEnglish: String,
        noteArabic: String,
        noteEnglish: String,
    ) = IslamicBabyName(
        id = id,
        nameArabic = arabic,
        transliteration = transliteration,
        gender = BabyNameGender.Boy,
        meaningArabic = meaningArabic,
        meaningEnglish = meaningEnglish,
        note = LocalizedFamilyText(noteArabic, noteEnglish),
    )

}
