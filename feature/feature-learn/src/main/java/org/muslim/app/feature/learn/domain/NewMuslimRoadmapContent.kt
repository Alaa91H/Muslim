package org.muslim.app.feature.learn.domain

data class BeginnerLocalizedText(
    val arabic: String,
    val english: String,
    val french: String,
    val spanish: String,
) {
    fun resolve(language: BeginnerLanguage): String = when (language) {
        BeginnerLanguage.ARABIC -> arabic
        BeginnerLanguage.ENGLISH -> english
        BeginnerLanguage.FRENCH -> french
        BeginnerLanguage.SPANISH -> spanish
    }
}

data class NewMuslimRoadmapStage(
    val id: String,
    val days: Int,
    val title: BeginnerLocalizedText,
    val goal: BeginnerLocalizedText,
    val checklist: List<BeginnerLocalizedText>,
    val lessonIds: List<String>,
)

object NewMuslimRoadmapContent {
    val stages: List<NewMuslimRoadmapStage> = listOf(
        NewMuslimRoadmapStage(
            id = "first_7_days",
            days = 7,
            title = BeginnerLocalizedText(
                arabic = "أول 7 أيام",
                english = "First 7 days",
                french = "Les 7 premiers jours",
                spanish = "Primeros 7 días",
            ),
            goal = BeginnerLocalizedText(
                arabic = "ثبت الأساسيات التي تحتاجها لتبدأ العبادة بهدوء.",
                english = "Stabilise the essentials you need to begin worship calmly.",
                french = "Stabilisez les bases nécessaires pour commencer à pratiquer sereinement.",
                spanish = "Afianza lo esencial para comenzar a practicar con calma.",
            ),
            checklist = listOf(
                BeginnerLocalizedText(
                    "افهم معنى الشهادة والتوحيد.",
                    "Understand the meaning of the Shahadah and tawhid.",
                    "Comprenez le sens de l'attestation et du tawhid.",
                    "Comprende el significado de la Shahadah y el tawhid.",
                ),
                BeginnerLocalizedText(
                    "تعرف على الصلوات الخمس والوضوء وتسلسل الركعة.",
                    "Learn the five prayers, wudu and the basic flow of one rakah.",
                    "Apprenez les cinq prières, les ablutions et le déroulement d'une rak'a.",
                    "Aprende las cinco oraciones, el wudu y la secuencia básica de una rakah.",
                ),
                BeginnerLocalizedText(
                    "ابدأ الفاتحة مع الاستماع والتصحيح.",
                    "Begin Al-Fatihah with listening and correction.",
                    "Commencez Al-Fatiha avec écoute et correction.",
                    "Empieza Al-Fatihah escuchando y corrigiendo la pronunciación.",
                ),
                BeginnerLocalizedText(
                    "اختر شخصًا أو مسجدًا موثوقًا للأسئلة.",
                    "Choose a trusted person or mosque for questions.",
                    "Choisissez une personne ou une mosquée de confiance pour vos questions.",
                    "Elige una persona o mezquita de confianza para tus preguntas.",
                ),
            ),
            lessonIds = listOf(
                "new_muslim_welcome",
                "new_muslim_belief",
                "new_muslim_prayer",
                "new_muslim_purification",
            ),
        ),
        NewMuslimRoadmapStage(
            id = "first_30_days",
            days = 30,
            title = BeginnerLocalizedText(
                arabic = "أول 30 يومًا",
                english = "First 30 days",
                french = "Les 30 premiers jours",
                spanish = "Primeros 30 días",
            ),
            goal = BeginnerLocalizedText(
                arabic = "حوّل الأساسيات إلى روتين قابل للاستمرار.",
                english = "Turn the essentials into a sustainable routine.",
                french = "Transformez les bases en une routine durable.",
                spanish = "Convierte lo esencial en una rutina sostenible.",
            ),
            checklist = listOf(
                BeginnerLocalizedText(
                    "ركز على انتظام الصلاة أكثر من كثرة النوافل.",
                    "Prioritise consistency in prayer over adding many optional acts.",
                    "Privilégiez la régularité de la prière avant de multiplier les actes surérogatoires.",
                    "Prioriza la constancia en la oración antes que añadir muchas prácticas opcionales.",
                ),
                BeginnerLocalizedText(
                    "تعلم الفاتحة وسورًا قصيرة بالتدريج.",
                    "Learn Al-Fatihah and short surahs gradually.",
                    "Apprenez progressivement Al-Fatiha et de courtes sourates.",
                    "Aprende Al-Fatihah y suras cortas de forma gradual.",
                ),
                BeginnerLocalizedText(
                    "اقرأ مقدارًا صغيرًا من القرآن بالمعنى كل يوم.",
                    "Read a small amount of Quran with meaning each day.",
                    "Lisez chaque jour une petite portion du Coran avec son sens.",
                    "Lee cada día una pequeña porción del Corán comprendiendo su significado.",
                ),
                BeginnerLocalizedText(
                    "تعلم الأحكام التي تمس حياتك الحالية فقط أولًا.",
                    "Learn first the rulings that actually affect your current life.",
                    "Apprenez d'abord les règles qui concernent réellement votre vie actuelle.",
                    "Aprende primero las normas que afectan directamente tu vida actual.",
                ),
            ),
            lessonIds = listOf(
                "new_muslim_quran",
                "new_muslim_daily_life",
                "pillars_islam",
                "pillars_iman",
            ),
        ),
        NewMuslimRoadmapStage(
            id = "first_90_days",
            days = 90,
            title = BeginnerLocalizedText(
                arabic = "أول 90 يومًا",
                english = "First 90 days",
                french = "Les 90 premiers jours",
                spanish = "Primeros 90 días",
            ),
            goal = BeginnerLocalizedText(
                arabic = "انتقل من التعلم المتفرق إلى مسارات منظمة وحياة متوازنة.",
                english = "Move from scattered learning to organised paths and balanced daily life.",
                french = "Passez d'un apprentissage dispersé à des parcours structurés et une vie équilibrée.",
                spanish = "Pasa de un aprendizaje disperso a rutas organizadas y una vida equilibrada.",
            ),
            checklist = listOf(
                BeginnerLocalizedText(
                    "أكمل مسارات الطهارة والصلاة والعقيدة والقرآن الأساسية.",
                    "Complete the core purification, prayer, faith and Quran paths.",
                    "Terminez les parcours essentiels de purification, prière, foi et Coran.",
                    "Completa las rutas básicas de purificación, oración, fe y Corán.",
                ),
                BeginnerLocalizedText(
                    "ابنِ صحبة متوازنة تحترم خصوصيتك وتدرجك.",
                    "Build balanced companionship that respects your privacy and pace.",
                    "Construisez un entourage équilibré qui respecte votre vie privée et votre rythme.",
                    "Construye una compañía equilibrada que respete tu privacidad y tu ritmo.",
                ),
                BeginnerLocalizedText(
                    "اختر عادة أخلاقية أو رقمية لتحسينها كل مرة.",
                    "Choose one ethical or digital habit to improve at a time.",
                    "Choisissez une habitude éthique ou numérique à améliorer à la fois.",
                    "Elige un hábito ético o digital para mejorar cada vez.",
                ),
                BeginnerLocalizedText(
                    "استعد مبكرًا لرمضان والزكاة وبقية العبادات عندما يحين وقتها.",
                    "Prepare early for Ramadan, zakat and other acts of worship when they become relevant.",
                    "Préparez-vous à l'avance pour Ramadan, la zakat et les autres actes lorsqu'ils deviennent pertinents.",
                    "Prepárate con antelación para Ramadán, zakat y otras prácticas cuando correspondan.",
                ),
            ),
            lessonIds = listOf(
                "faith_tawhid_worship",
                "wudu",
                "salah",
                "quran_understanding",
                "ethics_foundation",
                "new_muslim_roadmap",
            ),
        ),
    )
}
