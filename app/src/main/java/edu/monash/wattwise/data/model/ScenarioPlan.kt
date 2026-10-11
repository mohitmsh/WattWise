package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The laundry routine saved from the scenario planner. The app keeps a single
 * plan, so every save uses the same id and replaces the previous row.
 */
@Entity(tableName = "scenario_plan")
data class ScenarioPlan(
    @PrimaryKey val id: Int = SINGLE_PLAN_ID,
    /** Hour of the day (0 to 23) the laundry is planned to start. */
    val startHour: Int,
    val dryerDaysPerWeek: Int,
    val monthlySaving: Double,
    /** True while a daily reminder alarm is scheduled for this plan. */
    val reminderEnabled: Boolean,
) {
    companion object {
        const val SINGLE_PLAN_ID = 1
    }
}
