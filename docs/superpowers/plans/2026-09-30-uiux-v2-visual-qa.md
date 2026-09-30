# UI/UX V2 Visual QA Implementation Plan

> **For agentic workers:** execute task by task with TDD; do not claim screenshot comparison without a reviewed baseline.

**Goal:** Produce reproducible Arabic/light and English/dark emulator screenshots for the Prayer Home Compose surface and publish them as CI artifacts, while accurately tracking remaining UX28/UX31 visual gates.

**Architecture:** Add a Hilt-backed app Activity instrumentation capture for Prayer Home using Android `UiAutomation`, then upload screenshots alongside emulator test evidence. Update the QA matrix verifier and plan so they distinguish captured artifacts from screenshot comparison and manual variant review.

**Tech Stack:** Kotlin, Android instrumentation, Android emulator, GitHub Actions, Python static verifier.

**Spec:** `docs/design/ui_ux_v2_execution_plan.md` UX27–UX31 and `docs/design/ui_ux_v2_feature_parity_baseline.md`.

## Global Constraints

- Preserve all functional behavior and religious content.
- Keep Arabic RTL and English LTR, light/dark, font scales, and compact/expanded device review first-class.
- Do not mark UX28 complete based only on screenshot creation; comparison and reviewed baselines remain required.

## Review Focus

- Emulator screenshot capture must wait for Compose content and produce a non-empty PNG.
- CI artifacts must be uploaded on success and failure for both API levels.
- Documentation must not describe CI emulator screenshots as coverage of all variants.

### Task 1: Capture a deterministic Prayer Home emulator screenshot

**Files:** create `app/src/androidTest/java/org/muslim/app/UiUxV2ScreenshotInstrumentedTest.kt`.

- [x] Add instrumentation tests that launch the Hilt-backed `MainActivity` and write Prayer Home Arabic/light and English/dark PNGs to app-private files.
- [x] Run the instrumentation test in CI and inspect the uploaded PNG dimensions/content (run #1342 passed; API 26 and API 36 artifacts are valid 320x640 PNGs).
- [x] Keep capture isolated from production code and user data.

### Task 2: Publish screenshot artifacts from CI

**Files:** modify `.github/workflows/ci.yml` emulator job.

- [x] Export the app-private PNG before the emulator runner shuts the device down; upload after runner completion.
- [x] Verify the workflow's artifact paths using the QA matrix static check.

### Task 3: Close only the automation portion of UX28

**Files:** modify `docs/design/ui_ux_v2_execution_plan.md`, `docs/design/ui_ux_v2_feature_parity_baseline.md`, `scripts/verify_uiux_v2_qa_matrix.py`.

- [x] Document the configured capture/export path and retain comparison/variant/manual QA gates.
- [x] Add a static contract that asserts emulator screenshot export/upload configuration and distinguishes capture from visual comparison.
- [x] Run the QA matrix verifier and verify end-to-end CI after extending screenshot polling to 10 minutes (run #1342 passed).

### Task 4: Review unobscured locale/theme captures and compact date layout

**Files:** `UiUxV2ScreenshotInstrumentedTest.kt`, `HomeScreen.kt`, CI screenshot capture and QA matrix contract.

- [x] Add Arabic/dark and English/light captures alongside existing variants.
- [x] Mark initial system permission onboarding handled in test setup and disable dynamic color for reproducible palettes.
- [x] Publish screenshot filenames only after encoding is complete.
- [x] Allow the Hijri and Gregorian date labels to wrap on narrow screens.
- [x] Add active-window diagnostics and inspect Compose accessibility descendants for the saved location.
- [x] Pass the four-variant instrumentation capture on both emulator APIs (run 36730684425).
- [x] Inspect all eight PNGs visually: unobscured content and complete dates; identified untranslated Hijri date and dark status icons for the next task.

Ruling: PNG signature/dimensions prove encoding, not screen readiness. Earlier English/dark images were dimmed; visual QA remains open until the new captures are inspected.

### Task 5: Localized date, system-bar contrast, and 200% font captures

- [x] Add localized Hijri date formatting while preserving the Arabic date presentation and calendar calculations.
- [x] Verify English Hijri year/month and preserved Arabic formatting with `HijriDateTest` locally.
- [x] Bind status/navigation bar icons to the selected app theme.
- [x] Add Arabic and English Prayer Home screenshots at system font scale 2.0, with scale restoration and an Activity configuration assertion.
- [ ] Compile the updated app and instrumentation tests.
- [ ] Pass CI on API 26 and 36, inspect all twelve images, and fix any clipping or incorrect system bar contrast found.
