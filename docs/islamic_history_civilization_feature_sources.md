# Islamic History & Civilization: Sources, Scope, and Map Boundaries

## Scope

The feature is being expanded from an **educational orientation** into a layered, source-led reference library. The timeline remains the entry point, but every currently visible era now has a bilingual long-form article with multiple sections, explicit source references, related-era links, and a dedicated reader view. The first batch is still curated rather than exhaustive, and specialist review remains required before the material is treated as an official historical account.

The timeline does not attempt to adjudicate contested political, sectarian, military, or biographical narratives. Its current top-level periods are deliberately broad: the Prophetic era, Rashidun period, Umayyads, Abbasids, overlapping regional civilizations, Ottomans, and the modern era. Existing detailed Sira material remains separately available in the app’s Reference Library and is linked instead of duplicated.


## Content architecture

The history feature now separates **UI**, **content contracts**, and **curated content**:

- `HistoryArticleModel.kt` defines long-form articles, sections, structured dates, sources, events, states, regions, and civilization topics.
- `IslamicHistorySources.kt` provides one shared source registry for articles, states, and future event/place records.
- `IslamicHistoryArticles.kt` contains the first long-form bilingual article set.
- `IslamicHistoryStates.kt` adds 18 major overlapping states and dynasties, grouped by broad region instead of forcing them into a single succession.
- `IslamicCivilizationContent.kt` adds 12 long-form thematic topics across knowledge/sciences, institutions, society/economy, and arts/built environment.
- `IslamicHistoricalEvents.kt` adds 22 structured chronological anchors with context, significance, category, and links to eras, states, atlas places, people, civilization topics, and sources.
- `IslamicHistoryPeopleProfiles.kt` and `IslamicHistoryPlaceProfiles.kt` hold the long-form profile catalogues for all 8 exposed historical figures and all 13 atlas places.
- `IslamicHistoryProfiles.kt` is the compact lookup facade used by UI, validation, and search code.
- `IslamicHistorySearch.kt` remains the dependency-free in-memory search/fallback implementation and Arabic normalization utility.
- `assets/history/search_index.json` packages the current 79 searchable entities as a versioned JSON snapshot (6 eras, 18 states, 22 events, 12 civilization topics, 8 people, and 13 places).
- `IslamicHistorySearchDatabase.kt` stores that snapshot in a local Room FTS4 index, while `IslamicHistorySearchRepository.kt` handles version-aware seeding, typed FTS queries, ranking, and automatic fallback to the in-memory search if database/asset initialization fails.
- `IslamicHistoryNavigation.kt` provides stable cross-section navigation targets so search results and related-entity links can open the correct destination.
- `HistoryContentValidator.kt` validates unique IDs, bilingual completeness, chronology sanity, atlas time ranges, source references, related-era/topic references, state/era links, event references, person/place links, profile coverage, and section structure.
- `HistoryContentValidatorTest.kt` makes those rules part of CI so broken references or incomplete articles are caught before merge.

The Compose screen now exposes dedicated **States & Dynasties**, **Civilization**, **Events**, and **Search** destinations. States use regional filters to make overlapping political histories visible side by side; Civilization uses thematic filters and dedicated readers so science, institutions, economic life, cities, architecture, and visual culture are not reduced to political chronology; Events adds chronological anchors with category and era filters plus detailed context/significance views. Search works locally across all major entity types and opens the matching detailed destination directly. Related states, events, people, places, and civilization topics are now actionable cross-links instead of dead-end labels. The People tab and atlas place cards open full profiles rather than ending at short summaries. The tab row is scrollable so the expanded information architecture remains usable on small screens. This separation is also the migration boundary for a later move from Kotlin constants to packaged JSON/Room content without rewriting the readers.

## Sources reviewed

| Area | Source | Implementation use |
| --- | --- | --- |
| Broad chronology and dynasties | [The Metropolitan Museum of Art: Chronology of the Islamic World](https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world/introduction/chronology) and its major-dynasties chronology | Used to cross-check major dynasty ranges and regional diversity, including the Umayyads, Abbasids, Andalusian, Seljuq, Mamluk, Ottoman, Timurid, Safavid, and Mughal periods. |
| Fatimid chronology | [The Met: The Art of the Fatimid Period (909–1171)](https://www.metmuseum.org/essays/the-art-of-the-fatimid-period-909-1171) | Used for the Fatimid state range and cautious summary of Cairo-centered cultural development. |
| Ayyubid chronology | [The Met: The Art of the Ayyubid Period (ca. 1171–1260)](https://www.metmuseum.org/essays/the-art-of-the-ayyubid-period-ca-1171-1260) | Used for the approximate Ayyubid state range and its Egypt/Levant/Yemen framing. |
| Ilkhanid and Timurid chronology | The Met Heilbrunn Timeline essays for the Ilkhanid and Timurid periods | Used to cross-check their date ranges and regional framing. |
| Scientific and cultural context | [The Met: Science and the Art of the Islamic World](https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world/unit-four) | Informed careful wording on astronomy, medicine, mathematics, optics, Arabic translation, scientific instruments, and circulation of knowledge; no lone-inventor claims are used. |
| Art and built environment | [The Met: Art of the Islamic World educator resources](https://www.metmuseum.org/learn/educators/curriculum-resources/art-of-the-islamic-world) | Supports the regional, material, and functional framing of architecture, calligraphy, and decorative arts. |
| Waqf and madrasah orientation | Encyclopaedia Britannica reference entries on waqf and madrasah | Used as supplementary orientation for definitions and institutional framing; local practice is explicitly presented as historically variable. |
| Trade and caravan networks | [UNESCO: About the Silk Roads](https://www.unesco.org/en/silk-roads/about-silk-roads) | Supports the framing of land and maritime routes as shifting networks carrying goods, ideas, languages, and beliefs rather than one fixed route. |
| Caliphal chronology context | [World History Encyclopedia: Islamic Caliphates](https://www.worldhistory.org/Islamic_Caliphates/) | Used only as a supplementary overview and cross-checked against the museum chronology; the app avoids importing its interpretive language. |
| Ibn Khaldun profile | [Ibn Haldun University: About Ibn Haldun](https://www.ihu.edu.tr/en/ibn-haldun-kimdir) | Used for basic dates, roles, and the scope of the *Muqaddimah* in the Ibn Khaldun card. |




## Packaged content and Room/FTS migration boundary

Phase 7 establishes the storage boundary needed to move the growing reference catalogue out of Kotlin constants without forcing a risky all-at-once rewrite.

The first migrated artifact is the **search/content index**:

- the current 79 public history entities are serialized into a packaged, versioned JSON asset;
- the asset declares both a schema version and a content version;
- startup/search initialization validates schema compatibility, unique entity keys, known entity types, and bilingual titles before indexing;
- a local Room FTS4 database is rebuilt transactionally only when the packaged content version or document count changes;
- search queries are debounced in Compose, normalized for Arabic matching, converted to safe prefix-token FTS expressions, and can be restricted to one entity type;
- failures in asset loading, Room initialization, or FTS querying fall back to the existing dependency-free search so the reference screen remains usable.

The long-form canonical domain records (articles, state/event/topic/profile details) still remain in Kotlin during this migration step. This is intentional: Phase 7 moves indexing and persistence first, then later content batches can be moved into packaged JSON/Room behind the same repository boundary without rewriting navigation and reader UI again.

## Search, cross-navigation, and atlas time filter

The feature now provides an offline unified search index over the curated in-app catalogue. Search covers era titles and summaries, states/dynasties, event context and significance, people and their long-form profile sections, atlas places and place profiles, and civilization-topic sections. Arabic search removes Qur'anic/Arabic diacritics and tatweel and normalizes common alef, ya, hamza-seat, and ta-marbuta variants to improve matching without network services.

Search results use typed navigation targets. Opening a result switches to the appropriate destination and opens the detailed era, state, event, person, place, or civilization topic when that entity supports a detail reader. The same navigation targets are reused for related-entity links inside event, state, civilization, person, and place detail views.

Atlas layers now carry explicit CE start/end metadata. A 610–1924 CE time slider selects the closest available educational map layer and displays states/dynasties active in the selected year plus the nearest structured events. This is a contextual learning aid, not a claim that the selected map overlay is a precise political boundary snapshot for that year; the existing schematic-map warning remains in force.

## People and place profile policy

People and places are treated as reusable entities rather than isolated cards. Every person currently shown in the People tab and every place currently exposed by an atlas layer has a corresponding long-form profile.

Profiles provide:

- a bilingual overview and multiple long-form sections;
- links to known eras, states, events, civilization topics, and related entities where appropriate;
- explicit source IDs;
- atlas coordinates for places through the existing canonical map entity rather than duplicate coordinates in the profile record.

The profile layer deliberately avoids inventing a precise association where the existing atlas catalogue does not yet contain the correct city. A person can therefore have no place link until the relevant place is added rather than being attached to a merely nearby or regionally related city.

## Event catalogue policy

The event catalogue is an **index of chronological anchors**, not a claim that Islamic history is primarily a sequence of wars, dynastic changes, or decisions made by rulers. Events include community/religious transitions, political changes, conflict, urban foundations, knowledge/cultural developments, and institutional changes. Each record contains:

- a structured CE date with exact/approximate/range/disputed precision;
- a bilingual summary, context paragraph, and significance paragraph;
- links to one or more broad eras;
- optional links to known states, atlas places, people, and civilization topics;
- explicit source IDs.

When an event is contested in interpretation, the record should describe the documented sequence and attribute interpretive claims rather than assigning motives as fact.

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
