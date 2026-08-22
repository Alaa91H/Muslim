# Traveller and Expat Corner: Content, Calculation, and Privacy Notes

## Purpose and boundaries

This feature provides **educational orientation and local technical aids**, not an individual fatwa. Its distance tool reports the great-circle distance between a user-selected locally stored departure point and a one-time GPS fix. It deliberately labels that output as a reference rather than a determination that the user personally may shorten or combine prayers.

The guidance repeatedly distinguishes between a technical calculation and the fiqh questions that depend on an individual’s route, distance convention, intended stay, prayer, ability, school of jurisprudence, and local authority. The wording directs users to qualified local scholars and their mosque timetable rather than presenting one view as universal.

## Sources reviewed

| Area | Source | Implementation decision |
| --- | --- | --- |
| Travel prayer and hardship | [AMJA: Shortening and combining prayers during travel](https://www.amjaonline.org/fatwa/en/3078/shortening-and-combining-prayers-and-ruling-on-sunnah-prayers-during-travel) | The guide acknowledges that shortening/combining are travel-fiqh questions and refers individual applications to a qualified scholar. It does not encode a ruling about intended stay. |
| Transport prayer | [AMJA: Praying while sitting on airplanes](https://www.amjaonline.org/fatwa/en/80576/praying-while-sitting-on-airplanes) | The transport guide uses ability- and safety-conscious language, avoiding a universal posture rule. |
| High-latitude zones and proportional estimations | [IslamOnline report of ECFR/Muslim World League resolution](https://fiqh.islamonline.net/en/praying-and-fasting-at-high-latitudes/) | The UI explains the 48° and 66° bands as an educational orientation and states that authorities adopt differing methods. |
| Calculation methods and higher-latitude approaches | [PrayTimes calculation documentation](https://praytimes.org/docs/calculation) | The screen describes the existing Middle-of-the-Night, One-Seventh, and twilight-angle options without claiming any is universally preferred. |
| Regional calculation-method diversity | [Fiqh Council of North America: Fajr and Isha method](https://fiqhcouncil.org/the-suggested-calculation-method-for-fajr-and-isha/) | The high-latitude copy tells users to use the method/timetable adopted by their mosque or recognised authority. |

## Calculation implementation

The feature reuses the application’s established `PrayerTimesCalculator`, whose solar model follows the MIT-licensed Adhan algorithm and existing project test vectors. It reads the saved prayer location, time zone, calculation method, high-latitude setting, Asr method, and manual adjustments. It therefore does not create a competing prayer-time calculator.

The travel-distance tool uses the haversine great-circle formula with the IUGG mean Earth radius of 6,371.0088 km. It presents 80 km and 90 km as selectable **reference thresholds** only. Neither option is represented as a universal legal minimum, and road distance is never substituted with straight-line distance.

## Offline compass and privacy

The local compass uses Android device sensors, local geomagnetic-declination correction, and the existing great-circle qibla bearing. Once an on-device coordinate is available, it requires no network request. The user must explicitly request each GPS fix; the feature does not register background location updates or upload a location. Only the selected departure latitude/longitude is stored locally in a dedicated Preferences DataStore and can be cleared in the UI.

## Release-review requirement

The project’s religious-content policy applies. A qualified Islamic-studies reviewer should review the Arabic and English travel, transport, purification, and high-latitude text, as well as the threshold presentation and links to local practice, before any official religious-content release.
