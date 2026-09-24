package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FamilySocialLearningContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            familyIntroduction,
            spouseSelection,
            marriageContractMahr,
            maritalLife,
            parenting,
            kinship,
            conflictAndSeparation,
        )
    }

    private fun familyLink() = LearningFeatureLink(
        destination = LearningFeatureDestination.FAMILY_LIFE,
        titleRes = R.string.learn_feature_family_title,
        bodyRes = R.string.learn_feature_family_body,
        actionRes = R.string.learn_feature_family_action,
    )

    private val familyIntroduction = LearningLesson(
        id = "family_intro",
        titleRes = R.string.learn_topic_family_intro,
        subtitleRes = R.string.learn_topic_family_intro_sub,
        estimatedMinutes = 13,
        featureLink = familyLink(),
        sections = listOf(
            LearningSection(
                id = "purpose",
                title = "الأسرة مسؤولية وسكن",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكر القرآن السكن والمودة والرحمة في العلاقة الزوجية.",
                        referenceIds = listOf("quran_30_21"),
                    ),
                    LearningContentBlock.Paragraph(
                        "دراسة الأسرة لا تبدأ عند وقوع المشكلة؛ تبدأ بفهم الاختيار والحقوق والتواصل والمال والتربية والحدود قبل الزواج وأثناءه.",
                    ),
                ),
            ),
            LearningSection(
                id = "rights",
                title = "حقوق متبادلة لا سلطة مطلقة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "العلاقة الأسرية تتضمن حقوقًا وواجبات متبادلة، ولا يستخدم الدين لتبرير الإذلال أو العنف أو السيطرة المالية أو العزل عن المساعدة.",
                    ),
                ),
            ),
            LearningSection(
                id = "layers",
                title = "ثلاث طبقات يجب فصلها",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("الفقه", "أحكام النكاح والنفقة والطلاق والحضانة ونحوها."),
                            LearningComparisonItem("القانون", "التسجيل المدني والملكية والسكن والأطفال تختلف باختلاف البلد."),
                            LearningComparisonItem("المهارات", "التواصل والميزانية وحل النزاع والتربية تحتاج تدريبًا عمليًا، لا معرفة الحكم فقط."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "feature",
                title = "متى تستخدم قسم Family Life؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ميزة Family Life تحتوي أدلة تفصيلية وأدوات للأسرة، بما فيها ما قبل الزواج، التوثيق، التواصل، الميزانية، التربية، الوساطة والسلامة عند الإساءة. هذا المسار يعطيك الخريطة التعليمية ويرسلك إلى الأداة عند الحاجة.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_30_21", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الروم، الآية 30:21", "Quran 30:21"),
        ),
    )

    private val spouseSelection = LearningLesson(
        id = "family_spouse_selection",
        titleRes = R.string.learn_topic_family_spouse_selection,
        subtitleRes = R.string.learn_topic_family_spouse_selection_sub,
        estimatedMinutes = 16,
        featureLink = familyLink(),
        sections = listOf(
            LearningSection(
                id = "criteria",
                title = "ما الذي تبحث عنه؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الدين والخلق", "اسأل عن الالتزام والسلوك الفعلي لا الصورة الاجتماعية فقط."),
                            LearningStepItem("التوافق", "ناقش القيم ونمط الحياة والسكن والعمل والأطفال والعلاقة مع العائلتين."),
                            LearningStepItem("القدرة على الحوار", "راقب كيف يتعامل الطرف مع الاختلاف والاعتذار والحدود."),
                            LearningStepItem("الواقع المالي", "الديون والدخل والالتزامات موضوعات مهمة قبل العقد وليست عيبًا في السؤال."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "questions",
                title = "أسئلة قبل الزواج",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المحادثات المبكرة عن التوقعات المالية، الخصوصية، الدراسة والعمل، مكان السكن، الأطفال والصحة تقلل المفاجآت. لا تحتاج إلى كشف ما لا حق للطرف فيه، لكن لا تخفِ أمرًا مؤثرًا في قرار الزواج.",
                    ),
                ),
            ),
            LearningSection(
                id = "pressure",
                title = "القبول والضغط",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الموافقة على الزواج يجب أن تكون حقيقية. الضغط الأسري أو التهديد أو استغلال التبعية المالية لا يصنع علاقة صحية، وقد تكون له آثار شرعية وقانونية تحتاج جهة مختصة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "verification",
                title = "تحقق دون تجسس",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يمكن السؤال من أشخاص موثوقين والتحقق من معلومات مهمة، لكن لا تجعل البحث ذريعة لاختراق الحسابات أو انتهاك الخصوصية أو نشر الشائعات.",
                    ),
                ),
            ),
        ),
    )

    private val marriageContractMahr = LearningLesson(
        id = "family_marriage_contract",
        titleRes = R.string.learn_topic_family_marriage_contract,
        subtitleRes = R.string.learn_topic_family_marriage_contract_sub,
        estimatedMinutes = 17,
        featureLink = familyLink(),
        sections = listOf(
            LearningSection(
                id = "contract",
                title = "العقد التزام وليس حفلًا",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "النكاح عقد له أركان وشروط وتفاصيل فقهية وقانونية. لا يعتمد التطبيق على قائمة قصيرة ليقرر صحة عقد فردي؛ بل يعلمك ما الذي يحتاج إلى توثيق وسؤال.",
                    ),
                ),
            ),
            LearningSection(
                id = "mahr",
                title = "المهر",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بإيتاء النساء صدقاتهن نحلة.",
                        referenceIds = listOf("quran_4_4"),
                    ),
                    LearningContentBlock.Paragraph(
                        "حدد المهر ومقدمه ومؤخره وعملته وطريقة السداد بوضوح. الغموض في الالتزامات المالية مصدر نزاع يمكن تجنبه بالتوثيق.",
                    ),
                ),
            ),
            LearningSection(
                id = "conditions",
                title = "الشروط والتوثيق",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اكتب ما اتفق عليه", "السكن والعمل والدراسة والالتزامات المالية إن كانت شروطًا متفقًا عليها."),
                            LearningStepItem("راجع القانون", "العقد الديني لا يغني دائمًا عن التسجيل المدني وحماية الحقوق القانونية."),
                            LearningStepItem("لا توقع ما لا تفهم", "اطلب شرحًا مستقلًا قبل قبول التزام طويل الأثر."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "disputes",
                title = "إذا وقع خلاف حول العقد",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "احفظ نسخة من العقد والمراسلات والإيصالات، ولا تعتمد على ذاكرة الأطراف وحدها. النزاع الجاد يحتاج مراجعة شرعية وقانونية مستقلة.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_4", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:4", "Quran 4:4"),
        ),
    )

    private val maritalLife = LearningLesson(
        id = "family_marital_life",
        titleRes = R.string.learn_topic_family_marital_life,
        subtitleRes = R.string.learn_topic_family_marital_life_sub,
        estimatedMinutes = 18,
        featureLink = familyLink(),
        sections = listOf(
            LearningSection(
                id = "communication",
                title = "التواصل اليومي",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اطلب بوضوح", "لا تجعل الطرف الآخر يخمن ما تريد ثم تحاسبه على التخمين."),
                            LearningStepItem("افصل المشكلة عن الشخص", "ناقش السلوك أو القرار بدل الإهانة الشاملة."),
                            LearningStepItem("اختر الوقت", "ليس كل خلاف مناسبًا للنقاش أثناء الغضب أو أمام الأطفال."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "money",
                title = "المال داخل الأسرة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الميزانية والديون والحسابات المشتركة والمصروف الشخصي تحتاج اتفاقًا واضحًا. لا تستخدم المال للعقاب أو التهديد أو إخفاء التزامات كبيرة تؤثر في الأسرة.",
                    ),
                ),
            ),
            LearningSection(
                id = "privacy",
                title = "خصوصية الزواج",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الخلاف الزوجي لا يصبح مادة عائلية أو اجتماعية لمجرد الغضب. شارك القدر الذي تحتاجه مع وسيط أو مختص، وحافظ على التفاصيل الحميمة والأسرار.",
                    ),
                ),
            ),
            LearningSection(
                id = "kindness",
                title = "المعروف",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بمعاشرة الزوجات بالمعروف.",
                        referenceIds = listOf("quran_4_19"),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_19", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:19", "Quran 4:19"),
        ),
    )

    private val parenting = LearningLesson(
        id = "family_parenting",
        titleRes = R.string.learn_topic_family_parenting,
        subtitleRes = R.string.learn_topic_family_parenting_sub,
        estimatedMinutes = 17,
        featureLink = familyLink(),
        sections = listOf(
            LearningSection(
                id = "example",
                title = "التربية بالقدوة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الطفل يتعلم من طريقة البيت في الكلام والاعتذار والصلاة واستخدام الهاتف أكثر مما يتعلم من المحاضرات وحدها.",
                    ),
                ),
            ),
            LearningSection(
                id = "age",
                title = "راع العمر والقدرة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "لا يُشرح لطفل صغير بالطريقة نفسها التي يُشرح بها لمراهق. قسم Family Life يحتوي مواد تربوية أوسع؛ استخدمها مع مراعاة الصحة والتطور والاحتياجات الخاصة.",
                    ),
                ),
            ),
            LearningSection(
                id = "discipline",
                title = "الانضباط بلا إذلال",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "التربية لا تبرر الإهانة أو التخويف المستمر أو العنف المؤذي. ضع قواعد واضحة وعواقب مناسبة للعمر، واطلب دعمًا متخصصًا إذا أصبح السلوك أو الضغط خارج السيطرة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "faith",
                title = "الدين في البيت",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اجعل العبادة مرئية", "صلِّ واقرأ واذكر الله بصورة طبيعية في البيت."),
                            LearningStepItem("اسمح بالأسئلة", "لا تعاقب الطفل على السؤال الصادق أو عدم الفهم."),
                            LearningStepItem("اربط الحكم بالمعنى", "اشرح الرحمة والأمانة والعبادة بدل الاكتفاء بالأوامر."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val kinship = LearningLesson(
        id = "family_kinship",
        titleRes = R.string.learn_topic_family_kinship,
        subtitleRes = R.string.learn_topic_family_kinship_sub,
        estimatedMinutes = 14,
        featureLink = familyLink(),
        sections = listOf(
            LearningSection(
                id = "ties",
                title = "صلة الرحم",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "صلة الأقارب تكون بالزيارة والاتصال والمساعدة والكلمة الطيبة بحسب القدرة والحاجة، وليست قالبًا واحدًا يناسب كل الأسر.",
                    ),
                ),
            ),
            LearningSection(
                id = "boundaries",
                title = "صلة مع حدود",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يمكن الحفاظ على أصل الصلة مع تقليل الاحتكاك أو تنظيم التواصل إذا كان القرب يؤدي إلى أذى أو تدخل مستمر. الحالات الخطرة تحتاج خطة سلامة لا ضغطًا اجتماعيًا.",
                    ),
                ),
            ),
            LearningSection(
                id = "parents",
                title = "الوالدان بعد الزواج",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الزواج لا يلغي بر الوالدين، وبر الوالدين لا يعطي أحدًا سلطة مطلقة على قرارات الزوجين الخاصة. التوازن يحتاج احترامًا وحدودًا واتفاقًا بين الزوجين.",
                    ),
                ),
            ),
            LearningSection(
                id = "care",
                title = "الرعاية عند المرض والكبر",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("وزع المسؤوليات", "لا تترك الرعاية على شخص واحد إذا كان يمكن للأسرة تقاسمها."),
                            LearningStepItem("احترم كرامة المحتاج", "المساعدة لا تسلب الشخص حقه في القرار قدر استطاعته."),
                            LearningStepItem("استعن بالمختصين", "الرعاية الطبية والنفسية والقانونية ليست بديلًا عن البر بل من وسائله."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val conflictAndSeparation = LearningLesson(
        id = "family_conflict_separation",
        titleRes = R.string.learn_topic_family_conflict_separation,
        subtitleRes = R.string.learn_topic_family_conflict_separation_sub,
        estimatedMinutes = 20,
        featureLink = familyLink(),
        sections = listOf(
            LearningSection(
                id = "mediation",
                title = "الإصلاح والوساطة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ذكر القرآن بعث حكم من أهله وحكم من أهلها عند خوف الشقاق.",
                        referenceIds = listOf("quran_4_35"),
                    ),
                    LearningContentBlock.Paragraph(
                        "الوسيط الجيد يحفظ السر ويحدد موضوع الخلاف ولا يفرض نفسه صاحب قرار على الزوجين.",
                    ),
                ),
            ),
            LearningSection(
                id = "safety",
                title = "عند العنف أو التهديد",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "السلامة مقدمة على جلسة صلح مشتركة. العنف الجسدي أو الجنسي أو النفسي أو المالي أو الرقمي يحتاج تقييمًا من جهة مختصة، وقد لا تكون الوساطة المباشرة آمنة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "divorce",
                title = "الطلاق والخلع والفسخ",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "هذه مسارات فقهية وقانونية مختلفة، وتتأثر باللفظ والنية والسياق والتوثيق والبلد والمذهب. لا يقدم التطبيق حاسبة تقرر وقوع الطلاق أو نوعه.",
                    ),
                ),
            ),
            LearningSection(
                id = "records",
                title = "احفظ الوقائع والحقوق",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("دوّن الألفاظ والتواريخ", "خصوصًا عند نزاع حول الطلاق أو اتفاق مالي."),
                            LearningStepItem("احفظ المستندات", "العقود والإيصالات والمراسلات المتعلقة بالسكن والمال والأطفال."),
                            LearningStepItem("اطلب رأيين مستقلين عند الحاجة", "الجانب الشرعي والجانب القانوني قد يحتاجان مختصين مختلفين."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_35", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:35", "Quran 4:35"),
        ),
    )
}
