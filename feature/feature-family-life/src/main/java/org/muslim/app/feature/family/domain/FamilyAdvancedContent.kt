package org.muslim.app.feature.family.domain

/**
 * Phase-two expansion of the family guide.
 *
 * Kept separate from [FamilyLifeContent] so the reference catalog can grow
 * without turning the legacy migration file into one very large source file.
 */
object FamilyAdvancedContent {
    val articles: List<FamilyGuideArticle> = listOf(
        FamilyGuideArticle(
            id = "choosing_spouse",
            title = LocalizedFamilyText("اختيار شريك الحياة", "Choosing a spouse"),
            summary = LocalizedFamilyText(
                "معايير عملية تجمع الدين والخلق والمسؤولية والتوافق والقدرة على بناء بيت مستقر.",
                "Practical criteria combining faith, character, responsibility, compatibility and the ability to build a stable home.",
            ),
            sections = listOf(
                section(
                    "ما الذي يُبحث عنه؟",
                    "What should be considered?",
                    "لا يُبنى الاختيار على المظهر أو المال وحدهما. تُراجع الأخلاق، وتحمل المسؤولية، وطريقة التعامل مع الأهل والمال والغضب، والاستعداد للتعلم والتعاون.",
                    "Do not base the decision on appearance or wealth alone. Consider character, responsibility, treatment of family, money and anger, and willingness to learn and cooperate.",
                    "التوافق لا يعني التطابق؛ بل وضوح القيم الأساسية والقدرة على إدارة الاختلاف باحترام دون إكراه أو إهانة.",
                    "Compatibility does not mean being identical; it means clarity on core values and the ability to manage differences respectfully without coercion or humiliation.",
                ),
                section(
                    "السؤال والتحقق",
                    "Ask and verify",
                    "يُسأل عن المعلومات المهمة من مصادر موثوقة دون تجسس أو تشهير، وتُناقش القضايا المؤثرة في الحياة الزوجية قبل اتخاذ قرار نهائي.",
                    "Ask about important matters through trustworthy sources without spying or public shaming, and discuss issues that materially affect married life before making a final decision.",
                    "الاستخارة والدعاء يعينان على طلب الخير بعد بذل الأسباب، ولا يغنيان عن السؤال والفحص والاستشارة.",
                    "Istikhara and supplication accompany responsible due diligence; they do not replace questions, verification and consultation.",
                ),
            ),
            references = listOf(
                reference("المودة والرحمة", "Affection and mercy", "Quran 30:21"),
                reference("التعارف والتقوى", "Knowing one another and righteousness", "Quran 49:13"),
            ),
        ),
        FamilyGuideArticle(
            id = "premarital_conversations",
            title = LocalizedFamilyText("حوارات ما قبل الزواج", "Premarital conversations"),
            summary = LocalizedFamilyText(
                "موضوعات ينبغي مناقشتها بوضوح قبل العقد لتقليل المفاجآت وسوء التوقعات بعد الزواج.",
                "Topics worth discussing clearly before marriage to reduce surprises and mismatched expectations.",
            ),
            sections = listOf(
                section(
                    "أسئلة أساسية",
                    "Core conversations",
                    "يناقش الطرفان السكن والعمل والدراسة والإنفاق والديون والأبناء والعلاقة مع العائلتين ومكان الإقامة وخطط الانتقال أو السفر.",
                    "Discuss housing, work, education, spending, debt, children, relationships with both families, where to live and possible relocation or travel.",
                    "تُناقش التوقعات المتعلقة بالأدوار المنزلية والخصوصية واستخدام الأجهزة والتواصل وقت الخلاف، مع تجنب الوعود غير الواقعية.",
                    "Discuss household roles, privacy, device use and communication during conflict while avoiding unrealistic promises.",
                ),
                section(
                    "المعلومات المؤثرة",
                    "Material information",
                    "يجب تجنب الخداع في المعلومات التي تؤثر جوهريًا في قرار الزواج، ويُطلب توجيه مختص عند وجود مسألة صحية أو قانونية أو مالية معقدة.",
                    "Avoid deception about information that materially affects the marriage decision, and seek qualified guidance for complex health, legal or financial matters.",
                    "لا تُحوّل الأسئلة إلى تحقيق أو انتهاك للخصوصية؛ المطلوب قدر مناسب من الوضوح يسمح بقرار واعٍ وآمن.",
                    "Do not turn questions into interrogation or a privacy violation; the aim is enough clarity for an informed and safe decision.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "mahr_financial_agreements",
            title = LocalizedFamilyText("المهر والاتفاقات المالية", "Mahr and financial agreements"),
            summary = LocalizedFamilyText(
                "تنظيم المهر والنفقة والديون والاتفاقات المالية بوضوح يحفظ الحقوق ويقلل النزاع.",
                "Clear treatment of mahr, maintenance, debt and financial agreements protects rights and reduces conflict.",
            ),
            sections = listOf(
                section(
                    "المهر حق للزوجة",
                    "Mahr belongs to the wife",
                    "يُتفق على المهر بوضوح، وما كان منه معجلًا أو مؤجلًا، ويُوثق بما يمنع النزاع، مع مراعاة القانون المحلي.",
                    "Agree clearly on the mahr, including immediate and deferred portions, and document it in a way that reduces disputes while following local law.",
                    "لا يُعامل المهر كثمن للمرأة، ولا يجوز الاستيلاء عليه أو الضغط عليها للتنازل عنه بغير رضا معتبر.",
                    "Mahr is not a price placed on a woman, and it should not be taken from her or waived through coercion.",
                ),
                section(
                    "الديون والميزانية",
                    "Debt and budgeting",
                    "من الحكمة كشف الديون والالتزامات المؤثرة قبل الزواج، ثم وضع ميزانية واقعية تحدد المصروفات الأساسية والادخار والطوارئ.",
                    "It is prudent to disclose material debts and obligations before marriage and build a realistic budget for essentials, savings and emergencies.",
                    "القضايا المالية الكبيرة والعقود والملكية المشتركة قد تحتاج محاسبًا أو محاميًا بالإضافة إلى المشورة الشرعية.",
                    "Large financial arrangements, contracts and shared property may require an accountant or lawyer in addition to religious guidance.",
                ),
            ),
            references = listOf(reference("إيتاء الصدقات", "Giving bridal gifts", "Quran 4:4")),
        ),
        FamilyGuideArticle(
            id = "marriage_documentation",
            title = LocalizedFamilyText("توثيق الزواج والشروط", "Marriage documentation and conditions"),
            summary = LocalizedFamilyText(
                "التمييز بين صحة العقد الدينية والتوثيق القانوني، وكتابة الشروط المهمة بوضوح.",
                "Distinguishing religious validity from civil registration and recording important conditions clearly.",
            ),
            sections = listOf(
                section(
                    "التوثيق يحفظ الحقوق",
                    "Documentation protects rights",
                    "حتى مع اكتمال العقد الشرعي ينبغي مراعاة متطلبات التوثيق الرسمي في البلد لحماية الحقوق المتعلقة بالسكن والنفقة والأطفال والإجراءات الإدارية.",
                    "Even when a religious contract is complete, follow local civil registration requirements to protect rights involving housing, maintenance, children and administration.",
                    "تختلف قوانين الاعتراف بالزواج بين البلدان؛ لذلك لا يفترض التطبيق أن عقدًا دينيًا خاصًا يكفي قانونيًا في كل مكان.",
                    "Recognition of marriage differs by jurisdiction, so the app does not assume that a private religious contract is legally sufficient everywhere.",
                ),
                section(
                    "الشروط في العقد",
                    "Conditions in the contract",
                    "تُكتب الشروط المهمة بصياغة واضحة ومفهومة للطرفين، ويُراجع مدى صحتها شرعًا وقانونًا قبل التوقيع.",
                    "Write important conditions clearly in language both parties understand and verify their religious and legal validity before signing.",
                    "عند الشك في شرط يتعلق بالسكن أو العمل أو السفر أو الملكية أو الطلاق، تُطلب مراجعة عالم موثوق ومحامٍ محلي.",
                    "For uncertain conditions involving housing, work, travel, property or divorce, seek trusted scholarly and local legal review.",
                ),
            ),
            references = listOf(reference("الوفاء بالعقود", "Honouring contracts", "Quran 5:1")),
        ),
        FamilyGuideArticle(
            id = "marital_communication",
            title = LocalizedFamilyText("التواصل والمودة داخل البيت", "Communication and affection at home"),
            summary = LocalizedFamilyText(
                "مهارات يومية لبناء المودة والاحترام وإدارة الاحتياجات والاختلاف دون إهانة.",
                "Daily practices for affection, respect and handling needs and differences without humiliation.",
            ),
            sections = listOf(
                section(
                    "حديث واضح ومحترم",
                    "Clear and respectful communication",
                    "يُعبّر كل طرف عن احتياجاته بصيغة واضحة ومحددة، ويستمع قبل الرد، ويتجنب التعميم والتهديد والسخرية.",
                    "Express needs clearly and specifically, listen before responding, and avoid sweeping statements, threats and mockery.",
                    "الاعتذار الصادق وإصلاح الضرر من وسائل استعادة الثقة بعد الخطأ.",
                    "A sincere apology and repairing harm are practical ways to rebuild trust after mistakes.",
                ),
                section(
                    "الخصوصية والمودة",
                    "Privacy and affection",
                    "تُحفظ أسرار الحياة الزوجية، ولا يُستعمل كشفها للسخرية أو الانتقام أو كسب الأنصار في الخلاف.",
                    "Protect marital privacy and do not expose private matters for mockery, retaliation or to recruit allies during conflict.",
                    "المودة تُبنى بأفعال صغيرة مستمرة: الشكر، والوقت المشترك، واللطف، ومراعاة التعب والمرض والضغط.",
                    "Affection grows through small consistent acts: gratitude, shared time, kindness and consideration for fatigue, illness and stress.",
                ),
            ),
            references = listOf(
                reference("المودة والرحمة", "Affection and mercy", "Quran 30:21"),
                reference("المعاشرة بالمعروف", "Living together with kindness", "Quran 4:19"),
            ),
        ),
        FamilyGuideArticle(
            id = "household_finances",
            title = LocalizedFamilyText("إدارة مال الأسرة", "Managing family finances"),
            summary = LocalizedFamilyText(
                "ميزانية وشفافية مالية تحفظ الاحتياجات وتمنع استعمال المال كوسيلة ضغط.",
                "Budgeting and financial transparency that protect needs and avoid using money as a tool of pressure.",
            ),
            sections = listOf(
                section(
                    "الوضوح المالي",
                    "Financial clarity",
                    "تُحدد المصروفات الأساسية والديون والادخار والطوارئ بواقعية، ويُتفق على كيفية اتخاذ القرارات الكبيرة بدل إخفاء الالتزامات.",
                    "Define essentials, debts, savings and emergency spending realistically, and agree how major decisions are made rather than hiding obligations.",
                    "ملكية الأصول والالتزامات القانونية قد تختلف بحسب العقد والقانون؛ لا يُفترض اشتراك الملكية تلقائيًا.",
                    "Ownership and legal liabilities can vary by contract and jurisdiction; shared ownership should not be assumed automatically.",
                ),
                section(
                    "منع الإكراه المالي",
                    "Avoid financial coercion",
                    "لا يُستخدم منع الضروريات أو الاستيلاء على الأموال أو التهديد بالدين لإجبار الطرف الآخر أو عزله.",
                    "Do not withhold necessities, take money or weaponise debt to coerce or isolate the other spouse.",
                    "عند وجود نزاع مالي كبير تُحفظ المستندات ويُطلب توجيه قانوني وشرعي مستقل.",
                    "For serious financial disputes, preserve records and seek independent legal and religious guidance.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "mediation_reconciliation",
            title = LocalizedFamilyText("الوساطة والتحكيم الأسري", "Family mediation and reconciliation"),
            summary = LocalizedFamilyText(
                "متى تكون الوساطة مفيدة، وكيف يُختار الوسطاء وتُحفظ حدودهم وخصوصية الزوجين.",
                "When mediation helps, how to choose mediators and protect boundaries and marital privacy.",
            ),
            sections = listOf(
                section(
                    "اختيار الوسيط",
                    "Choosing a mediator",
                    "يُختار من عُرف بالحكمة والعدل والسرية وعدم تأجيج الخصومة، ويمكن أن يكون مستشارًا أسريًا مؤهلًا أو شخصين موثوقين من العائلتين.",
                    "Choose someone known for wisdom, fairness and confidentiality who will not inflame the dispute. This may be a qualified family counsellor or trusted representatives from both families.",
                    "يُحدد هدف الوساطة ومسائلها، ولا يُعطى الوسيط سلطة مطلقة على قرارات الزوجين أو أموالهم أو أسرارهم.",
                    "Define the scope and goal of mediation; a mediator should not gain unrestricted control over the couple's decisions, finances or private information.",
                ),
                section(
                    "متى لا تكفي الوساطة؟",
                    "When mediation is not enough",
                    "عند وجود عنف أو تهديد أو خوف حقيقي قد تكون جلسة مشتركة غير آمنة؛ تُقدّم السلامة والدعم المتخصص على المصالحة الشكلية.",
                    "Where violence, threats or genuine fear are present, joint mediation may be unsafe; safety and specialist support take priority over superficial reconciliation.",
                    "المسائل القانونية أو الخطر على الأطفال تحتاج جهات مختصة ولا تُختزل في جلسة عائلية.",
                    "Legal matters or risks to children require qualified authorities and should not be reduced to a family meeting.",
                ),
            ),
            references = listOf(reference("التحكيم عند الشقاق", "Arbitration in serious discord", "Quran 4:35")),
        ),
        FamilyGuideArticle(
            id = "abuse_safety",
            title = LocalizedFamilyText("السلامة عند الإساءة أو العنف", "Safety when abuse or violence occurs"),
            summary = LocalizedFamilyText(
                "إرشادات سلامة عامة عند وجود عنف أو تهديد أو سيطرة مؤذية، دون مطالبة المتضرر بالمواجهة المنفردة.",
                "General safety guidance for violence, threats or harmful control without asking a person at risk to confront the abuser alone.",
            ),
            sections = listOf(
                section(
                    "الأمان أولًا",
                    "Safety first",
                    "عند وجود خطر مباشر تُطلب المساعدة الطارئة المحلية ويُنتقل إلى مكان آمن إن أمكن. لا تنتظر جلسة إصلاح إذا كان التأخير يزيد الخطر.",
                    "In immediate danger, contact local emergency support and move to a safe place where possible. Do not wait for reconciliation if delay increases risk.",
                    "يمكن حفظ نسخ آمنة من الوثائق والأرقام المهمة وخطة تواصل مع شخص موثوق إذا كان ذلك لا يزيد الخطر.",
                    "Where doing so does not increase risk, keep safe copies of important documents and numbers and a communication plan with a trusted person.",
                ),
                section(
                    "الدعم المتخصص",
                    "Specialist support",
                    "الإساءة قد تكون جسدية أو جنسية أو نفسية أو مالية أو رقمية. تقييم الحالة يحتاج جهات حماية أو مختصين وليس حكمًا آليًا من التطبيق.",
                    "Abuse can be physical, sexual, psychological, financial or digital. Assessing a situation requires qualified support services, not an automated app judgment.",
                    "وجود أطفال أو حمل أو سلاح أو تهديد بالقتل أو ملاحقة قد يرفع مستوى الخطر ويستدعي استجابة عاجلة من الجهات المختصة.",
                    "Children, pregnancy, weapons, death threats or stalking can increase risk and may require urgent intervention from qualified services.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "divorce_general_principles",
            title = LocalizedFamilyText("مبادئ عامة في الطلاق", "General principles of divorce"),
            summary = LocalizedFamilyText(
                "مقدمة منظمة لفهم الطلاق والعدة والرجعة والحقوق دون إصدار حكم على واقعة شخصية.",
                "A structured introduction to divorce, waiting periods, revocation and rights without ruling on an individual case.",
            ),
            sections = listOf(
                section(
                    "لماذا لا توجد حاسبة طلاق؟",
                    "Why there is no divorce calculator",
                    "وقوع الطلاق ونوعه قد يتأثر باللفظ والنية والسياق وعدد الطلقات وأحكام القضاء والمذهب؛ لذلك لا يمكن اختزاله في أسئلة نعم أو لا داخل التطبيق.",
                    "Whether divorce occurred and what type it is may depend on wording, intention, context, prior pronouncements, courts and school of law, so it cannot safely be reduced to yes-or-no questions in an app.",
                    "إذا حدثت عبارة طلاق أو نزاع حولها، تُحفظ الكلمات والتاريخ والظروف بدقة ويُراجع عالم مؤهل والجهة القانونية المختصة.",
                    "If divorce words were used or disputed, preserve the wording, date and circumstances accurately and consult a qualified scholar and the relevant legal authority.",
                ),
                section(
                    "حفظ الحقوق",
                    "Protecting rights",
                    "لا تُستعمل ألفاظ الطلاق للتهديد المتكرر أو الضغط، وتُعالج مسائل السكن والنفقة والأطفال والوثائق بهدوء واستشارة موثوقة.",
                    "Divorce language should not be used repeatedly as a threat or pressure tactic. Housing, maintenance, children and documentation should be handled carefully with trusted guidance.",
                    "تختلف إجراءات التسجيل والاعتراف القضائي بين البلدان، وقد يلزم إجراء مدني مستقل حتى بعد معالجة الحكم الشرعي.",
                    "Registration and court recognition differ by country, and a separate civil process may be required even after the religious question is addressed.",
                ),
            ),
            references = listOf(
                reference("الطلاق بمعروف", "Divorce with good conduct", "Quran 2:229-231"),
                reference("أحكام العدة والإشهاد", "Waiting period and witnessing", "Quran 65:1-2"),
            ),
        ),
        FamilyGuideArticle(
            id = "khul_annulment",
            title = LocalizedFamilyText("الخلع والفسخ", "Khulʿ and annulment"),
            summary = LocalizedFamilyText(
                "تعريف عام بالخلع والفسخ والفرق بين المسارات، مع التأكيد أن التطبيق لا يحدد المسار المناسب لحالة فردية.",
                "A general explanation of khulʿ and annulment while emphasising that the app does not choose the appropriate route for an individual case.",
            ),
            sections = listOf(
                section(
                    "مسارات مختلفة",
                    "Different routes",
                    "الخلع والفسخ والطلاق ليست ألفاظًا مترادفة في الفقه، وتختلف آثارها وشروطها وإجراءاتها بين المذاهب والأنظمة القضائية.",
                    "Khulʿ, annulment and divorce are not interchangeable fiqh terms; their conditions, effects and procedures differ across schools and legal systems.",
                    "لا يُطلب من المستخدم تحديد المسار بنفسه من خلال التطبيق؛ بل يجمع المعلومات والوثائق ويسأل جهة مؤهلة.",
                    "The user should not self-select a legal or religious route through the app; gather relevant information and documents and consult a qualified authority.",
                ),
                section(
                    "الحقوق والاتفاق",
                    "Rights and agreement",
                    "قد ترتبط بعض صور الخلع بعوض أو إعادة جزء من المهر، بينما تختلف حالات الضرر والفسخ؛ لذلك يلزم تقييم الوقائع لا قاعدة آلية واحدة.",
                    "Some forms of khulʿ may involve compensation or return of mahr, while harm-based annulment is different; the facts need assessment rather than one automated rule.",
                    "تُراجع أي تسوية مالية أو اتفاق نهائي قبل التوقيع، خصوصًا عند وجود أطفال أو ديون أو أصول مشتركة.",
                    "Review any financial settlement or final agreement before signing, especially when children, debt or shared assets are involved.",
                ),
            ),
            references = listOf(reference("الخلع", "Khulʿ", "Quran 2:229")),
        ),
    )

    val metadata: List<FamilyTopicMetadata> = listOf(
        meta("choosing_spouse", FamilyTopicCategory.BeforeMarriage, "اختيار", "توافق", "spouse", "compatibility"),
        meta("premarital_conversations", FamilyTopicCategory.BeforeMarriage, "قبل الزواج", "ديون", "سكن", "premarital"),
        meta("mahr_financial_agreements", FamilyTopicCategory.Marriage, "مهر", "ديون", "ميزانية", "mahr"),
        meta("marriage_documentation", FamilyTopicCategory.Marriage, "توثيق", "شروط", "قانون", "documentation"),
        meta("marital_communication", FamilyTopicCategory.MaritalLife, "تواصل", "مودة", "خصوصية", "communication"),
        meta("household_finances", FamilyTopicCategory.MaritalLife, "مال", "ميزانية", "نفقة", "budget"),
        meta("mediation_reconciliation", FamilyTopicCategory.ConflictResolution, "وساطة", "تحكيم", "صلح", "mediation"),
        meta("abuse_safety", FamilyTopicCategory.ConflictResolution, "عنف", "اساءة", "سلامة", "abuse"),
        meta("divorce_general_principles", FamilyTopicCategory.Separation, "طلاق", "عدة", "رجعة", "divorce"),
        meta("khul_annulment", FamilyTopicCategory.Separation, "خلع", "فسخ", "عوض", "annulment"),
    )

    private fun section(
        titleAr: String,
        titleEn: String,
        firstAr: String,
        firstEn: String,
        secondAr: String,
        secondEn: String,
    ) = FamilyGuideSection(
        title = LocalizedFamilyText(titleAr, titleEn),
        paragraphs = listOf(
            LocalizedFamilyText(firstAr, firstEn),
            LocalizedFamilyText(secondAr, secondEn),
        ),
    )

    private fun reference(titleAr: String, titleEn: String, citation: String) =
        FamilyEvidenceReference(LocalizedFamilyText(titleAr, titleEn), citation)

    private fun meta(
        articleId: String,
        category: FamilyTopicCategory,
        vararg keywords: String,
    ) = FamilyTopicMetadata(articleId, category, keywords.toList())
}
