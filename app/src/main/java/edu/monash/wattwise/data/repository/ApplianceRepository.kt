package edu.monash.wattwise.data.repository

import android.app.Application
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceCategory
import kotlinx.coroutines.flow.Flow

/** Single access point for appliance data, so the ViewModel never touches the DAO directly. */
class ApplianceRepository(application: Application) {

    private val applianceDao = WattWiseDatabase.getDatabase(application).applianceDao()

    /** The user's appliances as a Flow, so the screens update when the table changes. */
    fun getAppliances(userId: String): Flow<List<Appliance>> = applianceDao.getAppliances(userId)

    /**
     * Searches by appliance name or category. Room stores a category as its
     * enum name (for example LAUNDRY), so the categories whose label matches
     * the search text are worked out here and passed to the query by name.
     */
    fun searchAppliances(userId: String, query: String): Flow<List<Appliance>> {
        val matchingCategories = ApplianceCategory.entries
            .filter { it.label.contains(query, ignoreCase = true) }
            .map { it.name }
        return applianceDao.searchAppliances(userId, query, matchingCategories)
    }

    /** Adds a new appliance. */
    suspend fun insert(appliance: Appliance) = applianceDao.insertAppliance(appliance)

    /** Saves the changes to an appliance. */
    suspend fun update(appliance: Appliance) = applianceDao.updateAppliance(appliance)

    /** Removes an appliance. */
    suspend fun delete(appliance: Appliance) = applianceDao.deleteAppliance(appliance)
}
