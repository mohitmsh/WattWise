package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.ScenarioRules

/** Estimates used by the laundry scenario planner. */
object SavingsCalculator {

    /**
     * Estimates the monthly saving in dollars from starting laundry later and
     * using the dryer on fewer days. The result is never negative.
     */
    fun estimateMonthlySaving(startHour: Float, dryerDaysPerWeek: Float): Double {
        val hoursShifted = (startHour - ScenarioRules.EARLIEST_START_HOUR).coerceAtLeast(0f)
        val dryerDaysAvoided = ScenarioRules.MAX_DRYER_DAYS - dryerDaysPerWeek
        val saving = hoursShifted * ScenarioRules.SAVING_PER_HOUR_SHIFTED +
            dryerDaysAvoided * ScenarioRules.SAVING_PER_DRYER_DAY_AVOIDED
        return saving.coerceAtLeast(0f).toDouble()
    }
}
