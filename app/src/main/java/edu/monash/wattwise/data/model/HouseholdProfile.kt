package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import edu.monash.wattwise.constants.BillRules
import edu.monash.wattwise.constants.WeatherApi

/**
 * The household details entered in the setup form. Each signed-in user has one
 * profile, so the user's id is the primary key and a save replaces their row.
 */
@Entity(tableName = "household_profile")
data class HouseholdProfile(
    /** The Firebase id of the user this household belongs to. */
    @PrimaryKey val userId: String,
    val homeType: String,
    val tariff: String,
    val occupants: Int,
    val dailyTargetKwh: Int,
    val billingStartDateMillis: Long,
    /** The most the household wants to pay for one billing period, in dollars. */
    val monthlyBudget: Int = BillRules.DEFAULT_BUDGET,
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
)
