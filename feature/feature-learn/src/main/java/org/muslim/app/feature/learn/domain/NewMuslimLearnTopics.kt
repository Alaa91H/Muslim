package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object NewMuslimLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic("new_muslim_welcome", LearnContent.CATEGORY_NEW_MUSLIM, R.string.learn_topic_new_muslim_welcome, R.string.learn_topic_new_muslim_welcome_sub, listOf(LearnStep("بداية هادئة", "الأولويات ومصادر الدعم والتدرج دون ضغط."))),
        LearnTopic("new_muslim_belief", LearnContent.CATEGORY_NEW_MUSLIM, R.string.learn_topic_new_muslim_belief, R.string.learn_topic_new_muslim_belief_sub, listOf(LearnStep("الإيمان والشهادة", "معنى الشهادة وخريطة الإيمان الأولى والتعامل مع الأسئلة."))),
        LearnTopic("new_muslim_prayer", LearnContent.CATEGORY_NEW_MUSLIM, R.string.learn_topic_new_muslim_prayer, R.string.learn_topic_new_muslim_prayer_sub, listOf(LearnStep("الصلاة خطوة بخطوة", "الأوقات والركعة والفاتحة وكيف تستخدم أقسام التطبيق."))),
        LearnTopic("new_muslim_purification", LearnContent.CATEGORY_NEW_MUSLIM, R.string.learn_topic_new_muslim_purification, R.string.learn_topic_new_muslim_purification_sub, listOf(LearnStep("الطهارة للمبتدئ", "الوضوء والغسل وما تحتاجه للصلاة دون وسواس."))),
        LearnTopic("new_muslim_quran", LearnContent.CATEGORY_NEW_MUSLIM, R.string.learn_topic_new_muslim_quran, R.string.learn_topic_new_muslim_quran_sub, listOf(LearnStep("القرآن للمبتدئ", "الترجمة والقاعدة النورانية وروتين يومي قصير."))),
        LearnTopic("new_muslim_daily_life", LearnContent.CATEGORY_NEW_MUSLIM, R.string.learn_topic_new_muslim_daily_life, R.string.learn_topic_new_muslim_daily_life_sub, listOf(LearnStep("الحياة اليومية", "الأسرة والطعام والمال والعادات والأذكار بالتدرج."))),
        LearnTopic("new_muslim_roadmap", LearnContent.CATEGORY_NEW_MUSLIM, R.string.learn_topic_new_muslim_roadmap, R.string.learn_topic_new_muslim_roadmap_sub, listOf(LearnStep("خطة 7/30/90 يومًا", "خارطة مرنة تحول التعلم من فوضى إلى مراحل واضحة."))),
    )
}
