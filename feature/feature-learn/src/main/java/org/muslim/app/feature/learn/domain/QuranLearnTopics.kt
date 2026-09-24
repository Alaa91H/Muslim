package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

/**
 * Legacy LearnTopic cards for the Quran and Tajweed academy category.
 *
 * Long-form lesson bodies live in QuranFoundationsContent and
 * TajweedCourseContent; this catalog only preserves navigation, favourites
 * and resource-backed card metadata while LearnTopic is being migrated.
 */
internal object QuranLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic(
            id = "quran_intro",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_quran_intro,
            subtitleRes = R.string.learn_topic_quran_intro_sub,
            steps = listOf(
                LearnStep("مدخل إلى القرآن", "ما القرآن وكيف تتوازن التلاوة والفهم والعمل."),
            ),
        ),
        LearnTopic(
            id = "quran_etiquette",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_quran_etiquette,
            subtitleRes = R.string.learn_topic_quran_etiquette_sub,
            steps = listOf(
                LearnStep("آداب التلاوة", "النية والاستعاذة والاستماع والتعامل مع المصحف الرقمي."),
            ),
        ),
        LearnTopic(
            id = "quran_structure",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_quran_structure,
            subtitleRes = R.string.learn_topic_quran_structure_sub,
            steps = listOf(
                LearnStep("بنية المصحف", "السور والآيات والأجزاء والمكي والمدني والقراءات."),
            ),
        ),
        LearnTopic(
            id = "quran_understanding",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_quran_understanding,
            subtitleRes = R.string.learn_topic_quran_understanding_sub,
            steps = listOf(
                LearnStep("فهم القرآن", "التدبر والترجمة والتفسير والسياق دون استنباط بغير علم."),
            ),
        ),
        LearnTopic(
            id = "quran_learning_plan",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_quran_learning_plan,
            subtitleRes = R.string.learn_topic_quran_learning_plan_sub,
            steps = listOf(
                LearnStep("خطة تعلم القرآن", "من تحديد المستوى إلى روتين يومي واستفادة من قارئ القرآن."),
            ),
        ),
        LearnTopic(
            id = "tajweed_intro",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_tajweed_intro,
            subtitleRes = R.string.learn_topic_tajweed_intro_sub,
            steps = listOf(
                LearnStep("مدخل إلى التجويد", "هدف التجويد والتلقي وألوان المصحف كوسيلة مساعدة."),
            ),
        ),
        LearnTopic(
            id = "tajweed_makharij",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_tajweed_makharij,
            subtitleRes = R.string.learn_topic_tajweed_makharij_sub,
            steps = listOf(
                LearnStep("المخارج والصفات", "تثبيت الحروف المتقاربة والتدريب العملي على النطق."),
            ),
        ),
        LearnTopic(
            id = "tajweed_noon_meem",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_tajweed_noon_meem,
            subtitleRes = R.string.learn_topic_tajweed_noon_meem_sub,
            steps = listOf(
                LearnStep("النون والميم والغنة", "الإظهار والإدغام والإقلاب والإخفاء وأحكام الميم."),
            ),
        ),
        LearnTopic(
            id = "tajweed_madd",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_tajweed_madd,
            subtitleRes = R.string.learn_topic_tajweed_madd_sub,
            steps = listOf(
                LearnStep("أحكام المد", "المد الطبيعي وأسباب الهمز والسكون والتدريب على المقادير."),
            ),
        ),
        LearnTopic(
            id = "tajweed_qalqalah",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_tajweed_qalqalah,
            subtitleRes = R.string.learn_topic_tajweed_qalqalah_sub,
            steps = listOf(
                LearnStep("القلقلة والتفخيم", "القلقلة والتفخيم والترقيق والراء ولام لفظ الجلالة."),
            ),
        ),
        LearnTopic(
            id = "tajweed_waqf",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_tajweed_waqf,
            subtitleRes = R.string.learn_topic_tajweed_waqf_sub,
            steps = listOf(
                LearnStep("الوقف والابتداء", "علامات الوقف وحفظ المعنى والتعامل مع ضيق النفس."),
            ),
        ),
        LearnTopic(
            id = "tajweed_practice",
            category = CATEGORY_QURAN,
            titleRes = R.string.learn_topic_tajweed_practice,
            subtitleRes = R.string.learn_topic_tajweed_practice_sub,
            steps = listOf(
                LearnStep("تطبيق التجويد", "دورة تدريب تجمع الاستماع والتسجيل والتصحيح والمراجعة."),
            ),
        ),
    )
}
