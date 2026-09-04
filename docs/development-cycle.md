# Muslim Continuous Development Cycle

## Purpose

Muslim is developed through bounded, reviewable improvement cycles. Each cycle selects a small group of high-value changes, implements them on `main`, validates them locally and in GitHub Actions, publishes one documented release, and then uses the release results to select the next cycle.

## The cycle

| Stage | Required outcome | Release gate |
|---|---|---|
| Discover | Audit the current branch, CI history, open regressions, and user-facing priorities. | Every selected item has a clear scope and acceptance criterion. |
| Design | Define the UX, technical, privacy, and religious-integrity constraints before coding. | No fixed prayer-time shortcuts; no weakening of local-first behavior. |
| Implement | Deliver one bounded batch on `main` with focused commits and regression tests. | Existing GPS, MWL 17° Isha, notification, and Qibla contracts remain intact. |
| Verify | Run contract scripts, formatting/diff checks, Detekt, unit tests, and available instrumentation tests. | No known failure is ignored or marked as passed without evidence. |
| Integrate | Push the batch and monitor every required GitHub Actions job. | Quality, emulator tests, and release artifacts must succeed. |
| Release | Create a version tag, publish APK/AAB assets, and write truthful English notes with confirmed links only. | Tag points to the verified commit and release assets have recorded digests. |
| Learn | Review CI failures, user feedback, regressions, and release artifacts. | The next cycle starts from evidence, not assumptions. |

## Non-negotiable contracts

Prayer calculations remain dynamic and use the Muslim World League profile with a 17° Isha angle. GPS failures must remain recoverable and must never terminate the application process. Qibla direction must reject invalid upright postures. Notification icons must use the approved monochrome brand identity. The app remains local-first unless a future change is explicitly designed, reviewed, and tested as a privacy-preserving exception.

## Release policy

A release is published only after the current commit passes the repository’s contract checks, Detekt, unit tests, emulator instrumentation tests, and release-artifact jobs. A release note must describe only changes that exist in the tagged commit. If an existing version tag points to an older commit, it must not be silently reused; the tag and release must be reconciled explicitly before publishing updated artifacts.

## Cycle boundary

One agent session executes one bounded cycle and records the next-cycle backlog. The cycle is intentionally not an unbounded background loop: an endless process cannot safely make unlimited product or release decisions without fresh evidence and explicit review. The repository document and CI history provide the durable hand-off point for starting the next cycle.
