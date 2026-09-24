package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object PurificationAblutionContent {

    val lessons: List<LearningLesson> by lazy { listOf(
        wudu,
        wuduNullifiers,
        wipingOverFootwear,
    ) }

    private val wudu = LearningLesson(
        id = "wudu",
        titleRes = R.string.learn_topic_wudu,
        subtitleRes = R.string.learn_topic_wudu_sub,
        estimatedMinutes = 18,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "quick_summary",
                title = "الملخص السريع",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("النية", "ينوي المسلم الوضوء بقلبه."),
                            LearningStepItem("اليدين والمضمضة والاستنشاق", "يبدأ بغسل الكفين ثم المضمضة والاستنشاق."),
                            LearningStepItem("الوجه واليدان", "يغسل الوجه ثم اليدين إلى المرفقين."),
                            LearningStepItem("الرأس والرجلان", "يمسح الرأس ثم يغسل الرجلين إلى الكعبين."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "evidence",
                title = "الدليل وأصل الصفة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        heading = "من القرآن",
                        text = "تذكر آية المائدة 5:6 غسل الوجه واليدين إلى المرفقين، ومسح الرأس، وغسل الرجلين إلى الكعبين.",
                        referenceIds = listOf("quran_5_6"),
                    ),
                    LearningContentBlock.Evidence(
                        heading = "من السنة",
                        text = "روى حمران صفة وضوء عثمان رضي الله عنه، وذكر أنه رأى النبي ﷺ يتوضأ على هذه الصفة.",
                        referenceIds = listOf("bukhari_164"),
                    ),
                ),
            ),
            LearningSection(
                id = "before_wudu",
                title = "قبل أن تبدأ",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("تحقق من الحاجة", "إذا كنت على وضوء ولم ينتقض فلا يلزم تجديده لكل صلاة، وإن كان التجديد قد يشرع في أحوال."),
                            LearningStepItem("أزل ما يمنع وصول الماء", "ما يشكل طبقة عازلة تمنع وصول الماء إلى العضو يحتاج إلى إزالة قبل الغسل."),
                            LearningStepItem("استخدم الماء باعتدال", "المقصود إسباغ الوضوء لا كثرة استهلاك الماء."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "obligatory_actions",
                title = "الأعمال الأساسية",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تدور فرائض الوضوء حول غسل الوجه، وغسل اليدين إلى المرفقين، ومسح الرأس، وغسل الرجلين إلى الكعبين، مع تفاصيل في النية والترتيب والموالاة والمضمضة والاستنشاق بين المذاهب. لذلك يعرض التطبيق الصفة العملية الجامعة ويُفرد الخلاف في موضع مستقل.",
                    ),
                    LearningContentBlock.Callout(
                        title = "لا تخلط بين الفرض والسنة",
                        body = "تكرار غسل العضو إلى ثلاث مرات من صفة الوضوء المشهورة، لكن أصل غسل العضو الواجب يتحقق بالمرة المستوعبة عند تحقق الشروط.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "prophetic_method",
                title = "صفة عملية خطوة بخطوة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("غسل الكفين", "اغسل الكفين في بداية الوضوء."),
                            LearningStepItem("المضمضة والاستنشاق", "أدخل الماء إلى الفم والأنف ثم أخرجه بلطف دون مبالغة تضر."),
                            LearningStepItem("غسل الوجه", "عمم الوجه بالماء من حد الشعر المعتاد إلى الذقن، وبين الأذنين."),
                            LearningStepItem("غسل اليدين إلى المرفقين", "اغسل كل يد مع المرفق، وراع وصول الماء إلى المواضع التي قد يغفل عنها."),
                            LearningStepItem("مسح الرأس", "امسح الرأس بالماء، وتوجد تفاصيل مذهبية في مقدار المسح وكيفيته."),
                            LearningStepItem("غسل الرجلين إلى الكعبين", "اغسل القدمين مع الكعبين وتفقد ما بين الأصابع دون تكلف."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "mistakes",
                title = "أخطاء شائعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ترك جزء جاف", "العجلة قد تترك جزءا من الكعب أو المرفق بلا ماء."),
                            LearningStepItem("المبالغة في التكرار", "لا تحول الوضوء إلى تكرار مفتوح بسبب الشك."),
                            LearningStepItem("وجود حاجز", "انتبه للمواد التي تصنع طبقة تمنع وصول الماء."),
                            LearningStepItem("إيذاء النفس", "لا تبالغ في الاستنشاق إذا كان ذلك يسبب ضررا أو كانت لديك تعليمات طبية خاصة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "faq",
                title = "أسئلة سريعة",
                blocks = listOf(
                    LearningContentBlock.QuestionAnswer(
                        question = "هل يجب غسل كل عضو ثلاث مرات؟",
                        answer = "لا. المقصود استيعاب العضو بالغسل الواجب، والتكرار إلى ثلاث من السنن المشهورة في صفة الوضوء.",
                        referenceIds = listOf("bukhari_164"),
                    ),
                    LearningContentBlock.QuestionAnswer(
                        question = "ماذا أفعل إذا شككت بعد الفراغ أن جزءا لم يصبه الماء؟",
                        answer = "الشك العارض بعد الفراغ لا يتحول إلى إعادة مستمرة. إذا تيقنت يقينا معتبرا من ترك جزء واجب فتعالج الحالة بحسب قرب الزمن وتفاصيل المذهب الذي تتبعه.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_5_6", LearningReferenceKind.QURAN, "القرآن الكريم، سورة المائدة، الآية 5:6", "Quran 5:6"),
            LearningReference("bukhari_164", LearningReferenceKind.HADITH, "صحيح البخاري 164، كتاب الوضوء", "Sahih al-Bukhari 164"),
        ),
    )

    private val wuduNullifiers = LearningLesson(
        id = "wudu_nullifiers",
        titleRes = R.string.learn_topic_wudu_nullifiers,
        subtitleRes = R.string.learn_topic_wudu_nullifiers_sub,
        estimatedMinutes = 12,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "certain",
                title = "نواقض واضحة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الخارج من السبيلين", "خروج البول أو الغائط أو الريح من النواقض المعروفة للوضوء."),
                            LearningStepItem("زوال الإدراك", "الإغماء والسكر ونحوهما مما يزيل الإدراك له أثر في الطهارة."),
                            LearningStepItem("النوم", "تفاصيل النوم الناقض مرتبطة بحاله وهيئته ودرجة الاستغراق عند الفقهاء."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "differences",
                title = "مواضع اختلاف فقهي",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        intro = "بعض الأسباب ليست محل اتفاق بنفس الصورة، ولذلك لا يعرضها التطبيق على أنها حكم واحد قطعي.",
                        items = listOf(
                            LearningComparisonItem("لمس المرأة أو الرجل", "تختلف المذاهب في أثر مجرد اللمس، واللمس بشهوة، وما إذا كان ينقض الوضوء."),
                            LearningComparisonItem("مس الفرج", "توجد أقوال مختلفة في أثر مس الفرج بلا حائل."),
                            LearningComparisonItem("أكل لحم الإبل", "من المسائل التي وقع فيها اختلاف مشهور بين أهل العلم."),
                            LearningComparisonItem("خروج الدم والقيء", "تختلف المذاهب في بعض صورهما ومقاديرهما وأثرهما في الوضوء."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "doubt",
                title = "إذا شككت",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "لا تبن الحكم على الوهم",
                        body = "من تيقن الطهارة ثم عرض له شك مجرد في حصول الناقض لا يجعل الشك وحده سببا لسلسلة من إعادة الوضوء. أما إذا تيقن الناقض فيتوضأ من جديد.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "practical",
                title = "تطبيق عملي",
                blocks = listOf(
                    LearningContentBlock.QuestionAnswer(
                        question = "نمت فترة قصيرة، هل انتقض وضوئي؟",
                        answer = "لا يكفي طول الزمن وحده للإجابة؛ تفاصيل هيئة النوم ودرجة الإدراك معتبرة في كتب الفقه. عند تكرر الحالة اتبع مذهبا معتبرا أو اسأل جهة علمية موثوقة.",
                    ),
                ),
            ),
        ),
    )

    private val wipingOverFootwear = LearningLesson(
        id = "wiping_footwear",
        titleRes = R.string.learn_topic_wiping_footwear,
        subtitleRes = R.string.learn_topic_wiping_footwear_sub,
        estimatedMinutes = 11,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "basis",
                title = "أصل الرخصة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ثبت عن النبي ﷺ المسح على الخفين، ومن شروط الصورة الواردة أنه لبسهما على طهارة.",
                        referenceIds = listOf("bukhari_206"),
                    ),
                ),
            ),
            LearningSection(
                id = "conditions",
                title = "الشروط العملية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اللبس على طهارة", "يلبس ما سيُمسح عليه بعد إكمال طهارة القدمين."),
                            LearningStepItem("المسح في الحدث الأصغر", "المسح رخصة في الوضوء، ولا يقوم مقام الغسل من الجنابة."),
                            LearningStepItem("الموضع", "المشروع مسح ظاهر ما يلبس على القدم، لا غسله بالماء أثناء مدة المسح."),
                            LearningStepItem("المدة", "ورد للمقيم يوم وليلة وللمسافر ثلاثة أيام بلياليها، مع تفاصيل فقهية في بداية حساب المدة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "duration_evidence",
                title = "مدة المسح",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "في صحيح مسلم عن علي رضي الله عنه تحديد ثلاثة أيام ولياليهن للمسافر، ويوم وليلة للمقيم.",
                        referenceIds = listOf("muslim_276a"),
                    ),
                ),
            ),
            LearningSection(
                id = "socks",
                title = "الجوارب والخف",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "تفصيل مذهبي مهم",
                        body = "اتساع حكم المسح من الخف إلى أنواع الجوارب الحديثة وشروط سمكها وثباتها وسترها محل تفصيل بين الفقهاء. لذلك لا يساوي التطبيق بين كل جورب وكل خف دون تنبيه.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "ending",
                title = "متى تنتهي الرخصة؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تنتهي الرخصة بانتهاء مدتها، وبحصول ما يوجب الغسل، وتوجد تفاصيل فقهية في أثر نزع الخف أو الجورب أثناء المدة. من يعتمد المسح بصورة متكررة يستحسن أن يختار قولا فقهيا معتبرا ويلتزم ضوابطه كاملة.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_206", LearningReferenceKind.HADITH, "صحيح البخاري 206، كتاب الوضوء", "Sahih al-Bukhari 206"),
            LearningReference("muslim_276a", LearningReferenceKind.HADITH, "صحيح مسلم 276a، كتاب الطهارة", "Sahih Muslim 276a"),
        ),
    )
}
