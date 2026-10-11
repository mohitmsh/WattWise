package edu.monash.wattwise.data

import edu.monash.wattwise.data.model.CoachInsight
import edu.monash.wattwise.data.model.UsagePeriod

/**
 * Placeholder data for the prototype. Each value here is replaced by Room,
 * Retrofit or the sensor simulator once the data layer is implemented.
 */
object SampleData {

    val coachInsights = listOf(
        CoachInsight("Off-peak appliance use", "Running flexible appliances after 9 PM", 24, true),
        CoachInsight("Cooling at 24°C", "Maintaining an efficient cooling set point", 18, true),
        CoachInsight("Shorter dryer cycles", "Reducing high-load laundry periods", 12, true),
        CoachInsight("Standby devices", "Devices drawing power overnight", 7, false),
        CoachInsight("Peak-hour cooking", "Oven use during evening demand", 5, false),
    )

    /** Live power in kilowatts over the last 30 minutes, oldest first. */
    val livePowerReadingsKw = listOf(1.1f, 1.3f, 1.2f, 1.7f, 1.5f, 2.2f, 2.0f, 2.7f, 2.4f, 2.84f)

    /** Daily usage in kilowatt-hours for one week, Monday first. */
    val weeklyUsageKwh = listOf(8f, 11f, 9f, 15f, 13f, 12f, 10f)

    val weekdayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    val usageTotalKwh = mapOf(
        UsagePeriod.DAY to "18.6",
        UsagePeriod.WEEK to "84.6",
        UsagePeriod.MONTH to "342",
    )
}
