# Hadith Cover Artwork

> **Scope:** This record documents the visual treatment used by the Hadith-library catalogue after the professional cover refresh.

The catalogue now renders its book covers directly in Jetpack Compose instead of packaging photographic or scanned cover images. Each collection receives its own heritage-inspired palette while sharing one scalable geometric system: double framing, a central medallion, restrained gold-toned ornament, and a subtle book-spine cue.

## Why the implementation changed

The previous catalogue used nine small PNG resources. Several were different crops of the same general Hadith-books photograph, while a few represented specific historical manuscripts or editions. Although those images had verified reuse status, using a specific historical page as the visual identity of a bundled text can accidentally imply that the bundled corpus reproduces that exact edition.

The new artwork deliberately avoids that implication. It is original application UI artwork, not a reproduction of a publisher cover, manuscript, or historical edition.

## Rendering contract

| Property | Result |
|---|---|
| Rendering | Native Compose drawing and text; no bitmap cover assets |
| Scaling | Resolution-independent at every screen density |
| Collection identity | Nine distinct curated palettes |
| Typography | The localized collection title from app resources |
| Ornament | Shared heritage-inspired geometric frame and medallion |
| Dark/light theme | Artwork retains a stable book identity while the surrounding card follows Material theme |
| APK impact | Previous cover PNG resources are removed |
| Data model | Cover resource IDs are removed from `HadithCollection`; corpus and Room behavior are unchanged |

## Representation boundary

The cover art is intentionally decorative. It identifies the collection in the app but does **not** claim to reproduce a specific printed edition, publisher binding, manuscript, or title page. The Hadith text metadata remains the authoritative description of the bundled corpus.

## Historical source research

Public-domain historical material remains useful as visual research, including early Sahih al-Bukhari manuscripts, older Sahih Muslim title pages, the 1937 first-edition Sunan al-Tirmidhi cover, the 1326 Al-Muwatta manuscript, and public-domain scans of other collections. Those references are not bundled into the application after this change.
