package org.muslim.app.feature.finance.data

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.muslim.app.feature.finance.domain.DebtEntry
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/** Schedules one device-local reminder per debt record. */
object DebtReminderScheduler {
    private const val ID_KEY = "debt_id"
    private const val PARTY_KEY = "party_name"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"

    /**
     * Schedules for 09:00 on the due date. A due date that has already passed
     * is deliberately not scheduled, preventing stale notification spam.
     */
    fun schedule(context: Context, entry: DebtEntry, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val dueDate = entry.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
        val zone = ZoneId.systemDefault()
        val target = dueDate.atTime(LocalTime.of(9, 0)).atZone(zone).toInstant().toEpochMilli()
        if (target <= nowMillis) return false

        val input = Data.Builder()
            .putString(ID_KEY, entry.id)
            .putString(PARTY_KEY, entry.partyName)
            .putDouble(AMOUNT_KEY, entry.amount)
            .putString(CURRENCY_KEY, entry.currency)
            .build()
        val request = OneTimeWorkRequestBuilder<DebtReminderWorker>()
            .setInputData(input)
            .setInitialDelay(target - nowMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(workName(entry.id), ExistingWorkPolicy.REPLACE, request)
        return true
    }

    fun cancel(context: Context, id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(id))
    }

    internal fun reminderData(input: Data): DebtReminderData? {
        val id = input.getString(ID_KEY) ?: return null
        val party = input.getString(PARTY_KEY) ?: return null
        val currency = input.getString(CURRENCY_KEY) ?: return null
        return DebtReminderData(id, party, input.getDouble(AMOUNT_KEY, 0.0), currency)
    }

    private fun workName(id: String) = "finance_debt_reminder_$id"
}

data class DebtReminderData(
    val id: String,
    val partyName: String,
    val amount: Double,
    val currency: String,
)
