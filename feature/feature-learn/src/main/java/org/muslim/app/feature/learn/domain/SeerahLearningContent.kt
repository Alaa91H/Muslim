package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object SeerahLearningContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            seerahMethod,
            seerahEarlyLife,
            seerahRevelationMakkah,
            seerahHijrah,
            seerahMadinah,
            seerahMajorEvents,
            seerahCharacterLegacy,
        )
    }

    private val seerahMethod = LearningLesson(
        id = "seerah_method",
        titleRes = R.string.learn_topic_seerah_method,
        subtitleRes = R.string.learn_topic_seerah_method_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "purpose",
                title = "لماذا ندرس السيرة؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "السيرة تساعد على فهم نزول الوحي في واقعه، وتكشف كيف طبق النبي ﷺ الإيمان والعبادة والأخلاق والدعوة وبناء المجتمع. الهدف ليس جمع القصص فقط، بل فهم التسلسل والسياق والدروس.",
                    ),
                ),
            ),
            LearningSection(
                id = "sources",
                title = "طبقات المصادر",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("القرآن", "أوثق أصل للأحداث التي نص عليها، مع الحاجة إلى تفسير السياق."),
                            LearningStepItem("الحديث الصحيح", "يحفظ تفاصيل مهمة من الوحي والهجرة والغزوات والعبادة والحياة اليومية."),
                            LearningStepItem("كتب السيرة والتاريخ", "تجمع التسلسل والأخبار، لكن أسانيد الأخبار ودرجاتها ليست في مستوى واحد."),
                            LearningStepItem("الدراسات الحديثة", "تفيد في الخرائط والتسلسل والتحليل إذا ميزت بين المصدر القديم والاستنتاج المعاصر."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "certainty",
                title = "درجات اليقين",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "ليس كل خبر مشهور في السيرة ثابتًا بالدرجة نفسها. التطبيق يفرق بين ما يشهد له القرآن أو الحديث الصحيح وبين التفاصيل التاريخية التي تحتاج مراجعة، ولا يحول الرواية الشائعة إلى حقيقة قطعية.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "timeline",
                title = "كيف تقرأ الخط الزمني؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "اقرأ السيرة على مراحل: ما قبل البعثة، بدء الوحي، المرحلة المكية، الهجرة، بناء المجتمع في المدينة، الأحداث العسكرية والسياسية، فتح مكة، ثم السنوات الأخيرة. هذا يمنع خلط أحداث متباعدة في قصة واحدة.",
                    ),
                ),
            ),
        ),
    )

    private val seerahEarlyLife = LearningLesson(
        id = "seerah_early_life",
        titleRes = R.string.learn_topic_seerah_early_life,
        subtitleRes = R.string.learn_topic_seerah_early_life_sub,
        estimatedMinutes = 13,
        sections = listOf(
            LearningSection(
                id = "makkah",
                title = "مكة والبيئة الأولى",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ولد النبي محمد ﷺ في مكة من قريش، ونشأ في بيئة عربية قبل الإسلام كان فيها البيت الحرام والتجارة والقبيلة حاضرة بقوة، مع انتشار الشرك وبقاء آثار من ملة إبراهيم.",
                    ),
                ),
            ),
            LearningSection(
                id = "orphan",
                title = "اليتم والرعاية",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكّرت سورة الضحى النبي ﷺ بنعمة الرعاية بعد اليتم والهداية والإغناء.",
                        referenceIds = listOf("quran_93_6_8"),
                    ),
                    LearningContentBlock.Paragraph(
                        "تفاصيل طفولته وانتقال كفالته تروى في كتب السيرة، ويُتعامل معها بحسب قوة كل خبر بدل مساواة جميع التفاصيل.",
                    ),
                ),
            ),
            LearningSection(
                id = "khadijah",
                title = "خديجة رضي الله عنها والحياة قبل البعثة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "كان زواجه من خديجة رضي الله عنها محورًا مهمًا في حياته قبل البعثة وبعدها؛ وكانت أول من ثبته عند بدء الوحي وآمنت به. بعض التفاصيل التجارية والعمرية المشهورة في المصادر التاريخية تختلف قوة أسانيدها.",
                    ),
                ),
            ),
            LearningSection(
                id = "preparation",
                title = "الخلوة قبل الوحي",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "حديث بدء الوحي في صحيح البخاري يذكر حب الخلاء للنبي ﷺ وتعبده في غار حراء قبل نزول الوحي.",
                        referenceIds = listOf("bukhari_3"),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_93_6_8", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الضحى، الآيات 93:6-8", "Quran 93:6-8"),
            LearningReference("bukhari_3", LearningReferenceKind.HADITH, "صحيح البخاري 3، كتاب بدء الوحي", "Sahih al-Bukhari 3"),
        ),
    )

    private val seerahRevelationMakkah = LearningLesson(
        id = "seerah_revelation_makkah",
        titleRes = R.string.learn_topic_seerah_revelation_makkah,
        subtitleRes = R.string.learn_topic_seerah_revelation_makkah_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "first_revelation",
                title = "بدء الوحي",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "يروي حديث عائشة رضي الله عنها في صحيح البخاري بدء الوحي في غار حراء ولقاء جبريل عليه السلام.",
                        referenceIds = listOf("bukhari_3"),
                    ),
                    LearningContentBlock.Evidence(
                        text = "أول ما نزل من صدر سورة العلق يبدأ بالأمر بالقراءة باسم الرب الخالق.",
                        referenceIds = listOf("quran_96_1_5"),
                    ),
                ),
            ),
            LearningSection(
                id = "early_call",
                title = "الدعوة الأولى",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "بدأت الدعوة بالإيمان بالله وترك الشرك وإصلاح القلب والسلوك. ومع اتساع الدعوة ظهر الصدام مع مصالح قريش الدينية والاجتماعية والاقتصادية، وواجه المسلمون صورًا متعددة من الضغط والأذى.",
                    ),
                ),
            ),
            LearningSection(
                id = "persecution",
                title = "الثبات أمام الأذى",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تعرض بعض المسلمين للأذى والمقاطعة والضغط، وانتقل بعضهم إلى الحبشة في مرحلة مبكرة. تفاصيل الأسماء والأعداد والتواريخ تحتاج إلى مصادر السيرة المتخصصة، بينما الدرس العام هو صبر الجماعة الناشئة مع تجنب رد الظلم بظلم.",
                    ),
                ),
            ),
            LearningSection(
                id = "makkah_themes",
                title = "موضوعات الوحي المكي",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("التوحيد", "ترسيخ عبادة الله وحده ونقد الشرك."),
                            LearningStepItem("الآخرة", "البعث والحساب والجنة والنار."),
                            LearningStepItem("قصص الأنبياء", "تثبيت الرسول والمؤمنين وبيان سنن الدعوة."),
                            LearningStepItem("الأخلاق والعدل", "إصلاح الإنسان والمجتمع قبل قيام الدولة."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_3", LearningReferenceKind.HADITH, "صحيح البخاري 3، كتاب بدء الوحي", "Sahih al-Bukhari 3"),
            LearningReference("quran_96_1_5", LearningReferenceKind.QURAN, "القرآن الكريم، سورة العلق، الآيات 96:1-5", "Quran 96:1-5"),
        ),
    )

    private val seerahHijrah = LearningLesson(
        id = "seerah_hijrah",
        titleRes = R.string.learn_topic_seerah_hijrah,
        subtitleRes = R.string.learn_topic_seerah_hijrah_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "الهجرة نقطة تحول",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الهجرة من مكة إلى المدينة لم تكن انتقالًا جغرافيًا فقط؛ انتقلت الدعوة إلى مرحلة بناء مجتمع منظم له مسجد وروابط اجتماعية واتفاقات ومسؤوليات مشتركة.",
                    ),
                ),
            ),
            LearningSection(
                id = "cave",
                title = "الغار والثقة بالله",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "يشير القرآن إلى وجود النبي ﷺ وصاحبه في الغار وإلى قوله: «لا تحزن إن الله معنا».",
                        referenceIds = listOf("quran_9_40"),
                    ),
                ),
            ),
            LearningSection(
                id = "planning",
                title = "التوكل مع الأخذ بالأسباب",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تظهر روايات الهجرة اجتماع التوكل مع التخطيط: اختيار الوقت والطريق والرفقة والتمويه المشروع والاستعانة بمن يؤدي دورًا محددًا. لا تعني الثقة بالله ترك الأسباب العملية.",
                    ),
                ),
            ),
            LearningSection(
                id = "calendar",
                title = "الهجرة والتقويم",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "التقويم الهجري جُعل مبدؤه مرتبطًا بسنة الهجرة في عهد عمر رضي الله عنه، لكن بداية السنة هي محرم وليست يوم وصول النبي ﷺ إلى المدينة. لا تخلط بين الحدث التاريخي ونقطة بدء عد السنوات.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_9_40", LearningReferenceKind.QURAN, "القرآن الكريم، سورة التوبة، الآية 9:40", "Quran 9:40"),
        ),
    )

    private val seerahMadinah = LearningLesson(
        id = "seerah_madinah",
        titleRes = R.string.learn_topic_seerah_madinah,
        subtitleRes = R.string.learn_topic_seerah_madinah_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "mosque",
                title = "المسجد مركز المجتمع",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "في المدينة صار المسجد مكانًا للصلاة والتعليم والاجتماع واستقبال الوفود وتنظيم شؤون عامة. هذا يوضح أن العبادة والتعليم والمجتمع لم تكن مجالات معزولة في التجربة النبوية.",
                    ),
                ),
            ),
            LearningSection(
                id = "brotherhood",
                title = "المهاجرون والأنصار",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "واجه المهاجرون فقدان المال والديار وشبكات الدعم، وكان للأنصار دور في الإيواء والنصرة. تظهر المرحلة كيف يعالج المجتمع آثار الهجرة بالنصرة والتكافل لا بالشعارات فقط.",
                    ),
                ),
            ),
            LearningSection(
                id = "plural_society",
                title = "إدارة مجتمع متعدد",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ضمت المدينة جماعات وقبائل وديانات مختلفة، وتعاملت السيرة مع عهود والتزامات وعلاقات حرب وسلم. تفاصيل الوثائق والأحداث السياسية تحتاج قراءة تاريخية دقيقة ولا تختزل في جملة واحدة.",
                    ),
                ),
            ),
            LearningSection(
                id = "law",
                title = "تدرج التشريع",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "في المرحلة المدنية نزلت أحكام كثيرة تنظم العبادة والأسرة والمعاملات والعلاقات العامة. فهم زمن النزول والسياق يساعد في تفسير انتقال المجتمع من جماعة مضطهدة إلى مجتمع ذي مسؤوليات أوسع.",
                    ),
                ),
            ),
        ),
    )

    private val seerahMajorEvents = LearningLesson(
        id = "seerah_major_events",
        titleRes = R.string.learn_topic_seerah_major_events,
        subtitleRes = R.string.learn_topic_seerah_major_events_sub,
        estimatedMinutes = 20,
        sections = listOf(
            LearningSection(
                id = "badr",
                title = "بدر",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكر القرآن نصر الله للمؤمنين في بدر مع قلة حالهم، وربط الحدث بالشكر والتقوى.",
                        referenceIds = listOf("quran_3_123"),
                    ),
                    LearningContentBlock.Paragraph(
                        "لا يختزل درس بدر في تفاصيل عسكرية؛ فهو أيضًا درس في القرار والشورى والثبات وكيف غيّر الانتصار موقع المجتمع المسلم في محيطه.",
                    ),
                ),
            ),
            LearningSection(
                id = "uhud",
                title = "أحد",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "عرض القرآن في آل عمران جوانب من اضطراب الصف في أحد وآثار مخالفة الأمر ثم التوبة والتربية بعد الهزيمة.",
                        referenceIds = listOf("quran_3_152"),
                    ),
                ),
            ),
            LearningSection(
                id = "ahzab",
                title = "الخندق والأحزاب",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكرت سورة الأحزاب مجيء الجيوش والابتلاء الشديد ثم دفع الله الأحزاب.",
                        referenceIds = listOf("quran_33_9"),
                    ),
                    LearningContentBlock.Paragraph(
                        "تظهر المرحلة قيمة التحصين والتخطيط والشورى مع الصبر النفسي في حصار طويل، لا مجرد المواجهة المباشرة.",
                    ),
                ),
            ),
            LearningSection(
                id = "hudaybiyyah",
                title = "الحديبية",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "افتتحت سورة الفتح بوصف الفتح المبين في سياق الحديبية وما ترتب عليها.",
                        referenceIds = listOf("quran_48_1"),
                    ),
                    LearningContentBlock.Paragraph(
                        "تُظهر الحديبية أن الاتفاق الذي يبدو لبعض الناس تنازلًا قصير المدى قد يفتح مجالًا أوسع للدعوة والسلام إذا قُرئت نتائجه واستراتيجيته كاملة.",
                    ),
                ),
            ),
            LearningSection(
                id = "conquest",
                title = "فتح مكة وما بعده",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "عاد النبي ﷺ إلى مكة منتصرًا بعد سنوات من الإخراج والصراع، ثم دخلت الدعوة مرحلة أوسع بين قبائل الجزيرة. ينبغي دراسة تفاصيل العفو والأمان وهدم الأصنام من مصادرها بدل تكرار خطب أو عبارات مشهورة بلا تثبت.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_3_123", LearningReferenceKind.QURAN, "القرآن الكريم، سورة آل عمران، الآية 3:123", "Quran 3:123"),
            LearningReference("quran_3_152", LearningReferenceKind.QURAN, "القرآن الكريم، سورة آل عمران، الآية 3:152", "Quran 3:152"),
            LearningReference("quran_33_9", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأحزاب، الآية 33:9", "Quran 33:9"),
            LearningReference("quran_48_1", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الفتح، الآية 48:1", "Quran 48:1"),
        ),
    )

    private val seerahCharacterLegacy = LearningLesson(
        id = "seerah_character_legacy",
        titleRes = R.string.learn_topic_seerah_character_legacy,
        subtitleRes = R.string.learn_topic_seerah_character_legacy_sub,
        estimatedMinutes = 16,
        sections = listOf(
            LearningSection(
                id = "example",
                title = "القدوة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "وصف القرآن رسول الله ﷺ بأنه أسوة حسنة لمن يرجو الله واليوم الآخر.",
                        referenceIds = listOf("quran_33_21"),
                    ),
                ),
            ),
            LearningSection(
                id = "character",
                title = "الخلق",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أثنى القرآن على خلق النبي ﷺ بقوله: «وإنك لعلى خلق عظيم».",
                        referenceIds = listOf("quran_68_4"),
                    ),
                    LearningContentBlock.Paragraph(
                        "قراءة السيرة أخلاقيًا تعني ملاحظة الرحمة والصبر والعدل والوفاء والشورى والتواضع في مواقف مختلفة، لا اقتطاع حادثة واحدة وبناء صورة كاملة عليها.",
                    ),
                ),
            ),
            LearningSection(
                id = "mercy",
                title = "الرحمة والرسالة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "وصف القرآن بعثة النبي ﷺ بأنها رحمة للعالمين.",
                        referenceIds = listOf("quran_21_107"),
                    ),
                ),
            ),
            LearningSection(
                id = "lessons",
                title = "كيف تنقل السيرة إلى حياتك؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اختر خُلقًا", "ركز أسبوعًا على خلق واحد مثل الصبر أو الوفاء واربطه بموقف صحيح من السيرة."),
                            LearningStepItem("افصل المبدأ عن ظرف الحدث", "ليس كل تصرف تاريخي قالبًا ينسخ حرفيًا في كل زمان ومكان."),
                            LearningStepItem("ارجع للفقه عند الأحكام", "السيرة مصدر مهم للفهم، لكن تنزيل حكم شرعي معاصر يحتاج أدوات الفقه والأصول."),
                            LearningStepItem("راجع المصدر", "قبل مشاركة قصة مؤثرة، تحقق من أصلها ودرجة خبرها."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_33_21", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأحزاب، الآية 33:21", "Quran 33:21"),
            LearningReference("quran_68_4", LearningReferenceKind.QURAN, "القرآن الكريم، سورة القلم، الآية 68:4", "Quran 68:4"),
            LearningReference("quran_21_107", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأنبياء، الآية 21:107", "Quran 21:107"),
        ),
    )
}
