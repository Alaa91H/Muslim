package org.muslim.app.feature.reference.domain

/** Long-form profiles for historical figures exposed by the history feature. */
object IslamicHistoryPeopleProfiles {
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

    fun byId(id: String): HistoryPersonProfile? = people.firstOrNull { it.personId == id }
}
