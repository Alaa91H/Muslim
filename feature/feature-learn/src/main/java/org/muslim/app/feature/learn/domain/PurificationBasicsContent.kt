package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object PurificationBasicsContent {

    val lessons: List<LearningLesson> by lazy { listOf(
        taharaOverview,
        waterAndImpurity,
        restroomEtiquette,
    ) }

    private val taharaOverview = LearningLesson(
        id = "tahara_intro",
        titleRes = R.string.learn_topic_tahara_intro,
        subtitleRes = R.string.learn_topic_tahara_intro_sub,
        estimatedMinutes = 9,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "meaning",
                title = "ما الطهارة؟",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الطهارة باب أساسي من أبواب العبادة؛ فهي تهيئة للمسلم للصلاة وما يشترط له رفع الحدث أو إزالة النجاسة. ويُفرّق في الدراسة بين الطهارة من الحدث، مثل الوضوء والغسل والتيمم، والطهارة من النجاسة التي تتعلق بالبدن أو الثوب أو المكان.",
                    ),
                    LearningContentBlock.Callout(
                        title = "قاعدة هذا المسار",
                        body = "تعلم الحكم ثم التطبيق العملي، ولا تنتقل إلى الوسوسة أو التفتيش المبالغ فيه. عند وجود حالة شخصية غير معتادة أو مرض أو نزف مستمر، يُرجع إلى أهل العلم والطبيب عند الحاجة.",
                        tone = LearningCalloutTone.IMPORTANT,
                    ),
                ),
            ),
            LearningSection(
                id = "map",
                title = "خريطة أحكام الطهارة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("الحدث الأصغر", "يرتفع بالوضوء، وينتقل إلى التيمم عند تحقق سببه الشرعي."),
                            LearningStepItem("الحدث الأكبر", "يرتفع بالغسل، وينتقل إلى التيمم عند تعذر استعمال الماء وفق شروطه."),
                            LearningStepItem("النجاسة", "المقصود إزالة عين النجاسة عن البدن أو الثوب أو المكان بالطريقة المعتبرة."),
                            LearningStepItem("الأعذار المستمرة", "لها أحكام خاصة في الطهارة والصلاة، ويُراعى فيها استمرار العذر ووقت العبادة."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "evidence",
                title = "الأصل القرآني",
                blocks = listOf(
                    LearningContentBlock.Evidence(
                        heading = "آية الطهارة",
                        text = "تجمع آية المائدة 5:6 أصول الوضوء، والغسل من الجنابة، والرخصة في التيمم عند فقد الماء أو تعذر استعماله.",
                        referenceIds = listOf("quran_5_6"),
                    ),
                ),
            ),
            LearningSection(
                id = "study_order",
                title = "كيف تدرس هذا المسار؟",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("ابدأ بالماء والنجاسة", "حتى تعرف ما الذي يصح التطهر به وكيف تزال النجاسة."),
                            LearningStepItem("أتقن الوضوء", "ثم تعلم نواقضه والمسح على الخفين وما يلحق به."),
                            LearningStepItem("تعلم الغسل والتيمم", "للحالات التي تحتاج طهارة كبرى أو يتعذر فيها الماء."),
                            LearningStepItem("ادرس الأحكام الخاصة", "مثل الحيض والنفاس والاستحاضة وصاحب العذر."),
                        ),
                    ),
                ),
            ),
        ),
        references = listOf(
            LearningReference(
                id = "quran_5_6",
                kind = LearningReferenceKind.QURAN,
                citation = "القرآن الكريم، سورة المائدة، الآية 5:6",
                locator = "Quran 5:6",
            ),
        ),
    )

    private val waterAndImpurity = LearningLesson(
        id = "water_impurity",
        titleRes = R.string.learn_topic_water_impurity,
        subtitleRes = R.string.learn_topic_water_impurity_sub,
        estimatedMinutes = 12,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "water",
                title = "الماء والطهارة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الأصل استعمال الماء الطهور في الوضوء والغسل وإزالة النجاسة. الماء المعتاد من شبكات الشرب والآبار والأنهار والأمطار داخل في هذا الأصل ما دام باقيا على وصف الماء ولم تتغلب عليه نجاسة.",
                    ),
                    LearningContentBlock.Callout(
                        title = "السلامة أولا",
                        body = "الحكم الشرعي في الطهارة لا يعني أن كل ماء صالح للشرب أو آمن طبيا. عند الشك في التلوث الصحي اتبع تعليمات السلطات المختصة.",
                        tone = LearningCalloutTone.WARNING,
                    ),
                ),
            ),
            LearningSection(
                id = "impurity",
                title = "إزالة النجاسة",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("حدد الموضع", "لا يلزم غسل ما لم تصبه النجاسة؛ المقصود معالجة المكان المتنجس."),
                            LearningStepItem("أزل عين النجاسة", "تزال المادة نفسها أولا إن كانت ظاهرة."),
                            LearningStepItem("اغسل حتى تزول", "يستعمل الماء حتى تزول عين النجاسة وآثارها المعتادة بقدر الاستطاعة."),
                            LearningStepItem("لا تتكلف", "الأثر الذي يشق زواله بعد بذل المعتاد تُراجع تفاصيله الفقهية دون وسوسة أو إتلاف."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "doubt",
                title = "الشك واليقين",
                blocks = listOf(
                    LearningContentBlock.Callout(
                        body = "لا تجعل الاحتمالات البعيدة سببا لغسل الثياب والجسد مرارا. إذا لم تتيقن من إصابة النجاسة فتعامل مع الأشياء على أصل الطهارة، مع إحالة الحالات الخاصة إلى فتوى موثوقة.",
                        tone = LearningCalloutTone.INFO,
                    ),
                ),
            ),
            LearningSection(
                id = "differences",
                title = "مسائل فيها تفصيل فقهي",
                blocks = listOf(
                    LearningContentBlock.Comparison(
                        intro = "توجد تفاصيل بين المذاهب في تقسيم المياه، ومقادير بعض النجاسات، وكيفية تطهير أنواع معينة من الأسطح. يعرض التطبيق الأصل العملي المشترك ويترك التفصيل الدقيق للمراجع الفقهية المعتمدة.",
                        items = listOf(
                            LearningComparisonItem("الماء المتغير", "يفرق الفقهاء بين تغير الماء بطاهر وتغيره بنجاسة، وتختلف بعض التفاصيل بحسب مقدار التغير وسببه."),
                            LearningComparisonItem("النجاسة اليسيرة", "توجد فروق في بعض الصور والمقادير المعفو عنها، لذلك لا يحول الدرس الحالات الدقيقة إلى فتوى شخصية."),
                        ),
                    ),
                ),
            ),
        ),
    )

    private val restroomEtiquette = LearningLesson(
        id = "restroom_etiquette",
        titleRes = R.string.learn_topic_restroom_etiquette,
        subtitleRes = R.string.learn_topic_restroom_etiquette_sub,
        estimatedMinutes = 8,
        contentVersion = 1,
        sections = listOf(
            LearningSection(
                id = "before_after",
                title = "قبل قضاء الحاجة وبعده",
                blocks = listOf(
                    LearningContentBlock.Steps(
                        listOf(
                            LearningStepItem("احفظ الخصوصية", "اختر مكانا ساترا واتبع أنظمة النظافة والمرافق العامة."),
                            LearningStepItem("تجنب تلويث المكان", "احرص على عدم إصابة الثوب أو البدن أو المكان بالنجاسة قدر الاستطاعة."),
                            LearningStepItem("نظف الموضع", "بعد الفراغ يُنظف موضع الخارج بالماء أو بما يقوم مقامه وفق الضوابط الفقهية."),
                            LearningStepItem("اغسل اليدين", "النظافة الصحية بعد استعمال دورة المياه مكملة للسلوك الشرعي الصحيح."),
                        ),
                    ),
                ),
            ),
            LearningSection(
                id = "istinja",
                title = "الاستنجاء والاستجمار",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "الاستنجاء هو تنظيف موضع الخارج بالماء، والاستجمار هو التنظيف بمادة طاهرة منظفة مباحة كالمناديل المناسبة. وقد يجمع المسلم بينهما عند الحاجة. المقصود إزالة عين الخارج وتحقيق النظافة دون إيذاء أو إسراف.",
                    ),
                    LearningContentBlock.Callout(
                        title = "المناديل الحديثة",
                        body = "يمكن أن تدخل المناديل المناسبة في معنى مواد التنظيف، لكن تفاصيل الاكتفاء بها وعدد المسحات وبعض الصور المختلف فيها تحتاج مراجعة فقهية عند الحاجة.",
                        tone = LearningCalloutTone.DIFFERENCE_OF_OPINION,
                    ),
                ),
            ),
            LearningSection(
                id = "public_spaces",
                title = "في العمل والسفر والأماكن العامة",
                blocks = listOf(
                    LearningContentBlock.Paragraph(
                        "استخدم المرافق بطريقة لا تضر الآخرين، ولا ترش الماء على الأرض بما يسبب خطرا أو فوضى، واحمل وسيلة نظافة بسيطة عند السفر إذا احتجت إليها. المقصد هو الجمع بين الطهارة والآداب والسلامة.",
                    ),
                ),
            ),
        ),
    )
}
