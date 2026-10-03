# Quran recitation sources

## Existing ayah playback

The bundled EveryAyah catalogue remains the source for ayah-by-ayah playback. Its recording URLs retain the existing stable reciter IDs, queue behavior, and per-ayah follow state.

## MP3Quran complete-surah catalogue

MP3Quran v3 returns reciters with one or more `moshaf` recordings. Each recording identifies its recitation label, HTTPS server, and available surahs in `surah_list`. Muslim models each recording separately from `Reciter`, because its files contain complete surahs rather than synchronized ayah clips. See the [official MP3Quran API documentation](https://www.mp3quran.net/eng/api).

The Android client fetches the catalogue on demand, accepts audio hosts only under `mp3quran.net`, and builds a surah URL only when that surah is advertised. The URL pattern is a three-digit surah number followed by `.mp3`. Source probes establish transport and seek metadata only. The provider catalogue does not report a bitrate, so Muslim must not label a recording's bitrate or quality unless it validates them separately.

The source probe first tries `HEAD`. If an origin rejects `HEAD` or omits verifiable audio length/range metadata, the client retries with `Range: bytes=0-0`; it accepts that fallback only for a `206 audio/mpeg` response whose `Content-Range` proves a single byte and a positive total length. The response body is closed without reading the rest of the recording. A `404`/`410` from `HEAD` is treated as an unavailable source without retrying.

The catalogue and source probe are data-layer groundwork. They are not yet connected to a playback control. Full-surah playback must be integrated into the shared Quran output/session contract before surfacing the choices in reader, downloads, Android Auto, or Cast. It must not masquerade as ayah-synchronous audio or claim tafsir follow, queue restoration, offline download, or Cast synchronization that it does not provide. Neither API metadata nor these probes report bitrate or subjective recitation fidelity; no quality ranking is inferred from them.
