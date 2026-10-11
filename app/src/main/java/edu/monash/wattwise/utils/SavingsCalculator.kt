package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.ScenarioRules

/** Estimates used by the laundry scenario planner. */
object SavingsCalculator {

    /**
     * Estimates the monthly saving in dollars of a laundry plan compared with
     * the usual routine: washing every day at the earliest start hour and
     * using the dryer every day.
     *
     * The saving has two parts: a cheaper price when the start hour falls in a
     * cheaper tariff period, and less energy when the dryer is used on fewer days.
     * On a flat-rate tariff the start hour makes no difference, so only the
     * dryer days change the result.
     */
    fun estimateMonthlySaving(startHour: Int, dryerDaysPerWeek: Int, tariff: String): Double {
        val usualRate = rateAt(ScenarioRules.EARLIEST_START_HOUR.toInt(), tariff)
        val plannedRate = rateAt(startHour, tariff)

        val usualWeeklyCost = ScenarioRules.DAYS_PER_WEEK *
            (ScenarioRules.WASH_LOAD_KWH + ScenarioRules.DRYER_LOAD_KWH) * usualRate
        val plannedWeeklyCost = (ScenarioRules.DAYS_PER_WEEK * ScenarioRules.WASH_LOAD_KWH +
            dryerDaysPerWeek * ScenarioRules.DRYER_LOAD_KWH) * plannedRate

        val monthlySaving = (usualWeeklyCost - plannedWeeklyCost) * ScenarioRules.WEEKS_PER_MONTH
        return monthlySaving.coerceAtLeast(0.0)
    }

    /** Price per kWh at [hour] on [tariff]. */
    private fun rateAt(hour: Int, tariff: String): Double =
        TariffCalculator.ratePerKwh(TariffCalculator.periodFor(tariff, hour))
}
