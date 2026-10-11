package edu.monash.wattwise.data.repository

import android.content.Context
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.HouseholdProfile
import kotlinx.coroutines.flow.Flow

/** Single access point for the saved household profile. */
class HouseholdRepository(context: Context) {

    private val householdProfileDao = WattWiseDatabase.getDatabase(context).householdProfileDao()

    val profile: Flow<HouseholdProfile?> = householdProfileDao.getProfile()

    suspend fun getProfileOnce(): HouseholdProfile? = householdProfileDao.getProfileOnce()

    suspend fun save(profile: HouseholdProfile) = householdProfileDao.saveProfile(profile)
}
