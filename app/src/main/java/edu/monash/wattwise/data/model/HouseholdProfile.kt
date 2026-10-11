package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
) {
    companion object {
        const val SINGLE_PROFILE_ID = 1
    }
}
