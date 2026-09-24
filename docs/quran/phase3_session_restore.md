# Quran Phase 3 — Persisted Recitation Session Restore

Date: 2026-09-24

## Goal

Recitation must survive process/service loss as a recoverable user session without
silently auto-starting audio when Android recreates the app.

The durable session records semantic playback intent rather than depending only on
an in-memory MediaPlayer instance.

## Persisted state

Each session stores:

- reciter id
- surah number
- ordered global ayah queue
- current global ayah
- per-ayah repeat count
- continuous/advance-to-next/end-of-Quran behavior
- media position in milliseconds
- whether the session was playing or paused
- save timestamp

Invalid sessions are rejected when the reciter id/queue/surah/current ayah/position
contract is inconsistent.

## Runtime ownership

`RecitationSessionRuntime` is the process-local owner of the current intent.
Writes are serialized by a Mutex and guarded by a monotonically increasing
generation so an older periodic write cannot resurrect a session after Stop or
replace a newer queue.

Terminal player transitions are explicit:

- `Completed` -> clear the persisted session
- `Stopped` -> clear the persisted session
- `Failed` -> preserve the persisted session for retry/restore

## Background position persistence

`RecitationPlaybackService` refreshes and persists the live media position every
2 seconds while the player is Playing or Paused. This is independent of the reader
screen, so background recitation continues to produce useful recovery snapshots.

If Android recreates the service after process death and the in-memory player is
Idle, the service stops with `START_NOT_STICKY`. It does not auto-play. The durable
session remains available to the reader.

## Explicit restore

When a new Quran reader ViewModel is created it loads at most one validated restore
candidate. Nothing starts automatically.

The reader surfaces a localized paused-recitation Snackbar with:

- Play: explicitly restores the same reciter, surah, remaining ayah queue,
  repeat/continuous semantics and saved media position.
- Close: discards the candidate and clears durable state.

A restored seek is one-shot: only the first restored ayah receives the saved
position. Repeats and later ayahs start normally from zero.

## Verification

- `QuranAudioPlayerTest` covers restored seek and terminal deactivation reasons.
- `RecitationSessionModelTest` covers model validation, remaining-queue recovery,
  and JSON round-trip.
- `scripts/verify_quran_session_restore.py` guards durable state, service
  persistence, explicit restore, terminal clearing, and no-autoplay behavior.

## Next milestone

The next Phase 3 increment can harden network transition behavior and download
ownership: loss/recovery of connectivity during queue preparation, deduplicating
foreground download work, and ensuring a restored session joins existing partial
downloads instead of creating competing transfers.
