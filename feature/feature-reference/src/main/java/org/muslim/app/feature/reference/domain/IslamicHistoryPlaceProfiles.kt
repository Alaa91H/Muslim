package org.muslim.app.feature.reference.domain

/** Long-form profiles for atlas places exposed by the history feature. */
object IslamicHistoryPlaceProfiles {
    val places = listOf(
        HistoricalPlaceProfile(
            placeId = "mecca",
            overview = HistoryText(
                "مكة مدينة محورية في السيرة والحج والتاريخ الديني الإسلامي، وكانت أيضاً جزءاً من شبكات التجارة والحركة في غرب الجزيرة العربية.",
                "Mecca is central to the Sira, Hajj, and Islamic religious history and also formed part of wider networks of trade and movement in western Arabia.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "sira",
                    title = HistoryText("مكة في السيرة", "Mecca in the Sira"),
                    paragraphs = listOf(
                        HistoryText(
                            "ارتبطت المرحلة المكية ببداية الوحي والدعوة وتكوين الجماعة المسلمة الأولى، وتفاصيلها متاحة في قسم السيرة المتخصص.",
                            "The Meccan phase is associated with the beginning of revelation, preaching, and the formation of the first Muslim community, with details available in the dedicated Sira section.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "pilgrimage",
                    title = HistoryText("الحج والحركة", "Pilgrimage and mobility"),
                    paragraphs = listOf(
                        HistoryText(
                            "أصبحت مكة عبر القرون عقدة لحركة الحجاج من مناطق بعيدة، ما ربطها بطرق برية وبحرية وأسواق وخدمات متعددة.",
                            "Over centuries Mecca became a major node for pilgrims arriving from distant regions, linking the city to overland and maritime routes, markets, and services.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("prophetic_era", "rashidun", "umayyad", "abbasid", "ottoman_modern"),
            eventIds = listOf("revelation_begins", "hijra_medina"),
            relatedTopicIds = listOf("trade_markets", "cities_urban_life"),
            sourceIds = listOf("internal_sira", "met_chronology"),
        ),
        HistoricalPlaceProfile(
            placeId = "medina",
            overview = HistoryText(
                "المدينة كانت مركز المجتمع الإسلامي في العهد المدني ومركزاً سياسياً مهماً في العقود الأولى بعد وفاة النبي ﷺ.",
                "Medina was the center of the Muslim community during the Medinan period and an important political center in the first decades after the Prophet’s death.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "community",
                    title = HistoryText("المجتمع المدني", "The Medinan community"),
                    paragraphs = listOf(
                        HistoryText(
                            "شهدت المدينة بناء مؤسسات المجتمع وتنظيم علاقاته ووقوع أحداث مركزية في السيرة بين 622 و632م.",
                            "Medina saw the organization of the community, development of institutions, and central Sira events between 622 and 632 CE.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "early_caliphate",
                    title = HistoryText("المركز السياسي المبكر", "Early political center"),
                    paragraphs = listOf(
                        HistoryText(
                            "بقيت المدينة مركزاً للقيادة في معظم فترة الخلفاء الثلاثة الأوائل، قبل أن تتغير مراكز القرار السياسي في مراحل لاحقة.",
                            "Medina remained a center of leadership through most of the first three caliphates before political decision-making shifted in later phases.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("prophetic_era", "rashidun"),
            eventIds = listOf("hijra_medina", "battle_badr", "prophet_death_632"),
            relatedTopicIds = listOf("cities_urban_life"),
            sourceIds = listOf("internal_sira", "met_chronology"),
        ),
        HistoricalPlaceProfile(
            placeId = "badr",
            overview = HistoryText(
                "بدر موضع في منطقة الحجاز ارتبط بغزوة بدر سنة 624م وبأحد أبرز أحداث السيرة النبوية.",
                "Badr is a locality in the Hijaz associated with the Battle of Badr in 624 CE, one of the major events of the Sira.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "event",
                    title = HistoryText("الحدث التاريخي", "Historical event"),
                    paragraphs = listOf(
                        HistoryText(
                            "تعرض المصادر الإسلامية غزوة بدر ضمن سياق التوتر بين مكة والمدينة بعد الهجرة، مع اختلاف في تفاصيل بعض الروايات.",
                            "Islamic sources present Badr within the context of post-Hijra tensions between Mecca and Medina, with some variation in narrative detail.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "memory",
                    title = HistoryText("المكان والذاكرة", "Place and memory"),
                    paragraphs = listOf(
                        HistoryText(
                            "اكتسب المكان مكانة في الذاكرة الدينية والتاريخية بسبب ارتباطه بالحدث، لذلك يظهر في الأطلس كموضع تاريخي لا كمدينة حضرية كبرى.",
                            "The site acquired religious and historical significance through its association with the event and is therefore shown in the atlas as a historical locality rather than a major urban center.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("prophetic_era"),
            eventIds = listOf("battle_badr"),
            sourceIds = listOf("internal_sira"),
        ),
        HistoricalPlaceProfile(
            placeId = "uhud",
            overview = HistoryText(
                "أحد منطقة جبلية قرب المدينة ارتبطت بغزوة أحد سنة 625م في روايات السيرة.",
                "Uhud is a mountainous area near Medina associated with the Battle of Uhud in 625 CE in Sira accounts.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "location",
                    title = HistoryText("الموقع", "Location"),
                    paragraphs = listOf(
                        HistoryText(
                            "يقع جبل أحد شمال المدينة، وقد جعل قربه من المدينة الموقع جزءاً مهماً من جغرافية السيرة المدنية.",
                            "Mount Uhud lies north of Medina, and its proximity to the city makes it an important part of the geography of the Medinan Sira.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "narratives",
                    title = HistoryText("روايات السيرة", "Sira narratives"),
                    paragraphs = listOf(
                        HistoryText(
                            "تتناول كتب السيرة تفاصيل المعركة وأحداثها، ويكتفي قسم التاريخ هنا بالتعريف الجغرافي والإحالة إلى المصادر المتخصصة.",
                            "Sira works provide detailed accounts of the battle; the history section limits itself to geographic orientation and directs readers to specialist material.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("prophetic_era"),
            sourceIds = listOf("internal_sira"),
        ),
        HistoricalPlaceProfile(
            placeId = "tabuk",
            overview = HistoryText(
                "تبوك مدينة وواحة في شمال غرب الجزيرة العربية ارتبطت في السيرة بحملة تبوك وبطرق الحركة بين الحجاز وبلاد الشام.",
                "Tabuk is a city and oasis in northwestern Arabia associated in the Sira with the Tabuk expedition and with routes linking the Hijaz and the Levant.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "routes",
                    title = HistoryText("الموقع والطرق", "Location and routes"),
                    paragraphs = listOf(
                        HistoryText(
                            "جعل موقع تبوك بينها وبين بلاد الشام محطة مهمة في طرق برية تاريخية، مع تغير أهمية المسارات بحسب العصور والظروف السياسية.",
                            "Tabuk’s position toward the Levant made it an important point on historic overland routes, whose importance changed across periods and political conditions.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "sira",
                    title = HistoryText("في السيرة", "In the Sira"),
                    paragraphs = listOf(
                        HistoryText(
                            "يرتبط اسمها بحملة تبوك في أواخر العهد المدني، بينما تُترك تفاصيل الروايات العسكرية والسياسية لقسم السيرة والمصادر.",
                            "Its name is associated with the Tabuk expedition late in the Medinan period, while detailed military and political narratives remain in the Sira and source material.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("prophetic_era"),
            relatedTopicIds = listOf("trade_markets"),
            sourceIds = listOf("internal_sira"),
        ),
        HistoricalPlaceProfile(
            placeId = "damascus",
            overview = HistoryText(
                "دمشق مدينة قديمة أصبحت مركز الحكم الأموي وأحد أهم المراكز السياسية والعمرانية في تاريخ بلاد الشام.",
                "Damascus is an ancient city that became the center of Umayyad rule and one of the major political and urban centers in the history of the Levant.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "umayyad",
                    title = HistoryText("العصر الأموي", "The Umayyad period"),
                    paragraphs = listOf(
                        HistoryText(
                            "منذ 661م ارتبطت دمشق بمركز الدولة الأموية، وأصبحت نقطة إدارة لأقاليم واسعة وشبكات عسكرية ومالية متعددة.",
                            "From 661 CE Damascus was associated with the center of Umayyad rule and administration of wide provinces and military and fiscal networks.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "continuity",
                    title = HistoryText("الاستمرار الحضري", "Urban continuity"),
                    paragraphs = listOf(
                        HistoryText(
                            "استمرت أهمية دمشق بعد نهاية الحكم الأموي بوصفها مدينة تجارية وعلمية ودينية في دول لاحقة.",
                            "Damascus remained important after Umayyad rule as a commercial, scholarly, and religious city under later states.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("umayyad", "regional_civilizations", "ottoman_modern"),
            stateIds = listOf("umayyad_caliphate", "ayyubids", "mamluks", "ottomans"),
            eventIds = listOf("umayyad_rule_661"),
            relatedTopicIds = listOf("cities_urban_life", "architecture"),
            sourceIds = listOf("met_chronology", "met_major_dynasties"),
        ),
        HistoricalPlaceProfile(
            placeId = "cordoba",
            overview = HistoryText(
                "قرطبة كانت من أبرز مراكز الأندلس السياسية والحضرية والثقافية، ولا سيما في عهد الأمويين في الأندلس.",
                "Cordoba was one of the leading political, urban, and cultural centers of al-Andalus, especially under the Umayyads of al-Andalus.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "capital",
                    title = HistoryText("مركز الحكم", "Center of rule"),
                    paragraphs = listOf(
                        HistoryText(
                            "أصبحت قرطبة مركز الإمارة الأموية ثم الخلافة الأموية في الأندلس، وربطت الإدارة بالحياة الحضرية والتجارة.",
                            "Cordoba became the center of the Umayyad emirate and later caliphate in al-Andalus, linking government to urban and commercial life.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "culture",
                    title = HistoryText("العلم والعمران", "Learning and urban culture"),
                    paragraphs = listOf(
                        HistoryText(
                            "ارتبطت المدينة بالمكتبات والتعليم والحرف والعمارة، لكن حجم هذه الأنشطة وتقديرات السكان تختلف باختلاف المصادر والفترات.",
                            "The city is associated with libraries, learning, crafts, and architecture, although estimates of their scale and of population vary across sources and periods.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("regional_civilizations"),
            stateIds = listOf("umayyads_al_andalus"),
            eventIds = listOf("al_andalus_711"),
            relatedTopicIds = listOf("cities_urban_life", "education_libraries", "architecture"),
            sourceIds = listOf("met_major_dynasties", "met_islamic_art"),
        ),
        HistoricalPlaceProfile(
            placeId = "kairouan",
            overview = HistoryText(
                "القيروان مدينة أسست في القرن السابع في إفريقية وأصبحت مركزاً سياسياً ودينياً وعلمياً مهماً في المغرب الإسلامي.",
                "Kairouan was founded in the seventh century in Ifriqiya and became an important political, religious, and scholarly center in the Islamic Maghreb.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "regional_center",
                    title = HistoryText("مركز إقليمي", "Regional center"),
                    paragraphs = listOf(
                        HistoryText(
                            "ارتبطت القيروان بإدارة إفريقية وبحركة العلماء والتجار والحجاج بين المغرب والمشرق عبر فترات مختلفة.",
                            "Kairouan was tied to the administration of Ifriqiya and to movements of scholars, merchants, and pilgrims between the Maghreb and the eastern Islamic world.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "built_environment",
                    title = HistoryText("العمران", "Built environment"),
                    paragraphs = listOf(
                        HistoryText(
                            "تُعد منشآتها الدينية والعمرانية من الشواهد المهمة على تطور المدن في شمال إفريقيا، مع تعرض المدينة لتحولات سياسية واقتصادية متعاقبة.",
                            "Its religious and urban monuments provide important evidence for the development of North African cities amid repeated political and economic change.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("umayyad", "regional_civilizations"),
            relatedTopicIds = listOf("cities_urban_life", "architecture", "education_libraries"),
            sourceIds = listOf("met_chronology", "met_islamic_art"),
        ),
        HistoricalPlaceProfile(
            placeId = "baghdad",
            overview = HistoryText(
                "بغداد أسست سنة 762م وأصبحت أحد أهم مراكز الحكم والتجارة والعلم في العصر العباسي ومراحل لاحقة.",
                "Baghdad was founded in 762 CE and became one of the most important centers of government, commerce, and learning in the Abbasid period and beyond.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "foundation",
                    title = HistoryText("التأسيس والموقع", "Foundation and location"),
                    paragraphs = listOf(
                        HistoryText(
                            "استفاد موقع بغداد على دجلة وقرب مناطق زراعية وطرق برية من شبكات العراق الاقتصادية، وساعد ذلك على نموها السريع.",
                            "Baghdad’s location on the Tigris near agricultural zones and overland routes connected it to Iraq’s economic networks and supported rapid growth.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "knowledge",
                    title = HistoryText("العلم والكتاب", "Learning and book culture"),
                    paragraphs = listOf(
                        HistoryText(
                            "احتضنت المدينة علماء ومترجمين وأطباء وكتّاباً وحرفيين، وأصبحت جزءاً من شبكات واسعة للكتاب والتعليم والرعاية العلمية.",
                            "The city hosted scholars, translators, physicians, writers, and artisans and became part of wide networks of books, education, and scholarly patronage.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("abbasid", "regional_civilizations"),
            stateIds = listOf("abbasid_caliphate", "ilkhanids"),
            eventIds = listOf("baghdad_founded_762", "abbasid_translation_scholarship", "baghdad_1258"),
            relatedTopicIds = listOf("translation_books", "education_libraries", "cities_urban_life"),
            sourceIds = listOf("met_chronology", "met_science"),
        ),
        HistoricalPlaceProfile(
            placeId = "basra",
            overview = HistoryText(
                "البصرة مركز حضري وميناء مهم في جنوب العراق، ارتبط بالتجارة واللغة والعلوم الدينية والأدبية في عصور مبكرة.",
                "Basra was an important urban center and port in southern Iraq, associated with trade, language, religious learning, and literary scholarship from early periods.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "port",
                    title = HistoryText("الميناء والشبكات", "Port and networks"),
                    paragraphs = listOf(
                        HistoryText(
                            "ربط موقع البصرة العراق بالخليج والمحيط الهندي، وكانت طرق الملاحة والتجارة تتغير بحسب المواسم والدول والأمن.",
                            "Basra’s location connected Iraq with the Gulf and Indian Ocean, while maritime and commercial routes changed with seasons, states, and security.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "learning",
                    title = HistoryText("مركز معرفي", "Center of learning"),
                    paragraphs = listOf(
                        HistoryText(
                            "ارتبطت المدينة بمدارس مبكرة في اللغة والأدب والعلوم الدينية، وأسهمت الحركة بين البصرة ومدن العراق الأخرى في تداول المعرفة.",
                            "The city is associated with early traditions of language, literature, and religious learning, with movement between Basra and other Iraqi cities supporting knowledge circulation.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("rashidun", "umayyad", "abbasid"),
            relatedTopicIds = listOf("trade_markets", "education_libraries"),
            sourceIds = listOf("unesco_silk_roads", "met_chronology"),
        ),
        HistoricalPlaceProfile(
            placeId = "samarkand",
            overview = HistoryText(
                "سمرقند مدينة رئيسية في آسيا الوسطى ارتبطت بطرق التجارة وبسلالات ومراكز علم وفن متعددة عبر القرون.",
                "Samarkand is a major Central Asian city associated with trade routes and with multiple dynasties and centers of learning and art across centuries.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "silk_roads",
                    title = HistoryText("طرق التجارة", "Trade routes"),
                    paragraphs = listOf(
                        HistoryText(
                            "كان موقع سمرقند ضمن شبكات آسيا الوسطى يجعلها نقطة تبادل للسلع والأشخاص واللغات والأفكار، لا محطة على طريق واحد ثابت.",
                            "Samarkand’s place within Central Asian networks made it a point of exchange for goods, people, languages, and ideas rather than a stop on one fixed route.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "timurid",
                    title = HistoryText("العصر التيموري", "The Timurid period"),
                    paragraphs = listOf(
                        HistoryText(
                            "برزت المدينة بصورة خاصة في العصر التيموري بوصفها مركزاً للرعاية المعمارية والفنية والعلمية.",
                            "The city became especially prominent under the Timurids as a center of architectural, artistic, and scholarly patronage.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("abbasid", "regional_civilizations", "ottoman_modern"),
            stateIds = listOf("samanids", "timurids"),
            relatedTopicIds = listOf("trade_markets", "architecture", "astronomy"),
            sourceIds = listOf("unesco_silk_roads", "met_timurid"),
        ),
        HistoricalPlaceProfile(
            placeId = "istanbul",
            overview = HistoryText(
                "إسطنبول، القسطنطينية سابقاً، أصبحت عاصمة عثمانية بعد 1453م ومركزاً سياسياً وتجارياً وعمرانياً رئيسياً.",
                "Istanbul, formerly Constantinople, became an Ottoman capital after 1453 and a major political, commercial, and urban center.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "capital",
                    title = HistoryText("العاصمة العثمانية", "Ottoman capital"),
                    paragraphs = listOf(
                        HistoryText(
                            "بعد الفتح العثماني أعيد تنظيم أجزاء من المدينة واستمرت فيها جماعات دينية ولغوية متعددة ضمن نظام إمبراطوري متغير.",
                            "After the Ottoman conquest parts of the city were reorganized, while multiple religious and linguistic communities continued within a changing imperial system.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "urbanism",
                    title = HistoryText("العمران والمؤسسات", "Urbanism and institutions"),
                    paragraphs = listOf(
                        HistoryText(
                            "شكلت المساجد والأسواق والأوقاف والمياه والطرق والمرافئ جزءاً من نمو المدينة، وارتبطت مشاريع معمار سنان خصوصاً بمشهدها في القرن السادس عشر.",
                            "Mosques, markets, endowments, water systems, roads, and ports shaped the city’s growth, with Mimar Sinan’s projects especially influential in the sixteenth-century urban landscape.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("ottoman_modern"),
            stateIds = listOf("ottomans"),
            eventIds = listOf("constantinople_1453"),
            relatedTopicIds = listOf("architecture", "waqf", "cities_urban_life"),
            relatedPersonIds = listOf("mimar_sinan"),
            sourceIds = listOf("met_chronology", "met_islamic_art"),
        ),
        HistoricalPlaceProfile(
            placeId = "cairo",
            overview = HistoryText(
                "القاهرة تأسست في العصر الفاطمي وأصبحت عبر دول لاحقة مركزاً سياسياً وعلمياً وتجارياً وعمرانياً من أهم مدن المنطقة.",
                "Cairo was founded in the Fatimid period and became under later states one of the region’s major political, scholarly, commercial, and urban centers.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "fatimid_foundation",
                    title = HistoryText("التأسيس الفاطمي", "Fatimid foundation"),
                    paragraphs = listOf(
                        HistoryText(
                            "أُنشئت القاهرة سنة 969م قرب الفسطاط لتكون مركزاً للحكم الفاطمي، ثم اتسع المجال الحضري واندفعت المدينة إلى علاقات معقدة مع المراكز الأقدم حولها.",
                            "Cairo was founded in 969 CE near Fustat as a center of Fatimid rule; the urban area later expanded through complex relationships with older settlements around it.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "later_centuries",
                    title = HistoryText("الأيوبيون والمماليك والعثمانيون", "Ayyubids, Mamluks, and Ottomans"),
                    paragraphs = listOf(
                        HistoryText(
                            "حافظت القاهرة على مكانة كبيرة في دول لاحقة، وارتبطت بالمدارس والبيمارستانات والأسواق والأوقاف والحرف وشبكات البحر الأحمر وشرق المتوسط.",
                            "Cairo retained major importance under later states and was associated with madrasas, bimaristans, markets, endowments, crafts, and networks linking the Red Sea and eastern Mediterranean.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("regional_civilizations", "ottoman_modern"),
            stateIds = listOf("fatimids", "ayyubids", "mamluks", "ottomans"),
            eventIds = listOf("cairo_founded_969", "ayyubid_1171", "mamluk_1250", "ottoman_egypt_1517"),
            relatedTopicIds = listOf("cities_urban_life", "education_libraries", "medicine_bimaristans", "waqf"),
            relatedPersonIds = listOf("ibn_al_haytham", "ibn_khaldun"),
            sourceIds = listOf("met_fatimid", "met_ayyubid", "met_chronology"),
        ),
    )

    fun byId(id: String): HistoricalPlaceProfile? = places.firstOrNull { it.placeId == id }
}
