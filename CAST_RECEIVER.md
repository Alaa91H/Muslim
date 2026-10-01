# Muslim Google Cast receiver in QuranLiveStream

This repository hosts the custom CAF Web Receiver at `web/cast/receiver.html`. It reuses QuranLiveStream typography and responsive visual ideas only. Muslim remains the playback master and queue authority. Receiver session and schema details, deployment, privacy and testing are documented in Muslim's `receiver/README.md`.

The custom receiver must be hosted over HTTPS and registered in Google Cast Console to obtain a real receiver application ID. The Android sender accepts that value via `CAST_RECEIVER_APP_ID`; no fake ID is bundled.
