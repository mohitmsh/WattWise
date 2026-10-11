package edu.monash.wattwise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.monash.wattwise.data.model.ScenarioPlan
import kotlinx.coroutines.flow.Flow

/** Database operations for the single-row "scenario_plan" table. */
@Dao
interface ScenarioPlanDao {

    /** Emits null until a plan has been saved, and again every time it changes. */
    @Query("SELECT * FROM scenario_plan WHERE id = :id")
    fun getPlan(id: Int = ScenarioPlan.SINGLE_PLAN_ID): Flow<ScenarioPlan?>

    /** Reads the plan once, for code that runs outside the UI such as the boot receiver. */
    @Query("SELECT * FROM scenario_plan WHERE id = :id")
    suspend fun getPlanOnce(id: Int = ScenarioPlan.SINGLE_PLAN_ID): ScenarioPlan?

    /** Inserts the plan, or replaces the existing row because the id is the same. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlan(plan: ScenarioPlan)

    @Query("DELETE FROM scenario_plan")
    suspend fun deletePlan()
}
