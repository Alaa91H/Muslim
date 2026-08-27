#!/usr/bin/env python3
"""Static guardrails for the live Adhan notification lifecycle."""

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REQUIRED_SNIPPETS = {
    "core/core-notifications/src/main/java/org/muslim/app/core/notifications/NotificationChannels.kt": [
        'const val ADHAN = "adhan_alert_v3"',
        "High-importance channel for the Adhan itself",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanNotifications.kt": [
        ".setOngoing(true)",
        ".setAutoCancel(false)",
        ".setVisibility(NotificationCompat.VISIBILITY_PUBLIC)",
        ".setPriority(NotificationCompat.PRIORITY_HIGH)",
        ".setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)",
        "fun cancelActiveAdhan(context: Context)",
        "fun cancelReminder(context: Context)",
        "cancelReminder(context)",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanNotificationActionReceiver.kt": [
        "ACTION_STOP",
        "AdhanPlaybackService.stop(context)",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanPlaybackService.kt": [
        "START_REDELIVER_INTENT",
        "val isPreviewing = MutableStateFlow(false)",
        "fun stopPreview(context: Context)",
        "PLAYBACK_WAKELOCK_TIMEOUT_MS",
        "private var activeRequest: PlaybackRequest? = null",
        "override fun onTaskRemoved(rootIntent: Intent?)",
        "startForegroundNotification(request)",
        "AdhanNotifications.cancelActiveAdhan(this)",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanAlarmReceiver.kt": [
        "AdhanNotifications.cancelReminder(appContext)",
        "presentationAllowed = deliveryPolicy.postVisibleNotification",
        "AdhanNotifications.cancelActiveAdhan(appContext)",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsViewModel.kt": [
        "val isPreviewing = AdhanPlaybackStatus.isPreviewing",
        "AdhanPlaybackService.stopPreview(context)",
        "isPreview = true",
        "fun saveAdhanCustomization(",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/home/HomeScreen.kt": [
        "PrayerAlertAction(",
        "AdhanCustomizeDialog(",
        "alertSettings.adhanVolumeFor(prayer)",
        "Prayer.Sunrise",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsScreen.kt": [
        "adjustmentMinutes = settings.adjustments[prayer]",
        "useGlobalVolume = settings.useGlobalAdhanVolume",
        "fun AdhanCustomizeDialog(",
    ],
    "app/src/androidTest/java/org/muslim/app/feature/prayertimes/notifications/AdhanNotificationsInstrumentedTest.kt": [
        "activeAdhan_isOngoingPublicHighPriority_andExposesOnlyTheExplicitStopAction",
        "showAdhan_confirmsTheFreshActiveCard_andCancelsTheEarlierReminder",
        "NotificationCompat.VISIBILITY_PUBLIC",
        "NotificationCompat.PRIORITY_HIGH",
    ],
}

FORBIDDEN_SNIPPETS = {
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanNotifications.kt": [
        ".setDeleteIntent(",
        "dismissible:",
        "stopOnDismiss:",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanNotificationActionReceiver.kt": [
        "ACTION_DISMISSED",
        "EXTRA_STOP_ON_DISMISS",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanPlaybackService.kt": [
        "EXTRA_NOTIFICATION_DISMISSIBLE",
        "EXTRA_STOP_ON_NOTIFICATION_DISMISS",
        "MAX_PLAYBACK_MS",
        "scheduleServiceStop",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/notifications/AdhanAlarmReceiver.kt": [
        "notificationDismissible",
        "stopOnNotificationDismiss",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsScreen.kt": [
        "settings_adhan_notification_dismissible",
        "settings_adhan_stop_on_dismiss",
    ],
    "feature/feature-prayer-times/src/main/java/org/muslim/app/feature/prayertimes/ui/settings/PrayerSettingsViewModel.kt": [
        "setAdhanNotificationDismissible",
        "setStopAdhanOnNotificationDismiss",
    ],
}


def verify() -> list[str]:
    failures: list[str] = []
    for relative_path, snippets in REQUIRED_SNIPPETS.items():
        path = ROOT / relative_path
        if not path.is_file():
            failures.append(f"missing required file: {relative_path}")
            continue
        content = path.read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet not in content:
                failures.append(f"{relative_path}: missing {snippet!r}")

    for relative_path, snippets in FORBIDDEN_SNIPPETS.items():
        content = (ROOT / relative_path).read_text(encoding="utf-8")
        for snippet in snippets:
            if snippet in content:
                failures.append(f"{relative_path}: must not contain {snippet!r}")
    return failures


if __name__ == "__main__":
    problems = verify()
    if problems:
        print("Adhan notification lifecycle static checks failed:")
        for problem in problems:
            print(f"- {problem}")
        raise SystemExit(1)
    print("Adhan notification lifecycle static checks passed.")
