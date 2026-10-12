package edu.monash.wattwise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.monash.wattwise.data.model.HouseholdProfile
import kotlinx.coroutines.flow.Flow

/** Database operations for the "household_profile" table, which has one row for each user. */
@Dao
interface HouseholdProfileDao {

    /** Emits null until the setup form has been saved for the first time. */
    @Query("SELECT * FROM household_profile WHERE userId = :userId")
    fun getProfile(userId: String): Flow<HouseholdProfile?>

    /** Reads the profile once, for code that runs outside the UI such as the background worker. */
    @Query("SELECT * FROM household_profile WHERE userId = :userId")
    suspend fun getProfileOnce(userId: String): HouseholdProfile?

    /** Inserts the profile, or replaces the user's existing row. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: HouseholdProfile)
}
