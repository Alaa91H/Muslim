# SDD ledger — plan: docs/superpowers/plans/2026-09-30-uiux-v2-visual-qa.md

Pre-flight: screenshot test uses the Hilt-backed application Activity; the CI job runs `:app:connectedDebugAndroidTest` before pulling artifacts. No cross-task conflicts.

Task 1: Added app Activity instrumentation screenshot capture for Prayer Home with saved/restored preferences. Local CI verifier will guard the capture contract; actual test execution is delegated to GitHub emulator jobs because no local emulator is connected.

Task 2: Added per-API screenshot extraction and artifact upload to emulator CI. First CI run captured the image and all API 26 app tests passed, but `adb pull` ran after the emulator runner shut down the device (`device offline`). Moved screenshot extraction into the runner script, before teardown; contract requires a non-empty image when tests succeed.

Task 3: Updated UX28/UX31 status and QA-matrix verifier. The screenshot artifact is explicitly not visual regression comparison; full variants, reviewed baselines, and device checks remain open.
