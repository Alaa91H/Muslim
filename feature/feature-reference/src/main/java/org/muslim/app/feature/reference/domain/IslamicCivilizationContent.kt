package org.muslim.app.feature.reference.domain

/**
 * Curated civilization catalogue covering knowledge, institutions, society/economy,
 * and the built/visual environment. Entries are deliberately thematic rather than tied
 * to one dynasty because these practices developed differently across regions and periods.
 */
object IslamicCivilizationContent {
    val topics = listOf(
        CivilizationTopic(
            id = "translation_books",
            category = CivilizationCategory.KnowledgeAndSciences,
            title = HistoryText("الترجمة والكتاب وانتقال المعرفة", "Translation, books, and knowledge circulation"),
            summary = HistoryText(
                "حركة معرفية متعددة المراكز واللغات نقلت النصوص وشرحتها وراجعتها وأعادت توظيفها في سياقات جديدة.",
                "A multilingual and multi-centered scholarly process in which texts were translated, commented on, revised, and reused in new contexts.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "languages_networks",
                    title = HistoryText("لغات وشبكات متعددة", "Multiple languages and networks"),
                    paragraphs = listOf(
                        HistoryText(
                            "انتقلت المعرفة عبر العربية والسريانية والفارسية واليونانية وغيرها، وشارك في الترجمة والتأليف علماء من خلفيات دينية واجتماعية متعددة. لذلك لا يعرض التطبيق حركة الترجمة باعتبارها عملاً لمؤسسة واحدة أو جماعة واحدة.",
                            "Knowledge circulated through Arabic, Syriac, Persian, Greek, and other languages, with scholars from varied religious and social backgrounds participating in translation and authorship. The app therefore avoids presenting translation as the work of one institution or one community.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "book_culture",
                    title = HistoryText("ثقافة الكتاب", "Book culture"),
                    paragraphs = listOf(
                        HistoryText(
                            "ساعد انتشار الورق والنسخ والتجارة بالكتب ورعاية المكتبات على تكوين بيئات للقراءة والنسخ والتعليق. واختلف حجم هذه البيئات وتنظيمها من مدينة إلى أخرى ومن عصر إلى آخر.",
                            "The spread of paper, copying, book commerce, and library patronage supported environments of reading, reproduction, and commentary. Their scale and organization varied significantly by city and period.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_science", "met_chronology"),
            relatedTopicIds = listOf("education_libraries", "mathematics", "astronomy"),
        ),
        CivilizationTopic(
            id = "mathematics",
            category = CivilizationCategory.KnowledgeAndSciences,
            title = HistoryText("الرياضيات والحساب", "Mathematics and calculation"),
            summary = HistoryText(
                "تطورت تقاليد الحساب والجبر والهندسة ضمن تفاعل بين حاجات عملية وتراثات علمية سابقة ومؤلفات جديدة.",
                "Traditions of calculation, algebra, and geometry developed through interaction between practical needs, earlier scholarly traditions, and new writing.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "algebra_calculation",
                    title = HistoryText("الجبر والحساب", "Algebra and calculation"),
                    paragraphs = listOf(
                        HistoryText(
                            "ارتبط اسم الخوارزمي بتاريخ مبكر للجبر والحساب باللغة العربية، لكن تطور الرياضيات كان تراكمياً وشارك فيه علماء كثيرون قبلَه وبعده في مناطق متعددة. يعرض التطبيق هذا التطور كسلسلة من الإضافات لا كاختراع منفرد.",
                            "Al-Khwarizmi is strongly associated with early Arabic works on algebra and calculation, but mathematical development was cumulative and involved many scholars before and after him across several regions. The app presents this as a chain of contributions rather than a single invention.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "applications",
                    title = HistoryText("تطبيقات عملية", "Practical applications"),
                    paragraphs = listOf(
                        HistoryText(
                            "استخدمت الرياضيات في التجارة والمواريث والمساحة والفلك وضبط الزمن والعمارة وغيرها. كما دفعت المسائل النظرية إلى تطوير طرق جديدة في البرهان والحساب.",
                            "Mathematics was used in commerce, inheritance calculation, surveying, astronomy, timekeeping, architecture, and other fields. Theoretical problems also encouraged new methods of proof and calculation.",
                        ),
                    ),
                ),
            ),
            personIds = listOf("al_khwarizmi"),
            sourceIds = listOf("met_science"),
            relatedTopicIds = listOf("astronomy", "architecture"),
        ),
        CivilizationTopic(
            id = "astronomy",
            category = CivilizationCategory.KnowledgeAndSciences,
            title = HistoryText("الفلك وضبط الزمن", "Astronomy and timekeeping"),
            summary = HistoryText(
                "جمع علم الفلك بين الرصد والحساب وصناعة الأدوات وتطبيقات التقويم والمواقيت والاتجاهات.",
                "Astronomy combined observation, calculation, instrument making, and applications involving calendars, timekeeping, and direction.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "observation",
                    title = HistoryText("الرصد والجداول", "Observation and tables"),
                    paragraphs = listOf(
                        HistoryText(
                            "أنتج علماء في مدن ومراصد مختلفة جداول وحسابات فلكية وصححوا نماذج موروثة وناقشوا دقة الرصد. ولم تكن الممارسة الفلكية موحدة؛ فقد اختلفت المدارس والأدوات وطرق الحساب عبر الزمن.",
                            "Scholars working in different cities and observatories produced astronomical tables and calculations, revised inherited models, and debated observational accuracy. Astronomical practice was not uniform; schools, instruments, and computational methods changed over time.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "instruments_time",
                    title = HistoryText("الأدوات والمواقيت", "Instruments and timekeeping"),
                    paragraphs = listOf(
                        HistoryText(
                            "استخدمت أدوات مثل الأسطرلاب وأجهزة الرصد في التعليم والقياس، وربطت بعض التطبيقات بين الفلك وتحديد الزمن والاتجاهات. ويجب التمييز بين المعرفة الفلكية النظرية وبين الاستخدامات اليومية المحلية.",
                            "Instruments such as astrolabes and observational devices were used in teaching and measurement, while some applications connected astronomy to determining time and direction. Theoretical astronomy and local everyday practice should be distinguished.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_science"),
            relatedTopicIds = listOf("mathematics", "translation_books"),
        ),
        CivilizationTopic(
            id = "medicine_bimaristans",
            category = CivilizationCategory.KnowledgeAndSciences,
            title = HistoryText("الطب والبيمارستانات", "Medicine and bimaristans"),
            summary = HistoryText(
                "تطور الطب من خلال التأليف والتعليم والممارسة والمؤسسات العلاجية في مدن متعددة، مع اختلاف كبير بين الأزمنة والأماكن.",
                "Medicine developed through writing, teaching, practice, and care institutions in many cities, with major variation across periods and places.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "medical_writing",
                    title = HistoryText("التأليف الطبي", "Medical writing"),
                    paragraphs = listOf(
                        HistoryText(
                            "جمع أطباء مثل الرازي وابن سينا بين تراثات طبية سابقة والملاحظة والتصنيف والتدريس، ثم أصبحت مؤلفاتهم جزءاً من تقاليد تعليمية واسعة داخل العالم الإسلامي وخارجه.",
                            "Physicians such as al-Razi and Ibn Sina combined earlier medical traditions with observation, classification, and teaching, and their works later became part of broad educational traditions within and beyond the Islamic world.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "care_institutions",
                    title = HistoryText("مؤسسات الرعاية", "Institutions of care"),
                    paragraphs = listOf(
                        HistoryText(
                            "ظهرت بيمارستانات في عدد من المدن بوصفها مؤسسات للعلاج، وفي بعض البيئات ارتبطت بالتدريب أو الصيدلة أو الأوقاف. لا يفترض التطبيق نموذجاً موحداً لكل بيمارستان، لأن التنظيم والتمويل والخدمات اختلفت تاريخياً.",
                            "Bimaristans appeared in a number of cities as institutions of care, and in some settings were connected to training, pharmacy, or endowments. The app does not assume one universal hospital model because organization, funding, and services varied historically.",
                        ),
                    ),
                ),
            ),
            personIds = listOf("al_razi", "ibn_sina"),
            sourceIds = listOf("met_science"),
            relatedTopicIds = listOf("waqf", "education_libraries"),
        ),
        CivilizationTopic(
            id = "optics",
            category = CivilizationCategory.KnowledgeAndSciences,
            title = HistoryText("البصريات ودراسة الضوء", "Optics and the study of light"),
            summary = HistoryText(
                "مثلت دراسة الضوء والرؤية مجالاً تداخلت فيه الرياضيات والملاحظة والنقاش الفلسفي والطبي.",
                "The study of light and vision brought together mathematics, observation, and philosophical and medical debate.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "vision_light",
                    title = HistoryText("الرؤية والضوء", "Vision and light"),
                    paragraphs = listOf(
                        HistoryText(
                            "ناقش علماء البصريات كيفية حدوث الرؤية وانتقال الضوء وخصائص الانعكاس والانكسار. ارتبط ابن الهيثم بصورة خاصة بتقاليد البحث في هذا المجال، مع اعتماد أعماله على نقاشات سابقة وتأثيرها في نقاشات لاحقة.",
                            "Scholars of optics debated how vision occurs, how light travels, and how reflection and refraction behave. Ibn al-Haytham is especially associated with this field, while his work drew on earlier debates and influenced later ones.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "observation_geometry",
                    title = HistoryText("الملاحظة والهندسة", "Observation and geometry"),
                    paragraphs = listOf(
                        HistoryText(
                            "جمع البحث البصري بين بناء مسائل هندسية وملاحظة الظواهر وتجارب توضيحية. ويعرض التطبيق هذه الممارسات دون إسقاط مفهوم المختبر الحديث عليها بصورة غير تاريخية.",
                            "Optical inquiry combined geometric problem-solving, observation of phenomena, and demonstrative experiments. The app presents these practices without projecting the modern laboratory model backward onto them.",
                        ),
                    ),
                ),
            ),
            personIds = listOf("ibn_al_haytham"),
            sourceIds = listOf("met_science"),
            relatedTopicIds = listOf("mathematics"),
        ),
        CivilizationTopic(
            id = "education_libraries",
            category = CivilizationCategory.Institutions,
            title = HistoryText("التعليم والمدارس والمكتبات", "Education, madrasas, and libraries"),
            summary = HistoryText(
                "تشكل التعليم عبر حلقات المساجد والمجالس والمدارس والمكتبات وبيوت العلماء، ولم يكن محصوراً في مؤسسة واحدة.",
                "Education took shape through mosque circles, scholarly gatherings, madrasas, libraries, and scholars’ households rather than a single institutional form.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "learning_settings",
                    title = HistoryText("بيئات التعلم", "Settings of learning"),
                    paragraphs = listOf(
                        HistoryText(
                            "تلقى الطلاب العلم في المساجد والبيوت والمدارس ومجالس العلماء، وكانت الإجازة والعلاقة المباشرة بالشيخ من عناصر نقل المعرفة في مجالات عديدة. اختلفت هذه الترتيبات حسب التخصص والمدينة والعصر.",
                            "Students learned in mosques, homes, madrasas, and scholarly gatherings, while authorization and direct teacher-student relationships played roles in transmitting knowledge across many fields. Arrangements varied by subject, city, and period.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "libraries",
                    title = HistoryText("المكتبات والنسخ", "Libraries and copying"),
                    paragraphs = listOf(
                        HistoryText(
                            "ضمت بعض المؤسسات والمجموعات الخاصة خزائن كتب، وأسهم النسخ والوقف والشراء والتجارة في تداول المؤلفات. ولا تعني شهرة مكتبة معينة أنها كانت النموذج الوحيد أو الأكبر في كل عصر.",
                            "Some institutions and private collections maintained book holdings, while copying, endowment, purchase, and trade helped circulate texts. The fame of a particular library does not mean it was the only or largest model in every period.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("britannica_madrasah", "met_chronology"),
            relatedTopicIds = listOf("translation_books", "waqf"),
        ),
        CivilizationTopic(
            id = "waqf",
            category = CivilizationCategory.Institutions,
            title = HistoryText("الوقف والخدمات العامة", "Waqf and public services"),
            summary = HistoryText(
                "كان الوقف أحد الأطر القانونية والمالية التي دعمت مؤسسات دينية وتعليمية وخيرية وخدمية في مناطق وفترات متعددة.",
                "Waqf was one legal and financial framework used to support religious, educational, charitable, and service institutions across many regions and periods.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "endowment_structure",
                    title = HistoryText("فكرة الوقف", "The endowment framework"),
                    paragraphs = listOf(
                        HistoryText(
                            "يقوم الوقف تاريخياً على تخصيص أصل أو ريع لغرض مستمر وفق شروط الواقف والأحكام القانونية السائدة. تنوعت الأصول الموقوفة بين العقارات والأراضي والمتاجر وغيرها.",
                            "Historically, waqf involved dedicating an asset or its revenue to an ongoing purpose under the founder’s conditions and prevailing legal rules. Endowed assets could include property, land, shops, and other resources.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "supported_services",
                    title = HistoryText("مجالات الإنفاق", "Supported services"),
                    paragraphs = listOf(
                        HistoryText(
                            "مولت الأوقاف في بعض البيئات مساجد ومدارس ومياه وطرقاً ورعاية للفقراء ومؤسسات علاجية. وكانت الإدارة والرقابة والأثر الاقتصادي تختلف تبعاً للقانون المحلي والسياسة والظروف الاجتماعية.",
                            "In some settings, endowments funded mosques, schools, water provision, roads, poor relief, and care institutions. Administration, oversight, and economic impact varied according to local law, politics, and social conditions.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("britannica_waqf"),
            relatedTopicIds = listOf("education_libraries", "medicine_bimaristans", "cities_urban_life"),
        ),
        CivilizationTopic(
            id = "trade_markets",
            category = CivilizationCategory.SocietyAndEconomy,
            title = HistoryText("التجارة والأسواق وشبكات الطرق", "Trade, markets, and route networks"),
            summary = HistoryText(
                "ربطت شبكات برية وبحرية مدناً وأقاليم متباعدة، وتحركت عبرها السلع والناس واللغات والأفكار.",
                "Overland and maritime networks connected distant cities and regions, carrying goods, people, languages, and ideas.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "routes",
                    title = HistoryText("شبكات متغيرة لا طريق واحد", "Changing networks, not one route"),
                    paragraphs = listOf(
                        HistoryText(
                            "لم تكن طرق الحرير أو التجارة في المحيط الهندي خطاً ثابتاً واحداً؛ بل شبكات تتغير بحسب المواسم والأمن والسياسة والأسواق. ولهذا يستخدم الأطلس في التطبيق خطوطاً تعليمية تقريبية فقط.",
                            "The Silk Roads and Indian Ocean trade were not single fixed lines but networks that changed with seasons, security, politics, and markets. The app therefore uses only schematic educational route lines in its atlas.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "market_life",
                    title = HistoryText("الأسواق والحرف", "Markets and crafts"),
                    paragraphs = listOf(
                        HistoryText(
                            "جمعت المدن بين أسواق محلية وتجارة بعيدة المدى وحرف متخصصة. اختلف تنظيم الأسواق والضرائب والرقابة والعملات من دولة إلى أخرى، فلا توجد بنية اقتصادية واحدة تصلح لوصف جميع المجتمعات الإسلامية.",
                            "Cities combined local markets, long-distance trade, and specialized crafts. Market regulation, taxation, oversight, and currencies differed from state to state, so no single economic structure describes all Muslim societies.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("unesco_silk_roads", "met_chronology"),
            relatedTopicIds = listOf("cities_urban_life", "agriculture_water"),
        ),
        CivilizationTopic(
            id = "agriculture_water",
            category = CivilizationCategory.SocietyAndEconomy,
            title = HistoryText("الزراعة والمياه والبيئة", "Agriculture, water, and environment"),
            summary = HistoryText(
                "اعتمدت المجتمعات على نظم زراعية ومائية محلية تأثرت بالمناخ والتربة والمعرفة التقنية وشبكات التجارة.",
                "Communities relied on local agricultural and water systems shaped by climate, soil, technical knowledge, and trade networks.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "water_systems",
                    title = HistoryText("إدارة المياه", "Water management"),
                    paragraphs = listOf(
                        HistoryText(
                            "استخدمت مناطق مختلفة الآبار والقنوات والسواقي والخزانات وغيرها من التقنيات بحسب البيئة. غالباً ما كانت هذه النظم امتداداً لتقاليد محلية قديمة جرى تطويرها أو صيانتها في عصور إسلامية مختلفة.",
                            "Different regions used wells, channels, waterwheels, reservoirs, and other techniques according to local conditions. Such systems often extended older local traditions that were maintained or adapted during different Islamic periods.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "crops_exchange",
                    title = HistoryText("المحاصيل والتبادل", "Crops and exchange"),
                    paragraphs = listOf(
                        HistoryText(
                            "أسهمت التجارة والهجرة في انتقال بعض النباتات والمحاصيل والمعارف الزراعية بين الأقاليم، لكن سرعة الانتقال وحجمه اختلفا ولا يمكن تفسيرهما بعامل سياسي واحد.",
                            "Trade and migration contributed to the movement of some plants, crops, and agricultural knowledge between regions, but the speed and scale of transfer varied and cannot be explained by one political factor.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("unesco_silk_roads", "met_chronology"),
            relatedTopicIds = listOf("trade_markets", "cities_urban_life"),
        ),
        CivilizationTopic(
            id = "cities_urban_life",
            category = CivilizationCategory.SocietyAndEconomy,
            title = HistoryText("المدن والحياة الحضرية", "Cities and urban life"),
            summary = HistoryText(
                "كانت المدينة ملتقى للإدارة والعبادة والتجارة والحرف والتعليم والسكن، مع اختلاف كبير بين بغداد والقاهرة وقرطبة ودمشق وسمرقند وغيرها.",
                "Cities brought together administration, worship, trade, crafts, education, and residence, with major differences among Baghdad, Cairo, Cordoba, Damascus, Samarkand, and other centers.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "urban_functions",
                    title = HistoryText("وظائف المدينة", "Urban functions"),
                    paragraphs = listOf(
                        HistoryText(
                            "قد تضم المدينة مساجد وأسواقاً ومؤسسات تعليمية وحمامات وورشاً وأحياء سكنية ومرافق للمياه، لكن شكلها لم يكن ثابتاً. كانت الجغرافيا والسياسة والاقتصاد والتقاليد العمرانية المحلية عوامل حاسمة في تطورها.",
                            "A city might contain mosques, markets, educational institutions, baths, workshops, residential quarters, and water facilities, but there was no fixed form. Geography, politics, economy, and local building traditions strongly shaped development.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "people_mobility",
                    title = HistoryText("السكان والحركة", "Population and mobility"),
                    paragraphs = listOf(
                        HistoryText(
                            "استقبلت المراكز الكبرى تجاراً وطلاباً وحرفيين وموظفين وحجاجاً ومهاجرين، ما جعلها أماكن لتبادل اللغات والعادات والمعارف. تغير حجم المدن وتركيبها مراراً بسبب الحرب والوباء والتجارة والتحولات السياسية.",
                            "Major centers received merchants, students, artisans, officials, pilgrims, and migrants, making them sites of linguistic, social, and intellectual exchange. Urban size and composition changed repeatedly through war, epidemic disease, trade, and political transformation.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_chronology", "unesco_silk_roads"),
            relatedTopicIds = listOf("trade_markets", "waqf", "architecture"),
        ),
        CivilizationTopic(
            id = "architecture",
            category = CivilizationCategory.ArtsAndBuiltEnvironment,
            title = HistoryText("العمارة وتشكيل المكان", "Architecture and the shaping of space"),
            summary = HistoryText(
                "تطورت عمارة المساجد والمدارس والقصور والتحصينات والمنشآت العامة عبر تقاليد إقليمية متعددة لا أسلوب إسلامي واحد ثابت.",
                "Mosques, madrasas, palaces, fortifications, and public works developed through many regional traditions rather than one fixed Islamic architectural style.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "regional_traditions",
                    title = HistoryText("تقاليد إقليمية", "Regional traditions"),
                    paragraphs = listOf(
                        HistoryText(
                            "تأثرت المباني بالمواد المتاحة والمناخ والتقنيات المحلية والتقاليد السابقة وشبكات الحرفيين والرعاية السياسية والدينية. لذلك تختلف عمارة المغرب والأندلس عن مصر والشام والأناضول وإيران والهند رغم وجود عناصر مشتركة أحياناً.",
                            "Buildings were shaped by available materials, climate, local techniques, earlier traditions, artisan networks, and political or religious patronage. Architecture in the Maghreb and al-Andalus therefore differs from Egypt, the Levant, Anatolia, Iran, and India despite some shared elements.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "public_space",
                    title = HistoryText("المبنى والمدينة", "Buildings and the city"),
                    paragraphs = listOf(
                        HistoryText(
                            "لا تُقرأ العمارة بوصفها زخرفة فقط؛ فالمساجد والمدارس والأسواق والجسور والحمامات والمنشآت المائية أدت وظائف اجتماعية واقتصادية ودينية وإدارية داخل المدينة.",
                            "Architecture is not only decoration: mosques, madrasas, markets, bridges, baths, and water structures performed social, economic, religious, and administrative functions within urban life.",
                        ),
                    ),
                ),
            ),
            personIds = listOf("mimar_sinan"),
            sourceIds = listOf("met_islamic_art", "met_chronology"),
            relatedTopicIds = listOf("cities_urban_life", "calligraphy_decorative_arts"),
        ),
        CivilizationTopic(
            id = "calligraphy_decorative_arts",
            category = CivilizationCategory.ArtsAndBuiltEnvironment,
            title = HistoryText("الخط والفنون الزخرفية", "Calligraphy and decorative arts"),
            summary = HistoryText(
                "ظهر الخط والزخرفة في المخطوطات والعمارة والخزف والمنسوجات والمعادن وغيرها، وتنوعت الأساليب بحسب الإقليم والوظيفة والعصر.",
                "Calligraphy and ornament appeared in manuscripts, architecture, ceramics, textiles, metalwork, and other media, with styles varying by region, function, and period.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "calligraphy",
                    title = HistoryText("الخط", "Calligraphy"),
                    paragraphs = listOf(
                        HistoryText(
                            "ارتبط الخط بنسخ القرآن والكتب والمراسلات والنقوش المعمارية، وتطورت أنماط متعددة على أيدي خطاطين ومدارس محلية. لا يمثل نوع خط واحد جميع العصور أو الأقاليم.",
                            "Calligraphy was used for Qur’an manuscripts, books, correspondence, and architectural inscriptions, while multiple scripts developed through calligraphers and local schools. No single script represents every region or period.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "ornament_materials",
                    title = HistoryText("الزخرفة والمواد", "Ornament and materials"),
                    paragraphs = listOf(
                        HistoryText(
                            "استخدم الفنانون والحرفيون أشكالاً هندسية ونباتية وكتابية وتمثيلية بدرجات مختلفة بحسب الوسط والسياق. وتكشف المواد وتقنيات الصنع عن شبكات تجارة وحرف وتبادل فني واسعة.",
                            "Artists and artisans used geometric, vegetal, epigraphic, and figural forms to varying degrees depending on medium and context. Materials and production techniques reveal broad networks of trade, craft, and artistic exchange.",
                        ),
                    ),
                ),
            ),
            sourceIds = listOf("met_islamic_art"),
            relatedTopicIds = listOf("architecture", "translation_books"),
        ),
    )

    fun byId(id: String): CivilizationTopic? = topics.firstOrNull { it.id == id }

    fun byCategory(category: CivilizationCategory?): List<CivilizationTopic> =
        if (category == null) topics else topics.filter { it.category == category }
}
