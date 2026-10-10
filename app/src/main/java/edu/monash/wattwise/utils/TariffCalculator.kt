package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.EnergyUnits
import edu.monash.wattwise.constants.TariffRules
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.model.TariffPeriod

/** Works out which pricing period applies and what energy costs under the household's tariff. */
object TariffCalculator {

    /** The pricing period for [hour] (0 to 23) under the tariff chosen in the household form. */
    fun periodFor(tariff: String, hour: Int): TariffPeriod = when (tariff) {
        TariffRules.TIME_OF_USE ->
            if (hour in TariffRules.PEAK_START_HOUR until TariffRules.PEAK_END_HOUR) TariffPeriod.PEAK
            else TariffPeriod.OFF_PEAK
        TariffRules.SOLAR_FEED_IN ->
            if (hour in TariffRules.SOLAR_START_HOUR until TariffRules.SOLAR_END_HOUR) TariffPeriod.SOLAR
            else TariffPeriod.FLAT
        else -> TariffPeriod.FLAT
    }

    /** Price in dollars per kWh during [period]. */
    fun ratePerKwh(period: TariffPeriod): Double = when (period) {
        TariffPeriod.PEAK -> TariffRules.PEAK_RATE
        TariffPeriod.OFF_PEAK -> TariffRules.OFF_PEAK_RATE
        TariffPeriod.SOLAR -> TariffRules.SOLAR_RATE
        TariffPeriod.FLAT -> TariffRules.FLAT_RATE
    }

    /** The average price per kWh across the 24 hours of a day on [tariff]. */
    fun averageRate(tariff: String): Double =
        (0 until EnergyUnits.HOURS_PER_DAY).sumOf { hour -> ratePerKwh(periodFor(tariff, hour)) } /
            EnergyUnits.HOURS_PER_DAY

    /** Cost in dollars of the saved [usage], pricing each hour by its own period. */
    fun cost(usage: List<HourlyUsage>, tariff: String): Double =
        usage.sumOf { it.kwh * ratePerKwh(periodFor(tariff, it.hour)) }

    /** Short text for the home screen, for example "PEAK UNTIL 9 PM". */
    fun periodText(tariff: String, hour: Int): String = when (periodFor(tariff, hour)) {
        TariffPeriod.PEAK -> "PEAK UNTIL ${Formatters.formatClockHour(TariffRules.PEAK_END_HOUR)}"
        TariffPeriod.OFF_PEAK ->
            if (hour < TariffRules.PEAK_START_HOUR) {
                "OFF-PEAK UNTIL ${Formatters.formatClockHour(TariffRules.PEAK_START_HOUR)}"
            } else {
                "OFF-PEAK NOW"
            }
        TariffPeriod.SOLAR -> "SOLAR HOURS UNTIL ${Formatters.formatClockHour(TariffRules.SOLAR_END_HOUR)}"
        TariffPeriod.FLAT -> "FLAT RATE"
    }
}
