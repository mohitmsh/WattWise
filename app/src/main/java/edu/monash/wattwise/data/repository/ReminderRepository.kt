package edu.monash.wattwise.data.repository

import android.content.Context
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.ApplianceReminder
import kotlinx.coroutines.flow.Flow

/** Single access point for the saved appliance reminder. */
class ReminderRepository(context: Context) {

    private val reminderDao = WattWiseDatabase.getDatabase(context).applianceReminderDao()

    /** The user's reminder as a Flow; it emits null when no reminder is set. */
    fun getReminder(userId: String): Flow<ApplianceReminder?> = reminderDao.getReminder(userId)

    /** Reads the reminder one time, for code that does not need updates. */
    suspend fun getReminderOnce(userId: String): ApplianceReminder? = reminderDao.getReminderOnce(userId)

    /** Saves the reminder and replaces the user's earlier one. */
    suspend fun save(reminder: ApplianceReminder) = reminderDao.saveReminder(reminder)

    /** Removes the user's reminder. */
    suspend fun delete(userId: String) = reminderDao.deleteReminder(userId)
}
