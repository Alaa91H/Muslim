# IoT & Cross-Platform Integration Sources

This note records the supported implementation boundaries for the Android Auto, Wear OS, and smart-home scope introduced for v1.21.0.

| Area | Verified implementation boundary | Primary source |
| --- | --- | --- |
| Android Auto media | The driver-facing media experience is provided by the host from a browsable media service. The app supplies a small content hierarchy, session metadata, safe transport callbacks and audio; it does not draw a custom driving UI. | [Android for Cars overview](https://developer.android.com/training/cars); [Media apps with MediaBrowserService](https://developers.google.com/cars/design/create-apps/media-apps/overview) |
| Media service | A `MediaBrowserService` declaration, media-session token, fast `onGetRoot`, and `onLoadChildren` browse tree are required for legacy browser clients. The Android documentation now recommends Media3 for new services, but this project already ships a tested `MediaSessionCompat` service and uses the compatible browser-service path. | [Building a media browser service](https://developer.android.com/media/legacy/audio/mediabrowserservice) |
| Assistant media control | Google Assistant or Gemini can control a live Android media session. Commands require accurate media metadata and supported transport callbacks; a service can answer playback search requests. | [Google Assistant and media apps](https://developer.android.com/media/implement/assistant) |
| Wear OS | Compose for Wear OS uses Wear-specific Material 3 components. The Wearable Data Layer is designed for same-app, same-signature phone/watch communication, is encrypted, and can route through Google servers when Bluetooth is unavailable. It is not a general-purpose local socket channel. | [Compose for Wear OS](https://developer.android.com/training/wearables/compose); [Wearable Data Layer](https://developer.android.com/training/wearables/data/overview) |
| Alexa smart home | Alexa Smart Home add-ons connect Alexa to a device cloud; they require endpoint discovery, account linking, authenticated directives, and a cloud endpoint such as Lambda. They cannot be fully provisioned from an Android APK alone. | [Amazon Alexa Smart Home Add-ons](https://developer.amazon.com/docs/alexaplus/smarthome/connect-your-cloud-with-addons.html) |

## Product and safety boundaries

The car catalog must expose only audio controls and offline or already-user-approved material. It must never begin downloads, show video, or require a driver to complete setup while driving. The current app contains offline Quran-recitation downloads and licensed adhan assets, but no bundled podcast audio; it must not label external links as playable podcasts.

The Wear app is a paired Android companion. Its data layer payload must contain the minimum necessary prayer snapshot and counter state, and the user must explicitly enable companion synchronization. Vibration is optional and controlled locally by the wearer.

Direct Google Home or Alexa automation needs a developer project, account-linking configuration, and an HTTPS cloud endpoint controlled by the deployer. No provider credential, webhook URL, user location, or adhan audio is sent to a third party by default. Any later home-automation adapter must be opt-in, disclose its destination, authenticate requests, and make the home speaker or automation choose how adhan playback occurs.

These features improve interoperability but do not certify Android Auto, Wear OS, Google Home, or Alexa store approval. Device, vehicle, region, companion-app, and provider policies still determine availability.
