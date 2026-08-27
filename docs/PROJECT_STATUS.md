# Muslim Project Status and Documentation Map

> **Status date:** 27 August 2026
> **Scope:** This document describes the code and product boundaries present on the `main` development line. It is not a store-approval statement, a security certification, or a religious ruling.

## Purpose

Muslim is organised as a local-first Android application for daily worship, Quran and hadith access, study, learning, practical planning, and selected companion-device workflows. This document is the detailed implementation map behind the repository README. It distinguishes delivered behaviour from future/provider-dependent work so that contributors and users can evaluate the project honestly.

## Navigation and information architecture

The main application normally has four persistent destinations: Prayer Times, Quran, Qibla, and More. During the adjusted Umm al-Qura month of Ramadan, Ramadan is inserted between Quran and Qibla, giving four primary content destinations plus More; outside Ramadan its existing More shortcut remains. More is the secondary hub for the rest of the product. Its groups are user-orderable and hideable, while each individual destination retains a single clear responsibility.

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
| Prayer times and Adhan | A local immutable calculation profile is shared by the prayer home, countdown, widget, Adhan scheduler, notification preview, Ramadan and travel preview. Its global baseline is MWL with Standard Asr, Seventh of the Night, Isha 17° and zero default user offsets; methods, Hanafi Asr, other high-latitude rules, custom angles and per-prayer offsets remain explicit settings. Saved cities retain IANA zones; GPS/manual coordinates resolve IANA locally before persistence. GPS accepts approximate/precise foreground access and uses current fused/platform fixes with local fallback; provider, geocoding, timezone, persistence and rescheduling failures become a recoverable location state at both provider and ViewModel boundaries. The synthesized Adhan fallback serializes each `AudioTrack` native operation with ownership invalidation and release, ending a platform release race safely. A scheduled-row alert icon opens an in-place responsive customisation dialog with a saved comfortable/compact density preference. The next-prayer time is green and remaining/elapsed values red. A live foreground service owns the non-dismissible public lock-screen card and reaffirms it if the app task is removed. | Sunrise remains excluded from Adhan scheduling. Output is a configurable astronomical calculation, not a personalised ruling or a guarantee of delivery under every OS battery, channel, permission or lock-screen policy. The in-notification Stop Adhan action is the supported rapid locked-device control; Android owns hardware volume-key routing and final lock-screen presentation. Users and communities remain responsible for selecting the convention appropriate to their practice. |
| Quran | Offline mushaf experience, bookmarks/progress, search/frequency tools, and user-managed recitation playback/downloads with a calm shared hierarchy for library summaries, coverage, reciter state and transfers. | Provider content and network terms apply to optional downloads. |
| Hadith | An offline catalogue presents verified public-domain physical-volume and historical-manuscript imagery, collection cards and chapter indexes for nine versioned local collections. In an Arabic UI, every bundled collection-scoped chapter title uses its Arabic display label while the source title remains the Room/Paging/navigation key. Arabic-normalised FTS search, bookmarks, sharing/copying and daily-Hadith support remain available only after the relevant book exists locally. | Search returns corpus text and metadata, not a comprehensive scholarly judgement about authenticity/context. Source/licence/review records remain tracked per bundled asset; general physical-volume imagery is not represented as a specific publisher edition. Chapter display localization is an interface layer, not a claimed scholarly edition. |
| Hadith performance model | Entering the catalogue imports no text. Selecting a book streams its GZIP NDJSON asset on `Dispatchers.IO` when available or Android’s unpacked NDJSON asset representation when the package removes the `.gz` suffix; the same 150-row Room batches, chapter/FTS indexes, and Paging 3 result remain in use. | Preparation progress and retry states are user-visible; no complete multi-book corpus list is held by the Hadith UI state and opening another book does not preload it. Technical asset paths and exceptions are not shown to readers. |
| Adhkar and Tasbih | Local counters, optional feedback/reminders, logs and widget support, with calm content surfaces and a single accessible primary Tasbih counting action. | Reminder/overlay availability remains subject to Android permissions and system restrictions. |
| Ramadan planning | Iftar/Suhoor timing and reminders, fasting-day tracker, reusable habit tracker, prayer completion and local Khatma/Taraweeh/Itikaf planning are presented through shared planning, section and recovery surfaces. Ramadan becomes a lower-navigation destination only during the locally calculated, user-adjusted Hijri month 9. | Fasting records, reminders, prayer completion and plan state remain local feature-owned data. No background worker or network request is required to move the seasonal navigation item. |
| Learning and reference | Local guides, Names of Allah, Hajj/Umrah, reference content, history/timeline/atlas, family and funeral/will materials, presented through grouped knowledge destinations and structured reading steps. The reference hub, search-empty state and paragraph reader use the shared scholarly hierarchy. | Religious and historical material is educational and requires source/specialist review outside software CI. |
| Hajj-day planning | A Hijri-date calculator presents its introduction, entered date and calculated Arafah, Nahr and Tashreeq dates through shared result surfaces, with a clear critical state for incomplete or invalid input. | Digit normalization, date parsing, calendar conversion, relative-day labels and the underlying seasonal calculation remain feature-owned and unchanged. |
| Family Life | Ruqyah passages and audio rows, baby-name search, Aqiqah planning/reminders and family guidance use shared cards, semantic notice surfaces and an accessible secondary action. | Bilingual content selection, safe-audio URL validation, external audio launch, name searching, date parsing, schedule calculation and reminder persistence remain feature-owned. |
| Scholarly Library | Starter catalogue, Arabic-normalised local search, citation fields, notes, flashcards, and authorised local pack import. | No third-party digital library, publisher edition, or automatically downloaded corpus is represented as bundled content. |
| Travel and expatriate tools | Travel-distance controls and assessment, local compass, transport orientation and high-latitude planning use shared cards, semantic notices, a critical GPS-failure state and accessible actions. | GPS permissions, saved origin, distance assessment, Qibla calculation, high-latitude preview and prayer-settings navigation remain unchanged; a distance or calculation output is not a personal verdict on qasr, jam', or another fiqh question. |
| Finance and Zakat | Educational transactions guide, provider shortcuts for external screening, local debt log, and a Zakat calculator with semantic price-fetch, Nisab, history and primary-action surfaces. | It is not investment advice, a screening ruling, a financial service, or a substitute for personal religious guidance. |
| Accessibility | High contrast, clearer Arabic reading, labelled controls, one-shot voice navigation and supplementary external links. | The implementation is not a certification of complete accessibility compliance or sign-language universality. |
| Android Auto | Media browsing/control and voice search for locally complete Quran recitations. | No driving-time download, video, or general interactive content. |
| Wear OS | Opt-in paired countdown/tasbih companion with a glanceable next-prayer hierarchy, explicit haptic-state feedback, local watch snapshot, and filtered increment request. | Non-standalone; phone is authoritative; no location/account/audio snapshot. |
| Home automation | Optional HTTPS event after local audible Adhan begins; endpoint/token settings under user control. | Not a direct Google Home/Assistant or Alexa Skill/Action. |

## Hadith data flow

The historic crash/freeze risk was addressed by removing the eager `readText()` + complete `List` parsing path and replacing the single corpus asset with independently versioned compressed book assets. Selecting a collection follows this sequence:

1. The catalogue reads fixed collection metadata only; it does not begin a text import.
2. The repository checks the selected collection version and its compressed asset.
3. It opens a streaming gzip reader on an I/O dispatcher, decodes one line at a time, and stores rows and FTS rows in fixed-size 150-row batches while emitting visible selected-book progress.
4. It derives and stores the selected book's chapter index; Room invalidates only its book-scoped Paging source as local data becomes available.
5. The ViewModel exposes a debounced `Flow<PagingData<Hadith>>`, cached in `viewModelScope`, filtered by the active collection/chapter/search state.
6. Compose consumes `LazyPagingItems`, renders stable row keys, and exposes refresh/append retries rather than terminating the screen.

The daily-Hadith worker remains inactive until at least one selected book is available locally; it does not import every book. The implementation aligns with Android's recommended Paging source → Pager → `LazyPagingItems` layering. [Android Paging overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) and [paged-data guide](https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data) provide the framework reference.

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
python3 scripts/verify_hadith_chapter_localization.py
python3 scripts/verify_scholar_library.py
python3 scripts/verify_iot_integration.py
python3 scripts/verify_accessibility.py
python3 scripts/verify_islamic_visual_identity.py
python3 scripts/verify_prayer_calculation_integrity.py
python3 scripts/verify_prayer_location_notification_contract.py
python3 scripts/verify_responsive_customization_layout.py
python3 scripts/verify_design_system_adoption.py
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
| [`content/hadith_cover_source_candidates.md`](content/hadith_cover_source_candidates.md) | Provenance and representation boundaries for the public-domain Hadith catalogue imagery. |
| [`content/hadith_chapter_title_localization.md`](content/hadith_chapter_title_localization.md) | Source, coverage, and representation limits for Arabic chapter-index labels. |
| [`design/premium_islamic_design_system.md`](design/premium_islamic_design_system.md) | Central theme, surface, Arabic/RTL, motion, accessibility, and adaptive-layout usage rules. |
| [`content/owner_rights_attestation.md`](content/owner_rights_attestation.md) | Product-owner distribution-rights attestation recorded for the current public release. |
| [`privacy/data_inventory.md`](privacy/data_inventory.md) | Local data, permissions, endpoints, and Data safety review baseline. |
| [`qa/p0_test_matrix.md`](qa/p0_test_matrix.md) | Required physical-device acceptance tests for critical worship paths. |
| [`qa/accessibility_release_checklist.md`](qa/accessibility_release_checklist.md) | Accessibility release checks for TalkBack, Switch Access, and scalable RTL UI. |
| [`qa/notification_identity_repair.md`](qa/notification_identity_repair.md) | Android launcher/status-bar identity migration, retained-card cleanup, and release verification limits. |
| [`qa/prayer_time_calculation_integrity.md`](qa/prayer_time_calculation_integrity.md) | Prayer calculation audit, reference vectors, profile contract, IANA resolution and verification limits. |
| [`qa/lazy_hadith_location_notification_audit.md`](qa/lazy_hadith_location_notification_audit.md) | Lazy per-book Hadith loading, GPS/IANA recovery, Isha baseline, Ramadan navigation, notification semantics, responsive customisation, lock-screen Adhan control, size audit and verification boundaries. |
| [`release/beta_test_charter.md`](release/beta_test_charter.md) | Scope, limits, and acceptance criteria for the invited closed beta. |
| [`release/closed_beta_distribution.md`](release/closed_beta_distribution.md) | Stable-signing, CI artifact, and invited-tester distribution workflow. |
| [`qa/beta_tester_guide.md`](qa/beta_tester_guide.md) | Tester installation, adhan verification, and privacy-preserving feedback guide. |

## What this project does not claim

The codebase does not claim to be a substitute for qualified scholarship, a universal fatwa source, a licensed redistribution of every classical text or audio work, a direct provider smart-home integration, a vehicle/watch/store certification, or a guarantee that the operating system will execute every scheduled action under all conditions. Those limits are intentional product-safety constraints, not omitted features hidden by documentation.
