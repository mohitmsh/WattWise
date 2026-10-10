package edu.monash.wattwise.data.model

import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.utils.TariffCalculator

/**
 * Everything the app knows about the household's situation right now. The
 * context rules read this to decide which recommendations to show.
 *
 * Sensory input: [reading] from the smart-meter simulator.
 * Non-sensory inputs: [hour] with the [tariff], [outsideTemperature] from the
 * weather API, and [isRenting] and [dailyTargetKwh] from the household form.
 */
data class EnergyContext(
    val reading: MeterReading,
    val hour: Int,
    val todayKwh: Double,
    /** The household's usual power in kW for this hour, from its own history. */
    val typicalKw: Double,
    /** The household's usual kWh from midnight up to this hour. */
    val typicalKwhByNow: Double,
    val dailyTargetKwh: Int,
    val tariff: String,
    val isRenting: Boolean,
    /** Null while the weather request is loading or if it failed. */
    val outsideTemperature: Double?,
    /** The appliance profiles the user has saved, used to suggest what may be running. */
    val appliances: List<Appliance> = emptyList(),
) {
    val liveKw: Double get() = EnergyCalculator.wattsToKilowatts(reading.aggregateWatts)

    val tariffPeriod: TariffPeriod get() = TariffCalculator.periodFor(tariff, hour)

    /** Percentage the live power is above or below the usual level for this hour. */
    val livePercentVsTypical: Int get() = EnergyCalculator.percentDifference(liveKw, typicalKw)

    /** Percentage today's energy is above or below the usual amount by this hour. */
    val todayPercentVsTypical: Int get() = EnergyCalculator.percentDifference(todayKwh, typicalKwhByNow)

    /** Share of the daily target used so far, from 0 to 1 or more. */
    val targetProgress: Float get() = if (dailyTargetKwh <= 0) 0f else (todayKwh / dailyTargetKwh).toFloat()

    /**
     * Score out of 100 comparing today with the household's own baseline:
     * using the usual amount scores [ContextThresholds.SCORE_AT_TYPICAL], using
     * less scores higher and using more scores lower.
     */
    val efficiencyScore: Int
        get() {
            if (todayKwh <= 0.0 || typicalKwhByNow <= 0.0) return ContextThresholds.SCORE_AT_TYPICAL
            val score = ContextThresholds.SCORE_AT_TYPICAL * typicalKwhByNow / todayKwh
            return score.toInt().coerceIn(0, ContextThresholds.MAX_SCORE)
        }
}
