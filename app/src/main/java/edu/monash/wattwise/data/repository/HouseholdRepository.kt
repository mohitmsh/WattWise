package edu.monash.wattwise.data.repository

import android.content.Context
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.HouseholdProfile
import kotlinx.coroutines.flow.Flow

/** Single access point for the saved household profile. */
class HouseholdRepository(context: Context) {

    private val householdProfileDao = WattWiseDatabase.getDatabase(context).householdProfileDao()

    /** The user's household as a Flow; it emits null until the setup form is saved. */
    fun getProfile(userId: String): Flow<HouseholdProfile?> = householdProfileDao.getProfile(userId)

    /** Reads the household one time, for code that does not need updates. */
    suspend fun getProfileOnce(userId: String): HouseholdProfile? = householdProfileDao.getProfileOnce(userId)

    /** Saves the household and replaces the user's earlier row. */
    suspend fun save(profile: HouseholdProfile) = householdProfileDao.saveProfile(profile)
}
