# Muslim Project Status and Documentation Map

> **Status date:** 27 August 2026
> **Scope:** This document describes the code and product boundaries present on the `main` development line. It is not a store-approval statement, a security certification, or a religious ruling.

## Purpose

Muslim is organised as a local-first Android application for daily worship, Quran and hadith access, study, learning, practical planning, and selected companion-device workflows. This document is the detailed implementation map behind the repository README. It distinguishes delivered behaviour from future/provider-dependent work so that contributors and users can evaluate the project honestly.

## Navigation and information architecture

The main application has four persistent destinations: Prayer Times, Quran, Qibla, and More. More is the secondary hub for the rest of the product. Its groups are user-orderable and hideable, while each individual destination retains a single clear responsibility.

| More hub group | Primary destinations | Organisation decision |
|---|---|---|
| **Worship** | Adhkar, Tasbih, Ramadan, Habits, Zakat, Prayer settings | Daily worship and scheduling utilities remain together. |
| **Knowledge** | Hadith, Learning Centre, Noorani/New Muslim, Traveller/Expat, Family Life, Funeral/Will, Reference, History, Scholarly Library | The Learning Centre owns the integrated Names of Allah and Hajj/Umrah paths; their duplicate top-level shortcuts were removed. |
| **Tools** | Islamic finance, Quran downloads, Quran search/frequency, offline maps | User-directed utilities and downloads are separated from religious study content. |
| **App** | Accessibility, notifications, permissions, privacy, smart devices, update controls, More-hub ordering | Application-level configuration is separate from worship and study tasks. |

This consolidation preserves all implemented destinations while reducing repeated entry points. It also allows the Learning Centre to manage its own nested back navigation for Names of Allah and Hajj/Umrah.

## Feature inventory

| Capability | Current implementation | Important boundary |
|---|---|---|
| Prayer times and Adhan | Local calculation, method/school/high-latitude preferences, saved or one-time location, scheduled notifications and playback controls. | Calculations and guides are technical/educational aids, not personalised rulings or a guarantee of delivery under every OS battery policy. |
| Quran | Offline mushaf experience, bookmarks/progress, search/frequency tools, and user-managed recitation playback/downloads with a calm shared hierarchy for library summaries, coverage, reciter state and transfers. | Provider content and network terms apply to optional downloads. |
| Hadith | Offline corpus preparation, collection browse, Arabic-normalised FTS search, bookmarks, sharing/copying, daily notification. | Search returns corpus text and metadata, not a comprehensive scholarly judgement about authenticity/context. |
| Hadith performance model | `hadith_full.ndjson.gz` is streamed on `Dispatchers.IO`, inserted into Room in 150-row batches, and shown through Paging 3 with 24-row pages and a prefetch window. | Preparation progress and retry states are user-visible; no complete corpus list is held by the Hadith UI state. |
| Adhkar and Tasbih | Local counters, optional feedback/reminders, logs and widget support, with calm content surfaces and a single accessible primary Tasbih counting action. | Reminder/overlay availability remains subject to Android permissions and system restrictions. |
| Ramadan planning | Iftar/Suhoor timing and reminders, fasting-day tracker, reusable habit tracker, prayer completion and local Khatma/Taraweeh/Itikaf planning are presented through shared planning, section and recovery surfaces. | Fasting records, reminders, prayer completion and plan state remain local feature-owned data. |
| Learning and reference | Local guides, Names of Allah, Hajj/Umrah, reference content, history/timeline/atlas, family and funeral/will materials, presented through grouped knowledge destinations and structured reading steps. The reference hub, search-empty state and paragraph reader use the shared scholarly hierarchy. | Religious and historical material is educational and requires source/specialist review outside software CI. |
| Scholarly Library | Starter catalogue, Arabic-normalised local search, citation fields, notes, flashcards, and authorised local pack import. | No third-party digital library, publisher edition, or automatically downloaded corpus is represented as bundled content. |
| Travel and expatriate tools | Distance reference, local qibla with a semantic compass hierarchy and calibration/recovery states, transport orientation, high-latitude explainer. | A distance or calculation output is not a personal verdict on qasr, jam', or another fiqh question. |
| Finance and Zakat | Educational transactions guide, provider shortcuts for external screening, local debt log, and a Zakat calculator with semantic price-fetch, Nisab, history and primary-action surfaces. | It is not investment advice, a screening ruling, a financial service, or a substitute for personal religious guidance. |
| Accessibility | High contrast, clearer Arabic reading, labelled controls, one-shot voice navigation and supplementary external links. | The implementation is not a certification of complete accessibility compliance or sign-language universality. |
| Android Auto | Media browsing/control and voice search for locally complete Quran recitations. | No driving-time download, video, or general interactive content. |
| Wear OS | Opt-in paired countdown/tasbih companion with a glanceable next-prayer hierarchy, explicit haptic-state feedback, local watch snapshot, and filtered increment request. | Non-standalone; phone is authoritative; no location/account/audio snapshot. |
| Home automation | Optional HTTPS event after local audible Adhan begins; endpoint/token settings under user control. | Not a direct Google Home/Assistant or Alexa Skill/Action. |

## Hadith data flow

The historic crash/freeze risk was addressed by removing the eager `readText()` + complete `List` parsing path for the large full corpus from the phone experience. The full asset is now line-delimited and gzip-compressed. Startup preparation follows this sequence:

1. The repository checks the locally stored corpus version and the compressed asset.
2. It opens a streaming gzip reader on an I/O dispatcher.
3. It decodes one line at a time, stores rows and FTS rows in fixed-size batches, and emits visible import progress.
4. Room invalidates its Paging source as local data becomes available.
5. The ViewModel exposes a debounced `Flow<PagingData<Hadith>>`, cached in `viewModelScope`.
6. Compose consumes `LazyPagingItems`, renders stable row keys, and exposes refresh/append retries rather than terminating the screen.

A deterministic daily hadith now calculates a row offset from the corpus count and queries one row. It does not call `observeAll()` or materialise the entire table. The implementation aligns with Android's recommended Paging source → Pager → `LazyPagingItems` layering. [Android Paging overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) and [paged-data guide](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data) provide the framework reference.

## Storage, privacy, and network map

| Data / event | Local handling | External handling |
|---|---|---|
| Prayer preferences, Quran progress, tasbih, debts, notes, flashcards | App-private Room/DataStore storage. | None by default. |
| Hadith corpus | Packaged compressed asset, then local Room/FTS. | None for preparation or browsing. |
| Quran media and translations | User-managed local files after optional download. | Only the selected provider request, when user initiates it. |
| Maps/mosques | Cached/local map state where supported. | Optional map tile/search provider request. |
| Wear snapshot | Minimal versioned Data Layer payload after opt-in. | Paired Google Play services Data Layer only. |
| Home event | Endpoint preference local; optional secret in Android Keystore. | User-selected HTTPS receiver, with minimal `adhan_started` event only when enabled. |

See [`../PRIVACY_POLICY.md`](../PRIVACY_POLICY.md) for the full policy and permission explanation.

## Modules

```text
app                         phone application, navigation, application composition
wear                        paired Wear OS companion
core:core-common            shared models and text/time utilities
core:core-design-system     theme and design primitives
core:core-ui                shared Compose UI helpers
core:core-database          database infrastructure
core:core-datastore         preferences and local settings
core:core-network           optional network clients
core:core-notifications     notification scheduling/control
core:core-location          location support
core:core-permissions       user-visible permission orchestration
feature:*                   independently scoped product capabilities
```

Feature-to-feature dependencies are avoided; the app module owns composition and navigation. The principal stack is Kotlin, Compose Material 3, Hilt, Coroutines/Flow, Room, DataStore, WorkManager, AlarmManager, OkHttp/Retrofit where a feature opts into network access, and Paging 3 for the large Hadith lists.

## Android system identity and retained-notification migration

The `main` branch uses a full-colour geometric Islamic mark for its `v2028` adaptive launcher and round-launcher resources, while all notification producers use a separate monochrome `v2028` small-icon vector. This distinction is intentional: Android owns the tint and final rendering of a status-bar small icon, whereas the launcher uses the full-colour identity.

| System surface | Migration behaviour | Boundary |
|---|---|---|
| Active Adhan | Retires `1001`, `1005`, `1010`, and `1012`; posts the current `1014` card as a non-dismissible ongoing foreground notification with one Stop Adhan action, and retires the earlier reminder. | The fresh `adhan_alert_v3` channel defaults to high importance; final lock-screen, heads-up, permission, and user-edited channel behaviour remains Android-controlled. |
| Next-prayer countdown | Retires `1003`, `1004`, `1011`, and `1013`; posts the current card as `1015`. | The card is a quiet system status surface, not a custom full-colour notification layout. |
| Quran recitation | Retires media cards `7006` and `7007`; the foreground service posts media card `7008`. | Android controls `MediaStyle` layout and lock-screen treatment. |

An internal package-replaced receiver clears all listed retired cards immediately after an in-place update; application startup also repeats the cleanup. The Adhan and countdown service paths repeat their cleanup before current work is published, while the Quran playback service does so during service creation. A live Adhan declares public lock-screen visibility, high-priority presentation, and immediate foreground-service behaviour. Settings previews track a separate state and cannot stop a live scheduled Adhan; the live card ends through natural audio completion or its explicit Stop Adhan action. The migration does not provide a custom large icon to these notification builders; system templates may show the new application identity independently. Detailed implementation and verification limits are in [`qa/notification_identity_repair.md`](qa/notification_identity_repair.md).

## Quality and release process

The project contains both focused unit tests and static boundary verifiers. Current targeted commands include:

```bash
python3 scripts/verify_hadith_paging_and_navigation.py
python3 scripts/verify_scholar_library.py
python3 scripts/verify_iot_integration.py
python3 scripts/verify_accessibility.py
python3 scripts/verify_islamic_visual_identity.py
./gradlew testDebugUnitTest :wear:testDebugUnitTest
./gradlew lintDebug
./gradlew detekt
```

GitHub Actions runs debug builds, unit tests, Android Lint, Detekt, and emulator tests. A signed release job builds both the phone and Wear APKs. Pushing an annotated `v*` tag runs the release path and publishes both artifacts when the workflow succeeds. This does not certify physical-device, Android Auto host, Wear hardware, accessibility-assistive-technology, store, provider, legal, licence, or scholarly approval.

## Documentation index

| Document | Scope |
|---|---|
| [`../README.md`](../README.md) | Repository entry point, implementation overview, build/release instructions. |
| [`../PRIVACY_POLICY.md`](../PRIVACY_POLICY.md) | Data, permissions, optional network actions, Wear, bridge, and security boundaries. |
| [`../CONTRIBUTING.md`](../CONTRIBUTING.md) | Contribution, module, content-review, and quality expectations. |
| [`accessibility_feature_sources.md`](accessibility_feature_sources.md) | Accessibility, clearer Arabic reading, voice navigation, and external sign-language-link decisions. |
| [`iot_feature_sources.md`](iot_feature_sources.md) | Android Auto, Wear Data Layer, and HTTPS bridge decisions. |
| [`islamic_finance_feature_sources.md`](islamic_finance_feature_sources.md) | Finance educational/screening boundaries. |
| [`islamic_history_civilization_feature_sources.md`](islamic_history_civilization_feature_sources.md) | Historical timeline and schematic atlas source/boundary notes. |
| [`noorani_new_muslim_feature_sources.md`](noorani_new_muslim_feature_sources.md) | Noorani-style and New-Muslim implementation/source decisions. |
| [`scholar_library_content_policy.md`](scholar_library_content_policy.md) | Library starter content and authorised import policy. |
| [`traveler_expat_feature_sources.md`](traveler_expat_feature_sources.md) | Travel, transport, qibla, and high-latitude guidance boundaries. |
| [`release/v1_scope.md`](release/v1_scope.md) | Scope, launch gates, supported surfaces, and product decisions for v1. |
| [`release/claim_register.md`](release/claim_register.md) | Approved product claims and the evidence required before store or UI use. |
| [`release/release_governance.md`](release/release_governance.md) | Approval roles, release-branch rules, and P0/P1/P2 release decisions. |
| [`release/play_console_checklist.md`](release/play_console_checklist.md) | Play Console, signing, AAB, listing, and staged-rollout readiness checklist. |
| [`release/release_runbook.md`](release/release_runbook.md) | Production artifact, closed-test, rollout, rollback, and secret-management workflow. |
| [`release/operations_support.md`](release/operations_support.md) | Support triage, hotfix, and post-release operating model. |
| [`content/README.md`](content/README.md) | Content source, licence, hash, and independent-review approval workflow. |
| [`content/owner_rights_attestation.md`](content/owner_rights_attestation.md) | Product-owner distribution-rights attestation recorded for the current public release. |
| [`privacy/data_inventory.md`](privacy/data_inventory.md) | Local data, permissions, endpoints, and Data safety review baseline. |
| [`qa/p0_test_matrix.md`](qa/p0_test_matrix.md) | Required physical-device acceptance tests for critical worship paths. |
| [`qa/accessibility_release_checklist.md`](qa/accessibility_release_checklist.md) | Accessibility release checks for TalkBack, Switch Access, and scalable RTL UI. |
| [`qa/notification_identity_repair.md`](qa/notification_identity_repair.md) | Android launcher/status-bar identity migration, retained-card cleanup, and release verification limits. |
| [`release/beta_test_charter.md`](release/beta_test_charter.md) | Scope, limits, and acceptance criteria for the invited closed beta. |
| [`release/closed_beta_distribution.md`](release/closed_beta_distribution.md) | Stable-signing, CI artifact, and invited-tester distribution workflow. |
| [`qa/beta_tester_guide.md`](qa/beta_tester_guide.md) | Tester installation, adhan verification, and privacy-preserving feedback guide. |

## What this project does not claim

The codebase does not claim to be a substitute for qualified scholarship, a universal fatwa source, a licensed redistribution of every classical text or audio work, a direct provider smart-home integration, a vehicle/watch/store certification, or a guarantee that the operating system will execute every scheduled action under all conditions. Those limits are intentional product-safety constraints, not omitted features hidden by documentation.
