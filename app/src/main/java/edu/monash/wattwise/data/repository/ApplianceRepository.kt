package edu.monash.wattwise.data.repository

import android.app.Application
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.Appliance
import kotlinx.coroutines.flow.Flow

/** Single access point for appliance data, so the ViewModel never touches the DAO directly. */
class ApplianceRepository(application: Application) {

    private val applianceDao = WattWiseDatabase.getDatabase(application).applianceDao()

    val allAppliances: Flow<List<Appliance>> = applianceDao.getAllAppliances()

    suspend fun insert(appliance: Appliance) = applianceDao.insertAppliance(appliance)

    suspend fun update(appliance: Appliance) = applianceDao.updateAppliance(appliance)

    suspend fun delete(appliance: Appliance) = applianceDao.deleteAppliance(appliance)
}
