package org.muslim.app.feature.reference.domain

/**
 * Long-form profiles for the people and atlas places already exposed by the history feature.
 *
 * Profiles reuse stable IDs from [IslamicHistoryContent] so events, civilization topics,
 * atlas markers, and future search results can deep-link without duplicating entities.
 */
object IslamicHistoryProfiles {
    val people = listOf(
        HistoryPersonProfile(
            personId = "khalid_ibn_al_walid",
            overview = HistoryText(
                "قائد من الجيل الإسلامي الأول ارتبطت سيرته بتحولات عسكرية وسياسية في الجزيرة العربية والشام والعراق خلال العقود الأولى من القرن السابع.",
                "An early Muslim commander whose career is associated with military and political transformations in Arabia, the Levant, and Iraq during the early seventh century.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "career_context",
                    title = HistoryText("السياق التاريخي", "Historical context"),
                    paragraphs = listOf(
                        HistoryText(
                            "تظهر أخبار خالد بن الوليد في مصادر السيرة والفتوح والتاريخ ضمن فترة تغير سريع بعد الهجرة ثم بعد وفاة النبي ﷺ. وتختلف الروايات في بعض التفاصيل، لذلك تُعرض سيرته هنا بوصفها مدخلاً لفهم المرحلة لا حكماً نهائياً على كل خبر.",
                            "Accounts of Khalid ibn al-Walid appear in Sira, conquest, and historical literature during a period of rapid change after the Hijra and after the Prophet’s death. Sources differ on some details, so the profile is presented as an orientation to the period rather than a final judgment on every report.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "legacy",
                    title = HistoryText("الحضور في الذاكرة التاريخية", "Historical memory"),
                    paragraphs = listOf(
                        HistoryText(
                            "يرتبط اسمه في الذاكرة الإسلامية بالقيادة العسكرية المبكرة، كما يُستخدم تتبع تحركاته لفهم علاقة الجيوش والمدن والطرق والتحولات السياسية في تلك المرحلة.",
                            "His name is strongly associated in Islamic memory with early military leadership, and tracing his movements helps explain relationships among armies, cities, routes, and political change in the period.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("prophetic_era", "rashidun"),
            eventIds = listOf("rashidun_expansion_630s"),
            sourceIds = listOf("internal_sira", "met_chronology"),
        ),
        HistoryPersonProfile(
            personId = "tariq_ibn_ziyad",
            overview = HistoryText(
                "قائد ارتبط في المصادر ببدايات دخول القوات الإسلامية إلى شبه الجزيرة الإيبيرية سنة 711م وبالمراحل الأولى لتشكل الأندلس الإسلامية.",
                "A commander associated in historical sources with the 711 CE crossing into the Iberian Peninsula and the earliest formation of Muslim al-Andalus.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "711_context",
                    title = HistoryText("سياق سنة 711م", "The context of 711"),
                    paragraphs = listOf(
                        HistoryText(
                            "وقع العبور ضمن تحولات أوسع في غرب المتوسط وشمال إفريقيا وشبه الجزيرة الإيبيرية، وتبعته سنوات من الحملات والتفاوض وإعادة تنظيم السلطة المحلية.",
                            "The crossing formed part of wider changes in the western Mediterranean, North Africa, and Iberia and was followed by years of campaigns, negotiation, and reorganization of local authority.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "sources_memory",
                    title = HistoryText("المصادر والذاكرة", "Sources and memory"),
                    paragraphs = listOf(
                        HistoryText(
                            "تعود كثير من الروايات التفصيلية عن طارق إلى مصادر متأخرة زمنياً عن الحدث، ولذلك يميز التطبيق بين الحقائق الزمنية العامة والروايات الأدبية أو المختلف في تفاصيلها.",
                            "Many detailed narratives about Tariq survive in sources written later than the events, so the app distinguishes broad chronological facts from literary or disputed details.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("umayyad", "regional_civilizations"),
            stateIds = listOf("umayyad_caliphate"),
            placeIds = listOf("cordoba"),
            eventIds = listOf("al_andalus_711"),
            sourceIds = listOf("met_chronology", "met_major_dynasties"),
        ),
        HistoryPersonProfile(
            personId = "al_khwarizmi",
            overview = HistoryText(
                "عالم ارتبط ببيئة بغداد العباسية وبأعمال في الحساب والجبر والجغرافيا والجداول الفلكية ضمن شبكة أوسع من الترجمة والتأليف.",
                "A scholar associated with Abbasid Baghdad and with works in arithmetic, algebra, geography, and astronomical tables within a wider network of translation and authorship.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "works",
                    title = HistoryText("مجالات العمل", "Fields of work"),
                    paragraphs = listOf(
                        HistoryText(
                            "تُنسب إلى الخوارزمي مؤلفات مؤثرة في الحساب والجبر، كما ارتبط اسمه بأعمال فلكية وجغرافية. جاءت هذه الأعمال ضمن ثقافة علمية متعددة المصادر واللغات.",
                            "Al-Khwarizmi is associated with influential works on arithmetic and algebra and with astronomical and geographical writing. These works emerged within a scholarly culture drawing on multiple languages and traditions.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "transmission",
                    title = HistoryText("انتقال المؤلفات", "Transmission of works"),
                    paragraphs = listOf(
                        HistoryText(
                            "ساعد نسخ الكتب وترجمتها ودراستها في انتقال بعض أعماله إلى بيئات تعليمية لاحقة، وأصبح اسمه مرتبطاً بتاريخ الجبر والحساب في دراسات كثيرة.",
                            "Copying, translation, and study helped transmit some of his works into later educational settings, and his name became closely associated with the history of algebra and calculation.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("abbasid"),
            stateIds = listOf("abbasid_caliphate"),
            placeIds = listOf("baghdad"),
            eventIds = listOf("abbasid_translation_scholarship"),
            relatedTopicIds = listOf("mathematics", "astronomy", "translation_books"),
            sourceIds = listOf("met_science", "met_chronology"),
        ),
        HistoryPersonProfile(
            personId = "al_razi",
            overview = HistoryText(
                "طبيب ومؤلف ارتبط اسمه بتاريخ الطب باللغة العربية وبالممارسة الطبية والتأليف في مدن من بينها الري وبغداد.",
                "A physician and author associated with the history of Arabic-language medicine and with medical practice and writing in cities including Rayy and Baghdad.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "medicine",
                    title = HistoryText("الطب والتأليف", "Medicine and writing"),
                    paragraphs = listOf(
                        HistoryText(
                            "تُظهر مؤلفاته اهتماماً بالتشخيص والعلاج وتنظيم المعرفة الطبية، وتعكس تراكماً يجمع بين تراثات سابقة والممارسة والخبرة السريرية.",
                            "His writings show sustained interest in diagnosis, treatment, and the organization of medical knowledge and reflect a cumulative tradition combining earlier learning with practice and clinical experience.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "institutions",
                    title = HistoryText("الممارسة والمؤسسات", "Practice and institutions"),
                    paragraphs = listOf(
                        HistoryText(
                            "تساعد دراسة سيرته على فهم علاقة الطبيب بالمدينة والبيمارستان والكتاب، مع ضرورة تجنب إسقاط تنظيم المستشفى الحديث على مؤسسات العصور الوسطى.",
                            "His career helps illuminate relationships among physician, city, bimaristan, and book culture while avoiding the projection of the modern hospital model onto medieval institutions.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("abbasid", "regional_civilizations"),
            placeIds = listOf("baghdad"),
            relatedTopicIds = listOf("medicine_bimaristans", "education_libraries"),
            sourceIds = listOf("met_science"),
        ),
        HistoryPersonProfile(
            personId = "ibn_al_haytham",
            overview = HistoryText(
                "باحث في البصريات والرياضيات ارتبطت أهم مراحل عمله بمصر، واشتهر بمناقشة الضوء والرؤية والاستدلال الهندسي والملاحظة.",
                "A scholar of optics and mathematics whose major career is associated with Egypt and who is known for work on light, vision, geometrical reasoning, and observation.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "optics",
                    title = HistoryText("البصريات", "Optics"),
                    paragraphs = listOf(
                        HistoryText(
                            "ناقش ابن الهيثم نظريات الرؤية وخصائص الضوء والانعكاس والانكسار ضمن تقليد علمي سابق عليه، وطور معالجة مؤثرة في تاريخ البصريات.",
                            "Ibn al-Haytham examined theories of vision and the behavior of light, reflection, and refraction within an older scholarly tradition and developed an influential treatment in the history of optics.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "method",
                    title = HistoryText("الملاحظة والاستدلال", "Observation and reasoning"),
                    paragraphs = listOf(
                        HistoryText(
                            "تجمع أعماله بين الهندسة والمناقشة النظرية والاستعانة بالملاحظة والتجارب التوضيحية، ويُفضّل وصف ذلك في سياقه التاريخي بدلاً من مطابقته مباشرة بمنهج علمي حديث.",
                            "His work combines geometry, theoretical argument, observation, and demonstrative experiments and is best described in its historical context rather than equated directly with a modern scientific method.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("abbasid", "regional_civilizations"),
            placeIds = listOf("cairo"),
            relatedTopicIds = listOf("optics", "mathematics"),
            sourceIds = listOf("met_science"),
        ),
        HistoryPersonProfile(
            personId = "ibn_sina",
            overview = HistoryText(
                "طبيب وفيلسوف من آسيا الوسطى وإيران ألّف أعمالاً واسعة الانتشار في الطب والفلسفة والمنطق والعلوم الطبيعية.",
                "A physician and philosopher from Central Asia and Iran whose works in medicine, philosophy, logic, and natural inquiry circulated widely.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "canon",
                    title = HistoryText("الطب", "Medicine"),
                    paragraphs = listOf(
                        HistoryText(
                            "أصبح القانون في الطب من أشهر مؤلفاته، وجمع مادة طبية منظمة أثرت في تقاليد تعليمية متعددة عبر قرون.",
                            "The Canon of Medicine became one of his best-known works, organizing medical material that influenced multiple teaching traditions over centuries.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "philosophy",
                    title = HistoryText("الفلسفة وانتقال المعرفة", "Philosophy and knowledge transmission"),
                    paragraphs = listOf(
                        HistoryText(
                            "تداخل مشروعه الفلسفي والطبي مع شبكات المخطوطات والتعليم والترجمة، ما يوضح كيف انتقلت الأفكار بين مدن ولغات ومؤسسات مختلفة.",
                            "His philosophical and medical project intersected with manuscript, educational, and translation networks, illustrating how ideas moved among cities, languages, and institutions.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("abbasid", "regional_civilizations"),
            placeIds = listOf("samarkand"),
            relatedTopicIds = listOf("medicine_bimaristans", "translation_books", "education_libraries"),
            sourceIds = listOf("met_science", "unesco_silk_roads"),
        ),
        HistoryPersonProfile(
            personId = "ibn_khaldun",
            overview = HistoryText(
                "مؤرخ وقاضٍ ورجل دولة تنقل بين المغرب والأندلس ومصر، واشتهر بالمقدمة التي ناقش فيها العمران والدولة والمجتمع وطرائق قراءة الأخبار.",
                "A historian, judge, and statesman who moved through the Maghreb, al-Andalus, and Egypt and is known for the Muqaddimah on society, states, urban life, and the evaluation of reports.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "career",
                    title = HistoryText("حياة بين المراكز السياسية", "A career across political centers"),
                    paragraphs = listOf(
                        HistoryText(
                            "عاش ابن خلدون في بيئة سياسية متغيرة وخدم في وظائف إدارية وقضائية متعددة، ثم استقر في مصر في جزء من حياته المتأخرة.",
                            "Ibn Khaldun lived in a changing political environment, held several administrative and judicial positions, and spent part of his later life in Egypt.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "muqaddimah",
                    title = HistoryText("المقدمة وقراءة التاريخ", "The Muqaddimah and reading history"),
                    paragraphs = listOf(
                        HistoryText(
                            "ناقشت المقدمة أسباب قيام الدول وتحولها، والعمران والاقتصاد والتعليم، كما شددت على نقد الأخبار ومقارنتها بأحوال المجتمع والإمكان التاريخي.",
                            "The Muqaddimah discusses state formation and change, urban life, economy, and education and emphasizes evaluating reports against social conditions and historical plausibility.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("regional_civilizations"),
            stateIds = listOf("mamluks"),
            placeIds = listOf("cairo"),
            relatedTopicIds = listOf("cities_urban_life", "education_libraries"),
            sourceIds = listOf("met_chronology"),
        ),
        HistoryPersonProfile(
            personId = "mimar_sinan",
            overview = HistoryText(
                "المعمار العثماني الأشهر في القرن السادس عشر، ارتبط بمشروعات مساجد ومنشآت عامة في إسطنبول ومدن أخرى ضمن جهاز الدولة العثمانية.",
                "The best-known Ottoman architect of the sixteenth century, associated with mosques and public works in Istanbul and other cities within the Ottoman state.",
            ),
            sections = listOf(
                HistoryArticleSection(
                    id = "imperial_architecture",
                    title = HistoryText("العمارة الإمبراطورية", "Imperial architecture"),
                    paragraphs = listOf(
                        HistoryText(
                            "عمل سنان ضمن مؤسسة معمارية مرتبطة بالدولة، وصمم وأشرف على مشروعات دينية ومدنية ومائية عديدة، مع اعتماد واسع على الحرفيين وفرق البناء.",
                            "Sinan worked within an architectural institution tied to the Ottoman state and designed or supervised many religious, civic, and water-related projects that depended on large teams of artisans and builders.",
                        ),
                    ),
                ),
                HistoryArticleSection(
                    id = "urban_legacy",
                    title = HistoryText("الأثر الحضري", "Urban legacy"),
                    paragraphs = listOf(
                        HistoryText(
                            "تكشف أعماله علاقة المسجد بالمجمعات الخدمية والمياه والطرق والمشهد الحضري، لذلك تُقرأ سيرته ضمن تاريخ المدينة والمؤسسات لا ضمن تاريخ الفن وحده.",
                            "His works reveal relationships among mosques, service complexes, water, roads, and urban form, so his career belongs to the history of cities and institutions as well as art.",
                        ),
                    ),
                ),
            ),
            eraIds = listOf("ottoman_modern"),
            stateIds = listOf("ottomans"),
            placeIds = listOf("istanbul"),
            relatedTopicIds = listOf("architecture", "cities_urban_life", "waqf"),
            sourceIds = listOf("met_islamic_art", "met_chronology"),
        ),
    )

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

    fun personById(id: String): HistoryPersonProfile? = people.firstOrNull { it.personId == id }

    fun placeById(id: String): HistoricalPlaceProfile? = places.firstOrNull { it.placeId == id }

    fun atlasPlaceById(id: String): HistoricalPlace? =
        IslamicHistoryContent.atlasLayers
            .asSequence()
            .flatMap { it.places.asSequence() }
            .firstOrNull { it.id == id }
}
