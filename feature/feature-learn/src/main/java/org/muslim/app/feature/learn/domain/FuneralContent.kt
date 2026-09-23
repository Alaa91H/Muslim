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

data class WillEducationSection(
    val id: String,
    val title: LocalizedFuneralText,
    val intro: LocalizedFuneralText,
    val steps: List<LocalizedFuneralText>,
    val reference: LocalizedFuneralText,
)

/**
 * General educational content for funerals and Islamic-will preparation.
 *
 * The material intentionally avoids issuing case-specific rulings. Local law,
 * medical requirements, cemetery policy and recognised scholarly differences
 * must be considered in real situations.
 */
object FuneralContent {
    val quickActionSteps = listOf(
        LocalizedFuneralText(
            "تحقق أولًا من الإجراءات الطبية والرسمية المطلوبة لإثبات الوفاة، ولا تنقل الجثمان أو تبدأ التجهيز قبل السماح النظامي.",
            "First complete the medical and official steps required to confirm the death. Do not move or prepare the body before local rules allow it.",
        ),
        LocalizedFuneralText(
            "تواصل مع الأسرة والمسجد أو جهة تجهيز جنائز موثوقة لتنسيق الغسل والتكفين والصلاة والدفن.",
            "Contact the family and a trusted mosque or Muslim funeral service to coordinate washing, shrouding, prayer, and burial.",
        ),
        LocalizedFuneralText(
            "اجمع وثائق الهوية والتأمين أو التصاريح والمعلومات التي تطلبها المستشفى أو الجهة الرسمية أو المقبرة.",
            "Gather identity documents, insurance or permits, and any information requested by the hospital, authorities, or cemetery.",
        ),
        LocalizedFuneralText(
            "احفظ كرامة الميت وخصوصيته، ولا تنشر الصور أو التفاصيل الحساسة إلا لضرورة وبمراعاة الأسرة والأنظمة.",
            "Protect the deceased person's dignity and privacy. Do not circulate images or sensitive details unless necessary and appropriate.",
        ),
        LocalizedFuneralText(
            "رتب صلاة الجنازة والدفن وفق الإمكانات واللوائح المحلية، واسأل الإمام في المسائل التي تختلف باختلاف المذهب أو الحالة.",
            "Arrange the funeral prayer and burial within local practical and legal requirements, and ask an imam about school- or case-specific rulings.",
        ),
        LocalizedFuneralText(
            "بعد الدفن رتّب الديون والأمانات والوثائق والوصية مع الشخص المسؤول وأهل الاختصاص بدل اتخاذ قرارات مالية متسرعة.",
            "After burial, organise debts, entrusted property, documents, and the will with the responsible person and qualified advisers rather than making rushed financial decisions.",
        ),
    )

    val guideSections = listOf(
        FuneralGuideSection(
            id = "first_steps",
            iconKey = "care",
            title = LocalizedFuneralText(
                "عند الاحتضار وبعد الوفاة مباشرة",
                "At the time of death and immediately after",
            ),
            intro = LocalizedFuneralText(
                "يُعامل المحتضر وأهله برفق وهدوء، ويُستعان بمسؤول المسجد أو جهة تجهيز الجنائز عند الحاجة.",
                "Treat the dying person and family gently and calmly, and contact the mosque or a funeral service when needed.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "يُذكَّر المحتضر بلطف بـ«لا إله إلا الله» من غير إلحاح أو جدال، ويُترك له الهدوء والخصوصية.",
                    "Gently prompt the dying person with 'There is no god but Allah' without pressure or argument, while preserving calm and privacy.",
                ),
                LocalizedFuneralText(
                    "بعد التحقق من الوفاة طبيًا والالتزام بإجراءات البلد، يُدعى للميت بخير ويُستر الجسد وتُحفظ كرامته.",
                    "After medical confirmation and compliance with local procedures, make good supplication, cover the body, and preserve the deceased person's dignity.",
                ),
                LocalizedFuneralText(
                    "أبلغ أفراد الأسرة والجهة التي ستتولى التجهيز، وتجنب الازدحام والتصوير والنشر غير الضروري.",
                    "Inform the family and the organisation responsible for preparation, and avoid unnecessary crowding, photography, or sharing.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح مسلم، كتاب الجنائز؛ كتب الفقه المعتمدة في أبواب الاحتضار والجنائز.",
                "General references: Sahih Muslim, Book of Funerals; recognised fiqh works on dying and funerals.",
            ),
        ),
        FuneralGuideSection(
            id = "documents_and_coordination",
            iconKey = "documents",
            title = LocalizedFuneralText(
                "الإجراءات الطبية والوثائق والتنسيق",
                "Medical procedures, documents, and coordination",
            ),
            intro = LocalizedFuneralText(
                "الجانب الشرعي لا يلغي الإجراءات النظامية؛ المستشفى والبلدية والمقبرة قد تطلب وثائق وتصاريح تختلف باختلاف البلد.",
                "Religious guidance does not replace official procedures. Hospitals, authorities, and cemeteries may require documents and permits that differ by country.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "اسأل الجهة الطبية أو الرسمية عن شهادة الوفاة والتصاريح ومواعيد تسليم الجثمان قبل أي ترتيب عملي.",
                    "Ask the medical or official authority about the death certificate, permits, and release procedure before practical arrangements.",
                ),
                LocalizedFuneralText(
                    "عيّن شخصًا واحدًا من الأسرة للتواصل مع المسجد والمقبرة وشركة الجنازة لتقليل التعارض وتكرار المعلومات.",
                    "Choose one family contact to coordinate with the mosque, cemetery, and funeral service to reduce conflicting instructions.",
                ),
                LocalizedFuneralText(
                    "في الوفاة المفاجئة أو الحوادث أو الحالات الطبية الخاصة اتبع تعليمات الجهات المختصة ولا تتدخل في الجثمان دون إذن.",
                    "For sudden deaths, accidents, or special medical cases, follow professional instructions and do not handle the body without authorisation.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المرجع العملي: الأنظمة الصحية والمدنية المحلية، مع الرجوع للإمام أو جهة الجنائز في الأحكام الشرعية.",
                "Practical reference: local health and civil regulations, alongside qualified Islamic guidance for religious rulings.",
            ),
        ),
        FuneralGuideSection(
            id = "washing",
            iconKey = "wash",
            title = LocalizedFuneralText("غسل الميت", "Washing the deceased"),
            intro = LocalizedFuneralText(
                "غسل الميت من فروض الكفايات عند جمهور الفقهاء، والأصل أن يتولاه شخص موثوق ومدرب مع مراعاة الخصوصية واللوائح الصحية.",
                "Washing the deceased is generally a communal obligation. It should be performed by a trusted, trained person while observing privacy and health rules.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "يُجهز مكان ساتر، ويقتصر الحضور على القائمين بالغسل ومن يحتاجون إلى مساعدتهم، مع ستر العورة وحفظ الكرامة.",
                    "Use a private space. Attendance should be limited to the washers and necessary helpers while preserving modesty and dignity.",
                ),
                LocalizedFuneralText(
                    "يكون الغسل بلطف وبماء طهور، ويبدأ عادة باليمين ومواضع الوضوء، ويكرر بقدر الحاجة للتنظيف.",
                    "Wash gently with clean water, commonly beginning with the right side and ablution areas, and repeat as needed for cleanliness.",
                ),
                LocalizedFuneralText(
                    "الحالات الطبية الخاصة أو المعدية أو التي تدخلت فيها الجهات الجنائية تحتاج إلى تعليمات مهنية قبل تطبيق الإجراءات المعتادة.",
                    "Special medical, infectious, or forensic cases require professional instructions before ordinary washing procedures are applied.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح البخاري وصحيح مسلم، كتاب الجنائز؛ أبواب غسل الميت في كتب الفقه.",
                "General references: Sahih al-Bukhari and Sahih Muslim, Books of Funerals; fiqh chapters on washing the deceased.",
            ),
        ),
        FuneralGuideSection(
            id = "shrouding",
            iconKey = "shroud",
            title = LocalizedFuneralText("التكفين", "Shrouding"),
            intro = LocalizedFuneralText(
                "المقصود من الكفن ستر الميت بلباس لائق بسيط من غير إسراف، مع مراعاة التفاصيل الفقهية والعملية المحلية.",
                "The shroud provides dignified, simple covering without extravagance, while respecting fiqh details and local practical requirements.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "يُجفف الجسد بعد الغسل ويُكفن بما يستره ويحفظ كرامته.",
                    "After washing, dry and shroud the body in a way that preserves dignity.",
                ),
                LocalizedFuneralText(
                    "تختلف بعض تفاصيل عدد قطع الكفن وهيئتها بين المذاهب والحالات، لذلك يتبع فريق تجهيز موثوق.",
                    "Some details about the number and arrangement of shroud pieces differ between schools and circumstances, so follow a qualified preparation team.",
                ),
                LocalizedFuneralText(
                    "تراعى احتياطات الصحة العامة وتعليمات المستشفى أو المقبرة، ولا يستخدم ما يمنع نظامًا أو يضر بالعاملين.",
                    "Observe public-health precautions and hospital or cemetery rules, and avoid materials that are prohibited or unsafe.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح البخاري وصحيح مسلم، كتاب الجنائز؛ أبواب التكفين في كتب الفقه.",
                "General references: Sahih al-Bukhari and Sahih Muslim, Books of Funerals; fiqh chapters on shrouding.",
            ),
        ),
        FuneralGuideSection(
            id = "procession",
            iconKey = "care",
            title = LocalizedFuneralText("حمل الجنازة والتشييع", "Funeral procession"),
            intro = LocalizedFuneralText(
                "تشييع الجنازة عبادة ومواساة، ويُراعى فيه الوقار والسلامة وتنظيم الطريق وتعليمات المسجد والمقبرة.",
                "Accompanying the funeral is an act of worship and support. Observe dignity, safety, route organisation, and mosque or cemetery instructions.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "تُنقل الجنازة بوقار من غير ممارسات تعرض الناس أو الجثمان للخطر.",
                    "Transport the deceased with dignity and without practices that put people or the body at risk.",
                ),
                LocalizedFuneralText(
                    "اتبع تعليمات المرور والمقبرة ومواعيد الدخول ولا تعطل المرافق العامة بحجة التشييع.",
                    "Follow traffic, cemetery, and access instructions and do not obstruct public facilities.",
                ),
                LocalizedFuneralText(
                    "حافظ على جو الدعاء والسكينة، وتجنب التصوير المزعج أو تحويل التشييع إلى مناسبة للاستعراض.",
                    "Maintain an atmosphere of supplication and calm, avoiding intrusive photography or turning the procession into a spectacle.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح البخاري وصحيح مسلم، كتاب الجنائز؛ أبواب اتباع الجنائز.",
                "General references: Sahih al-Bukhari and Sahih Muslim, Books of Funerals; chapters on accompanying funerals.",
            ),
        ),
        FuneralGuideSection(
            id = "prayer",
            iconKey = "prayer",
            title = LocalizedFuneralText("صلاة الجنازة", "Funeral prayer"),
            intro = LocalizedFuneralText(
                "صلاة الجنازة دعاء للميت وهي فرض كفاية عند جمهور الفقهاء، وتقام عادة جماعة بإشراف الإمام أو الجهة المنظمة.",
                "The funeral prayer is a supplication for the deceased and is generally a communal obligation. It is normally led in congregation by an imam or organiser.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "يقف المصلون بخشوع واستقبال القبلة، وهي صلاة بلا ركوع ولا سجود.",
                    "Worshippers stand reverently facing the qiblah; it is a prayer without bowing or prostration.",
                ),
                LocalizedFuneralText(
                    "الصورة المشهورة أربع تكبيرات تتخللها القراءة والصلاة على النبي ﷺ والدعاء للميت، ثم التسليم، مع تفاصيل معتبرة بين المذاهب.",
                    "The well-known form uses four takbirs with recitation, blessings on the Prophet, supplication for the deceased, and salam, with recognised school-specific details.",
                ),
                LocalizedFuneralText(
                    "إن لم تعرف الترتيب أو الأدعية فاتبع الإمام بهدوء، والمقصود حضور القلب والدعاء.",
                    "If you do not know the order or supplications, calmly follow the imam; the central purpose is sincere prayer for the deceased.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح مسلم، كتاب الجنائز؛ أبواب الصلاة على الميت في كتب الفقه.",
                "General references: Sahih Muslim, Book of Funerals; fiqh chapters on the funeral prayer.",
            ),
        ),
        FuneralGuideSection(
            id = "burial",
            iconKey = "burial",
            title = LocalizedFuneralText("الدفن", "Burial"),
            intro = LocalizedFuneralText(
                "الدفن يُنفذ بما يحفظ حرمة الميت ويوافق أحكام المقبرة والقانون المحلي، ويقوده عادة العاملون المختصون أو أهل الخبرة.",
                "Burial should preserve the deceased person's dignity and comply with cemetery rules and local law, normally under the direction of experienced staff.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "تأكد من موضع القبر وتصريح الدفن وترتيبات المقبرة قبل وصول الجنازة.",
                    "Confirm the grave location, burial authorisation, and cemetery arrangements before the funeral arrives.",
                ),
                LocalizedFuneralText(
                    "إن كانت المقبرة تسمح بمشاركة الأسرة في إنزال الجثمان أو ردم القبر، فاتبع تعليمات السلامة والعاملين.",
                    "If the cemetery allows family participation in lowering the body or filling the grave, follow staff and safety instructions.",
                ),
                LocalizedFuneralText(
                    "تفاصيل وضع الجثمان واللحد والشق والتلقين بعد الدفن من المسائل التي قد تختلف فيها المذاهب والأعراف؛ اتبع الإمام الموثوق.",
                    "Details such as grave construction, positioning, and post-burial practices can differ by school and custom; follow qualified local guidance.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح البخاري وصحيح مسلم، كتاب الجنائز؛ أبواب الدفن في كتب الفقه.",
                "General references: Sahih al-Bukhari and Sahih Muslim, Books of Funerals; fiqh chapters on burial.",
            ),
        ),
        FuneralGuideSection(
            id = "after_burial",
            iconKey = "prayer",
            title = LocalizedFuneralText("ما بعد الدفن والدعاء", "After burial and supplication"),
            intro = LocalizedFuneralText(
                "بعد الدفن يُدعى للميت ويُراعى أهلُه، ثم تبدأ مسؤوليات عملية مثل حفظ الوثائق وتسوية الديون والأمانات.",
                "After burial, pray for the deceased and support the family, then address practical responsibilities such as documents, debts, and entrusted property.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "يُدعى للميت بالمغفرة والثبات، مع تجنب إلزام الناس بصيغ أو ممارسات محل خلاف وكأنها شرط لازم.",
                    "Pray for forgiveness and steadfastness for the deceased, without treating disputed formulas or customs as mandatory.",
                ),
                LocalizedFuneralText(
                    "يُمنح أهل الميت وقتًا للحزن والراحة، وتُعرض المساعدة العملية بدل تحميلهم ضيافة أو ترتيبات مرهقة.",
                    "Give the bereaved time to grieve and rest, offering practical help rather than burdening them with hospitality or exhausting arrangements.",
                ),
                LocalizedFuneralText(
                    "ابدأ بجمع الديون والأمانات والوثائق ومعلومات الوصية لتسليمها للمنفذ أو أهل الاختصاص.",
                    "Begin gathering debts, entrusted items, documents, and will information for the executor or qualified advisers.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: سنن أبي داود، كتاب الجنائز؛ وأبواب الدعاء للميت وقضاء الحقوق في كتب الفقه.",
                "General references: Sunan Abi Dawud, Book of Funerals; fiqh discussions on supplication for the deceased and settling rights.",
            ),
        ),
        FuneralGuideSection(
            id = "condolences",
            iconKey = "support",
            title = LocalizedFuneralText("التعزية ومساندة الأسرة", "Condolences and supporting the family"),
            intro = LocalizedFuneralText(
                "المقصود من التعزية المواساة والدعاء وتخفيف العبء، لا زيادة التكاليف الاجتماعية على أهل الميت.",
                "Condolences are meant to comfort, pray, and reduce burdens rather than create extra social obligations for the bereaved.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "يُعزى أهل الميت بكلام طيب ويُدعى للميت بالمغفرة ولأهله بالصبر.",
                    "Offer kind words of condolence and pray for forgiveness for the deceased and patience for the family.",
                ),
                LocalizedFuneralText(
                    "يمكن مساعدة الأسرة بالطعام أو المواصلات أو متابعة بعض الإجراءات بدل مطالبتهم بخدمة الزوار.",
                    "Help with meals, transport, or administrative tasks rather than expecting the bereaved family to serve visitors.",
                ),
                LocalizedFuneralText(
                    "راع الخصوصية ولا تنشر سبب الوفاة أو تفاصيل الأسرة أو صور الجنازة دون إذن واضح.",
                    "Respect privacy and do not publish the cause of death, family details, or funeral images without clear permission.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح البخاري وصحيح مسلم، كتاب الجنائز؛ أبواب التعزية وآدابها في كتب الفقه.",
                "General references: Sahih al-Bukhari and Sahih Muslim, Books of Funerals; fiqh chapters on condolence etiquette.",
            ),
        ),
        FuneralGuideSection(
            id = "cemetery_visits",
            iconKey = "care",
            title = LocalizedFuneralText("زيارة المقابر وآدابها", "Visiting cemeteries and etiquette"),
            intro = LocalizedFuneralText(
                "زيارة المقابر للتذكر والدعاء مشروعة في الجملة، مع حفظ حرمة المكان واتباع أنظمة المقبرة.",
                "Cemetery visits are generally for reflection and supplication while respecting the sanctity of the place and cemetery rules.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "اتبع مواعيد الزيارة وتعليمات المقبرة، واحترم القبور والزوار ولا تمش فوق القبور أو تتسبب في إتلافها.",
                    "Follow visiting hours and cemetery rules, respect graves and visitors, and avoid damaging or walking over graves.",
                ),
                LocalizedFuneralText(
                    "يُسلَّم على أهل القبور ويُدعى لهم، وتُترك الممارسات التي تخالف التوحيد أو تنتهك حرمة المكان.",
                    "Offer greetings and supplicate for those buried, avoiding practices that conflict with monotheism or violate the sanctity of the cemetery.",
                ),
                LocalizedFuneralText(
                    "المسائل المختلف فيها، مثل بعض صور الزيارة أو تخصيص أعمال معينة، تُراجع مع عالم موثوق بدل تحويل الدليل العام إلى فتوى شخصية.",
                    "Disputed matters, including some forms of visitation or specially assigned acts, should be reviewed with a qualified scholar rather than turning general guidance into a personal ruling.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجع العامة: صحيح مسلم، كتاب الجنائز؛ أبواب زيارة القبور في كتب الفقه.",
                "General references: Sahih Muslim, Book of Funerals; fiqh chapters on cemetery visitation.",
            ),
        ),
    )

    val willChecklist: List<LocalizedFuneralText>
        get() = WillEducationContent.willChecklist

    val willEducationSections: List<WillEducationSection>
        get() = WillEducationContent.willEducationSections

    val willReferences: LocalizedFuneralText
        get() = WillEducationContent.willReferences

    fun searchGuideSections(
        query: String,
        isArabic: Boolean,
    ): List<FuneralGuideSection> = guideSections.filter { section ->
        section.searchableText(isArabic).containsQuery(query)
    }

    fun searchWillEducationSections(
        query: String,
        isArabic: Boolean,
    ): List<WillEducationSection> = willEducationSections.filter { section ->
        section.searchableText(isArabic).containsQuery(query)
    }
}

private fun FuneralGuideSection.searchableText(isArabic: Boolean): String = buildString {
    append(title.pick(isArabic))
    append(' ')
    append(intro.pick(isArabic))
    append(' ')
    steps.forEach { step ->
        append(step.pick(isArabic))
        append(' ')
    }
    append(reference.pick(isArabic))
}

private fun WillEducationSection.searchableText(isArabic: Boolean): String = buildString {
    append(title.pick(isArabic))
    append(' ')
    append(intro.pick(isArabic))
    append(' ')
    steps.forEach { step ->
        append(step.pick(isArabic))
        append(' ')
    }
    append(reference.pick(isArabic))
}

private fun LocalizedFuneralText.pick(isArabic: Boolean): String =
    if (isArabic) arabic else english

private val ArabicDiacritics = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")

private fun String.containsQuery(query: String): Boolean {
    val normalizedQuery = query.normalizeForSearch()
    return normalizedQuery.isEmpty() || normalizeForSearch().contains(normalizedQuery)
}

private fun String.normalizeForSearch(): String = trim()
    .lowercase()
    .replace(ArabicDiacritics, "")
    .replace("ـ", "")
    .replace("أ", "ا")
    .replace("إ", "ا")
    .replace("آ", "ا")
    .replace("ى", "ي")
    .replace("ة", "ه")
    .replace("ؤ", "و")
    .replace("ئ", "ي")
