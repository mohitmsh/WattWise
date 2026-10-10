package edu.monash.wattwise.data.model

/**
 * One period today when an appliance was switched on, found by reading the
 * appliance's channel in the smart-meter data.
 *
 * @param startTime and [endTime] are "HH:mm" text.
 * @param isRunning true when the appliance is still on at the latest reading.
 */
data class ApplianceRun(
    val appliance: MeterAppliance,
    val startTime: String,
    val endTime: String,
    val kwh: Double,
    val isRunning: Boolean,
)
