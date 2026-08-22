package org.muslim.app.core.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build

/**
 * Every permission the app uses, grouped by kind:
 *
 *  - [Kind.Runtime] — dangerous permissions requested at runtime through the
 *    system dialog (Android 6+).
 *  - [Kind.SpecialAccess] — "special app access" toggles living in the system
 *    settings (exact alarms, overlay, notification policy). They cannot be
 *    requested with `requestPermissions`; the app must deep-link the user to
 *    the matching settings screen.
 *  - [Kind.Normal] — normal permissions auto-granted at install time; kept for
 *    the manager's completeness report so the user sees the full picture.
 *
 * The unified permission manager (PROJECT_PROMPT.md §3.3) drives the
 * permissions screen in Settings and every runtime request site, so one
 * definition lives here and the rest of the app consults it.
 */
@SuppressLint("InlinedApi")
enum class AppPermission(
    val labelRes: Int,
    val descriptionRes: Int,
    val kind: Kind,
    /** The runtime permission string (only for [Kind.Runtime]). */
    val runtimePermission: String? = null,
    /** Minimum SDK for which this permission exists / matters. */
    val minSdk: Int = Build.VERSION_CODES.BASE,
    /** Optional companion permission requested together (e.g. coarse+fine). */
    val companionRuntimePermission: String? = null,
) {

    /** Prayer-time & adhan notifications (Android 13+). */
    Notifications(
        R.string.permission_notifications,
        R.string.permission_notifications_desc,
        Kind.Runtime,
        runtimePermission = Manifest.permission.POST_NOTIFICATIONS,
        minSdk = Build.VERSION_CODES.TIRAMISU,
    ),

    /** Fine location for prayer times, Qibla and the mosque finder. */
    Location(
        R.string.permission_location,
        R.string.permission_location_desc,
        Kind.Runtime,
        runtimePermission = Manifest.permission.ACCESS_FINE_LOCATION,
        companionRuntimePermission = Manifest.permission.ACCESS_COARSE_LOCATION,
    ),

    /** Microphone access for one-shot, user-triggered voice navigation. */
    Microphone(
        R.string.permission_microphone,
        R.string.permission_microphone_desc,
        Kind.Runtime,
        runtimePermission = Manifest.permission.RECORD_AUDIO,
    ),

    /** Exact alarms — adhan & prayer reminders fire on time (Android 12+). */
    ExactAlarms(
        R.string.permission_exact_alarms,
        R.string.permission_exact_alarms_desc,
        Kind.SpecialAccess,
        minSdk = Build.VERSION_CODES.S,
    ),

    /** Overlay — floating adhkar above other apps. */
    Overlay(
        R.string.permission_overlay,
        R.string.permission_overlay_desc,
        Kind.SpecialAccess,
    ),

    /** Notification policy access — Do-Not-Disturb during prayer. */
    NotificationPolicy(
        R.string.permission_dnd,
        R.string.permission_dnd_desc,
        Kind.SpecialAccess,
    ),

    /** Boot receiver — reminders reschedule automatically after reboot. */
    BootCompleted(
        R.string.permission_boot,
        R.string.permission_boot_desc,
        Kind.Normal,
    ),

    /** Vibration — haptic feedback in tasbih & notifications. */
    Vibrate(
        R.string.permission_vibrate,
        R.string.permission_vibrate_desc,
        Kind.Normal,
    ),

    /**
     * Battery optimization exemption — keeps prayer times, adhan and
     * reminders alive around the clock on OEM devices that aggressively
     * kill background work (Android 6+).
     */
    BatteryOptimization(
        R.string.permission_battery_optimization,
        R.string.permission_battery_optimization_desc,
        Kind.SpecialAccess,
        minSdk = Build.VERSION_CODES.M,
    ),

    /**
     * Notification-listener access — pauses Quran recitation while soundful
     * notifications ring, so the tilaawah never overlaps a notification tone.
     */
    NotificationListener(
        R.string.permission_notification_listener,
        R.string.permission_notification_listener_desc,
        Kind.SpecialAccess,
    );

    enum class Kind { Runtime, SpecialAccess, Normal }
}
