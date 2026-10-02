# Muslim ↔ QuranLiveStream Cast integration

## Responsibility and state ownership

`QuranAudioPlayer` remains the only queue/repeat/ayah authority. The Quran reader maps its current ayah, selected reciter, translation/tafsir, and prayer snapshot into schema v2. Standard Cast media LOAD/play/pause/seek/stop carries audio; `urn:x-cast:org.muslim.quran` carries `FULL_STATE` and display updates. The QuranLiveStream receiver uses CAF (`CastReceiverContext` and `PlayerManager`), renders payload text as text nodes, and rejects unsupported or stale updates. It never advances to another ayah.

## Display assets and runtime boundary

The CAF receiver source of truth lives in QuranLiveStream at `web/cast/`. It reuses QuranLiveStream's local Arabic font stylesheet and WOFF2 files plus a responsive split-stage composition inspired by its TV layout. Muslim intentionally keeps only integration documentation here; it does not mirror or package the receiver. The receiver does not import the Node broadcast worker, FFmpeg, RTMP targets, benchmark or watchdog logic.

## Receiver registration

1. Host `web/cast/receiver.html` on a stable HTTPS URL.
2. Register it as a Custom Web Receiver in Google Cast Console and enter that URL.
3. Supply the real eight-character ID to Muslim with `-PCAST_RECEIVER_APP_ID=XXXXXXXX` or environment variable `CAST_RECEIVER_APP_ID`.
4. Rebuild Muslim and verify on a physical Cast device. If unset, builds still work and Google’s standard audio receiver remains the discovery fallback; Quran display is unavailable.

## Protocol v2

Namespace: `urn:x-cast:org.muslim.quran`. State envelopes include `type` and `payload`; payload includes schemaVersion, sessionId, sequence, timestamp, surah/ayah text and metadata, reciter, audio URL, playback position/state, repeat and queue range, translation, tafsir, and prayer times. Receiver replies with `UI_READY`, `REQUEST_FULL_STATE`, `UNSUPPORTED_SCHEMA`, and `MEDIA_ERROR`.

## Privacy and offline media

Online HTTPS recitation URLs are loaded by the Cast device directly. Downloaded audio is served from app-private storage through Muslim's temporary token URL while a Cast session is active; GET, HEAD and byte ranges are supported. The listener binds to an active local IPv4 interface, and the random URL token expires after four hours or is revoked when the Cast session ends. Audio is never uploaded. The server contract has JVM coverage; physical network reachability and real-device seeking still require Cast hardware validation.

## Local QA

Run `npm test` in QuranLiveStream to execute existing quality gates and receiver protocol/order checks. In Muslim run `gradlew :feature:feature-quran:testDebugUnitTest :app:testDebugUnitTest :core:core-cast:testDebugUnitTest :app:compileDebugKotlin`.
