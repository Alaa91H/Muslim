package org.example.islamicapp.feature.learn.domain

import org.example.islamicapp.feature.learn.R

/**
 * Structured learning content (PROJECT_PROMPT.md §6 Phase 5): step-by-step
 * wudu / ghusl / tayammum / salah guides, special prayers and a rak'ah
 * reference table.
 *
 * Religious content note (§10): the texts follow mainstream, widely
 * published fiqh wording. Madhhab differences are noted neutrally. Always
 * subject to independent scholarly review before release.
 */
data class LearnStep(
    val title: String,
    val description: String,
    val dua: String? = null,
)

data class LearnTopic(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val steps: List<LearnStep>,
    val notes: String? = null,
)

object LearnContent {

    val topics: List<LearnTopic> = listOf(
        LearnTopic(
            id = "wudu",
            titleRes = R.string.learn_topic_wudu,
            subtitleRes = R.string.learn_topic_wudu_sub,
            steps = listOf(
                LearnStep("النية", "ينوي الطهارة في قلبه، ويسمي (بسم الله)."),
                LearnStep("غسل الكفين", "يغسل كفيه ثلاث مرات."),
                LearnStep("المضمضة والاستنشاق", "يتمضمض ويستنشق ثلاثًا، ويستنثر."),
                LearnStep("غسل الوجه", "يغسل وجهه ثلاثًا من منابت الشعر إلى الذقن."),
                LearnStep("غسل اليدين إلى المرفقين", "يغسل يده اليمنى ثم اليسرى إلى المرفقين ثلاثًا."),
                LearnStep("مسح الرأس والأذنين", "يمسح رأسه مرة، ويمسح أذنيه بالماء الجديد."),
                LearnStep("غسل الرجلين", "يغسل رجله اليمنى ثم اليسرى إلى الكعبين ثلاثًا."),
                LearnStep(
                    "الدعاء بعد الوضوء",
                    "يستحب بعد الفراغ أن يقول: «أشهد أن لا إله إلا الله وحده لا شريك له، وأشهد أن محمدًا عبده ورسوله».",
                    dua = "اللهم اجعلني من التوابين واجعلني من المتطهرين",
                ),
            ),
            notes = "الترتيب والموالاة من الفرائض عند الجمهور. تكرار الغسل ثلاثًا سنة. خلاف مذهبي يسير في مسح الأذنين (بماء الرأس أو بماء جديد) يعرضه المختصون بحياد.",
        ),
        LearnTopic(
            id = "ghusl",
            titleRes = R.string.learn_topic_ghusl,
            subtitleRes = R.string.learn_topic_ghusl_sub,
            steps = listOf(
                LearnStep("النية والتسمية", "ينوي الاغتسال ويسمي."),
                LearnStep("غسل اليدين والفرج", "يغسل يديه، ثم يغسل فرجه."),
                LearnStep("الوضوء", "يتوضأ وضوءه للصلاة."),
                LearnStep("إفاضة الماء على الرأس", "يُفْضِئ الماء على رأسه ثلاثًا مع تدليك الأصول."),
                LearnStep("غسل بقية الجسد", "يغسل سائر جسده، ويتيامن في الغسل."),
            ),
            notes = "الغسل واجب عند الجنابة والحيض والنفاس. يكفي أن يعمّ الماء جميع البدن مع النية والمضمضة والاستنشاق عند الجمهور.",
        ),
        LearnTopic(
            id = "tayammum",
            titleRes = R.string.learn_topic_tayammum,
            subtitleRes = R.string.learn_topic_tayammum_sub,
            steps = listOf(
                LearnStep("النية", "ينوي التيمم لرفع الحدث — عند فقد الماء أو العجز عن استعماله."),
                LearnStep("الضربة الأولى", "يضرب يديه على التراب الطاهر."),
                LearnStep("مسح الوجه", "يمسح وجهه بهما."),
                LearnStep("الضربة الثانية ومسح اليدين", "يضرب ضربة ثانية ويمسح يديه إلى المرفقين."),
            ),
            notes = "يجوز التيمم عند فقد الماء أو المرض الذي يمنع استعماله. يبطل التيمم بوجود الماء (مع القدرة) وبما يبطل الوضوء.",
        ),
        LearnTopic(
            id = "salah",
            titleRes = R.string.learn_topic_salah,
            subtitleRes = R.string.learn_topic_salah_sub,
            steps = listOf(
                LearnStep("القيام والتكبير", "يستقبل القبلة، ويرفع يديه مكبِّرًا (تكبيرة الإحرام) قائلًا: «الله أكبر»."),
                LearnStep("الاستفتاح والقراءة", "يقرأ دعاء الاستفتاح ثم الفاتحة وما تيسر من القرآن."),
                LearnStep("الركوع", "يركع قائلًا: «الله أكبر»، ويطمئن، ويقول: «سبحان ربي العظيم» ثلاثًا."),
                LearnStep("الرفع من الركوع", "يرفع قائلًا: «سمع الله لمن حمده، ربنا ولك الحمد»."),
                LearnStep("السجود الأول", "يسجد قائلًا: «الله أكبر»، ويقول: «سبحان ربي الأعلى» ثلاثًا."),
                LearnStep("الجلسة بين السجدتين", "يجلس مطمئنًا ويقول: «رب اغفر لي وارحمني»."),
                LearnStep("السجود الثاني", "يسجد الثانية كالأولى، ثم يقوم للركعة التالية."),
                LearnStep("التشهد الأخير والتسليم", "في آخر الصلاة يجلس للتشهد، ويصلي على النبي ﷺ، ثم يسلم عن يمينه وعن شماله."),
            ),
            notes = "الطمأنينة ركن عند الجمهور. التفاصيل الدقيقة (موضع اليدين، كيفية السجود، القنوت) تختلف بين المذاهب الأربعة — تُعرض فروقها بحياد في قسم الفروقات المذهبية.",
        ),
        LearnTopic(
            id = "special",
            titleRes = R.string.learn_topic_special,
            subtitleRes = R.string.learn_topic_special_sub,
            steps = listOf(
                LearnStep("صلاة الجنازة", "أربع تكبيرات بلا ركوع ولا سجود؛ بعد الأولى الفاتحة، وبعد الثانية الصلاة على النبي ﷺ، وبعد الثالثة الدعاء للميت، وبعد الرابعة التسليم."),
                LearnStep("صلاة العيدين", "ركعتان: يكبر في الأولى سبع تكبيرات (بعد تكبيرة الإحرام) وفي الثانية خمسًا (عند الجمهور)، ثم يخطب الإمام بعدها."),
                LearnStep("التراويح", "قيام رمضان بعد صلاة العشاء، مثنى مثنى، ويُختم بوتر."),
                LearnStep("الوتر", "ركعة فأكثر (وصلًا أو فصلًا)؛ يختم بها صلاة الليل."),
                LearnStep("الضحى", "ركعتان فأكثر، من ارتفاع الشمس إلى قبل الزوال."),
                LearnStep("التهجد", "قيام الليل بعد نوم، مثنى مثنى، وأفضله في الثلث الأخير."),
                LearnStep("الاستخارة", "ركعتان من غير الفريضة ثم دعاء الاستخارة: «اللهم إني أستخيرك بعلمك...»."),
                LearnStep("الجمع والقصر للمسافر", "يقصر المسافر الرباعية إلى ركعتين، ويجمع بين الظهر والعصر والمغرب والعشاء جمع تقديم أو تأخير."),
            ),
        ),
        LearnTopic(
            id = "rakats",
            titleRes = R.string.learn_topic_rakats,
            subtitleRes = R.string.learn_topic_rakats_sub,
            steps = listOf(
                LearnStep("الفجر", "ركعتان، تجهر فيهما القراءة."),
                LearnStep("الظهر", "أربع ركعات، تُسرّ القراءة."),
                LearnStep("العصر", "أربع ركعات، تُسرّ القراءة."),
                LearnStep("المغرب", "ثلاث ركعات، يجهر في الأوليين."),
                LearnStep("العشاء", "أربع ركعات، يجهر في الأوليين."),
                LearnStep("الجمعة", "ركعتان بعد الخطبة — بدل الظهر."),
                LearnStep("العيدان", "ركعتان."),
                LearnStep("التراويح والوتر", "التراويح 8 أو 20 ركعة (خلاف مشهور)، ثم الوتر 1–11 ركعة."),
            ),
        ),
        LearnTopic(
            id = "madhhab",
            titleRes = R.string.learn_topic_madhhab,
            subtitleRes = R.string.learn_topic_madhhab_sub,
            steps = listOf(
                LearnStep("مسح الرأس", "الحنفية: مسح ربع الرأس. الشافعية: مسح كل الرأس. المالكية والحنابلة: مسح جميع الرأس أيضًا (الحنابلة يمسح الأذنين معه)."),
                LearnStep("موضع اليدين في الصلاة", "الحنفية: تحت السرة. الشافعية: تحت الصدر. المالكية: تحت الصدر (بعد التكبير). الحنابلة: تحت السرة أو فوقها."),
                LearnStep("القنوت", "شافعية: في الفجر دائمًا. الحنفية: في الوتر. المالكية والحنابلة: عند النوازل في الفريضة."),
                LearnStep("التشهد الأول", "الجميع يتشهدون بعد الركعتين في الثلاثية والرباعية، ويقومون بعدها."),
                LearnStep("جلسة التشهد", "الافتراش في الجلسات كلها عند الشافعية؛ والتورك في الأخيرة عند الحنفية والمالكية والحنابلة (مع خلاف في موضعه)."),
            ),
            notes = "هذه الفروق عرض محايد لا ترجيح فيه؛ يختار المسلم ما يطمئن إليه مع مراجعة أهل العلم. المشروع لا يرجّح مذهبًا على آخر (§6 المرحلة 5 و§10).",
        ),
    )

    fun byId(id: String): LearnTopic? = topics.firstOrNull { it.id == id }
}
