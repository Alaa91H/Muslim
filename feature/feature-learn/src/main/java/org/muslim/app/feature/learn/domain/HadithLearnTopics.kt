package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object HadithLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic("sunnah_intro", LearnContent.CATEGORY_SUNNAH, R.string.learn_topic_sunnah_intro, R.string.learn_topic_sunnah_intro_sub, listOf(LearnStep("السنة والحديث", "المعنى والمكانة ومنهج التعلم المسؤول."))),
        LearnTopic("hadith_anatomy", LearnContent.CATEGORY_SUNNAH, R.string.learn_topic_hadith_anatomy, R.string.learn_topic_hadith_anatomy_sub, listOf(LearnStep("الإسناد والمتن", "الرواة والطرق وكيف يبنى المرجع الحديثي."))),
        LearnTopic("hadith_grades", LearnContent.CATEGORY_SUNNAH, R.string.learn_topic_hadith_grades, R.string.learn_topic_hadith_grades_sub, listOf(LearnStep("درجات الحديث", "الصحيح والحسن والضعيف والموضوع دون تبسيط مخل."))),
        LearnTopic("hadith_verification", LearnContent.CATEGORY_SUNNAH, R.string.learn_topic_hadith_verification, R.string.learn_topic_hadith_verification_sub, listOf(LearnStep("التحقق من الحديث", "البحث عن المصدر واللفظ والدرجة قبل النشر."))),
        LearnTopic("hadith_understanding", LearnContent.CATEGORY_SUNNAH, R.string.learn_topic_hadith_understanding, R.string.learn_topic_hadith_understanding_sub, listOf(LearnStep("فهم الحديث", "السياق والجمع بين النصوص والفرق بين الرواية والاستنباط."))),
        LearnTopic("hadith_library_guide", LearnContent.CATEGORY_SUNNAH, R.string.learn_topic_hadith_library_guide, R.string.learn_topic_hadith_library_guide_sub, listOf(LearnStep("دليل مكتبة الحديث", "استخدام المجموعات والبحث والدرجة والمصدر داخل التطبيق."))),
    )
}
