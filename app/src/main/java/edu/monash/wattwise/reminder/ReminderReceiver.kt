package edu.monash.wattwise.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import edu.monash.wattwise.constants.Reminders
import edu.monash.wattwise.constants.ScenarioRules
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.NotificationHelper

/**
 * Receives the alarm from AlarmManager, shows the laundry reminder
 * notification, and sets the alarm again for the next day.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val startHour = intent.getIntExtra(Reminders.EXTRA_START_HOUR, ScenarioRules.EARLIEST_START_HOUR.toInt())
        Log.d(TAG, "Reminder alarm received for a ${Formatters.formatHour(startHour)} start")

        val shown = NotificationHelper.show(
            context = context,
            notificationId = Reminders.NOTIFICATION_ID,
            title = "Laundry reminder",
            text = "Your planned laundry start is ${Formatters.formatHour(startHour)}, in " +
                "${ScenarioRules.REMINDER_LEAD_MINUTES} minutes.",
        )
        Log.d(TAG, if (shown) "Reminder notification shown" else "Notification not shown: permission not granted")

        // An alarm fires only once, so schedule tomorrow's reminder now.
        ReminderScheduler(context).schedule(startHour)
    }

    private companion object {
        const val TAG = "REMINDER_ALARM"
    }
}
