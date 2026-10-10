package edu.monash.wattwise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import edu.monash.wattwise.data.model.HourlyUsage
import kotlinx.coroutines.flow.Flow

/** Database operations for the "hourly_usage" table. */
@Dao
interface HourlyUsageDao {

    /** Emits the full usage history again every time a new hour is saved. */
    @Query("SELECT * FROM hourly_usage ORDER BY dayStartMillis ASC, hour ASC")
    fun getAllUsage(): Flow<List<HourlyUsage>>

    /** The most recent hour saved, or null when the table is empty. */
    @Query("SELECT * FROM hourly_usage ORDER BY dayStartMillis DESC, hour DESC LIMIT 1")
    suspend fun getLatestUsage(): HourlyUsage?

    /** Total kWh saved for the day that starts at [dayStartMillis]; null when nothing is saved. */
    @Query("SELECT SUM(kwh) FROM hourly_usage WHERE dayStartMillis = :dayStartMillis")
    suspend fun getTotalForDay(dayStartMillis: Long): Double?

    @Insert
    suspend fun insertAll(usage: List<HourlyUsage>)
}
