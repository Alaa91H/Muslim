package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object EthicsDailyLifeContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            ethicsFoundation,
            speechEthics,
            parentsFamily,
            neighboursCommunity,
            angerConflict,
            privacyBoundaries,
            workDigitalLife,
        )
    }

    private val ethicsFoundation = LearningLesson(
        id = "ethics_foundation",
        titleRes = R.string.learn_topic_ethics_foundation,
        subtitleRes = R.string.learn_topic_ethics_foundation_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "worship",
                title = "الأخلاق جزء من الدين",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "جمع القرآن بين العدل والإحسان وإيتاء ذي القربى والنهي عن الفحشاء والمنكر والبغي.",
                        referenceIds = listOf("quran_16_90"),
                    ),
                    LearningContentBlock.Paragraph(
                        "الأخلاق ليست مادة تجميلية منفصلة عن العبادة؛ الصدق والرحمة والعدل وحفظ الحقوق تظهر أثر الإيمان في التعامل مع الناس.",
                    ),
                ),
            ),
            LearningSection(
                id = "intention",
                title = "النية والسلوك",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "حسن القصد مهم، لكنه لا يلغي أثر السلوك على الآخرين. عند الخطأ اجمع بين تصحيح النية والاعتذار وإصلاح الضرر قدر الإمكان.",
                    ),
                ),
            ),
            LearningSection(
                id = "balance",
                title = "اللين لا يعني ترك الحدود",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("الرحمة", "التعامل بلطف وتفهم من غير إذلال أو تشهير."),
                            LearningComparisonItem("العدل", "إعطاء الحقوق وعدم تبرير الظلم بحجة التسامح."),
                            LearningComparisonItem("الحزم", "وضع حد واضح للأذى أو الاستغلال بطريقة منضبطة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "practice",
                title = "مراجعة يومية قصيرة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("قبل الكلام", "هل ما سأقوله صادق ونافع وبأسلوب مناسب؟"),
                            LearningStepItem("بعد الخلاف", "هل ظلمت أو بالغت أو نقلت كلامًا لا يلزم؟"),
                            LearningStepItem("قبل النوم", "اختر موقفًا واحدًا لتحسنه غدًا بدل جلد الذات أو تجاهل الخطأ."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_16_90", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النحل، الآية 16:90", "Quran 16:90"),
        ),
    )

    private val speechEthics = LearningLesson(
        id = "ethics_speech",
        titleRes = R.string.learn_topic_ethics_speech,
        subtitleRes = R.string.learn_topic_ethics_speech_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "truth",
                title = "القول السديد",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن المؤمنين بتقوى الله وقول قول سديد.",
                        referenceIds = listOf("quran_33_70"),
                    ),
                ),
            ),
            LearningSection(
                id = "mockery",
                title = "السخرية واللمز والتنابز",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "نهت سورة الحجرات عن السخرية واللمز والتنابز بالألقاب.",
                        referenceIds = listOf("quran_49_11"),
                    ),
                ),
            ),
            LearningSection(
                id = "suspicion_backbiting",
                title = "الظن والتجسس والغيبة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "نهى القرآن عن كثير من الظن والتجسس والغيبة في سياق واحد.",
                        referenceIds = listOf("quran_49_12"),
                    ),
                    LearningContentBlock.Callout(
                        body = "نقل مشكلة إلى مسؤول أو جهة حماية لرفع ضرر ليس هو نفسه التشهير للتسلية. السياق والضرورة والقدر المطلوب مهمة في مسائل الكلام عن الآخرين.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "online",
                title = "قبل الإرسال والنشر",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("تحقق", "لا تنشر خبرًا أو اتهامًا لمجرد أنه وصل من شخص تعرفه."),
                            LearningStepItem("اخفض الحرارة", "إذا كنت غاضبًا، اكتب الرد ثم راجعه قبل الإرسال."),
                            LearningStepItem("لا تعرض الخصوصيات", "صورة أو رسالة خاصة لا تصبح مباحة للنشر لمجرد أنك طرف فيها."),
                            LearningStepItem("صحح علنًا إن أخطأت علنًا", "إذا نشرت معلومة خاطئة فأظهر التصحيح بوضوح لمن وصلتهم."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_33_70", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الأحزاب، الآية 33:70", "Quran 33:70"),
            LearningReference("quran_49_11", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الحجرات، الآية 49:11", "Quran 49:11"),
            LearningReference("quran_49_12", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الحجرات، الآية 49:12", "Quran 49:12"),
        ),
    )

    private val parentsFamily = LearningLesson(
        id = "ethics_family",
        titleRes = R.string.learn_topic_ethics_family,
        subtitleRes = R.string.learn_topic_ethics_family_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "parents",
                title = "الوالدان",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "قرن القرآن الإحسان إلى الوالدين بالنهي عن التأفف والزجر والأمر بالقول الكريم.",
                        referenceIds = listOf("quran_17_23"),
                    ),
                ),
            ),
            LearningSection(
                id = "disagreement",
                title = "الإحسان مع الاختلاف",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "بر الوالدين لا يعني طاعة كل طلب مهما كان؛ قد توجد حدود شرعية أو قانونية أو صحية أو أمان شخصي. يمكن رفض الطلب مع بقاء الأدب وعدم الإهانة.",
                    ),
                ),
            ),
            LearningSection(
                id = "home",
                title = "الأخلاق داخل البيت",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الاحترام", "لا تجعل الألفة مبررًا لرفع الصوت أو الإهانة."),
                            LearningStepItem("المسؤولية", "شارك في ما تستطيع من خدمة البيت ولا تتعامل مع الآخرين كخدم."),
                            LearningStepItem("الخصوصية", "استأذن قبل مشاركة أسرار أو صور أو خلافات عائلية."),
                            LearningStepItem("الإصلاح", "عند الخطأ اعتذر بوضوح وأصلح الأثر بدل تبرير القسوة بالضغط."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "abuse",
                title = "عند وجود أذى أو عنف",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الصبر والبر لا يفرضان البقاء في خطر. إذا وُجد عنف أو تهديد أو استغلال فاطلب مساعدة آمنة من جهة موثوقة ومختصة، ثم اسأل أهل العلم عن الواجبات الدينية الخاصة بالحالة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_17_23", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الإسراء، الآية 17:23", "Quran 17:23"),
        ),
    )

    private val neighboursCommunity = LearningLesson(
        id = "ethics_neighbours",
        titleRes = R.string.learn_topic_ethics_neighbours,
        subtitleRes = R.string.learn_topic_ethics_neighbours_sub,
        estimatedMinutes = 13,
        sections = listOf(
            LearningSection(
                id = "neighbour",
                title = "حق الجار",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكر القرآن الإحسان إلى الجار ذي القربى والجار الجنب ضمن شبكة من الحقوق الاجتماعية.",
                        referenceIds = listOf("quran_4_36"),
                    ),
                ),
            ),
            LearningSection(
                id = "public",
                title = "المساحات المشتركة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الهدوء", "راع أوقات الراحة ولا تجعل حقك في منزلك ضررًا مستمرًا على غيرك."),
                            LearningStepItem("النظافة", "لا تلقِ العبء في الممرات أو الأماكن المشتركة على الآخرين."),
                            LearningStepItem("الالتزام", "احترم الأنظمة العامة والعقود ما لم تتضمن معصية أو ظلمًا يتطلب معالجة قانونية أو شرعية."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "difference",
                title = "العيش مع المختلف",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "حسن الجوار والعدل والتعاون في المعروف لا يشترط اتفاقًا في الدين أو الثقافة. يمكنك الحفاظ على هويتك واحترام الآخر في الوقت نفسه.",
                    ),
                ),
            ),
            LearningSection(
                id = "service",
                title = "من الأخلاق إلى الخدمة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "زيارة المريض، مساعدة المحتاج، الإصلاح بين الناس، وحماية الضعيف أمثلة على انتقال الأخلاق من شعور داخلي إلى أثر اجتماعي.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_36", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:36", "Quran 4:36"),
        ),
    )

    private val angerConflict = LearningLesson(
        id = "ethics_conflict",
        titleRes = R.string.learn_topic_ethics_conflict,
        subtitleRes = R.string.learn_topic_ethics_conflict_sub,
        estimatedMinutes = 16,
        sections = listOf(
            LearningSection(
                id = "anger",
                title = "إدارة الغضب",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "مدح القرآن الكاظمين الغيظ والعافين عن الناس.",
                        referenceIds = listOf("quran_3_134"),
                    ),
                ),
            ),
            LearningSection(
                id = "respond",
                title = "ادفع بالتي هي أحسن",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "وجّه القرآن إلى دفع السيئة بالتي هي أحسن في سياق إصلاح العداوة.",
                        referenceIds = listOf("quran_41_34"),
                    ),
                ),
            ),
            LearningSection(
                id = "workflow",
                title = "خطة خلاف عملية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("سمِّ المشكلة", "تكلم عن الفعل المحدد بدل وصف الشخص كله بالسوء."),
                            LearningStepItem("استمع", "افهم رواية الطرف الآخر قبل الرد على شيء لم يقله."),
                            LearningStepItem("اطلب حلًا قابلًا للتنفيذ", "حدد ما تريد أن يتغير بدل تكرار اللوم."),
                            LearningStepItem("أوقف الحوار عند التصعيد", "تأجيل النقاش أحيانًا أفضل من تحويله إلى إهانة أو تهديد."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "boundaries",
                title = "العفو والحقوق",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "العفو فضيلة في مواضعه، لكنه لا يمنع طلب الحق أو التوثيق أو الاستعانة بجهة مختصة عند الضرر. لا تستخدم النصوص الأخلاقية لإجبار المتضرر على إسقاط حقوقه.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_3_134", LearningReferenceKind.QURAN, "القرآن الكريم، سورة آل عمران، الآية 3:134", "Quran 3:134"),
            LearningReference("quran_41_34", LearningReferenceKind.QURAN, "القرآن الكريم، سورة فصلت، الآية 41:34", "Quran 41:34"),
        ),
    )

    private val privacyBoundaries = LearningLesson(
        id = "ethics_privacy",
        titleRes = R.string.learn_topic_ethics_privacy,
        subtitleRes = R.string.learn_topic_ethics_privacy_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "homes",
                title = "الاستئذان",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "نهى القرآن عن دخول بيوت غير بيوتكم حتى تستأنسوا وتسلموا على أهلها.",
                        referenceIds = listOf("quran_24_27"),
                    ),
                ),
            ),
            LearningSection(
                id = "secrets",
                title = "الأسرار والمحادثات",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الأمانة تشمل ما يأتمنك عليه الناس من تفاصيلهم وصورهم ومراسلاتهم. مشاركة لقطة شاشة أو تسجيل خاص قد تكون خيانة للثقة حتى إن كان الوصول التقني إليه ممكنًا.",
                    ),
                ),
            ),
            LearningSection(
                id = "consent",
                title = "الموافقة والحدود",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "اسأل قبل التصوير أو النشر أو مشاركة الموقع أو إعطاء رقم شخص لغيره. السكوت أو القرابة لا يعنيان موافقة دائمة على كشف الخصوصية.",
                    ),
                ),
            ),
            LearningSection(
                id = "safety",
                title = "الخصوصية ليست غطاءً للأذى",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "إذا تعلق الأمر بخطر حقيقي أو إساءة أو جريمة، قد تحتاج إلى مشاركة قدر لازم من المعلومات مع جهة قادرة على الحماية. الهدف تقليل الضرر لا نشر الأسرار على نطاق واسع.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_24_27", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النور، الآية 24:27", "Quran 24:27"),
        ),
    )

    private val workDigitalLife = LearningLesson(
        id = "ethics_work_digital",
        titleRes = R.string.learn_topic_ethics_work_digital,
        subtitleRes = R.string.learn_topic_ethics_work_digital_sub,
        estimatedMinutes = 16,
        sections = listOf(
            LearningSection(
                id = "contracts",
                title = "الالتزام بالعهود",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "افتتحت سورة المائدة بالأمر بالوفاء بالعقود.",
                        referenceIds = listOf("quran_5_1"),
                    ),
                ),
            ),
            LearningSection(
                id = "work",
                title = "الأمانة في العمل والدراسة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الوقت", "لا تسجل ساعات لم تعملها ولا تترك مسؤولية متفقًا عليها بلا إخطار."),
                            LearningStepItem("العمل المنسوب لك", "لا تقدم عمل شخص آخر أو محتوى مولدًا آليًا على أنه جهدك إذا كانت القواعد تتطلب الإفصاح."),
                            LearningStepItem("الممتلكات", "معدات المؤسسة وبياناتها وصلاحياتها أمانة تستخدم للغرض المسموح."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "digital",
                title = "الأخلاق الرقمية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الحسابات", "لا تدخل حساب غيرك أو تستخدم بياناته دون إذن."),
                            LearningStepItem("الصور والمحتوى", "احترم حقوق الناس وخصوصيتهم وحقوق النشر."),
                            LearningStepItem("المعلومات", "فرّق بين الحقيقة والرأي والإشاعة قبل إعادة النشر."),
                            LearningStepItem("الإدمان والانتباه", "اضبط الإشعارات والاستخدام إذا بدأت الشاشة تستهلك الصلاة أو النوم أو المسؤوليات."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "mistakes",
                title = "إذا أخطأت",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "احذف الضرر إن أمكن، صحح المعلومة، أعد الحق، واعتذر لمن تضرر. التوبة الأخلاقية ليست شعورًا داخليًا فقط عندما يكون للناس حق مباشر.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_5_1", LearningReferenceKind.QURAN, "القرآن الكريم، سورة المائدة، الآية 5:1", "Quran 5:1"),
        ),
    )
}
