package edu.monash.wattwise.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.monash.wattwise.data.model.Appliance
import kotlinx.coroutines.flow.Flow

/** Database operations for the "appliances" table. */
@Dao
interface ApplianceDao {

    /** Emits the user's appliances again every time the table changes. */
    @Query("SELECT * FROM appliances WHERE userId = :userId ORDER BY name ASC")
    fun getAppliances(userId: String): Flow<List<Appliance>>

    /**
     * Search: the user's appliances whose name contains [query], or whose
     * category is one of [categories]. "||" joins text in SQL, so the pattern
     * becomes %query% and matches the text anywhere in the name. LIKE ignores
     * upper and lower case. A blank query matches every appliance.
     */
    @Query(
        "SELECT * FROM appliances WHERE userId = :userId " +
            "AND (name LIKE '%' || :query || '%' OR category IN (:categories)) ORDER BY name ASC",
    )
    fun searchAppliances(userId: String, query: String, categories: List<String>): Flow<List<Appliance>>

    /** Reads the user's appliances once, for the cloud backup. */
    @Query("SELECT * FROM appliances WHERE userId = :userId ORDER BY name ASC")
    suspend fun getAppliancesOnce(userId: String): List<Appliance>

    /** Adds several appliances in one step, used when a backup is restored. */
    @Insert
    suspend fun insertAll(appliances: List<Appliance>)

    /** Adds one appliance. Room gives it the next id. */
    @Insert
    suspend fun insertAppliance(appliance: Appliance)

    /** Saves the changed values of an appliance that is already in the table. */
    @Update
    suspend fun updateAppliance(appliance: Appliance)

    /** Removes one appliance from the table. */
    @Delete
    suspend fun deleteAppliance(appliance: Appliance)
}
