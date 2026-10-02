# Muslim Google Cast receiver

The custom CAF Web Receiver is maintained in QuranLiveStream at `web/cast/receiver.html`. This repository contains the Muslim Android sender and its protocol/deployment documentation in `receiver/README.md`. QuranLiveStream contributes receiver layout, local Arabic fonts, and responsive Quran presentation; Muslim remains the only playback and queue authority.

The receiver requires HTTPS hosting and a real Custom Web Receiver ID from Google Cast Console. Configure the ID in Muslim with `CAST_RECEIVER_APP_ID`; no placeholder ID is treated as a working registration.
