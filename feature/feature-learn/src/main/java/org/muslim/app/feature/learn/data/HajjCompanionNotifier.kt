package org.muslim.app.feature.learn.data

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.muslim.app.core.notifications.NotificationChannels
import org.muslim.app.feature.learn.R
import org.muslim.app.feature.learn.domain.HajjMansik

/**
 * Builds and posts the daily Pilgrim Companion notification. Separated from
 * [HajjCompanionWorker] so the Android framework side-effect can be faked in
 * unit tests. Tapping the notification opens the Learn hub (Hajj and Umrah).
 */
open class HajjCompanionNotifier(private val context: Context) {

    fun show(mansik: HajjMansik) {
        NotificationChannels.create(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent().apply {
                setClassName(context, MAIN_ACTIVITY)
                data = "muslim://learn".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val title = context.getString(titleRes(mansik))
        val body = context.getString(messageRes(mansik))
        val notification = Notification.Builder(context, NotificationChannels.HAJJ)
            .setSmallIcon(org.muslim.app.core.notifications.R.drawable.ic_muslim_status_bar_v2028)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, notification)
    }

    private fun titleRes(mansik: HajjMansik): Int = when (mansik) {
        HajjMansik.TARWIYAH -> R.string.hajj_companion_tarwiyah_title
        HajjMansik.ARAFAT -> R.string.hajj_companion_arafah_title
        HajjMansik.NAHR -> R.string.hajj_companion_nahr_title
        HajjMansik.TASHREEQ_1 -> R.string.hajj_companion_tashreeq1_title
        HajjMansik.TASHREEQ_2 -> R.string.hajj_companion_tashreeq2_title
        HajjMansik.TASHREEQ_3 -> R.string.hajj_companion_tashreeq3_title
    }

    private fun messageRes(mansik: HajjMansik): Int = when (mansik) {
        HajjMansik.TARWIYAH -> R.string.hajj_companion_tarwiyah_msg
        HajjMansik.ARAFAT -> R.string.hajj_companion_arafah_msg
        HajjMansik.NAHR -> R.string.hajj_companion_nahr_msg
        HajjMansik.TASHREEQ_1 -> R.string.hajj_companion_tashreeq1_msg
        HajjMansik.TASHREEQ_2 -> R.string.hajj_companion_tashreeq2_msg
        HajjMansik.TASHREEQ_3 -> R.string.hajj_companion_tashreeq3_msg
    }

    private companion object {
        const val NOTIFICATION_ID = 7010
        const val MAIN_ACTIVITY = "org.muslim.app.MainActivity"
    }
}
