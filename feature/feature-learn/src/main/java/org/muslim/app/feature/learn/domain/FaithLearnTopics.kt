package org.muslim.app.feature.learn.domain

import org.muslim.app.feature.learn.R

internal object FaithLearnTopics {
    val topics: List<LearnTopic> = listOf(
        LearnTopic("faith_tawhid_worship", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_tawhid_worship, R.string.learn_topic_faith_tawhid_worship_sub, listOf(LearnStep("التوحيد والعبادة", "دعوة الرسل إلى عبادة الله وحده وفهم معنى العبادة دون تحويل الدرس إلى أحكام على الأشخاص."))),
        LearnTopic("faith_knowing_allah", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_knowing_allah, R.string.learn_topic_faith_knowing_allah_sub, listOf(LearnStep("معرفة الله", "الوحدانية والأسماء والصفات وإثبات ما جاء في الوحي بلا مماثلة."))),
        LearnTopic("faith_angels", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_angels, R.string.learn_topic_faith_angels_sub, listOf(LearnStep("الإيمان بالملائكة", "ما ثبت في الوحي وحدود ما يمكن الجزم به في عالم الغيب."))),
        LearnTopic("faith_books", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_books, R.string.learn_topic_faith_books_sub, listOf(LearnStep("الكتب والوحي", "الإيمان بما أنزل الله وفهم مكانة القرآن في حياة المسلم."))),
        LearnTopic("faith_messengers", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_messengers, R.string.learn_topic_faith_messengers_sub, listOf(LearnStep("الرسل والنبوة", "وحدة أصل دعوتهم، الإيمان بهم جميعًا، والتمييز بين النص الثابت والتفاصيل التاريخية."))),
        LearnTopic("faith_last_day", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_last_day, R.string.learn_topic_faith_last_day_sub, listOf(LearnStep("اليوم الآخر", "الموت والبعث والحساب والجزاء وأثر الإيمان بالآخرة في السلوك."))),
        LearnTopic("faith_qadar", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_qadar, R.string.learn_topic_faith_qadar_sub, listOf(LearnStep("الإيمان بالقدر", "العلم والقدر والمسؤولية والأخذ بالأسباب دون تبسيط للمسائل الدقيقة."))),
        LearnTopic("faith_questions", LearnContent.CATEGORY_FAITH, R.string.learn_topic_faith_questions, R.string.learn_topic_faith_questions_sub, listOf(LearnStep("الأسئلة والشبهات", "منهج التحقق من النص والسياق والمصدر والتعامل المتوازن مع الإشكالات."))),
    )
}
