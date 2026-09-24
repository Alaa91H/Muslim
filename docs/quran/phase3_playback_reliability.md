# Quran Phase 3 — Playback and Offline Reliability

Date: 2026-09-24

## Goal

The reader must treat recitation as a resilient local-first media session rather
than as "download something, then hope MediaPlayer starts".

This first Phase 3 milestone establishes one explicit pipeline:

1. Resolve the requested ayahs against validated app-private audio files.
2. If every file is present and non-empty, start immediately without entering a
   download state or posting download progress.
3. If any file is missing or invalid, download only what is needed.
4. Validate the resulting local queue again before playback.
5. Start the audio engine and publish `Playing` only after `start()` succeeds.
6. Surface a typed failure with the affected ayah when preparation or playback
   fails.
7. Keep the last playback request so the user can retry the same range, repeat
   mode, and continuation intent.
8. When the engine fails mid-queue, retry resumes from the failed ayah and
   continues the remaining queue rather than replaying already completed ayahs.

## Failure contract

`RecitationFailureReason` distinguishes:

- `DownloadFailed`
- `AudioFileUnavailable`
- `EngineUnavailable`
- `PreparationFailed`
- `StartFailed`
- `EngineError`

The reader maps preparation/download failures separately from engine failures and
presents a Snackbar with an explicit retry action. Repeated identical failures are
observable through a monotonically increasing failure sequence rather than the old
anonymous integer error counter.

## Offline contract

A local recitation file is usable only when it is a real file with a non-zero
length. Empty files no longer count as downloaded in playback readiness,
per-reciter coverage, or complete-surah checks.

The reader checks the local queue before enabling download UI, so fully downloaded
recitation starts without a transient "downloading" state.

## State correctness

The audio player no longer sets `PlaybackState.Playing` before the underlying
engine successfully starts. Preparation, start, resume, repeat restart, and engine
callbacks all flow through typed failure handling.

Foreground media-service activation also happens only after successful engine
start.

## Verification

- `QuranAudioPlayerTest` covers engine-unavailable, prepare-failure,
  start-failure, and typed engine-error transitions.
- `RecitationRepositoryQueueTest` verifies non-empty local queues and rejects
  empty/missing files.
- `scripts/verify_quran_playback_reliability.py` statically guards the
  local-first, typed-failure, and retry contracts in CI.

## Next Phase 3 milestone

The next increment should build on this contract to harden persisted playback
session restoration, download/session ownership, network/offline transitions, and
audio continuity across process/service recreation.
