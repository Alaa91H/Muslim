package org.muslim.app.feature.family.domain

/**
 * Phase-four expansion for parents, kinship, extended-family boundaries and
 * ordinary Muslim household life.
 *
 * The material remains general education: legal, medical and case-specific
 * religious questions are explicitly routed to qualified local professionals.
 */
object FamilyDailyKinshipContent {
    val articles: List<FamilyGuideArticle> = listOf(
        FamilyGuideArticle(
            id = "parents_kindness_boundaries",
            title = LocalizedFamilyText("بر الوالدين مع الحدود الصحية", "Honouring parents with healthy boundaries"),
            summary = LocalizedFamilyText(
                "الجمع بين البر والاحترام وبين حماية البيت والزواج والخصوصية من التدخل المؤذي.",
                "Combining kindness and respect with protecting the household, marriage and privacy from harmful intrusion.",
            ),
            sections = listOf(
                section(
                    "البر أصل ثابت",
                    "Kindness is foundational",
                    "يُحسن المسلم إلى والديه بالكلمة والخدمة والدعاء والزيارة بحسب الاستطاعة، خصوصًا عند الكبر والحاجة.",
                    "A Muslim should treat parents with kind speech, service, supplication and contact according to ability, especially in old age and need.",
                    "البر لا يعني الموافقة على كل طلب، ولا يجيز ظلم الزوج أو الزوجة أو الأطفال لإرضاء طرف آخر.",
                    "Honouring parents does not require agreeing to every request, nor does it justify wronging a spouse or children to please someone else.",
                ),
                section(
                    "حدود بلا إساءة",
                    "Boundaries without disrespect",
                    "يمكن رفض الطلب المؤذي أو غير الممكن بلغة هادئة مع تقديم بديل معقول متى أمكن.",
                    "A harmful or impossible request can be declined calmly while offering a reasonable alternative where possible.",
                    "عند النزاع المتكرر يفيد الاتفاق على أوقات الزيارة، وطريقة التواصل، وما يبقى خاصًا بين الزوجين.",
                    "For recurring tension, agree on visiting times, communication patterns and what remains private between spouses.",
                ),
            ),
            references = listOf(
                quran("الإحسان إلى الوالدين", "Kindness to parents", "Quran 17:23-24"),
                quran("الشكر للوالدين مع عدم الطاعة في المعصية", "Gratitude with limits to obedience", "Quran 31:14-15"),
            ),
        ),
        FamilyGuideArticle(
            id = "elder_parent_care",
            title = LocalizedFamilyText("رعاية الوالدين عند الكبر", "Caring for ageing parents"),
            summary = LocalizedFamilyText(
                "تنظيم الرعاية والزيارات والطب والمال بين الإخوة بطريقة رحيمة ومستدامة.",
                "Organising care, visits, health support and finances among siblings in a compassionate and sustainable way.",
            ),
            sections = listOf(
                section(
                    "خطة رعاية واقعية",
                    "A realistic care plan",
                    "تُكتب الاحتياجات اليومية والمواعيد الطبية والأدوية ومن يستطيع المساعدة، بدل الاعتماد على شخص واحد حتى الإنهاك.",
                    "List daily needs, medical appointments, medicines and who can help instead of relying on one person until burnout.",
                    "يُراعى قدر الإمكان رأي الوالد وكرامته وخصوصيته في القرارات التي تمسه.",
                    "As far as possible, preserve the parent's voice, dignity and privacy in decisions that affect them.",
                ),
                section(
                    "تقاسم المسؤولية",
                    "Sharing responsibility",
                    "يمكن توزيع الرعاية بين الوقت والمال والتنقل والأعمال الإدارية بحسب قدرة كل فرد، وليس بالضرورة بصورة متطابقة.",
                    "Care can be shared through time, money, transport and administration according to each person's capacity rather than identically.",
                    "المسائل الطبية أو القانونية المتعلقة بالأهلية والوكالات تحتاج مختصين في البلد ولا يحسمها التطبيق.",
                    "Medical or legal questions about capacity and powers of attorney need qualified local professionals and are not decided by the app.",
                ),
            ),
            references = listOf(
                quran("القول الكريم عند الكبر", "Respectful speech in old age", "Quran 17:23-24"),
                health("قرارات الرعاية الصحية", "Healthcare decisions", "Qualified local health professionals"),
            ),
        ),
        FamilyGuideArticle(
            id = "supporting_parents_financially",
            title = LocalizedFamilyText("مساندة الوالدين ماليًا", "Financial support for parents"),
            summary = LocalizedFamilyText(
                "إدارة المساعدة المالية للوالدين مع الحفاظ على احتياجات الأسرة والشفافية بين الإخوة.",
                "Managing financial help for parents while protecting household needs and transparency among siblings.",
            ),
            sections = listOf(
                section(
                    "تقدير الحاجة والاستطاعة",
                    "Assess need and ability",
                    "يُنظر إلى الاحتياجات الأساسية للوالدين وإمكانات الأبناء والتزاماتهم، ويُتجنب تحميل شخص واحد ما لا يطيق.",
                    "Consider parents' essential needs, each child's means and existing obligations, avoiding an unsustainable burden on one person.",
                    "إذا كانت هناك ديون أو ممتلكات أو إعانات رسمية فالأفضل جمع صورة مالية واضحة قبل اتخاذ قرارات كبيرة.",
                    "Where debt, assets or public benefits exist, build a clear financial picture before making large commitments.",
                ),
                section(
                    "الشفافية بين الإخوة",
                    "Transparency among siblings",
                    "يفيد تسجيل ما تم الاتفاق عليه من مساهمات ومصاريف حتى لا تتحول الرعاية إلى مصدر اتهام أو خصومة.",
                    "Document agreed contributions and expenses so care does not become a source of suspicion or conflict.",
                    "الالتزامات الشرعية والقانونية التفصيلية تختلف باختلاف الحال والبلد وتحتاج سؤالًا مختصًا.",
                    "Detailed religious and legal obligations vary by circumstances and jurisdiction and require qualified advice.",
                ),
            ),
            references = listOf(
                quran("الإنفاق على الوالدين والأقربين", "Spending for parents and relatives", "Quran 2:215"),
            ),
        ),
        FamilyGuideArticle(
            id = "maintaining_kinship",
            title = LocalizedFamilyText("صلة الرحم بصورة مستدامة", "Sustainable family ties"),
            summary = LocalizedFamilyText(
                "وسائل عملية لصلة الأقارب بالزيارة والاتصال والمساعدة دون تحويلها إلى عبء أو منافسة.",
                "Practical ways to maintain kinship through contact, visits and support without turning it into burden or competition.",
            ),
            sections = listOf(
                section(
                    "صور متعددة للصلة",
                    "Many forms of connection",
                    "صلة الرحم لا تقتصر على الزيارة؛ قد تكون اتصالًا أو رسالة أو سؤالًا عن المريض أو مساعدة عملية أو مالية بحسب الحاجة.",
                    "Maintaining kinship is not limited to visits; it may be a call, message, checking on illness or practical and financial help according to need.",
                    "الاستمرار القليل المنتظم أنفع من اندفاع مؤقت يتبعه انقطاع طويل.",
                    "Small, consistent contact is often better than intense short periods followed by long absence.",
                ),
                section(
                    "عند اختلاف الطباع",
                    "When personalities differ",
                    "تُخفف الاحتكاكات المتوقعة بتحديد وقت الزيارة وموضوعاتها وعدم فتح ملفات قديمة كل مرة.",
                    "Reduce predictable friction by setting appropriate visit length and avoiding reopening old disputes every time.",
                    "إذا تعذر القرب الشديد يمكن إبقاء الحد الأدنى من الاتصال المحترم بقدر آمن ومعقول.",
                    "If close contact is not workable, maintain a safe and reasonable minimum of respectful connection where possible.",
                ),
            ),
            references = listOf(
                quran("تقوى الله والأرحام", "Mindfulness of Allah and family ties", "Quran 4:1"),
                quran("التحذير من قطع الأرحام", "Warning against severing family ties", "Quran 47:22-23"),
            ),
        ),
        FamilyGuideArticle(
            id = "harmful_relatives_boundaries",
            title = LocalizedFamilyText("التعامل مع القريب المؤذي", "Dealing with a harmful relative"),
            summary = LocalizedFamilyText(
                "التمييز بين صلة الرحم وبين تعريض النفس أو الأطفال للإيذاء أو الابتزاز أو الإهانة المستمرة.",
                "Distinguishing family connection from exposing yourself or children to abuse, blackmail or repeated humiliation.",
            ),
            sections = listOf(
                section(
                    "صلة لا تعني تمكين الأذى",
                    "Connection does not mean enabling harm",
                    "يمكن تقليل مدة اللقاء أو تغيير مكانه أو جعله بحضور أشخاص موثوقين عند وجود إساءة متكررة.",
                    "Where repeated harm occurs, contact can be shortened, moved to a safer place or held with trusted people present.",
                    "التهديد والعنف والاستغلال المالي أو الجنسي وحماية الأطفال ليست مسائل تُحل بالمجاملة العائلية.",
                    "Threats, violence, financial or sexual exploitation and child safeguarding are not matters to solve through family politeness alone.",
                ),
                section(
                    "توثيق وطلب دعم",
                    "Document and seek support",
                    "في الحالات الخطرة تُحفظ الرسائل أو الوثائق المهمة بطريقة آمنة ويُطلب دعم الجهات المختصة.",
                    "In serious situations, preserve relevant messages or documents safely and seek qualified support.",
                    "تحديد الحد الشرعي المناسب للعلاقة في حالة معينة قد يحتاج عالمًا موثوقًا يعرف تفاصيل الواقعة.",
                    "Determining the appropriate religious boundary in a specific case may require a trusted scholar who understands the facts.",
                ),
            ),
            references = listOf(
                guidance("منع الضرر مع حفظ الحقوق", "Preventing harm while preserving rights", "Case-specific qualified guidance"),
            ),
        ),
        FamilyGuideArticle(
            id = "inlaws_household_boundaries",
            title = LocalizedFamilyText("الأسرة الممتدة وحدود بيت الزوجية", "Extended family and marital-home boundaries"),
            summary = LocalizedFamilyText(
                "تنظيم الزيارات والخصوصية والقرارات بين الزوجين والأهل دون عزل أو تدخل مفرط.",
                "Organising visits, privacy and decision-making between spouses and extended family without isolation or overreach.",
            ),
            sections = listOf(
                section(
                    "البيت له خصوصية",
                    "The home needs privacy",
                    "يُتفق بين الزوجين على أوقات الزيارة والمبيت والمفاتيح والدخول المفاجئ بما يراعي الراحة والستر.",
                    "Spouses should agree on visits, overnight stays, keys and unannounced entry in a way that protects comfort and privacy.",
                    "لا يُطلب من أحد الزوجين قبول كشف تفاصيل الحياة الخاصة للأهل لإثبات الاحترام أو الولاء.",
                    "Neither spouse should be required to expose private marital details to relatives as proof of respect or loyalty.",
                ),
                section(
                    "الأهل سند لا إدارة بديلة",
                    "Family can support without taking over",
                    "تُستفاد خبرة الأهل ونصيحتهم، لكن القرارات اليومية للبيت تبقى مسؤولية الزوجين ما لم توجد ضرورة أو خطر.",
                    "Relatives' experience and advice can help, but ordinary household decisions remain the couple's responsibility unless safety or necessity requires intervention.",
                    "عند الخلاف بين الزوج والأهل أو الزوجة والأهل تُمنع الإهانة ويُبحث عن ترتيب عملي لا عن معسكرات متقابلة.",
                    "When tension arises between a spouse and relatives, prevent insults and seek practical arrangements rather than opposing camps.",
                ),
            ),
            references = listOf(
                quran("آداب الاستئذان ودخول البيوت", "Permission and household privacy", "Quran 24:27"),
            ),
        ),
        FamilyGuideArticle(
            id = "family_reconciliation_after_distance",
            title = LocalizedFamilyText("إصلاح العلاقة بعد القطيعة أو البعد", "Repairing family ties after distance"),
            summary = LocalizedFamilyText(
                "خطوات متدرجة لإعادة التواصل عندما يكون ذلك آمنًا ومفيدًا، دون إنكار ما وقع من أذى.",
                "Gradual steps to restore contact when safe and beneficial without denying past harm.",
            ),
            sections = listOf(
                section(
                    "بداية صغيرة",
                    "Start small",
                    "يمكن البدء برسالة محايدة أو سؤال عن الصحة أو مناسبة عامة بدل محاولة حل سنوات من الخلاف في جلسة واحدة.",
                    "Begin with a neutral message, checking on health or a public occasion rather than trying to solve years of conflict in one meeting.",
                    "تُحدد توقعات واقعية؛ عودة السلام قد تسبق عودة الثقة الكاملة.",
                    "Keep expectations realistic; civil contact may return before full trust does.",
                ),
                section(
                    "الاعتذار والإصلاح",
                    "Apology and repair",
                    "الاعتذار المفيد يحدد الخطأ دون تبرير طويل، ويعترف بالأثر، ويقترح سلوكًا مختلفًا مستقبلًا.",
                    "A useful apology names the wrong without lengthy excuses, recognises its impact and proposes different behaviour going forward.",
                    "إذا كانت العودة إلى العلاقة تعيد خطرًا حقيقيًا فالأولوية للأمان مع طلب توجيه مختص.",
                    "If renewed contact recreates genuine danger, safety takes priority and qualified guidance should be sought.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "household_worship_routine",
            title = LocalizedFamilyText("نظام عبادة واقعي داخل البيت", "A realistic worship routine at home"),
            summary = LocalizedFamilyText(
                "تنظيم الصلاة والقرآن والأذكار في حياة الأسرة دون تحويل البيت إلى جدول مرهق.",
                "Organising prayer, Quran and remembrance in family life without turning the home into an exhausting schedule.",
            ),
            sections = listOf(
                section(
                    "ثوابت قليلة مستمرة",
                    "A few consistent anchors",
                    "يُبنى الروتين على الفرائض أولًا ثم عادات يسيرة مثل قراءة قصيرة أو ذكر جماعي غير ملزم أو مراجعة حفظ الأطفال.",
                    "Build the routine around obligatory worship first, then simple habits such as short reading, optional family remembrance or reviewing children's memorisation.",
                    "لا يُقاس صلاح البيت بكثرة البرامج؛ الاستمرار والرحمة ومراعاة المرض والعمل والنوم أهم من الجدول المثالي.",
                    "A good home is not measured by the number of programmes; consistency, mercy and consideration for illness, work and sleep matter more than an ideal schedule.",
                ),
                section(
                    "القدوة قبل الأوامر",
                    "Example before commands",
                    "يرى الأطفال الصلاة والقراءة والصدق والاعتذار في سلوك الكبار، فلا تصبح العبادة مجرد أوامر موجهة للصغار.",
                    "Children should see prayer, reading, honesty and apology in adults so worship does not become a set of commands aimed only at them.",
                    "عند ضغط الدراسة والعمل تُحفظ الأولويات وتخفف الأنشطة الإضافية بدل خلق نزاع يومي.",
                    "During intense work or study periods, protect priorities and reduce optional activities rather than creating daily conflict.",
                ),
            ),
            references = listOf(
                quran("الأمر بالصلاة والصبر عليها", "Prayer and perseverance", "Quran 20:132"),
            ),
        ),
        FamilyGuideArticle(
            id = "family_shura_decisions",
            title = LocalizedFamilyText("الشورى واتخاذ قرارات الأسرة", "Family consultation and decisions"),
            summary = LocalizedFamilyText(
                "طريقة عملية للقرارات المتعلقة بالمال والسكن والعمل والأبناء بعيدًا عن الفرض والصراع.",
                "A practical approach to decisions about money, housing, work and children without coercion or power struggles.",
            ),
            sections = listOf(
                section(
                    "حدد القرار والمعلومات",
                    "Define the decision and facts",
                    "قبل النقاش يُحدد ما الذي يحتاج قرارًا، وما المعلومات الناقصة، وما الموعد النهائي، ومن يتأثر بالنتيجة.",
                    "Before discussion, define the decision, missing information, deadline and who will be affected.",
                    "القرارات الكبيرة لا تُحسم تحت الغضب أو في نهاية يوم مرهق إن أمكن تأجيلها.",
                    "Major decisions should not be settled in anger or at the end of an exhausting day when they can reasonably wait.",
                ),
                section(
                    "صوت كل متأثر",
                    "Hear those affected",
                    "يُسمع رأي الزوجين، ويُشرك الأبناء بقدر عمرهم في القرارات التي تمس حياتهم دون تحميلهم مسؤولية الكبار.",
                    "Both spouses should be heard, and children can be included at an age-appropriate level in decisions affecting them without carrying adult responsibility.",
                    "إذا تعذر الاتفاق تُكتب الخيارات وتكاليفها ويُطلب رأي خبير عند الحاجة.",
                    "If agreement is difficult, write down options and trade-offs and seek expert input where useful.",
                ),
            ),
            references = listOf(
                quran("وأمرهم شورى بينهم", "Their affairs are by consultation", "Quran 42:38"),
            ),
        ),
        FamilyGuideArticle(
            id = "family_budget_moderation",
            title = LocalizedFamilyText("الإنفاق المنزلي بلا إسراف ولا تقتير", "Household spending without excess or deprivation"),
            summary = LocalizedFamilyText(
                "ميزانية أسرية توازن الضروريات والادخار والكرم والترفيه ضمن القدرة.",
                "A household budget balancing essentials, savings, generosity and recreation within actual means.",
            ),
            sections = listOf(
                section(
                    "ابدأ بالثابت",
                    "Start with essentials",
                    "تُحصر تكاليف السكن والطعام والنقل والديون والصحة ثم يحدد الادخار والطوارئ قبل الإنفاق الاختياري.",
                    "List housing, food, transport, debt and health costs first, then savings and emergencies before discretionary spending.",
                    "المصاريف الصغيرة المتكررة تُراجع شهريًا لأنها قد تكون أكبر من المشتريات النادرة الواضحة.",
                    "Review small recurring expenses monthly because they can exceed occasional visible purchases.",
                ),
                section(
                    "كرم بلا ضغط",
                    "Generosity without pressure",
                    "الهدايا والضيافة والصدقة أبواب خير، لكنها لا تحتاج إلى مظهر يفوق القدرة أو ديون لإرضاء الناس.",
                    "Gifts, hospitality and charity are good, but they do not require status spending or debt to impress others.",
                    "يتفق الزوجان على سقف للمشتريات التي تحتاج تشاورًا مسبقًا لتقليل المفاجآت.",
                    "Spouses can agree on a spending threshold that requires prior discussion to reduce surprises.",
                ),
            ),
            references = listOf(
                quran("الاعتدال في الإنفاق", "Moderation in spending", "Quran 25:67"),
            ),
        ),
        FamilyGuideArticle(
            id = "household_privacy_devices",
            title = LocalizedFamilyText("خصوصية الأسرة والأجهزة", "Household privacy and devices"),
            summary = LocalizedFamilyText(
                "قواعد للهواتف والصور وكلمات المرور والموقع والمراقبة تحمي الثقة والسلامة.",
                "Rules for phones, photos, passwords, location and monitoring that protect both trust and safety.",
            ),
            sections = listOf(
                section(
                    "الخصوصية ليست سرية مطلقة",
                    "Privacy is not unlimited secrecy",
                    "لكل فرد مساحة مناسبة لعمره ودوره، مع قواعد معلنة عند وجود مخاطر حقيقية على الأطفال أو أموال الأسرة.",
                    "Each family member needs age- and role-appropriate privacy, with clear safeguards where genuine child or financial risks exist.",
                    "تفتيش الهاتف سرًا أو مشاركة كلمات المرور بالإكراه ليس بديلًا عن معالجة فقدان الثقة.",
                    "Secret phone searches or coerced password sharing are not substitutes for addressing broken trust.",
                ),
                section(
                    "الصور والموقع",
                    "Images and location",
                    "لا تُنشر صور الأسرة أو الأطفال أو مواقعهم دون مراعاة الخصوصية والمخاطر والرضا المناسب للعمر.",
                    "Do not publish family or child images or locations without considering privacy, risk and age-appropriate consent.",
                    "تُغلق مشاركة الموقع الدائمة عندما لا توجد حاجة واضحة، وتُراجع صلاحيات التطبيقات دوريًا.",
                    "Disable persistent location sharing when there is no clear need and review app permissions periodically.",
                ),
            ),
            references = listOf(
                quran("أوقات الخصوصية والاستئذان", "Private times and permission", "Quran 24:58-59"),
                guidance("السلامة الرقمية الأسرية", "Family digital safety", "Age-appropriate privacy and safeguarding guidance"),
            ),
        ),
        FamilyGuideArticle(
            id = "family_work_study_balance",
            title = LocalizedFamilyText("التوازن بين العمل والدراسة والأسرة", "Balancing work, study and family"),
            summary = LocalizedFamilyText(
                "توزيع الوقت والطاقة بصورة تحمي الرزق والتعليم والعلاقات والراحة.",
                "Allocating time and energy in a way that protects livelihood, education, relationships and rest.",
            ),
            sections = listOf(
                section(
                    "الوقت ليس وحده المشكلة",
                    "It is not only about time",
                    "يُنظر إلى الطاقة والانتباه أيضًا؛ فقد يكون الشخص حاضرًا في البيت لكنه منشغل تمامًا بالهاتف أو العمل.",
                    "Consider energy and attention as well as hours; someone can be physically home but entirely occupied by a phone or work.",
                    "يفيد تحديد أوقات خالية من العمل للأكل أو الحديث أو النوم بدل بقاء العمل مفتوحًا طوال اليوم.",
                    "Set work-free periods for meals, conversation or sleep rather than allowing work to remain open all day.",
                ),
                section(
                    "مواسم الضغط",
                    "Busy seasons",
                    "في الامتحانات أو المشاريع أو المرض تُخفف بعض الالتزامات مؤقتًا مع توضيح أن هذا استثناء له نهاية.",
                    "During exams, projects or illness, temporarily reduce some commitments while making clear that the arrangement has an end point.",
                    "بعد الموسم المزدحم تُراجع الترتيبات حتى لا يتحول المؤقت إلى غياب دائم.",
                    "After a demanding period, review routines so a temporary exception does not become permanent absence.",
                ),
            ),
        ),
        FamilyGuideArticle(
            id = "guests_neighbours_home",
            title = LocalizedFamilyText("الضيافة والجيران وراحة البيت", "Guests, neighbours and household peace"),
            summary = LocalizedFamilyText(
                "إكرام الضيف والجيران مع احترام ميزانية الأسرة وخصوصيتها ووقت راحتها.",
                "Honouring guests and neighbours while respecting the household's budget, privacy and need for rest.",
            ),
            sections = listOf(
                section(
                    "ضيافة مناسبة للاستطاعة",
                    "Hospitality within means",
                    "يُكرم الضيف بما يتيسر دون تكلف يرهق الأسرة أو يخلق دينًا أو توترًا بين الزوجين.",
                    "Welcome guests within your means without creating debt, exhaustion or conflict between spouses.",
                    "من حق الأسرة تنظيم الزيارات الطويلة والمبيت والعدد بما يناسب السكن والخصوصية.",
                    "A household may organise long visits, overnight stays and guest numbers according to space and privacy.",
                ),
                section(
                    "حق الجار",
                    "The neighbour's right",
                    "تُراعى الضوضاء والمواقف والممرات والروائح والأماكن المشتركة، ويُحل الخلاف مباشرة وبأدب قبل التصعيد.",
                    "Consider noise, parking, corridors, smells and shared spaces, resolving issues directly and politely before escalation.",
                    "عند وجود تهديد أو مضايقة مستمرة يمكن استخدام القنوات القانونية المحلية بدل المواجهة الخطرة.",
                    "Where there are threats or persistent harassment, local legal channels may be safer than risky confrontation.",
                ),
            ),
            references = listOf(
                hadith("إكرام الضيف والجار", "Honouring guests and neighbours", "Sahih al-Bukhari and Sahih Muslim"),
            ),
        ),
        FamilyGuideArticle(
            id = "family_healthcare_planning",
            title = LocalizedFamilyText("تنظيم الرعاية الصحية للأسرة", "Organising family healthcare"),
            summary = LocalizedFamilyText(
                "حفظ المعلومات الصحية الضرورية والمواعيد والأدوية والطوارئ دون تحويل أحد أفراد الأسرة إلى طبيب.",
                "Keeping essential health information, appointments, medicines and emergency plans organised without turning a family member into the doctor.",
            ),
            sections = listOf(
                section(
                    "معلومات أساسية محدثة",
                    "Keep essentials current",
                    "يفيد حفظ قائمة أدوية وحساسيات وأرقام أطباء وتأمين ومعلومات طوارئ في مكان آمن يسهل الوصول إليه عند الحاجة.",
                    "Keep medicines, allergies, clinicians, insurance and emergency information current in a secure but accessible place.",
                    "يُراجع تاريخ انتهاء الأدوية ومحتويات الإسعاف الأولي دوريًا، ولا تُشارك أدوية الوصفات بين أفراد الأسرة.",
                    "Review medicine expiry dates and first-aid supplies periodically, and do not share prescription medicines between family members.",
                ),
                section(
                    "متى نطلب المساعدة؟",
                    "When to seek help",
                    "الأعراض الطارئة أو التغيرات النفسية الخطرة تحتاج جهات طبية مختصة، ولا يؤخرها انتظار نصيحة عائلية أو دينية.",
                    "Medical emergencies or dangerous mental-health changes require qualified care and should not be delayed while waiting for family or religious advice.",
                    "التطبيق يساعد على التنظيم العام فقط ولا يشخص المرض ولا يحدد العلاج.",
                    "The app supports general organisation only; it does not diagnose illness or prescribe treatment.",
                ),
            ),
            references = listOf(
                health("الرعاية الصحية الفردية", "Individual healthcare", "Qualified local healthcare professionals"),
            ),
        ),
        FamilyGuideArticle(
            id = "family_weekly_meeting",
            title = LocalizedFamilyText("اجتماع أسري أسبوعي بسيط", "A simple weekly family meeting"),
            summary = LocalizedFamilyText(
                "اجتماع قصير لمراجعة المواعيد والمهام والميزانية والمشكلات قبل أن تتراكم.",
                "A short meeting to review schedules, tasks, money and problems before they accumulate.",
            ),
            sections = listOf(
                section(
                    "عشرون دقيقة منظمة",
                    "A short structured meeting",
                    "يبدأ الاجتماع بما سار جيدًا ثم المواعيد القادمة والمهام والمصروفات وأي مشكلة تحتاج قرارًا.",
                    "Start with what went well, then upcoming schedules, tasks, expenses and any issue requiring a decision.",
                    "يُحدد موضوع أو موضوعان فقط للنقاش العميق حتى لا يتحول الاجتماع إلى جلسة محاسبة طويلة.",
                    "Choose only one or two issues for deeper discussion so the meeting does not become a long criticism session.",
                ),
                section(
                    "متابعة ما اتفق عليه",
                    "Follow through",
                    "يُكتب من سيفعل ماذا ومتى، ثم تُراجع النتيجة في الاجتماع التالي دون سخرية أو لوم عام.",
                    "Write down who will do what and by when, then review it next time without ridicule or broad blame.",
                    "يمكن للأطفال المشاركة بقدر عمرهم في الجدول والمهام واقتراح أنشطة مشتركة.",
                    "Children can participate at an age-appropriate level in schedules, chores and suggestions for shared activities.",
                ),
            ),
            references = listOf(
                quran("الشورى", "Consultation", "Quran 42:38"),
            ),
        ),
    )

    val metadata: List<FamilyTopicMetadata> = listOf(
        meta("parents_kindness_boundaries", FamilyTopicCategory.Kinship, "والدين", "بر", "حدود", "parents", "boundaries"),
        meta("elder_parent_care", FamilyTopicCategory.Kinship, "كبار السن", "رعاية", "elder care"),
        meta("supporting_parents_financially", FamilyTopicCategory.Kinship, "والدين", "مال", "نفقة", "parents finance"),
        meta("maintaining_kinship", FamilyTopicCategory.Kinship, "صلة", "رحم", "اقارب", "kinship"),
        meta("harmful_relatives_boundaries", FamilyTopicCategory.Kinship, "قريب مؤذي", "حدود", "harmful relatives"),
        meta("inlaws_household_boundaries", FamilyTopicCategory.Kinship, "اهل الزوج", "اهل الزوجة", "in laws", "privacy"),
        meta("family_reconciliation_after_distance", FamilyTopicCategory.Kinship, "قطيعة", "اصلاح", "reconciliation"),
        meta("household_worship_routine", FamilyTopicCategory.DailyLife, "عبادة", "صلاة", "قران", "worship routine"),
        meta("family_shura_decisions", FamilyTopicCategory.DailyLife, "شورى", "قرار", "decision"),
        meta("family_budget_moderation", FamilyTopicCategory.DailyLife, "ميزانية", "اسراف", "budget"),
        meta("household_privacy_devices", FamilyTopicCategory.DailyLife, "خصوصية", "اجهزة", "privacy", "devices"),
        meta("family_work_study_balance", FamilyTopicCategory.DailyLife, "عمل", "دراسة", "توازن", "work", "study"),
        meta("guests_neighbours_home", FamilyTopicCategory.DailyLife, "ضيف", "جار", "neighbour", "guest"),
        meta("family_healthcare_planning", FamilyTopicCategory.DailyLife, "صحة", "دواء", "healthcare"),
        meta("family_weekly_meeting", FamilyTopicCategory.DailyLife, "اجتماع", "اسبوعي", "family meeting"),
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

    private fun quran(titleAr: String, titleEn: String, citation: String) =
        reference(FamilyEvidenceType.Quran, titleAr, titleEn, citation)

    private fun hadith(titleAr: String, titleEn: String, citation: String) =
        reference(FamilyEvidenceType.Hadith, titleAr, titleEn, citation)

    private fun health(titleAr: String, titleEn: String, citation: String) =
        reference(FamilyEvidenceType.Health, titleAr, titleEn, citation)

    private fun guidance(titleAr: String, titleEn: String, citation: String) =
        reference(FamilyEvidenceType.Guidance, titleAr, titleEn, citation)

    private fun reference(
        type: FamilyEvidenceType,
        titleAr: String,
        titleEn: String,
        citation: String,
    ) = FamilyEvidenceReference(
        title = LocalizedFamilyText(titleAr, titleEn),
        citation = citation,
        type = type,
    )

    private fun meta(
        articleId: String,
        category: FamilyTopicCategory,
        vararg keywords: String,
    ) = FamilyTopicMetadata(articleId, category, keywords.toList())
}
