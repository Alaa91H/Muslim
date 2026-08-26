# Contributing to Muslim

Thank you for contributing to **Muslim**. The project combines Android engineering with religious, privacy, source-licence, accessibility, and safety considerations. A change is ready for review only when its implementation and its stated boundary are both accurate.

## Contribution areas

| Area | Examples | Required care |
|---|---|---|
| **Android engineering** | Compose UI, Room, alarms, media, performance, testing | Preserve module boundaries, lifecycle safety, device compatibility, and the project quality gates. |
| **Religious and educational content** | Quran/hadith metadata, adhkar, guides, historical summaries | Provide documented sources and preserve uncertainty, grading, translation, and scholarly-review boundaries. Code review is not a substitute for specialist review. |
| **Translations and accessibility** | Resource strings, RTL behaviour, TalkBack labels, clearer reading | Keep strings in resources, avoid hard-coded UI text, and test the affected interaction path. |
| **Privacy and integrations** | Permissions, maps, downloads, Wear, vehicle media, HTTPS bridge | Document data flow, consent/default state, storage, network endpoint, and provider limitation before implementation. |
| **Documentation** | README, policy, feature notes, changelog | Describe only implemented behaviour; state important exclusions and provider-dependent work clearly. |

## Before opening a pull request

Work from a focused branch and keep the pull request narrow enough for code and content review. Ensure the following checks are run for every relevant change:

```bash
# Build both packaged applications when a change may affect shared code.
./gradlew :app:assembleDebug :wear:assembleDebug

# Run unit tests and quality gates.
./gradlew testDebugUnitTest :wear:testDebugUnitTest
./gradlew lintDebug
./gradlew detekt

# Run feature-boundary verifiers when the related area changes.
python3 scripts/verify_hadith_paging_and_navigation.py
python3 scripts/verify_scholar_library.py
python3 scripts/verify_iot_integration.py
```

Do not silence a warning or relax a static-analysis rule simply to pass a build. Fix the cause, or document a genuinely unavoidable toolchain issue in the pull request and discuss it before merging. CI is the final authority for repeatable debug builds, lint, Detekt, unit tests, emulator checks, and signed-release builds.

## Architecture rules

The repository is multi-module. Feature modules depend on core modules, not on other feature modules. The `app` module owns application composition and top-level navigation; `wear` is a paired companion application. Keep domain logic out of composables, expose asynchronous state through lifecycle-aware flows, and avoid blocking the main thread.

For large local data sets, do not read or render an entire corpus as a single in-memory collection. The Hadith feature is the reference pattern: stream preparation off the main thread, persist in bounded batches, query through Room, and render through Paging in Compose. Preserve explicit loading, retry, and failure states whenever asynchronous preparation can fail.

Do not add a second top-level shortcut when a feature is intentionally owned by an existing hub. For example, Names of Allah and Hajj/Umrah are nested in the Learning Centre; top-level navigation should remain clear rather than duplicate those paths.

## Content, source, and licence rules

Religious and historical material requires a separate content-review path. Include the source, edition/licence status where relevant, scope, translation status, and any necessary uncertainty statement in the pull request. Do not copy a publisher edition, digital-library file, map plate, image, or third-party audio simply because an underlying classical work is old.

The Scholarly Library must not receive an unlicensed full corpus. Follow [`docs/scholar_library_content_policy.md`](docs/scholar_library_content_policy.md): imported local packs must identify the source and licence, and the product must not present starter metadata or editorial study guidance as a redistributed library.

Do not turn technical calculations into personal rulings. Travel distances, high-latitude previews, financial tools, and educational guidance must preserve their existing boundary text and, where appropriate, direct users to qualified local guidance.

## Privacy and integration rules

Start new network, permission, companion-device, or automation work with a documented data-flow boundary. Defaults should be conservative and user-controlled. Do not transmit content, location, audio, account data, or study state unless the feature explicitly requires it and the behaviour is documented.

The existing home-automation bridge is an opt-in user-configured HTTPS event receiver, not a direct Google Home/Assistant or Alexa skill. The Wear companion is paired, non-standalone, and intentionally syncs only a minimal prayer/tasbih snapshot. Android Auto exposes only already-downloaded Quran recitations while driving. Preserve these product-safety constraints.

## UI, resources, and documentation

Use Kotlin and Jetpack Compose consistently with the surrounding module. Keep user-facing text in resources; preserve RTL behaviour and use AutoMirrored icons where a directional icon requires it. Avoid deprecated APIs and resolve actionable compiler, lint, and Detekt findings instead of adding suppressions without reason.

Update documentation in the same pull request whenever behaviour changes. At minimum, update the relevant feature note under `docs/`, and update `README.md`, `PRIVACY_POLICY.md`, or `docs/PROJECT_STATUS.md` when the public product map, privacy behaviour, or project architecture changes. Add a release-note entry only when preparing the release phase.

## Pull request description

A complete pull request describes the user-visible change, affected modules, tests/verifiers run, documentation updated, content or licence impact, privacy/network impact, and limits that remain outside the change. Include reproduction steps for a bug fix and avoid screenshots or logs that expose private user data.

## Communication

Use GitHub Issues and pull requests for reproducible problems and review discussion. Repository contact details are listed in [`README.md`](README.md). By contributing, you agree that your contribution may be distributed under the repository's [GPL-3.0 licence](LICENSE).
