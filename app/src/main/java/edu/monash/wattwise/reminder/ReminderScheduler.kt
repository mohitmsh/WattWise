package edu.monash.wattwise.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import edu.monash.wattwise.constants.Reminders
import java.util.Calendar
import java.util.Date

/** Schedules and cancels the daily appliance reminder with AlarmManager. */
class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Sets an alarm [Reminders.LEAD_MINUTES] minutes before [startHour] to
     * remind the user to start [applianceLabel]. If that time has already
     * passed today, the alarm is set for tomorrow. Scheduling again replaces
     * the previous alarm.
     */
    fun schedule(startHour: Int, applianceLabel: String) {
        val triggerAtMillis = nextReminderTime(startHour)
        val pendingIntent = reminderPendingIntent(startHour, applianceLabel)

        // Exact alarms need permission from Android 12; without it, use an inexact alarm.
        val canUseExactAlarm = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canUseExactAlarm) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
        Log.d(TAG, "Reminder alarm for the $applianceLabel set for ${Date(triggerAtMillis)} (exact = $canUseExactAlarm)")
    }

    /** Removes the alarm, so no more reminders arrive. */
    fun cancel() {
        // Cancelling needs a PendingIntent that matches the one used to schedule.
        alarmManager.cancel(reminderPendingIntent(startHour = 0, applianceLabel = ""))
        Log.d(TAG, "Reminder alarm cancelled")
    }

    /** The intent AlarmManager sends to [ReminderReceiver] when the alarm goes off. */
    private fun reminderPendingIntent(startHour: Int, applianceLabel: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra(Reminders.EXTRA_START_HOUR, startHour)
        intent.putExtra(Reminders.EXTRA_APPLIANCE, applianceLabel)
        return PendingIntent.getBroadcast(
            context,
            Reminders.ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** The next time the reminder must arrive, in milliseconds: today if that time is still to come, otherwise tomorrow. */
    private fun nextReminderTime(startHour: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, startHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MINUTE, -Reminders.LEAD_MINUTES)
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private companion object {
        const val TAG = "REMINDER_ALARM"
    }
}
