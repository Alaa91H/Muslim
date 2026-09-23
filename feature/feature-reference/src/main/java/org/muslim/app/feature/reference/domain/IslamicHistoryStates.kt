package org.muslim.app.feature.reference.domain

/**
 * Major overlapping states and dynasties. These records intentionally sit beside the
 * broad era timeline because several states coexisted for long periods.
 */
object IslamicHistoryStates {
    val states = listOf(
        HistoricalState(
            id = "rashidun",
            title = HistoryText("الخلافة الراشدة", "Rashidun caliphate"),
            period = HistoricalDate(632, 661),
            summary = HistoryText(
                "مرحلة الحكم التي تلت وفاة النبي ﷺ واتسعت خلالها سلطة الدولة خارج الجزيرة العربية.",
                "The period of rule after the Prophet’s death, during which authority expanded beyond Arabia.",
            ),
            region = HistoryRegion.MultiRegional,
            eraIds = listOf("rashidun"),
            sourceIds = listOf("met_chronology", "world_history_caliphates"),
        ),
        HistoricalState(
            id = "umayyad_caliphate",
            title = HistoryText("الدولة الأموية", "Umayyad caliphate"),
            period = HistoricalDate(661, 750),
            summary = HistoryText(
                "دولة اتخذت دمشق مركزاً وربطت أقاليم واسعة من غرب المتوسط إلى آسيا الوسطى.",
                "A Damascus-centered state spanning wide regions from the western Mediterranean to Central Asia.",
            ),
            region = HistoryRegion.MultiRegional,
            eraIds = listOf("umayyad"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "abbasid_caliphate",
            title = HistoryText("الدولة العباسية", "Abbasid caliphate"),
            period = HistoricalDate(750, 1258),
            summary = HistoryText(
                "خلافة ارتبطت بغداد بمراحلها المركزية مع ظهور دول إقليمية متزامنة.",
                "A caliphate strongly associated with Baghdad alongside the rise of concurrent regional states.",
            ),
            region = HistoryRegion.MultiRegional,
            eraIds = listOf("abbasid"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "umayyads_al_andalus",
            title = HistoryText("الأمويون في الأندلس", "Umayyads of al-Andalus"),
            period = HistoricalDate(756, 1031),
            summary = HistoryText(
                "حكم أموي مستقل في الأندلس تطور من إمارة إلى خلافة، وكانت قرطبة أبرز مراكزه.",
                "An independent Umayyad polity in al-Andalus that developed from an emirate into a caliphate centered on Cordoba.",
            ),
            region = HistoryRegion.MaghrebAndAlAndalus,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "samanids",
            title = HistoryText("الدولة السامانية", "Samanids"),
            period = HistoricalDate(819, 1005),
            summary = HistoryText(
                "سلالة حكمت أجزاء من إيران وآسيا الوسطى وأسهمت مدنها في شبكات التجارة والثقافة.",
                "A dynasty ruling parts of Iran and Central Asia whose cities participated in major commercial and cultural networks.",
            ),
            region = HistoryRegion.IranAndCentralAsia,
            eraIds = listOf("abbasid", "regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "fatimids",
            title = HistoryText("الدولة الفاطمية", "Fatimid dynasty"),
            period = HistoricalDate(909, 1171),
            summary = HistoryText(
                "نشأت في شمال إفريقيا ثم اتخذت القاهرة مركزاً رئيسياً وتركت إرثاً عمرانياً وفنياً وعلمياً واسعاً.",
                "A dynasty that emerged in North Africa and later made Cairo a principal center, leaving a major urban, artistic, and scholarly legacy.",
            ),
            region = HistoryRegion.EgyptAndLevant,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_fatimid"),
        ),
        HistoricalState(
            id = "seljuqs_iran",
            title = HistoryText("السلاجقة في إيران", "Seljuqs of Iran"),
            period = HistoricalDate(1040, 1196, HistoryDatePrecision.Approximate),
            summary = HistoryText(
                "قوة إقليمية واسعة امتدت عبر إيران وآسيا الوسطى وأجزاء من العراق والأناضول.",
                "A major regional power spanning Iran, Central Asia, and parts of Iraq and Anatolia.",
            ),
            region = HistoryRegion.IranAndCentralAsia,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "almoravids",
            title = HistoryText("المرابطون", "Almoravids"),
            period = HistoricalDate(1062, 1147, HistoryDatePrecision.Approximate),
            summary = HistoryText(
                "دولة امتد نفوذها في المغرب والأندلس وربطت مناطق صحراوية ومتوسطية.",
                "A state spanning the Maghreb and al-Andalus and linking Saharan and Mediterranean regions.",
            ),
            region = HistoryRegion.MaghrebAndAlAndalus,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "seljuqs_rum",
            title = HistoryText("سلاجقة الروم", "Seljuqs of Rum"),
            period = HistoricalDate(1081, 1307),
            summary = HistoryText(
                "دولة أناضولية ربطت طرق التجارة بين الأناضول وشرق المتوسط والداخل الآسيوي.",
                "An Anatolian state connecting trade routes between Anatolia, the eastern Mediterranean, and inland Asia.",
            ),
            region = HistoryRegion.Anatolia,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "almohads",
            title = HistoryText("الموحدون", "Almohads"),
            period = HistoricalDate(1130, 1269, HistoryDatePrecision.Approximate),
            summary = HistoryText(
                "دولة خلفت المرابطين في أجزاء واسعة من المغرب والأندلس.",
                "A state that succeeded the Almoravids across much of the Maghreb and al-Andalus.",
            ),
            region = HistoryRegion.MaghrebAndAlAndalus,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "ayyubids",
            title = HistoryText("الدولة الأيوبية", "Ayyubid dynasty"),
            period = HistoricalDate(1171, 1260, HistoryDatePrecision.Approximate),
            summary = HistoryText(
                "دولة أسسها صلاح الدين في مصر ثم امتدت سلطتها إلى مناطق من الشام واليمن.",
                "A dynasty founded by Salah al-Din in Egypt whose authority extended into parts of the Levant and Yemen.",
            ),
            region = HistoryRegion.EgyptAndLevant,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_ayyubid"),
        ),
        HistoricalState(
            id = "nasrids",
            title = HistoryText("مملكة بني نصر في غرناطة", "Nasrid kingdom of Granada"),
            period = HistoricalDate(1232, 1492),
            summary = HistoryText(
                "آخر دولة إسلامية كبرى في الأندلس، تمركزت في غرناطة وارتبطت بإرث معماري بارز.",
                "The last major Muslim-ruled state in al-Andalus, centered on Granada and associated with a major architectural legacy.",
            ),
            region = HistoryRegion.MaghrebAndAlAndalus,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "mamluks",
            title = HistoryText("سلطنة المماليك", "Mamluk sultanate"),
            period = HistoricalDate(1250, 1517),
            summary = HistoryText(
                "سلطنة تمركزت في مصر والشام وكانت القاهرة أحد أهم مراكزها السياسية والعلمية والتجارية.",
                "A sultanate centered in Egypt and the Levant, with Cairo among its major political, scholarly, and commercial centers.",
            ),
            region = HistoryRegion.EgyptAndLevant,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "ilkhanids",
            title = HistoryText("الدولة الإيلخانية", "Ilkhanids"),
            period = HistoricalDate(1256, 1353),
            summary = HistoryText(
                "فرع مغولي حكم إيران والعراق وأقاليم مجاورة ثم أصبح جزء من نخبه الحاكمة مسلماً.",
                "A Mongol dynasty ruling Iran, Iraq, and neighboring regions, whose ruling elite later included Muslim converts.",
            ),
            region = HistoryRegion.IranAndCentralAsia,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_ilkhanid"),
        ),
        HistoricalState(
            id = "ottomans",
            title = HistoryText("الدولة العثمانية", "Ottoman empire"),
            period = HistoricalDate(1299, 1923),
            summary = HistoryText(
                "دولة طويلة العمر نشأت في الأناضول واتسعت عبر البلقان وشرق المتوسط وأجزاء من المشرق وشمال إفريقيا.",
                "A long-lived state that emerged in Anatolia and expanded across the Balkans, eastern Mediterranean, parts of the Middle East, and North Africa.",
            ),
            region = HistoryRegion.MultiRegional,
            eraIds = listOf("ottoman_modern"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "timurids",
            title = HistoryText("الدولة التيمورية", "Timurids"),
            period = HistoricalDate(1370, 1507, HistoryDatePrecision.Approximate),
            summary = HistoryText(
                "سلالة تمركزت في آسيا الوسطى وإيران واشتهرت برعاية العمارة والفنون والكتب.",
                "A dynasty centered in Central Asia and Iran and noted for patronage of architecture, arts, and books.",
            ),
            region = HistoryRegion.IranAndCentralAsia,
            eraIds = listOf("regional_civilizations"),
            sourceIds = listOf("met_timurid"),
        ),
        HistoricalState(
            id = "safavids",
            title = HistoryText("الدولة الصفوية", "Safavid empire"),
            period = HistoricalDate(1501, 1722),
            summary = HistoryText(
                "دولة تمركزت في إيران وأعادت تشكيل الحياة السياسية والثقافية والدينية في المنطقة.",
                "A state centered in Iran that reshaped the region’s political, cultural, and religious life.",
            ),
            region = HistoryRegion.IranAndCentralAsia,
            eraIds = listOf("ottoman_modern"),
            sourceIds = listOf("met_major_dynasties"),
        ),
        HistoricalState(
            id = "mughals",
            title = HistoryText("الدولة المغولية في الهند", "Mughal empire"),
            period = HistoricalDate(1526, 1858),
            summary = HistoryText(
                "إمبراطورية حكمت معظم شبه القارة الهندية في مراحل مختلفة وارتبطت بإدارة واسعة ورعاية كبيرة للفنون والعمارة.",
                "An empire that ruled much of the Indian subcontinent at different times and was associated with extensive administration and major artistic and architectural patronage.",
            ),
            region = HistoryRegion.SouthAsia,
            eraIds = listOf("ottoman_modern"),
            sourceIds = listOf("met_major_dynasties"),
        ),
    ).sortedBy { it.period.startCe }

    fun byId(id: String): HistoricalState? = states.firstOrNull { it.id == id }

    fun byRegion(region: HistoryRegion?): List<HistoricalState> =
        if (region == null) states else states.filter { it.region == region }
}
