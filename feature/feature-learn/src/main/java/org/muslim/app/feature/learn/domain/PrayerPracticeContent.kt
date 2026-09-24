package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object PrayerPracticeContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            prayerMethod,
            prayerPillars,
            rakahReference,
            prayerNullifiers,
            sujudSahw,
        )
    }

    private val prayerMethod = LearningLesson(
        id = "salah",
        titleRes = R.string.learn_topic_salah,
        subtitleRes = R.string.learn_topic_salah_sub,
        estimatedMinutes = 24,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "principle",
                title = "الأصل في صفة الصلاة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "قال النبي ﷺ: «صلوا كما رأيتموني أصلي». لذلك تُتعلم الصفة من السنة الثابتة مع التفريق بين الأركان والواجبات والسنن.",
                        referenceIds = listOf("bukhari_631"),
                    ),
                ),
            ),
            LearningSection(
                id = "opening",
                title = "من التكبير إلى القراءة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("القيام", "يقف القادر مستقبلا القبلة ومطمئنا، ويستحضر الصلاة التي يؤديها."),
                            LearningStepItem("تكبيرة الإحرام", "يقول «الله أكبر» للدخول في الصلاة، ويرفع يديه على الصفة المروية."),
                            LearningStepItem("دعاء الاستفتاح", "من السنن أن يقرأ دعاء استفتاح صحيحا، وتوجد صيغ متعددة ثابتة."),
                            LearningStepItem("الاستعاذة والبسملة", "يستعيذ بالله ثم يقرأ الفاتحة، وتوجد تفاصيل في الجهر بالبسملة."),
                            LearningStepItem("الفاتحة", "قراءة الفاتحة من أصول الصلاة، مع تفصيل حكم المأموم خلف الإمام بين الفقهاء."),
                            LearningStepItem("سورة بعد الفاتحة", "يقرأ ما تيسر في الركعتين الأوليين في المواضع المعتادة."),
                        ),
                    ),
                    LearningContentBlock.Evidence(
                        text = "ورد في صحيح البخاري أن من لم يقرأ بفاتحة الكتاب لا صلاة له، وهو أصل في مكانة الفاتحة.",
                        referenceIds = listOf("bukhari_756"),
                    ),
                ),
            ),
            LearningSection(
                id = "bowing",
                title = "الركوع والاعتدال",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الركوع", "يكبر ويركع حتى يستقر بدنه، ويضع يديه على ركبتيه على الهيئة المعتبرة."),
                            LearningStepItem("ذكر الركوع", "يقول من الأذكار الواردة مثل «سبحان ربي العظيم» دون اعتقاد أن عددا واحدا هو الصورة الوحيدة الصحيحة."),
                            LearningStepItem("الرفع", "يرفع من الركوع حتى يعتدل قائما ويأتي بالذكر المشروع للإمام والمنفرد والمأموم بحسب حاله."),
                            LearningStepItem("الطمأنينة", "لا ينتقل مباشرة من الركوع إلى السجود دون اعتدال واستقرار."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "prostration",
                title = "السجود والجلوس بين السجدتين",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("السجود الأول", "يسجد على الأعضاء المعروفة ويطمئن، مع تجنب إيذاء من بجانبه أو بسط الذراعين على الأرض."),
                            LearningStepItem("ذكر السجود", "يقول من الأذكار المأثورة مثل «سبحان ربي الأعلى»، ويكثر من الدعاء المشروع."),
                            LearningStepItem("الجلوس بين السجدتين", "يرفع ويجلس مطمئنا ويأتي بالدعاء الوارد."),
                            LearningStepItem("السجود الثاني", "يسجد ثانية ثم ينتقل إلى الركعة التالية على الصفة المشروعة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "tashahhud",
                title = "التشهد والتسليم",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("التشهد الأول", "في الصلاة الثلاثية والرباعية يجلس بعد الركعتين للتشهد الأول ثم يقوم."),
                            LearningStepItem("التشهد الأخير", "يجلس في آخر الصلاة ويقرأ التشهد والصلاة على النبي ﷺ."),
                            LearningStepItem("الدعاء قبل السلام", "يشرع الدعاء بالأدعية النبوية قبل التسليم."),
                            LearningStepItem("التسليم", "يخرج من الصلاة بالتسليم على الصفة المشروعة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "voice",
                title = "الجهر والإسرار",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يجهر الإمام والمنفرد في مواضع الجهر المعروفة من الفجر وأوليي المغرب والعشاء، ويُسر في الظهر والعصر وبقية المواضع. المأموم لا يحول الصلاة الجماعية إلى أصوات متنافسة.",
                    ),
                ),
            ),
            LearningSection(
                id = "differences",
                title = "هيئات متعددة صحيحة",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        intro = "بعض التفاصيل لا ينبغي أن تتحول إلى معيار للحكم على صلاة الآخرين.",
                        items = listOf(
                            LearningComparisonItem("موضع اليدين", "توجد أقوال معتبرة في موضع وضع اليدين حال القيام."),
                            LearningComparisonItem("رفع اليدين", "توجد تفاصيل في مواضع رفع اليدين عند الانتقال."),
                            LearningComparisonItem("جلسة التشهد", "تختلف بعض الهيئات في الافتراش والتورك ومحل الإشارة بالإصبع."),
                            LearningComparisonItem("القنوت", "له مواضع وأحكام مختلفة بحسب الصلاة والمذهب والنازلة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "mistakes",
                title = "أخطاء عملية شائعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("السرعة", "أخطر ما يفسد التعلم أداء الركوع والسجود بلا طمأنينة."),
                            LearningStepItem("مسابقة الإمام", "في الجماعة يتبع المأموم الإمام ولا يسبقه في الانتقالات."),
                            LearningStepItem("النظر للهاتف", "لا تجعل الإشعارات والمشتتات تقطع حضورك؛ فعّل وضعا هادئا إن احتجت."),
                            LearningStepItem("الهوس بالتفاصيل الصغيرة", "أتقن الأركان والواجبات أولا ثم توسع في السنن والهيئات."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_631", LearningReferenceKind.HADITH, "صحيح البخاري 631، كتاب الأذان", "Sahih al-Bukhari 631"),
            LearningReference("bukhari_756", LearningReferenceKind.HADITH, "صحيح البخاري 756، كتاب الأذان", "Sahih al-Bukhari 756"),
        ),
    )

    private val prayerPillars = LearningLesson(
        id = "salah_arkan",
        titleRes = R.string.learn_topic_salah_arkan,
        subtitleRes = R.string.learn_topic_salah_arkan_sub,
        estimatedMinutes = 18,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "framework",
                title = "لماذا نفرق بين الركن والواجب والسنة؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "هذا التقسيم يحدد ماذا يحدث إذا تُرك الفعل عمدا أو سهوا. لكن عدد الأركان والواجبات وطريقة تصنيف بعض الأفعال يختلف بين المذاهب، لذلك لا يعرض التطبيق قائمة مذهب واحد على أنها التقسيم الوحيد.",
                    ),
                ),
            ),
            LearningSection(
                id = "pillars",
                title = "الأركان",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("القيام مع القدرة", "في الفريضة للقادر، مع استثناءات المرض والعجز."),
                            LearningStepItem("تكبيرة الإحرام", "هي التكبير الذي يدخل به المصلي في الصلاة."),
                            LearningStepItem("القراءة الأساسية", "من أبرزها الفاتحة، مع تفصيل المأموم بين المذاهب."),
                            LearningStepItem("الركوع والاعتدال", "يشمل الركوع ثم الرفع والاستقرار."),
                            LearningStepItem("السجود والجلوس", "يشمل السجدتين والجلوس بينهما مع الطمأنينة."),
                            LearningStepItem("الختام", "التشهد الأخير وما يلحق به والتسليم بحسب التقسيم الفقهي."),
                            LearningStepItem("الترتيب والطمأنينة", "لا تتحول الصلاة إلى حركات غير مرتبة أو انتقالات بلا استقرار."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "obligations",
                title = "الواجبات والسنن",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("الواجب", "في بعض المذاهب يُجبر تركه سهوا بسجود السهو، لكن التقسيم نفسه يختلف من مذهب إلى آخر."),
                            LearningComparisonItem("السنة", "فعل مشروع يكمل الصلاة، وتركه لا يعامل كترك الركن."),
                            LearningComparisonItem("الهيئة", "بعض التفاصيل في وضع اليدين والنظر والجلسات من هيئات الصلاة التي تتسع فيها السنة والخلاف."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "forgotten",
                title = "إذا نسيت فعلا",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "لا تستخدم قاعدة واحدة لكل شيء",
                        body = "الركن المنسي لا يعالج دائما بالطريقة نفسها التي يعالج بها واجب أو سنة. إذا تذكرت أثناء الصلاة فالموضع الذي تذكرت فيه مهم لتحديد ما تفعل.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "learning_priority",
                title = "ترتيب التعلم",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("أولا", "احفظ الصفة الأساسية والفاتحة والأذكار الضرورية."),
                            LearningStepItem("ثانيا", "افهم ما لا تصح الصلاة بدونه."),
                            LearningStepItem("ثالثا", "تعلم السهو وكيفية تدارك الخلل."),
                            LearningStepItem("رابعا", "توسع في السنن والهيئات واختلاف المذاهب."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val rakahReference = LearningLesson(
        id = "rakats",
        titleRes = R.string.learn_topic_rakats,
        subtitleRes = R.string.learn_topic_rakats_sub,
        estimatedMinutes = 8,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "obligatory",
                title = "الفرائض اليومية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الفجر", "ركعتان فرضا."),
                            LearningStepItem("الظهر", "أربع ركعات فرضا."),
                            LearningStepItem("العصر", "أربع ركعات فرضا."),
                            LearningStepItem("المغرب", "ثلاث ركعات فرضا."),
                            LearningStepItem("العشاء", "أربع ركعات فرضا."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "structure",
                title = "كيف تتغير القراءة والجلسات؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الصلاة الثنائية تنتهي بعد الركعتين. الثلاثية فيها تشهد بعد الثانية ثم ركعة ثالثة. الرباعية فيها تشهد بعد الثانية ثم ركعتان إضافيتان وتشهد أخير. تفاصيل القراءة بعد الركعتين الأوليين تتبع الصفة الفقهية المعروفة.",
                    ),
                ),
            ),
            LearningSection(
                id = "friday_travel",
                title = "الجمعة والسفر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "صلاة الجمعة ركعتان بعد الخطبة لمن تلزمه وتصح منه. والمسافر قد يقصر الصلوات الرباعية إلى ركعتين عند تحقق شروط القصر، بينما الفجر والمغرب لا يقصران.",
                    ),
                ),
            ),
            LearningSection(
                id = "avoid_confusion",
                title = "لا تخلط الفرض بالراتبة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "عدد ركعات الفريضة ثابت، أما السنن الرواتب والوتر والضحى وقيام الليل فلها باب مستقل. عرض التطبيق للنوافل بجوار الفريضة لا يجعلها جزءا من عدد الفرض.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
    )

    private val prayerNullifiers = LearningLesson(
        id = "nullifiers",
        titleRes = R.string.learn_topic_nullifiers,
        subtitleRes = R.string.learn_topic_nullifiers_sub,
        estimatedMinutes = 13,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "prayer_breakers",
                title = "ما يبطل الصلاة في الجملة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("انتقاض الطهارة", "إذا حصل ناقض متيقن للوضوء أثناء الصلاة تنقطع الطهارة المطلوبة."),
                            LearningStepItem("الكلام العمد لغير مصلحة الصلاة", "الكلام البشري المقصود خارج أذكار الصلاة له حكم معروف في إبطالها."),
                            LearningStepItem("الأكل والشرب عمدا", "ليس من أفعال الصلاة المعتادة ويؤثر في صحتها."),
                            LearningStepItem("ترك ركن عمدا", "تعمد إسقاط ركن لا يعامل كسهو يسير."),
                            LearningStepItem("الحركة الكثيرة المنفصلة عن الصلاة", "الحركة الخارجة عن هيئة الصلاة من المسائل التي يراعى فيها العرف والحاجة والتتابع."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "does_not_break",
                title = "أفعال لا تعني البطلان تلقائيا",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الحركة اليسيرة", "الحاجة إلى تعديل اللباس أو حمل طفل أو دفع أذى لا تعني بطلان الصلاة بمجرد الحركة."),
                            LearningStepItem("البكاء", "البكاء من خشية الله أو لألم لا يعامل ببساطة كالكلام العمد."),
                            LearningStepItem("الشك العارض", "مجرد خاطر أو شك لا يساوي يقين الحدث أو يقين ترك الركن."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "wudu_link",
                title = "نواقض الوضوء",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تفاصيل نواقض الوضوء موجودة في درس مستقل ضمن مسار الطهارة. الربط هنا مهم لأن الصلاة تعتمد على استمرار الطهارة.",
                    ),
                ),
            ),
            LearningSection(
                id = "uncertain",
                title = "إذا لم تكن متأكدا",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "لا تقطع الصلاة بسبب احتمال ضعيف أو إحساس متكرر غير متيقن. المسائل المتعلقة بالوسواس تحتاج تطبيق قاعدة اليقين وعدم الاسترسال مع الشك.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
    )

    private val sujudSahw = LearningLesson(
        id = "sujud_sahw",
        titleRes = R.string.learn_topic_sujud_sahw,
        subtitleRes = R.string.learn_topic_sujud_sahw_sub,
        estimatedMinutes = 15,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "why",
                title = "لماذا يشرع سجود السهو؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "سجود السهو وسيلة شرعية لجبر أنواع من الخلل غير المقصود في الصلاة. أشهر أسبابه الزيادة أو النقص أو الشك، لكن الحكم يتغير بحسب ما وقع وبحسب المذهب.",
                    ),
                ),
            ),
            LearningSection(
                id = "cases",
                title = "الحالات الكبرى",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("زيادة سهوا", "مثل زيادة قيام أو ركعة أو فعل من جنس الصلاة دون قصد."),
                            LearningStepItem("نقص سهوا", "مثل نسيان واجب عند من يصنفه واجبا يجبر بالسجود."),
                            LearningStepItem("الشك", "يبني المصلي على اليقين أو غالب الظن وفق التفصيل الفقهي والحالة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "before_after",
                title = "قبل السلام أم بعده؟",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "موضع خلاف وتفصيل",
                        body = "ورد السجود قبل السلام وبعده في السنة، وتختلف المذاهب في ترتيب الحالات على هذين الموضعين. الأفضل أن يتعلم المسلم طريقة مذهبه أو فتوى الجهة التي يرجع إليها بدلا من خلط القواعد.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "missing_pillar",
                title = "نسيان ركن ليس مجرد سجود سهو",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "إذا تُرك ركن من الصلاة فغالبا يحتاج إلى الإتيان به وبما بعده بحسب موضع التذكر، وليس مجرد سجدتين في النهاية. لهذا ينبغي تحديد ما الذي نُسي ومتى تذكره المصلي.",
                    ),
                ),
            ),
            LearningSection(
                id = "frequent_doubt",
                title = "الشك المتكرر والوسواس",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "من ابتلي بشك متكرر لا يعامل كل خاطر كحادثة جديدة؛ لأن ذلك يفتح باب الوسواس. يحتاج إلى قاعدة مستقرة يتبعها وألا يعيد الصلاة بلا يقين معتبر.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
        ),
    )
}
