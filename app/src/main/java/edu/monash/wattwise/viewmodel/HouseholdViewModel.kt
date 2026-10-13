package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.constants.HouseholdOptions
import edu.monash.wattwise.data.model.HouseholdProfile
import edu.monash.wattwise.data.model.MapPlace
import edu.monash.wattwise.data.repository.CloudBackupRepository
import edu.monash.wattwise.data.repository.HouseholdRepository
import edu.monash.wattwise.data.sensor.DeviceLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Exposes the saved household profile and saves changes from the setup form. */
class HouseholdViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HouseholdRepository(application)
    private val cloudBackup = CloudBackupRepository(application)
    private val locationProvider = DeviceLocationProvider(application)

    /**
     * Finds where the device is and passes it to [onResult], or null if it cannot
     * be found. The map screen asks for the location permission before calling this.
     */
    fun findDeviceLocation(onResult: (MapPlace?) -> Unit) = locationProvider.requestLocation(onResult)

    /** The saved household of [userId]; emits null when nobody is signed in or nothing is saved yet. */
    fun getProfile(userId: String?): Flow<HouseholdProfile?> =
        if (userId == null) flowOf(null) else repository.getProfile(userId)

    /**
     * Checks once whether [userId] has a household, then calls [onResult] on the
     * main thread. Room is checked first. If Room has nothing, for example after
     * the app is installed again, the backup in Firestore is restored into Room.
     * Used straight after sign-in to choose the next screen.
     */
    fun checkProfileExists(userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val exists = withContext(Dispatchers.IO) {
                repository.getProfileOnce(userId) != null || cloudBackup.restore(userId)
            }
            onResult(exists)
        }
    }

    /** Saves the profile to Room, then copies the user's data to the cloud backup. */
    fun saveProfile(profile: HouseholdProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.save(profile)
            cloudBackup.backup(profile.userId)
        }
    }

    /** Saves the monthly budget chosen on the bill forecast screen. */
    fun saveBudget(current: HouseholdProfile, monthlyBudget: Int) {
        saveProfile(current.copy(monthlyBudget = monthlyBudget))
    }

    /** Saves the home location chosen on the map. */
    fun saveLocation(userId: String, current: HouseholdProfile?, place: MapPlace) {
        val profile = current ?: defaultProfile(userId)
        saveProfile(profile.copy(locationName = place.name, latitude = place.latitude, longitude = place.longitude))
    }

    /** Saves the light or dark theme chosen in the More screen. */
    fun saveTheme(userId: String, current: HouseholdProfile?, lightTheme: Boolean) {
        val profile = current ?: defaultProfile(userId)
        saveProfile(profile.copy(lightTheme = lightTheme))
    }

    /** Turns the daily target notification on or off. */
    fun saveUsageAlerts(userId: String, current: HouseholdProfile?, usageAlerts: Boolean) {
        val profile = current ?: defaultProfile(userId)
        saveProfile(profile.copy(usageAlerts = usageAlerts))
    }

    /** The profile used when a setting is saved before the setup form has been filled in. */
    private fun defaultProfile(userId: String) = HouseholdProfile(
        userId = userId,
        homeType = HouseholdOptions.HOME_TYPES.first(),
        tariff = HouseholdOptions.TARIFFS[HouseholdOptions.DEFAULT_TARIFF_INDEX],
        occupants = HouseholdOptions.DEFAULT_OCCUPANTS.toInt(),
        dailyTargetKwh = HouseholdOptions.DEFAULT_DAILY_TARGET_KWH.toInt(),
        billingStartDateMillis = System.currentTimeMillis(),
    )
}
