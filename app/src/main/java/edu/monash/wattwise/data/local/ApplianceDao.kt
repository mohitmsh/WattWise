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

    /** Emits the full list again every time the table changes. */
    @Query("SELECT * FROM appliances ORDER BY name ASC")
    fun getAllAppliances(): Flow<List<Appliance>>

    @Insert
    suspend fun insertAppliance(appliance: Appliance)

    @Update
    suspend fun updateAppliance(appliance: Appliance)

    @Delete
    suspend fun deleteAppliance(appliance: Appliance)
}
