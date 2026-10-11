package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.monash.wattwise.constants.WeatherApi

/**
 * The household details entered in the setup form. The app keeps a single
 * profile, so every save uses the same id and replaces the previous row.
 */
@Entity(tableName = "household_profile")
data class HouseholdProfile(
    @PrimaryKey val id: Int = SINGLE_PROFILE_ID,
    val homeType: String,
    val tariff: String,
    val occupants: Int,
    val dailyTargetKwh: Int,
    val billingStartDateMillis: Long,
    /** True for renters, who get advice about habits only; owners also get upgrade advice. */
    val isRenting: Boolean = true,
    /** When true, a notification is shown on the day usage passes the daily target. */
    val usageAlerts: Boolean = true,
    /** The theme chosen in the More screen. */
    val lightTheme: Boolean = false,
    // Home location chosen on the map; the weather is requested for this point.
    val locationName: String = WeatherApi.DEFAULT_LOCATION_NAME,
    val latitude: Double = WeatherApi.DEFAULT_LATITUDE,
    val longitude: Double = WeatherApi.DEFAULT_LONGITUDE,
) {
    companion object {
        const val SINGLE_PROFILE_ID = 1
    }
}
