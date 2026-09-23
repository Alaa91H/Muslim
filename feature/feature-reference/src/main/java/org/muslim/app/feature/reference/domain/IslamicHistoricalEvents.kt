package org.muslim.app.feature.reference.domain

/**
 * Structured event catalogue for the history destination.
 *
 * Events are chronological anchors rather than a claim that history is a single political
 * sequence. Each event carries context, significance, links to existing states/places/people,
 * and explicit sources.
 */
object IslamicHistoricalEvents {
    val events = listOf(
        HistoricalEvent(
            id = "revelation_begins",
            date = HistoricalDate(610, precision = HistoryDatePrecision.Approximate),
            category = HistoricalEventCategory.ReligiousAndCommunity,
            title = HistoryText("بداية الوحي", "Beginning of revelation"),
            summary = HistoryText(
                "يرتبط عام 610م تقريباً في الموروث الإسلامي ببداية نزول الوحي على النبي محمد ﷺ في مكة.",
                "Around 610 CE is associated in Islamic tradition with the beginning of revelation to Prophet Muhammad in Mecca.",
            ),
            context = HistoryText(
                "كانت مكة مركزاً دينياً وتجارياً في غرب الجزيرة العربية، وبدأت الدعوة الإسلامية في بيئة قبلية متعددة الروابط والمصالح.",
                "Mecca was a religious and commercial center in western Arabia, and the Islamic mission began within a tribal society shaped by multiple networks and interests.",
            ),
            significance = HistoryText(
                "يمثل الحدث بداية الإطار الزمني للسيرة النبوية في التطبيق، بينما تبقى الروايات التفصيلية في قسم السيرة المتخصص.",
                "This marks the beginning of the app’s Sira chronology, while detailed narrative material remains in the dedicated Sira section.",
            ),
            eraIds = listOf("prophetic_era"),
            placeIds = listOf("mecca"),
            relatedTopicIds = listOf("translation_books"),
            sourceIds = listOf("internal_sira", "met_chronology"),
        ),
        HistoricalEvent(
            id = "hijra_medina",
            date = HistoricalDate(622),
            category = HistoricalEventCategory.ReligiousAndCommunity,
            title = HistoryText("الهجرة إلى المدينة", "Hijra to Medina"),
            summary = HistoryText(
                "هاجر النبي ﷺ والمسلمون من مكة إلى المدينة سنة 622م، وأصبحت الهجرة لاحقاً نقطة ابتداء التقويم الهجري.",
                "Prophet Muhammad and the Muslim community migrated from Mecca to Medina in 622 CE; the Hijra later became the starting point of the Hijri calendar.",
            ),
            context = HistoryText(
                "جاءت الهجرة بعد سنوات من الدعوة في مكة واتصالات مع أهل يثرب، وأعادت تشكيل المجال الاجتماعي والسياسي للجماعة المسلمة.",
                "The migration followed years of preaching in Mecca and contacts with people in Yathrib, reshaping the social and political setting of the Muslim community.",
            ),
            significance = HistoryText(
                "يمثل الحدث انتقالاً من المرحلة المكية إلى بناء مجتمع منظم في المدينة، ويشكل أحد أهم المراسي الزمنية في التاريخ الإسلامي.",
                "The event marks the transition from the Meccan phase to an organized community in Medina and is one of the central chronological anchors of Islamic history.",
            ),
            eraIds = listOf("prophetic_era"),
            placeIds = listOf("mecca", "medina"),
            sourceIds = listOf("internal_sira", "met_chronology"),
        ),
        HistoricalEvent(
            id = "battle_badr",
            date = HistoricalDate(624),
            category = HistoricalEventCategory.Conflict,
            title = HistoryText("غزوة بدر", "Battle of Badr"),
            summary = HistoryText(
                "وقعت بدر سنة 624م بين المسلمين من المدينة وقوة مكية، وتعد من أبرز أحداث السيرة المبكرة.",
                "Badr took place in 624 CE between the Medinan Muslim community and a Meccan force and is a major event in the early Sira.",
            ),
            context = HistoryText(
                "وقعت المواجهة ضمن توتر متصاعد بين المدينة ومكة بعد الهجرة، وتعرضها مصادر السيرة بتفاصيل متفاوتة.",
                "The confrontation occurred amid escalating tension between Medina and Mecca after the Hijra, and Sira sources preserve differing levels of detail.",
            ),
            significance = HistoryText(
                "أثرت بدر في توازنات المرحلة المبكرة وفي الذاكرة الدينية والتاريخية الإسلامية، من دون اختزال التطورات اللاحقة في نتيجة معركة واحدة.",
                "Badr affected early political balances and later Islamic religious and historical memory, without reducing subsequent developments to the outcome of a single battle.",
            ),
            eraIds = listOf("prophetic_era"),
            placeIds = listOf("badr", "medina"),
            sourceIds = listOf("internal_sira"),
        ),
        HistoricalEvent(
            id = "prophet_death_632",
            date = HistoricalDate(632),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("وفاة النبي محمد ﷺ", "Death of Prophet Muhammad"),
            summary = HistoryText(
                "توفي النبي محمد ﷺ في المدينة سنة 632م، وبدأت بعد ذلك مرحلة الخلافة الراشدة.",
                "Prophet Muhammad died in Medina in 632 CE, after which the Rashidun period began.",
            ),
            context = HistoryText(
                "أوجدت الوفاة أسئلة عملية وسياسية حول القيادة وإدارة المجتمع الذي توسعت علاقاته في أواخر العهد المدني.",
                "The death created immediate practical and political questions about leadership and governance in a community whose relationships had expanded during the Medinan years.",
            ),
            significance = HistoryText(
                "يفصل التطبيق هنا بين السيرة النبوية من جهة والتاريخ السياسي والإداري اللاحق من جهة أخرى، مع بقاء التداخل بينهما مفهوماً تاريخياً.",
                "The app uses this point to distinguish the Prophet’s biography from later political and administrative history while recognizing their historical continuity.",
            ),
            eraIds = listOf("prophetic_era", "rashidun"),
            placeIds = listOf("medina"),
            sourceIds = listOf("internal_sira", "met_chronology"),
        ),
        HistoricalEvent(
            id = "rashidun_expansion_630s",
            date = HistoricalDate(636, precision = HistoryDatePrecision.Approximate),
            category = HistoricalEventCategory.Conflict,
            title = HistoryText("تحولات الشام والعراق في ثلاثينيات القرن السابع", "Levant and Iraq transformations in the 630s"),
            summary = HistoryText(
                "شهدت ثلاثينيات القرن السابع معارك وتحولات سياسية كبرى في الشام والعراق ضمن توسع الدولة في عهد الخلفاء الراشدين.",
                "The 630s saw major battles and political transformations in the Levant and Iraq during the expansion of the Rashidun state.",
            ),
            context = HistoryText(
                "تداخلت الحرب مع انهيار أو تراجع هياكل إمبراطورية سابقة وتغير الإدارة والضرائب والحاميات والمدن.",
                "Warfare intersected with the weakening or collapse of earlier imperial structures and with changes in administration, taxation, garrisons, and cities.",
            ),
            significance = HistoryText(
                "فتحت هذه التحولات مرحلة جديدة في تاريخ المشرق، لكن نتائجها المحلية اختلفت من إقليم إلى آخر ولم تقع بصورة متطابقة.",
                "These changes opened a new phase in Middle Eastern history, but local outcomes differed by region and did not unfold identically everywhere.",
            ),
            eraIds = listOf("rashidun"),
            stateIds = listOf("rashidun"),
            personIds = listOf("khalid_ibn_al_walid"),
            sourceIds = listOf("met_chronology", "world_history_caliphates"),
        ),
        HistoricalEvent(
            id = "umayyad_rule_661",
            date = HistoricalDate(661),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("بداية الحكم الأموي", "Beginning of Umayyad rule"),
            summary = HistoryText(
                "يؤرخ عام 661م عادة لبداية الدولة الأموية واتخاذ دمشق مركزاً للحكم.",
                "The year 661 CE is conventionally used to mark the beginning of Umayyad rule, centered on Damascus.",
            ),
            context = HistoryText(
                "جاء الانتقال بعد سنوات من النزاع الداخلي في الفتنة الأولى، وتختلف المصادر في بعض تفاصيلها وتقييماتها.",
                "The transition followed years of internal conflict during the First Fitna, whose details and interpretations vary across sources.",
            ),
            significance = HistoryText(
                "أصبح مركز القرار السياسي في دمشق، وبدأت مرحلة جديدة من التنظيم الإداري والاتساع الإقليمي.",
                "Political decision-making became centered in Damascus, opening a new phase of administrative development and territorial expansion.",
            ),
            eraIds = listOf("rashidun", "umayyad"),
            stateIds = listOf("umayyad_caliphate"),
            placeIds = listOf("damascus"),
            sourceIds = listOf("met_major_dynasties", "world_history_caliphates"),
        ),
        HistoricalEvent(
            id = "al_andalus_711",
            date = HistoricalDate(711),
            category = HistoricalEventCategory.Conflict,
            title = HistoryText("بداية الحكم الإسلامي في الأندلس", "Beginning of Muslim rule in al-Andalus"),
            summary = HistoryText(
                "شهد عام 711م عبور قوات إلى شبه الجزيرة الإيبيرية وبداية مرحلة جديدة من الحكم الإسلامي في الأندلس.",
                "In 711 CE, forces crossed into the Iberian Peninsula, beginning a new period of Muslim rule in al-Andalus.",
            ),
            context = HistoryText(
                "كانت التحولات جزءاً من تغيرات سياسية أوسع في غرب المتوسط، وتلتها مراحل متعددة من الفتح والاستقرار وإعادة التنظيم.",
                "The developments formed part of wider political changes in the western Mediterranean and were followed by multiple phases of conquest, settlement, and reorganization.",
            ),
            significance = HistoryText(
                "ارتبطت الأندلس لاحقاً بدول ومراكز حضرية وثقافية متعددة، ولا يختزل تاريخها في حدث العبور الأول وحده.",
                "Al-Andalus later developed multiple states and urban and cultural centers, and its history cannot be reduced to the initial crossing alone.",
            ),
            eraIds = listOf("umayyad", "regional_civilizations"),
            stateIds = listOf("umayyad_caliphate"),
            personIds = listOf("tariq_ibn_ziyad"),
            placeIds = listOf("cordoba"),
            sourceIds = listOf("met_chronology", "met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "abbasid_revolution_750",
            date = HistoricalDate(750),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("قيام الدولة العباسية", "Beginning of Abbasid rule"),
            summary = HistoryText(
                "انتهى الحكم الأموي في المشرق سنة 750م وبدأ الحكم العباسي، بينما استمر الوجود الأموي لاحقاً في الأندلس.",
                "Umayyad rule in the eastern caliphate ended in 750 CE and Abbasid rule began, while an Umayyad polity later continued in al-Andalus.",
            ),
            context = HistoryText(
                "جاء التحول بعد حركة سياسية وعسكرية واسعة استندت إلى شبكات في خراسان والعراق ومناطق أخرى.",
                "The transition followed a broad political and military movement supported by networks in Khurasan, Iraq, and elsewhere.",
            ),
            significance = HistoryText(
                "غيرت المرحلة مركز الثقل السياسي وأعادت تشكيل شبكات النخبة والإدارة، لكنها لم تُلغِ تعدد المراكز الإسلامية.",
                "The change shifted political centers of gravity and reshaped elite and administrative networks without eliminating the plurality of Muslim centers.",
            ),
            eraIds = listOf("umayyad", "abbasid"),
            stateIds = listOf("umayyad_caliphate", "abbasid_caliphate"),
            sourceIds = listOf("met_chronology", "met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "baghdad_founded_762",
            date = HistoricalDate(762),
            category = HistoricalEventCategory.FoundationAndUrbanism,
            title = HistoryText("تأسيس بغداد", "Foundation of Baghdad"),
            summary = HistoryText(
                "أسس الخليفة العباسي المنصور بغداد سنة 762م، وأصبحت لاحقاً من أبرز مدن الحكم والتجارة والعلم.",
                "The Abbasid caliph al-Mansur founded Baghdad in 762 CE, and it later became a major center of government, trade, and learning.",
            ),
            context = HistoryText(
                "اختير موقع المدينة في العراق ضمن شبكة نهرية وبرية كثيفة تربط مناطق زراعية وتجارية واسعة.",
                "The city’s Iraqi location sat within dense river and overland networks connecting major agricultural and commercial regions.",
            ),
            significance = HistoryText(
                "غدت بغداد عقدة سياسية واقتصادية ومعرفية أثرت في تاريخ مناطق واسعة، مع استمرار مراكز أخرى بالتوازي معها.",
                "Baghdad became a major political, economic, and intellectual node influencing wide regions while other centers continued in parallel.",
            ),
            eraIds = listOf("abbasid"),
            stateIds = listOf("abbasid_caliphate"),
            placeIds = listOf("baghdad"),
            relatedTopicIds = listOf("translation_books", "cities_urban_life"),
            sourceIds = listOf("met_chronology", "met_science"),
        ),
        HistoricalEvent(
            id = "abbasid_translation_scholarship",
            date = HistoricalDate(800, 900, HistoryDatePrecision.Range),
            category = HistoricalEventCategory.KnowledgeAndCulture,
            title = HistoryText("ازدهار الترجمة والتأليف في بغداد العباسية", "Expansion of translation and scholarship in Abbasid Baghdad"),
            summary = HistoryText(
                "ازدهرت في القرنين الثامن والتاسع أنشطة الترجمة والتأليف والحساب والفلك والطب في بغداد ومراكز أخرى.",
                "During the eighth and ninth centuries, translation, authorship, calculation, astronomy, and medicine expanded in Baghdad and other centers.",
            ),
            context = HistoryText(
                "اعتمدت هذه الأنشطة على رعاية متعددة، وكتّاب ومترجمين وعلماء من خلفيات متنوعة، وعلى شبكات الكتب والمعرفة القادمة من لغات ومناطق مختلفة.",
                "These activities depended on varied patronage, writers, translators, and scholars from diverse backgrounds, as well as book and knowledge networks spanning multiple languages and regions.",
            ),
            significance = HistoryText(
                "أنتجت المرحلة مؤلفات وشروحاً وترجمات أثرت في تقاليد علمية لاحقة داخل العالم الإسلامي وخارجه، من دون اختزالها في مؤسسة واحدة أو لحظة واحدة.",
                "The period produced works, commentaries, and translations that influenced later scholarly traditions within and beyond the Islamic world, without being reducible to one institution or single moment.",
            ),
            eraIds = listOf("abbasid"),
            stateIds = listOf("abbasid_caliphate"),
            placeIds = listOf("baghdad"),
            personIds = listOf("al_khwarizmi"),
            relatedTopicIds = listOf("translation_books", "mathematics", "astronomy"),
            sourceIds = listOf("met_science", "met_chronology"),
        ),
        HistoricalEvent(
            id = "fatimid_909",
            date = HistoricalDate(909),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("قيام الدولة الفاطمية", "Beginning of Fatimid rule"),
            summary = HistoryText(
                "قامت الدولة الفاطمية في شمال إفريقيا سنة 909م قبل انتقال مركزها لاحقاً إلى مصر.",
                "The Fatimid state was established in North Africa in 909 CE before its center later shifted to Egypt.",
            ),
            context = HistoryText(
                "نشأت في فضاء مغاربي متعدد القوى ثم توسعت تدريجياً شرقاً، في وقت استمرت فيه دول أخرى في الأندلس والمشرق.",
                "It emerged in a politically diverse Maghrebi setting and later expanded eastward while other states continued in al-Andalus and the eastern Islamic world.",
            ),
            significance = HistoryText(
                "أضافت الدولة مركزاً سياسياً وثقافياً مهماً إلى خريطة العالم الإسلامي المتعدد السلالات.",
                "The dynasty added an important political and cultural center to an Islamic world already characterized by multiple concurrent dynasties.",
            ),
            eraIds = listOf("abbasid", "regional_civilizations"),
            stateIds = listOf("fatimids"),
            sourceIds = listOf("met_fatimid"),
        ),
        HistoricalEvent(
            id = "cairo_founded_969",
            date = HistoricalDate(969),
            category = HistoricalEventCategory.FoundationAndUrbanism,
            title = HistoryText("تأسيس القاهرة الفاطمية", "Foundation of Fatimid Cairo"),
            summary = HistoryText(
                "بعد دخول الفاطميين مصر سنة 969م أُنشئت القاهرة لتكون مركزاً جديداً للحكم.",
                "After the Fatimid conquest of Egypt in 969 CE, Cairo was founded as a new center of rule.",
            ),
            context = HistoryText(
                "نشأت المدينة قرب الفسطاط ضمن وادي النيل، ثم تطورت علاقتها بالمراكز السكانية والتجارية القائمة حولها.",
                "The city arose near Fustat in the Nile Valley and developed in relation to existing population and commercial centers around it.",
            ),
            significance = HistoryText(
                "أصبحت القاهرة لاحقاً إحدى أهم مدن العالم الإسلامي في السياسة والتعليم والتجارة والعمران عبر دول متعاقبة.",
                "Cairo later became one of the Islamic world’s major cities for politics, learning, trade, and urban culture across successive states.",
            ),
            eraIds = listOf("regional_civilizations"),
            stateIds = listOf("fatimids"),
            placeIds = listOf("cairo"),
            relatedTopicIds = listOf("cities_urban_life", "education_libraries"),
            sourceIds = listOf("met_fatimid", "met_chronology"),
        ),
        HistoricalEvent(
            id = "ayyubid_1171",
            date = HistoricalDate(1171),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("بداية الحكم الأيوبي في مصر", "Beginning of Ayyubid rule in Egypt"),
            summary = HistoryText(
                "انتهى الحكم الفاطمي في مصر سنة 1171م وبدأت المرحلة الأيوبية بقيادة صلاح الدين.",
                "Fatimid rule in Egypt ended in 1171 CE, beginning the Ayyubid period under Salah al-Din.",
            ),
            context = HistoryText(
                "جاء التحول ضمن صراعات إقليمية واسعة شملت مصر والشام والقوى الصليبية ومراكز سياسية متعددة.",
                "The transition occurred amid wider regional struggles involving Egypt, the Levant, Crusader powers, and multiple political centers.",
            ),
            significance = HistoryText(
                "أعاد الأيوبيون تشكيل علاقات مصر والشام وأنماط الرعاية السياسية والعسكرية والعمرانية في المنطقة.",
                "Ayyubid rule reshaped relations between Egypt and the Levant and patterns of political, military, and architectural patronage in the region.",
            ),
            eraIds = listOf("regional_civilizations"),
            stateIds = listOf("ayyubids"),
            placeIds = listOf("cairo"),
            sourceIds = listOf("met_ayyubid"),
        ),
        HistoricalEvent(
            id = "baghdad_1258",
            date = HistoricalDate(1258),
            category = HistoricalEventCategory.Conflict,
            title = HistoryText("سقوط بغداد بيد المغول", "Mongol capture of Baghdad"),
            summary = HistoryText(
                "استولى جيش هولاكو على بغداد سنة 1258م وانتهت بذلك الخلافة العباسية في المدينة.",
                "Hulagu’s army captured Baghdad in 1258 CE, ending the Abbasid caliphate in the city.",
            ),
            context = HistoryText(
                "جاء الحدث ضمن التوسع المغولي في غرب آسيا، وأصاب العراق بأضرار بشرية وعمرانية وسياسية كبيرة.",
                "The event formed part of Mongol expansion into western Asia and caused major human, urban, and political damage in Iraq.",
            ),
            significance = HistoryText(
                "كان تحولاً كبيراً في تاريخ العراق والخلافة، لكنه لم يمثل نهاية المجتمعات أو الدول أو النشاط العلمي الإسلامي في مناطق أخرى.",
                "It was a major turning point for Iraq and the caliphate, but it did not mark the end of Muslim societies, states, or scholarship elsewhere.",
            ),
            eraIds = listOf("abbasid", "regional_civilizations"),
            stateIds = listOf("abbasid_caliphate"),
            placeIds = listOf("baghdad"),
            sourceIds = listOf("met_chronology", "met_ilkhanid"),
        ),
        HistoricalEvent(
            id = "mamluk_1250",
            date = HistoricalDate(1250),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("قيام سلطنة المماليك", "Beginning of the Mamluk sultanate"),
            summary = HistoryText(
                "بدأ حكم المماليك في مصر سنة 1250م، ثم ارتبطت السلطنة بمصر والشام حتى الفتح العثماني سنة 1517م.",
                "Mamluk rule in Egypt began in 1250 CE, and the sultanate later encompassed Egypt and the Levant until the Ottoman conquest in 1517.",
            ),
            context = HistoryText(
                "نشأت السلطنة في أعقاب أزمة الخلافة الأيوبية في مصر وفي سياق حروب إقليمية وتحولات عسكرية واسعة.",
                "The sultanate emerged after a crisis in Ayyubid rule in Egypt amid regional warfare and wider military change.",
            ),
            significance = HistoryText(
                "أصبحت القاهرة مركزاً سياسياً وعلمياً وتجارياً بارزاً، وربطت السلطنة شرق المتوسط والبحر الأحمر والحجاز.",
                "Cairo became a major political, scholarly, and commercial center, while the sultanate linked the eastern Mediterranean, Red Sea, and Hijaz.",
            ),
            eraIds = listOf("regional_civilizations"),
            stateIds = listOf("mamluks"),
            placeIds = listOf("cairo"),
            relatedTopicIds = listOf("trade_markets", "education_libraries"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "ottoman_emergence_1299",
            date = HistoricalDate(1299, precision = HistoryDatePrecision.Approximate),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("نشوء الدولة العثمانية", "Emergence of the Ottoman state"),
            summary = HistoryText(
                "يستخدم عام 1299م تقليدياً كتاريخ تقريبي لنشوء الإمارة العثمانية في شمال غربي الأناضول.",
                "The year 1299 CE is conventionally used as an approximate date for the emergence of the Ottoman principality in northwestern Anatolia.",
            ),
            context = HistoryText(
                "نشأت الإمارة في بيئة أناضولية متعددة الإمارات والقوى بعد تراجع السلطة السلجوقية والمغولية المباشرة.",
                "The principality emerged in a fragmented Anatolian environment of multiple powers following the decline of direct Seljuq and Mongol authority.",
            ),
            significance = HistoryText(
                "توسعت الدولة لاحقاً عبر الأناضول والبلقان ثم صارت إحدى القوى الكبرى في شرق المتوسط ومناطق أخرى.",
                "The state later expanded across Anatolia and the Balkans and became a major power in the eastern Mediterranean and beyond.",
            ),
            eraIds = listOf("regional_civilizations", "ottoman_modern"),
            stateIds = listOf("ottomans"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "constantinople_1453",
            date = HistoricalDate(1453),
            category = HistoricalEventCategory.Conflict,
            title = HistoryText("فتح القسطنطينية", "Ottoman conquest of Constantinople"),
            summary = HistoryText(
                "استولى العثمانيون بقيادة محمد الثاني على القسطنطينية سنة 1453م.",
                "The Ottomans under Mehmed II captured Constantinople in 1453 CE.",
            ),
            context = HistoryText(
                "جاء الحصار بعد عقود من التوسع العثماني في الأناضول والبلقان وفي ظل تراجع الإمبراطورية البيزنطية.",
                "The siege followed decades of Ottoman expansion in Anatolia and the Balkans amid the long decline of the Byzantine Empire.",
            ),
            significance = HistoryText(
                "أصبحت المدينة عاصمة عثمانية كبرى ومركزاً سياسياً وتجارياً وعمرانياً متعدد السكان والتقاليد.",
                "The city became a major Ottoman capital and a political, commercial, and urban center with a diverse population and cultural traditions.",
            ),
            eraIds = listOf("ottoman_modern"),
            stateIds = listOf("ottomans"),
            placeIds = listOf("istanbul"),
            relatedTopicIds = listOf("cities_urban_life", "architecture"),
            sourceIds = listOf("met_chronology", "met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "granada_1492",
            date = HistoricalDate(1492),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("نهاية حكم بني نصر في غرناطة", "End of Nasrid rule in Granada"),
            summary = HistoryText(
                "استسلمت غرناطة سنة 1492م، منهية الحكم السياسي لبني نصر وآخر دولة إسلامية كبرى في الأندلس.",
                "Granada surrendered in 1492 CE, ending Nasrid political rule and the last major Muslim-ruled state in al-Andalus.",
            ),
            context = HistoryText(
                "سبق الحدث قرون من التحولات السياسية والعسكرية في شبه الجزيرة الإيبيرية، ولم يؤدِ فوراً إلى اختفاء المجتمعات المسلمة المحلية.",
                "The event followed centuries of political and military change in Iberia and did not immediately erase local Muslim communities.",
            ),
            significance = HistoryText(
                "يمثل نهاية مرحلة سياسية، بينما استمرت آثار الأندلس الثقافية والعمرانية والفكرية بعد زوال الدولة.",
                "It marks the end of a political phase, while Andalusi cultural, architectural, and intellectual legacies continued beyond the state’s fall.",
            ),
            eraIds = listOf("regional_civilizations"),
            stateIds = listOf("nasrids"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "safavid_1501",
            date = HistoricalDate(1501),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("قيام الدولة الصفوية", "Beginning of Safavid rule"),
            summary = HistoryText(
                "أسس الشاه إسماعيل الأول الدولة الصفوية في إيران سنة 1501م.",
                "Shah Ismail I established Safavid rule in Iran in 1501 CE.",
            ),
            context = HistoryText(
                "ظهرت الدولة في فضاء إيراني وآسيوي غربي متداخل القوى، وتنافست لاحقاً مع العثمانيين والأوزبك وغيرهم.",
                "The state emerged in a politically complex Iranian and western Asian setting and later competed with the Ottomans, Uzbeks, and others.",
            ),
            significance = HistoryText(
                "أعادت الدولة تشكيل السياسة والثقافة والمؤسسات الدينية في إيران خلال القرنين السادس عشر والسابع عشر.",
                "Safavid rule reshaped politics, culture, and religious institutions in Iran during the sixteenth and seventeenth centuries.",
            ),
            eraIds = listOf("ottoman_modern"),
            stateIds = listOf("safavids"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "ottoman_egypt_1517",
            date = HistoricalDate(1517),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("دخول مصر والشام تحت الحكم العثماني", "Egypt and the Levant under Ottoman rule"),
            summary = HistoryText(
                "هزم العثمانيون سلطنة المماليك سنة 1517م، وأصبحت مصر وأجزاء واسعة من الشام ضمن الدولة العثمانية.",
                "The Ottomans defeated the Mamluk sultanate in 1517 CE, bringing Egypt and much of the Levant under Ottoman rule.",
            ),
            context = HistoryText(
                "جاء التحول ضمن تنافسات عسكرية وتجارية أوسع في شرق المتوسط والبحر الأحمر.",
                "The transition occurred amid wider military and commercial rivalries in the eastern Mediterranean and Red Sea.",
            ),
            significance = HistoryText(
                "أعاد الحدث ترتيب الإدارة والطرق السياسية والاقتصادية في المنطقة، مع استمرار أدوار النخب والمؤسسات المحلية بأشكال مختلفة.",
                "The event reorganized administration and political-economic networks in the region while many local elites and institutions continued in changing forms.",
            ),
            eraIds = listOf("ottoman_modern"),
            stateIds = listOf("mamluks", "ottomans"),
            placeIds = listOf("cairo"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "mughal_1526",
            date = HistoricalDate(1526),
            category = HistoricalEventCategory.PoliticalTransition,
            title = HistoryText("بداية الدولة المغولية في الهند", "Beginning of Mughal rule in India"),
            summary = HistoryText(
                "أسس بابر دولة المغول في شمال الهند بعد معركة بانيبات الأولى سنة 1526م.",
                "Babur established Mughal rule in northern India after the First Battle of Panipat in 1526 CE.",
            ),
            context = HistoryText(
                "جاء قيام الدولة في فضاء سياسي هندي وآسيوي أوسط متعدد السلالات وشبكات الهجرة والعسكر والثقافة الفارسية.",
                "The state emerged within a politically diverse Indian and Central Asian world shaped by dynasties, migration, military networks, and Persianate culture.",
            ),
            significance = HistoryText(
                "تطورت الإمبراطورية لاحقاً إلى إحدى أكبر دول جنوب آسيا، وأسهمت في تشكيل الإدارة والفنون والعمارة والاقتصاد في المنطقة.",
                "The empire later became one of South Asia’s largest states and shaped administration, arts, architecture, and economic life across the region.",
            ),
            eraIds = listOf("ottoman_modern"),
            stateIds = listOf("mughals"),
            relatedTopicIds = listOf("architecture", "calligraphy_decorative_arts"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalEvent(
            id = "ottoman_caliphate_abolished_1924",
            date = HistoricalDate(1924),
            category = HistoricalEventCategory.InstitutionalChange,
            title = HistoryText("إلغاء مؤسسة الخلافة العثمانية", "Abolition of the Ottoman caliphate"),
            summary = HistoryText(
                "ألغت الجمهورية التركية مؤسسة الخلافة سنة 1924م بعد انتهاء السلطنة العثمانية.",
                "The Turkish Republic abolished the caliphate institution in 1924 after the end of the Ottoman sultanate.",
            ),
            context = HistoryText(
                "جاء القرار ضمن إعادة بناء مؤسسات الدولة التركية بعد الحرب العالمية الأولى وحرب الاستقلال التركية.",
                "The decision formed part of the institutional reconstruction of the Turkish state after the First World War and the Turkish War of Independence.",
            ),
            significance = HistoryText(
                "أنهى القرار مؤسسة سياسية تاريخية مرتبطة بالدولة العثمانية، لكنه لم ينهِ التنوع السياسي والديني للمجتمعات المسلمة في العالم الحديث.",
                "The decision ended a historic political institution associated with the Ottoman state but did not end the political or religious diversity of Muslim societies in the modern world.",
            ),
            eraIds = listOf("ottoman_modern"),
            stateIds = listOf("ottomans"),
            sourceIds = listOf("met_chronology"),
        ),
    ).sortedBy { it.date.startCe ?: Int.MAX_VALUE }

    fun byId(id: String): HistoricalEvent? = events.firstOrNull { it.id == id }

    fun byCategory(category: HistoricalEventCategory?): List<HistoricalEvent> =
        if (category == null) events else events.filter { it.category == category }

    fun byEra(eraId: String?): List<HistoricalEvent> =
        if (eraId == null) events else events.filter { eraId in it.eraIds }
}
