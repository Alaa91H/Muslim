package org.muslim.app.feature.learn.domain

/**
 * Offline starter material inspired by the progression of Qaida Noorania and
 * Baghdadia primers. It is intentionally not a reproduction of either text.
 * A qualified Arabic/Quran teacher should review pronunciation with learners.
 */
data class NooraniText(
    val arabic: String,
    val english: String,
) {
    fun resolve(isArabic: Boolean): String = if (isArabic) arabic else english
}

enum class MakhrajGroup(
    val title: NooraniText,
    val cue: NooraniText,
) {
    THROAT(
        NooraniText("الحلق", "Throat"),
        NooraniText("راقب خروج الهواء من الحلق", "Notice the airflow from the throat"),
    ),
    TONGUE(
        NooraniText("اللسان", "Tongue"),
        NooraniText("ضع اللسان برفق ولا تكلّف", "Place the tongue gently; do not strain"),
    ),
    LIPS(
        NooraniText("الشفتان", "Lips"),
        NooraniText("انتبه لانطباق الشفتين أو تدويرهما", "Notice lip closure or rounding"),
    ),
    OPEN_MOUTH(
        NooraniText("الفم المفتوح", "Open mouth"),
        NooraniText("افتح الفم بلا ضغط واطلب تصحيحاً", "Open naturally and ask for feedback"),
    ),
}

data class ArabicLetter(
    val id: String,
    val display: String,
    val spokenArabic: String,
    val name: NooraniText,
    val example: NooraniText,
    val group: MakhrajGroup,
)

data class ReadingStage(
    val id: String,
    val title: NooraniText,
    val description: NooraniText,
    val samples: List<String>,
)

enum class BeginnerLanguage(val label: String) {
    ARABIC("العربية"),
    ENGLISH("English"),
    FRENCH("Français"),
    SPANISH("Español"),
}

data class NewMuslimStep(
    val title: String,
    val description: String,
    val arabicPhrase: String? = null,
)

data class NewMuslimGuide(
    val welcome: String,
    val steps: List<NewMuslimStep>,
    val reviewNote: String,
)

object NooraniContent {
    val letters = listOf(
        letter("alif", "ا", "أَلِف", "ألف", "البدء مع فتحة: أَ", MakhrajGroup.OPEN_MOUTH),
        letter("ba", "ب", "باء", "باء", "مثال تدريبي: بَ", MakhrajGroup.LIPS),
        letter("ta", "ت", "تاء", "تاء", "مثال تدريبي: تَ", MakhrajGroup.TONGUE),
        letter("tha", "ث", "ثاء", "ثاء", "مثال تدريبي: ثَ", MakhrajGroup.TONGUE),
        letter("jim", "ج", "جيم", "جيم", "مثال تدريبي: جَ", MakhrajGroup.TONGUE),
        letter("ha", "ح", "حاء", "حاء", "مثال تدريبي: حَ", MakhrajGroup.THROAT),
        letter("kha", "خ", "خاء", "خاء", "مثال تدريبي: خَ", MakhrajGroup.THROAT),
        letter("dal", "د", "دال", "دال", "مثال تدريبي: دَ", MakhrajGroup.TONGUE),
        letter("dhal", "ذ", "ذال", "ذال", "مثال تدريبي: ذَ", MakhrajGroup.TONGUE),
        letter("ra", "ر", "راء", "راء", "مثال تدريبي: رَ", MakhrajGroup.TONGUE),
        letter("zay", "ز", "زاي", "زاي", "مثال تدريبي: زَ", MakhrajGroup.TONGUE),
        letter("sin", "س", "سين", "سين", "مثال تدريبي: سَ", MakhrajGroup.TONGUE),
        letter("shin", "ش", "شين", "شين", "مثال تدريبي: شَ", MakhrajGroup.TONGUE),
        letter("sad", "ص", "صاد", "صاد", "مثال تدريبي: صَ", MakhrajGroup.TONGUE),
        letter("dad", "ض", "ضاد", "ضاد", "مثال تدريبي: ضَ", MakhrajGroup.TONGUE),
        letter("ta-emphatic", "ط", "طاء", "طاء", "مثال تدريبي: طَ", MakhrajGroup.TONGUE),
        letter("dha", "ظ", "ظاء", "ظاء", "مثال تدريبي: ظَ", MakhrajGroup.TONGUE),
        letter("ayn", "ع", "عين", "عين", "مثال تدريبي: عَ", MakhrajGroup.THROAT),
        letter("ghayn", "غ", "غين", "غين", "مثال تدريبي: غَ", MakhrajGroup.THROAT),
        letter("fa", "ف", "فاء", "فاء", "مثال تدريبي: فَ", MakhrajGroup.LIPS),
        letter("qaf", "ق", "قاف", "قاف", "مثال تدريبي: قَ", MakhrajGroup.TONGUE),
        letter("kaf", "ك", "كاف", "كاف", "مثال تدريبي: كَ", MakhrajGroup.TONGUE),
        letter("lam", "ل", "لام", "لام", "مثال تدريبي: لَ", MakhrajGroup.TONGUE),
        letter("mim", "م", "ميم", "ميم", "مثال تدريبي: مَ", MakhrajGroup.LIPS),
        letter("nun", "ن", "نون", "نون", "مثال تدريبي: نَ", MakhrajGroup.TONGUE),
        letter("ha-final", "ه", "هاء", "هاء", "مثال تدريبي: هَ", MakhrajGroup.THROAT),
        letter("waw", "و", "واو", "واو", "مثال تدريبي: وَ", MakhrajGroup.LIPS),
        letter("ya", "ي", "ياء", "ياء", "مثال تدريبي: يَ", MakhrajGroup.TONGUE),
    )

    val stages = listOf(
        ReadingStage(
            id = "harakat",
            title = NooraniText("الحركات القصيرة", "Short vowels"),
            description = NooraniText(
                "تدرّب على الفتحة والضمة والكسرة مع الحرف نفسه.",
                "Practise fatḥah, ḍammah and kasrah with the same letter.",
            ),
            samples = listOf("بَ", "بُ", "بِ"),
        ),
        ReadingStage(
            id = "madd",
            title = NooraniText("حروف المد", "Long vowels"),
            description = NooraniText(
                "مدّ الصوت برفق ثم قارنه بنطق معلم موثوق.",
                "Lengthen the sound gently, then compare it with a trusted teacher.",
            ),
            samples = listOf("بَا", "بُو", "بِي"),
        ),
        ReadingStage(
            id = "sukun",
            title = NooraniText("السكون والشدة", "Sukūn and shaddah"),
            description = NooraniText(
                "توقف خفيف في السكون، وراجع الشدة مع المعلم.",
                "Use a light stop for sukūn and review shaddah with a teacher.",
            ),
            samples = listOf("أَبْ", "أَبَّ", "مِنْ"),
        ),
    )

    fun guide(language: BeginnerLanguage): NewMuslimGuide = when (language) {
        BeginnerLanguage.ARABIC -> arabicGuide
        BeginnerLanguage.ENGLISH -> englishGuide
        BeginnerLanguage.FRENCH -> frenchGuide
        BeginnerLanguage.SPANISH -> spanishGuide
    }

    private fun letter(
        id: String,
        display: String,
        spoken: String,
        arabicName: String,
        arabicExample: String,
        group: MakhrajGroup,
    ) = ArabicLetter(
        id = id,
        display = display,
        spokenArabic = spoken,
        name = NooraniText(arabicName, spoken),
        example = NooraniText(arabicExample, "Practice sound: $display with a short vowel"),
        group = group,
    )

    private val arabicGuide = NewMuslimGuide(
        welcome = "مرحباً بك. هذا دليل تعليمي قصير؛ الدخول في الإسلام اختيار حر وشخصي، وخذ وقتك للسؤال والتعلّم.",
        steps = listOf(
            NewMuslimStep(
                "الإيمان والشهادة",
                "إذا آمنت بالله وحده وأن محمداً رسول الله، فقل الشهادتين عن اقتناع. يمكن لمسجد أو معلم موثوق أن يساندك، لكنه ليس بديلاً عن قرارك الشخصي.",
                "أشهد أن لا إله إلا الله، وأشهد أن محمداً رسول الله",
            ),
            NewMuslimStep(
                "ابدأ بالطهارة",
                "تعلّم الوضوء للصلاة بهدوء: غسل الوجه، واليدين إلى المرفقين، ومسح الرأس، وغسل القدمين إلى الكعبين. توجد تفاصيل معتبرة؛ راجع دليلاً موثوقاً أو معلماً.",
            ),
            NewMuslimStep(
                "خطوات أولى قابلة للتدرج",
                "ابدأ بتعلّم الصلاة والفاتحة شيئاً فشيئاً، واقرأ ترجمة للقرآن، ولا بأس أن تسأل عن أي أمر لا تفهمه.",
            ),
            NewMuslimStep(
                "ابنِ صحبة داعمة",
                "تواصل مع مسجد أو معلّم أو مجتمع موثوق يحترم خصوصيتك، وخذ التعلم بوتيرة تناسبك.",
            ),
        ),
        reviewNote = "هذا تبسيط تعليمي، لا فتوى فردية. قد تختلف بعض التفاصيل الفقهية؛ اعرض تطبيقك العملي على معلّم أو جهة علمية موثوقة.",
    )

    private val englishGuide = NewMuslimGuide(
        welcome = "Welcome. This is a short educational guide; accepting Islam is a free, personal decision. Take your time to ask and learn.",
        steps = listOf(
            NewMuslimStep(
                "Faith and testimony",
                "If you believe that Allah alone is worthy of worship and that Muhammad is His Messenger, say the testimony with conviction. A trusted mosque or teacher can support you, but your decision is personal.",
                "Ashhadu an la ilaha illa Allah, wa ashhadu anna Muhammadan rasul Allah",
            ),
            NewMuslimStep(
                "Begin with purification",
                "Learn wudu for prayer calmly: wash the face, arms to the elbows, wipe the head, and wash the feet to the ankles. There are recognised scholarly details, so learn with a trusted guide or teacher.",
            ),
            NewMuslimStep(
                "First steps at your pace",
                "Begin learning the prayer and Al-Fatihah gradually, read a Quran translation, and feel free to ask about anything you do not understand.",
            ),
            NewMuslimStep(
                "Build supportive company",
                "Connect with a mosque, teacher, or trusted community that respects your privacy, and learn at a pace that works for you.",
            ),
        ),
        reviewNote = "This is an educational summary, not a personal fatwa. Practical details can differ among scholarly schools; review your practice with a trusted teacher or scholarly organisation.",
    )

    private val frenchGuide = NewMuslimGuide(
        welcome = "Bienvenue. Ce guide très court est éducatif ; embrasser l’islam est un choix libre et personnel. Prenez le temps de poser vos questions et d’apprendre.",
        steps = listOf(
            NewMuslimStep(
                "Foi et attestation",
                "Si vous croyez qu’Allah seul mérite l’adoration et que Muhammad est Son Messager, prononcez l’attestation avec conviction. Une mosquée ou un enseignant de confiance peut vous accompagner, mais votre décision est personnelle.",
                "Ashhadu an la ilaha illa Allah, wa ashhadu anna Muhammadan rasul Allah",
            ),
            NewMuslimStep(
                "Commencer par la purification",
                "Apprenez calmement les ablutions pour la prière : laver le visage, les bras jusqu’aux coudes, essuyer la tête et laver les pieds jusqu’aux chevilles. Certains détails reconnus varient ; apprenez avec un guide fiable.",
            ),
            NewMuslimStep(
                "Premiers pas à votre rythme",
                "Apprenez progressivement la prière et Al-Fatiha, lisez une traduction du Coran et posez vos questions quand vous en avez.",
            ),
            NewMuslimStep(
                "Trouver un entourage bienveillant",
                "Contactez une mosquée, un enseignant ou une communauté fiable qui respecte votre vie privée, et avancez à votre rythme.",
            ),
        ),
        reviewNote = "Ceci est un résumé éducatif, non une fatwa personnelle. Des détails pratiques peuvent différer selon les écoles ; vérifiez votre pratique avec un enseignant ou une organisation savante de confiance.",
    )

    private val spanishGuide = NewMuslimGuide(
        welcome = "Bienvenido. Esta es una guía educativa breve; aceptar el islam es una decisión libre y personal. Tómate el tiempo para preguntar y aprender.",
        steps = listOf(
            NewMuslimStep(
                "Fe y testimonio",
                "Si crees que solo Allah merece adoración y que Muhammad es Su Mensajero, pronuncia el testimonio con convicción. Una mezquita o un maestro de confianza puede acompañarte, pero la decisión es personal.",
                "Ashhadu an la ilaha illa Allah, wa ashhadu anna Muhammadan rasul Allah",
            ),
            NewMuslimStep(
                "Empezar con la purificación",
                "Aprende el wudu para la oración con calma: lava la cara, los brazos hasta los codos, pasa las manos por la cabeza y lava los pies hasta los tobillos. Hay detalles reconocidos entre estudiosos; aprende con una guía fiable.",
            ),
            NewMuslimStep(
                "Primeros pasos a tu ritmo",
                "Aprende gradualmente la oración y Al-Fatihah, lee una traducción del Corán y pregunta libremente lo que no entiendas.",
            ),
            NewMuslimStep(
                "Crear una compañía de apoyo",
                "Conecta con una mezquita, un maestro o una comunidad de confianza que respete tu privacidad, y aprende al ritmo que te resulte adecuado.",
            ),
        ),
        reviewNote = "Este es un resumen educativo, no una fatwa personal. Los detalles prácticos pueden diferir entre escuelas; revisa tu práctica con un maestro u organización académica de confianza.",
    )
}
