package org.muslim.app.feature.reference.domain

/** Bilingual, source-led educational content for the Islamic history destination. */
data class HistoryText(
    val arabic: String,
    val english: String,
) {
    fun resolve(language: HistoryLanguage): String = if (language == HistoryLanguage.Arabic) arabic else english
}

enum class HistoryLanguage { Arabic, English }

data class HistoryEra(
    val id: String,
    val startCe: Int,
    val endCe: Int?,
    val title: HistoryText,
    val summary: HistoryText,
    val highlights: List<HistoryText>,
)

data class HistoryCoordinate(
    val latitude: Double,
    val longitude: Double,
)

data class HistoricalPlace(
    val id: String,
    val title: HistoryText,
    val coordinate: HistoryCoordinate,
    val note: HistoryText,
)

data class HistoricalRoute(
    val id: String,
    val title: HistoryText,
    val note: HistoryText,
    val coordinates: List<HistoryCoordinate>,
)

data class HistoricalMapLayer(
    val id: String,
    val eraId: String,
    val title: HistoryText,
    val summary: HistoryText,
    val schematicArea: List<HistoryCoordinate>,
    val routes: List<HistoricalRoute>,
    val places: List<HistoricalPlace>,
    val initialCenter: HistoryCoordinate,
    val zoom: Double,
)

data class HistoryPerson(
    val id: String,
    val years: String,
    val field: HistoryText,
    val name: HistoryText,
    val summary: HistoryText,
    val contribution: HistoryText,
)

/**
 * A compact, curated orientation rather than an exhaustive history. Dates are
 * Common Era dates. Map areas are deliberately approximate teaching overlays;
 * political boundaries and control changed continually across all periods.
 */
object IslamicHistoryContent {
    val timeline = listOf(
        HistoryEra(
            id = "prophetic_era",
            startCe = 610,
            endCe = 632,
            title = HistoryText("البعثة والسيرة النبوية", "Prophetic mission and sira"),
            summary = HistoryText(
                "مرحلة الدعوة في مكة ثم بناء المجتمع في المدينة؛ تُقرأ تفاصيل السيرة في قسم المراجع داخل التطبيق.",
                "The call in Mecca and the building of a community in Medina; detailed sira remains available in the app’s Reference section.",
            ),
            highlights = listOf(
                HistoryText("610م: بداية الوحي وفق الموروث الإسلامي.", "610 CE: beginning of revelation in Islamic tradition."),
                HistoryText("622م: الهجرة إلى المدينة وبداية التأريخ الهجري.", "622 CE: migration to Medina and the beginning of the Hijri era."),
                HistoryText("632م: وفاة النبي محمد ﷺ.", "632 CE: death of Prophet Muhammad."),
            ),
        ),
        HistoryEra(
            id = "rashidun",
            startCe = 632,
            endCe = 661,
            title = HistoryText("الخلافة الراشدة", "Rashidun caliphate"),
            summary = HistoryText(
                "مرحلة مبكرة من الحكم بعد وفاة النبي ﷺ، ارتبطت بتحولات سياسية وإدارية واتساع جغرافي سريع.",
                "An early period of rule after the Prophet’s death, associated with political and administrative change and rapid geographical expansion.",
            ),
            highlights = listOf(
                HistoryText("المدينة ثم الكوفة برزتا كمركزين سياسيين في مراحل مختلفة.", "Medina and later Kufa emerged as political centres at different stages."),
                HistoryText("تسجل المصادر التاريخية معارك وتحولات في الشام والعراق ومصر وإيران.", "Historical sources record battles and changes across the Levant, Iraq, Egypt, and Iran."),
            ),
        ),
        HistoryEra(
            id = "umayyad",
            startCe = 661,
            endCe = 750,
            title = HistoryText("الدولة الأموية", "Umayyad caliphate"),
            summary = HistoryText(
                "اتخذت دمشق مركزاً للحكم، وامتدت شبكات الدولة عبر مناطق واسعة من غرب المتوسط إلى آسيا الوسطى.",
                "Centred in Damascus, its state networks reached across wide regions from the western Mediterranean to Central Asia.",
            ),
            highlights = listOf(
                HistoryText("661م: انتقال مركز الحكم إلى دمشق.", "661 CE: the centre of rule moved to Damascus."),
                HistoryText("711م: بداية الفتح الإسلامي في الأندلس ضمن تحولات غرب المتوسط.", "711 CE: the beginning of Muslim rule in al-Andalus amid wider western Mediterranean changes."),
            ),
        ),
        HistoryEra(
            id = "abbasid",
            startCe = 750,
            endCe = 1258,
            title = HistoryText("الدولة العباسية", "Abbasid caliphate"),
            summary = HistoryText(
                "برزت بغداد مركزاً سياسياً وثقافياً في حقب متعددة، مع قيام سلالات ومراكز علمية إقليمية متزامنة.",
                "Baghdad became a political and cultural centre in several periods, alongside concurrent regional dynasties and centres of learning.",
            ),
            highlights = listOf(
                HistoryText("750م: قيام الدولة العباسية.", "750 CE: beginning of Abbasid rule."),
                HistoryText("762م: تأسيس بغداد، التي أصبحت من أهم مدن العلم والتجارة في عصور متعددة.", "762 CE: foundation of Baghdad, which became an important city of learning and trade in several eras."),
                HistoryText("1258م: سقوط بغداد في الغزو المغولي؛ لا يعني ذلك توقف المجتمعات والدول الإسلامية الأخرى.", "1258 CE: Baghdad fell in the Mongol invasion; this did not end other Muslim societies and states."),
            ),
        ),
        HistoryEra(
            id = "regional_civilizations",
            startCe = 756,
            endCe = 1492,
            title = HistoryText("حضارات ومراكز إقليمية", "Regional civilizations and centres"),
            summary = HistoryText(
                "شهدت الأندلس والمغرب ومصر والشام وإيران وآسيا الوسطى والهند وجنوب شرق آسيا تجارب سياسية وثقافية متعددة ومتداخلة.",
                "Al-Andalus, the Maghreb, Egypt, the Levant, Iran, Central Asia, India, and Southeast Asia experienced diverse and overlapping political and cultural developments.",
            ),
            highlights = listOf(
                HistoryText("قرطبة والقاهرة وسمرقند وبخارى ومدن أخرى كانت عقداً للعلم والتجارة والفنون في فترات مختلفة.", "Cordoba, Cairo, Samarkand, Bukhara, and other cities were hubs of learning, trade, and arts in different periods."),
                HistoryText("توضح طرق التجارة انتقال السلع والأفكار واللغات، لا مساراً واحداً ثابتاً.", "Trade routes show movement of goods, ideas, and languages, not one fixed itinerary."),
            ),
        ),
        HistoryEra(
            id = "ottoman_modern",
            startCe = 1299,
            endCe = null,
            title = HistoryText("العثمانيون والعصر الحديث", "Ottomans and the modern era"),
            summary = HistoryText(
                "تزامنت الدولة العثمانية مع دول ومجتمعات إسلامية عديدة، ثم تعددت تجارب الدول المسلمة والمجتمعات المسلمة في العصر الحديث.",
                "The Ottoman state coexisted with many other Muslim states and communities; modern Muslim societies have since followed diverse national and transnational paths.",
            ),
            highlights = listOf(
                HistoryText("1453م: فتح القسطنطينية في العهد العثماني.", "1453 CE: Ottoman conquest of Constantinople."),
                HistoryText("1924م: إلغاء مؤسسة الخلافة العثمانية في تركيا.", "1924 CE: abolition of the Ottoman caliphate institution in Türkiye."),
            ),
        ),
    )

    val atlasLayers = listOf(
        HistoricalMapLayer(
            id = "early_hijaz",
            eraId = "prophetic_era",
            title = HistoryText("الحجاز ومحطات السيرة", "Hijaz and selected sira locations"),
            summary = HistoryText(
                "علامات جغرافية تعليمية مختارة من السيرة؛ راجع قسم السيرة للنص والسياق الكامل.",
                "Selected educational geographic markers from the sira; see the Sira section for full text and context.",
            ),
            schematicArea = listOf(
                HistoryCoordinate(30.0, 34.5),
                HistoryCoordinate(30.0, 46.5),
                HistoryCoordinate(16.0, 46.5),
                HistoryCoordinate(16.0, 34.5),
            ),
            routes = listOf(
                HistoricalRoute(
                    id = "hijra_orientation",
                    title = HistoryText("اتجاه الهجرة: مكة إلى المدينة", "Migration orientation: Mecca to Medina"),
                    note = HistoryText("خط توجيهي مبسط، وليس إعادة بناء لمسار الرحلة التفصيلي.", "A simplified orientation line, not a reconstruction of the detailed journey."),
                    coordinates = listOf(
                        HistoryCoordinate(21.3891, 39.8579),
                        HistoryCoordinate(24.5247, 39.5692),
                    ),
                ),
            ),
            places = listOf(
                HistoricalPlace("mecca", HistoryText("مكة", "Mecca"), HistoryCoordinate(21.3891, 39.8579), HistoryText("مدينة محورية في السيرة والحج.", "A pivotal city in the sira and Hajj.")),
                HistoricalPlace("medina", HistoryText("المدينة", "Medina"), HistoryCoordinate(24.5247, 39.5692), HistoryText("مركز المجتمع المدني في السيرة.", "Centre of the Medinan community in the sira.")),
                HistoricalPlace("badr", HistoryText("بدر", "Badr"), HistoryCoordinate(23.7833, 38.7833), HistoryText("موضع معركة بدر في السرد التاريخي الإسلامي.", "Site associated with the Battle of Badr in Islamic historical narratives.")),
                HistoricalPlace("uhud", HistoryText("أحد", "Uhud"), HistoryCoordinate(24.5050, 39.6167), HistoryText("موضع غزوة أحد قرب المدينة.", "Site associated with the Battle of Uhud near Medina.")),
                HistoricalPlace("tabuk", HistoryText("تبوك", "Tabuk"), HistoryCoordinate(28.3833, 36.5833), HistoryText("محطة معروفة في سياق غزوة تبوك.", "A well-known location in the context of Tabuk.")),
            ),
            initialCenter = HistoryCoordinate(24.0, 40.0),
            zoom = 4.4,
        ),
        HistoricalMapLayer(
            id = "umayyad_extent",
            eraId = "umayyad",
            title = HistoryText("الأمويون: نطاق تقريبي", "Umayyads: schematic reach"),
            summary = HistoryText(
                "منطقة تعليمية تقريبية للنطاق في فترات الذروة؛ ليست حدوداً سياسية دقيقة أو ثابتة.",
                "An approximate teaching area for periods of greatest reach; not precise or fixed political boundaries.",
            ),
            schematicArea = listOf(
                HistoryCoordinate(36.0, -8.5),
                HistoryCoordinate(42.0, 8.0),
                HistoryCoordinate(43.0, 35.0),
                HistoryCoordinate(41.0, 68.0),
                HistoryCoordinate(32.0, 73.0),
                HistoryCoordinate(18.0, 68.0),
                HistoryCoordinate(14.0, 45.0),
                HistoryCoordinate(18.0, 15.0),
                HistoryCoordinate(27.0, -8.5),
            ),
            routes = emptyList(),
            places = listOf(
                HistoricalPlace("damascus", HistoryText("دمشق", "Damascus"), HistoryCoordinate(33.5138, 36.2765), HistoryText("مركز الدولة الأموية.", "Centre of the Umayyad caliphate.")),
                HistoricalPlace("cordoba", HistoryText("قرطبة", "Cordoba"), HistoryCoordinate(37.8882, -4.7794), HistoryText("من أبرز مدن الأندلس لاحقاً.", "One of the major cities of later al-Andalus.")),
                HistoricalPlace("kairouan", HistoryText("القيروان", "Kairouan"), HistoryCoordinate(35.6781, 10.0963), HistoryText("مدينة محورية في تاريخ إفريقية.", "A pivotal city in the history of Ifriqiya.")),
            ),
            initialCenter = HistoryCoordinate(31.0, 30.0),
            zoom = 2.4,
        ),
        HistoricalMapLayer(
            id = "abbasid_networks",
            eraId = "abbasid",
            title = HistoryText("شبكات عباسية وطرق قوافل", "Abbasid networks and caravan routes"),
            summary = HistoryText(
                "مسارات تعليمية مختارة تربط مدناً تجارية وعلمية؛ كانت شبكات القوافل متعددة ومتغيرة وليست خطاً واحداً.",
                "Selected teaching routes connect trading and learning cities; caravan networks were multiple and changing, not a single line.",
            ),
            schematicArea = listOf(
                HistoryCoordinate(38.0, 20.0),
                HistoryCoordinate(42.0, 49.0),
                HistoryCoordinate(39.0, 67.0),
                HistoryCoordinate(30.0, 69.0),
                HistoryCoordinate(23.0, 55.0),
                HistoryCoordinate(25.0, 29.0),
            ),
            routes = listOf(
                HistoricalRoute(
                    id = "silk_roads_orientation",
                    title = HistoryText("طريق بري: بغداد إلى سمرقند", "Overland orientation: Baghdad to Samarkand"),
                    note = HistoryText("تمثيل مبسط لجزء من شبكات طرق الحرير والتجارة عبر آسيا الوسطى.", "A simplified representation of part of Silk Roads and Central Asian trade networks."),
                    coordinates = listOf(
                        HistoryCoordinate(33.3152, 44.3661),
                        HistoryCoordinate(36.2605, 59.6168),
                        HistoryCoordinate(39.6542, 66.9597),
                    ),
                ),
                HistoricalRoute(
                    id = "indian_ocean_orientation",
                    title = HistoryText("طريق بحري: البصرة إلى المحيط الهندي", "Maritime orientation: Basra to the Indian Ocean"),
                    note = HistoryText("خط تقريبي يوضح اتصال موانئ الخليج والمحيط الهندي، لا مسار ملاحي تاريخي دقيق.", "An approximate line showing links between Gulf and Indian Ocean ports, not an exact historical sailing track."),
                    coordinates = listOf(
                        HistoryCoordinate(30.5085, 47.7835),
                        HistoryCoordinate(26.2235, 50.5876),
                        HistoryCoordinate(23.5880, 58.3829),
                        HistoryCoordinate(19.9975, 73.7898),
                    ),
                ),
            ),
            places = listOf(
                HistoricalPlace("baghdad", HistoryText("بغداد", "Baghdad"), HistoryCoordinate(33.3152, 44.3661), HistoryText("من أبرز مراكز الخلافة والعلوم والتجارة في عصور متعددة.", "A major centre of caliphal rule, learning, and trade in several eras.")),
                HistoricalPlace("basra", HistoryText("البصرة", "Basra"), HistoryCoordinate(30.5085, 47.7835), HistoryText("ميناء ومركز معرفي مبكر.", "An early port and intellectual centre.")),
                HistoricalPlace("samarkand", HistoryText("سمرقند", "Samarkand"), HistoryCoordinate(39.6542, 66.9597), HistoryText("مدينة بارزة في تاريخ آسيا الوسطى الإسلامي.", "A prominent city in the Islamic history of Central Asia.")),
            ),
            initialCenter = HistoryCoordinate(32.0, 53.0),
            zoom = 2.8,
        ),
        HistoricalMapLayer(
            id = "ottoman_world",
            eraId = "ottoman_modern",
            title = HistoryText("العثمانيون: نطاق تقريبي", "Ottomans: schematic reach"),
            summary = HistoryText(
                "منطقة تقريبية تعليمية لدولة اتسع نفوذها وانكمش عبر قرون، مع وجود دول ومجتمعات إسلامية أخرى متزامنة.",
                "An approximate teaching area for a state whose reach expanded and contracted over centuries, alongside other Muslim states and communities.",
            ),
            schematicArea = listOf(
                HistoryCoordinate(48.0, 16.0),
                HistoryCoordinate(47.0, 38.0),
                HistoryCoordinate(42.0, 44.0),
                HistoryCoordinate(37.0, 38.0),
                HistoryCoordinate(30.0, 32.0),
                HistoryCoordinate(30.0, 24.0),
                HistoryCoordinate(36.0, 17.0),
            ),
            routes = emptyList(),
            places = listOf(
                HistoricalPlace("istanbul", HistoryText("إسطنبول", "Istanbul"), HistoryCoordinate(41.0082, 28.9784), HistoryText("عاصمة عثمانية بعد 1453م.", "An Ottoman capital after 1453 CE.")),
                HistoricalPlace("cairo", HistoryText("القاهرة", "Cairo"), HistoryCoordinate(30.0444, 31.2357), HistoryText("مدينة محورية في تاريخ المنطقة ومراكزها العلمية.", "A pivotal city in the region’s history and scholarly networks.")),
            ),
            initialCenter = HistoryCoordinate(38.0, 29.0),
            zoom = 3.3,
        ),
    )

    val personalities = listOf(
        HistoryPerson(
            id = "khalid_ibn_al_walid",
            years = "c. 592–642 CE",
            field = HistoryText("قائد", "Commander"),
            name = HistoryText("خالد بن الوليد", "Khalid ibn al-Walid"),
            summary = HistoryText("قائد عسكري من الجيل الإسلامي الأول؛ ترد سيرته في مصادر تاريخية متعددة مع اختلاف في الروايات والتقييمات.", "A military commander from the early Muslim generation; historical sources contain varied narratives and assessments of his career."),
            contribution = HistoryText("يوضع في هذا الدليل ضمن شخصيات التحولات العسكرية والسياسية المبكرة، لا كحكم على أحداثها المعقدة.", "Included here as a figure in early military and political transformations, not as a verdict on their complex events."),
        ),
        HistoryPerson(
            id = "tariq_ibn_ziyad",
            years = "d. c. 720 CE",
            field = HistoryText("قائد", "Commander"),
            name = HistoryText("طارق بن زياد", "Tariq ibn Ziyad"),
            summary = HistoryText("يرتبط في المصادر ببدايات الحكم الإسلامي في الأندلس في أوائل القرن الثامن.", "Associated in historical sources with the beginnings of Muslim rule in al-Andalus in the early eighth century."),
            contribution = HistoryText("يساعد ذكره على وصل الأطلس بين غرب المتوسط وتاريخ الأندلس المتعدد المراحل.", "His entry connects the atlas between the western Mediterranean and the multi-stage history of al-Andalus."),
        ),
        HistoryPerson(
            id = "al_khwarizmi",
            years = "c. 780–850 CE",
            field = HistoryText("رياضيات وفلك", "Mathematics and astronomy"),
            name = HistoryText("الخوارزمي", "Al-Khwarizmi"),
            summary = HistoryText("عالم ارتبطت أعماله بالحساب والجبر والجداول الفلكية في بيئة بغداد العلمية.", "A scholar associated with arithmetic, algebra, and astronomical tables in Baghdad’s scholarly milieu."),
            contribution = HistoryText("توضح سيرته أن الترجمة والتأليف والحساب كانت متداخلة في شبكات المعرفة العباسية.", "His career shows how translation, writing, and calculation interacted in Abbasid knowledge networks."),
        ),
        HistoryPerson(
            id = "al_razi",
            years = "c. 865–925 CE",
            field = HistoryText("طب وفلسفة طبيعية", "Medicine and natural philosophy"),
            name = HistoryText("الرازي", "Al-Razi"),
            summary = HistoryText("طبيب ومؤلف من أبرز الأسماء في التراث الطبي المكتوب باللغة العربية.", "A physician and author among the prominent names in Arabic-language medical literature."),
            contribution = HistoryText("تربط سيرته بين المستشفيات والكتب الطبية وتداول المعرفة بين المدن.", "His entry links hospitals, medical texts, and the circulation of knowledge among cities."),
        ),
        HistoryPerson(
            id = "ibn_al_haytham",
            years = "c. 965–1040 CE",
            field = HistoryText("بصريات ورياضيات", "Optics and mathematics"),
            name = HistoryText("ابن الهيثم", "Ibn al-Haytham"),
            summary = HistoryText("باحث اشتهر بدراساته في البصريات والرياضيات وطرائق الاستدلال التجريبي.", "A researcher known for studies in optics, mathematics, and approaches to experimental reasoning."),
            contribution = HistoryText("تُقرأ سيرته في سياق طويل لتاريخ العلوم لا بوصفها اختراعاً منفرداً معزولاً.", "His work is read within a long history of science, not as an isolated single invention."),
        ),
        HistoryPerson(
            id = "ibn_sina",
            years = "980–1037 CE",
            field = HistoryText("طب وفلسفة", "Medicine and philosophy"),
            name = HistoryText("ابن سينا", "Ibn Sina"),
            summary = HistoryText("طبيب وفيلسوف ألّف أعمالاً مؤثرة في الطب والفلسفة، وانتشرت مخطوطاتها عبر مناطق واسعة.", "A physician and philosopher whose influential writings in medicine and philosophy circulated widely."),
            contribution = HistoryText("توضح سيرته دور المخطوطات والمدارس والترجمة في انتقال المعرفة.", "His entry illustrates the role of manuscripts, schools, and translation in transmitting knowledge."),
        ),
        HistoryPerson(
            id = "ibn_khaldun",
            years = "1332–1406 CE",
            field = HistoryText("تاريخ وفكر اجتماعي", "History and social thought"),
            name = HistoryText("ابن خلدون", "Ibn Khaldun"),
            summary = HistoryText("مؤرخ وقاضٍ ورجل دولة من تونس والمغرب والأندلس ومصر؛ اشتهر بمقدمة كتابه في التاريخ.", "A historian, judge, and statesman connected with Tunisia, the Maghreb, al-Andalus, and Egypt; known for the introduction to his history."),
            contribution = HistoryText("تفتح مقدمته أسئلة عن العمران والتغير الاجتماعي وكيف يقرأ المؤرخ الروايات.", "His Muqaddimah opens questions about civilization, social change, and how historians evaluate narratives."),
        ),
        HistoryPerson(
            id = "mimar_sinan",
            years = "c. 1489–1588 CE",
            field = HistoryText("عمارة", "Architecture"),
            name = HistoryText("معمار سنان", "Mimar Sinan"),
            summary = HistoryText("معمار عثماني ارتبطت أعماله بالمساجد والمنشآت العامة والبنية الحضرية في القرن السادس عشر.", "An Ottoman architect associated with mosques, public works, and urban infrastructure in the sixteenth century."),
            contribution = HistoryText("تبيّن سيرته أن الحضارة تشمل العمارة والإدارة والحياة الحضرية بقدر ما تشمل النصوص السياسية.", "His entry shows that civilization includes architecture, administration, and urban life as much as political texts."),
        ),
    )

    fun eraById(id: String): HistoryEra? = timeline.firstOrNull { it.id == id }

    fun mapLayerById(id: String): HistoricalMapLayer? = atlasLayers.firstOrNull { it.id == id }
}
