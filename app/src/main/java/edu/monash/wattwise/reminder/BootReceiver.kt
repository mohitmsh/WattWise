package edu.monash.wattwise.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import edu.monash.wattwise.data.repository.AuthRepository
import edu.monash.wattwise.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android clears all alarms when the device restarts. This receiver runs
 * after the restart, reads the saved reminder from Room and sets the reminder again.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d(TAG, "Device restarted, checking for a saved reminder")

        // goAsync keeps the receiver alive while the database is read on a background thread.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Only the signed-in user's reminder is restored.
                val userId = AuthRepository().currentUser?.uid
                val reminder = userId?.let { ReminderRepository(context).getReminderOnce(it) }
                if (reminder != null) {
                    ReminderScheduler(context).schedule(reminder.startHour, reminder.applianceLabel)
                    Log.d(TAG, "Reminder restored after restart")
                } else {
                    Log.d(TAG, "No reminder to restore")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "REMINDER_ALARM"
    }
}
