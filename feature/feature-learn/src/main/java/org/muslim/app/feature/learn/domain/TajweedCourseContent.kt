package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object TajweedCourseContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            tajweedIntroduction,
            makharijAndSifat,
            noonAndMeem,
            maddRules,
            qalqalahAndHeavyLight,
            waqfAndIbtida,
            tajweedPractice,
        )
    }

    private val tajweedIntroduction = LearningLesson(
        id = "tajweed_intro",
        titleRes = R.string.learn_topic_tajweed_intro,
        subtitleRes = R.string.learn_topic_tajweed_intro_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "ما التجويد؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "التجويد هو إعطاء الحروف حقوقها ومستحقاتها في النطق بحسب ما قرره علماء الأداء، مع القراءة بتؤدة من غير تكلف ولا تصنع.",
                    ),
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بالترتيل في قوله تعالى: «ورتل القرآن ترتيلا».",
                        referenceIds = listOf("quran_73_4"),
                    ),
                ),
            ),
            LearningSection(
                id = "purpose",
                title = "هدف التعلم",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("سلامة الحروف", "ألا يتحول حرف إلى حرف آخر بسبب مخرج أو صفة خاطئة."),
                            LearningStepItem("وضوح الأحكام", "تمييز الغنة والمد والإخفاء والإدغام ونحوها عند مواضعها."),
                            LearningStepItem("ثبات الأداء", "تطبيق القاعدة أثناء القراءة الطبيعية، لا حفظ تعريفات دون ممارسة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "colors",
                title = "ألوان التجويد",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الألوان في المصحف الرقمي ترميز بصري مساعد وقد تختلف مخططاتها بين التطبيقات والمصاحف. المرجع هو القاعدة المنطوقة والنص، لا اللون نفسه.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "teacher",
                title = "لماذا تحتاج إلى السماع؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "بعض الأخطاء لا تظهر في الكتابة، مثل مقدار الغنة وصفة الحرف ودرجة التفخيم. لذلك يكون الاستماع والتلقي على قارئ متقن عنصرًا أساسيًا في تحسين الأداء.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_73_4", LearningReferenceKind.QURAN, "القرآن الكريم، سورة المزمل، الآية 73:4", "Quran 73:4"),
        ),
    )

    private val makharijAndSifat = LearningLesson(
        id = "tajweed_makharij",
        titleRes = R.string.learn_topic_tajweed_makharij,
        subtitleRes = R.string.learn_topic_tajweed_makharij_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "makhraj",
                title = "المخرج",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المخرج هو الموضع الذي يعتمد عليه صوت الحرف عند نطقه. تجمع مخارج الحروف في مناطق كبرى مثل الجوف والحلق واللسان والشفتين والخيشوم، مع تفاصيل أدق داخل كل منطقة.",
                    ),
                ),
            ),
            LearningSection(
                id = "groups",
                title = "ابدأ بالمجموعات المتقاربة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الحلق", "درب أذنك على التفريق بين الهمزة والهاء، والعين والحاء، والغين والخاء."),
                            LearningStepItem("طرف اللسان", "انتبه للفروق بين التاء والدال والطاء، وبين السين والصاد والزاي."),
                            LearningStepItem("الشفتان", "راجع الباء والميم والواو وما يحتاجه كل حرف من انطباق أو انضمام."),
                            LearningStepItem("الضاد والظاء", "من أكثر الأزواج التي يقع فيها الخلط عند غير المتقنين، ويحتاج تصحيحها إلى سماع مباشر."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "sifat",
                title = "صفات الحروف",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الصفة تشرح كيفية خروج الحرف بعد معرفة موضعه، مثل الشدة والرخاوة والاستعلاء والاستفال والهمس والجهر. الهدف العملي أن تساعدك الصفة في تثبيت الفرق بين الحروف المتقاربة.",
                    ),
                ),
            ),
            LearningSection(
                id = "practice",
                title = "تمرين عملي",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اعزل الحرف", "انطقه ساكنًا بعد همزة وصل أو حركة مناسبة."),
                            LearningStepItem("قارن زوجًا", "بدل التدريب على 10 حروف معًا، قارن حرفين متقاربين."),
                            LearningStepItem("أدخل الحرف في كلمة", "بعد نجاح الصوت منفردًا طبقه داخل كلمة قرآنية."),
                            LearningStepItem("اطلب تصحيحًا", "التسجيل الذاتي مفيد، لكن الأذن قد تعتاد خطأها؛ لذلك تحتاج مرجعًا خارجيًا."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val noonAndMeem = LearningLesson(
        id = "tajweed_noon_meem",
        titleRes = R.string.learn_topic_tajweed_noon_meem,
        subtitleRes = R.string.learn_topic_tajweed_noon_meem_sub,
        estimatedMinutes = 19,
        sections = listOf(
            LearningSection(
                id = "noon",
                title = "النون الساكنة والتنوين",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("الإظهار", "يظهر صوت النون أو التنوين بوضوح عند حروف الإظهار المعروفة."),
                            LearningComparisonItem("الإدغام", "يدخل صوت النون أو التنوين في الحرف التالي في مواضع محددة، مع غنة أو بدونها بحسب الحرف."),
                            LearningComparisonItem("الإقلاب", "تقلب النون أو التنوين ميمًا مخفاة عند الباء مع الغنة."),
                            LearningComparisonItem("الإخفاء", "يكون النطق بين الإظهار والإدغام مع بقاء الغنة عند حروف الإخفاء."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "meem",
                title = "الميم الساكنة",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("إخفاء شفوي", "عند الباء مع غنة واضحة من الخيشوم."),
                            LearningComparisonItem("إدغام شفوي", "عند الميم فتصيران كميم مشددة مع الغنة."),
                            LearningComparisonItem("إظهار شفوي", "مع بقية الحروف مع الانتباه خصوصًا قرب الواو والفاء."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "ghunnah",
                title = "الغنة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الغنة صوت يخرج من الخيشوم ويظهر بوضوح في النون والميم المشددتين وبعض أحكام الإخفاء والإدغام. مقدارها يُتعلم بالسماع والمشافهة أكثر من العد الميكانيكي.",
                    ),
                ),
            ),
            LearningSection(
                id = "mistakes",
                title = "أخطاء شائعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("زيادة الغنة", "لا تحول كل نون أو ميم إلى غنة طويلة."),
                            LearningStepItem("إخفاء المخرج", "الغنة لا تعني ابتلاع الحرف أو فقدان صوت الحرف التالي."),
                            LearningStepItem("التطبيق دون سياق", "تدرب على أمثلة قصيرة ثم اقرأ آية كاملة حتى تنتقل القاعدة إلى القراءة الطبيعية."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val maddRules = LearningLesson(
        id = "tajweed_madd",
        titleRes = R.string.learn_topic_tajweed_madd,
        subtitleRes = R.string.learn_topic_tajweed_madd_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "natural",
                title = "المد الطبيعي",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المد الطبيعي هو الأساس الذي تقوم عليه بقية المدود، ويكون في حروف المد عند تحقق شروطها من غير سبب زائد كهمز أو سكون.",
                    ),
                ),
            ),
            LearningSection(
                id = "causes",
                title = "الهمز والسكون",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تتفرع المدود عندما يأتي الهمز أو السكون قبل حرف المد أو بعده بحسب النوع. أسماء المدود ومقاديرها التفصيلية ترتبط بالرواية وطريق الأداء الذي يقرأ به المتعلم.",
                    ),
                    LearningContentBlock.Callout(
                        title = "لا تخلط الروايات",
                        body = "قد تختلف أوجه المد ومقاديره بين الروايات والطرق. المسار التعليمي يجب أن يصرح بالرواية المعتمدة في التطبيق العملي بدل جمع مقادير من مصادر متعددة.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "practice",
                title = "طريقة التدريب",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("حدد حرف المد", "ابحث أولًا عن الألف أو الواو أو الياء المستوفية لشروط المد."),
                            LearningStepItem("حدد السبب", "هل يوجد همز أو سكون مرتبط بالحرف؟"),
                            LearningStepItem("استمع", "قارن أداءك بقارئ يقرأ بالرواية نفسها."),
                            LearningStepItem("لا تعد بالأصابع أثناء التلاوة", "استخدم العد في التدريب فقط ثم انتقل إلى الإحساس الزمني المستقر."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "colors",
                title = "المد في ألوان المصحف",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "إذا أبرز القارئ الرقمي مواضع المد بلون، فاستعمل اللون كتنبيه إلى موضع القاعدة ثم تحقق من نوع المد ومقداره من الدرس أو المعلم.",
                    ),
                ),
            ),
        ),
    )

    private val qalqalahAndHeavyLight = LearningLesson(
        id = "tajweed_qalqalah",
        titleRes = R.string.learn_topic_tajweed_qalqalah,
        subtitleRes = R.string.learn_topic_tajweed_qalqalah_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "qalqalah",
                title = "القلقلة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "القلقلة اضطراب صوت الحرف الساكن من حروفها المعروفة بحيث يظهر صوته من غير إضافة حركة كاملة. يزداد وضوحها عند الوقف على الحرف.",
                    ),
                    LearningContentBlock.Callout(
                        body = "من أشهر الأخطاء تحويل القلقلة إلى فتحة أو ضمة أو كسرة صريحة؛ المطلوب صوت الحرف لا حركة جديدة.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "tafkhim",
                title = "التفخيم والترقيق",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "بعض الحروف مستعلية مفخمة في أصلها، وبعضها مرقق، وهناك حروف تتغير أحوالها بحسب السياق مثل الراء ولام لفظ الجلالة في تفاصيل معروفة.",
                    ),
                ),
            ),
            LearningSection(
                id = "ra",
                title = "الراء",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تفخيم الراء وترقيقها يعتمد على حركتها وما قبلها وبعض الأحوال المركبة. لا تحفظ قائمة طويلة قبل إتقان الحالات الواضحة ثم انتقل للاستثناءات.",
                    ),
                ),
            ),
            LearningSection(
                id = "lam",
                title = "لام لفظ الجلالة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تُفخم لام لفظ الجلالة بعد فتح أو ضم، وترقق بعد كسر في القاعدة التعليمية المشهورة، مع العناية بعدم المبالغة في التفخيم.",
                    ),
                ),
            ),
        ),
    )

    private val waqfAndIbtida = LearningLesson(
        id = "tajweed_waqf",
        titleRes = R.string.learn_topic_tajweed_waqf,
        subtitleRes = R.string.learn_topic_tajweed_waqf_sub,
        estimatedMinutes = 16,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "لماذا الوقف مهم؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الوقف والابتداء يؤثران في المعنى كما يؤثران في النفس والأداء. الوقوف في موضع غير مناسب قد يقطع الجملة أو يوهم معنى غير مقصود.",
                    ),
                ),
            ),
            LearningSection(
                id = "marks",
                title = "علامات المصحف",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المصاحف تستخدم علامات للوقف والوصل تساعد القارئ على اختيار الموضع المناسب. شكل العلامات وتفاصيل اصطلاحها قد تختلف قليلًا بين الطبعات.",
                    ),
                ),
            ),
            LearningSection(
                id = "breath",
                title = "إذا ضاق النفس",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("لا تجازف بالمعنى", "اختر موضعًا أقرب يحافظ على المعنى بدل دفع النفس حتى ينقطع في مكان سيئ."),
                            LearningStepItem("أعد من موضع مناسب", "إذا اضطررت إلى وقف عارض، قد تحتاج عند الاستئناف إلى الرجوع بكلمة أو أكثر ليستقيم المعنى."),
                            LearningStepItem("درّب النفس تدريجيًا", "تحسين النفس مفيد، لكنه ليس مبررًا لقراءة سريعة أو مضغوطة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "tafsir",
                title = "اربط الوقف بالتفسير",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "علامة الوقف أداة قوية لكنها لا تغني عن فهم الجملة. عند الآيات المركبة أو المواضع التي تحتمل معنى دقيقًا، ارجع إلى مصحف مضبوط أو تفسير وتعليم موثوق.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
    )

    private val tajweedPractice = LearningLesson(
        id = "tajweed_practice",
        titleRes = R.string.learn_topic_tajweed_practice,
        subtitleRes = R.string.learn_topic_tajweed_practice_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "cycle",
                title = "دورة التدريب",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("استمع", "اختر آيتين أو ثلاثًا واستمع إلى قارئ متقن أكثر من مرة."),
                            LearningStepItem("حدد قاعدة واحدة", "لا تحاول تصحيح المخارج والمدود والغنة والوقف كلها في جولة واحدة."),
                            LearningStepItem("سجل نفسك", "اقرأ المقطع وسجله ثم قارنه بالمثال."),
                            LearningStepItem("صحح وأعد", "أعد الموضع الخاطئ وحده ثم أدخله في الآية."),
                            LearningStepItem("اعرض على معلم", "اجمع الأخطاء التي لم تستطع حسمها واعرضها على قارئ متقن."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "reader_integration",
                title = "التكامل مع قارئ القرآن",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "استخدم التلاوة الصوتية وألوان التجويد وإعادة المقطع في قارئ القرآن كأدوات تدريب. لا يحتاج قسم Learn إلى نسخ المصحف أو محرك الصوت؛ يكفي أن يشرح للمتعلم كيف يوظفهما.",
                    ),
                ),
            ),
            LearningSection(
                id = "progress",
                title = "كيف تقيس التقدم؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("انخفاض الأخطاء المتكررة", "راقب الأخطاء التي كانت تعود في كل صفحة."),
                            LearningStepItem("ثبات السرعة", "لا تعتبر السرعة تقدمًا إذا انخفضت دقة المخارج والوقف."),
                            LearningStepItem("الانتقال من القاعدة إلى القراءة", "النجاح الحقيقي أن تطبق الحكم دون توقف ذهني طويل."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "next",
                title = "بعد إتقان الأساسيات",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "بعد تثبيت القواعد الأساسية يمكن الانتقال إلى دراسة أوسع للصفات، الوقف والابتداء، والروايات والطرق على يد أهل الاختصاص. لا يقدم التطبيق إجازة قراءة أو تصحيحًا صوتيًا تلقائيًا على أنه بديل عن الشيخ.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
    )
}
