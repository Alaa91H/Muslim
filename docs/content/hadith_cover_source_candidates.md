# Hadith Cover Provenance

> **Scope:** This record covers the real physical-volume and historical-manuscript imagery used by the Hadith-library catalogue after `v1.25.10`. It is an asset-provenance record, not a statement that any image reproduces the exact edition represented by the bundled Hadith text.

The catalogue previously used original illustrative cover artwork. It now uses compact 240×360 PNG resources derived from verified public-domain source images. Contemporary commercial publisher covers, storefront images, and files without an explicit reuse right remain excluded.

| Catalogue collection | Packaged resource | Image source | Confirmed reuse status | Representation boundary |
|---|---|---|---|---|
| Al-Bukhari | `hadith_cover_bukhari.png` | [`Hadith Books.jpg`](https://commons.wikimedia.org/wiki/File:Hadith_Books.jpg) | The author, Bakkouz, released the photograph into the public domain worldwide and grants unrestricted use where a dedication is not legally possible. | Distinct crop of genuine physical Hadith volumes; the UI label, not the photo, identifies the collection. |
| Muslim | `hadith_cover_muslim.png` | [`Hadith Books.jpg`](https://commons.wikimedia.org/wiki/File:Hadith_Books.jpg) | Public-domain dedication by Bakkouz. | Distinct crop of genuine physical Hadith volumes; not represented as a particular publisher edition. |
| Abu Dawud | `hadith_cover_abudawud.png` | [`Hadith Books.jpg`](https://commons.wikimedia.org/wiki/File:Hadith_Books.jpg) | Public-domain dedication by Bakkouz. | Distinct crop of genuine physical Hadith volumes; not represented as a particular publisher edition. |
| At-Tirmidhi | `hadith_cover_tirmidhi.png` | [`سنن الترمذي.jpg`](https://commons.wikimedia.org/wiki/File:%D8%B3%D9%86%D9%86_%D8%A7%D9%84%D8%AA%D8%B1%D9%85%D8%B0%D9%8A.jpg) | The Commons page identifies the title as *Sunan al-Tirmidhi* and records that the Egyptian work is public domain in Egypt and meets Commons’ U.S. public-domain hosting requirement. | Historical title page / cover for the named collection. |
| An-Nasai | `hadith_cover_nasai.png` | [`Hadith Books.jpg`](https://commons.wikimedia.org/wiki/File:Hadith_Books.jpg) | Public-domain dedication by Bakkouz. | Distinct crop of genuine physical Hadith volumes; not represented as a particular publisher edition. |
| Ibn Majah | `hadith_cover_ibnmajah.png` | [`Hadith Books.jpg`](https://commons.wikimedia.org/wiki/File:Hadith_Books.jpg) | Public-domain dedication by Bakkouz. | Distinct crop of genuine physical Hadith volumes; not represented as a particular publisher edition. |
| Muwatta Malik | `hadith_cover_muwatta.png` | [`Muwatta (Malik ibn Anas) 1326 Salé Morocco Marinid manuscript.png`](https://commons.wikimedia.org/wiki/File:Muwatta_(Malik_ibn_Anas)_1326_Sal%C3%A9_Morocco_Marinid_manuscript.png) | The Commons page identifies a faithful reproduction of a two-dimensional public-domain work and carries the Public Domain Mark, stating that it is free of known copyright restrictions. | Authentic 1326 manuscript representation for the named collection. |
| Riyad as-Salihin | `hadith_cover_riyad.png` | [`Hadith Books.jpg`](https://commons.wikimedia.org/wiki/File:Hadith_Books.jpg) | Public-domain dedication by Bakkouz. | Distinct crop of genuine physical Hadith volumes; not represented as a particular publisher edition. |
| An-Nawawi 40 | `hadith_cover_nawawi40.png` | [`Nawawi 40 manuscript.PNG`](https://commons.wikimedia.org/wiki/File:Nawawi_40_manuscript.PNG) | The Commons page identifies a Nawawi Forty Hadith manuscript and records public-domain status in its country of origin and in the United States, carrying the Public Domain Mark. | Authentic manuscript representation for the named collection. |

## Preparation and integrity

The source originals were used only to create display-scale resources. The final assets retain source imagery without added wording, title replacement, or a claim that a general physical-volume photograph is a specific edition. The catalogue continues to show its own localized collection name and source description as the authoritative metadata.

| Check | Result |
|---|---|
| Final resource dimensions | Nine PNG resources at 240×360 pixels. |
| Packaging discipline | Only the display-scale derived resources are stored in the Android drawable directory; large source copies are excluded from the APK. |
| Runtime data model | Unchanged: selecting a collection streams only that collection’s local content into Room in bounded batches. |
| Licensing decision | Only Commons sources with an explicit public-domain statement were used. |

## References

[1]: https://commons.wikimedia.org/wiki/File:Hadith_Books.jpg "Wikimedia Commons — File:Hadith Books.jpg"
[2]: https://commons.wikimedia.org/wiki/File:%D8%B3%D9%86%D9%86_%D8%A7%D9%84%D8%AA%D8%B1%D9%85%D8%B0%D9%8A.jpg "Wikimedia Commons — File:سنن الترمذي.jpg"
[3]: https://commons.wikimedia.org/wiki/File:Muwatta_(Malik_ibn_Anas)_1326_Sal%C3%A9_Morocco_Marinid_manuscript.png "Wikimedia Commons — Muwatta (Malik ibn Anas) 1326 Salé Morocco Marinid manuscript"
[4]: https://commons.wikimedia.org/wiki/File:Nawawi_40_manuscript.PNG "Wikimedia Commons — Nawawi 40 manuscript"
