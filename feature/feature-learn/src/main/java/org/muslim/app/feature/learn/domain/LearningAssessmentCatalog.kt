package org.muslim.app.feature.learn.domain

/**
 * Stable self-check questions layered on top of the academy lessons.
 *
 * Assessments are intentionally separated from lesson prose so they can evolve
 * without changing lesson ids or completion state. All questions remain part
 * of the same scholarly-review lifecycle as their parent lessons.
 */
data class LearningAssessmentEntry(
    val lessonId: String,
    val quiz: LearningContentBlock.Quiz,
    val reviewStatus: LearningReviewStatus = LearningReviewStatus.NEEDS_SCHOLAR_REVIEW,
)

object LearningAssessmentCatalog {

    val entries: List<LearningAssessmentEntry> = listOf(
        entry(
            lessonId = "pillars_islam",
            quizId = "islam_iman_ihsan",
            question = "أي ترتيب يعكس إطار حديث جبريل في الدرس؟",
            options = listOf(
                option("a", "الإسلام ثم الإيمان ثم الإحسان"),
                option("b", "الإحسان ثم الزكاة ثم الحج"),
                option("c", "الفقه ثم التاريخ ثم اللغة"),
            ),
            correct = "a",
            explanation = "يعرض الدرس مراتب الإسلام والإيمان والإحسان بوصفها خريطة تعليمية مترابطة.",
        ),
        entry(
            lessonId = "pillars_iman",
            quizId = "six_pillars",
            question = "أي مما يلي عُدَّ في الدرس من أركان الإيمان؟",
            options = listOf(
                option("a", "الإيمان بالملائكة"),
                option("b", "تعلم لغة عربية ثانية"),
                option("c", "السفر إلى بلد مسلم"),
            ),
            correct = "a",
            explanation = "الملائكة أحد الأركان الستة المذكورة في حديث جبريل.",
        ),
        entry(
            lessonId = "wudu",
            quizId = "wudu_learning",
            question = "ما المنهج الأنسب عند الشك المتكرر في الطهارة؟",
            options = listOf(
                option("a", "إعادة الوضوء عند كل احتمال"),
                option("b", "تعلم القاعدة وعدم بناء النقض على مجرد الشك"),
                option("c", "ترك الصلاة حتى يزول الشك تمامًا"),
            ),
            correct = "b",
            explanation = "المسار يفرق بين اليقين والشك ويحذر من تحويل الطهارة إلى باب للوسواس.",
        ),
        entry(
            lessonId = "salah",
            quizId = "prayer_learning",
            question = "ما الأولوية عند تعلم الصلاة؟",
            options = listOf(
                option("a", "تعلم الشروط والأركان والترتيب قبل الانشغال بالتفاصيل النادرة"),
                option("b", "حفظ جميع مسائل الخلاف أولًا"),
                option("c", "تأجيل الصلاة حتى إتقان كل السنن"),
            ),
            correct = "a",
            explanation = "منهج الأكاديمية يقدم ما تتوقف عليه الصلاة أولًا ثم يبني التفاصيل تدريجيًا.",
        ),
        entry(
            lessonId = "fasting",
            quizId = "fasting_foundation",
            question = "عند وجود مرض أو سفر يؤثر في الصيام، ما الأسلوب الصحيح للتعلم؟",
            options = listOf(
                option("a", "تطبيق قاعدة واحدة على كل الحالات"),
                option("b", "الرجوع لتفاصيل الأعذار والقضاء بحسب الحالة"),
                option("c", "تجاهل الحالة الصحية دائمًا"),
            ),
            correct = "b",
            explanation = "أحكام الأعذار تختلف باختلاف الحالة، لذلك يفصل المسار بينها بدل التعميم.",
        ),
        entry(
            lessonId = "zakat",
            quizId = "zakat_scope",
            question = "لماذا لا تكفي نسبة واحدة لحساب كل صور الزكاة؟",
            options = listOf(
                option("a", "لأن الأموال والأنصبة والحول والمصارف لها تفاصيل مختلفة"),
                option("b", "لأن الزكاة اختيارية دائمًا"),
                option("c", "لأن الزكاة لا تتعلق بالمال"),
            ),
            correct = "a",
            explanation = "المسار يفصل أنواع الأموال وشروطها بدل تقديم حاسبة عامة بلا سياق.",
        ),
        entry(
            lessonId = "quran_intro",
            quizId = "quran_context",
            question = "عند فهم آية، ما الذي يحذر منه المسار؟",
            options = listOf(
                option("a", "قراءة السياق"),
                option("b", "بناء حكم كامل على ترجمة جملة منفردة بلا سياق"),
                option("c", "الرجوع إلى تفسير موثوق"),
            ),
            correct = "b",
            explanation = "فهم القرآن يحتاج سياقًا ومصادر تفسيرية موثوقة، لا اقتطاع جملة واحدة.",
        ),
        entry(
            lessonId = "sunnah_intro",
            quizId = "hadith_source",
            question = "قبل نشر حديث، ما الخطوة الأساسية؟",
            options = listOf(
                option("a", "التحقق من المصدر واللفظ والدرجة"),
                option("b", "الاكتفاء بصورة متداولة"),
                option("c", "اعتبار كثرة المشاركات دليل صحة"),
            ),
            correct = "a",
            explanation = "مسار السنة يركز على التحقق من المصدر واللفظ والدرجة قبل النقل.",
        ),
        entry(
            lessonId = "seerah_method",
            quizId = "seerah_certainty",
            question = "كيف يعامل المسار تفاصيل السيرة التاريخية؟",
            options = listOf(
                option("a", "كل رواية مشهورة قطعية"),
                option("b", "يفرق بين القرآن والحديث الصحيح والأخبار متفاوتة الثبوت"),
                option("c", "يرفض كل المصادر التاريخية"),
            ),
            correct = "b",
            explanation = "المنهج يميز طبقات المصادر ودرجات اليقين بدل مساواة الأخبار كلها.",
        ),
        entry(
            lessonId = "ethics_foundation",
            quizId = "ethics_boundaries",
            question = "أي عبارة توافق منهج درس الأخلاق؟",
            options = listOf(
                option("a", "الرحمة تعني قبول كل أذى"),
                option("b", "الحزم والحدود يمكن أن يجتمعا مع الرحمة والعدل"),
                option("c", "النية الحسنة تلغي أثر السلوك"),
            ),
            correct = "b",
            explanation = "الدرس يفرق بين الرحمة وترك الحقوق، وبين حسن القصد وإصلاح أثر الخطأ.",
        ),
        entry(
            lessonId = "family_intro",
            quizId = "family_layers",
            question = "لماذا يفصل المسار بين الفقه والقانون والمهارات الأسرية؟",
            options = listOf(
                option("a", "لأن كل طبقة تجيب عن أسئلة مختلفة"),
                option("b", "لأن القانون يغني عن الفقه دائمًا"),
                option("c", "لأن مهارات التواصل لا قيمة لها"),
            ),
            correct = "a",
            explanation = "الحكم الشرعي والوضع القانوني ومهارات التواصل مجالات متقاطعة لكنها ليست شيئًا واحدًا.",
        ),
        entry(
            lessonId = "finance_intro",
            quizId = "finance_layers",
            question = "هل كون معاملة جائزة شرعًا يعني تلقائيًا أنها مناسبة ماليًا لك؟",
            options = listOf(
                option("a", "نعم في كل الحالات"),
                option("b", "لا، فالمخاطر والقدرة على السداد والملاءمة المالية أسئلة إضافية"),
                option("c", "لا توجد مخاطر في المعاملات الجائزة"),
            ),
            correct = "b",
            explanation = "المسار يميز بين الحكم الشرعي والوضع القانوني والمخاطر والملاءمة المالية.",
        ),
        entry(
            lessonId = "new_muslim_roadmap",
            quizId = "roadmap_flexibility",
            question = "ماذا تعني خطة 7 / 30 / 90 يومًا؟",
            options = listOf(
                option("a", "موعد شرعي إلزامي للوصول إلى مستوى محدد"),
                option("b", "تقسيم تعليمي مرن لتنظيم التعلم"),
                option("c", "بديل عن التعلم مع معلم موثوق"),
            ),
            correct = "b",
            explanation = "الخطة أداة تنظيمية مرنة تراعي اختلاف اللغة والبيئة والقدرة.",
        ),
    )

    val byLessonId: Map<String, List<LearningAssessmentEntry>> =
        entries.groupBy { it.lessonId }

    fun forLesson(lessonId: String): List<LearningAssessmentEntry> =
        byLessonId[lessonId].orEmpty()

    fun find(lessonId: String, quizId: String): LearningAssessmentEntry? =
        byLessonId[lessonId]?.firstOrNull { it.quiz.id == quizId }

    private fun entry(
        lessonId: String,
        quizId: String,
        question: String,
        options: List<LearningQuizOption>,
        correct: String,
        explanation: String,
    ) = LearningAssessmentEntry(
        lessonId = lessonId,
        quiz = LearningContentBlock.Quiz(
            id = quizId,
            question = question,
            options = options,
            correctOptionId = correct,
            explanation = explanation,
        ),
    )

    private fun option(id: String, text: String) =
        LearningQuizOption(id = id, text = text)
}
