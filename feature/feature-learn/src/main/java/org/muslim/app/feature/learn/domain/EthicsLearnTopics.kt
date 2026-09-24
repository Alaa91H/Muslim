package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object EthicsLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic("ethics_foundation", LearnContent.CATEGORY_ETHICS, R.string.learn_topic_ethics_foundation, R.string.learn_topic_ethics_foundation_sub, listOf(LearnStep("الأخلاق في الدين", "العدل والرحمة والنية والحدود والمراجعة اليومية."))),
        LearnTopic("ethics_speech", LearnContent.CATEGORY_ETHICS, R.string.learn_topic_ethics_speech, R.string.learn_topic_ethics_speech_sub, listOf(LearnStep("أخلاق الكلام", "الصدق والسخرية والغيبة والخصوصية والنشر الرقمي."))),
        LearnTopic("ethics_family", LearnContent.CATEGORY_ETHICS, R.string.learn_topic_ethics_family, R.string.learn_topic_ethics_family_sub, listOf(LearnStep("الأسرة والوالدان", "الإحسان والاختلاف والحدود والتعامل مع الأذى."))),
        LearnTopic("ethics_neighbours", LearnContent.CATEGORY_ETHICS, R.string.learn_topic_ethics_neighbours, R.string.learn_topic_ethics_neighbours_sub, listOf(LearnStep("الجيران والمجتمع", "حسن الجوار والمساحات المشتركة وخدمة الناس."))),
        LearnTopic("ethics_conflict", LearnContent.CATEGORY_ETHICS, R.string.learn_topic_ethics_conflict, R.string.learn_topic_ethics_conflict_sub, listOf(LearnStep("الغضب والخلاف", "إدارة الغضب والحوار وطلب الحق دون ظلم."))),
        LearnTopic("ethics_privacy", LearnContent.CATEGORY_ETHICS, R.string.learn_topic_ethics_privacy, R.string.learn_topic_ethics_privacy_sub, listOf(LearnStep("الخصوصية والحدود", "الاستئذان والأسرار والموافقة والسلامة."))),
        LearnTopic("ethics_work_digital", LearnContent.CATEGORY_ETHICS, R.string.learn_topic_ethics_work_digital, R.string.learn_topic_ethics_work_digital_sub, listOf(LearnStep("العمل والحياة الرقمية", "العقود والأمانة والبيانات وحقوق الآخرين على الإنترنت."))),
    )
}
