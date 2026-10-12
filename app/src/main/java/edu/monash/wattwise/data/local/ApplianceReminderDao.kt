package edu.monash.wattwise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.monash.wattwise.data.model.ApplianceReminder
import kotlinx.coroutines.flow.Flow

/** Database operations for the "appliance_reminder" table, which has one row for each user. */
@Dao
interface ApplianceReminderDao {

    /** Emits null until a reminder has been set, and again every time it changes. */
    @Query("SELECT * FROM appliance_reminder WHERE userId = :userId")
    fun getReminder(userId: String): Flow<ApplianceReminder?>

    /** Reads the reminder once, for code that runs outside the UI such as the boot receiver. */
    @Query("SELECT * FROM appliance_reminder WHERE userId = :userId")
    suspend fun getReminderOnce(userId: String): ApplianceReminder?

    /** Inserts the reminder, or replaces the user's existing row. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReminder(reminder: ApplianceReminder)

    /** Removes the user's reminder. */
    @Query("DELETE FROM appliance_reminder WHERE userId = :userId")
    suspend fun deleteReminder(userId: String)
}
