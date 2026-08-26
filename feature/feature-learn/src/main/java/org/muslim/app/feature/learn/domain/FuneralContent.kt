package org.muslim.app.feature.learn.domain

data class LocalizedFuneralText(
    val arabic: String,
    val english: String,
)

data class FuneralGuideSection(
    val id: String,
    val iconKey: String,
    val title: LocalizedFuneralText,
    val intro: LocalizedFuneralText,
    val steps: List<LocalizedFuneralText>,
    val reference: LocalizedFuneralText,
)

/**
 * A concise orientation for families. It deliberately avoids case-specific
 * rulings; local law, cemetery policy and recognised scholarly differences
 * must be followed in practice.
 */
object FuneralContent {
    val guideSections = listOf(
        FuneralGuideSection(
            id = "first_steps",
            iconKey = "care",
            title = LocalizedFuneralText("عند الاحتضار وبعد الوفاة", "At the time of death and immediately after"),
            intro = LocalizedFuneralText(
                "يُعامل المحتضر وأهله برفق وهدوء، ويُستعان بمسؤول المسجد أو جهة تجهيز الجنائز عند الحاجة.",
                "Treat the dying person and family gently and calmly, and contact the mosque or a funeral service when needed.",
            ),
            steps = listOf(
                LocalizedFuneralText("يُذكَّر المحتضر بلطف بـ«لا إله إلا الله» من غير إلحاح أو جدال.", "Gently prompt the dying person with ‘There is no god but Allah,’ without pressure or argument."),
                LocalizedFuneralText("بعد التحقق الطبي والالتزام بإجراءات البلد، يُدعى للميت بخير ويُستر الجسد، ويُرتّب مع ذويه إجراءات التجهيز والدفن.", "After medical confirmation and following local procedures, make a good supplication, cover the body, and coordinate preparation and burial with the family."),
                LocalizedFuneralText("تُقدَّم الديون والالتزامات والوثائق المهمة إلى المنفذ أو أهل الاختصاص، مع مراعاة النظام المحلي.", "Provide debts, obligations, and important documents to the executor or relevant professionals, observing local law."),
            ),
            reference = LocalizedFuneralText("المراجع: صحيح مسلم 916–920؛ دليل الأحكام التفصيلي داخل التطبيق للاسترشاد.", "References: Sahih Muslim 916–920; consult the app’s detailed guide for orientation."),
        ),
        FuneralGuideSection(
            id = "washing",
            iconKey = "wash",
            title = LocalizedFuneralText("غسل الميت", "Washing the deceased"),
            intro = LocalizedFuneralText(
                "غسل الميت من فروض الكفايات عند جمهور الفقهاء؛ والأصل أن يتولاه شخص موثوق ومدرّب مع مراعاة الخصوصية واللوائح الصحية.",
                "Washing the deceased is generally a communal obligation; it should be performed by a trusted, trained person while observing privacy and health rules.",
            ),
            steps = listOf(
                LocalizedFuneralText("يُجهَّز مكان ساتر، ويقتصر الحضور على القائمين بالغسل ومن يعينهم، مع ستر العورة وحفظ كرامة الميت.", "Use a private space; only washers and necessary helpers should attend, while preserving the deceased’s dignity."),
                LocalizedFuneralText("يكون الغسل بلطف وبماء طهور، ويبدأ عادةً باليمين ومواضع الوضوء؛ ويُعاد عند الحاجة حتى يتحقق التنظيف.", "Wash gently with clean water, generally beginning with the right side and ablution areas; repeat as needed until clean."),
                LocalizedFuneralText("لا تُطبّق هذه الخطوات في الحالات الطبية أو النظامية الخاصة إلا بتوجيه مختص؛ اسأل الجهة المسؤولة عن الغسل في مدينتك.", "Do not apply these steps in special medical or legal cases without professional direction; ask the local funeral service for guidance."),
            ),
            reference = LocalizedFuneralText("المرجع: أحكام الجنائز، باب تغسيل الميت وتكفينه؛ توجد تفاصيل معتبرة تختلف بين المذاهب.", "Reference: funeral jurisprudence on washing and shrouding; valid details differ across schools."),
        ),
        FuneralGuideSection(
            id = "shrouding",
            iconKey = "shroud",
            title = LocalizedFuneralText("التكفين", "Shrouding"),
            intro = LocalizedFuneralText(
                "الكفن حق للميت، والمقصود ستره بلباس لائق بسيط من غير إسراف؛ ويتولى فريق التجهيز المحلي ترتيب التفاصيل المناسبة.",
                "The shroud is a right of the deceased. Its purpose is dignified, simple covering without extravagance; the local preparation team can arrange the details.",
            ),
            steps = listOf(
                LocalizedFuneralText("يُجفف الجسد بعد الغسل ويُكفَّن بما يستره على وجه يراعي الكرامة والبساطة.", "After washing, dry and shroud the body in a way that preserves dignity and simplicity."),
                LocalizedFuneralText("تختلف هيئة وعدد قطع الكفن في بعض التفاصيل الفقهية؛ اتبع القائمين المعتمدين أو مذهب المجتمع المحلي.", "The form and number of shroud pieces differ in some jurisprudential details; follow qualified local staff or the community’s school."),
                LocalizedFuneralText("تُراعى احتياطات الصحة العامة وإجراءات المقبرة أو المستشفى، ولا تُؤخر الإجراءات اللازمة بلا سبب.", "Observe public-health precautions and cemetery or hospital procedures, and do not delay necessary arrangements without reason."),
            ),
            reference = LocalizedFuneralText("المرجع: أحكام الجنائز، باب أحكام التكفين.", "Reference: funeral jurisprudence, section on shrouding."),
        ),
        FuneralGuideSection(
            id = "prayer",
            iconKey = "prayer",
            title = LocalizedFuneralText("صلاة الجنازة", "Funeral prayer"),
            intro = LocalizedFuneralText(
                "صلاة الجنازة دعاء للميت، وهي فرض كفاية عند جمهور الفقهاء. تُقام عادةً جماعةً بإشراف الإمام أو الجهة المنظمة.",
                "The funeral prayer is a supplication for the deceased and is generally a communal obligation. It is normally led in congregation by an imam or organiser.",
            ),
            steps = listOf(
                LocalizedFuneralText("يقف المصلون بخشوع واستقبال القبلة، وهي صلاة بلا ركوع ولا سجود.", "Worshippers stand reverently facing the qiblah; it is a prayer without bowing or prostration."),
                LocalizedFuneralText("الصورة الشائعة: أربع تكبيرات؛ بعد الأولى الفاتحة، وبعد الثانية الصلاة على النبي ﷺ، وبعد الثالثة الدعاء للميت، ثم التسليم بعد الرابعة.", "The common form has four takbirs: Al-Fatihah after the first, blessings on the Prophet after the second, supplication for the deceased after the third, then salam after the fourth."),
                LocalizedFuneralText("إن التبس عليك الترتيب، تابع الإمام بهدوء؛ فالتفاصيل والسنن قد تختلف بحسب المذهب.", "If you are unsure of the order, calmly follow the imam; details and recommended practices can differ by school."),
            ),
            reference = LocalizedFuneralText("المراجع: صحيح مسلم، كتاب الجنائز؛ أحكام الجنائز، باب الصلاة على الميت.", "References: Sahih Muslim, Book of Funerals; funeral jurisprudence, section on funeral prayer."),
        ),
        FuneralGuideSection(
            id = "condolences",
            iconKey = "support",
            title = LocalizedFuneralText("التعزية والمقابر", "Condolences and cemeteries"),
            intro = LocalizedFuneralText(
                "المقصود من التعزية مواساة أهل الميت والدعاء له، ومن زيارة المقابر الاعتبار والدعاء؛ وتُراعى الآداب والأنظمة المحلية.",
                "Condolences are meant to comfort the bereaved and pray for the deceased; cemetery visits foster reflection and supplication, subject to local etiquette and rules.",
            ),
            steps = listOf(
                LocalizedFuneralText("يُعزّى أهل الميت بكلام طيب، ويُدعى للميت بالمغفرة ولأهله بالصبر. يُحترم حزنهم ولا تُفرض عليهم ضيافة أو إجراءات مرهقة.", "Offer kind words of condolence, pray for forgiveness for the deceased and patience for the family. Respect their grief without imposing hospitality or burdens."),
                LocalizedFuneralText("اتبع تعليمات المقبرة وقوانين البلد في أوقات الدفن والزيارة، واسأل الإمام المحلي عند اختلاف العرف أو الحكم.", "Follow cemetery instructions and local law regarding burial and visiting; ask a local imam when custom or rulings differ."),
                LocalizedFuneralText("عند الزيارة يُسلَّم على أهل القبور ويُدعى لهم، مع اجتناب كل ما يخالف التوحيد أو يخل بحرمة المكان.", "When visiting, offer greetings and supplicate for those buried, avoiding anything that conflicts with monotheism or the sanctity of the place."),
            ),
            reference = LocalizedFuneralText("المراجع: صحيح مسلم 918، 919، 923؛ أحكام الجنائز، باب التعزية وزيارة القبور.", "References: Sahih Muslim 918, 919, 923; funeral jurisprudence, sections on condolences and cemetery visits."),
        ),
    )

    val willChecklist = listOf(
        LocalizedFuneralText("دوّن الديون والحقوق والالتزامات المالية بوضوح، ثم راجعها مع أهل الاختصاص.", "Record debts, rights, and financial obligations clearly, then review them with qualified professionals."),
        LocalizedFuneralText("سمِّ منفذًا أو شخصًا موثوقًا ووسيلة تواصل واضحة بعد أخذ موافقته.", "Name an executor or trusted person and clear contact information after obtaining their agreement."),
        LocalizedFuneralText("دوّن ملاحظات الوصاية على القُصَّر إن وجدت، وراعِ ما يلزم قانونًا في بلدك.", "Record guardianship notes for minors where applicable, observing what local law requires."),
        LocalizedFuneralText("اكتب وصايا التجهيز والجنازة بما لا يخالف الشرع أو اللوائح المحلية، واترك التنفيذ للجهة المؤهلة.", "Write funeral-preparation wishes that do not conflict with Islamic guidance or local rules, and leave execution to qualified staff."),
        LocalizedFuneralText("راجع الوصية عند تغيّر الأسرة أو الديون أو الأصول أو القانون، واستعن بعالم موثوق ومحامٍ أو كاتب عدل.", "Review the will when family, debts, assets, or law change, and consult a qualified scholar and lawyer or notary."),
    )

    val willReferences = LocalizedFuneralText(
        "التأصيل: البقرة 2:180؛ صحيح البخاري 2738. الوصية المالية والميراث يحتاجان مراجعة عالم موثوق ومحامٍ/كاتب عدل وفق بلدك.",
        "Basis: Qur’an 2:180; Sahih al-Bukhari 2738. Financial bequests and inheritance need review by a qualified scholar and a lawyer/notary in your jurisdiction.",
    )
}
