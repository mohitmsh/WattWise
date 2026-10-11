package edu.monash.wattwise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.monash.wattwise.data.model.HouseholdProfile
import kotlinx.coroutines.flow.Flow

/** Database operations for the single-row "household_profile" table. */
@Dao
interface HouseholdProfileDao {

    /** Emits null until the setup form has been saved for the first time. */
    @Query("SELECT * FROM household_profile WHERE id = :id")
    fun getProfile(id: Int = HouseholdProfile.SINGLE_PROFILE_ID): Flow<HouseholdProfile?>

    /** Reads the profile once, for code that runs outside the UI such as the background worker. */
    @Query("SELECT * FROM household_profile WHERE id = :id")
    suspend fun getProfileOnce(id: Int = HouseholdProfile.SINGLE_PROFILE_ID): HouseholdProfile?

    /** Inserts the profile, or replaces the existing row because the id is the same. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: HouseholdProfile)
}
