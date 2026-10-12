package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The daily reminder a user set from the energy map: which appliance to
 * start and at what hour. Each signed-in user has one reminder, so the
 * user's id is the primary key and a save replaces their row.
 */
@Entity(tableName = "appliance_reminder")
data class ApplianceReminder(
    /** The Firebase id of the user this reminder belongs to. */
    @PrimaryKey val userId: String,
    /** The appliance to start, for example "Dishwasher". */
    val applianceLabel: String,
    /** Hour of the day (0 to 23) the appliance is planned to start. */
    val startHour: Int,
)

/**
 * The appliances a household can usually run at a different time of day.
 *
 * @param shortLabel a shorter name that fits on a chip.
 * @param loadKwh energy one normal cycle uses, a typical figure for the appliance.
 */
enum class FlexibleAppliance(val label: String, val shortLabel: String, val loadKwh: Double) {
    WASHING_MACHINE("Washing machine", "Washer", 1.0),
    TUMBLE_DRYER("Tumble dryer", "Dryer", 2.5),
    DISHWASHER("Dishwasher", "Dishwasher", 1.2),
}
