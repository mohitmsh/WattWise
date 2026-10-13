package edu.monash.wattwise.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import edu.monash.wattwise.constants.Reminders
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.NotificationHelper

/**
 * Receives the alarm from AlarmManager, shows the appliance reminder
 * notification, and sets the alarm again for the next day.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val startHour = intent.getIntExtra(Reminders.EXTRA_START_HOUR, Reminders.DEFAULT_START_HOUR)
        val applianceLabel = intent.getStringExtra(Reminders.EXTRA_APPLIANCE) ?: "appliance"
        Log.d(TAG, "Reminder alarm received: $applianceLabel at ${Formatters.formatHour(startHour)}")

        val shown = NotificationHelper.show(
            context = context,
            notificationId = Reminders.NOTIFICATION_ID,
            title = "$applianceLabel reminder",
            text = "Start the ${applianceLabel.lowercase()} at ${Formatters.formatClockHour(startHour)}, in " +
                "${Reminders.LEAD_MINUTES} minutes. This is the low-cost time you selected on the energy map.",
        )
        Log.d(TAG, if (shown) "Reminder notification shown" else "Notification not shown: permission not granted")

        // An alarm fires only once, so schedule tomorrow's reminder now.
        ReminderScheduler(context).schedule(startHour, applianceLabel)
    }

    private companion object {
        const val TAG = "REMINDER_ALARM"
    }
}
