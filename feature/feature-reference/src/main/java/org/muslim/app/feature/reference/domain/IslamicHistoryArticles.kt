package org.muslim.app.feature.reference.domain

/**
 * First long-form history articles.
 *
 * This is intentionally a curated first batch: it replaces the "headline-only" experience
 * for every era currently visible in the timeline while the larger article/event/place
 * catalogue is built out incrementally.
 */
object IslamicHistoryArticles {
    val sources = listOf(
        HistorySource(
            id = "met_chronology",
            title = HistoryText(
                "التسلسل الزمني للعالم الإسلامي — متحف المتروبوليتان للفنون",
                "Chronology of the Islamic World — The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world/introduction/chronology",
        ),
        HistorySource(
            id = "met_science",
            title = HistoryText(
                "العلم وفنون العالم الإسلامي — متحف المتروبوليتان للفنون",
                "Science and the Art of the Islamic World — The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world/unit-four",
        ),
        HistorySource(
            id = "unesco_silk_roads",
            title = HistoryText(
                "حول طرق الحرير — اليونسكو",
                "About the Silk Roads — UNESCO",
            ),
            kind = HistorySourceKind.Unesco,
            url = "https://www.unesco.org/en/silk-roads/about-silk-roads",
        ),
        HistorySource(
            id = "world_history_caliphates",
            title = HistoryText(
                "الخلافات الإسلامية — موسوعة التاريخ العالمي",
                "Islamic Caliphates — World History Encyclopedia",
            ),
            kind = HistorySourceKind.Reference,
            url = "https://www.worldhistory.org/Islamic_Caliphates/",
            note = HistoryText(
                "مرجع تمهيدي مساعد، ويُقارن بالمراجع المتحفية والأكاديمية قبل اعتماد التفاصيل.",
                "A supplementary orientation source; details are cross-checked against museum and academic references.",
            ),
        ),
        HistorySource(
            id = "internal_sira",
            title = HistoryText(
                "قسم السيرة النبوية في المكتبة المرجعية داخل التطبيق",
                "The in-app Sira reference library",
            ),
            kind = HistorySourceKind.Internal,
            note = HistoryText(
                "يُستخدم للربط مع السرد التفصيلي للسيرة بدلاً من تكراره داخل قسم التاريخ.",
                "Used to link to the detailed Sira narrative rather than duplicating it in the history section.",
            ),
        ),
    )

    val articles = listOf(
        HistoryArticle(
            id = "era_prophetic_mission",
            eraId = "prophetic_era",
            title = HistoryText("البعثة والسيرة النبوية", "Prophetic mission and sira"),
            lead = HistoryText(
                "تضع هذه المرحلة نشأة المجتمع الإسلامي الأول في سياقها الزمني بين مكة والمدينة، مع إبقاء تفاصيل السيرة في قسمها المتخصص داخل المكتبة.",
                "This period places the formation of the first Muslim community in chronological context between Mecca and Medina, while detailed Sira remains in its dedicated library section.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "meccan_phase",
                    title = HistoryText("المرحلة المكية", "The Meccan phase"),
                    paragraphs = listOf(
                        HistoryText(
                            "ترتبط بداية الدعوة الإسلامية في الموروث الإسلامي ببدء الوحي سنة 610م تقريباً. شهدت المرحلة المكية تشكل الجماعة المسلمة الأولى وتركيز الخطاب على التوحيد والمسؤولية الأخلاقية واليوم الآخر، في بيئة اجتماعية وتجارية قبلية معقدة.",
                            "Islamic tradition associates the beginning of the mission with the start of revelation around 610 CE. The Meccan phase saw the formation of the first Muslim community and a strong emphasis on monotheism, moral responsibility, and the afterlife within a complex tribal and commercial society.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "hijra",
                    title = HistoryText("الهجرة والتحول إلى المدينة", "Migration and the move to Medina"),
                    paragraphs = listOf(
                        HistoryText(
                            "أصبحت الهجرة إلى المدينة سنة 622م نقطة تحول زمنية ومجتمعية؛ ومنها يبدأ التقويم الهجري. انتقل المسلمون من جماعة مضغوطة في مكة إلى مجتمع منظم في المدينة له علاقات وتحالفات ومسؤوليات عامة أوسع.",
                            "The migration to Medina in 622 CE became a major chronological and social turning point and later the starting point of the Hijri calendar. Muslims moved from a pressured community in Mecca into a more organized society in Medina with broader alliances and public responsibilities.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "medinan_community",
                    title = HistoryText("المجتمع المدني", "The Medinan community"),
                    paragraphs = listOf(
                        HistoryText(
                            "شهدت السنوات المدنية بناء مؤسسات المجتمع، وتنظيم علاقاته الداخلية والخارجية، ووقوع عدد من المواجهات والاتفاقات التي تعرضها كتب السيرة بتفاصيل وروايات متفاوتة. لهذا يعرض قسم التاريخ الإطار العام ويُحيل إلى قسم السيرة للقراءة التفصيلية.",
                            "The Medinan years included institution-building, the organization of internal and external relations, and a series of conflicts and agreements described in Sira literature with varying levels of detail and transmission. The history section therefore provides the broad frame and links to the dedicated Sira material for fuller study.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "transition_632",
                    title = HistoryText("سنة 632م والانتقال التاريخي", "632 CE and the historical transition"),
                    paragraphs = listOf(
                        HistoryText(
                            "توفي النبي محمد ﷺ سنة 632م، وبدأت بعد ذلك مرحلة سياسية جديدة قادها الخلفاء الراشدون. لفهم هذا الانتقال يجب التمييز بين السيرة الدينية للنبي ﷺ وبين التاريخ السياسي والإداري اللاحق للدولة والمجتمعات الإسلامية.",
                            "Prophet Muhammad died in 632 CE, after which a new political phase began under the Rashidun caliphs. Understanding this transition benefits from distinguishing the Prophet’s religious biography from the later political and administrative history of Muslim states and societies.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_chronology", "internal_sira"),
            relatedEraIds = listOf("rashidun"),
            tags = setOf("sira", "mecca", "medina", "hijra"),
        ),
        HistoryArticle(
            id = "era_rashidun",
            eraId = "rashidun",
            title = HistoryText("الخلافة الراشدة", "Rashidun caliphate"),
            lead = HistoryText(
                "مرحلة 632–661م شكلت الانتقال من مجتمع المدينة في أواخر السيرة إلى دولة أوسع جغرافياً، مع تغيرات سريعة في الإدارة والحرب والموارد ومراكز السلطة.",
                "The period from 632 to 661 CE marked the transition from the Medinan community of the late Sira to a much wider polity, accompanied by rapid changes in administration, warfare, resources, and centers of authority.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "succession",
                    title = HistoryText("الخلافة بعد وفاة النبي ﷺ", "Leadership after the Prophet’s death"),
                    paragraphs = listOf(
                        HistoryText(
                            "بدأت المرحلة بخلافة أبي بكر ثم عمر ثم عثمان ثم علي رضي الله عنهم. وتتناول المصادر الإسلامية المبكرة واللاحقة تفاصيل البيعة والإدارة والخلافات السياسية بدرجات مختلفة من السرد والتفسير، لذلك ينبغي عرض المسائل المتنازع عليها مع نسب الأقوال إلى مصادرها.",
                            "The period proceeded through the caliphates of Abu Bakr, Umar, Uthman, and Ali. Early and later Muslim sources describe questions of allegiance, administration, and political disagreement with differing levels of detail and interpretation, so disputed matters should be presented with claims attributed to their sources.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "expansion",
                    title = HistoryText("الاتساع والتحول الإداري", "Expansion and administrative change"),
                    paragraphs = listOf(
                        HistoryText(
                            "اتسعت مناطق الحكم بسرعة خارج الجزيرة العربية إلى بلاد الشام والعراق ومصر وأجزاء من إيران. فرض هذا الاتساع تحديات جديدة في إدارة المدن والجند والموارد والضرائب، وأسهم في ظهور أمصار ومراكز إدارية وعسكرية جديدة.",
                            "Rule expanded rapidly beyond Arabia into the Levant, Iraq, Egypt, and parts of Iran. This expansion created new challenges in managing cities, armies, resources, and taxation and contributed to the rise of new garrison towns and administrative centers.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "centres",
                    title = HistoryText("المدينة والكوفة ومراكز الحكم", "Medina, Kufa, and centers of rule"),
                    paragraphs = listOf(
                        HistoryText(
                            "بقيت المدينة مركزاً سياسياً ورمزياً أساسياً في معظم المرحلة، ثم برزت الكوفة بصورة خاصة في خلافة علي. ويكشف انتقال مركز القرار بين المدن عن ارتباط السياسة بالتغيرات العسكرية والجغرافية والاجتماعية.",
                            "Medina remained a major political and symbolic center through much of the period, while Kufa became especially important during Ali’s caliphate. The movement of decision-making between cities illustrates the connection between politics and military, geographic, and social change.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "first_fitna",
                    title = HistoryText("الفتنة الأولى ونهاية المرحلة", "The First Fitna and the end of the period"),
                    paragraphs = listOf(
                        HistoryText(
                            "شهدت أواخر المرحلة صراعات داخلية عميقة عُرفت في التأريخ الإسلامي بالفتنة الأولى. تختلف الروايات في بعض تفاصيل الأحداث ودوافع المشاركين، ولهذا يعرض التطبيق التسلسل المتفق عليه قدر الإمكان ويتجنب تحويل الروايات الجدلية إلى حقائق غير منسوبة.",
                            "The later years saw deep internal conflicts commonly known in Islamic historiography as the First Fitna. Sources differ on some details and on interpretations of participants’ motives, so the app presents the broad chronology where possible and avoids turning contested narratives into unattributed fact.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_chronology", "world_history_caliphates"),
            relatedEraIds = listOf("prophetic_era", "umayyad"),
            tags = setOf("caliphate", "administration", "expansion", "fitna"),
        ),
        HistoryArticle(
            id = "era_umayyad",
            eraId = "umayyad",
            title = HistoryText("الدولة الأموية", "Umayyad caliphate"),
            lead = HistoryText(
                "بين 661 و750م أصبحت دمشق مركزاً لدولة واسعة ربطت أقاليم متعددة من غرب المتوسط إلى آسيا الوسطى، ورافقت ذلك تحولات إدارية ونقدية وعمرانية كبيرة.",
                "Between 661 and 750 CE, Damascus became the center of a wide state linking regions from the western Mediterranean to Central Asia, alongside major administrative, monetary, and urban changes.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "damascus",
                    title = HistoryText("دمشق مركزاً للحكم", "Damascus as a center of rule"),
                    paragraphs = listOf(
                        HistoryText(
                            "ارتبط قيام الدولة الأموية بانتقال مركز الحكم إلى دمشق، وهي مدينة ذات موقع استراتيجي وشبكات إدارية وتجارية راسخة. ساعدت هذه البيئة في إدارة أقاليم واسعة ومتنوعة سكانياً ولغوياً.",
                            "The rise of Umayyad rule was associated with the transfer of the political center to Damascus, a strategically located city with established administrative and commercial networks. This setting helped govern a wide and linguistically and socially diverse set of provinces.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "administration",
                    title = HistoryText("الإدارة والعملة واللغة", "Administration, coinage, and language"),
                    paragraphs = listOf(
                        HistoryText(
                            "شهد العصر الأموي عمليات متدرجة لتعريب جانب من الإدارة وتطوير أنماط نقدية وإدارية أكثر توحيداً. لم تكن هذه التحولات لحظة واحدة، بل مساراً اختلف توقيته وطبيعته من إقليم إلى آخر.",
                            "The Umayyad period saw gradual processes of Arabizing parts of the administration and developing more standardized monetary and administrative practices. These changes were not a single event and varied in timing and form across regions.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "west_east",
                    title = HistoryText("من الأندلس إلى آسيا الوسطى", "From al-Andalus to Central Asia"),
                    paragraphs = listOf(
                        HistoryText(
                            "شهد القرن الثامن توسعاً في الغرب إلى شبه الجزيرة الإيبيرية وفي الشرق نحو مناطق آسيا الوسطى والسند. ويجب قراءة هذه التحركات ضمن سياقات محلية متعددة، لا باعتبارها عملية متطابقة في كل المناطق.",
                            "The eighth century saw expansion westward into the Iberian Peninsula and eastward toward parts of Central Asia and Sindh. These developments should be read within distinct local contexts rather than as an identical process everywhere.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "transition_750",
                    title = HistoryText("التحول إلى العصر العباسي", "Transition to Abbasid rule"),
                    paragraphs = listOf(
                        HistoryText(
                            "انتهى الحكم الأموي في المشرق سنة 750م بعد الثورة العباسية، بينما استمر فرع أموي لاحقاً في الأندلس. لذلك لا تمثل سنة 750م نهاية الحضور الأموي في التاريخ الإسلامي كله.",
                            "Umayyad rule in the eastern caliphate ended in 750 CE after the Abbasid revolution, while an Umayyad branch later continued in al-Andalus. The year 750 therefore did not mark the end of all Umayyad political presence in Islamic history.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_chronology", "world_history_caliphates"),
            relatedEraIds = listOf("rashidun", "abbasid", "regional_civilizations"),
            tags = setOf("damascus", "administration", "al-andalus", "coinage"),
        ),
        HistoryArticle(
            id = "era_abbasid",
            eraId = "abbasid",
            title = HistoryText("الدولة العباسية", "Abbasid caliphate"),
            lead = HistoryText(
                "بدأ الحكم العباسي سنة 750م، وأصبحت بغداد منذ تأسيسها في القرن الثامن إحدى أهم مدن السياسة والتجارة والمعرفة في العالم الإسلامي، مع تغير قوة الخلافة وظهور دول إقليمية عبر القرون.",
                "Abbasid rule began in 750 CE, and Baghdad, founded in the eighth century, became one of the most important political, commercial, and intellectual cities of the Islamic world, even as caliphal power changed and regional states emerged over the centuries.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "baghdad",
                    title = HistoryText("بغداد وشبكات الدولة", "Baghdad and imperial networks"),
                    paragraphs = listOf(
                        HistoryText(
                            "تأسست بغداد سنة 762م وأصبحت مركزاً إدارياً واقتصادياً متصلاً بالعراق وإيران وآسيا الوسطى والخليج وطرق أبعد. أسهم موقعها وشبكاتها النهرية والبرية في نموها السريع.",
                            "Baghdad was founded in 762 CE and became an administrative and economic center connected to Iraq, Iran, Central Asia, the Gulf, and more distant routes. Its location and river and overland networks contributed to its rapid growth.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "knowledge",
                    title = HistoryText("العلم والترجمة", "Learning and translation"),
                    paragraphs = listOf(
                        HistoryText(
                            "ازدهرت في بغداد ومدن أخرى أنشطة الترجمة والتأليف والطب والفلك والرياضيات. شارك في هذه الحياة العلمية مسلمون ومسيحيون ويهود وصابئة وغيرهم، وكان انتقال المعرفة عملية تراكمية عبر لغات ومؤسسات وشبكات رعاية متعددة.",
                            "Baghdad and other cities supported major activity in translation, authorship, medicine, astronomy, and mathematics. Muslims, Christians, Jews, Sabians, and others participated in this intellectual life, and knowledge moved cumulatively through multiple languages, institutions, and patronage networks.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "regional_power",
                    title = HistoryText("صعود الدول والمراكز الإقليمية", "The rise of regional states and centers"),
                    paragraphs = listOf(
                        HistoryText(
                            "مع مرور الوقت لم تعد السلطة السياسية مركزة بصورة واحدة في بغداد؛ ظهرت سلالات ودول إقليمية تمتعت بدرجات مختلفة من الاستقلال، بينما استمرت مكانة الخلافة العباسية الدينية والسياسية بأشكال متغيرة.",
                            "Over time, political authority was no longer concentrated in a single way in Baghdad. Regional dynasties and states exercised varying degrees of independence while the Abbasid caliphate retained changing forms of religious and political significance.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "1258",
                    title = HistoryText("سقوط بغداد سنة 1258م", "The fall of Baghdad in 1258"),
                    paragraphs = listOf(
                        HistoryText(
                            "استولى المغول على بغداد سنة 1258م، وكان الحدث تحوّلاً كبيراً في تاريخ العراق والخلافة العباسية في المدينة. لكنه لم يكن نهاية الحضارة الإسلامية أو المجتمعات المسلمة؛ فقد استمرت مراكز سياسية وعلمية عديدة في مصر والشام والأناضول وإيران وآسيا الوسطى وغيرها.",
                            "The Mongols captured Baghdad in 1258 CE, a major turning point for Iraq and the Abbasid caliphate in the city. It was not the end of Islamic civilization or Muslim societies, however; numerous political and intellectual centers continued in Egypt, the Levant, Anatolia, Iran, Central Asia, and elsewhere.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_chronology", "met_science", "unesco_silk_roads"),
            relatedEraIds = listOf("umayyad", "regional_civilizations"),
            tags = setOf("baghdad", "translation", "science", "trade"),
        ),
        HistoryArticle(
            id = "era_regional_civilizations",
            eraId = "regional_civilizations",
            title = HistoryText("حضارات ومراكز إقليمية", "Regional civilizations and centers"),
            lead = HistoryText(
                "لا يمكن اختزال التاريخ الإسلامي الوسيط في خلافة واحدة؛ فقد تزامنت مراكز ودول ومجتمعات متعددة من الأندلس والمغرب إلى مصر والشام وإيران وآسيا الوسطى والهند وإفريقيا.",
                "Medieval Islamic history cannot be reduced to a single caliphate. Multiple centers, states, and societies coexisted from al-Andalus and the Maghreb to Egypt, the Levant, Iran, Central Asia, India, and Africa.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "plural_centres",
                    title = HistoryText("تعدد المراكز", "Multiple centers"),
                    paragraphs = listOf(
                        HistoryText(
                            "كانت قرطبة والقاهرة والقيروان وفاس ودمشق وبخارى وسمرقند وغيرها مراكز سياسية أو علمية أو تجارية في أزمنة مختلفة. ويكشف هذا التعدد أن النشاط الحضاري لم ينتقل في خط واحد من عاصمة إلى أخرى.",
                            "Cordoba, Cairo, Kairouan, Fez, Damascus, Bukhara, Samarkand, and other cities served as political, intellectual, or commercial centers at different times. This plurality shows that cultural activity did not move along a single line from one capital to another.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "trade",
                    title = HistoryText("التجارة وحركة الناس والأفكار", "Trade and the movement of people and ideas"),
                    paragraphs = listOf(
                        HistoryText(
                            "ربطت شبكات برية وبحرية مناطق البحر المتوسط والصحراء الكبرى والبحر الأحمر والخليج والمحيط الهندي وآسيا الوسطى. نقلت هذه الشبكات السلع والكتب والتقنيات واللغات والأفكار، وكانت طرقها تتغير بتغير الأمن والأسواق والدول.",
                            "Overland and maritime networks connected the Mediterranean, the Sahara, the Red Sea, the Gulf, the Indian Ocean, and Central Asia. These networks carried goods, books, technologies, languages, and ideas, while routes shifted with security, markets, and political change.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "institutions",
                    title = HistoryText("مؤسسات العلم والمدينة", "Institutions of learning and urban life"),
                    paragraphs = listOf(
                        HistoryText(
                            "دعمت المساجد والمدارس والمكتبات والبيمارستانات والأوقاف أشكالاً مختلفة من التعليم والخدمة العامة. اختلفت هذه المؤسسات من مكان إلى آخر ومن عصر إلى آخر، لذلك يتجنب التطبيق تقديم نموذج حضاري واحد ثابت لكل العالم الإسلامي.",
                            "Mosques, madrasas, libraries, hospitals, and endowments supported different forms of learning and public service. These institutions varied by region and period, so the app avoids presenting a single fixed institutional model for the entire Islamic world.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "arts",
                    title = HistoryText("الفنون والعمارة", "Arts and architecture"),
                    paragraphs = listOf(
                        HistoryText(
                            "تطورت أنماط معمارية وفنية محلية متعددة تحت تأثير المواد المتاحة والتقاليد السابقة وشبكات الحرفيين والرعاية السياسية والدينية. لذلك يدرس القسم الحضارة بوصفها شبكة من التجارب الإقليمية المتفاعلة.",
                            "Multiple local architectural and artistic traditions developed through available materials, earlier traditions, artisan networks, and political and religious patronage. The civilization section therefore treats Islamic civilization as a network of interacting regional experiences.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_chronology", "met_science", "unesco_silk_roads"),
            relatedEraIds = listOf("umayyad", "abbasid", "ottoman_modern"),
            tags = setOf("cities", "trade", "institutions", "architecture"),
        ),
        HistoryArticle(
            id = "era_ottoman_modern",
            eraId = "ottoman_modern",
            title = HistoryText("العثمانيون والعصر الحديث", "Ottomans and the modern era"),
            lead = HistoryText(
                "يمتد هذا المدخل من نشوء الدولة العثمانية أواخر القرن الثالث عشر إلى التحولات الكبرى التي أعادت تشكيل المجتمعات والدول ذات الأغلبية المسلمة في العصر الحديث.",
                "This entry runs from the rise of the Ottoman state in the late thirteenth century to the major transformations that reshaped Muslim-majority societies and states in the modern era.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "rise",
                    title = HistoryText("النشوء والتوسع", "Formation and expansion"),
                    paragraphs = listOf(
                        HistoryText(
                            "نشأت الدولة العثمانية في الأناضول ضمن بيئة سياسية متعددة القوى، ثم توسعت في البلقان والأناضول وشرق المتوسط. كان فتح القسطنطينية سنة 1453م محطة بارزة جعلت إسطنبول مركزاً إمبراطورياً مهماً.",
                            "The Ottoman state emerged in Anatolia within a politically fragmented environment and later expanded through the Balkans, Anatolia, and the eastern Mediterranean. The conquest of Constantinople in 1453 CE was a major milestone that made Istanbul an important imperial center.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "institutions",
                    title = HistoryText("الإدارة والمجتمع", "Administration and society"),
                    paragraphs = listOf(
                        HistoryText(
                            "حكم العثمانيون أقاليم شديدة التنوع الديني واللغوي والاجتماعي عبر مؤسسات تغيرت عبر القرون. لا ينبغي تصوير النظام العثماني كتركيب ثابت؛ فقد تطورت علاقات المركز بالأقاليم والجيش والضرائب والقضاء باستمرار.",
                            "The Ottomans governed highly diverse religious, linguistic, and social populations through institutions that changed over centuries. The Ottoman system should not be treated as static; relations between the center, provinces, military, taxation, and courts evolved continuously.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "coexisting_states",
                    title = HistoryText("عالم إسلامي متعدد الدول", "A multi-state Islamic world"),
                    paragraphs = listOf(
                        HistoryText(
                            "تزامنت الدولة العثمانية مع دول إسلامية كبرى أخرى، منها الصفويون في إيران والمغول في الهند، إضافة إلى دول ومجتمعات في إفريقيا وآسيا الوسطى وجنوب شرق آسيا. ولهذا لا يستخدم التطبيق العثمانيين بوصفهم إطاراً وحيداً لكل التاريخ الإسلامي المتأخر.",
                            "The Ottoman state coexisted with other major Muslim powers, including the Safavids in Iran and the Mughals in India, alongside states and societies in Africa, Central Asia, and Southeast Asia. The app therefore does not use the Ottoman experience as the sole framework for later Islamic history.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "modern_transition",
                    title = HistoryText("التحول إلى العصر الحديث", "Transition into the modern era"),
                    paragraphs = listOf(
                        HistoryText(
                            "شهد القرنان التاسع عشر والعشرون إصلاحات داخلية، وضغوطاً إمبراطورية أوروبية، وحروباً وتغيرات اقتصادية وحدودية واسعة. أُلغيت مؤسسة الخلافة العثمانية في تركيا سنة 1924م، بينما استمرت المجتمعات المسلمة بعد ذلك في مسارات وطنية وإقليمية وعالمية متعددة.",
                            "The nineteenth and twentieth centuries brought internal reforms, European imperial pressure, wars, and broad economic and territorial change. The Ottoman caliphate institution was abolished in Türkiye in 1924, while Muslim societies thereafter continued along diverse national, regional, and global trajectories.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_chronology", "unesco_silk_roads"),
            relatedEraIds = listOf("regional_civilizations"),
            tags = setOf("ottoman", "istanbul", "modern", "institutions"),
        ),
    )

    fun articleForEra(eraId: String): HistoryArticle? = articles.firstOrNull { it.eraId == eraId }

    fun sourceById(id: String): HistorySource? = sources.firstOrNull { it.id == id }
}
