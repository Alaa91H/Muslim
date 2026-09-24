package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object NewMuslimLearningContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            newMuslimWelcome,
            newMuslimBelief,
            newMuslimPrayer,
            newMuslimPurification,
            newMuslimQuran,
            newMuslimDailyLife,
            newMuslimRoadmap,
        )
    }

    private val newMuslimWelcome = LearningLesson(
        id = "new_muslim_welcome",
        titleRes = R.string.learn_topic_new_muslim_welcome,
        subtitleRes = R.string.learn_topic_new_muslim_welcome_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "start",
                title = "ابدأ من الأصل",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الدخول في الإسلام قرار إيماني شخصي، وليس سباقًا لحفظ كل الأحكام في أيام قليلة. ابدأ بالتوحيد والشهادة، ثم ابنِ العبادة والعلم والسلوك تدريجيًا.",
                    ),
                ),
            ),
            LearningSection(
                id = "mercy",
                title = "لا تجعل كثرة المعلومات حاجزًا",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "قرر القرآن أن الله لا يكلف نفسًا إلا وسعها.",
                        referenceIds = listOf("quran_2_286"),
                    ),
                    LearningContentBlock.Callout(
                        body = "إذا أخطأت في تفصيل لم تتعلمه بعد، صحح ما تستطيع وتعلم بهدوء. لا تجعل الخوف من الخطأ سببًا لترك الصلاة أو الابتعاد عن المجتمع.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "support",
                title = "اختر مصادر الدعم",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("معلم موثوق", "شخص يشرح بهدوء ويقبل الأسئلة ولا يستغل حاجتك أو خصوصيتك."),
                            LearningStepItem("مسجد أو مجتمع آمن", "مكان يساعدك على الصلاة والتعلم ويترك لك مساحة للتدرج."),
                            LearningStepItem("مصادر واضحة", "استخدم القرآن والترجمة الموثوقة والدروس التي تذكر مصادرها بدل المقاطع المجهولة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "identity",
                title = "الإسلام لا يمحو شخصيتك",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قد تتغير بعض العبادات والعادات والحدود، لكنك لا تحتاج إلى تغيير اسمك أو لغتك أو ثقافتك كلها لمجرد أنك أصبحت مسلمًا. ما يحتاج تغييرًا يحدد بدليل، لا بضغط اجتماعي.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_286", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:286", "Quran 2:286"),
        ),
    )

    private val newMuslimBelief = LearningLesson(
        id = "new_muslim_belief",
        titleRes = R.string.learn_topic_new_muslim_belief,
        subtitleRes = R.string.learn_topic_new_muslim_belief_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "shahada",
                title = "الشهادتان",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الشهادة تعني الإقرار بأن لا معبود بحق إلا الله وأن محمدًا رسول الله، مع قبول ذلك عن علم واقتناع. ليست مجرد جملة صوتية منفصلة عن المعنى.",
                    ),
                ),
            ),
            LearningSection(
                id = "pillars",
                title = "خريطة الإيمان الأولى",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الله", "اعبد الله وحده ولا تجعل العبادة لغيره."),
                            LearningStepItem("الرسل والوحي", "آمن برسل الله وبما أنزل، وخاتمهم محمد ﷺ."),
                            LearningStepItem("الآخرة", "الحياة ليست منتهية بالموت؛ هناك بعث وحساب وجزاء."),
                            LearningStepItem("القدر", "الإيمان بعلم الله وقدره لا يلغي مسؤولية الإنسان عن اختياراته."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "questions",
                title = "الأسئلة ليست مشكلة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "وجود سؤال أو صعوبة في فهم مسألة لا يخرجك من مسار التعلم. دوّن السؤال، وميز بين الشك العابر وبين الاعتراض العلمي، وابحث عن شرح موثوق بدل الغرق في مقاطع متعارضة.",
                    ),
                ),
            ),
            LearningSection(
                id = "hope",
                title = "لا تيأس من الماضي",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "نهى القرآن عن القنوط من رحمة الله وفتح باب الرجوع إليه.",
                        referenceIds = listOf("quran_39_53"),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_39_53", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الزمر، الآية 39:53", "Quran 39:53"),
        ),
    )

    private val newMuslimPrayer = LearningLesson(
        id = "new_muslim_prayer",
        titleRes = R.string.learn_topic_new_muslim_prayer,
        subtitleRes = R.string.learn_topic_new_muslim_prayer_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "priority",
                title = "الصلاة أول مهارة عملية",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر الله موسى بإقامة الصلاة لذكره، وهو معنى مركزي في وظيفة الصلاة.",
                        referenceIds = listOf("quran_20_14"),
                    ),
                ),
            ),
            LearningSection(
                id = "first_week",
                title = "ما الذي تتعلمه أولًا؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الأوقات", "اعرف أسماء الصلوات الخمس وترتيبها ووقت كل واحدة من ميزة مواقيت الصلاة."),
                            LearningStepItem("الوضوء", "تعلم الحد الأدنى الصحيح ثم وسع معرفتك بالسنن والتفاصيل."),
                            LearningStepItem("ركعة نموذجية", "تعلم التكبير والقيام والركوع والسجود والجلوس على ترتيب صحيح."),
                            LearningStepItem("الفاتحة", "ابدأ بحفظها مع تصحيح النطق تدريجيًا، ولا تنتظر الكمال قبل أن تبدأ التعلم العملي."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "tools",
                title = "استخدم أقسام التطبيق",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ارجع إلى مسار الصلاة الكامل في Learn لشرح الشروط والأركان والسهو والجماعة والسفر والمرض، واستخدم مواقيت الصلاة والقبلة كأدوات مساعدة مستقلة.",
                    ),
                ),
            ),
            LearningSection(
                id = "mistakes",
                title = "إذا أخطأت في الصلاة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "ليست كل الأخطاء في درجة واحدة؛ بعض المسائل ركن وبعضها واجب أو سنة، وبعضها يعالج بسجود السهو. إذا وقع خطأ لا تعرف حكمه، سجله واسأل بدل إعادة كل صلاة بسبب الشك.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_20_14", LearningReferenceKind.QURAN, "القرآن الكريم، سورة طه، الآية 20:14", "Quran 20:14"),
        ),
    )

    private val newMuslimPurification = LearningLesson(
        id = "new_muslim_purification",
        titleRes = R.string.learn_topic_new_muslim_purification,
        subtitleRes = R.string.learn_topic_new_muslim_purification_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "purpose",
                title = "الطهارة للصلاة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الطهارة العملية تبدأ بمعرفة الوضوء وما ينقضه، ومتى تحتاج إلى الغسل، وماذا تفعل عند تعذر الماء. لا تحتاج إلى دراسة كل الفروع قبل أول صلاة.",
                    ),
                ),
            ),
            LearningSection(
                id = "wudu",
                title = "الوضوء خطوة بخطوة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("النية", "اعرف في قلبك أنك تتوضأ للصلاة أو للطهارة."),
                            LearningStepItem("الأعضاء الأساسية", "اغسل الوجه واليدين إلى المرفقين، امسح الرأس، واغسل القدمين إلى الكعبين وفق الدليل التفصيلي."),
                            LearningStepItem("الترتيب والتعلم", "استخدم درس الوضوء الكامل لتتعلم الواجبات والسنن والأخطاء الشائعة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "ghusl",
                title = "متى أحتاج الغسل؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "للغسل أسباب معروفة مرتبطة بالجنابة والحيض والنفاس وغيرها. إذا كانت حالتك خاصة أو فيها تفاصيل صحية فارجع إلى درس الغسل الكامل أو اسأل جهة علمية موثوقة.",
                    ),
                ),
            ),
            LearningSection(
                id = "obsession",
                title = "لا تجعل الطهارة بابًا للوسواس",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الأصل عدم إعادة الوضوء لمجرد الشك. تعلم القاعدة ثم امضِ، ولا تفتش عن النجاسة أو النقض بلا سبب واضح.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
    )

    private val newMuslimQuran = LearningLesson(
        id = "new_muslim_quran",
        titleRes = R.string.learn_topic_new_muslim_quran,
        subtitleRes = R.string.learn_topic_new_muslim_quran_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "translation",
                title = "ابدأ بالمعنى مع تعلم العربية",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "إذا كنت لا تعرف العربية فاقرأ ترجمة موثوقة لتفهم الرسالة، وبالتوازي تعلم الحروف والفاتحة والسور القصيرة. الترجمة تفسير للمعنى وليست بديلًا عن النص العربي في التلاوة.",
                    ),
                ),
            ),
            LearningSection(
                id = "noorani",
                title = "القاعدة النورانية داخل التطبيق",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "استخدم قسم الحروف والحركات والمد والسكون والشدة لتأسيس القراءة. النطق يحتاج تصحيحًا من معلم أو قارئ متقن متى أمكن.",
                    ),
                ),
            ),
            LearningSection(
                id = "routine",
                title = "روتين بسيط",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("5 دقائق", "استمع إلى مقطع قصير مع متابعة النص."),
                            LearningStepItem("5 دقائق", "اقرأ ترجمة الآيات وافهم الفكرة الأساسية."),
                            LearningStepItem("5 دقائق", "تدرب على الفاتحة أو سورة قصيرة دون إضافة كمية كبيرة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "questions",
                title = "عند آية صعبة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "لا تبنِ عقيدة أو حكمًا على ترجمة جملة واحدة دون سياق. استخدم مسار فهم القرآن والتفسير، ثم اسأل عند بقاء الإشكال.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
    )

    private val newMuslimDailyLife = LearningLesson(
        id = "new_muslim_daily_life",
        titleRes = R.string.learn_topic_new_muslim_daily_life,
        subtitleRes = R.string.learn_topic_new_muslim_daily_life_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "priorities",
                title = "لا تغير حياتك دفعة واحدة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الواجبات أولًا", "ركز على الصلاة وما تحتاجه للطهارة والصيام عند وقته."),
                            LearningStepItem("اترك الواضح تدريجيًا", "إذا عرفت أن عادة محرمة فضع خطة عملية واقعية لتركها، واطلب مساعدة عند الإدمان أو الضرر."),
                            LearningStepItem("أضف عادة نافعة", "ذكر قصير، قراءة قرآن، أو درس موثوق أفضل من قائمة طويلة لا تستمر."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "family",
                title = "الأسرة والأصدقاء",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قد لا يفهم المقربون قرارك. لا تجعل الخلاف الديني ذريعة للإهانة أو قطع كل علاقة. حافظ على البر والصدق والحدود، واطلب دعمًا إذا تعرضت لتهديد أو ضغط خطير.",
                    ),
                ),
            ),
            LearningSection(
                id = "food_money",
                title = "الطعام والمال والعمل",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "هذه المجالات فيها أحكام وتفاصيل لا تختصر في نصيحة عامة. تعلم الأساسيات التي تمس حياتك الآن، واستعمل أقسام الزكاة والتمويل والأسرة في التطبيق عند الحاجة.",
                    ),
                ),
            ),
            LearningSection(
                id = "adhkar",
                title = "الذكر والعادات اليومية",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ميزة الأذكار يمكن أن تساعدك في أذكار الصباح والمساء والنوم وغيرها، لكن لا تحولها إلى عبء عددي. ابدأ بالقليل الصحيح الذي تفهمه ثم زد تدريجيًا.",
                    ),
                ),
            ),
        ),
    )

    private val newMuslimRoadmap = LearningLesson(
        id = "new_muslim_roadmap",
        titleRes = R.string.learn_topic_new_muslim_roadmap,
        subtitleRes = R.string.learn_topic_new_muslim_roadmap_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "days_7",
                title = "أول 7 أيام",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ثبت معنى الشهادة", "افهم التوحيد ورسالة محمد ﷺ بلغة واضحة."),
                            LearningStepItem("ابدأ الصلاة", "تعرف على الأوقات والوضوء وتسلسل الركعة والفاتحة."),
                            LearningStepItem("اختر جهة دعم", "اعثر على شخص أو مسجد موثوق للسؤال دون ضغط."),
                            LearningStepItem("لا تغرق في الخلافات", "أجل المسائل الدقيقة التي لا تؤثر في عبادتك الحالية."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "days_30",
                title = "أول 30 يومًا",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ثبّت الصلاة اليومية", "ركز على الانتظام أكثر من جمع النوافل."),
                            LearningStepItem("تعلم الفاتحة وسورًا قصيرة", "صحح النطق تدريجيًا باستخدام القاعدة النورانية والاستماع."),
                            LearningStepItem("ابدأ قراءة القرآن بالمعنى", "مقدار صغير يومي مع تفسير ميسر."),
                            LearningStepItem("تعلم الحلال والحرام الذي يمس حياتك", "الطعام والمال والعلاقات حسب واقعك الحقيقي."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "days_90",
                title = "أول 90 يومًا",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("وسع العلم المنظم", "انتقل لمسارات الطهارة والصلاة والعقيدة والقرآن بدل التعلم المتناثر."),
                            LearningStepItem("ابنِ صحبة متوازنة", "تواصل مع مجتمع يساعدك على العبادة والحياة الطبيعية معًا."),
                            LearningStepItem("راجع عاداتك", "اختر عادة مالية أو رقمية أو أخلاقية لتحسينها كل مرة."),
                            LearningStepItem("ضع خطة سنوية", "استعد لرمضان والزكاة والحج عندما تصبح أحكامها متعلقة بك."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "not_deadline",
                title = "الخطة مرنة وليست موعدًا نهائيًا",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "7/30/90 يومًا تقسيم تعليمي لإدارة التعلم، وليس حكمًا شرعيًا بأن كل شخص يجب أن يصل إلى المستوى نفسه في المدة نفسها. العمر واللغة والصحة والبيئة تختلف.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
    )
}
