#!/usr/bin/env python3
"""Guard the Quran Phase 3 persisted recitation-session contract."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STORE = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/RecitationSessionStore.kt"
PLAYER = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/QuranAudioPlayer.kt"
SERVICE = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/RecitationPlaybackService.kt"
BRIDGE = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/RecitationPlaybackBridge.kt"
VIEW_MODEL = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderViewModel.kt"
READER = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    store = STORE.read_text(encoding="utf-8")
    player = PLAYER.read_text(encoding="utf-8")
    service = SERVICE.read_text(encoding="utf-8")
    bridge = BRIDGE.read_text(encoding="utf-8")
    view_model = VIEW_MODEL.read_text(encoding="utf-8")
    reader = READER.read_text(encoding="utf-8")

    require("data class PersistedRecitationSession" in store, "durable recitation session model is required")
    require("val positionMs: Long" in store, "session must persist media position")
    require("val currentGlobalNumber: Int" in store, "session must persist the current ayah")
    require("val wasPlaying: Boolean" in store, "session must persist playing/paused state")
    require("private var generation = 0L" in store, "session writes must be generation guarded")
    require("Mutex()" in store and "withLock" in store, "session writes must be serialized")

    require("startPositionMs: Long = 0L" in player, "audio queue must accept a restored start position")
    require("engine.seekTo(seekPosition)" in player, "restored playback must seek before start")
    require("pendingStartPositionMs = 0L" in player, "restored seek must be one-shot")

    require("enum class PlaybackDeactivationReason" in bridge, "terminal playback reasons are required")
    require("PlaybackDeactivationReason.Failed" in bridge, "failed playback must preserve the durable session")
    require("sessionRuntime.clear()" in bridge, "completed/stopped playback must clear the session")

    require("@Inject lateinit var sessionRuntime: RecitationSessionRuntime" in service, "service must own background session persistence")
    require("SESSION_PERSIST_INTERVAL_MS" in service, "service must persist background position periodically")
    require("sessionRuntime.persist(" in service, "service must write playback snapshots")
    require(
        "return START_NOT_STICKY" in service,
        "a recreated service with an idle player must stop without auto-starting playback",
    )

    require("val restorableSession: StateFlow<PersistedRecitationSession?>" in view_model, "reader VM must expose a restore candidate")
    require("fun resumeRestorableSession()" in view_model, "session restore must be an explicit user action")
    require("fun discardRestorableSession()" in view_model, "restore candidate must be dismissible")
    require("startPositionMs = session.positionMs" in view_model, "restore must pass the persisted media position")
    require("sessionRuntime.begin(intent)" in view_model, "new playback must establish durable session ownership")

    require("viewModel.resumeRestorableSession()" in reader, "reader must expose explicit resume")
    require("viewModel::discardRestorableSession" in reader, "reader must expose explicit discard")
    require(
        "restorableSession != null" in reader and "playbackState == PlaybackState.Idle" in reader,
        "restore prompt must only appear while the live player is idle",
    )

    print("Quran persisted recitation-session contract verified.")


if __name__ == "__main__":
    main()
