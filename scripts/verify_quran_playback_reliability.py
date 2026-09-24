#!/usr/bin/env python3
"""Guard the Quran Phase 3 playback/offline reliability contract."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PLAYER = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/QuranAudioPlayer.kt"
REPOSITORY = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/data/RecitationRepository.kt"
VIEW_MODEL = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderViewModel.kt"
READER = ROOT / "feature/feature-quran/src/main/java/org/muslim/app/feature/quran/ui/QuranReaderScreen.kt"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def require_ordered(text: str, needles: list[str], message: str) -> None:
    position = -1
    for needle in needles:
        position = text.find(needle, position + 1)
        if position < 0:
            raise AssertionError(message)


def main() -> None:
    player = PLAYER.read_text(encoding="utf-8")
    repository = REPOSITORY.read_text(encoding="utf-8")
    view_model = VIEW_MODEL.read_text(encoding="utf-8")
    reader = READER.read_text(encoding="utf-8")

    require("enum class RecitationFailureReason" in player, "typed recitation failures are required")
    require("val lastFailure: StateFlow<RecitationFailureEvent?>" in player, "player must expose the latest typed failure")
    require("errorCount" not in player, "legacy playback error counter must not return")

    prepared_start = player.find("engine.setOnPreparedListener")
    prepared_end = player.find("engine.setOnCompletionListener", prepared_start)
    require(
        prepared_start >= 0 and prepared_end > prepared_start,
        "prepared-listener playback contract is required",
    )
    prepared_block = player[prepared_start:prepared_end]
    require_ordered(
        prepared_block,
        [
            "runCatching {",
            "engine.start()",
            ".onSuccess {",
            "_playbackState.value = PlaybackState.Playing",
            ".onFailure {",
        ],
        "Playing state must follow a successful engine start",
    )

    require("fun buildLocalRecitationQueue" in repository, "offline queue validator is required")
    require("length() > 0L" in repository, "empty audio files must not count as offline-ready")
    require("suspend fun localQueue(" in repository, "repository must expose the local-first queue path")

    require("val recitationFailure: StateFlow<RecitationFailureEvent?>" in view_model, "reader VM must expose retryable failures")
    require("fun retryPlaybackAfterFailure()" in view_model, "reader VM must retain a retry path")
    require("fun retryGlobalNumbers(" in view_model, "retry must be able to resume from the failed ayah")
    require(
        "retryGlobalNumbers(request.globalNumbers, failedGlobal)" in view_model,
        "retry must trim the queue to the failed ayah when that position is known",
    )
    require("recitationRepository.localQueue(" in view_model, "reader VM must check local audio before downloading")
    require("RecitationFailureReason.DownloadFailed" in view_model, "download failures must be explicit")
    require("playbackErrorCount" not in view_model, "legacy error-count presentation must not return")

    require("Snackbar(" in reader, "reader must surface playback recovery through a snackbar")
    require("viewModel::retryPlaybackAfterFailure" in reader, "reader snackbar must expose retry")
    require("playbackErrorCount" not in reader, "reader must not regress to error-count toasts")

    print("Quran playback/offline reliability contract verified.")


if __name__ == "__main__":
    main()
