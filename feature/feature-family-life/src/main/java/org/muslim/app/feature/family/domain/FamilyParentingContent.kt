package org.muslim.app.feature.family.domain

/**
 * Phase-three reference content for newborn care, parenting and ruqyah.
 *
 * Medical, safeguarding and legal statements stay deliberately general and
 * direct users to qualified local professionals where individual assessment is
 * needed. Fiqh material is educational and avoids automated case rulings.
 */
object FamilyParentingContent {
    val ruqyahSupplications: List<RuqyahSupplication> = listOf(
        RuqyahSupplication(
            id = "remove_harm",
            title = LocalizedFamilyText("دعاء طلب الشفاء", "Supplication for healing"),
            arabic = "أَذْهِبِ الْبَأْسَ رَبَّ النَّاسِ، اشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            meaning = LocalizedFamilyText(
                "اللهم أزل الضر واشفِ شفاءً تامًا، فالشفاء منك وحدك.",
                "O Lord of mankind, remove the harm and grant complete healing; You alone are the Healer.",
            ),
            reference = LocalizedFamilyText("صحيح البخاري وصحيح مسلم", "Sahih al-Bukhari and Sahih Muslim"),
        ),
        RuqyahSupplication(
            id = "bismillah_ruqyah",
            title = LocalizedFamilyText("بسم الله أرقيك", "In the name of Allah I perform ruqyah for you"),
            arabic = "بِسْمِ اللَّهِ أَرْقِيكَ، مِنْ كُلِّ شَيْءٍ يُؤْذِيكَ، مِنْ شَرِّ كُلِّ نَفْسٍ أَوْ عَيْنِ حَاسِدٍ، اللَّهُ يَشْفِيكَ، بِسْمِ اللَّهِ أَرْقِيكَ",
            meaning = LocalizedFamilyText(
                "رقية بالدعاء إلى الله من كل أذى، مع إسناد الشفاء إليه وحده.",
                "A supplication seeking Allah's protection from harm while attributing healing to Him alone.",
            ),
            reference = LocalizedFamilyText("صحيح مسلم", "Sahih Muslim"),
        ),
        RuqyahSupplication(
            id = "perfect_words",
            title = LocalizedFamilyText("الاستعاذة بكلمات الله التامات", "Seeking refuge in Allah's perfect words"),
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            meaning = LocalizedFamilyText(
                "أستعيذ بكلمات الله التامات من شر مخلوقاته.",
                "I seek refuge in Allah's perfect words from the evil of what He created.",
            ),
            reference = LocalizedFamilyText("صحيح مسلم", "Sahih Muslim"),
        ),
        RuqyahSupplication(
            id = "pain_supplication",
            title = LocalizedFamilyText("دعاء موضع الألم", "Supplication for a painful area"),
            arabic = "بِسْمِ اللَّهِ، بِسْمِ اللَّهِ، بِسْمِ اللَّهِ. أَعُوذُ بِعِزَّةِ اللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ",
            meaning = LocalizedFamilyText(
                "يضع المسلم يده على موضع الألم إن أمكن ويقول «بسم الله» ثلاثًا ثم يدعو بالاستعاذة بعزة الله وقدرته سبع مرات.",
                "Where appropriate, place the hand on the painful area, say “Bismillah” three times, then seek refuge in Allah's might and power seven times.",
            ),
            reference = LocalizedFamilyText("صحيح مسلم", "Sahih Muslim"),
        ),
        RuqyahSupplication(
            id = "children_protection",
            title = LocalizedFamilyText("تعويذ الأطفال", "Supplication for protecting children"),
            arabic = "أُعِيذُكُمَا بِكَلِمَاتِ اللَّهِ التَّامَّةِ، مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ",
            meaning = LocalizedFamilyText(
                "دعاء بالحفظ للأطفال بكلمات الله التامة من الشرور.",
                "A supplication asking Allah to protect children through His perfect words from harm.",
            ),
            reference = LocalizedFamilyText("صحيح البخاري", "Sahih al-Bukhari"),
        ),
    )

    val articles: List<FamilyGuideArticle> = listOf(
        FamilyGuideArticle(
            id = "pregnancy_preparation",
            title = LocalizedFamilyText("الاستعداد للحمل والولادة", "Preparing for pregnancy and birth"),
            summary = LocalizedFamilyText(
                "استعداد أسري يجمع الدعاء والرعاية الصحية والتخطيط العملي دون تحميل الأم أو الأب توقعات غير واقعية.",
                "Family preparation combining supplication, health care and practical planning without unrealistic expectations.",
            ),
            sections = listOf(
                section(
                    "الرعاية والصحة",
                    "Health and care",
                    "تُراجع الرعاية الطبية المعتادة والفحوصات الموصى بها مع مختصين، وتُناقش الأدوية والمكملات والحالات المزمنة معهم بدل الاعتماد على نصائح عامة.",
                    "Routine medical care and recommended checks should be discussed with qualified professionals, including medicines, supplements and chronic conditions rather than relying on generic advice.",
                    "الدعاء والذكر لا يتعارضان مع الاستفادة من الطب، ولا ينبغي تأخير تقييم الأعراض المقلقة بسبب انتظار علاج غير طبي.",
                    "Supplication and remembrance are compatible with medical care; concerning symptoms should not be left unevaluated while waiting for non-medical remedies.",
                ),
                section(
                    "استعداد البيت",
                    "Preparing the home",
                    "تُرتب الاحتياجات الأساسية للمولود والميزانية والإجازات والدعم بعد الولادة، مع تجنب الإنفاق التنافسي أو شراء ما لا تدعو إليه حاجة.",
                    "Plan essentials, budget, leave and post-birth support while avoiding competitive spending or unnecessary purchases.",
                    "من المفيد الاتفاق مسبقًا على من يساعد في الأيام الأولى وحدود الزيارات ووقت الراحة والخصوصية.",
                    "It helps to agree in advance who will help in the first days, visitor boundaries, rest and privacy.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "postpartum_family_support",
            title = LocalizedFamilyText("فترة النفاس ودعم الأسرة", "Postpartum period and family support"),
            summary = LocalizedFamilyText(
                "تقديم الراحة والرعاية للأم والطفل وتقاسم المسؤوليات وملاحظة مؤشرات الحاجة إلى دعم صحي عاجل.",
                "Supporting mother and baby through rest, shared responsibilities and awareness of signs that need urgent professional care.",
            ),
            sections = listOf(
                section(
                    "التعافي والرحمة",
                    "Recovery and kindness",
                    "فترة ما بعد الولادة تحتاج إلى راحة وتغذية مناسبة ومتابعة صحية. يُراعى تعب الأم وتُوزع أعمال البيت والزوار بما يحفظ تعافيها وخصوصيتها.",
                    "The postpartum period requires rest, appropriate nutrition and health follow-up. Household tasks and visitors should be managed in a way that supports recovery and privacy.",
                    "تغير المزاج والتعب شائعان، لكن الأعراض الشديدة أو المستمرة أو الأفكار المؤذية للنفس أو الطفل تستلزم مساعدة مهنية عاجلة.",
                    "Mood changes and fatigue can occur, but severe or persistent symptoms or thoughts of harm to self or baby require urgent professional help.",
                ),
                section(
                    "دور الأسرة",
                    "The family's role",
                    "الدعم العملي مثل إعداد الطعام والعناية بالمنزل وحماية وقت النوم أنفع من كثرة النصائح واللوم.",
                    "Practical support such as meals, household help and protecting sleep is often more useful than repeated advice or blame.",
                    "لا ينبغي الضغط على الأم لإخفاء الألم أو الإرهاق أو مشاعرها خوفًا من الحكم عليها.",
                    "A mother should not be pressured to hide pain, exhaustion or emotions out of fear of judgment.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "newborn_sunnahs_evidence",
            title = LocalizedFamilyText("سنن المولود وما يثبت منها", "Newborn practices and their evidence"),
            summary = LocalizedFamilyText(
                "تمييز الممارسات المشهورة المتعلقة بالمولود، مع إظهار ما يحتاج إلى مراجعة علمية عند اختلاف قوة الدليل.",
                "Distinguishing common newborn practices while flagging matters whose evidence or application may need scholarly review.",
            ),
            sections = listOf(
                section(
                    "التسمية والتحنيك",
                    "Naming and tahnik",
                    "ثبتت التسمية والتحنيك في السنة، مع مراعاة سلامة الطفل والنظافة وعدم تطبيق أي ممارسة بطريقة قد تسبب الاختناق أو الأذى.",
                    "Naming and tahnik are known from the Sunnah, while the baby's safety, hygiene and choking risk must always be considered.",
                    "لا تُستخدم الممارسات الدينية ذريعة لتجاوز تعليمات السلامة الطبية للمولود.",
                    "Religious practice should never be used to bypass newborn safety guidance.",
                ),
                section(
                    "مسائل يكثر السؤال عنها",
                    "Common questions",
                    "بعض التفاصيل المتعلقة بالأذان في أذن المولود أو توقيت حلق الشعر ووزنه أو أحكام أخرى قد يقع فيها خلاف في ثبوت الحديث أو التطبيق؛ فيُذكر الخلاف دون جزم غير لازم.",
                    "Some details such as the adhan in a newborn's ear, timing and valuation of shaved hair, or related practices involve differences over evidence or application; present such differences without unwarranted certainty.",
                    "الأصل تجنب العادات التي تتضمن إيذاء الطفل أو مواد غير مأمونة ولو شاع استعمالها.",
                    "Avoid customs that expose the baby to harm or unsafe substances even if they are culturally common.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "breastfeeding_child_care",
            title = LocalizedFamilyText("الرضاعة ورعاية الرضيع", "Breastfeeding and infant care"),
            summary = LocalizedFamilyText(
                "رعاية متوازنة تراعي مصلحة الطفل وصحة الأم وتمنع تحويل الرضاعة إلى مجال للوم أو الإكراه.",
                "Balanced care that protects the baby's needs and the mother's health without turning feeding into blame or coercion.",
            ),
            sections = listOf(
                section(
                    "الرضاعة والتغذية",
                    "Feeding",
                    "الرضاعة الطبيعية لها فوائد معروفة، لكن القدرة والظروف الصحية تختلف. تُطلب مساعدة مختصة عند الألم أو ضعف النمو أو صعوبة الرضاعة.",
                    "Breastfeeding has recognised benefits, but circumstances and health vary. Seek qualified support for pain, poor growth or feeding difficulty.",
                    "عندما تكون هناك حاجة إلى بدائل تغذية آمنة فلا ينبغي وصم الأسرة أو تعريض الطفل للخطر بسبب ضغط اجتماعي.",
                    "When safe alternative feeding is needed, families should not be shamed or a baby put at risk because of social pressure.",
                ),
                section(
                    "السلامة اليومية",
                    "Daily safety",
                    "تُتبع إرشادات النوم الآمن ومقاعد السيارة والحمام والأدوية بحسب الجهات الصحية المختصة في البلد.",
                    "Follow local professional guidance for safe sleep, car seats, bathing and medicines.",
                    "أي صعوبة تنفس أو خمول شديد أو جفاف أو حرارة مقلقة عند الرضيع تستدعي تقييمًا طبيًا سريعًا.",
                    "Breathing difficulty, marked lethargy, dehydration or concerning fever in an infant needs prompt medical assessment.",
                ),
            ),
            references = listOf(reference("الرضاعة", "Breastfeeding", "Quran 2:233")),
        ),
        FamilyGuideArticle(
            id = "aqiqah_complete_guide",
            title = LocalizedFamilyText("دليل العقيقة العملي", "Practical aqiqah guide"),
            summary = LocalizedFamilyText(
                "تخطيط العقيقة ومواعيدها والذبح والتوزيع بصورة تعليمية تراعي الاستطاعة والقانون المحلي.",
                "Planning aqiqah dates, slaughter and distribution as general education while respecting means and local law.",
            ),
            sections = listOf(
                section(
                    "الموعد والاستطاعة",
                    "Timing and ability",
                    "اليوم السابع هو الموعد الأشهر في السنة، وتذكر بعض كتب الفقه الرابع عشر والحادي والعشرين عند الفوات. التطبيق يعرض هذه المواعيد للتخطيط ولا يجعلها حكمًا ملزمًا.",
                    "The seventh day is the best-known Sunnah timing, while some fiqh discussions mention the fourteenth and twenty-first if it is missed. The app shows them for planning, not as a binding ruling.",
                    "العقيقة مرتبطة بالاستطاعة، ولا ينبغي الدخول في دين أو حرج مالي من أجل مظهر اجتماعي.",
                    "Aqiqah is connected to ability; families should not take on debt or hardship for social display.",
                ),
                section(
                    "الذبح والتوزيع",
                    "Slaughter and distribution",
                    "تراعى أحكام الذبح الشرعي والرفق بالحيوان والقوانين المحلية، ويجوز تنظيم التنفيذ عبر جهة موثوقة عند الحاجة.",
                    "Observe lawful slaughter requirements, animal welfare and local law, using a trusted service where needed.",
                    "يمكن الانتفاع باللحم وإهداؤه والتصدق منه بحسب ما يراه أهل العلم في المذهب والبلد، دون تحويل الأمر إلى منافسة أو إسراف.",
                    "Meat may be used, gifted and shared in charity according to scholarly guidance and local practice, without extravagance or competition.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "choosing_child_name",
            title = LocalizedFamilyText("اختيار اسم المولود", "Choosing a child's name"),
            summary = LocalizedFamilyText(
                "اختيار اسم حسن المعنى مع التحقق من نطقه وكتابته وآثاره القانونية والثقافية في البلد.",
                "Choosing a name with a good meaning while checking pronunciation, spelling and practical legal or cultural implications.",
            ),
            sections = listOf(
                section(
                    "المعنى قبل الشهرة",
                    "Meaning before popularity",
                    "يُختار الاسم لمعناه الحسن وسلامته من المعاني القبيحة أو التعظيم غير المشروع، ولا يكفي أن يكون رائجًا أو مرتبطًا بشخصية مشهورة.",
                    "Choose a name for a sound meaning and avoid degrading or improper meanings; popularity or celebrity association is not enough.",
                    "قد تتغير دلالة الاسم بين اللغات، لذلك يستحسن سؤال أهل اللغة عند الأسماء غير المألوفة أو المنقولة من ثقافات أخرى.",
                    "A name's meaning can change across languages, so consult knowledgeable speakers when a name is unusual or borrowed from another culture.",
                ),
                section(
                    "الكتابة الرسمية",
                    "Official spelling",
                    "يفيد الاتفاق على كتابة لاتينية مستقرة للاسم قبل التسجيل الرسمي لتقليل اختلاف الوثائق لاحقًا.",
                    "Agreeing on a consistent Latin-script spelling before registration can reduce document mismatches later.",
                    "قاعدة الأسماء في التطبيق مرجع لغوي مختصر وليست شهادة شرعية بصلاحية كل اسم في كل سياق.",
                    "The app's name catalogue is a language aid, not a religious certification of every name in every context.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "parenting_early_years",
            title = LocalizedFamilyText("التربية من الولادة إلى ست سنوات", "Parenting from birth to age six"),
            summary = LocalizedFamilyText(
                "بناء الأمان والتعلق واللغة والعادات الأولى بالقدوة والرحمة والحدود البسيطة.",
                "Building security, attachment, language and early habits through example, kindness and simple boundaries.",
            ),
            sections = listOf(
                section(
                    "الأمان والارتباط",
                    "Security and attachment",
                    "يحتاج الطفل في سنواته الأولى إلى الاستجابة والحنان والروتين المتوقع أكثر من كثرة الأوامر والعقوبات.",
                    "In the early years, responsive care, affection and predictable routines matter more than frequent commands or punishment.",
                    "يُعلّم الطفل كلمات المشاعر والطلب والاستئذان تدريجيًا، ويُعطى خيارات بسيطة تناسب عمره.",
                    "Gradually teach words for feelings, asking and permission, and offer simple age-appropriate choices.",
                ),
                section(
                    "بداية العبادة",
                    "Introducing worship",
                    "يتعرف الطفل إلى الصلاة والقرآن بالدخول اللطيف في أجواء البيت والقدوة والقصص القصيرة، دون تكليف يفوق عمره.",
                    "Introduce prayer and Quran through a gentle home environment, example and short stories without expectations beyond the child's age.",
                    "الهدف في هذه المرحلة تكوين الألفة والمحبة والعادة لا اختبار الطفل أو مقارنته بغيره.",
                    "The aim at this stage is familiarity, affection and habit, not testing the child or comparing them with others.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "parenting_school_age",
            title = LocalizedFamilyText("التربية في سن المدرسة", "Parenting during school age"),
            summary = LocalizedFamilyText(
                "المسؤولية والعادات الدراسية والصلاة والصداقة والإنترنت بحدود واضحة تناسب العمر.",
                "Responsibility, study habits, prayer, friendships and internet use with clear age-appropriate boundaries.",
            ),
            sections = listOf(
                section(
                    "المسؤولية التدريجية",
                    "Growing responsibility",
                    "يُعطى الطفل مهام منزلية ودراسية مناسبة لعمره، وتُراجع معه النتائج بدل إنجاز كل شيء عنه أو تركه دون متابعة.",
                    "Give age-appropriate home and school responsibilities and review outcomes rather than doing everything for the child or leaving them entirely unsupported.",
                    "يُمدح الجهد والصدق وتحمل المسؤولية، وليس الدرجة أو الفوز فقط.",
                    "Praise effort, honesty and responsibility rather than grades or winning alone.",
                ),
                section(
                    "المدرسة والأصدقاء",
                    "School and friends",
                    "يُعرف الوالدان بيئة المدرسة والأصدقاء دون تجسس مهين، ويُفتح حوار ثابت عن التنمر والضغط الجماعي والمحتوى الذي يراه الطفل.",
                    "Parents should understand school and friendship environments without humiliating surveillance, with regular conversation about bullying, peer pressure and content exposure.",
                    "عند ظهور تغيرات حادة في السلوك أو الخوف من المدرسة أو إيذاء النفس تُطلب مساعدة مختصة مبكرًا.",
                    "Marked behaviour changes, fear of school or self-harm concerns should prompt early professional support.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "parenting_teens",
            title = LocalizedFamilyText("تربية المراهقين", "Parenting teenagers"),
            summary = LocalizedFamilyText(
                "الانتقال من التحكم المباشر إلى الحوار والمسؤولية المتدرجة مع بقاء الحدود والحماية.",
                "Moving from direct control toward dialogue and growing responsibility while keeping boundaries and safeguarding.",
            ),
            sections = listOf(
                section(
                    "الاستقلال مع المسؤولية",
                    "Independence with responsibility",
                    "يحتاج المراهق مساحة لاتخاذ قرارات تناسب عمره وتحمل نتائجها، مع حدود واضحة في السلامة والمال والوقت والدراسة.",
                    "Teenagers need room for age-appropriate decisions and consequences, with clear limits around safety, money, time and education.",
                    "التحكم الكامل والرقابة السرية الدائمة قد يدفعان إلى الإخفاء؛ الأفضل قواعد معلنة ومراجعة دورية للثقة.",
                    "Total control and constant covert monitoring can encourage secrecy; clear rules and periodic review of trust are usually healthier.",
                ),
                section(
                    "الحوار في القيم",
                    "Values and difficult conversations",
                    "تُناقش أسئلة العقيدة والعلاقات والجسد والهوية والمستقبل بلغة هادئة تسمح بالسؤال دون سخرية أو تخويف.",
                    "Discuss faith, relationships, the body, identity and the future calmly so questions can be asked without ridicule or intimidation.",
                    "عند موضوعات الصحة النفسية أو الإدمان أو العنف أو الاستغلال يُستعان بمختصين ولا تُكتفى الموعظة وحدها.",
                    "For mental-health concerns, addiction, violence or exploitation, involve qualified professionals rather than relying on advice alone.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "children_prayer_quran",
            title = LocalizedFamilyText("تعليم الصلاة والقرآن للأبناء", "Teaching children prayer and Quran"),
            summary = LocalizedFamilyText(
                "منهج تدريجي يربط الصلاة والقرآن بالقدوة والفهم والرفق والاستمرار.",
                "A gradual approach connecting prayer and Quran to example, understanding, gentleness and consistency.",
            ),
            sections = listOf(
                section(
                    "القدوة أولًا",
                    "Example comes first",
                    "رؤية الصلاة في وقتها وهدوء البيت عند العبادة أقوى من كثرة التوبيخ، ويُشرح معنى ما يفعله الطفل بقدر فهمه.",
                    "Seeing prayer practised consistently and calmly can be more powerful than repeated scolding; explain what the child is doing at an age-appropriate level.",
                    "تُجزأ المهارات: الوضوء، الحركات، السور القصيرة، ثم الاستقلال تدريجيًا بدل طلب الإتقان دفعة واحدة.",
                    "Break skills into steps: wudu, movements, short surahs and gradual independence rather than demanding mastery all at once.",
                ),
                section(
                    "القرآن علاقة مستمرة",
                    "A continuing relationship with Quran",
                    "يُختار مقدار قصير ثابت يناسب الطفل، ويُجمع بين التلاوة والفهم والحفظ دون جعل الخطأ في التجويد سببًا للإهانة.",
                    "Choose a short, consistent amount suited to the child, combining recitation, understanding and memorisation without humiliating mistakes.",
                    "إذا تحول البرنامج إلى صراع يومي تُراجع الكمية والأسلوب والمعلم ووقت الدرس.",
                    "If learning becomes a daily conflict, reassess the amount, method, teacher and timing.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "discipline_without_harm",
            title = LocalizedFamilyText("الانضباط بلا إيذاء", "Discipline without harm"),
            summary = LocalizedFamilyText(
                "حدود واضحة وعواقب تربوية متناسبة تحمي كرامة الطفل وتبتعد عن العنف والإذلال.",
                "Clear limits and proportionate educational consequences that protect a child's dignity and avoid violence and humiliation.",
            ),
            sections = listOf(
                section(
                    "الحد قبل العقوبة",
                    "Teach the limit first",
                    "يحتاج الطفل إلى معرفة القاعدة مسبقًا وما المتوقع منه وما النتيجة المنطقية إذا خالفها، بدل العقوبة المفاجئة تحت الغضب.",
                    "Children need to know the rule, the expectation and the logical consequence beforehand rather than receiving sudden punishment in anger.",
                    "تكون النتيجة مرتبطة بالسلوك قدر الإمكان، قصيرة ومفهومة، ثم يعود التعامل الطبيعي بعد انتهاء الموقف.",
                    "Consequences should be related to the behaviour where possible, brief and understandable, followed by a return to normal relationship.",
                ),
                section(
                    "ما يجب تجنبه",
                    "What to avoid",
                    "الضرب المؤذي والإهانة والتهديد والهجر الطويل والتشهير وكسر الممتلكات ليست وسائل تربية سليمة.",
                    "Harmful hitting, humiliation, threats, prolonged rejection, public shaming and destroying belongings are not sound parenting tools.",
                    "إذا شعر الوالد أنه سيفقد السيطرة يبتعد عن الموقف مؤقتًا ويطلب دعمًا بدل الاستمرار في التصعيد.",
                    "If a parent feels they may lose control, stepping away temporarily and seeking support is safer than escalating.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "child_digital_safety",
            title = LocalizedFamilyText("سلامة الطفل الرقمية", "Child digital safety"),
            summary = LocalizedFamilyText(
                "قواعد عملية للأجهزة والخصوصية والمحتوى والتواصل والشراء الإلكتروني بحسب العمر.",
                "Practical age-based rules for devices, privacy, content, communication and online spending.",
            ),
            sections = listOf(
                section(
                    "قواعد معلنة",
                    "Visible rules",
                    "تُحدد أوقات الأجهزة وأماكن استخدامها ونوع المحتوى المسموح والشراء داخل التطبيقات، وتُشرح الأسباب بدل الاكتفاء بالمنع.",
                    "Set device times, locations, allowed content and in-app purchase rules, explaining the reasons rather than relying on prohibition alone.",
                    "تُراجع القواعد مع العمر والنضج، ويُعرف الطفل أن عليه إبلاغ والديه عن الرسائل المخيفة أو الطلبات السرية أو الابتزاز دون خوف من العقوبة.",
                    "Review rules as maturity grows, and ensure the child can report frightening messages, secrecy requests or blackmail without fearing punishment.",
                ),
                section(
                    "الخصوصية والصور",
                    "Privacy and images",
                    "يتعلم الطفل عدم مشاركة العنوان والمدرسة وكلمات المرور والصور الخاصة ومعلومات الأسرة مع الغرباء.",
                    "Teach children not to share addresses, school details, passwords, private images or family information with strangers.",
                    "ينبغي للوالدين أيضًا احترام خصوصية الطفل وعدم نشر صوره ومواقفه المحرجة دون حاجة أو مراعاة لمصلحته.",
                    "Parents should also respect the child's privacy and avoid unnecessary posting of images or embarrassing moments.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "body_privacy_safeguarding",
            title = LocalizedFamilyText("خصوصية الجسد وحماية الطفل", "Body privacy and safeguarding"),
            summary = LocalizedFamilyText(
                "تعليم الحدود والاستئذان والقدرة على قول لا والإبلاغ عن اللمس أو الرسائل غير المريحة.",
                "Teaching boundaries, permission, the ability to say no and how to report uncomfortable touch or messages.",
            ),
            sections = listOf(
                section(
                    "لغة واضحة تناسب العمر",
                    "Clear age-appropriate language",
                    "يتعلم الطفل أسماء أعضاء الجسد وحدود الخصوصية ومن يحق له المساعدة في النظافة أو الفحص وبأي شروط، دون تخويف أو إشعار بالعار.",
                    "Teach age-appropriate body names, privacy boundaries and who may help with hygiene or medical examination and under what conditions, without fear or shame.",
                    "يُعلم أن بعض المفاجآت مؤقتة، أما الأسرار التي تسبب خوفًا أو تتعلق بالجسد أو التهديد فيجب إخبار شخص بالغ موثوق بها.",
                    "Teach that some surprises are temporary, but secrets involving fear, the body or threats should be shared with a trusted adult.",
                ),
                section(
                    "إذا أفصح الطفل",
                    "If a child discloses harm",
                    "يُستمع للطفل بهدوء دون لوم أو استجواب متكرر، وتُتخذ إجراءات السلامة ويُطلب دعم الجهات المختصة.",
                    "Listen calmly without blame or repeated interrogation, take safety steps and seek qualified safeguarding support.",
                    "لا يواجه الوالد المشتبه به بطريقة قد تزيد الخطر قبل أخذ توجيه متخصص عندما تكون هناك شبهة اعتداء.",
                    "Where abuse is suspected, avoid confrontation that could increase risk before obtaining specialist safeguarding guidance.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "sibling_fairness",
            title = LocalizedFamilyText("العدل بين الأبناء وإدارة الغيرة", "Fairness between siblings and jealousy"),
            summary = LocalizedFamilyText(
                "العدل في الاهتمام والعطاء مع مراعاة اختلاف العمر والحاجة وعدم صناعة المقارنات بين الإخوة.",
                "Fairness in attention and giving while recognising different ages and needs and avoiding harmful comparison.",
            ),
            sections = listOf(
                section(
                    "العدل ليس التطابق",
                    "Fairness is not identical treatment",
                    "قد يحتاج طفل علاجًا أو وقتًا أو مصروفًا أكثر من غيره، والعدل يكون في مراعاة الحاجة دون تفضيل قائم على الهوى.",
                    "One child may need more medical care, time or spending; fairness means responding to need rather than preference.",
                    "تُشرح الفروق المناسبة للأطفال بدل تركهم يفسرونها على أنها محبة لشخص ورفض لآخر.",
                    "Explain appropriate differences rather than leaving children to interpret them as love for one and rejection of another.",
                ),
                section(
                    "منع المقارنات",
                    "Avoid comparisons",
                    "لا يُستخدم تفوق أحد الأبناء لإهانة الآخر، وتُوصف السلوكيات المطلوب تحسينها دون تثبيت ألقاب مثل الكسول أو المشاغب.",
                    "Do not use one child's success to humiliate another. Describe behaviours that need change rather than fixing labels such as lazy or troublesome.",
                    "يُخصص لكل طفل وقت واهتمام مناسب حتى مع ضغط الحياة.",
                    "Give each child appropriate individual attention even when family life is busy.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "children_faith_questions",
            title = LocalizedFamilyText("أسئلة الأبناء عن الإيمان", "Children's questions about faith"),
            summary = LocalizedFamilyText(
                "استقبال أسئلة العقيدة والقدر والعبادة والشك بصدق وهدوء يتناسب مع العمر.",
                "Receiving questions about belief, destiny, worship and doubt with honesty and age-appropriate calm.",
            ),
            sections = listOf(
                section(
                    "السؤال ليس مشكلة",
                    "Questions are not a problem",
                    "يسأل الطفل ليفهم العالم، وقد تكون أسئلته مباشرة أو صعبة. يُشكر على السؤال ويُجاب بما يعرف الوالد دون اختلاق جواب.",
                    "Children ask in order to understand the world, and questions can be direct or difficult. Welcome the question and answer what you know without inventing certainty.",
                    "يمكن قول «لا أعرف وسأبحث معك» ثم الرجوع إلى مصدر موثوق؛ فهذا يعلم الأمانة العلمية.",
                    "It is acceptable to say “I don't know; let's look it up together” and use a trusted source, modelling intellectual honesty.",
                ),
                section(
                    "عند الشك أو النفور",
                    "When doubt or resistance appears",
                    "لا تُستخدم السخرية أو التخويف أو الاتهام عند وجود شك؛ يُبحث عن أصل السؤال وتجربة الطفل ومصدر الفكرة.",
                    "Do not respond to doubt with ridicule, intimidation or accusation; explore the source of the question, the child's experience and where the idea came from.",
                    "الموضوعات العميقة قد تحتاج معلمًا حسن الأسلوب يجمع العلم وفهم المرحلة العمرية.",
                    "Difficult topics may benefit from a knowledgeable teacher who also understands the child's developmental stage.",
                ),
            ),
        ),
    )

    val metadata: List<FamilyTopicMetadata> = listOf(
        meta("pregnancy_preparation", FamilyTopicCategory.Newborn, "حمل", "ولادة", "pregnancy", "birth"),
        meta("postpartum_family_support", FamilyTopicCategory.Newborn, "نفاس", "ام", "postpartum", "mother"),
        meta("newborn_sunnahs_evidence", FamilyTopicCategory.Newborn, "مولود", "تحنيك", "اذان", "newborn", "sunnah"),
        meta("breastfeeding_child_care", FamilyTopicCategory.Newborn, "رضاعة", "رضيع", "breastfeeding", "infant"),
        meta("aqiqah_complete_guide", FamilyTopicCategory.Newborn, "عقيقة", "سابع", "ذبح", "aqiqah"),
        meta("choosing_child_name", FamilyTopicCategory.Newborn, "اسم", "تسمية", "name", "naming"),
        meta("parenting_early_years", FamilyTopicCategory.Parenting, "طفولة", "سنوات اولى", "early years"),
        meta("parenting_school_age", FamilyTopicCategory.Parenting, "مدرسة", "اصدقاء", "school age"),
        meta("parenting_teens", FamilyTopicCategory.Parenting, "مراهق", "مراهقة", "teen", "adolescent"),
        meta("children_prayer_quran", FamilyTopicCategory.Parenting, "صلاة", "قران", "prayer", "quran"),
        meta("discipline_without_harm", FamilyTopicCategory.Parenting, "انضباط", "عقاب", "ضرب", "discipline"),
        meta("child_digital_safety", FamilyTopicCategory.Parenting, "انترنت", "هاتف", "رقمي", "digital safety"),
        meta("body_privacy_safeguarding", FamilyTopicCategory.Parenting, "جسد", "خصوصية", "اعتداء", "safeguarding"),
        meta("sibling_fairness", FamilyTopicCategory.Parenting, "اخوة", "عدل", "غيرة", "siblings"),
        meta("children_faith_questions", FamilyTopicCategory.Parenting, "ايمان", "اسئلة", "شك", "faith questions"),
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
