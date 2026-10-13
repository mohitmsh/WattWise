package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.data.model.ApplianceReminder
import edu.monash.wattwise.data.repository.CloudBackupRepository
import edu.monash.wattwise.data.repository.ReminderRepository
import edu.monash.wattwise.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/** Saves the appliance reminder to Room and keeps its alarm in step with it. */
class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReminderRepository(application)
    private val cloudBackup = CloudBackupRepository(application)
    private val reminderScheduler = ReminderScheduler(application)

    /** The saved reminder of [userId]; emits null when nobody is signed in or none is set. */
    fun getReminder(userId: String?): Flow<ApplianceReminder?> =
        if (userId == null) flowOf(null) else repository.getReminder(userId)

    /** Saves a daily reminder to start [applianceLabel] at [startHour] and sets its alarm. */
    fun setReminder(userId: String, applianceLabel: String, startHour: Int) {
        val reminder = ApplianceReminder(userId = userId, applianceLabel = applianceLabel, startHour = startHour)
        viewModelScope.launch(Dispatchers.IO) {
            repository.save(reminder)
            cloudBackup.backup(userId)
        }
        reminderScheduler.schedule(startHour, applianceLabel)
    }

    /** Deletes the reminder from Room and from the backup, and cancels its alarm. */
    fun removeReminder(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(userId)
            cloudBackup.backup(userId)
        }
        reminderScheduler.cancel()
    }

    /**
     * Sets the alarm again for a signed-in user who has a reminder. Android
     * removes alarms when an app is updated or force-stopped, and the alarm is
     * cancelled at sign-out, so this runs when the app opens and after sign-in.
     */
    fun restoreReminder(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val reminder = repository.getReminderOnce(userId)
            if (reminder != null) reminderScheduler.schedule(reminder.startHour, reminder.applianceLabel)
        }
    }

    /** Stops the alarm when the user signs out. The reminder stays saved for their next sign-in. */
    fun cancelReminderAlarm() = reminderScheduler.cancel()
}
