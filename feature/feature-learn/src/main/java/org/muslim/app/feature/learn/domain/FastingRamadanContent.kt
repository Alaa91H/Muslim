package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FastingRamadanContent {

    val lessons: List<LearningLesson> by lazy {
        listOf(
            fastingTravelIllness,
            ramadanSunnahs,
            laylatQadrAndItikaf,
            voluntaryFasting,
        )
    }

    private val fastingTravelIllness = LearningLesson(
        id = "fasting_travel_illness",
        titleRes = R.string.learn_topic_fasting_travel_illness,
        subtitleRes = R.string.learn_topic_fasting_travel_illness_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "travel",
                title = "الصيام في السفر",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "المسافر له رخصة الفطر إذا تحقق وصف السفر شرعا، ويقضي بعد رمضان. وقد يصوم إذا لم تلحقه مشقة معتبرة، لكن الرخصة ليست عيبا ولا نقصا في العبادة.",
                    ),
                    LearningContentBlock.Evidence(
                        text = "ورد في صحيح البخاري إنكار الصيام في السفر على من بلغ به الصوم مشقة شديدة، وهو دليل على مراعاة القدرة والضرر.",
                        referenceIds = listOf("bukhari_1946"),
                    ),
                ),
            ),
            LearningSection(
                id = "illness",
                title = "الصيام مع المرض",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("مرض عارض", "إذا كان المرض مؤقتا ويشق معه الصيام أو يضر، يفطر المريض ويقضي عند القدرة."),
                            LearningStepItem("مرض مزمن", "إذا كان العجز مستمرا ولا يرجى زواله فله باب الفدية بحسب الحكم الفقهي."),
                            LearningStepItem("دواء ضروري", "الحاجة إلى دواء منتظم قد تؤثر في القدرة على الصيام، ويحتاج تقديرها إلى رأي طبي مناسب."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "better_choice",
                title = "هل الأفضل الصيام أم الفطر؟",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "لا توجد إجابة واحدة لكل مسافر أو مريض. ينظر إلى المشقة والضرر والحال الشخصية وما عليه المذهب أو الفتوى الموثوقة.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "practical",
                title = "قبل السفر أو العلاج",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("اعرف أوقاتك", "راجع الفجر والمغرب في مكان الوصول والمنطقة الزمنية الجديدة."),
                            LearningStepItem("خطط للأدوية", "لا تغيّر جرعات العلاج ذاتيا من أجل الصيام."),
                            LearningStepItem("اعرف عدد الأيام", "سجل ما أفطرته لتسهيل القضاء لاحقا."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("bukhari_1946", LearningReferenceKind.HADITH, "صحيح البخاري 1946، كتاب الصوم", "Sahih al-Bukhari 1946"),
        ),
    )

    private val ramadanSunnahs = LearningLesson(
        id = "ramadan_sunnahs",
        titleRes = R.string.learn_topic_ramadan_sunnahs,
        subtitleRes = R.string.learn_topic_ramadan_sunnahs_sub,
        estimatedMinutes = 13,
        sections = listOf(
            LearningSection(
                id = "daily",
                title = "سنن يومية",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("السحور", "احرص عليه دون إسراف، وأخره إلى ما قبل الفجر بما لا يوقعك في الشك."),
                            LearningStepItem("تعجيل الفطر", "إذا تحقق غروب الشمس فابدأ الإفطار دون تأخير متكلف."),
                            LearningStepItem("القرآن والذكر", "اجعل لرمضان وردا ثابتا يناسب وقتك وقدرتك."),
                            LearningStepItem("الصدقة", "استثمر الشهر في الإحسان بما تقدر عليه دون إضرار بمن تعول."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "night",
                title = "قيام رمضان",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "قيام رمضان من شعائر الشهر، وتدخل فيه التراويح والوتر. عدد الركعات وهيئة الجماعة من المسائل التي فيها سعة وخلاف معتبر، فلا يجعل التطبيق عددا واحدا معيارا لصحة القيام.",
                    ),
                ),
            ),
            LearningSection(
                id = "quran",
                title = "رمضان والقرآن",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "قرن القرآن بين شهر رمضان ونزول القرآن، وهو أصل في عناية المسلم بالقراءة والتدبر في هذا الشهر.",
                        referenceIds = listOf("quran_2_185"),
                    ),
                ),
            ),
            LearningSection(
                id = "balance",
                title = "التوازن",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "لا تجعل كثرة البرامج سببا لترك الفرائض أو النوم عن الصلاة أو الإضرار بالصحة. الاستمرار المتوازن أنفع من خطة مرهقة تنقطع بعد أيام.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_2_185", LearningReferenceKind.QURAN, "القرآن الكريم، سورة البقرة، الآية 2:185", "Quran 2:185"),
        ),
    )

    private val laylatQadrAndItikaf = LearningLesson(
        id = "laylat_qadr_itikaf",
        titleRes = R.string.learn_topic_laylat_qadr_itikaf,
        subtitleRes = R.string.learn_topic_laylat_qadr_itikaf_sub,
        estimatedMinutes = 12,
        sections = listOf(
            LearningSection(
                id = "qadr",
                title = "ليلة القدر",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        text = "خص القرآن ليلة القدر بسورة كاملة وبيّن أن العبادة فيها خير من ألف شهر.",
                        referenceIds = listOf("quran_97_1_5"),
                    ),
                    LearningContentBlock.Paragraph(
                        "يُطلب تحري ليلة القدر في العشر الأواخر، مع عناية خاصة بالليالي الوترية في النصوص. لا يثبت التطبيق ليلة بعينها كل عام من غير دليل.",
                    ),
                ),
            ),
            LearningSection(
                id = "last_ten",
                title = "العشر الأواخر",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("حافظ على الفرائض", "لا تجعل قيام الليل سببا لتضييع صلاة الفجر أو واجباتك الأساسية."),
                            LearningStepItem("زد من القيام والذكر", "وزع العبادة على ما تستطيع الاستمرار عليه."),
                            LearningStepItem("الدعاء", "أكثر من الدعاء والاستغفار وطلب العفو."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "itikaf",
                title = "الاعتكاف",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الاعتكاف عبادة لزوم المسجد بنية التعبد، وله شروط وأحكام تتعلق بالمكث والخروج وما ينافيه. التطبيق يعرض الأصل، أما التفاصيل العملية فتتبع أحكام المسجد والفقه الذي يرجع إليه المستخدم.",
                    ),
                    LearningContentBlock.Callout(
                        body = "إدارة المسجد وأنظمة السلامة والإقامة الليلية جزء من الواقع العملي للاعتكاف؛ يجب احترامها.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "avoid_claims",
                title = "تجنب الادعاءات غير الموثوقة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "لا يعتمد الدرس علامات منتشرة غير ثابتة لتحديد ليلة القدر أثناء الليل، ولا رسائل متداولة تزعم الجزم بليلة معينة.",
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference("quran_97_1_5", LearningReferenceKind.QURAN, "القرآن الكريم، سورة القدر، الآيات 97:1-5", "Quran 97:1-5"),
        ),
    )

    private val voluntaryFasting = LearningLesson(
        id = "voluntary_fasting",
        titleRes = R.string.learn_topic_voluntary_fasting,
        subtitleRes = R.string.learn_topic_voluntary_fasting_sub,
        estimatedMinutes = 14,
        sections = listOf(
            LearningSection(
                id = "examples",
                title = "أمثلة من صيام التطوع",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ست من شوال", "من أشهر نوافل ما بعد رمضان، مع تفصيل ترتيبها مع القضاء عند الفقهاء."),
                            LearningStepItem("عرفة لغير الحاج", "يُستحب صيام يوم عرفة لغير الحاج، أما الحاج بعرفة فله حكم آخر بحسب السنة والحال."),
                            LearningStepItem("عاشوراء", "يُشرع صيام عاشوراء، ويستحب ضم يوم قبله أو بعده بحسب ما ورد في السنة."),
                            LearningStepItem("الاثنين والخميس", "من الأيام التي وردت فيها فضيلة الصيام."),
                            LearningStepItem("الأيام البيض", "الثالث عشر والرابع عشر والخامس عشر من الشهر الهجري من أشهر صور الصيام الشهري."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "prohibited",
                title = "أيام لا يصام فيها",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "لا يصام يوم عيد الفطر ولا يوم عيد الأضحى. وأيام التشريق لها أحكام خاصة، مع استثناءات محدودة معروفة في الحج.",
                    ),
                ),
            ),
            LearningSection(
                id = "priority",
                title = "الفرض قبل النفل",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        title = "ترتيب الأولويات",
                        body = "من عليه قضاء من رمضان لا ينبغي أن يهمل القضاء وينشغل ببرنامج طويل من النوافل دون معرفة الحكم الذي يتبعه في الجمع بينهما.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "sustainable",
                title = "صيام مستدام",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "اختر من النوافل ما يناسب صحتك وعملك والتزاماتك، ولا تجعل المنافسة أو الشعور بالذنب يدفعانك إلى نمط يضر بصحتك أو يعطلك عن واجباتك.",
                    ),
                ),
            ),
        ),
    )
}
