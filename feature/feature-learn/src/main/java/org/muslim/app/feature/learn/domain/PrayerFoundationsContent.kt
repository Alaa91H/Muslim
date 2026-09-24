package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object PrayerFoundationsContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            prayerOverview,
            prayerConditions,
            prayerTimes,
            adhanAndIqamah,
            qiblaAndIntention,
        )
    }

    private val prayerOverview = LearningLesson(
        id = "prayer_intro",
        titleRes = R.string.learn_topic_prayer_intro,
        subtitleRes = R.string.learn_topic_prayer_intro_sub,
        estimatedMinutes = 10,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "place",
                title = "مكانة الصلاة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الصلاة عبادة يومية متكررة تربط المسلم بربه، وهي من أركان الإسلام. هذا المسار لا يكتفي بتعليم الحركات؛ بل يشرح ما قبل الصلاة، صفتها، ما يصححها وما يبطلها، وأحكام الجماعة والسفر والمرض والجمعة والنوافل.",
                    ),
                    LearningContentBlock.Evidence(
                        heading = "الوقت جزء من العبادة",
                        text = "قرر القرآن أن الصلاة مكتوبة على المؤمنين في أوقات محددة، ولذلك يبدأ تعلم الصلاة بمعرفة الوقت والطهارة والقبلة والشروط.",
                        referenceIds = listOf("quran_4_103"),
                    ),
                ),
            ),
            LearningSection(
                id = "map",
                title = "خريطة مسار الصلاة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("قبل الصلاة", "تعلم الوقت والطهارة وستر العورة والقبلة والنية والأذان والإقامة."),
                            LearningStepItem("داخل الصلاة", "تعلم القيام والقراءة والركوع والاعتدال والسجود والجلوس والتشهد والتسليم."),
                            LearningStepItem("الخلل والسهو", "ميز بين الركن والواجب والسنة، وتعلم متى يحتاج الخلل إلى تدارك أو سجود سهو."),
                            LearningStepItem("الصلاة مع الآخرين", "تعلم الجماعة والإمامة والصفوف والجمعة."),
                            LearningStepItem("الأحوال الخاصة", "تعلم صلاة المسافر والمريض والنوافل والصلوات ذات الأسباب."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "method",
                title = "كيف تتعلم الصفة؟",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "المرجع العملي الجامع هو قول النبي ﷺ: «صلوا كما رأيتموني أصلي»، وهو أصل مهم في تعلم هيئة الصلاة.",
                        referenceIds = listOf("bukhari_631"),
                    ),
                    LearningContentBlock.Callout(
                        title = "الاختلاف في بعض الهيئات",
                        body = "توجد فروق معتبرة بين المذاهب في بعض السنن والهيئات، مثل موضع اليدين وبعض صيغ الأذكار والقنوت. لا يجعل التطبيق هذه الفروق سببا للطعن في صحة صلاة المسلم.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_4_103", LearningReferenceKind.QURAN, "القرآن الكريم، سورة النساء، الآية 4:103", "Quran 4:103"),
            LearningReference("bukhari_631", LearningReferenceKind.HADITH, "صحيح البخاري 631، كتاب الأذان", "Sahih al-Bukhari 631"),
        ),
    )

    private val prayerConditions = LearningLesson(
        id = "shurut",
        titleRes = R.string.learn_topic_shurut,
        subtitleRes = R.string.learn_topic_shurut_sub,
        estimatedMinutes = 15,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "ما معنى الشرط؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الشرط يسبق الصلاة أو يصاحبها ويؤثر في صحتها. يختلف عن الركن الذي يكون جزءا من الصلاة نفسها. فهم هذا الفرق يمنع الخلط بين نسيان فعل داخل الصلاة وبين الدخول فيها مع فقد شرط.",
                    ),
                ),
            ),
            LearningSection(
                id = "conditions",
                title = "الشروط الأساسية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الإسلام والعقل والتمييز", "هذه من شروط التكليف أو الصحة بحسب التفصيل الفقهي."),
                            LearningStepItem("دخول الوقت", "لا تؤدى الفريضة قبل دخول وقتها إلا في صور الجمع المشروعة بضوابطها."),
                            LearningStepItem("الطهارة", "يرفع الحدث بالوضوء أو الغسل أو التيمم عند تحقق سببه، وتزال النجاسة بحسب القدرة."),
                            LearningStepItem("ستر العورة", "يلبس المصلي ما يحقق الستر المعتبر، مع اختلاف بعض التفاصيل بين المذاهب."),
                            LearningStepItem("استقبال القبلة", "يتوجه إلى الكعبة مع القدرة، وللعاجز أو الخائف أو المسافر صور خاصة."),
                            LearningStepItem("النية", "يعين بقلبه الصلاة التي يريد أداءها، ولا يحتاج إلى تحويل النية إلى طقس لفظي متكرر."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "incapacity",
                title = "العجز لا يعني ترك الصلاة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "إذا عجز المصلي عن بعض الشروط عجزا حقيقيا، فله أحكام بحسب استطاعته. لا يؤخر الصلاة تلقائيا إلى خروج الوقت لمجرد عدم قدرته على الصورة الكاملة.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "common_mistakes",
                title = "أخطاء قبل التكبير",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الصلاة قبل الوقت", "الاعتماد على وقت تقريبي دون تحقق قد يؤدي إلى أداء الفرض قبل دخوله."),
                            LearningStepItem("إهمال نجاسة معلومة", "إذا تيقن المصلي نجاسة مؤثرة وأمكنه إزالتها فعليه معالجتها قبل الصلاة."),
                            LearningStepItem("الوسوسة في النية", "مجرد قيامك للصلاة المعينة مع قصدها يكفي في أصل النية، فلا تكررها بسبب الشك."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "faq",
                title = "أسئلة تطبيقية",
                blocks = listOf(
                    LearningContentBlock.QuestionAnswer(
                        question = "اكتشفت بعد الصلاة أن اتجاه القبلة كان غير دقيق، ماذا أفعل؟",
                        answer = "يختلف الحكم بحسب قدرتك على التحري، ومقدار الانحراف، وهل كان الخطأ مع اجتهاد معتبر أو مع ترك الاستطاعة. الحالات غير الواضحة تُعرض على جهة علمية موثوقة.",
                    ),
                ),
            ),
        ),
    )

    private val prayerTimes = LearningLesson(
        id = "salah_times",
        titleRes = R.string.learn_topic_salah_times,
        subtitleRes = R.string.learn_topic_salah_times_sub,
        estimatedMinutes = 16,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "principle",
                title = "الأصل في المواقيت",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ثبت في صحيح مسلم بيان حدود أوقات الصلوات الخمس من الفجر إلى العشاء.",
                        referenceIds = listOf("muslim_612b"),
                    ),
                    LearningContentBlock.Callout(
                        title = "التطبيق والحساب",
                        body = "التطبيق يحسب المواقيت فلكيا وفق الإعدادات المختارة. الحساب وسيلة لتقدير العلامات الشرعية، وقد تختلف مناهج الزوايا وخطوط العرض العالية بين الجهات.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "five_times",
                title = "الصلوات الخمس",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الفجر", "يبدأ بطلوع الفجر الصادق وينتهي بطلوع الشمس."),
                            LearningStepItem("الظهر", "يبدأ بزوال الشمس عن وسط السماء ويمتد إلى دخول العصر وفق علامته الفقهية."),
                            LearningStepItem("العصر", "يبدأ بعد انتهاء وقت الظهر، وتوجد تفاصيل مذهبية في بداية الوقت."),
                            LearningStepItem("المغرب", "يبدأ بغروب قرص الشمس وينتهي بمغيب الشفق على التفصيل المعروف."),
                            LearningStepItem("العشاء", "يبدأ بعد مغيب الشفق، وحد الاختيار المشهور إلى نصف الليل في حديث المواقيت."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "schools",
                title = "فروق فقهية وحسابية",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        intro = "الاختلاف لا يعني أن الوقت عشوائي؛ بل توجد علامات شرعية مع اختلاف في بعض الحدود وكيفية تنزيلها.",
                        items = listOf(
                            LearningComparisonItem("بداية العصر", "المشهور اختلاف الحنفية عن جمهور الفقهاء في علامة دخول العصر المتعلقة بطول الظل."),
                            LearningComparisonItem("الفجر والعشاء", "الزوايا الفلكية المستخدمة في التقاويم الحديثة تختلف بين الهيئات، خصوصا في المناطق ذات الشفق الطويل."),
                            LearningComparisonItem("خطوط العرض العالية", "قد تغيب بعض العلامات في أجزاء من السنة، فتستعمل جهات علمية طرق تقدير مختلفة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "missed",
                title = "النوم والنسيان والتأخير",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ينبغي أداء الصلاة في وقتها. أما النوم أو النسيان أو الأعذار الطارئة فلها أحكامها، ولا ينبغي تحويل الرخصة إلى عادة لتأخير الصلاة عن وقتها.",
                    ),
                ),
            ),
            LearningSection(
                id = "practical",
                title = "استخدام التطبيق",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اختر طريقة الحساب", "استخدم الجهة أو المنهج المناسب لبلدك أو مسجدك."),
                            LearningStepItem("تحقق من الموقع والمنطقة الزمنية", "الخطأ فيهما يغيّر وقت الصلاة حتى لو كانت المعادلة صحيحة."),
                            LearningStepItem("قارن عند الانتقال", "في السفر إلى بلد جديد راجع تقويم المسجد المحلي عند وجود فرق ملحوظ."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("muslim_612b", LearningReferenceKind.HADITH, "صحيح مسلم 612b، كتاب المساجد ومواضع الصلاة", "Sahih Muslim 612b"),
        ),
    )

    private val adhanAndIqamah = LearningLesson(
        id = "adhan",
        titleRes = R.string.learn_topic_adhan,
        subtitleRes = R.string.learn_topic_adhan_sub,
        estimatedMinutes = 14,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "purpose",
                title = "وظيفة الأذان والإقامة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الأذان إعلان بدخول وقت الصلاة ودعوة إليها، والإقامة إعلان بالقيام إلى الفريضة. وهما من شعائر الصلاة الظاهرة، ولهما صيغ ثابتة مع فروق فقهية محدودة في بعض التفاصيل.",
                    ),
                    LearningContentBlock.Evidence(
                        text = "حديث مالك بن الحويرث يجمع بين تعلم الصلاة والأذان والإمامة: إذا حضرت الصلاة يؤذن أحد الجماعة ويؤمهم من هو أولى وفق الضوابط.",
                        referenceIds = listOf("bukhari_631"),
                    ),
                ),
            ),
            LearningSection(
                id = "respond",
                title = "ماذا يفعل من يسمع الأذان؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("استمع وأجب", "يشرع أن تقول مثل ما يقول المؤذن، وتأتي بالذكر الوارد عند الحيعلتين."),
                            LearningStepItem("صل على النبي ﷺ", "بعد انتهاء الأذان تأتي بالصلاة على النبي ﷺ."),
                            LearningStepItem("ادع بالدعاء الوارد", "يسن سؤال الله للنبي ﷺ الوسيلة والفضيلة والمقام المحمود."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "fajr",
                title = "أذان الفجر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "من خصائص أذان الفجر التثويب بقول «الصلاة خير من النوم» في موضعه المعروف، مع فروق تفصيلية بين صيغ الأذان في المذاهب.",
                    ),
                ),
            ),
            LearningSection(
                id = "home",
                title = "الفرد والجماعة الصغيرة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "وجود تطبيق يؤذن لا يحول الإشعار الإلكتروني إلى أذان جماعة من إنسان. عند إقامة جماعة صغيرة تُراعى أحكام الأذان والإقامة المعروفة، ولا يعتمد على صوت الهاتف بديلا آليا في كل حكم.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_631", LearningReferenceKind.HADITH, "صحيح البخاري 631، كتاب الأذان", "Sahih al-Bukhari 631"),
        ),
    )

    private val qiblaAndIntention = LearningLesson(
        id = "qibla_niyyah",
        titleRes = R.string.learn_topic_qibla_niyyah,
        subtitleRes = R.string.learn_topic_qibla_niyyah_sub,
        estimatedMinutes = 10,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "qibla",
                title = "استقبال القبلة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن بالتوجه نحو المسجد الحرام في الصلاة، وهو أصل استقبال القبلة لمن قدر عليه.",
                        referenceIds = listOf("quran_2_144"),
                    ),
                    LearningContentBlock.Paragraph(
                        "البعيد عن مكة يتحرى جهة الكعبة بوسائل معتبرة، ولا يُطلب منه إصابة نقطة هندسية مستحيلة مع كل حركة بسيطة. البوصلة أداة مساعدة وتتأثر بالمعادن والمجالات المغناطيسية.",
                    ),
                ),
            ),
            LearningSection(
                id = "intention",
                title = "النية",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "محل النية القلب: تعرف أي صلاة تؤدي ولماذا تقوم لها. المقصود القصد الحقيقي، لا تكرار عبارة بصوت مرتفع حتى تشعر بدرجة معينة من اليقين.",
                    ),
                    LearningContentBlock.Callout(
                        title = "مقاومة الوسوسة",
                        body = "إذا قمت لصلاة الظهر مثلا وأنت تعلم أنك ستصلي الظهر، فلا تجعل إعادة النية اللفظية شرطا لبدء الصلاة.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "exceptions",
                title = "العجز والسفر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "من عجز عن معرفة القبلة بعد بذل وسعه، أو عجز عن استقبالها لمرض أو خوف أو ظرف نقل خاص، فله تفصيل فقهي بحسب حاله. يُجمع بين الاستطاعة والمحافظة على وقت الصلاة.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_144", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:144", "Quran 2:144"),
        ),
    )
}
