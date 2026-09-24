package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object ZakatCoreContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            zakatOverview,
            zakatNisabHaul,
            zakatRecipients,
            zakatFitr,
        )
    }

    private val zakatOverview = LearningLesson(
        id = "zakat",
        titleRes = R.string.learn_topic_zakat,
        subtitleRes = R.string.learn_topic_zakat_sub,
        estimatedMinutes = 16,
        contentVersion = 2,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "ما الزكاة؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الزكاة عبادة مالية واجبة في أموال مخصوصة عند تحقق شروطها، وليست صدقة تطوعية فقط. تختلف أحكامها باختلاف نوع المال: نقد، ذهب وفضة، تجارة، زروع وثمار، أنعام، وغيرها.",
                    ),
                    LearningContentBlock.Evidence(
                        text = "حدد القرآن مصارف الزكاة في ثمانية أصناف في سورة التوبة.",
                        referenceIds = listOf("quran_9_60"),
                    ),
                ),
            ),
            LearningSection(
                id = "questions",
                title = "أربع أسئلة قبل الحساب",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ما نوع المال؟", "النقد وعروض التجارة لا تعالج بالطريقة نفسها التي تعالج بها الزروع أو الأنعام."),
                            LearningStepItem("هل بلغ النصاب؟", "لكل باب نصاب أو شرط مالي معتبر."),
                            LearningStepItem("هل يشترط الحول؟", "يشترط مرور الحول في بعض الأموال، ولا يشترط بالصورة نفسها في الزروع والثمار."),
                            LearningStepItem("من يستحقها؟", "لا يكفي حساب المبلغ؛ يجب صرف الزكاة في مصرف معتبر."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "quality",
                title = "من المال الطيب",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "وجّه القرآن إلى الإنفاق من طيبات ما كسب الإنسان وما أخرج الله من الأرض، وعدم تعمد الرديء.",
                        referenceIds = listOf("quran_2_267"),
                    ),
                ),
            ),
            LearningSection(
                id = "calculator",
                title = "التعلم والحاسبة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "الحاسبة أداة مساعدة",
                        body = "حاسبة الزكاة في التطبيق تساعد في جمع الأصول وتقدير النصاب والمبلغ وفق افتراضات محددة، لكنها لا تحسم المسائل المختلف فيها مثل الحلي والديون وبعض الاستثمارات.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "map",
                title = "خريطة المسار",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("النصاب والحول", "تعلم متى يبدأ الوجوب."),
                            LearningStepItem("الأموال الحديثة", "النقد والحسابات والذهب والفضة والتجارة والاستثمار."),
                            LearningStepItem("الديون والحلي", "مسائل تحتاج تفصيلا ولا تختزل في خانة رقمية واحدة."),
                            LearningStepItem("المصارف", "تعرف من يجوز دفع الزكاة إليه."),
                            LearningStepItem("الزروع والأنعام", "أبواب لها مقادير ونُصب خاصة."),
                            LearningStepItem("زكاة الفطر", "عبادة مستقلة مرتبطة بنهاية رمضان."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_9_60", LearningReferenceKind.QURAN, "القرآن الكريم، سورة التوبة، الآية 9:60", "Quran 9:60"),
            LearningReference("quran_2_267", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:267", "Quran 2:267"),
        ),
    )

    private val zakatNisabHaul = LearningLesson(
        id = "zakat_nisab_haul",
        titleRes = R.string.learn_topic_zakat_nisab_haul,
        subtitleRes = R.string.learn_topic_zakat_nisab_haul_sub,
        estimatedMinutes = 15,
        sections = listOf(
            LearningSection(
                id = "nisab",
                title = "ما النصاب؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "النصاب هو الحد المالي الذي تبدأ عنده الزكاة في المال الذي يشترط له النصاب. في النقود المعاصرة تُربط القيمة عادة بنصاب الذهب أو الفضة بحسب المنهج الفقهي المعتمد.",
                    ),
                    LearningContentBlock.Evidence(
                        text = "ورد في حديث علي رضي الله عنه ذكر مائتي درهم من الفضة وعشرين دينارا من الذهب، مع مقدار ربع العشر بعد مرور الحول.",
                        referenceIds = listOf("abudawud_1573"),
                    ),
                ),
            ),
            LearningSection(
                id = "project_values",
                title = "قيم الحاسبة الحالية",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "الحاسبة الحالية في المشروع تستخدم 85 غراما للذهب و595 غراما للفضة، ثم تقارن قيمة المال بالنصاب المحسوب من أسعار المعادن. هذه إعدادات فقهية-تقريبية للمشروع وتبقى خاضعة للمراجعة العلمية.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "haul",
                title = "الحول",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "في أموال مثل النقد والذهب والفضة وعروض التجارة يرتبط الوجوب بمرور حول قمري عند تحقق شروطه. أما الزروع والثمار فلها وقت مرتبط بالحصاد، فلا تنقل قاعدة الحول إليها تلقائيا.",
                    ),
                ),
            ),
            LearningSection(
                id = "changing_balance",
                title = "تغير المال أثناء السنة",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "مسألة تحتاج منهجا ثابتا",
                        body = "زيادة الرصيد ونقصانه، المال المستفاد أثناء الحول، الرواتب الشهرية، وضم الأموال بعضها إلى بعض فيها تفاصيل فقهية. للحساب السنوي المنتظم اتبع طريقة علمية واحدة ولا تغيرها كل سنة للوصول إلى مبلغ أقل.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "rate",
                title = "ربع العشر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "في الأموال النقدية وما يلحق بها في الحسبة الشائعة يكون المقدار ربع العشر، أي 2.5%، بعد تحقق النصاب والحول والشروط الأخرى.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("abudawud_1573", LearningReferenceKind.HADITH, "سنن أبي داود 1573، كتاب الزكاة", "Sunan Abi Dawud 1573"),
        ),
    )

    private val zakatRecipients = LearningLesson(
        id = "zakat_recipients",
        titleRes = R.string.learn_topic_zakat_recipients,
        subtitleRes = R.string.learn_topic_zakat_recipients_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "eight",
                title = "المصارف الثمانية",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "سورة التوبة 9:60 حصرت مصارف الزكاة في الفقراء والمساكين والعاملين عليها والمؤلفة قلوبهم وفي الرقاب والغارمين وفي سبيل الله وابن السبيل.",
                        referenceIds = listOf("quran_9_60"),
                    ),
                ),
            ),
            LearningSection(
                id = "practical",
                title = "التطبيق العملي",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("تحقق من الاستحقاق", "لا يكفي أن يكون الشخص محتاجا في الظاهر؛ بعض المصارف لها شروط أدق."),
                            LearningStepItem("احفظ الكرامة", "إيصال الزكاة لا يبرر إحراج المحتاج أو نشر حاله."),
                            LearningStepItem("استعن بجهة موثوقة", "الجمعيات والمؤسسات قد تسهل التحقق والتوزيع إذا كانت موثوقة وشفافة."),
                            LearningStepItem("وثق لنفسك", "احتفظ بسجل بسيط للمبلغ والتاريخ والجهة دون تخزين معلومات حساسة عن المستفيدين."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "family",
                title = "الأقارب",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "دفع الزكاة للأقارب قد يكون مشروعا في صور وممنوعا في صور، خصوصا مع من تجب نفقتهم عليك. صلة القرابة وحدها لا تكفي للحكم.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "organizations",
                title = "الوكالة والجمعيات",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "يجوز توكيل جهة في إيصال الزكاة إذا كانت مأمونة وتعرف المصارف، لكن ينبغي معرفة هل يصل المال كزكاة وفي الوقت المناسب، وهل تقتطع الجهة رسوما وكيف تصنفها.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_9_60", LearningReferenceKind.QURAN, "القرآن الكريم، سورة التوبة، الآية 9:60", "Quran 9:60"),
        ),
    )

    private val zakatFitr = LearningLesson(
        id = "zakat_fitr",
        titleRes = R.string.learn_topic_zakat_fitr,
        subtitleRes = R.string.learn_topic_zakat_fitr_sub,
        estimatedMinutes = 13,
        sections = listOf(
            LearningSection(
                id = "basis",
                title = "أصل زكاة الفطر",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "ثبت في صحيح البخاري فرض زكاة الفطر صاعا من الطعام على المسلمين، وأمر بإخراجها قبل خروج الناس إلى صلاة العيد.",
                        referenceIds = listOf("bukhari_1503"),
                    ),
                ),
            ),
            LearningSection(
                id = "who",
                title = "عن من تخرج؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "تتعلق زكاة الفطر بالأشخاص لا بحجم المدخرات السنوية مثل زكاة المال. أحكام من تلزمه عن نفسه وعن من يعول، والقدرة المالية المعتبرة، تحتاج إلى التفصيل الفقهي.",
                    ),
                ),
            ),
            LearningSection(
                id = "food_cash",
                title = "طعام أم قيمة نقدية؟",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "خلاف معتبر",
                        body = "إخراج القيمة النقدية بدل الطعام من أشهر مسائل الخلاف بين المذاهب والهيئات المعاصرة. الحاسبة يمكنها تقدير قيمة الصاع كأداة، لكنها لا تحسم هل الإخراج النقدي هو المعتمد في حالتك.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "time",
                title = "وقت الإخراج",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("قبل العيد", "الأصل أن تصل إلى مستحقها قبل صلاة العيد."),
                            LearningStepItem("التعجيل", "يجوز التعجيل قبل العيد بمدة في صور معروفة، وتختلف بعض التفاصيل بحسب الجهة والمذهب."),
                            LearningStepItem("التوزيع الإلكتروني", "إذا دفعتها لمنصة أو جمعية فانتبه إلى وقت وصولها الفعلي للمستحق."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_1503", LearningReferenceKind.HADITH, "صحيح البخاري 1503، كتاب الزكاة", "Sahih al-Bukhari 1503"),
        ),
    )
}
