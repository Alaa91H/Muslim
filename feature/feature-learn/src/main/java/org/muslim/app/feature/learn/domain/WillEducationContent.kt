package org.muslim.app.feature.learn.domain

/**
 * Educational and planning content for the Islamic-will side of the feature.
 *
 * Kept separate from the funeral journey so each content object stays focused
 * and easier to maintain as the reference library grows.
 */
object WillEducationContent {
    val willChecklist = listOf(
        LocalizedFuneralText(
            "دوّن بياناتك الأساسية ومكان الوثائق المهمة بحيث يستطيع الشخص الموثوق الوصول إليها عند الحاجة.",
            "Record your basic information and where important documents are kept so a trusted person can find them when needed.",
        ),
        LocalizedFuneralText(
            "دوّن الديون والحقوق والالتزامات المالية بوضوح، مع أسماء الجهات أو الأشخاص والمستندات المؤيدة.",
            "Record debts, rights, and financial obligations clearly, including relevant people, organisations, and supporting documents.",
        ),
        LocalizedFuneralText(
            "دوّن الأمانات والممتلكات التي ليست لك وما يجب رده إلى أصحابه.",
            "Record entrusted items and property that does not belong to you and must be returned to its owners.",
        ),
        LocalizedFuneralText(
            "سمِّ منفذًا أو شخصًا موثوقًا ووسيلة تواصل واضحة بعد أخذ موافقته.",
            "Name an executor or trusted person and provide clear contact information after obtaining their agreement.",
        ),
        LocalizedFuneralText(
            "دوّن ملاحظات الوصاية على القُصَّر إن وجدت، وراجع المتطلبات القانونية في بلدك.",
            "Record guardianship notes for minors where applicable and review the legal requirements in your country.",
        ),
        LocalizedFuneralText(
            "اكتب رغبات التجهيز والجنازة بما لا يخالف الشرع أو اللوائح المحلية واترك التنفيذ للجهة المؤهلة.",
            "Record funeral-preparation wishes that do not conflict with Islamic guidance or local rules and leave execution to qualified people.",
        ),
        LocalizedFuneralText(
            "اكتب الوصايا أو التبرعات الخيرية منفصلة عن أنصبة الميراث، ولا تعتمدها ماليًا قبل مراجعة أهل العلم والاختصاص.",
            "Keep charitable bequests or donations separate from inheritance shares and do not rely on them financially before qualified review.",
        ),
        LocalizedFuneralText(
            "لا تضع كلمات المرور أو مفاتيح المحافظ أو الأسرار الحساسة مباشرة في نص قابل للمشاركة؛ دوّن بدلًا منها مكان الوصول الآمن.",
            "Do not place passwords, wallet keys, or highly sensitive secrets directly in shareable text; record a secure access location instead.",
        ),
        LocalizedFuneralText(
            "راجع الوصية عند تغيّر الأسرة أو الديون أو الأصول أو القانون، وحدد تاريخ آخر مراجعة.",
            "Review the will when family circumstances, debts, assets, or law change, and note the date of the latest review.",
        ),
    )

    val willEducationSections = listOf(
        WillEducationSection(
            id = "purpose_and_scope",
            title = LocalizedFuneralText("ما الذي تنظمه الوصية؟", "What does a will organise?"),
            intro = LocalizedFuneralText(
                "المسودة تساعد على جمع المعلومات والحقوق والرغبات، لكنها لا تتحول وحدها إلى وثيقة شرعية أو قانونية نافذة.",
                "The draft helps collect information, rights, and wishes, but it does not by itself become an enforceable Islamic or legal instrument.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "اجعل الهدف الأول حفظ الحقوق وتقليل الالتباس على الأسرة بعد الوفاة.",
                    "Make preservation of rights and reduction of uncertainty for the family the first goal.",
                ),
                LocalizedFuneralText(
                    "افصل بين المعلومات التنظيمية وبين الأحكام الشرعية والقانونية التي تحتاج مراجعة مختص.",
                    "Separate organisational information from Islamic and legal rulings that require qualified review.",
                ),
                LocalizedFuneralText(
                    "احتفظ بنسخة يمكن الوصول إليها فعلًا ولا تجعلها مخفية لدرجة لا يستطيع المنفذ العثور عليها.",
                    "Keep an accessible copy rather than hiding it so completely that the executor cannot find it.",
                ),
            ),
            reference = LocalizedFuneralText(
                "التأصيل العام: البقرة 2:180؛ صحيح البخاري، كتاب الوصايا.",
                "General basis: Qur'an 2:180; Sahih al-Bukhari, Book of Wills.",
            ),
        ),
        WillEducationSection(
            id = "debts_and_trusts",
            title = LocalizedFuneralText("الديون والحقوق والأمانات", "Debts, rights, and entrusted property"),
            intro = LocalizedFuneralText(
                "وضوح الديون والأمانات من أهم ما يخفف النزاع ويحفظ حقوق الناس.",
                "Clear records of debts and entrusted property are among the most important ways to preserve other people's rights and reduce disputes.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "اكتب ما عليك وما لك، والجهة المقابلة، والمبلغ أو الوصف، ومكان المستند.",
                    "Record what you owe and what is owed to you, the counterparty, amount or description, and where the evidence is stored.",
                ),
                LocalizedFuneralText(
                    "ميّز بين الدين والأمانة والاشتراك المالي والملكية المشتركة حتى لا تختلط على الورثة.",
                    "Distinguish debts, entrusted property, shared finances, and co-owned property so they are not confused by heirs.",
                ),
                LocalizedFuneralText(
                    "حدّث القائمة بعد السداد أو التغيير ولا تترك معلومات قديمة تبدو كأنها التزام قائم.",
                    "Update the list after repayment or change so outdated information is not mistaken for an active obligation.",
                ),
            ),
            reference = LocalizedFuneralText(
                "راجع أحكام قضاء الديون والحقوق في كتب الفقه، والقوانين المحلية المتعلقة بالتركة والإثبات.",
                "Review fiqh guidance on debts and rights together with local estate and evidence law.",
            ),
        ),
        WillEducationSection(
            id = "bequests_and_inheritance",
            title = LocalizedFuneralText("الوصايا المالية والميراث", "Financial bequests and inheritance"),
            intro = LocalizedFuneralText(
                "الوصية المالية ليست وسيلة حرة لإعادة توزيع أنصبة الميراث؛ حدود الوصية ومن يستحقها تحتاج تطبيقًا فقهيًا وقانونيًا صحيحًا.",
                "A financial bequest is not a free mechanism for rewriting inheritance shares. Its limits and eligible recipients require correct Islamic and legal application.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "لا تستخدم خانة الوصايا لتغيير أنصبة الورثة أو حرمان وارث دون مراجعة علمية وقانونية.",
                    "Do not use the bequest field to rewrite heirs' shares or disinherit someone without qualified Islamic and legal review.",
                ),
                LocalizedFuneralText(
                    "إذا كانت هناك وصية خيرية أو لغير وارث، دوّن مقصدها والجهة المقترحة بوضوح واترك التحقق من الحد والتنفيذ للمختصين.",
                    "For a charitable or non-heir bequest, clearly record the intended purpose and recipient, leaving validation of limits and execution to qualified advisers.",
                ),
                LocalizedFuneralText(
                    "الأملاك في أكثر من بلد أو العقود المعقدة تحتاج غالبًا إلى استشارة قانونية متخصصة.",
                    "Property in multiple countries or complex contracts will often require specialised legal advice.",
                ),
            ),
            reference = LocalizedFuneralText(
                "للتأصيل: النساء 4:11–12 و4:176؛ وحديث الثلث في الصحيحين. التطبيق التفصيلي يحتاج عالمًا ومحاميًا أو كاتب عدل.",
                "For the general basis: Qur'an 4:11-12 and 4:176, and the hadith concerning one third in the two Sahih collections. Detailed application needs a qualified scholar and lawyer or notary.",
            ),
        ),
        WillEducationSection(
            id = "executor_and_documents",
            title = LocalizedFuneralText("المنفذ والوصول إلى الوثائق", "Executor and document access"),
            intro = LocalizedFuneralText(
                "اختيار شخص موثوق لا يكفي وحده؛ يحتاج إلى معرفة مسؤوليته وكيف يصل إلى الوثائق عند الحاجة.",
                "Choosing a trusted person is not enough by itself. They need to understand the role and how to access necessary documents.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "اسأل الشخص قبل تسجيله كمنفذ أو جهة اتصال وتأكد من موافقته.",
                    "Ask the person before naming them as executor or contact and confirm their agreement.",
                ),
                LocalizedFuneralText(
                    "دوّن وسيلة اتصال بديلة ومكان حفظ الوثائق المهمة.",
                    "Record an alternative contact method and the location of important documents.",
                ),
                LocalizedFuneralText(
                    "لا تضع الأسرار الرقمية الحساسة داخل نص المشاركة؛ استخدم وسيلة آمنة منفصلة للوصول إليها.",
                    "Do not place sensitive digital secrets in the shareable draft; use a separate secure access method.",
                ),
            ),
            reference = LocalizedFuneralText(
                "راجع قانون بلدك في صفة المنفذ وصلاحياته، خصوصًا إذا كانت التركة أو الأسرة موزعة بين دول.",
                "Review local law on the executor's legal status and powers, especially when the estate or family spans multiple countries.",
            ),
        ),
        WillEducationSection(
            id = "guardianship",
            title = LocalizedFuneralText("القُصَّر والوصاية", "Minors and guardianship"),
            intro = LocalizedFuneralText(
                "يمكن للمسودة أن تسجل رغبتك ومعلومات تساعد الأسرة، لكن تعيين الوصي قانونيًا يختلف من بلد لآخر.",
                "The draft can record your wishes and useful family information, but legal appointment of a guardian differs by jurisdiction.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "اكتب أسماء القُصَّر والمعلومات المهمة لرعايتهم دون جمع بيانات حساسة لا حاجة لها.",
                    "Record minors' names and necessary care information without collecting unnecessary sensitive data.",
                ),
                LocalizedFuneralText(
                    "ناقش الرغبة في الوصاية مسبقًا مع الشخص المقترح ولا تفترض موافقته.",
                    "Discuss guardianship wishes with the proposed person in advance rather than assuming consent.",
                ),
                LocalizedFuneralText(
                    "وثّق الرغبة بالصورة القانونية الصحيحة وفق بلدك بعد الاستشارة.",
                    "Document the preference in the legally appropriate form for your jurisdiction after advice.",
                ),
            ),
            reference = LocalizedFuneralText(
                "هذه مسألة تجمع بين المسؤولية الشرعية وقانون الأسرة المحلي، لذلك تحتاج مراجعة مختصين.",
                "This area combines Islamic responsibility with local family law and therefore needs qualified review.",
            ),
        ),
        WillEducationSection(
            id = "funeral_wishes",
            title = LocalizedFuneralText("رغبات التجهيز والجنازة", "Funeral preparation wishes"),
            intro = LocalizedFuneralText(
                "تسجيل الرغبات يساعد الأسرة، لكن التنفيذ الفعلي يتبع الحكم الشرعي والإمكان العملي والأنظمة المحلية.",
                "Recording wishes can help the family, but actual implementation remains subject to Islamic guidance, practical feasibility, and local rules.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "يمكن تسجيل المسجد أو جهة الجنائز المفضلة وأسماء الأشخاص الذين ينبغي التواصل معهم.",
                    "You can record a preferred mosque or funeral service and the people who should be contacted.",
                ),
                LocalizedFuneralText(
                    "اكتب ما يفيد الأسرة عمليًا ولا تحول المسودة إلى تعليمات تلزمهم بما لا يستطيعون تنفيذه.",
                    "Record practical information without turning the draft into instructions that the family cannot realistically follow.",
                ),
                LocalizedFuneralText(
                    "إذا كانت لديك رغبة قد تتعارض مع قانون البلد أو سياسة المقبرة فتحقق منها مسبقًا.",
                    "If a wish may conflict with local law or cemetery policy, check its feasibility in advance.",
                ),
            ),
            reference = LocalizedFuneralText(
                "يُرجع في تفاصيل أحكام الجنائز إلى عالم موثوق، وفي الإجراءات إلى الجهات المحلية المختصة.",
                "Refer detailed funeral rulings to a qualified scholar and procedures to the relevant local authorities.",
            ),
        ),
        WillEducationSection(
            id = "witnesses_and_legal_form",
            title = LocalizedFuneralText("الشهود والتوثيق والصيغة القانونية", "Witnesses, documentation, and legal form"),
            intro = LocalizedFuneralText(
                "متطلبات التوقيع والشهود والتوثيق تختلف كثيرًا بين الدول؛ المسودة داخل التطبيق لا تستبدل هذه المتطلبات.",
                "Signature, witness, and notarisation requirements vary widely between jurisdictions; the in-app draft does not replace them.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "اسأل محاميًا أو كاتب عدل عن الصيغة المقبولة في بلدك قبل الاعتماد على النسخة النهائية.",
                    "Ask a lawyer or notary about the accepted form in your jurisdiction before relying on a final version.",
                ),
                LocalizedFuneralText(
                    "تأكد من أن النسخة المعتمدة مؤرخة وموقعة ومشهودة بالطريقة المطلوبة إن كان القانون يشترط ذلك.",
                    "Ensure the relied-upon version is dated, signed, and witnessed in the required manner when local law requires it.",
                ),
                LocalizedFuneralText(
                    "ألغ أو استبدل النسخ القديمة بوضوح حتى لا تبقى عدة نسخ متعارضة.",
                    "Clearly revoke or replace outdated copies so conflicting versions do not remain in circulation.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المتطلبات القانونية تختلف حسب الدولة والولاية؛ استعن بجهة قانونية مؤهلة.",
                "Legal requirements vary by country and region; use qualified local legal advice.",
            ),
        ),
        WillEducationSection(
            id = "review_and_updates",
            title = LocalizedFuneralText("المراجعة الدورية", "Periodic review"),
            intro = LocalizedFuneralText(
                "الوصية والمعلومات المرافقة لها تتقادم؛ المراجعة المنتظمة تمنع الاعتماد على بيانات قديمة.",
                "A will and its supporting information become outdated. Periodic review helps prevent reliance on stale information.",
            ),
            steps = listOf(
                LocalizedFuneralText(
                    "راجعها بعد الزواج أو الطلاق أو الولادة أو الوفاة أو تغير كبير في المال أو السكن.",
                    "Review it after marriage, divorce, births, deaths, or major changes in finances or residence.",
                ),
                LocalizedFuneralText(
                    "حدّث أرقام التواصل والديون ومواقع الوثائق عند تغيرها.",
                    "Update contact details, debts, and document locations when they change.",
                ),
                LocalizedFuneralText(
                    "دوّن تاريخ آخر مراجعة وموعد مراجعة مقترح بدل ترك الوثيقة سنوات بلا تحديث.",
                    "Record the date of the last review and a suggested next review rather than leaving the document unchanged for years.",
                ),
            ),
            reference = LocalizedFuneralText(
                "المراجعة التنظيمية لا تغني عن إعادة التحقق الشرعي والقانوني عند تغير الظروف.",
                "Administrative review does not replace renewed Islamic and legal review when circumstances change.",
            ),
        ),
    )

    val willReferences = LocalizedFuneralText(
        "التأصيل العام: البقرة 2:180؛ النساء 4:11–12 و4:176؛ صحيح البخاري، كتاب الوصايا. الوصية المالية والميراث يحتاجان مراجعة عالم موثوق ومحامٍ أو كاتب عدل وفق بلدك.",
        "General basis: Qur'an 2:180; 4:11-12 and 4:176; Sahih al-Bukhari, Book of Wills. Financial bequests and inheritance need review by a qualified scholar and a lawyer or notary in your jurisdiction.",
    )
}
