package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object PrayerSituationsContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            congregationAndImamah,
            rawatib,
            travelerPrayer,
            sickPrayer,
            jumuah,
            specialPrayers,
        )
    }

    private val congregationAndImamah = LearningLesson(
        id = "congregation_imamah",
        titleRes = R.string.learn_topic_congregation_imamah,
        subtitleRes = R.string.learn_topic_congregation_imamah_sub,
        estimatedMinutes = 16,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "following",
                title = "متابعة الإمام",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ثبت أن الإمام جُعل ليؤتم به، فيركع المأموم بعد ركوعه ويسجد بعد سجوده ولا يسبقه.",
                        referenceIds = listOf("bukhari_722"),
                    ),
                    LearningContentBlock.Callout(
                        title = "المسابقة",
                        body = "المأموم لا يحول الصلاة إلى سباق مع الإمام. المقصود المتابعة المنظمة لا التأخر الطويل ولا السبق.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "rows",
                title = "الصفوف والجماعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("أكمل الصفوف", "يتقدم المصلون لإكمال الصفوف بطريقة لا تسبب مزاحمة أو أذى."),
                            LearningStepItem("استووا باعتدال", "تسوية الصف مقصد من مقاصد الجماعة، ولا تحتاج إلى إيذاء القدمين أو المبالغة في التلاصق."),
                            LearningStepItem("لا تمر أمام المصلين بلا حاجة", "احترم سترتهم ومساحة الصلاة خصوصا في المساجد المزدحمة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "latecomer",
                title = "المسبوق",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "من دخل والإمام في الصلاة يدخل معه في الحالة التي هو عليها، ثم يقضي ما فاته بعد سلام الإمام حسب التفصيل الفقهي. إدراك الركعة وتحديد ما يقضى من المسائل التي ينبغي تعلمها من مذهب معتبر.",
                    ),
                ),
            ),
            LearningSection(
                id = "imam",
                title = "الإمامة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الإمامة مسؤولية في صحة الصلاة وراحة الجماعة. يُراعى العلم بما تحتاجه الصلاة، وحال المصلين، وعدم الإطالة التي تشق عليهم دون سبب.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_722", LearningReferenceKind.HADITH, "صحيح البخاري 722، كتاب الأذان", "Sahih al-Bukhari 722"),
        ),
    )

    private val rawatib = LearningLesson(
        id = "rawatib",
        titleRes = R.string.learn_topic_rawatib,
        subtitleRes = R.string.learn_topic_rawatib_sub,
        estimatedMinutes = 11,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "definition",
                title = "ما السنن الرواتب؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "هي نوافل مرتبطة بالفرائض قبلها أو بعدها. المقصود منها زيادة القرب من الله وجبر النقص وتنظيم عبادة يومية ثابتة، وليست جزءا من عدد ركعات الفرض.",
                    ),
                ),
            ),
            LearningSection(
                id = "daily_pattern",
                title = "النمط اليومي المشهور",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("قبل الفجر", "ركعتا سنة الفجر من آكد الرواتب."),
                            LearningStepItem("حول الظهر", "وردت رواتب قبل الظهر وبعده بصور متعددة صحيحة."),
                            LearningStepItem("بعد المغرب", "ركعتان بعد المغرب."),
                            LearningStepItem("بعد العشاء", "ركعتان بعد العشاء."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "differences",
                title = "اختلاف العدد والتفصيل",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "تختلف بعض طرق عد الرواتب المؤكدة بحسب الروايات والمذاهب، لذلك لا يعرض التطبيق رقما واحدا باعتباره الصورة الوحيدة الصحيحة.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "missed",
                title = "إذا فاتت النافلة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قضاء بعض الرواتب أو نقلها إلى وقت آخر له تفاصيل مرتبطة بسبب الفوات ووقت النهي. المحافظة عليها مهمة، لكن الفريضة تبقى مقدمة عليها.",
                    ),
                ),
            ),
        ),
    )

    private val travelerPrayer = LearningLesson(
        id = "traveler_prayer",
        titleRes = R.string.learn_topic_traveler_prayer,
        subtitleRes = R.string.learn_topic_traveler_prayer_sub,
        estimatedMinutes = 16,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "qasr",
                title = "قصر الصلاة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المسافر قد يقصر الظهر والعصر والعشاء من أربع ركعات إلى ركعتين عند تحقق شروط السفر. الفجر والمغرب لا يقصران.",
                    ),
                    LearningContentBlock.Callout(
                        title = "ليست المسافة وحدها كل الحكم",
                        body = "تعريف السفر ومسافته ومدة الإقامة التي ينتهي معها حكمه من مسائل الخلاف الفقهي، فلا يحول التطبيق رقما جغرافيا واحدا إلى فتوى شخصية.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "combining",
                title = "الجمع",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الجمع بين الظهر والعصر وبين المغرب والعشاء له أسباب وضوابط، والسفر من أشهرها. لا يعني كون الشخص مسافرا أن الجمع واجب في كل صلاة.",
                    ),
                ),
            ),
            LearningSection(
                id = "route",
                title = "أثناء الطريق",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("خطط للوقت", "اعرف مواقيت الصلاة قبل الانطلاق ومحطات التوقف الممكنة."),
                            LearningStepItem("القبلة", "استعمل التحري والأدوات المساعدة عندما يمكنك الوقوف للصلاة."),
                            LearningStepItem("وسائل النقل", "إذا تعذر النزول أو القيام أو استقبال القبلة فالحكم يتغير بحسب القدرة والظرف."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "residence",
                title = "الإقامة المؤقتة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "نية مدة الإقامة في المكان تؤثر في استمرار أحكام السفر عند الفقهاء، وتختلف الحدود التفصيلية بين المذاهب. الأفضل للمسافر المتكرر أن يعرف القاعدة التي يتبعها قبل الرحلة.",
                    ),
                ),
            ),
        ),
    )

    private val sickPrayer = LearningLesson(
        id = "sick_prayer",
        titleRes = R.string.learn_topic_sick_prayer,
        subtitleRes = R.string.learn_topic_sick_prayer_sub,
        estimatedMinutes = 15,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "principle",
                title = "الصلاة بحسب الاستطاعة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المريض لا يترك الصلاة لمجرد عجزه عن هيئة معينة. يصلي بحسب قدرته، ويخفف عنه فيما يعجز عنه من قيام أو ركوع أو سجود أو طهارة وفق الضوابط.",
                    ),
                    LearningContentBlock.Callout(
                        title = "السلامة",
                        body = "لا تعرض نفسك للسقوط أو تفاقم الإصابة من أجل حركة تعجز عنها فعلا. الحكم الشرعي في هيئة الصلاة يراعى مع تعليمات الطبيب المتعلقة بالحركة والوزن والجرح.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "positions",
                title = "القيام والجلوس والاضطجاع",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("إن استطعت القيام", "تؤدي ما تستطيع قائما، ويمكن الجلوس عند المشقة أو العجز المعتبر."),
                            LearningStepItem("إن عجزت عن السجود", "توجد كيفية للإيماء بحسب القدرة، ولا يلزم وضع الرأس على سطح مرتفع بطريقة قد تضر."),
                            LearningStepItem("إن تغيرت القدرة", "من تحسنت قدرته أثناء الصلاة ينتقل إلى الهيئة التي أصبح قادرا عليها بحسب التفصيل."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "purification",
                title = "الطهارة مع المرض",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الجروح والجبائر والقسطرة وسلس البول ونحوها لها أحكام مرتبطة بالطهارة وصاحب العذر. لا يعاد شرحها هنا بل ترتبط بمسار الطهارة.",
                    ),
                ),
            ),
            LearningSection(
                id = "hospital",
                title = "في المستشفى",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يمكن للمريض سؤال الطاقم عن وقت آمن للحركة، اتجاه تقريبي للقبلة، وإمكانية تنظيف موضع الصلاة. الأجهزة الطبية واشتراطات السلامة مقدمة في جانبها المهني، وتطبق الرخص الشرعية بحسب القدرة.",
                    ),
                ),
            ),
        ),
    )

    private val jumuah = LearningLesson(
        id = "jumuah",
        titleRes = R.string.learn_topic_jumuah,
        subtitleRes = R.string.learn_topic_jumuah_sub,
        estimatedMinutes = 14,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "basis",
                title = "أصل الجمعة",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "أمر القرآن عند النداء لصلاة الجمعة بالسعي إلى ذكر الله وترك البيع، وهو أصل في مكانة الجمعة ووجوبها على من تلزمه.",
                        referenceIds = listOf("quran_62_9"),
                    ),
                ),
            ),
            LearningSection(
                id = "structure",
                title = "بنية صلاة الجمعة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الحضور", "يأتي المسلم بسكينة ويتجنب إيذاء الناس أو تخطي الرقاب بغير حاجة."),
                            LearningStepItem("الخطبة", "ينصت للخطبة ويتجنب الانشغال بالكلام والهاتف."),
                            LearningStepItem("الصلاة", "يصلي مع الإمام ركعتين جمعة بعد الخطبة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "who",
                title = "على من تجب؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "توجد شروط تتعلق بالإقامة والقدرة والذكورة وغيرها في كتب الفقه، مع أعذار كالمرض والسفر والخوف. من لم تلزمه الجمعة أو فاتته يصلي الظهر بحسب حكم حاله.",
                    ),
                ),
            ),
            LearningSection(
                id = "work_study",
                title = "العمل والدراسة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "ترتيب وقت الجمعة مع جهة العمل أو الدراسة يحتاج تخطيطا مبكرا واحترام الأنظمة المحلية. إذا تعذر الحضور بسبب حالة خاصة مستمرة فالحكم الشخصي يحتاج سؤال جهة علمية تعرف تفاصيل الواقع.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_62_9", LearningReferenceKind.QURAN, "القرآن الكريم، سورة الجمعة، الآية 62:9", "Quran 62:9"),
        ),
    )

    private val specialPrayers = LearningLesson(
        id = "special",
        titleRes = R.string.learn_topic_special,
        subtitleRes = R.string.learn_topic_special_sub,
        estimatedMinutes = 19,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "witr",
                title = "الوتر وقيام الليل",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الوتر ختام صلاة الليل وله صور متعددة في عدد الركعات وهيئتها. قيام الليل نافلة واسعة الباب، والمهم تعلم الصورة الصحيحة دون إلزام الناس بهيئة واحدة في مسائل الاجتهاد.",
                    ),
                ),
            ),
            LearningSection(
                id = "duha",
                title = "صلاة الضحى",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تؤدى بعد ارتفاع الشمس إلى ما قبل الزوال، وعدد ركعاتها له سعة ضمن ما ورد من السنة.",
                    ),
                ),
            ),
            LearningSection(
                id = "istikhara",
                title = "الاستخارة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("قرار مباح", "تكون الاستخارة في الأمور التي يطلب فيها المسلم التوفيق بين خيارات مشروعة."),
                            LearningStepItem("ركعتان", "يصلي ركعتين من غير الفريضة."),
                            LearningStepItem("الدعاء", "يدعو بدعاء الاستخارة الوارد ثم يمضي في الأسباب والاستشارة."),
                            LearningStepItem("لا تنتظر علامة خارقة", "ليست الاستخارة شرطا أن يرى بعدها حلما أو شعورا خاصا."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "eid",
                title = "صلاة العيد",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "لصلاة العيد هيئة خاصة وتكبيرات زائدة، وتفاصيل عدد التكبيرات ومواضعها تختلف بين المذاهب. الأفضل اتباع إمام الجماعة وعدم تحويل الخلاف إلى اضطراب في الصف.",
                    ),
                ),
            ),
            LearningSection(
                id = "eclipse_rain",
                title = "الكسوف والاستسقاء",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "لهما صفات وأسباب مخصوصة في السنة. عند وقوع السبب يتبع المسلم التعليم الشرعي الموثوق أو صلاة المسجد بدلا من القياس على الصلاة اليومية العادية.",
                    ),
                ),
            ),
            LearningSection(
                id = "funeral_link",
                title = "صلاة الجنازة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "صلاة الجنازة لا ركوع فيها ولا سجود، ولها تكبيرات وأدعية مخصوصة. تفاصيل تجهيز الميت والجنازة موجودة في قسم الجنائز المستقل داخل التطبيق.",
                    ),
                ),
            ),
        ),
    )
}
