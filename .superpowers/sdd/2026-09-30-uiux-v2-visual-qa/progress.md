# SDD ledger — plan: docs/superpowers/plans/2026-09-30-uiux-v2-visual-qa.md

Pre-flight: screenshot test uses the Hilt-backed application Activity; the CI job runs `:app:connectedDebugAndroidTest` before pulling artifacts. No cross-task conflicts.

Task 1: Added app Activity instrumentation screenshot capture for Prayer Home with saved/restored preferences. Local CI verifier will guard the capture contract; actual test execution is delegated to GitHub emulator jobs because no local emulator is connected.

Task 2: Added per-API screenshot extraction and artifact upload to emulator CI. Contract requires a non-empty image for a successful test job.

Task 3: Updated UX28/UX31 status and QA-matrix verifier. The screenshot artifact is explicitly not visual regression comparison; full variants, reviewed baselines, and device checks remain open.
