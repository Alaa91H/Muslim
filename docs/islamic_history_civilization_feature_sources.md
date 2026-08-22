# Islamic History & Civilization: Sources, Scope, and Map Boundaries

## Scope

The feature is an **educational orientation**, not a comprehensive or official historical account. It offers a compact bilingual timeline, selected historical figures, and interactive geographic overlays. The aim is to help a learner connect periods, centres of learning, routes, and biographies while clearly retaining uncertainty, plurality, and the need for specialist review.

The timeline does not attempt to adjudicate contested political, sectarian, military, or biographical narratives. Its selection of periods is deliberately broad: the Prophetic era, Rashidun period, Umayyads, Abbasids, overlapping regional civilizations, Ottomans, and the modern era. Existing detailed Sira material remains separately available in the app’s Reference Library.

## Sources reviewed

| Area | Source | Implementation use |
| --- | --- | --- |
| Broad chronology and dynasties | [The Metropolitan Museum of Art: Chronology of the Islamic World](https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world/introduction/chronology) | Used to cross-check major dynasty ranges and regional diversity, including the Umayyads, Abbasids, Andalusian, Ottoman, Safavid, Timurid, Mamluk, and Mughal periods. |
| Scientific and cultural context | [The Met: Science and the Art of the Islamic World](https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world/unit-four) | Informed careful wording on astronomy, medicine, Arabic translation, scientific instruments, and circulation of knowledge; no lone-inventor claims are used. |
| Trade and caravan networks | [UNESCO: About the Silk Roads](https://www.unesco.org/en/silk-roads/about-silk-roads) | Supports the framing of land and maritime routes as shifting networks carrying goods, ideas, languages, and beliefs rather than one fixed route. |
| Caliphal chronology context | [World History Encyclopedia: Islamic Caliphates](https://www.worldhistory.org/Islamic_Caliphates/) | Used only as a supplementary overview and cross-checked against the museum chronology; the app avoids importing its interpretive language. |
| Ibn Khaldun profile | [Ibn Haldun University: About Ibn Haldun](https://www.ihu.edu.tr/en/ibn-haldun-kimdir) | Used for basic dates, roles, and the scope of the *Muqaddimah* in the Ibn Khaldun card. |

## Map policy

The atlas uses **original vector overlays authored for this application**. It does not copy third-party historical-map images, atlas plates, or boundary datasets. It uses the application’s established OpenFreeMap/MapLibre basemap at runtime for geographic context, then adds the following original overlays:

| Overlay | Meaning | Explicit limitation |
| --- | --- | --- |
| Schematic areas | Broad educational orientation for selected periods | Not borders, sovereignty claims, a claim of continuous control, or a full representation of all states and communities. |
| Route lines | Simplified land/maritime orientation between selected cities | Not reconstructed caravan itineraries, sailing tracks, battle routes, or evidence of a single historic route. |
| Place markers | Selected cities and sites that support the presented topic | Not an exhaustive gazetteer and not a precise archaeological coordinate dataset. |

The UI visibly labels all areas and lines as schematic. Dates, political geography, place spellings, events, and biographies should receive specialist Islamic-history review before the content is used as an official educational account.

## Licensing and network behavior

No external historical image, scanned atlas, copyrighted textbook map, or third-party biography text is bundled. The original line/polygon/marker data is shipped as Kotlin data. The basemap is provided through the same open MapLibre/OpenFreeMap setup already used by the app, so the base map may require network access unless the user has previously downloaded an offline map area through the app’s established offline-map feature. The historical overlays themselves are local and require no map-data API key.
