package edu.monash.wattwise.data.model

/**
 * One row of the REFIT smart-meter file for House 1. All power values are in
 * watts. Only the appliance channels the app gives advice about are kept.
 */
data class MeterReading(
    val time: String,
    val unixSeconds: Long,
    val aggregateWatts: Int,
    val tumbleDryerWatts: Int,
    val washingMachineWatts: Int,
    val dishwasherWatts: Int,
    val heaterWatts: Int,
) {
    /** Hour of the day (0 to 23) taken from the "HH:mm:ss" time text. */
    val hour: Int get() = time.substringBefore(":").toIntOrNull() ?: 0

    /** Power in watts on the channel for [appliance]. */
    fun wattsFor(appliance: MeterAppliance): Int = when (appliance) {
        MeterAppliance.TUMBLE_DRYER -> tumbleDryerWatts
        MeterAppliance.WASHING_MACHINE -> washingMachineWatts
        MeterAppliance.DISHWASHER -> dishwasherWatts
        MeterAppliance.HEATER -> heaterWatts
    }
}
