package edu.monash.wattwise.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import edu.monash.wattwise.constants.BackgroundWork
import edu.monash.wattwise.constants.Reminders
import edu.monash.wattwise.data.repository.AuthRepository
import edu.monash.wattwise.data.repository.HouseholdRepository
import edu.monash.wattwise.data.repository.UsageHistoryRepository
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.NotificationHelper
import java.util.concurrent.TimeUnit

/**
 * Background task run by WorkManager. It saves every finished hour of meter
 * usage to Room, so the report and the household's baseline stay up to date
 * even when the app is closed. doWork() already runs off the main thread.
 */
class UsageSummaryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Usage summary work started")
        return try {
            val repository = UsageHistoryRepository(applicationContext)
            val totalBefore = repository.getTodayTotal()
            val hoursSaved = repository.saveMissingHours()
            val totalAfter = repository.getTodayTotal()
            Log.d(TAG, "Usage summary work finished: $hoursSaved new hours saved to Room")

            alertIfTargetPassed(totalBefore, totalAfter)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Usage summary work failed, WorkManager will try again", e)
            Result.retry()
        }
    }

    /**
     * Shows a notification when the hours just saved took today's total past
     * the daily target. Comparing the totals before and after means the alert
     * appears once on the day, not on every run. It respects the "Smart usage
     * alerts" switch in the More screen.
     */
    private suspend fun alertIfTargetPassed(totalBefore: Double, totalAfter: Double) {
        // The alert is for whoever is signed in; with nobody signed in there is no target to check.
        val userId = AuthRepository().currentUser?.uid ?: return
        val profile = HouseholdRepository(applicationContext).getProfileOnce(userId) ?: return
        val target = profile.dailyTargetKwh
        val passedTargetNow = totalBefore <= target && totalAfter > target
        if (!passedTargetNow) return

        if (!profile.usageAlerts) {
            Log.d(TAG, "Daily target passed, but usage alerts are switched off")
            return
        }
        val shown = NotificationHelper.show(
            context = applicationContext,
            notificationId = Reminders.TARGET_ALERT_NOTIFICATION_ID,
            title = "Daily energy target passed",
            text = "You have used ${Formatters.formatKwh(totalAfter)} kWh today and your target is $target kWh. " +
                "Open WattWise for advice.",
        )
        Log.d(TAG, "Daily target passed: notification ${if (shown) "shown" else "blocked by permission"}")
    }

    companion object {
        private const val TAG = "USAGE_WORKER"

        /**
         * Runs the worker once now, so the history is up to date whenever the app
         * opens, and then every 15 minutes. KEEP means opening the app again does
         * not create a second copy of the periodic work.
         */
        fun schedule(context: Context) {
            val startupRequest = OneTimeWorkRequestBuilder<UsageSummaryWorker>().build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(BackgroundWork.STARTUP_WORK_NAME, ExistingWorkPolicy.REPLACE, startupRequest)

            val periodicRequest = PeriodicWorkRequestBuilder<UsageSummaryWorker>(
                BackgroundWork.REPEAT_INTERVAL_MINUTES,
                TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BackgroundWork.PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest,
            )
            Log.d(TAG, "Usage summary work scheduled every ${BackgroundWork.REPEAT_INTERVAL_MINUTES} minutes")
        }
    }
}
