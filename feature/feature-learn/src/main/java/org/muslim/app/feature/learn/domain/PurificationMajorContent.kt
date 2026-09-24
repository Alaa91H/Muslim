package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object PurificationMajorContent {

    val lessons: List<LearningLesson> = listOf(
        ghusl,
        tayammum,
        menstruationAndPostpartum,
        excusedPerson,
    )

    private val ghusl = LearningLesson(
        id = "ghusl",
        titleRes = R.string.learn_topic_ghusl,
        subtitleRes = R.string.learn_topic_ghusl_sub,
        estimatedMinutes = 15,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "when",
                title = "متى يجب الغسل؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الجنابة", "يجب الغسل بسبب الجماع، وبسبب خروج المني على الصفة المعتبرة في كتب الفقه."),
                            LearningStepItem("انتهاء الحيض", "إذا انقطع دم الحيض وتحققت علامة الطهر تغتسل المرأة قبل ما يشترط له الطهارة الكبرى."),
                            LearningStepItem("انتهاء النفاس", "بعد انقطاع دم النفاس وثبوت الطهر يُشرع الغسل."),
                        ),
                    ),
                    LearningContentBlock.Callout(
                        title = "حالات تحتاج سؤالا خاصا",
                        body = "الإفرازات غير المعتادة، النزف المرضي، العمليات الجراحية، والجروح أو الأجهزة الطبية قد تحتاج جمعا بين الحكم الفقهي والتوجيه الطبي.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "evidence",
                title = "الأصل من السنة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "وصفت عائشة رضي الله عنها غسل النبي ﷺ من الجنابة: غسل اليدين والفرج، ثم الوضوء، ثم إيصال الماء إلى أصول الشعر، ثم إفاضة الماء على الرأس والجسد.",
                        referenceIds = listOf("muslim_316a"),
                    ),
                    LearningContentBlock.Evidence(
                        text = "جاء في حديث أم سليم أن رؤية الماء بعد الاحتلام توجب الغسل للمرأة كما للرجل.",
                        referenceIds = listOf("muslim_313a"),
                    ),
                ),
            ),
            LearningSection(
                id = "minimum",
                title = "الحد الأدنى والصفة الأكمل",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الحد الأدنى يدور حول النية وتعميم البدن بالماء على الوجه المعتبر، مع اختلاف فقهي في بعض التفاصيل مثل المضمضة والاستنشاق والدلك. أما الصفة الأكمل فتتبع ما ورد في غسل النبي ﷺ.",
                    ),
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("النية", "ينوي رفع الحدث الأكبر."),
                            LearningStepItem("تنظيف موضع الأذى", "يغسل ما على البدن من أذى أو نجاسة."),
                            LearningStepItem("الوضوء", "يتوضأ وضوء الصلاة ضمن صفة الغسل الأكمل."),
                            LearningStepItem("الرأس", "يوصل الماء إلى أصول الشعر ثم يفيض الماء على الرأس."),
                            LearningStepItem("سائر البدن", "يعمم الجسد بالماء ويتفقد المواضع الخفية من غير وسوسة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "hair",
                title = "الشعر والضفائر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المعتبر وصول الماء إلى أصول الشعر وفروة الرأس، وتوجد تفاصيل في نقض الضفائر بحسب سبب الغسل وحال الشعر. إذا كان الشعر أو التسريحة تمنع وصول الماء فعلا فلا يكفي مجرد بلل السطح.",
                    ),
                ),
            ),
            LearningSection(
                id = "mistakes",
                title = "أخطاء شائعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("نسيان مواضع مخفية", "مثل ما وراء الأذن أو ثنيات الجلد أو ما تحت مادة عازلة."),
                            LearningStepItem("تحويل الغسل إلى وسواس", "لا يلزم تكرار الغسل مرات بسبب احتمالات بعيدة بعد تعميم الماء."),
                            LearningStepItem("الخلط بين النظافة والطهارة", "الاستحمام العادي قد يحقق الغسل إذا تحققت نيته وشروطه، لكن النظافة وحدها ليست بديلا عن النية عند من يشترطها."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("muslim_316a", LearningReferenceKind.HADITH, "صحيح مسلم 316a، كتاب الحيض", "Sahih Muslim 316a"),
            LearningReference("muslim_313a", LearningReferenceKind.HADITH, "صحيح مسلم 313a، كتاب الحيض", "Sahih Muslim 313a"),
        ),
    )

    private val tayammum = LearningLesson(
        id = "tayammum",
        titleRes = R.string.learn_topic_tayammum,
        subtitleRes = R.string.learn_topic_tayammum_sub,
        estimatedMinutes = 13,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "why",
                title = "ما التيمم؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "التيمم بدل شرعي عن الطهارة بالماء عند تحقق سببه، وليس طريقة أسهل تُختار مع القدرة الطبيعية على استعمال الماء. من أسبابه فقد الماء أو تعذر استعماله بسبب ضرر معتبر ونحو ذلك.",
                    ),
                ),
            ),
            LearningSection(
                id = "evidence",
                title = "الدليل",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "نصت آية المائدة 5:6 على التيمم بالصعيد الطيب عند عدم وجود الماء في الأحوال المذكورة في الآية.",
                        referenceIds = listOf("quran_5_6"),
                    ),
                    LearningContentBlock.Evidence(
                        text = "في حديث عمار رضي الله عنه بيّن النبي ﷺ أن التيمم يكفي فيه مسح الوجه واليدين بعد الضرب على الأرض.",
                        referenceIds = listOf("bukhari_338"),
                    ),
                ),
            ),
            LearningSection(
                id = "steps",
                title = "الصفة العملية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("تحقق من السبب", "تأكد من فقد الماء أو تعذر استعماله على الوجه المعتبر."),
                            LearningStepItem("النية", "انو التيمم للطهارة المطلوبة."),
                            LearningStepItem("الصعيد الطيب", "اضرب بيديك على صعيد طيب مناسب."),
                            LearningStepItem("الوجه واليدان", "امسح الوجه واليدين على الصفة الواردة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "differences",
                title = "تفاصيل المذاهب",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "نوع الصعيد الذي يصح به التيمم، وعدد الضربات، ومدى مسح اليدين، وبعض أحكام الوقت وإعادة التيمم فيها تفاصيل بين المذاهب. الدرس يثبت الأصل المشترك ويترك الفروع الدقيقة للمسار الفقهي.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "ending",
                title = "متى ينتهي حكمه؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "إذا زال العذر وأصبح الماء متاحا ويمكن استعماله بلا ضرر، يعود الأصل وهو الطهارة بالماء للعبادات اللاحقة. كما يؤثر في التيمم ما يؤثر في الطهارة بحسب نوع الحدث الذي تيمم عنه.",
                    ),
                ),
            ),
            LearningSection(
                id = "real_world",
                title = "السفر والمرض",
                blocks = listOf(
                    LearningContentBlock.QuestionAnswer(
                        question = "أنا في طائرة أو قطار، هل أنتقل مباشرة إلى التيمم؟",
                        answer = "لا. وجود وسيلة نقل لا يكفي وحده؛ يُنظر أولا في توفر الماء وإمكان استعماله بأمان ومن غير مخالفة لتعليمات السلامة، ثم في شروط التيمم حسب الحالة.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_5_6", LearningReferenceKind.QURAN, "القرآن الكريم، سورة المائدة، الآية 5:6", "Quran 5:6"),
            LearningReference("bukhari_338", LearningReferenceKind.HADITH, "صحيح البخاري 338، كتاب التيمم", "Sahih al-Bukhari 338"),
        ),
    )

    private val menstruationAndPostpartum = LearningLesson(
        id = "menstruation_postpartum",
        titleRes = R.string.learn_topic_menstruation_postpartum,
        subtitleRes = R.string.learn_topic_menstruation_postpartum_sub,
        estimatedMinutes = 20,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "terms",
                title = "المصطلحات الأساسية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الحيض", "دم طبيعي معروف للمرأة في أوقات معتادة، وله أحكام خاصة في الصلاة والصيام والطواف والعلاقة الزوجية."),
                            LearningStepItem("النفاس", "الدم المتعلق بالولادة، وله في الجملة أحكام الحيض في مسائل العبادة مع تفاصيل المدة."),
                            LearningStepItem("الاستحاضة", "نزف ليس حيضا ولا نفاسا، وتحتاج أحكامه إلى تمييز العادة والصفات والمدة حسب الحالة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "quran",
                title = "أصل حكم الحيض",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكرت سورة البقرة 2:222 الحيض، ونهت عن الجماع أثناءه حتى يحصل الطهر والتطهر.",
                        referenceIds = listOf("quran_2_222"),
                    ),
                ),
            ),
            LearningSection(
                id = "worship",
                title = "الصلاة والصيام",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ثبت عن عائشة رضي الله عنها أن الحائض كانت تقضي الصوم بعد الطهر ولا تقضي الصلوات التي فاتتها زمن الحيض.",
                        referenceIds = listOf("muslim_335c"),
                    ),
                    LearningContentBlock.Callout(
                        title = "تمييز اليوم مهم",
                        body = "وقت بداية الدم ووقت تحقق الطهر قد يؤثران في صلاة أو صوم يوم بعينه؛ لذلك تُسجل المرأة عادتها ووقت التغير بقدر معقول عند الحاجة.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "purity_sign",
                title = "معرفة الطهر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تُعرف نهاية الحيض بعلامة الطهر المعتادة للمرأة، مثل الجفاف التام أو القصة البيضاء عند من تراها. الإفرازات والنزف المتقطع والحالات غير المعتادة تحتاج معرفة العادة والسياق ولا يصح اختزالها في قاعدة زمنية واحدة لكل النساء.",
                    ),
                ),
            ),
            LearningSection(
                id = "differences",
                title = "لماذا تحتاج هذه المسائل تفصيلا؟",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        intro = "هذه الأبواب من أكثر أبواب الطهارة التي تختلف فيها التفاصيل بحسب العادة والمذهب والصورة الطبية.",
                        items = listOf(
                            LearningComparisonItem("أقل الحيض وأكثره", "للمذاهب مناهج مختلفة في تحديد بعض الحدود والتمييز بين الحيض والاستحاضة."),
                            LearningComparisonItem("النقاء المتخلل", "حكم الانقطاع القصير بين دفعات الدم يختلف بحسب المدة والعادة والتفصيل الفقهي."),
                            LearningComparisonItem("النفاس", "توجد تفاصيل في أكثر مدته وفي الدم المنقطع والعائد."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "medical",
                title = "الجانب الطبي",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "النزف غير الطبيعي",
                        body = "النزف الغزير، الألم الشديد، الدوار، النزف أثناء الحمل، أو تغير النمط بصورة مقلقة ليست مسألة فقهية فقط؛ تحتاج تقييما طبيا مناسبا.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_222", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:222", "Quran 2:222"),
            LearningReference("muslim_335c", LearningReferenceKind.HADITH, "صحيح مسلم 335c، كتاب الحيض", "Sahih Muslim 335c"),
        ),
    )

    private val excusedPerson = LearningLesson(
        id = "excused_person",
        titleRes = R.string.learn_topic_excused_person,
        subtitleRes = R.string.learn_topic_excused_person_sub,
        estimatedMinutes = 10,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "definition",
                title = "ما المقصود بالعذر المستمر؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يدخل في هذا الباب من يستمر معه سبب يؤثر في الطهارة على نحو يصعب معه أداء العبادة بالطريقة المعتادة، مثل بعض صور سلس البول أو النزف المستمر أو خروج الريح المرضي. ليست كل حالة متكررة عذرا مستمرا بالمعنى الفقهي.",
                    ),
                ),
            ),
            LearningSection(
                id = "principle",
                title = "المبدأ العملي",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اعرف نمط الحالة", "هل تنقطع مدة تكفي للطهارة والصلاة أم تستمر غالب الوقت؟"),
                            LearningStepItem("احفظ النجاسة بقدر الاستطاعة", "استعمل وسائل مناسبة تمنع انتشار النجاسة من غير ضرر."),
                            LearningStepItem("اتبع حكم المذهب أو الفتوى", "تفاصيل الوضوء لكل وقت أو لكل صلاة ومدى استمرار الطهارة تختلف بين المذاهب."),
                            LearningStepItem("لا تترك الصلاة تلقائيا", "وجود المرض أو العذر لا يعني سقوط الصلاة من نفسه؛ بل توجد رخص وكيفيات بحسب القدرة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "health",
                title = "العلاج والخصوصية",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "سلس البول والنزف المتكرر وأعراض الجهاز الهضمي قد تكون لها أسباب طبية قابلة للعلاج. طلب العلاج لا يتعارض مع الأخذ بالرخصة الشرعية، ويحافظ التطبيق على صياغة لا تطلب من المستخدم إدخال تفاصيل صحية حساسة.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "ask",
                title = "متى أحتاج فتوى شخصية؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "إذا كانت الحالة تتبدل خلال اليوم، أو يصعب تحديد وقت الانقطاع، أو كانت مرتبطة بقسطرة أو جرح أو عملية أو دواء، فالأفضل عرض وصف مختصر دقيق للحالة على جهة علمية موثوقة مع الاستفادة من رأي الطبيب في الجانب الطبي.",
                    ),
                ),
            ),
        ),
    )
}
