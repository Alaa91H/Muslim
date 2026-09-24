package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FinanceTransactionsLearningContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            financeIntroduction,
            saleContracts,
            ribaBasics,
            debtAndLoans,
            ecommerceAndInstallments,
            businessAndInvesting,
            financeToolsGuide,
        )
    }

    private fun financeLink() = LearningFeatureLink(
        destination = LearningFeatureDestination.FINANCE,
        titleRes = R.string.learn_feature_finance_title,
        bodyRes = R.string.learn_feature_finance_body,
        actionRes = R.string.learn_feature_finance_action,
    )

    private val financeIntroduction = LearningLesson(
        id = "finance_intro",
        titleRes = R.string.learn_topic_finance_intro,
        subtitleRes = R.string.learn_topic_finance_intro_sub,
        estimatedMinutes = 14,
        featureLink = financeLink(),
        sections = listOf(
            LearningSection(
                id = "principle",
                title = "الأصل في المعاملات",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "فرّق القرآن بين البيع والربا، وأباح البيع وحرم الربا.",
                        referenceIds = listOf("quran_2_275"),
                    ),
                    LearningContentBlock.Paragraph(
                        "تعلم المعاملات يبدأ بفهم العقد والرضا والملكية والتسليم والشفافية، ثم دراسة الممنوعات الخاصة مثل الربا والغرر والغش بحسب صورة المعاملة.",
                    ),
                ),
            ),
            LearningSection(
                id = "layers",
                title = "الفقه والقانون والمخاطر",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("الحكم الشرعي", "يسأل عن بنية العقد ومصدر الربح والشروط والالتزامات."),
                            LearningComparisonItem("الوضع القانوني", "يحدد حقوق المستهلك والضرائب والتراخيص والتنفيذ بحسب البلد."),
                            LearningComparisonItem("المخاطر المالية", "تبحث القدرة على السداد والسيولة والخسارة ولا يحسمها الحكم الشرعي وحده."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "documents",
                title = "اقرأ قبل أن توافق",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("السعر النهائي", "اجمع الرسوم والعمولات والتكاليف المتكررة."),
                            LearningStepItem("الالتزام", "اعرف ما يجب عليك ومتى ينتهي العقد وما الذي يحدث عند التأخر."),
                            LearningStepItem("الملكية والتسليم", "تحقق ماذا تشتري فعلًا ومتى تنتقل الملكية أو المنفعة."),
                            LearningStepItem("الخروج", "افهم الإلغاء والاسترداد والبيع المبكر قبل الدخول."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "feature",
                title = "ميزة Islamic Finance",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ميزة المالية في التطبيق تحتوي أدلة للشراء والبيع والديون والتجارة الإلكترونية، ودفتر ديون مع تنبيهات، وروابط لمزودي فحص الأسهم. هذا المسار يشرح المفاهيم ولا يكرر تلك الأدوات.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_275", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:275", "Quran 2:275"),
        ),
    )

    private val saleContracts = LearningLesson(
        id = "finance_sale_contracts",
        titleRes = R.string.learn_topic_finance_sale_contracts,
        subtitleRes = R.string.learn_topic_finance_sale_contracts_sub,
        estimatedMinutes = 17,
        featureLink = financeLink(),
        sections = listOf(
            LearningSection(
                id = "clarity",
                title = "وضوح المبيع والثمن",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("المبيع", "صف السلعة أو الخدمة وصفًا يزيل الالتباس المؤثر."),
                            LearningStepItem("الثمن", "حدد السعر والعملة والرسوم وطريقة الدفع."),
                            LearningStepItem("التسليم", "حدد الموعد والمكان أو آلية التسليم الرقمي."),
                            LearningStepItem("العيوب", "أفصح عن العيب المؤثر ولا تعتمد على إخفائه ثم اشتراط عدم المسؤولية."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "consent",
                title = "التراضي",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الموافقة الصحيحة تتطلب فهمًا حقيقيًا للشروط المهمة. الإكراه أو التضليل أو الرسوم المخفية يفسد الثقة وقد يؤثر في صحة أو عدالة المعاملة بحسب حالتها.",
                    ),
                ),
            ),
            LearningSection(
                id = "ownership",
                title = "ما الذي تملك وتبيع؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "صور بيع ما لا يملكه البائع أو ما لا يقدر على تسليمه تحتاج تفصيلًا فقهيًا. في التجارة الحديثة قد تكون العلاقة وكالة أو وساطة أو طلبًا مسبقًا، لذلك لا تحكم من اسم المنتج التسويقي وحده.",
                    ),
                ),
            ),
            LearningSection(
                id = "records",
                title = "التوثيق",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الفاتورة والعقد وسجل المحادثة ووصف المنتج وسياسة الاسترجاع تحفظ الحقوق عند النزاع، خصوصًا في البيع عن بعد.",
                    ),
                ),
            ),
        ),
    )

    private val ribaBasics = LearningLesson(
        id = "finance_riba",
        titleRes = R.string.learn_topic_finance_riba,
        subtitleRes = R.string.learn_topic_finance_riba_sub,
        estimatedMinutes = 18,
        featureLink = financeLink(),
        sections = listOf(
            LearningSection(
                id = "warning",
                title = "تحريم الربا",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "جاء في القرآن تحريم الربا والأمر بترك ما بقي منه.",
                        referenceIds = listOf("quran_2_275", "quran_2_278"),
                    ),
                ),
            ),
            LearningSection(
                id = "loan",
                title = "الزيادة المشروطة على القرض",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "من أوضح الصور التعليمية أن يشترط المقرض زيادة على أصل القرض مقابل الزمن. أما المنتجات المصرفية والاستثمارية المركبة فلا يكفي النظر إلى الاسم أو النسبة وحدها؛ يجب فهم العقد كاملًا.",
                    ),
                ),
            ),
            LearningSection(
                id = "not_labels",
                title = "غيّر الاسم لا يغير الحقيقة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "وصف رسم بأنه «خدمة» أو «إدارة» لا يكفي لإثبات الجواز، كما أن وجود نسبة مئوية لا يعني وحده أنها ربا. السؤال عن سبب الرسم وما يقابله وبنية الالتزام.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "complex",
                title = "التمويل المعاصر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المرابحة والإجارة والبطاقات والتمويل العقاري والتأخير وإعادة الجدولة صور تختلف عقودها وتطبيقاتها. استخدم هذا الدرس لفهم الأسئلة، ثم اعرض العقد الفعلي على مختص موثوق.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_275", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:275", "Quran 2:275"),
            LearningReference("quran_2_278", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:278", "Quran 2:278"),
        ),
    )

    private val debtAndLoans = LearningLesson(
        id = "finance_debt_loans",
        titleRes = R.string.learn_topic_finance_debt_loans,
        subtitleRes = R.string.learn_topic_finance_debt_loans_sub,
        estimatedMinutes = 17,
        featureLink = financeLink(),
        sections = listOf(
            LearningSection(
                id = "writing",
                title = "اكتب الدين",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "تضمنت أطول آية في القرآن توجيهات واسعة لتوثيق الدين المؤجل.",
                        referenceIds = listOf("quran_2_282"),
                    ),
                ),
            ),
            LearningSection(
                id = "ledger",
                title = "ماذا تسجل؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الأطراف", "من الدائن ومن المدين؟"),
                            LearningStepItem("المبلغ والعملة", "تجنب العبارات العامة مثل «سأعيده لاحقًا»."),
                            LearningStepItem("موعد الاستحقاق", "سجل التاريخ أو طريقة تحديده."),
                            LearningStepItem("السداد", "احفظ ما يثبت الدفعات الجزئية والكاملة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "hardship",
                title = "عند العسر",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "وجه القرآن إلى إنظار المعسر إلى ميسرة، وذكر فضل التصدق عليه.",
                        referenceIds = listOf("quran_2_280"),
                    ),
                ),
            ),
            LearningSection(
                id = "app",
                title = "دفتر الديون في التطبيق",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "دفتر الديون والتنبيهات أداة تنظيمية، وليس عقدًا قانونيًا ولا إثباتًا كافيًا في كل نظام قضائي. احتفظ بالمستندات الأصلية اللازمة.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_282", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:282", "Quran 2:282"),
            LearningReference("quran_2_280", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:280", "Quran 2:280"),
        ),
    )

    private val ecommerceAndInstallments = LearningLesson(
        id = "finance_ecommerce",
        titleRes = R.string.learn_topic_finance_ecommerce,
        subtitleRes = R.string.learn_topic_finance_ecommerce_sub,
        estimatedMinutes = 16,
        featureLink = financeLink(),
        sections = listOf(
            LearningSection(
                id = "online",
                title = "البيع الرقمي بيع حقيقي",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تطبيق أو موقع الويب لا يغير الأسئلة الأساسية: ما السلعة أو الخدمة؟ كم السعر النهائي؟ من البائع؟ متى التسليم؟ وما سياسة الإلغاء والاسترجاع؟",
                    ),
                ),
            ),
            LearningSection(
                id = "installments",
                title = "التقسيط",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قد تختلف صورة التقسيط المشروع عن قرض بفائدة أو غرامة متزايدة. افحص السعر المتفق عليه، جهة التمويل، غرامات التأخير، والالتزامات الإضافية بدل الاعتماد على عبارة «اشتر الآن وادفع لاحقًا».",
                    ),
                ),
            ),
            LearningSection(
                id = "subscriptions",
                title = "الاشتراكات والتجديد",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الفترة", "اعرف متى يبدأ التجديد ومتى يتوقف."),
                            LearningStepItem("الإلغاء", "تحقق من آلية الإلغاء قبل الدفع."),
                            LearningStepItem("السعر", "راقب انتقال العرض المجاني أو المخفض إلى السعر الكامل."),
                            LearningStepItem("البيانات", "لا تمنح صلاحيات دفع أو بيانات أوسع من الحاجة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "fraud",
                title = "الغش والادعاءات",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "التقييمات المزيفة، الندرة المصطنعة، إخفاء الرسوم، والمواصفات الكاذبة أمثلة على ممارسات تفسد الشفافية حتى لو أمكن تنفيذ الدفع تقنيًا.",
                    ),
                ),
            ),
        ),
    )

    private val businessAndInvesting = LearningLesson(
        id = "finance_business_investing",
        titleRes = R.string.learn_topic_finance_business_investing,
        subtitleRes = R.string.learn_topic_finance_business_investing_sub,
        estimatedMinutes = 18,
        featureLink = financeLink(),
        sections = listOf(
            LearningSection(
                id = "business",
                title = "دخل النشاط ومصدر الربح",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "اسأل ماذا تبيع الشركة أو المشروع، وكيف يحقق الربح، وما العقود والالتزامات الأساسية. نشاط مباح لا يعني أن كل طريقة تمويل أو كل معاملة داخله مباحة.",
                    ),
                ),
            ),
            LearningSection(
                id = "screening",
                title = "فحص الأسهم",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "نتيجة الفحص الشرعي للأسهم تعتمد على المنهجية والبيانات والفترة الزمنية، وقد تختلف بين المزودين. التطبيق لا يصدر حكمًا مستقلًا ولا توصية بالشراء أو البيع.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "risk",
                title = "الجواز لا يساوي ملاءمة الاستثمار",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "حتى لو كان المنتج مقبولًا شرعًا وفق جهة معتبرة، قد يكون عالي المخاطر أو غير مناسب لسيولتك وأهدافك. القرار المالي يحتاج فهم المخاطر والتكلفة والتنويع والقدرة على تحمل الخسارة.",
                    ),
                ),
            ),
            LearningSection(
                id = "red_flags",
                title = "إشارات تستحق التوقف",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ربح مضمون مرتفع", "الوعد بعائد كبير بلا مخاطرة يحتاج تدقيقًا شديدًا."),
                            LearningStepItem("ضغط زمني", "لا تجعل عبارة «الفرصة الأخيرة» تمنعك من قراءة العقد."),
                            LearningStepItem("مصدر ربح غامض", "إذا لم تفهم من أين يأتي المال فلا تدخل قبل الفهم."),
                            LearningStepItem("ترخيص مجهول", "تحقق من الجهة القانونية والمزود قبل تحويل الأموال."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val financeToolsGuide = LearningLesson(
        id = "finance_tools_guide",
        titleRes = R.string.learn_topic_finance_tools_guide,
        subtitleRes = R.string.learn_topic_finance_tools_guide_sub,
        estimatedMinutes = 12,
        featureLink = financeLink(),
        sections = listOf(
            LearningSection(
                id = "guides",
                title = "أدلة المعاملات",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تحتوي ميزة Islamic Finance على أدلة عملية مختصرة للشراء والبيع والقروض والديون والتجارة الإلكترونية. استخدمها كقائمة مراجعة أولية لا كفتوى لعقد معقد.",
                    ),
                ),
            ),
            LearningSection(
                id = "debts",
                title = "دفتر الديون والتنبيهات",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يمكن تسجيل ما لك وما عليك، المبلغ والعملة والاستحقاق والملاحظات، وتفعيل التذكير. لا تسجل بيانات حساسة أكثر من الحاجة، واحتفظ بالمستند القانوني خارج الأداة عند اللزوم.",
                    ),
                ),
            ),
            LearningSection(
                id = "screeners",
                title = "مزودو فحص الأسهم",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "توفر الميزة روابط لمزودين خارجيين للفحص. منهجية المزود وترخيصه وبياناته مسؤوليته، ويجب مراجعة التقرير بدل اعتبار اسم المزود ضمانًا نهائيًا.",
                    ),
                ),
            ),
            LearningSection(
                id = "limits",
                title = "ما الذي لا تفعله الأداة؟",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الميزة لا تنشئ عقدًا قانونيًا، ولا تقرر وحدها أن منتجًا مصرفيًا حلال أو حرام، ولا تعطي توصية استثمارية أو ضمان عائد.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference(
                id = "internal_finance_feature",
                kind = LearningReferenceKind.OTHER,
                citation = "ميزة Islamic Finance المدمجة في تطبيق Muslim",
                locator = "feature-finance",
                note = "مرجع تقني لوظائف الأدلة ودفتر الديون وروابط الفحص، وليس فتوى مالية.",
            ),
        ),
    )
}
