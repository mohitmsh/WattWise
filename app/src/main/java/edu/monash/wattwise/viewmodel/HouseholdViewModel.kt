package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.constants.HouseholdOptions
import edu.monash.wattwise.data.model.HouseholdProfile
import edu.monash.wattwise.data.model.MapPlace
import edu.monash.wattwise.data.repository.HouseholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Exposes the saved household profile and saves changes from the setup form. */
class HouseholdViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HouseholdRepository(application)

    val profile: Flow<HouseholdProfile?> = repository.profile

    fun saveProfile(profile: HouseholdProfile) {
        viewModelScope.launch(Dispatchers.IO) { repository.save(profile) }
    }

    /** Saves the home location chosen on the map. */
    fun saveLocation(current: HouseholdProfile?, place: MapPlace) {
        val profile = current ?: defaultProfile()
        saveProfile(profile.copy(locationName = place.name, latitude = place.latitude, longitude = place.longitude))
    }

    /** Saves the light or dark theme chosen in the More screen. */
    fun saveTheme(current: HouseholdProfile?, lightTheme: Boolean) {
        val profile = current ?: defaultProfile()
        saveProfile(profile.copy(lightTheme = lightTheme))
    }

    /** Turns the daily target notification on or off. */
    fun saveUsageAlerts(current: HouseholdProfile?, usageAlerts: Boolean) {
        val profile = current ?: defaultProfile()
        saveProfile(profile.copy(usageAlerts = usageAlerts))
    }

    /** The profile used when a setting is saved before the setup form has been filled in. */
    private fun defaultProfile() = HouseholdProfile(
        homeType = HouseholdOptions.HOME_TYPES.first(),
        tariff = HouseholdOptions.TARIFFS[HouseholdOptions.DEFAULT_TARIFF_INDEX],
        occupants = HouseholdOptions.DEFAULT_OCCUPANTS.toInt(),
        dailyTargetKwh = HouseholdOptions.DEFAULT_DAILY_TARGET_KWH.toInt(),
        billingStartDateMillis = System.currentTimeMillis(),
    )
}
