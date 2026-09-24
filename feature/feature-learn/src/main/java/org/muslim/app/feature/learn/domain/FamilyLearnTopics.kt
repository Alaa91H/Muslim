package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FamilyLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic("family_intro", LearnContent.CATEGORY_FAMILY, R.string.learn_topic_family_intro, R.string.learn_topic_family_intro_sub, listOf(LearnStep("مدخل إلى الأسرة", "المودة والحقوق والمهارات والحدود بين الفقه والقانون والحياة اليومية."))),
        LearnTopic("family_spouse_selection", LearnContent.CATEGORY_FAMILY, R.string.learn_topic_family_spouse_selection, R.string.learn_topic_family_spouse_selection_sub, listOf(LearnStep("اختيار الزوج", "الدين والخلق والتوافق والأسئلة المهمة قبل الزواج."))),
        LearnTopic("family_marriage_contract", LearnContent.CATEGORY_FAMILY, R.string.learn_topic_family_marriage_contract, R.string.learn_topic_family_marriage_contract_sub, listOf(LearnStep("العقد والمهر", "التوثيق والشروط والمهر وحفظ الحقوق."))),
        LearnTopic("family_marital_life", LearnContent.CATEGORY_FAMILY, R.string.learn_topic_family_marital_life, R.string.learn_topic_family_marital_life_sub, listOf(LearnStep("الحياة الزوجية", "التواصل والمال والخصوصية والمعاشرة بالمعروف."))),
        LearnTopic("family_parenting", LearnContent.CATEGORY_FAMILY, R.string.learn_topic_family_parenting, R.string.learn_topic_family_parenting_sub, listOf(LearnStep("التربية", "القدوة والانضباط المناسب للعمر وبناء الإيمان في البيت."))),
        LearnTopic("family_kinship", LearnContent.CATEGORY_FAMILY, R.string.learn_topic_family_kinship, R.string.learn_topic_family_kinship_sub, listOf(LearnStep("القرابة والرعاية", "صلة الرحم والحدود والوالدان والرعاية عند المرض والكبر."))),
        LearnTopic("family_conflict_separation", LearnContent.CATEGORY_FAMILY, R.string.learn_topic_family_conflict_separation, R.string.learn_topic_family_conflict_separation_sub, listOf(LearnStep("الخلاف والانفصال", "الوساطة والسلامة والطلاق والخلع وحفظ الوقائع والحقوق."))),
    )
}
