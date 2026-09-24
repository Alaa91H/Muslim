package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object SeerahLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic("seerah_method", LearnContent.CATEGORY_SEERAH, R.string.learn_topic_seerah_method, R.string.learn_topic_seerah_method_sub, listOf(LearnStep("منهج دراسة السيرة", "المصادر ودرجات اليقين والخط الزمني."))),
        LearnTopic("seerah_early_life", LearnContent.CATEGORY_SEERAH, R.string.learn_topic_seerah_early_life, R.string.learn_topic_seerah_early_life_sub, listOf(LearnStep("قبل البعثة", "مكة والنشأة وخديجة رضي الله عنها والخلوة قبل الوحي."))),
        LearnTopic("seerah_revelation_makkah", LearnContent.CATEGORY_SEERAH, R.string.learn_topic_seerah_revelation_makkah, R.string.learn_topic_seerah_revelation_makkah_sub, listOf(LearnStep("الوحي والمرحلة المكية", "بدء الوحي والدعوة والثبات وموضوعات القرآن المكي."))),
        LearnTopic("seerah_hijrah", LearnContent.CATEGORY_SEERAH, R.string.learn_topic_seerah_hijrah, R.string.learn_topic_seerah_hijrah_sub, listOf(LearnStep("الهجرة", "التحول إلى المدينة والتخطيط والتوكل وبداية المرحلة الجديدة."))),
        LearnTopic("seerah_madinah", LearnContent.CATEGORY_SEERAH, R.string.learn_topic_seerah_madinah, R.string.learn_topic_seerah_madinah_sub, listOf(LearnStep("بناء مجتمع المدينة", "المسجد والتكافل وإدارة المجتمع وتدرج التشريع."))),
        LearnTopic("seerah_major_events", LearnContent.CATEGORY_SEERAH, R.string.learn_topic_seerah_major_events, R.string.learn_topic_seerah_major_events_sub, listOf(LearnStep("الأحداث الكبرى", "بدر وأحد والأحزاب والحديبية وفتح مكة ضمن سياقها."))),
        LearnTopic("seerah_character_legacy", LearnContent.CATEGORY_SEERAH, R.string.learn_topic_seerah_character_legacy, R.string.learn_topic_seerah_character_legacy_sub, listOf(LearnStep("الخلق والقدوة", "كيف تُقرأ السيرة لتعلم الرحمة والعدل والصبر والوفاء."))),
    )
}
