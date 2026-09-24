package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object ZakatAssetsContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            zakatCashMetals,
            zakatBusinessInvestments,
            zakatDebtsJewelry,
            zakatCropsLivestock,
            zakatCalculatorGuide,
        )
    }

    private val zakatCashMetals = LearningLesson(
        id = "zakat_cash_metals",
        titleRes = R.string.learn_topic_zakat_cash_metals,
        subtitleRes = R.string.learn_topic_zakat_cash_metals_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "cash",
                title = "النقد والحسابات",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يدخل في التقييم النقد المتاح والأرصدة التي يملكها الشخص ملكا تاما ويمكنه التصرف فيها، مع مراعاة شروط النصاب والحول وما يخص الديون والحقوق المعلقة.",
                    ),
                ),
            ),
            LearningSection(
                id = "gold_silver",
                title = "الذهب والفضة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ورد في حديث علي رضي الله عنه أصل نصاب الفضة والذهب ومقدار ربع العشر بعد الحول.",
                        referenceIds = listOf("abudawud_1573"),
                    ),
                    LearningContentBlock.Paragraph(
                        "تُقوّم المعادن بسعر معتبر عند حساب الزكاة. الحاسبة الحالية تسمح بإدخال الأوزان وأسعار الغرام يدويا أو عبر السعر المتاح في ميزة الزكاة.",
                    ),
                ),
            ),
            LearningSection(
                id = "currency",
                title = "تعدد العملات",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "إذا كان لديك أموال بعملات متعددة، تُحوّل إلى عملة تقييم واحدة بسعر صرف مناسب وقت الحساب ثم تُجمع، مع الانتباه إلى أن أسعار الصرف تتغير.",
                    ),
                ),
            ),
            LearningSection(
                id = "accounts",
                title = "الحسابات المقيدة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الأموال المحجوزة أو المتنازع عليها أو التي لا يمكن الوصول إليها فعليا قد تحتاج حكما مختلفا عن الرصيد المتاح. لا يكفي ظهور الرقم في تطبيق مصرفي للحكم الفقهي النهائي.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("abudawud_1573", LearningReferenceKind.HADITH, "سنن أبي داود 1573، كتاب الزكاة", "Sunan Abi Dawud 1573"),
        ),
    )

    private val zakatBusinessInvestments = LearningLesson(
        id = "zakat_business_investments",
        titleRes = R.string.learn_topic_zakat_business_investments,
        subtitleRes = R.string.learn_topic_zakat_business_investments_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "trade",
                title = "عروض التجارة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "البضائع المعدة للبيع بقصد التجارة تعامل في الفقه معاملة تختلف عن الأصول التي يحتفظ بها النشاط للاستخدام، مثل بعض المعدات والمباني التشغيلية. لذلك يجب تحديد وظيفة الأصل قبل إدخال قيمته في الحاسبة.",
                    ),
                ),
            ),
            LearningSection(
                id = "inventory",
                title = "المخزون والذمم",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("مخزون البيع", "يقوم بما يعكس قيمته التجارية المعتبرة في وقت الزكاة وفق المنهج الذي تتبعه."),
                            LearningStepItem("النقد التجاري", "يدخل مع الأموال الزكوية الأخرى."),
                            LearningStepItem("الديون لك", "الذمم المدينة تختلف أحكامها باختلاف قوة الدين وإمكان تحصيله."),
                            LearningStepItem("الأصول التشغيلية", "ليست كل آلة أو سيارة أو عقار في الشركة من عروض التجارة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "investments",
                title = "الأسهم والاستثمارات",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "لا توجد خانة واحدة لكل استثمار",
                        body = "الأسهم والصناديق والاستثمارات طويلة الأجل قد تختلف زكاتها بحسب نية المتاجرة أو الاحتفاظ وطبيعة الأصول داخل الاستثمار. إدخال كامل القيمة في خانة «الاستثمارات» هو تبسيط حسابي وليس فتوى لكل منتج مالي.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "professional",
                title = "الحالات الكبيرة والمعقدة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الشركات، المحافظ الكبيرة، الخيارات والمشتقات، الصناديق المركبة والعقارات الاستثمارية تحتاج عادة إلى محاسب يعرف البيانات المالية وإلى مرجع شرعي متخصص في الزكاة والمعاملات.",
                    ),
                ),
            ),
        ),
    )

    private val zakatDebtsJewelry = LearningLesson(
        id = "zakat_debts_jewelry",
        titleRes = R.string.learn_topic_zakat_debts_jewelry,
        subtitleRes = R.string.learn_topic_zakat_debts_jewelry_sub,
        estimatedMinutes = 16,
        sections = listOf(
            LearningSection(
                id = "debts",
                title = "الديون التي عليك",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "هل تخصم الديون من الوعاء الزكوي؟ وما مقدار الدين الذي يخصم: كامل الرصيد أم الأقساط القريبة فقط؟ هذه من المسائل التي تختلف فيها مناهج الفقهاء والهيئات المعاصرة.",
                    ),
                    LearningContentBlock.Callout(
                        title = "افتراض الحاسبة",
                        body = "الحاسبة الحالية تسمح بخصم قيمة «الديون المستحقة عليك» من إجمالي الأصول قبل فحص النصاب. هذا افتراض برمجي للمشروع وليس ترجيحا نهائيا لكل أنواع الديون.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "receivables",
                title = "الديون التي لك",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الدين المتوقع تحصيله من شخص قادر ليس كالدين المتعثر أو المنكر. توقيت زكاته وكيفية التعامل معه من المسائل التي تحتاج معرفة حال الدين الفعلية.",
                    ),
                ),
            ),
            LearningSection(
                id = "jewelry",
                title = "حلي الاستعمال",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "خلاف مشهور",
                        body = "زكاة حلي الذهب والفضة المباح المعد للاستعمال من مسائل الخلاف المعروفة بين المذاهب. لا ينبغي أن تجعل الحاسبة إدخال وزن الحلي إلزاميا من غير أن يعرف المستخدم القول الذي يتبعه.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "documentation",
                title = "توثيق الحساب",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("افصل أنواع الدين", "ميز بين قرض طويل الأجل وفاتورة مستحقة الآن والتزام مختلف عليه."),
                            LearningStepItem("سجل منهجك", "اكتب لنفسك هل تخصم الدين وكيف، لتستخدم المنهج نفسه في السنة التالية."),
                            LearningStepItem("اطلب مراجعة عند الشك", "الحالات الكبيرة أو المركبة تستحق سؤال مختص بدلا من تعديل رقم عشوائيا في الحاسبة."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val zakatCropsLivestock = LearningLesson(
        id = "zakat_crops_livestock",
        titleRes = R.string.learn_topic_zakat_crops_livestock,
        subtitleRes = R.string.learn_topic_zakat_crops_livestock_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "crops",
                title = "الزروع والثمار",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بإيتاء حق الزرع يوم حصاده، وهو أصل يبين اختلاف هذا الباب عن أموال الحول.",
                        referenceIds = listOf("quran_6_141"),
                    ),
                    LearningContentBlock.Evidence(
                        text = "في صحيح البخاري: فيما سقي بالمطر أو الماء الطبيعي العشر، وفيما سقي بآلة ومؤونة نصف العشر.",
                        referenceIds = listOf("bukhari_1483"),
                    ),
                ),
            ),
            LearningSection(
                id = "crop_details",
                title = "ما الذي يحتاج تفصيلا؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "أنواع المحاصيل التي تجب فيها الزكاة، النصاب، تكاليف الزراعة، وطريقة التعامل مع أنظمة الري المختلطة تختلف في التفاصيل. هذه الحاسبة العامة لا تحسب زكاة الزروع تلقائيا.",
                    ),
                ),
            ),
            LearningSection(
                id = "livestock",
                title = "الأنعام",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "للإبل والبقر والغنم أنصبة ومقادير خاصة مرتبطة بالعدد والحول وحال الرعي في كثير من التفصيلات. لا يصح تحويلها إلى نسبة 2.5% من قيمة القطيع دون معرفة الباب الفقهي.",
                    ),
                ),
            ),
            LearningSection(
                id = "specialist",
                title = "متى تحتاج جدولا متخصصا؟",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "المزارع ومربو الماشية يحتاجون جداول فقهية متخصصة ومدخلات تختلف جذريا عن حاسبة زكاة المال الحالية.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_6_141", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأنعام، الآية 6:141", "Quran 6:141"),
            LearningReference("bukhari_1483", LearningReferenceKind.HADITH, "صحيح البخاري 1483، كتاب الزكاة", "Sahih al-Bukhari 1483"),
        ),
    )

    private val zakatCalculatorGuide = LearningLesson(
        id = "zakat_calculator_guide",
        titleRes = R.string.learn_topic_zakat_calculator_guide,
        subtitleRes = R.string.learn_topic_zakat_calculator_guide_sub,
        estimatedMinutes = 10,
        sections = listOf(
            LearningSection(
                id = "scope",
                title = "ماذا تحسب الأداة الحالية؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("النقد", "الأموال النقدية التي تدخلها."),
                            LearningStepItem("الذهب والفضة", "الوزن مضروبا في سعر الغرام."),
                            LearningStepItem("عروض التجارة", "قيمة التجارة التي يحددها المستخدم."),
                            LearningStepItem("الاستثمارات", "قيمة يضيفها المستخدم بعد أن يقرر فقهيا ما الذي يدخل منها."),
                            LearningStepItem("الديون عليك", "تخصمها الأداة وفق نموذجها الحالي قبل فحص النصاب."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "threshold",
                title = "كيف تفحص النصاب؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تحسب الأداة قيمة نصاب الذهب وقيمة نصاب الفضة من أسعار الغرام المدخلة. عندما يتوفر السعران تستخدم العتبة الأقل في النموذج الحالي، ثم تطبق 2.5% إذا بلغ الوعاء تلك العتبة.",
                    ),
                    LearningContentBlock.Callout(
                        title = "هذا سلوك البرنامج لا حكم شخصي",
                        body = "اختيار الذهب أو الفضة مرجعا للنقود المعاصرة من المسائل التي تناقشها الهيئات والفقهاء. عرض طريقة الحاسبة بشفافية يسمح للمستخدم بمعرفة افتراضها بدل إخفائه.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "fitr",
                title = "زكاة الفطر في الحاسبة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ميزة زكاة الفطر تضرب قيمة الصاع التقديرية التي يدخلها المستخدم في عدد الأشخاص. تحديد مقدار الصاع محليا، ونوع الطعام، وحكم دفع القيمة نقدا مسائل مستقلة عن العملية الحسابية نفسها.",
                    ),
                ),
            ),
            LearningSection(
                id = "not_supported",
                title = "ما لا تحسبه تلقائيا",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "لا تتولى الحاسبة العامة أنصبة الإبل والبقر والغنم، ولا زكاة الزروع بتفاصيل الري، ولا تحلل مكونات الأسهم والصناديق، ولا تفصل أنواع الديون والحلي المختلف فيها.",
                    ),
                ),
            ),
            LearningSection(
                id = "workflow",
                title = "طريقة استخدام احترافية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("حدد الحكم أولا", "اعرف ما الذي يدخل في زكاتك قبل إدخال الأرقام."),
                            LearningStepItem("أدخل أسعارا حديثة", "خصوصا الذهب والفضة والعملات."),
                            LearningStepItem("راجع الناتج", "قارن المبلغ مع حساب يدوي بسيط إذا كانت القيمة كبيرة."),
                            LearningStepItem("احفظ السجل", "ميزة الزكاة تدعم سجلا سنويا يساعدك على تتبع ما حسبته."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference(
                id = "internal_zakat_calculator",
                kind = LearningReferenceKind.OTHER,
                citation = "حاسبة الزكاة المدمجة في تطبيق Muslim",
                locator = "feature-zakat / ZakatCalculator",
                note = "مرجع تقني لشرح سلوك الحاسبة، وليس مصدرا شرعيا.",
            ),
        ),
    )
}
