# Accessibility Centre: Sources, Licensing, and Boundaries

## Implementation decisions

The feature is built around **user-controlled accessibility aids**, not an accessibility service or continuous microphone process. It provides a clear visual mode, a user-triggered one-shot voice command, accessible Compose controls, reading improvements for Arabic Quran and adhkar text, and external sign-language learning links.

| Area | Decision | Boundary |
| --- | --- | --- |
| TalkBack and low vision | Use Material/Compose labels, headings, non-icon-only actions, visible focusable controls, larger Arabic line height, a bundled Arabic sans font, and a high-contrast theme. | Automated UI semantics cannot certify every user journey; testing with TalkBack and Android Accessibility Scanner remains required before an official accessibility claim. |
| Voice navigation | Use Android `SpeechRecognizer` only after a deliberate press on the app’s listening control and after runtime microphone permission. Prefer on-device recognition when the device provides it; otherwise use the configured Android recognizer. The application persists no audio and no transcript. | Android documentation warns that the platform recognizer may stream audio to remote servers and is not designed for continuous recognition. Availability, offline models, languages, and privacy handling are controlled by the device service. |
| Quran/adhkar reading | Bundle Noto Sans Arabic and offer clear Arabic forms and increased line spacing in a reading mode; preserve the original Quran text, tajweed markup, and recitation flow. | This is a legibility option, **not a medical treatment and not a claim that a font cures dyslexia.** Arabic reading needs are individual and should be adjustable with system font/display settings too. |
| Sign-language learning | Open external BSL teaching videos from Al Isharah for wudu and salah; do not embed, download, copy, or imply ownership of the videos. | BSL is not Arabic Sign Language and sign languages differ by country/community. Links are supplementary education, not a universal sign-language translation nor a fiqh ruling. |

## Sources reviewed

| Topic | Source | Use |
| --- | --- | --- |
| Android accessible interfaces | [Android Developers: Build accessible apps](https://developer.android.com/guide/topics/ui/accessibility) | Used to guide explicit content labels, accessible interaction flows, contrast and testing-oriented design. |
| Android speech recognition | [Android Developers: `SpeechRecognizer`](https://developer.android.com/reference/android/speech/SpeechRecognizer) | Confirms runtime microphone permission, explicit lifecycle destruction, optional on-device recognizer, Android 11 recognition-service query, errors, and the warning that a service may stream audio remotely. |
| Arabic reading font | [Google Fonts: Noto Sans Arabic](https://fonts.google.com/noto/specimen/Noto+Sans+Arabic) | Official font source; describes the Arabic sans family and its broad glyph coverage. |
| Font licence | [SIL Open Font License 1.1](https://openfontlicense.org/) | Noto Sans Arabic is distributed under OFL 1.1; the application bundles the official Google Fonts binary and retains this source note. |
| Wudu in BSL | [Al Isharah: How to make Wudhu in BSL](https://www.youtube.com/watch?v=VwqROPP-dq0) | External link only. The public video description identifies Al Isharah and says it is a BSL wudu tutorial. |
| Salah in BSL | [Al Isharah: How to pray Salah in BSL](https://www.youtube.com/watch?v=gaO3dbYxWcE) | External link only. The page title identifies it as a BSL salah tutorial. |

## Privacy and content review

No microphone session starts in the background. The voice feature remains disabled until the user enables it from the Accessibility Centre and then taps the listen control. Recognized candidate text exists in memory only long enough to match a navigation command, then is discarded. The app tells the user that a configured Android recognizer can be network-backed.

The spoken-navigation vocabulary should be tested with Arabic and English speakers, TalkBack users, and different Android recognizers. Quran and religious learning content should receive specialist review independently from code review. The BSL links should be periodically checked for availability, creator attribution, captions, and continuing relevance before any official accessibility release.
