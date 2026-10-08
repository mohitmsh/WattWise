package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A household appliance profile, stored as one row in the "appliances" table. */
@Entity(tableName = "appliances")
data class Appliance(
    // 0 tells Room to generate the next id when the row is inserted.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: ApplianceCategory,
    val watts: Int,
    val dailyCost: Double,
    val status: ApplianceStatus,
)

/** Groups shown in the category dropdown of the appliance form. */
enum class ApplianceCategory(val label: String) {
    KITCHEN("Kitchen"),
    COOLING("Cooling"),
    HEATING("Heating"),
    LAUNDRY("Laundry"),
    ELECTRONICS("Electronics"),
}

/** How an appliance's usage compares with its expected range. */
enum class ApplianceStatus(val label: String) {
    HIGH("High"),
    NORMAL("Normal"),
    SCHEDULED("Scheduled"),
    NEW("New"),
}
