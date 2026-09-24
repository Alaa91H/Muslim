# Project health baseline

Date: 2026-09-24  
Baseline branch: `main`  
Baseline commit: `c81688544eb3fd97e9882476b3854f0190ca6a63`

## Purpose

This document starts the stability and architecture phase of the Muslim roadmap. It
records concrete repository observations before refactoring so future changes can be
measured against a known baseline.

## Current architecture snapshot

The repository is already a Kotlin/Jetpack Compose multi-module Android application
with a phone app, a paired Wear OS app, 9 core modules, and 14 feature modules.

The intended dependency rule in `settings.gradle.kts` is:

> Feature modules depend on core modules, not on other feature modules.

The baseline currently contains three direct feature-to-feature dependency edges:

- `:feature:feature-learn -> :feature:feature-qibla`
- `:feature:feature-settings -> :feature:feature-hadith`
- `:feature:feature-settings -> :feature:feature-learn`

These are now treated as temporary legacy exceptions. CI rejects any new feature edge
and also rejects stale allowlist entries after an existing edge is removed.

## First remediation sequence

1. **Learning -> Qibla**
   - Move reusable Qibla calculation contracts/math to an appropriate core module.
   - Move or abstract reusable compass-heading infrastructure so Learning does not
     import Qibla UI implementation details.
   - Remove the `feature-learn -> feature-qibla` dependency and its allowlist entry.

2. **Settings -> Hadith / Learning**
   - Introduce cross-feature notification/settings contracts in core infrastructure.
   - Keep feature-owned scheduling implementations inside their features.
   - Compose those implementations from the app layer (or a core contract binding)
     rather than importing feature implementation classes into Settings.
   - Remove both Settings feature edges and their allowlist entries.

3. **Keep the boundary permanent**
   - Any new shared model, service contract, or reusable UI primitive must move to the
     narrowest appropriate core module instead of creating another feature dependency.

## Quality baseline

The existing CI already runs build, unit tests, Android Lint, Detekt, content
verification, accessibility/design verifiers, and emulator instrumentation suites.
The architecture verifier is added to the fast quality job so invalid dependency
direction is rejected before the expensive build and emulator stages.

## Refactor candidates observed during the baseline

The following large implementation files should be split during later focused phases,
without mixing those changes into the dependency-boundary work:

- Quran reader UI: `QuranReaderScreen.kt`
- Hajj/Umrah content: `HajjUmrahContent.kt`
- Names of Allah content: `NamesOfAllah.kt`
- Settings UI: `SettingsScreen.kt`

File size alone is not treated as a defect; these are review targets because they
concentrate substantial responsibilities and therefore carry higher regression risk.

## Exit criteria for the architecture phase

The phase is complete when:

- no feature module directly depends on another feature module;
- the temporary allowlist in `scripts/verify_module_boundaries.py` is empty;
- CI continues to pass build, unit, lint, Detekt, and emulator gates;
- shared contracts live in core modules with explicit ownership;
- user-visible behavior for Quran, prayer calculations, notifications, and stored
  preferences remains unchanged unless a dedicated feature change explicitly says so.


## Progress

### 2026-09-24 — Learning/Qibla boundary removed

The first legacy feature edge was removed without changing user-visible compass
behaviour:

- Qibla bearing/distance/route math now lives in `core-common`.
- Compass posture validation and heading smoothing now live in `core-common`.
- The Compose sensor adapter now lives in `core-ui`.
- Qibla and Learning consume the same shared implementation.
- `:feature:feature-learn -> :feature:feature-qibla` was removed from Gradle and
  from the temporary architecture allowlist.

Two legacy feature edges remain, both owned by Settings notification coordination.


### 2026-09-24 — Settings feature boundaries removed

The remaining direct feature-to-feature dependencies were replaced by core-owned
notification coordination contracts:

- `feature-settings` no longer imports Hadith repositories/schedulers.
- `feature-settings` no longer imports the Hajj scheduler from Learning.
- Hadith contributes its timed-notification settings through Hilt.
- Learning contributes its Hajj enable/disable side effect through Hilt.
- `FeatureNotificationCoordinator` in `core-notifications` dispatches those
  integrations without owning feature data or workers.
- the architecture allowlist is now empty.

At this point, feature modules depend on core modules rather than directly on other
feature modules, matching the repository's intended module boundary.
