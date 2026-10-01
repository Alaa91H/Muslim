# Quran Cast Web Receiver

This folder contains the custom Google Cast Web Receiver that renders the current ayah, selected translation, installed tafsir entries, and prayer times supplied by the Muslim Android sender.

## Register and configure

1. Host `index.html` on a stable HTTPS origin and register a **Custom Web Receiver** in the Google Cast SDK Developer Console.
2. Put its eight-character application ID in the Android release overlay at `app/src/main/res/values/cast.xml` under `muslim_cast_receiver_application_id`. Do not use the placeholder value.
3. Add the deployed receiver URL in the Cast Console and test on a real Cast device. Debug builds use Google's public Default Media Receiver for audio; custom Quran display is enabled only when the configured ID matches the selected receiver.

The receiver accepts schema version 1 on `urn:x-cast:org.muslim.quran`. It uses text nodes for Quran and tafsir content, so streamed content cannot inject markup. Translation and tafsir language tags control reading direction. Prayer time labels arrive in the app's prayer enum language-independent form; translations can be added to the payload contract without changing Quran text.

`QuranLiveStream` is a separate server-side broadcast system. Its platform-specific stream renderer is not a Chromecast Web Receiver, so this small receiver uses Google's Cast Application Framework directly and keeps the app's versioned Quran payload contract.
