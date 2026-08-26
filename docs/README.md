# Muslim Documentation

This directory holds implementation-boundary notes and product documentation for the Muslim Android project. Read these documents with the code and release changelog: they describe the current implementation and intentional limits, not guarantees of provider, store, scholarly, legal, security, or accessibility certification.

## Start here

| Document | Audience | Purpose |
|---|---|---|
| [`../README.md`](../README.md) | Users, contributors, reviewers | Product overview, feature map, architecture, build, test, and release instructions. |
| [`PROJECT_STATUS.md`](PROJECT_STATUS.md) | Reviewers and maintainers | Detailed implemented-feature status, navigation organisation, data flows, quality process, and known boundaries. |
| [`../PRIVACY_POLICY.md`](../PRIVACY_POLICY.md) | Users and privacy reviewers | Local data, permissions, optional networking, Wear, Android Auto, and home-bridge handling. |
| [`../CONTRIBUTING.md`](../CONTRIBUTING.md) | Contributors | Code, content, privacy, documentation, navigation, and verification expectations. |

## Feature notes

| Document | Covered feature boundary |
|---|---|
| [`accessibility_feature_sources.md`](accessibility_feature_sources.md) | Accessibility centre, clearer Arabic reading, voice navigation, and supplementary sign-language links. |
| [`iot_feature_sources.md`](iot_feature_sources.md) | Android Auto media browsing, paired Wear OS data flow, and the opt-in HTTPS home-automation bridge. |
| [`islamic_visual_identity.md`](islamic_visual_identity.md) | Modern Islamic Minimalism tokens, vector ornaments, reader quietness, accessibility, and verification. |
| [`qa/notification_identity_repair.md`](qa/notification_identity_repair.md) | Current Android launcher/status-bar identity migration, retained notification cleanup, verification scope, and upgrade note. |
| [`islamic_finance_feature_sources.md`](islamic_finance_feature_sources.md) | Educational finance material, screening-provider boundaries, and local debt tracking. |
| [`islamic_history_civilization_feature_sources.md`](islamic_history_civilization_feature_sources.md) | Timeline, schematic atlas, and historical-content source/claim limits. |
| [`noorani_new_muslim_feature_sources.md`](noorani_new_muslim_feature_sources.md) | Noorani-style learning, device TTS, and New-Muslim educational content. |
| [`scholar_library_content_policy.md`](scholar_library_content_policy.md) | Starter catalogue, citations, local notes/flashcards, and authorised content-pack import rules. |
| [`traveler_expat_feature_sources.md`](traveler_expat_feature_sources.md) | Travel-distance reference, transport guidance, compass, and high-latitude explanatory limits. |

## Maintenance rule

When a change alters public behaviour, data handling, a provider integration, source/licence status, navigation ownership, or a user-visible limit, update the relevant document in the same pull request. Keep the language specific: name what the feature does, what it stores or transmits, whether it is opt-in, and what it explicitly does **not** provide.
