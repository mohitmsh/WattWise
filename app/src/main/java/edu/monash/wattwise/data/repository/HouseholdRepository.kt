package edu.monash.wattwise.data.repository

import android.app.Application
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.HouseholdProfile
import kotlinx.coroutines.flow.Flow

/** Single access point for the saved household profile. */
class HouseholdRepository(application: Application) {

    private val householdProfileDao = WattWiseDatabase.getDatabase(application).householdProfileDao()

    val profile: Flow<HouseholdProfile?> = householdProfileDao.getProfile()

    suspend fun save(profile: HouseholdProfile) = householdProfileDao.saveProfile(profile)
}
