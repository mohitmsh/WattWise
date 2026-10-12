package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A household appliance profile, stored as one row in the "appliances" table. */
@Entity(tableName = "appliances")
data class Appliance(
    // 0 tells Room to generate the next id when the row is inserted.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** The Firebase id of the user who added this appliance. */
    val userId: String,
    val name: String,
    val category: ApplianceCategory,
    val watts: Int,
    /** Hours the appliance runs on a normal day, entered by the user. */
    val hoursPerDay: Double,
    /**
     * The smart-meter channel that measures this appliance, like a smart plug.
     * Null means it is not measured, so its energy is estimated from power and hours.
     */
    val meterChannel: MeterAppliance? = null,
)

/**
 * Groups shown in the category dropdown of the appliance form.
 *
 * @param isFlexible true when the appliance can usually be run at a different time of day.
 */
enum class ApplianceCategory(val label: String, val isFlexible: Boolean) {
    KITCHEN("Kitchen", true),
    // A fridge or freezer runs all day, so it cannot be moved to a cheaper time.
    REFRIGERATION("Fridge and freezer", false),
    COOLING("Cooling", false),
    HEATING("Heating", false),
    LAUNDRY("Laundry", true),
    ELECTRONICS("Electronics", false),
}

/** How large an appliance's share of the household's daily energy target is. */
enum class ApplianceStatus(val label: String) {
    HIGH("High use"),
    NORMAL("Normal"),
}
