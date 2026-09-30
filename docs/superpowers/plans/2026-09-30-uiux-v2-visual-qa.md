# UI/UX V2 Visual QA Implementation Plan

> **For agentic workers:** execute task by task with TDD; do not claim screenshot comparison without a reviewed baseline.

**Goal:** Produce reproducible emulator screenshots for the existing critical Compose test surface and publish them as CI artifacts, while accurately tracking remaining UX28/UX31 visual gates.

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

- [x] Add an instrumentation test that launches the Hilt-backed `MainActivity` and writes `uiux-v2/prayer-home-ar-light.png` to app-private files.
- [ ] Run the instrumentation test in CI and inspect the uploaded PNG dimensions/content (run #1340 passed capture but lacked shared-storage export; CI now pulls the app-specific external file while Gradle runs).
- [x] Keep capture isolated from production code and user data.

### Task 2: Publish screenshot artifacts from CI

**Files:** modify `.github/workflows/ci.yml` emulator job.

- [x] Export the app-private PNG before the emulator runner shuts the device down; upload after runner completion.
- [x] Verify the workflow's artifact paths using the QA matrix static check.

### Task 3: Close only the automation portion of UX28

**Files:** modify `docs/design/ui_ux_v2_execution_plan.md`, `docs/design/ui_ux_v2_feature_parity_baseline.md`, `scripts/verify_uiux_v2_qa_matrix.py`.

- [x] Document the configured capture/export path and retain comparison/variant/manual QA gates.
- [x] Add a static contract that asserts emulator screenshot export/upload configuration and distinguishes capture from visual comparison.
- [x] Run the QA matrix verifier; it passes locally. CI validation remains pending.
