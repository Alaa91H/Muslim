package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FastingCoreContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            fastingOverview,
            fastingDay,
            fastingNullifiers,
            fastingExemptions,
            fastingWomen,
        )
    }

    private val fastingOverview = LearningLesson(
        id = "fasting",
        titleRes = R.string.learn_topic_fasting,
        subtitleRes = R.string.learn_topic_fasting_sub,
        estimatedMinutes = 16,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "ما الصيام؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الصيام عبادة تقوم على الإمساك عن المفطرات من طلوع الفجر الصادق إلى غروب الشمس بنية العبادة. وصيام رمضان فريضة، وله أحكام في النية والوقت والمفطرات والأعذار والقضاء والفدية والكفارة.",
                    ),
                    LearningContentBlock.Evidence(
                        heading = "أصل الفريضة",
                        text = "فرض القرآن الصيام على المؤمنين وبيّن أن من مقاصده التقوى.",
                        referenceIds = listOf("quran_2_183"),
                    ),
                ),
            ),
            LearningSection(
                id = "ramadan",
                title = "رمضان",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "بيّن القرآن أن رمضان هو الشهر الذي أنزل فيه القرآن، وأمر من شهد الشهر بصيامه، مع رخصة المرض والسفر وقضاء أيام أخر.",
                        referenceIds = listOf("quran_2_185"),
                    ),
                    LearningContentBlock.Callout(
                        title = "الرؤية والتقويم",
                        body = "دخول رمضان والعيد مرتبط بإثبات الشهر لدى الجهة التي يعتمدها المسلم في بلده أو مجتمعه. لا يحول التطبيق اختلاف طرق الإثبات إلى قرار شخصي ملزم.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "pillars",
                title = "أركان الصيام العملية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("النية", "يقصد المسلم الصيام لله، وتفاصيل وقت النية وتجديدها تختلف بين الفرض والنفل وبين المذاهب."),
                            LearningStepItem("بداية الإمساك", "يبدأ الصوم عند طلوع الفجر الصادق، لا عند منتصف الليل ولا بمجرد انتهاء السحور."),
                            LearningStepItem("الإمساك عن المفطرات", "يمتنع عمدا عن الأكل والشرب والجماع وسائر المفطرات المعتبرة."),
                            LearningStepItem("نهاية اليوم", "ينتهي الصوم بغروب الشمس، ثم يشرع الفطر."),
                        ),
                    ),
                    LearningContentBlock.Evidence(
                        text = "تجمع آية البقرة 2:187 حد بداية الإمساك عند الفجر وإتمام الصيام إلى الليل.",
                        referenceIds = listOf("quran_2_187"),
                    ),
                ),
            ),
            LearningSection(
                id = "ethics",
                title = "الصيام ليس جوعا فقط",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الغاية التعليمية من الصيام تشمل ضبط اللسان والسلوك والابتعاد عن الظلم والخصومة. صحة الصوم الفقهية باب، وكمال الأجر وحفظ الأخلاق باب آخر؛ فلا ينبغي اختزال رمضان في الامتناع عن الطعام.",
                    ),
                ),
            ),
            LearningSection(
                id = "learning_map",
                title = "خريطة هذا المسار",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اليوم الصائم", "تعلم الفجر والسحور والإمساك والإفطار."),
                            LearningStepItem("المفطرات", "ميز بين العمد والنسيان والخطأ وما لا يفسد الصوم."),
                            LearningStepItem("الأعذار", "المرض والسفر والعجز والحمل والرضاعة والحيض والنفاس."),
                            LearningStepItem("ما بعد الفوات", "القضاء والفدية والكفارة بحسب سبب الفطر."),
                            LearningStepItem("رمضان والنافلة", "ليلة القدر والاعتكاف وصيام التطوع والأيام المنهي عنها."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_183", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:183", "Quran 2:183"),
            LearningReference("quran_2_185", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:185", "Quran 2:185"),
            LearningReference("quran_2_187", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:187", "Quran 2:187"),
        ),
    )

    private val fastingDay = LearningLesson(
        id = "fasting_day",
        titleRes = R.string.learn_topic_fasting_day,
        subtitleRes = R.string.learn_topic_fasting_day_sub,
        estimatedMinutes = 13,
        sections = listOf(
            LearningSection(
                id = "night",
                title = "من الليل إلى الفجر",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("النية", "يعقد نية صيام الفرض في الوقت المعتبر عند مذهبه، ويبتعد عن تكرارها الوسواسي."),
                            LearningStepItem("السحور", "يتسحر ولو بالقليل إذا تيسر، ولا يظن أن ترك السحور يبطل الصوم."),
                            LearningStepItem("الفجر", "يتوقف عن الأكل والشرب عند تحقق دخول الفجر الصادق."),
                        ),
                    ),
                    LearningContentBlock.Evidence(
                        text = "ورد في صحيح البخاري الحث على السحور وبيان أن فيه بركة.",
                        referenceIds = listOf("bukhari_1923"),
                    ),
                ),
            ),
            LearningSection(
                id = "day",
                title = "أثناء النهار",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يحفظ الصائم صومه من المفطرات، ويستمر في أعماله المعتادة بما يناسب طاقته. المرض وأعراض الجفاف أو انخفاض السكر ونحوها لها جانب طبي يجب ألا يُهمَل.",
                    ),
                    LearningContentBlock.Callout(
                        title = "السلامة الصحية",
                        body = "إذا ظهرت أعراض خطيرة كالإغماء أو ارتباك الوعي أو علامات جفاف شديد، فاطلب المساعدة الطبية المناسبة. حكم إكمال الصوم أو الفطر عند الضرر يراعى مع التقييم الصحي والفتوى الموثوقة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "iftar",
                title = "الإفطار",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ثبت في صحيح البخاري استحباب تعجيل الفطر بعد تحقق غروب الشمس.",
                        referenceIds = listOf("bukhari_1957"),
                    ),
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("تحقق من الغروب", "لا يفطر قبل الوقت بسبب الجوع أو توقيت غير موثوق."),
                            LearningStepItem("ابدأ باعتدال", "الإفطار عبادة وليس سببا للإسراف أو الإضرار بالجسم."),
                            LearningStepItem("صل المغرب في وقتها", "نظم الطعام بحيث لا يؤدي الإفطار إلى تضييع الصلاة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "mistakes",
                title = "أخطاء شائعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الإمساك المبكر جدا", "لا تجعل دقائق طويلة قبل الفجر جزءا واجبا من الصوم."),
                            LearningStepItem("الشك في كل شيء", "لا تعتبر طعم المعجون أو غبار الطريق أو كل إحساس في الحلق مفطرا تلقائيا."),
                            LearningStepItem("الإفراط في الطعام", "الإسراف قد يضيع مقاصد الصيام ويضر بالصحة."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_1923", LearningReferenceKind.HADITH, "صحيح البخاري 1923، كتاب الصوم", "Sahih al-Bukhari 1923"),
            LearningReference("bukhari_1957", LearningReferenceKind.HADITH, "صحيح البخاري 1957، كتاب الصوم", "Sahih al-Bukhari 1957"),
        ),
    )

    private val fastingNullifiers = LearningLesson(
        id = "fasting_nullifiers",
        titleRes = R.string.learn_topic_fasting_nullifiers,
        subtitleRes = R.string.learn_topic_fasting_nullifiers_sub,
        estimatedMinutes = 18,
        sections = listOf(
            LearningSection(
                id = "clear",
                title = "مفطرات واضحة في الجملة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الأكل والشرب عمدا", "تناول الطعام أو الشراب عمدا في نهار الصيام يفسد الصوم."),
                            LearningStepItem("الجماع عمدا", "من أعظم مفطرات صيام رمضان، وله أحكام خاصة في القضاء والكفارة."),
                            LearningStepItem("الحيض والنفاس", "إذا بدأ الدم المعتبر قبل الغروب لا يصح صوم ذلك اليوم، ويقضى بعد الطهر."),
                            LearningStepItem("القيء المتعمد", "له حكم معروف في كتب الفقه مع التفريق بين الاستقاء المتعمد وما غلب الإنسان."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "forgetting",
                title = "النسيان والخطأ",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "من أكل أو شرب ناسيا وهو صائم يتم صومه؛ جاء ذلك صريحا في صحيح البخاري.",
                        referenceIds = listOf("bukhari_1933"),
                    ),
                    LearningContentBlock.Callout(
                        body = "النسيان ليس كالعمد. عند تذكر الصوم يتوقف فورا ويكمل يومه.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "modern",
                title = "الأدوية والإجراءات الحديثة",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        intro = "الحقن والبخاخات والقطرات والغسيل الكلوي والمناظير والتغذية الطبية مسائل معاصرة تختلف صورها وأحكامها؛ لا يصح جمعها تحت قاعدة واحدة.",
                        items = listOf(
                            LearningComparisonItem("دواء غير غذائي", "يُنظر إلى طريق دخوله وطبيعته وهل يقوم مقام الغذاء أو يصل إلى الجوف على الصورة المعتبرة."),
                            LearningComparisonItem("الإجراءات الطبية", "قد يجتمع فيها الحكم الشرعي مع ضرورة صحية لا يجوز تجاهلها."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "sexual",
                title = "الجماع والكفارة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "في صحيح البخاري قصة من جامع زوجته في نهار رمضان، وذُكرت له مراتب الكفارة المعروفة.",
                        referenceIds = listOf("bukhari_1936"),
                    ),
                    LearningContentBlock.Callout(
                        title = "لا تعمم الكفارة",
                        body = "الكفارة المغلظة لا تلحق بكل سبب من أسباب فساد الصوم بالطريقة نفسها. تفاصيل القضاء والكفارة تختلف بحسب السبب والعمد والقدرة والمذهب.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "doubt",
                title = "إذا شككت هل فسد الصوم",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "لا تنتقل من صحة الصوم إلى الحكم بفساده بسبب احتمال ضعيف. حدد الواقعة الفعلية: ماذا دخل؟ كيف؟ وهل كان عمدا؟ ثم ارجع إلى حكم موثوق إذا كانت الصورة غير واضحة.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_1933", LearningReferenceKind.HADITH, "صحيح البخاري 1933، كتاب الصوم", "Sahih al-Bukhari 1933"),
            LearningReference("bukhari_1936", LearningReferenceKind.HADITH, "صحيح البخاري 1936، كتاب الصوم", "Sahih al-Bukhari 1936"),
        ),
    )

    private val fastingExemptions = LearningLesson(
        id = "fasting_exemptions",
        titleRes = R.string.learn_topic_fasting_exemptions,
        subtitleRes = R.string.learn_topic_fasting_exemptions_sub,
        estimatedMinutes = 17,
        sections = listOf(
            LearningSection(
                id = "quran",
                title = "الرخصة في القرآن",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "نص القرآن على رخصة المريض والمسافر وأن عليهما عدة من أيام أخر، مع التأكيد على إرادة اليسر.",
                        referenceIds = listOf("quran_2_184_185"),
                    ),
                ),
            ),
            LearningSection(
                id = "groups",
                title = "أهم الأعذار",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("المرض", "إذا كان الصوم يسبب ضررا أو مشقة غير معتادة، ينظر في الرخصة والقضاء بعد القدرة."),
                            LearningStepItem("السفر", "للمسافر رخصة الفطر عند تحقق وصف السفر، وله أن يصوم في أحوال بحسب القدرة والتفصيل."),
                            LearningStepItem("العجز المزمن", "من لا يرجى قدرته على الصوم له باب الفدية وفق شروطه."),
                            LearningStepItem("الحمل والرضاعة", "الحكم يتعلق بالخوف على الأم أو الطفل، وتفاصيل القضاء والفدية فيها اختلاف فقهي."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "qada_fidya",
                title = "القضاء والفدية",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        items = listOf(
                            LearningComparisonItem("القضاء", "تعويض يوم الصيام بيوم لاحق، وهو الأصل في الفطر المؤقت الذي يزول سببه."),
                            LearningComparisonItem("الفدية", "إطعام محتاج عن اليوم في صور العجز الدائم وبعض الصور المختلف فيها."),
                            LearningComparisonItem("الكفارة", "عقوبة تعبدية مخصوصة لبعض الانتهاكات، ولا تساوي القضاء أو الفدية."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "medical",
                title = "تقييم الضرر",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الأمراض المزمنة، الحمل، السكري، أمراض الكلى والأدوية المنتظمة تحتاج قرارا صحيا فرديا. الطبيب يحدد الخطر الطبي، والجهة العلمية تطبق الحكم الشرعي على الحالة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_184_185", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآيتان 2:184-185", "Quran 2:184-185"),
        ),
    )

    private val fastingWomen = LearningLesson(
        id = "fasting_women",
        titleRes = R.string.learn_topic_fasting_women,
        subtitleRes = R.string.learn_topic_fasting_women_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "menstruation",
                title = "الحيض والنفاس",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ثبت عن عائشة رضي الله عنها أن الحائض تقضي الصيام بعد الطهر ولا تقضي الصلوات التي فاتتها زمن الحيض.",
                        referenceIds = listOf("muslim_335c"),
                    ),
                    LearningContentBlock.Paragraph(
                        "إذا بدأ الحيض أو النفاس قبل غروب الشمس فاليوم يحتاج إلى قضاء. وإذا تحقق الطهر في الليل قبل الفجر تدخل المرأة اليوم الجديد بحكم الطاهرات بعد مراعاة الغسل للصلاة.",
                    ),
                ),
            ),
            LearningSection(
                id = "pregnancy",
                title = "الحمل والرضاعة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الحامل والمرضع قد تحتاجان الفطر إذا خافتا ضررا معتبرا على النفس أو الطفل. تقدير الخطر الطبي لا يعتمد على الشعور وحده في الحالات المعقدة.",
                    ),
                    LearningContentBlock.Callout(
                        title = "تفصيل فقهي",
                        body = "هل يلزم القضاء وحده أو تلزم معه فدية في بعض صور الخوف على الطفل؟ هذه من المسائل التي تختلف فيها المذاهب، لذلك لا يعطي التطبيق حكما واحدا لكل الحالات.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "medication",
                title = "الأدوية والصحة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الحمل والرضاعة والنزف غير المعتاد حالات صحية حساسة. لا تغيري جرعات الأدوية أو توقيتها من أجل الصيام دون توجيه طبي مناسب.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "tracking",
                title = "تسجيل الأيام",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "من المفيد الاحتفاظ بعدد الأيام التي تحتاج إلى قضاء دون تسجيل تفاصيل صحية خاصة لا حاجة لها. الهدف تنظيم القضاء لا إنشاء ملف طبي داخل قسم التعلم.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("muslim_335c", LearningReferenceKind.HADITH, "صحيح مسلم 335c، كتاب الحيض", "Sahih Muslim 335c"),
        ),
    )
}
