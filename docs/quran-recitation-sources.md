# Quran recitation sources

## Existing ayah playback

The bundled EveryAyah catalogue remains the source for ayah-by-ayah playback. Its recording URLs retain the existing stable reciter IDs, queue behavior, and per-ayah follow state.

## MP3Quran complete-surah catalogue

MP3Quran v3 returns reciters with one or more `moshaf` recordings. Each recording identifies its recitation label, HTTPS server, and available surahs in `surah_list`. Muslim models each recording separately from `Reciter`, because its files contain complete surahs rather than synchronized ayah clips. See the [official MP3Quran API documentation](https://www.mp3quran.net/eng/api).

The Android client fetches the catalogue on demand, accepts audio hosts only under `mp3quran.net`, and builds a surah URL only when that surah is advertised. The URL pattern is a three-digit surah number followed by `.mp3`. Source probes establish transport and seek metadata only. The provider catalogue does not report a bitrate, so Muslim must not label a recording's bitrate or quality unless it validates them separately.

The source probe first tries `HEAD`. If an origin rejects `HEAD` or omits verifiable audio length/range metadata, the client retries with `Range: bytes=0-0`; it accepts that fallback only for a `206 audio/mpeg` response whose `Content-Range` proves a single byte and a positive total length. The response body is closed without reading the rest of the recording. A `404`/`410` from `HEAD` is treated as an unavailable source without retrying.

The catalogue is available from the Quran reader's recitation settings. It is searchable by reciter name, recitation label, or stable catalogue ID. Selecting an entry starts the complete surah through the shared `QuranAudioPlayer` and persists its position in the common recitation session; the anchor ayah is not treated as an audio synchronization point. Whole-surah choices remain distinct from the per-ayah EveryAyah reciter catalogue.

The reader offers both playback and a verified-size download action for each advertised recording. Downloads use the existing foreground download service, persist their source and request across process recreation, and use the resumable `.part` transfer path. Files are stored under app-private `quran_recitations/full_surah/<sha256-recording-id>/<surah>.mp3` storage; the provider recording ID is hashed before it becomes part of a path, and audio URLs must use HTTPS on an MP3Quran host. Playback prefers a non-empty local file, preserving the same player queue and resumable session position, and streams from the provider when the file is not present. No audio is uploaded elsewhere.

A full-surah Android Auto model, Cast handoff for this playback scope, and per-recording live seek/hardware validation remain open. Playback and source probes do not establish bitrate or subjective listening quality, so no recording quality ranking is inferred from provider catalogue metadata.

## EveryAyah live transport sample

On 2026-10-04, the opt-in `EveryAyahFolderCheckTest` checked three URLs for each of the 46 bundled per-ayah reciters: 001:001, 002:286, and 114:006. All 138 requests returned a validated `206 audio/mpeg` response with a one-byte `Content-Range` and a readable byte. This is a sampled transport/seek check, not an exhaustive audit of every ayah file, a bitrate measurement, or a subjective listening-quality ranking. The audit remains opt-in (`-DnetworkTests=true`) so ordinary offline unit tests do not depend on EveryAyah availability.
