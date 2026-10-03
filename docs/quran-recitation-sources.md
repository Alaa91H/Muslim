# Quran recitation sources

## Existing ayah playback

The bundled EveryAyah catalogue remains the source for ayah-by-ayah playback. Its recording URLs retain the existing stable reciter IDs, queue behavior, and per-ayah follow state.

## MP3Quran complete-surah catalogue

MP3Quran v3 returns reciters with one or more `moshaf` recordings. Each recording identifies its recitation label, HTTPS server, and available surahs in `surah_list`. Muslim models each recording separately from `Reciter`, because its files contain complete surahs rather than synchronized ayah clips. See the [official MP3Quran API documentation](https://www.mp3quran.net/eng/api).

The Android client fetches the catalogue on demand, accepts audio hosts only under `mp3quran.net`, and builds a surah URL only when that surah is advertised. The URL pattern is a three-digit surah number followed by `.mp3`. HTTP HEAD probes accept a source only when it returns status 200, `audio/mpeg`, and a positive content length; `Accept-Ranges: bytes` is recorded when offered. These checks establish transport and seek metadata only. The provider catalogue does not report a bitrate, so Muslim must not label a recording's bitrate or quality unless it validates them separately.

The catalogue and source probe are data-layer groundwork. They are not yet connected to a playback control. Full-surah playback must be integrated into the shared Quran output/session contract before surfacing the choices in reader, downloads, Android Auto, or Cast. It must not masquerade as ayah-synchronous audio or claim tafsir follow, queue restoration, offline download, or Cast synchronization that it does not provide.
