package edu.monash.wattwise.data.repository

import android.content.Context
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.ScenarioPlan
import kotlinx.coroutines.flow.Flow

/** Single access point for the saved scenario plan. */
class ScenarioRepository(context: Context) {

    private val scenarioPlanDao = WattWiseDatabase.getDatabase(context).scenarioPlanDao()

    val plan: Flow<ScenarioPlan?> = scenarioPlanDao.getPlan()

    suspend fun getPlanOnce(): ScenarioPlan? = scenarioPlanDao.getPlanOnce()

    suspend fun save(plan: ScenarioPlan) = scenarioPlanDao.savePlan(plan)

    suspend fun delete() = scenarioPlanDao.deletePlan()
}
