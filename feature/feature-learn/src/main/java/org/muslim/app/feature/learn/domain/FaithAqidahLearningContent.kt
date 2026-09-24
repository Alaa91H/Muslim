package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FaithAqidahLearningContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            islamImanIhsan,
            pillarsOfFaith,
            tawhidAndWorship,
            knowingAllah,
            angels,
            revealedBooks,
            messengers,
            lastDay,
            qadar,
            faithQuestions,
        )
    }

    private val islamImanIhsan = LearningLesson(
        id = "pillars_islam",
        titleRes = R.string.learn_topic_pillars_islam,
        subtitleRes = R.string.learn_topic_pillars_islam_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "framework",
                title = "الإسلام والإيمان والإحسان",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "حديث جبريل جمع مراتب الدين في تعليم واحد: الإسلام، ثم الإيمان، ثم الإحسان.",
                        referenceIds = listOf("muslim_8a"),
                    ),
                    LearningContentBlock.Paragraph(
                        "هذه المستويات ليست مواد منفصلة عن الحياة: الإسلام ينظم الأعمال الظاهرة، والإيمان يبين ما يصدق به القلب، والإحسان يربي مراقبة الله وإتقان العبادة.",
                    ),
                ),
            ),
            LearningSection(
                id = "five_pillars",
                title = "أركان الإسلام الخمسة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الشهادتان", "الإقرار بوحدانية الله ورسالة محمد ﷺ مع فهم معناهما وقبولهما."),
                            LearningStepItem("الصلاة", "إقامة الصلوات المفروضة في أوقاتها وتعلم شروطها وأركانها تدريجيًا."),
                            LearningStepItem("الزكاة", "حق مالي واجب عند تحقق شروطه، وله أنصبة ومصارف وتفاصيل مستقلة."),
                            LearningStepItem("صيام رمضان", "عبادة سنوية تجمع الإمساك والنية والتقوى ولها أحكام للأعذار والقضاء."),
                            LearningStepItem("الحج", "واجب على المستطيع مرة في العمر وله مناسك وشروط ومسار تعلم مستقل."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "ihsan",
                title = "الإحسان ليس مرحلة للنخبة فقط",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "فسر النبي ﷺ الإحسان في حديث جبريل بأن تعبد الله على مقام المراقبة؛ فإنك إن لم تكن تراه فإنه يراك.",
                        referenceIds = listOf("muslim_8a"),
                    ),
                ),
            ),
            LearningSection(
                id = "learning_order",
                title = "كيف تتعلم الدين من هذه الخريطة؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("صحح الأصل", "افهم الشهادة وأركان الإيمان قبل الغرق في الفروع."),
                            LearningStepItem("تعلم ما تحتاجه الآن", "الصلاة والطهارة وما يمس حياتك مقدم على تفاصيل نادرة."),
                            LearningStepItem("حوّل العلم إلى عمل", "المعلومة التي لا تؤثر في العبادة والخلق تحتاج مراجعة للنية وطريقة التعلم."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference(
                id = "muslim_8a",
                kind = LearningReferenceKind.HADITH,
                citation = "صحيح مسلم، كتاب الإيمان، حديث جبريل",
                locator = "Sahih Muslim 8a",
            ),
        ),
    )

    private val pillarsOfFaith = LearningLesson(
        id = "pillars_iman",
        titleRes = R.string.learn_topic_pillars_iman,
        subtitleRes = R.string.learn_topic_pillars_iman_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "six",
                title = "أركان الإيمان الستة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "بيّن حديث جبريل الإيمان بالله وملائكته وكتبه ورسله واليوم الآخر والقدر خيره وشره.",
                        referenceIds = listOf("muslim_8a", "quran_2_285"),
                    ),
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الإيمان بالله", "توحيده وعبادته وحده والإيمان بما أخبر به عن نفسه."),
                            LearningStepItem("الملائكة", "الإيمان بوجودهم وما ثبت من وظائفهم وأسمائهم دون اختراع تفاصيل غيبية."),
                            LearningStepItem("الكتب", "الإيمان بما أنزل الله من وحي، وبالقرآن كتابًا خاتمًا محفوظًا للمسلمين."),
                            LearningStepItem("الرسل", "التصديق بجميع رسل الله وعدم رد رسول ثبتت رسالته في الوحي."),
                            LearningStepItem("اليوم الآخر", "الإيمان بالبعث والحساب والجزاء وما ثبت من أحوال الآخرة."),
                            LearningStepItem("القدر", "الإيمان بعلم الله وقدره مع بقاء مسؤولية الإنسان عن اختياره وعمله."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "unity",
                title = "لا نؤمن ببعض ونرد بعضًا",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "قرر القرآن أن المؤمنين يؤمنون بالله وملائكته وكتبه ورسله ولا يفرقون بين رسله من جهة أصل التصديق.",
                        referenceIds = listOf("quran_2_285", "quran_4_136"),
                    ),
                ),
            ),
            LearningSection(
                id = "unseen",
                title = "الغيب يؤخذ من الوحي",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "لا تضف إلى عالم الملائكة أو الآخرة أو القدر قصصًا وتفاصيل لمجرد انتشارها. في موضوعات الغيب تكون دقة المصدر أهم من كثرة التفاصيل.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "next",
                title = "من الخريطة إلى الدروس المتخصصة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "كل ركن من هذه الأركان له درس مستقل في هذا المسار. استخدم هذا الدرس كخريطة، ثم انتقل لما تحتاج إلى فهمه بتفصيل أكبر.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("muslim_8a", LearningReferenceKind.HADITH, "صحيح مسلم، كتاب الإيمان، حديث جبريل", "Sahih Muslim 8a"),
            LearningReference("quran_2_285", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:285", "Quran 2:285"),
            LearningReference("quran_4_136", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:136", "Quran 4:136"),
        ),
    )

    private val tawhidAndWorship = LearningLesson(
        id = "faith_tawhid_worship",
        titleRes = R.string.learn_topic_faith_tawhid_worship,
        subtitleRes = R.string.learn_topic_faith_tawhid_worship_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "message",
                title = "دعوة الرسل إلى عبادة الله",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "كرر القرآن أن دعوة الرسل قائمة على عبادة الله وحده واجتناب ما يعبد من دونه.",
                        referenceIds = listOf("quran_16_36", "quran_21_25"),
                    ),
                ),
            ),
            LearningSection(
                id = "worship",
                title = "ما المقصود بالعبادة؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "العبادة تشمل أعمال القلب واللسان والجوارح التي يتقرب بها العبد إلى الله: كالدعاء والصلاة والخوف والرجاء والمحبة والذكر والطاعة.",
                    ),
                ),
            ),
            LearningSection(
                id = "means",
                title = "الأسباب لا تستقل بالتأثير",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الأخذ بالأسباب الطبية والمالية والاجتماعية مشروع في أصله، لكن المسلم لا يجعل السبب إلهًا ولا ينسب له قدرة مطلقة مستقلة عن الله.",
                    ),
                ),
            ),
            LearningSection(
                id = "careful_labels",
                title = "لا تحول درس التوحيد إلى محكمة للناس",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الحكم على فعل معين غير الحكم على شخص بعينه. مسائل الشرك والكفر لها شروط وموانع وتفاصيل قضائية وعلمية؛ هذا المسار يعلّم الأصول ولا يمنح المستخدم أداة لتكفير الأفراد.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_16_36", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النحل، الآية 16:36", "Quran 16:36"),
            LearningReference("quran_21_25", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأنبياء، الآية 21:25", "Quran 21:25"),
        ),
    )

    private val knowingAllah = LearningLesson(
        id = "faith_knowing_allah",
        titleRes = R.string.learn_topic_faith_knowing_allah,
        subtitleRes = R.string.learn_topic_faith_knowing_allah_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "oneness",
                title = "الله واحد",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "تقرر سورة الإخلاص وحدانية الله وتنزهه عن الولد والمماثل.",
                        referenceIds = listOf("quran_112"),
                    ),
                ),
            ),
            LearningSection(
                id = "attributes",
                title = "إثبات بلا مماثلة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "جمع القرآن بين نفي المماثلة عن الله وإثبات السمع والبصر له.",
                        referenceIds = listOf("quran_42_11"),
                    ),
                    LearningContentBlock.Paragraph(
                        "في التعليم التأسيسي نثبت لله ما أثبته الوحي وننفي عنه ما نفاه الوحي، مع القطع بأنه لا يشبه خلقه. التفصيل الكلامي والمذهبي المتخصص لا يُختزل في بطاقة مبتدئ.",
                    ),
                ),
            ),
            LearningSection(
                id = "names",
                title = "الأسماء الحسنى طريق للمعرفة والعبادة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تعلم اسم من أسماء الله لا يقتصر على حفظ الترجمة؛ انظر إلى معناه وآثاره في الدعاء والخوف والرجاء والسلوك. قسم أسماء الله في التطبيق يكمل هذا المسار.",
                    ),
                ),
            ),
            LearningSection(
                id = "avoid_speculation",
                title = "قف حيث يقف الدليل",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "لا تبنِ تصورًا عن ذات الله من الخيال أو الصور أو التشبيهات البشرية. وفي المقابل لا تنسب لله اسمًا أو وصفًا لم يثبت لمجرد أن المعنى يبدو حسنًا.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_112", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الإخلاص 112:1-4", "Quran 112"),
            LearningReference("quran_42_11", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الشورى، الآية 42:11", "Quran 42:11"),
        ),
    )

    private val angels = LearningLesson(
        id = "faith_angels",
        titleRes = R.string.learn_topic_faith_angels,
        subtitleRes = R.string.learn_topic_faith_angels_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "belief",
                title = "الإيمان بالملائكة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "عدّ القرآن الإيمان بالملائكة ضمن أصول الإيمان.",
                        referenceIds = listOf("quran_2_285"),
                    ),
                ),
            ),
            LearningSection(
                id = "what_we_know",
                title = "نعلم بقدر ما ورد",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الوجود", "نؤمن بأنهم خلق من خلق الله، لا آلهة ولا أبناء لله."),
                            LearningStepItem("الطاعة", "نؤمن بما وصفهم به الوحي من عبادة الله وتنفيذ أمره."),
                            LearningStepItem("الأسماء والوظائف", "نثبت ما صح من أسماء ووظائف، ولا نعمم أسماء شعبية بلا دليل."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "stories",
                title = "القصص الشائعة ليست مصدرًا",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "تفاصيل أشكال الملائكة أو أعدادهم أو وظائفهم لا تؤخذ من القصص المتداولة أو المقاطع القصيرة. تحقق من القرآن والحديث الصحيح قبل النشر.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "effect",
                title = "أثر الإيمان بالملائكة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يزيد الإيمان بالغيب ومراقبة الإنسان لأعماله، ويذكره بأن عالم الوجود أوسع مما تدركه الحواس وحدها.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_285", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:285", "Quran 2:285"),
        ),
    )

    private val revealedBooks = LearningLesson(
        id = "faith_books",
        titleRes = R.string.learn_topic_faith_books,
        subtitleRes = R.string.learn_topic_faith_books_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "revelation",
                title = "الوحي هداية من الله",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بالإيمان بالكتاب المنزل على محمد ﷺ وبما أنزل من قبل.",
                        referenceIds = listOf("quran_4_136"),
                    ),
                ),
            ),
            LearningSection(
                id = "named",
                title = "ما نؤمن به",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "نؤمن بما سمّاه الوحي من كتب الله، ونؤمن إجمالًا بما أنزله على رسله. لا ننسب كتابًا إلى الله أو نحدد تفاصيل تاريخية بلا دليل.",
                    ),
                ),
            ),
            LearningSection(
                id = "quran",
                title = "القرآن مرجع المسلم العملي",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "دراسة العقيدة لا تستغني عن قراءة القرآن وفهم سياقه. انتقل من هذا الدرس إلى مسار القرآن لفهم الوحي والتفسير وآداب القراءة.",
                    ),
                ),
            ),
            LearningSection(
                id = "comparison",
                title = "الحوار حول الكتب السابقة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الحوار الديني يحتاج عدلًا ودقة. لا تنسب نصًا أو عقيدة إلى أتباع دين آخر من منشور مجهول؛ ارجع إلى مصادرهم المعلنة وإلى أهل العلم عند المقارنة.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_136", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:136", "Quran 4:136"),
        ),
    )

    private val messengers = LearningLesson(
        id = "faith_messengers",
        titleRes = R.string.learn_topic_faith_messengers,
        subtitleRes = R.string.learn_topic_faith_messengers_sub,
        estimatedMinutes = 16,
        sections = listOf(
            LearningSection(
                id = "mission",
                title = "رسالة الأنبياء",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "يقرر القرآن وحدة أصل دعوة الرسل إلى عبادة الله.",
                        referenceIds = listOf("quran_21_25", "quran_16_36"),
                    ),
                ),
            ),
            LearningSection(
                id = "all",
                title = "نصدق بجميع الرسل",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أعلن المؤمنون أنهم لا يفرقون بين رسل الله من جهة أصل الإيمان والتصديق.",
                        referenceIds = listOf("quran_2_285"),
                    ),
                ),
            ),
            LearningSection(
                id = "muhammad",
                title = "اتباع محمد ﷺ",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المسلم يتبع الوحي الذي جاء به محمد ﷺ، ويتعلم سنته من مصادر حديثية موثوقة. مسارا السنة والسيرة في التطبيق يكملان هذا الدرس.",
                    ),
                ),
            ),
            LearningSection(
                id = "stories",
                title = "ميز بين القرآن والرواية التاريخية",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "قصص الأنبياء باب يكثر فيه النقل غير الموثق. اجعل القرآن والحديث الصحيح أصلًا، وافصل بوضوح بين النص الثابت والتفاصيل التاريخية أو الإسرائيليات.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_21_25", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأنبياء، الآية 21:25", "Quran 21:25"),
            LearningReference("quran_16_36", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النحل، الآية 16:36", "Quran 16:36"),
            LearningReference("quran_2_285", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:285", "Quran 2:285"),
        ),
    )

    private val lastDay = LearningLesson(
        id = "faith_last_day",
        titleRes = R.string.learn_topic_faith_last_day,
        subtitleRes = R.string.learn_topic_faith_last_day_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "belief",
                title = "اليوم الآخر أصل من أصول الإيمان",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكر القرآن اليوم الآخر ضمن الأصول التي يجب الإيمان بها.",
                        referenceIds = listOf("quran_4_136"),
                    ),
                ),
            ),
            LearningSection(
                id = "meaning",
                title = "الحياة اختبار وليست عبثًا",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "يربط القرآن خلق الموت والحياة بالابتلاء في حسن العمل.",
                        referenceIds = listOf("quran_67_2"),
                    ),
                ),
            ),
            LearningSection(
                id = "subjects",
                title = "موضوعات هذا الباب",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الموت والبرزخ", "نثبت ما جاء به الوحي دون بناء خرائط تفصيلية من القصص."),
                            LearningStepItem("البعث والحشر", "الإيمان بأن الله يبعث الخلق للحساب."),
                            LearningStepItem("الحساب والجزاء", "الإنسان مسؤول عن عمله، ورحمة الله وعدله أصلان في فهم الجزاء."),
                            LearningStepItem("الجنة والنار", "نثبت ما ثبت في الوحي ونحذر من تحديد مصير أشخاص بأعيانهم بلا دليل."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "effect",
                title = "لماذا يغير هذا حياتك؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الإيمان بالآخرة يعيد ترتيب الأولويات: الحقوق والصدق والتوبة والعبادة ليست خسارة قصيرة المدى، لأن الحساب لا ينتهي بنهاية الحياة الدنيا.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_136", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:136", "Quran 4:136"),
            LearningReference("quran_67_2", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الملك، الآية 67:2", "Quran 67:2"),
        ),
    )

    private val qadar = LearningLesson(
        id = "faith_qadar",
        titleRes = R.string.learn_topic_faith_qadar,
        subtitleRes = R.string.learn_topic_faith_qadar_sub,
        estimatedMinutes = 19,
        sections = listOf(
            LearningSection(
                id = "pillar",
                title = "الإيمان بالقدر",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "عدّ حديث جبريل الإيمان بالقدر خيره وشره ضمن حقيقة الإيمان.",
                        referenceIds = listOf("muslim_8a"),
                    ),
                ),
            ),
            LearningSection(
                id = "knowledge_action",
                title = "القدر لا يلغي العمل",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "علم الله وقدره لا يحول الإنسان إلى آلة بلا اختيار. الشريعة تخاطب الإنسان بالأمر والنهي والمسؤولية، ولذلك لا يصح الاحتجاج بالقدر لتبرير ظلم أو ترك واجب.",
                    ),
                ),
            ),
            LearningSection(
                id = "calamity",
                title = "القدر والمصيبة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "عند المصيبة يجتمع الإيمان بالقدر مع الحزن والدعاء والعلاج وطلب الحق. التسليم لله لا يعني ترك الأسباب أو منع المتضرر من الشكوى والمساعدة.",
                    ),
                ),
            ),
            LearningSection(
                id = "questions",
                title = "المسائل الدقيقة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "العلاقة بين العلم الإلهي والمشيئة والاختيار من أبواب العقيدة الدقيقة. إذا دخل السؤال في تفاصيل فلسفية أو مذهبية، فاعرض الأقوال من مصادر موثوقة بدل تقديم جواب مختصر على أنه يحسم كل الخلاف.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("muslim_8a", LearningReferenceKind.HADITH, "صحيح مسلم، كتاب الإيمان، حديث جبريل", "Sahih Muslim 8a"),
        ),
    )

    private val faithQuestions = LearningLesson(
        id = "faith_questions",
        titleRes = R.string.learn_topic_faith_questions,
        subtitleRes = R.string.learn_topic_faith_questions_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "questioning",
                title = "السؤال ليس عيبًا",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "السؤال الصادق جزء طبيعي من التعلم. المشكلة ليست في وجود السؤال، بل في أخذ جواب خطير من مصدر مجهول أو بناء يقين على مقطع مجتزأ.",
                    ),
                ),
            ),
            LearningSection(
                id = "method",
                title = "طريقة التعامل مع شبهة أو إشكال",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("صغ السؤال بدقة", "هل الإشكال في نص، ترجمة، حكم، تاريخ، أم تجربة شخصية؟"),
                            LearningStepItem("تحقق من الأصل", "اقرأ الآية في سياقها وتأكد من صحة الحديث ونصه."),
                            LearningStepItem("فرّق بين الدين وسلوك المتدين", "خطأ شخص مسلم لا يثبت أن المبدأ الديني نفسه يأمر بخطئه."),
                            LearningStepItem("اطلب المصدر الأقوى", "قدّم القرآن والحديث الصحيح والشرح العلمي المتخصص على المقاطع الجدلية."),
                            LearningStepItem("اترك مساحة لعدم المعرفة", "ليس لازمًا أن تملك جوابًا فوريًا لكل سؤال حتى تستمر في التعلم والعبادة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "mental_load",
                title = "لا تحول البحث إلى دوامة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "إذا أصبحت متابعة الجدل الديني تسبب قلقًا قهريًا أو تعطل نومك وعبادتك وحياتك، قلل التعرض للمحتوى الجدلي واطلب مساعدة من معلم موثوق، ومع مختص صحي عند وجود أعراض نفسية مستمرة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "discussion",
                title = "أدب الحوار",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ناقش الفكرة دون تحقير صاحبها، واعترف إذا لم تعرف، ولا تنسب للآخر قولًا لم يقله. الهدف الوصول إلى الحق لا الفوز أمام الجمهور.",
                    ),
                ),
            ),
        ),
    )
}
