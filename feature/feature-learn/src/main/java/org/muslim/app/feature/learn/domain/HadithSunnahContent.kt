package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object HadithSunnahContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            sunnahIntroduction,
            hadithAnatomy,
            hadithGrades,
            hadithVerification,
            hadithUnderstanding,
            hadithLibraryGuide,
        )
    }

    private val sunnahIntroduction = LearningLesson(
        id = "sunnah_intro",
        titleRes = R.string.learn_topic_sunnah_intro,
        subtitleRes = R.string.learn_topic_sunnah_intro_sub,
        estimatedMinutes = 13,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "ما السنة وما الحديث؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "السنة في الاستعمال الشرعي تشمل ما ثبت عن النبي ﷺ من قول أو فعل أو تقرير أو صفة بحسب اصطلاح العلماء، والحديث هو الخبر الذي ينقل ذلك إلينا بإسناد ومتن. وقد تختلف دلالة المصطلحين قليلًا بين علوم الحديث والفقه والأصول.",
                    ),
                ),
            ),
            LearningSection(
                id = "authority",
                title = "مكانة السنة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بطاعة الله والرسول والرد إليهما عند التنازع.",
                        referenceIds = listOf("quran_4_59"),
                    ),
                    LearningContentBlock.Paragraph(
                        "السنة تشرح كثيرًا من تفاصيل العبادة والتطبيق العملي للنص القرآني. لكن نسبة قول إلى النبي ﷺ تحتاج تثبتًا؛ فالمعنى الصحيح لا يبرر استعمال رواية غير ثابتة.",
                    ),
                ),
            ),
            LearningSection(
                id = "learning",
                title = "كيف تدرس السنة؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ابدأ بالنص والمصدر", "اقرأ متن الحديث ومعه اسم الكتاب والباب والرقم إن توفر."),
                            LearningStepItem("اعرف درجته", "تمييز الصحيح والحسن والضعيف خطوة أولى، وليس نهاية الفهم."),
                            LearningStepItem("افهم السياق", "اسأل عن سبب الورود واللفظ الكامل والروايات الأخرى ذات الصلة."),
                            LearningStepItem("فرّق بين الرواية والحكم", "وجود حديث صحيح لا يعني أن كل استنباط منه صحيح أو متفق عليه."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "responsibility",
                title = "مسؤولية النقل",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ورد الوعيد الشديد في الكذب المتعمد على النبي ﷺ، ولذلك لا ينشر التطبيق نصًا منسوبًا إليه بلا مصدر.",
                        referenceIds = listOf("bukhari_108"),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_59", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:59", "Quran 4:59"),
            LearningReference("bukhari_108", LearningReferenceKind.HADITH, "صحيح البخاري 108، كتاب العلم", "Sahih al-Bukhari 108"),
        ),
    )

    private val hadithAnatomy = LearningLesson(
        id = "hadith_anatomy",
        titleRes = R.string.learn_topic_hadith_anatomy,
        subtitleRes = R.string.learn_topic_hadith_anatomy_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "isnad_matn",
                title = "الإسناد والمتن",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("الإسناد", "سلسلة الرواة الذين نقلوا الخبر بعضهم عن بعض حتى يصل إلى من رواه عن النبي ﷺ أو غيره."),
                            LearningComparisonItem("المتن", "نص الخبر نفسه الذي تنتهي إليه سلسلة الإسناد."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "narrators",
                title = "الرواة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يدرس المحدثون عدالة الرواة وضبطهم، وإمكان اللقاء والسماع بينهم، وما يروونه مقارنة بروايات غيرهم. الحكم على راوٍ أو إسناد مجال تخصصي لا يؤخذ من انطباع شخصي أو نتيجة بحث عامة.",
                    ),
                ),
            ),
            LearningSection(
                id = "routes",
                title = "الطرق والشواهد",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قد يروى الحديث من أكثر من طريق، وقد تختلف بعض الألفاظ. جمع الطرق يساعد في كشف الوهم، وتقوية بعض الروايات في مواضع، وفهم اللفظ المحفوظ بدقة أكبر.",
                    ),
                ),
            ),
            LearningSection(
                id = "book_reference",
                title = "مرجع الحديث داخل التطبيق",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("المجموعة", "مثل صحيح البخاري أو صحيح مسلم أو سنن أبي داود."),
                            LearningStepItem("الباب", "يساعد على فهم موضع الحديث في تصنيف المصنف."),
                            LearningStepItem("الرقم", "معرف عملي للوصول إلى الرواية داخل المجموعة."),
                            LearningStepItem("الدرجة والمصدر", "بيانات مساعدة يجب قراءتها مع سياق الكتاب والرواية."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val hadithGrades = LearningLesson(
        id = "hadith_grades",
        titleRes = R.string.learn_topic_hadith_grades,
        subtitleRes = R.string.learn_topic_hadith_grades_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "common",
                title = "أشهر درجات القبول والرد",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("صحيح", "رواية استوفت شروط الصحة عند من حكم بها من اتصال السند وعدالة الرواة وضبطهم والسلامة من الشذوذ والعلة وفق منهج النقد."),
                            LearningComparisonItem("حسن", "رواية مقبولة دون مرتبة الصحيح في الضبط في التقسيم المشهور، مع تفاصيل واختلافات اصطلاحية بين الأئمة."),
                            LearningComparisonItem("ضعيف", "لم تستوف شروط القبول على الوجه المطلوب؛ ودرجات الضعف وأسبابه متعددة."),
                            LearningComparisonItem("موضوع", "خبر مكذوب مصنوع لا يجوز نسبته إلى النبي ﷺ على أنه حديث ثابت."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "not_binary",
                title = "الدرجة ليست زرًا ثنائيًا",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قد يختلف النقاد في بعض الأحاديث بسبب اختلافهم في راوٍ أو اتصال أو علة أو طريق معين. كما قد يكون للحديث أكثر من إسناد وحكم. لذلك تحفظ واجهة التطبيق نص الدرجة كما ورد في بيانات المصدر ولا تدعي أنها حكم جديد صادر عن التطبيق.",
                    ),
                ),
            ),
            LearningSection(
                id = "books",
                title = "مكانة الكتب",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المجموعات الحديثية ليست كلها على شرط واحد؛ فالصحيحان صنفا لجمع الصحيح عند مؤلفيهما، بينما كتب السنن وغيرها تجمع أبوابًا فقهية وقد تشتمل على درجات مختلفة. اسم الكتاب وحده لا يغني عن قراءة بيانات الرواية.",
                    ),
                ),
            ),
            LearningSection(
                id = "weak",
                title = "الحديث الضعيف والعمل",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "مسألة لها تفصيل وخلاف",
                        body = "استعمال الحديث الضعيف في فضائل الأعمال أو غيرها له شروط ومناهج مختلفة عند العلماء. التطبيق لا يحول كلمة «ضعيف» إلى فتوى آلية بالعمل أو الترك.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
        ),
    )

    private val hadithVerification = LearningLesson(
        id = "hadith_verification",
        titleRes = R.string.learn_topic_hadith_verification,
        subtitleRes = R.string.learn_topic_hadith_verification_sub,
        estimatedMinutes = 16,
        sections = listOf(
            LearningSection(
                id = "workflow",
                title = "خطوات التحقق",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ابحث عن عبارة مميزة", "استخدم كلمات قليلة واضحة من المتن بدل نسخ منشور طويل قد يحتوي أخطاء."),
                            LearningStepItem("حدد المصدر", "اعرف الكتاب والباب والرقم أو موضع التخريج."),
                            LearningStepItem("قارن اللفظ", "تحقق هل المنشور يقتبس النص كاملًا أم يدمج روايتين أو شرحًا مع الحديث."),
                            LearningStepItem("راجع الدرجة", "انظر إلى حكم المصدر أو أهل الاختصاص، خصوصًا خارج الصحيحين."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "viral",
                title = "الرسائل المتداولة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "صورة جميلة أو عبارة مؤثرة أو آلاف المشاركات ليست دليلًا على ثبوت الحديث. إذا لم تجد مصدرًا واضحًا فلا تنسب النص إلى النبي ﷺ.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "paraphrase",
                title = "الحديث بالمعنى",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قد ينقل بعض أهل العلم المعنى لا اللفظ بشروط معروفة، لكن المستخدم العادي ينبغي أن يميز بوضوح بين «نص الحديث» وبين «شرح معناه» حتى لا يتحول الشرح إلى نص منسوب للنبي ﷺ.",
                    ),
                ),
            ),
            LearningSection(
                id = "lying",
                title = "لماذا هذا التشدد؟",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "شدة التحذير من الكذب على النبي ﷺ تجعل التثبت من المصدر جزءًا من الأمانة العلمية والدينية.",
                        referenceIds = listOf("bukhari_106"),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_106", LearningReferenceKind.HADITH, "صحيح البخاري 106، كتاب العلم", "Sahih al-Bukhari 106"),
        ),
    )

    private val hadithUnderstanding = LearningLesson(
        id = "hadith_understanding",
        titleRes = R.string.learn_topic_hadith_understanding,
        subtitleRes = R.string.learn_topic_hadith_understanding_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "context",
                title = "لا تقرأ جملة معزولة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "فهم الحديث قد يحتاج معرفة من المخاطب، وما السؤال الذي أجاب عنه النبي ﷺ، وهل الحكم عام أو خاص، وهل وردت روايات أخرى تقيد اللفظ أو تفسره.",
                    ),
                ),
            ),
            LearningSection(
                id = "quran",
                title = "القرآن والسنة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بالرجوع إلى الله والرسول عند التنازع، وهو أصل في قراءة السنة مع القرآن لا في عزلهما عن بعضهما.",
                        referenceIds = listOf("quran_4_59"),
                    ),
                ),
            ),
            LearningSection(
                id = "fiqh",
                title = "من الحديث إلى الحكم الفقهي",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الفقيه لا يبني الحكم على صحة الحديث وحدها؛ ينظر أيضًا في الدلالة، والجمع بين النصوص، والناسخ والمنسوخ، والعام والخاص، وعمل الصحابة وقواعد الاستنباط بحسب منهجه. لذلك قد يتفق العلماء على صحة حديث ويختلفون في بعض فقهه.",
                    ),
                ),
            ),
            LearningSection(
                id = "conflict",
                title = "إذا بدا تعارض بين حديثين",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("تأكد من الثبوت", "قد يكون أحد النصين غير ثابت أصلًا."),
                            LearningStepItem("اجمع الروايات", "اختلاف السياق قد يزيل التعارض الظاهري."),
                            LearningStepItem("افحص العموم والخصوص", "قد يكون أحد الحديثين في حالة أخص."),
                            LearningStepItem("لا ترجح وحدك في المسائل الدقيقة", "ارجع إلى شرح حديث أو فقه معتبر عند استمرار الإشكال."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_59", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:59", "Quran 4:59"),
        ),
    )

    private val hadithLibraryGuide = LearningLesson(
        id = "hadith_library_guide",
        titleRes = R.string.learn_topic_hadith_library_guide,
        subtitleRes = R.string.learn_topic_hadith_library_guide_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "scope",
                title = "ماذا توفر مكتبة الحديث؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ميزة الحديث في التطبيق مخصصة للتصفح والبحث داخل مجموعات حديثية محلية، مع الفصل بين المجموعة والباب والنص والترجمة والدرجة والمصدر. قسم Learn يشرح المنهج ولا يكرر قاعدة البيانات.",
                    ),
                ),
            ),
            LearningSection(
                id = "collections",
                title = "المجموعات الموجودة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تتضمن المكتبة الحالية صحيح البخاري وصحيح مسلم وسنن أبي داود والترمذي والنسائي وابن ماجه وموطأ مالك ورياض الصالحين والأربعين النووية. لكل مجموعة غرض وبنية مختلفة، فلا تُعامل كلها ككتاب واحد.",
                    ),
                ),
            ),
            LearningSection(
                id = "search",
                title = "استخدام البحث بذكاء",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ابدأ بكلمات مميزة", "كلمة أو عبارتان أفضل من سؤال طويل."),
                            LearningStepItem("افتح النتيجة في بابها", "قراءة عنوان الباب قد توضح سياق اختيار المصنف للحديث."),
                            LearningStepItem("قارن النتائج", "قد يظهر الحديث نفسه أو معناه في أكثر من مجموعة."),
                            LearningStepItem("احفظ المرجع", "عند المشاركة احتفظ باسم المجموعة والرقم بدل إرسال النص وحده."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "grade",
                title = "حدود بيانات الدرجة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "حقل الدرجة في التطبيق بيانات مرجعية مرتبطة بالمصدر الذي جُمعت منه المكتبة؛ لا يمثل اجتهادًا مستقلًا من التطبيق ولا يغني عن التخريج المتخصص في البحث العلمي.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference(
                id = "internal_hadith_library",
                kind = LearningReferenceKind.OTHER,
                citation = "مكتبة الحديث المدمجة في تطبيق Muslim",
                locator = "feature-hadith",
                note = "مرجع تقني لشرح وظيفة المكتبة، وليس مصدرًا جديدًا لتصحيح الأحاديث.",
            ),
        ),
    )
}
