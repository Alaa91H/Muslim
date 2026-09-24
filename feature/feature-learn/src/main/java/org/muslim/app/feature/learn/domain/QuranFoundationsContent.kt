package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object QuranFoundationsContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            quranIntroduction,
            quranEtiquette,
            quranStructure,
            quranUnderstanding,
            quranLearningPlan,
        )
    }

    private val quranIntroduction = LearningLesson(
        id = "quran_intro",
        titleRes = R.string.learn_topic_quran_intro,
        subtitleRes = R.string.learn_topic_quran_intro_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "what",
                title = "ما القرآن؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "القرآن كلام الله المنزل على النبي محمد ﷺ، المتعبد بتلاوته، والمنقول إلينا بالتواتر. تعلم القرآن يشمل القراءة الصحيحة، وفهم المعنى، والعمل والهداية، لا مجرد إنهاء صفحات كثيرة.",
                    ),
                    LearningContentBlock.Evidence(
                        text = "أخبر الله بحفظ الذكر، وهو أصل في الثقة بنقل القرآن وصيانته.",
                        referenceIds = listOf("quran_15_9"),
                    ),
                ),
            ),
            LearningSection(
                id = "learn",
                title = "فضل التعلم والتعليم",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "قال النبي ﷺ: «خيركم من تعلم القرآن وعلمه».",
                        referenceIds = listOf("bukhari_5027"),
                    ),
                    LearningContentBlock.Paragraph(
                        "يدخل في التعلم تصحيح القراءة، والحفظ، وفهم المفردات والمعاني، ومعرفة ما يحتاجه القارئ من أحكام الأداء.",
                    ),
                ),
            ),
            LearningSection(
                id = "three_tracks",
                title = "ثلاثة مسارات متوازية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("التلاوة", "تحسين النطق والإيقاع الصحيح للتلاوة دون تكلف."),
                            LearningStepItem("الفهم", "الرجوع إلى ترجمة أو تفسير موثوق لفهم المعنى بحسب مستوى المتعلم."),
                            LearningStepItem("العمل", "ربط الآيات بالهداية والسلوك والعبادة دون استخراج أحكام تخصصية بغير علم."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "reader",
                title = "قارئ القرآن داخل التطبيق",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "ميزة القرآن مسؤولة عن عرض المصحف والتلاوة والترجمات والحفظ والعلامات وألوان التجويد. قسم Learn يشرح كيف تستخدم هذه الأدوات للتعلم ولا يكرر بيانات المصحف.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_15_9", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الحجر، الآية 15:9", "Quran 15:9"),
            LearningReference("bukhari_5027", LearningReferenceKind.HADITH, "صحيح البخاري 5027، كتاب فضائل القرآن", "Sahih al-Bukhari 5027"),
        ),
    )

    private val quranEtiquette = LearningLesson(
        id = "quran_etiquette",
        titleRes = R.string.learn_topic_quran_etiquette,
        subtitleRes = R.string.learn_topic_quran_etiquette_sub,
        estimatedMinutes = 11,
        sections = listOf(
            LearningSection(
                id = "intention",
                title = "النية والحضور",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "اقرأ طلبًا للهداية والقرب من الله، لا لمجرد تسجيل إنجاز. اختر وقتًا تستطيع فيه التركيز ولو كانت الكمية قليلة.",
                    ),
                ),
            ),
            LearningSection(
                id = "start",
                title = "الاستعاذة والبسملة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بالاستعاذة بالله من الشيطان عند قراءة القرآن.",
                        referenceIds = listOf("quran_16_98"),
                    ),
                    LearningContentBlock.Paragraph(
                        "تأتي البسملة في أوائل السور على الصفة المعروفة، مع استثناء سورة التوبة. وتفاصيل أحكام الجهر والوصل من مباحث القراءة والتجويد.",
                    ),
                ),
            ),
            LearningSection(
                id = "listening",
                title = "الاستماع",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "وجّه القرآن إلى الاستماع والإنصات عند تلاوته.",
                        referenceIds = listOf("quran_7_204"),
                    ),
                    LearningContentBlock.Paragraph(
                        "استخدام تسجيل قارئ متقن مفيد للتصحيح والمقارنة، لكنه لا يعوض المعلم عندما يحتاج المتعلم إلى تشخيص دقيق للمخارج والصفات.",
                    ),
                ),
            ),
            LearningSection(
                id = "devices",
                title = "المصحف الرقمي",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الهاتف أداة لعرض النص وليس المصحف الورقي في كل التفاصيل الفقهية. أحكام الطهارة ولمس المصحف من المسائل التي فيها تفصيل فقهي، فلا يحول التطبيق إعدادًا تقنيًا إلى حكم شرعي واحد.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_16_98", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النحل، الآية 16:98", "Quran 16:98"),
            LearningReference("quran_7_204", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأعراف، الآية 7:204", "Quran 7:204"),
        ),
    )

    private val quranStructure = LearningLesson(
        id = "quran_structure",
        titleRes = R.string.learn_topic_quran_structure,
        subtitleRes = R.string.learn_topic_quran_structure_sub,
        estimatedMinutes = 13,
        sections = listOf(
            LearningSection(
                id = "units",
                title = "السورة والآية والجزء",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("السورة", "القرآن مقسم إلى 114 سورة، ولكل سورة آيات محددة."),
                            LearningStepItem("الآية", "وحدة من نص السورة لها رقم داخلها في أنظمة العرض المعتادة."),
                            LearningStepItem("الجزء", "تقسيم عملي إلى 30 جزءًا يسهل تنظيم القراءة والختمة."),
                            LearningStepItem("الحزب والربع", "تقسيمات أصغر مستخدمة في كثير من المصاحف لتسهيل المراجعة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "makki_madani",
                title = "المكي والمدني",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "من أشهر الاصطلاحات أن المكي ما نزل قبل الهجرة والمدني ما نزل بعدها، بصرف النظر عن مكان النزول. معرفة ذلك تساعد في فهم سياق الخطاب والتدرج التاريخي.",
                    ),
                ),
            ),
            LearningSection(
                id = "revelation",
                title = "ترتيب النزول وترتيب المصحف",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ترتيب السور والآيات في المصحف ليس هو نفسه التسلسل التاريخي لنزول الوحي. لذلك تعرض ميزة القرآن بيانات السور مستقلة عن ترتيب النزول.",
                    ),
                ),
            ),
            LearningSection(
                id = "qiraat",
                title = "القراءات والروايات",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "مجال علمي متخصص",
                        body = "للقرآن قراءات متواترة وروايات وطرق لها أسانيد وضوابط. لا ينبغي اختزال اختلاف القراءة في «لهجات» أو تغيير نص عشوائي. قارئ التطبيق يحتاج أن يصرح بالرواية التي يعرضها أو يسمعها المستخدم.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
    )

    private val quranUnderstanding = LearningLesson(
        id = "quran_understanding",
        titleRes = R.string.learn_topic_quran_understanding,
        subtitleRes = R.string.learn_topic_quran_understanding_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "reflection",
                title = "التدبر والفهم",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "دعا القرآن إلى تدبر آياته وعدم المرور عليها بلا فهم.",
                        referenceIds = listOf("quran_47_24"),
                    ),
                ),
            ),
            LearningSection(
                id = "layers",
                title = "طبقات الفهم",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("المعنى المباشر", "ابدأ بترجمة موثوقة أو تفسير ميسر لفهم الكلمات والجملة."),
                            LearningStepItem("السياق", "اقرأ ما قبل الآية وما بعدها، واعرف موضوع السورة."),
                            LearningStepItem("التفسير", "ارجع إلى تفسير معتبر عند وجود معنى محتمل أو سبب نزول أو حكم."),
                            LearningStepItem("الاستنباط", "الأحكام العقدية والفقهية الدقيقة ليست مجالًا للتخمين الشخصي من ترجمة واحدة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "translations",
                title = "الترجمة ليست القرآن نفسه",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الترجمة تنقل معنى اجتهاديًا إلى لغة أخرى، وقد تختلف الترجمات في اختيار الألفاظ. عند بناء فهم دقيق قارن ترجمة موثوقة بتفسير، ولا تجعل اختلاف الصياغة تناقضًا في النص العربي.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "notes",
                title = "طريقة تدوين نافعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اكتب السؤال", "ما الذي لم أفهمه في هذه الآية؟"),
                            LearningStepItem("سجل المصدر", "اكتب اسم التفسير أو الترجمة التي رجعت إليها."),
                            LearningStepItem("افصل النص عن ملاحظتك", "لا تخلط كلام المفسر أو استنتاجك الشخصي مع نص الآية."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_47_24", LearningReferenceKind.QURAN, "القرآن الكريم، سورة محمد، الآية 47:24", "Quran 47:24"),
        ),
    )

    private val quranLearningPlan = LearningLesson(
        id = "quran_learning_plan",
        titleRes = R.string.learn_topic_quran_learning_plan,
        subtitleRes = R.string.learn_topic_quran_learning_plan_sub,
        estimatedMinutes = 10,
        sections = listOf(
            LearningSection(
                id = "baseline",
                title = "حدد نقطة البداية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الحروف", "إن كنت لا تميز الحروف والحركات فابدأ بمسار القاعدة النورانية."),
                            LearningStepItem("القراءة", "إن كنت تقرأ ببطء فركز على الطلاقة ومخارج الحروف قبل حفظ قواعد كثيرة."),
                            LearningStepItem("التجويد", "إن كانت القراءة سليمة إجمالًا فابدأ بالقواعد الأكثر تكرارًا ثم التطبيق."),
                            LearningStepItem("الحفظ", "الحفظ يحتاج مراجعة متباعدة، لا إضافة جديد بلا تثبيت القديم."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "routine",
                title = "روتين 20 دقيقة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("5 دقائق استماع", "استمع لمقطع قصير من قارئ متقن."),
                            LearningStepItem("10 دقائق قراءة", "اقرأ نفس المقطع ببطء وسجل مواضع الخطأ."),
                            LearningStepItem("5 دقائق مراجعة", "كرر موضعين أو ثلاثة بدل إعادة صفحة كاملة بلا تركيز."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "feedback",
                title = "التغذية الراجعة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "التجويد علم أداء؛ النص واللون لا يستطيعان سماع خطئك. اجعل المراجعة مع معلم أو قارئ متقن جزءًا دوريًا من الخطة إذا أمكن.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "tools",
                title = "استخدام ميزات التطبيق",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "استفد من التلاوة الصوتية، العلامات، آخر موضع قراءة، والتنزيلات دون اتصال. ألوان التجويد تساعد في ملاحظة القاعدة، لكنها لا تثبت وحدها أن النطق صحيح.",
                    ),
                ),
            ),
        ),
    )
}
