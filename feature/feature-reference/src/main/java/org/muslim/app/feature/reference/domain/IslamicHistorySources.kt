package org.muslim.app.feature.reference.domain

/**
 * Shared source registry for the Islamic history & civilization feature.
 *
 * Content records reference sources by stable IDs so articles, states, events and future
 * atlas/place entries can share citations without duplicating metadata.
 */
object IslamicHistorySources {
    val all = listOf(
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
            id = "met_major_dynasties",
            title = HistoryText(
                "التسلسل الزمني للإمبراطوريات والسلالات الكبرى في العالم الإسلامي — متحف المتروبوليتان",
                "Chronology of Major Empires and Dynasties in the Islamic World — The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/-/media/files/learn/for%20educators/publications%20for%20educators/islamic%20teacher%20resource/chronology.pdf",
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
            id = "met_fatimid",
            title = HistoryText(
                "فن العصر الفاطمي (909–1171) — متحف المتروبوليتان للفنون",
                "The Art of the Fatimid Period (909–1171) — The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/essays/the-art-of-the-fatimid-period-909-1171",
        ),
        HistorySource(
            id = "met_ayyubid",
            title = HistoryText(
                "فن العصر الأيوبي (نحو 1171–1260) — متحف المتروبوليتان للفنون",
                "The Art of the Ayyubid Period (ca. 1171–1260) — The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/essays/the-art-of-the-ayyubid-period-ca-1171-1260",
        ),
        HistorySource(
            id = "met_ilkhanid",
            title = HistoryText(
                "فن العصر الإيلخاني (1256–1353) — متحف المتروبوليتان للفنون",
                "The Art of the Ilkhanid Period (1256–1353) — The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/essays/the-art-of-the-ilkhanid-period-1256-1353",
        ),
        HistorySource(
            id = "met_timurid",
            title = HistoryText(
                "فن العصر التيموري (نحو 1370–1507) — متحف المتروبوليتان للفنون",
                "The Art of the Timurid Period (ca. 1370–1507) — The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/essays/the-art-of-the-timurid-period-ca-1370-1507",
        ),
        HistorySource(
            id = "met_islamic_art",
            title = HistoryText(
                "فن العالم الإسلامي — مواد تعليمية من متحف المتروبوليتان للفنون",
                "Art of the Islamic World — educator resources from The Metropolitan Museum of Art",
            ),
            kind = HistorySourceKind.Museum,
            url = "https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world",
        ),
        HistorySource(
            id = "britannica_waqf",
            title = HistoryText(
                "الوقف — الموسوعة البريطانية",
                "Waqf — Encyclopaedia Britannica",
            ),
            kind = HistorySourceKind.Reference,
            url = "https://www.britannica.com/topic/waqf",
        ),
        HistorySource(
            id = "britannica_madrasah",
            title = HistoryText(
                "المدرسة — الموسوعة البريطانية",
                "Madrasah — Encyclopaedia Britannica",
            ),
            kind = HistorySourceKind.Reference,
            url = "https://www.britannica.com/topic/madrasah",
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

    fun byId(id: String): HistorySource? = all.firstOrNull { it.id == id }
}
