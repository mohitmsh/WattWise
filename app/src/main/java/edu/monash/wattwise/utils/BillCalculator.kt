package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.BillRules
import edu.monash.wattwise.data.model.HourlyUsage

/**
 * The estimated bill for the current billing period.
 *
 * @param periodStartMillis midnight on the first day of the billing period.
 * @param completedDays days of the period that have finished.
 * @param daysLeft days still to come, counting today.
 * @param costSoFar cost of the energy used on the completed days, in dollars.
 * @param averageDailyCost usual energy cost of one day, in dollars.
 * @param supplyCharge the fixed charge for the whole period, in dollars.
 * @param projectedBill the estimated total for the period, in dollars.
 * @param lowEstimate the total if the remaining days are like the household's cheapest days.
 * @param highEstimate the total if the remaining days are like the household's most expensive days.
 * @param cumulativeCost the running total after each day of the period, for the chart.
 */
data class BillForecast(
    val periodStartMillis: Long,
    val completedDays: Int,
    val daysLeft: Int,
    val costSoFar: Double,
    val averageDailyCost: Double,
    val supplyCharge: Double,
    val projectedBill: Double,
    val lowEstimate: Double,
    val highEstimate: Double,
    val cumulativeCost: List<Float>,
)

/** Estimates the electricity bill from the usage history saved in Room. */
object BillCalculator {

    /**
     * Midnight on the first day of the billing period that contains [nowMillis].
     * The date from the household form starts a period, and a new period
     * starts every [BillRules.PERIOD_DAYS] days after it.
     */
    fun periodStart(billingStartMillis: Long, nowMillis: Long): Long {
        val today = DateUtils.startOfDay(nowMillis)
        var start = DateUtils.startOfDay(billingStartMillis)
        if (start > today) return today
        while (DateUtils.addDays(start, BillRules.PERIOD_DAYS) <= today) {
            start = DateUtils.addDays(start, BillRules.PERIOD_DAYS)
        }
        return start
    }

    /**
     * Builds the forecast in three steps:
     * 1. add up the cost of each finished day of the billing period;
     * 2. assume each remaining day costs the household's usual daily amount;
     * 3. add the fixed supply charge for the whole period.
     *
     * Today is counted as a remaining day, because it has not finished yet.
     */
    fun forecast(
        usage: List<HourlyUsage>,
        tariff: String,
        billingStartMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
    ): BillForecast {
        val today = DateUtils.startOfDay(nowMillis)
        val start = periodStart(billingStartMillis, nowMillis)
        val usageByDay = usage.groupBy { it.dayStartMillis }

        // Step 1: cost of every finished day of this period.
        val periodDays = (0 until BillRules.PERIOD_DAYS).map { DateUtils.addDays(start, it) }
        val completedCosts = periodDays.filter { it < today }.map { day -> dailyCost(usageByDay[day], tariff) }
        val costSoFar = completedCosts.sum()

        // Step 2: the usual cost of a day, from every finished day saved in Room.
        val historyCosts = usageByDay.filterKeys { it < today }.values.map { dailyCost(it, tariff) }.sorted()
        val averageDailyCost = if (historyCosts.isEmpty()) 0.0 else historyCosts.average()
        val cheapDayCost = historyCosts.take(BillRules.RANGE_DAYS).averageOrZero()
        val expensiveDayCost = historyCosts.takeLast(BillRules.RANGE_DAYS).averageOrZero()
        val daysLeft = BillRules.PERIOD_DAYS - completedCosts.size

        // Step 3: the supply charge is the same every day, whatever is used.
        val supplyCharge = BillRules.DAILY_SUPPLY_CHARGE * BillRules.PERIOD_DAYS

        // Running total for the chart: real costs for finished days, the usual cost for the others.
        var runningTotal = 0.0
        val cumulativeCost = periodDays.indices.map { index ->
            val energyCost = if (index < completedCosts.size) completedCosts[index] else averageDailyCost
            runningTotal += energyCost + BillRules.DAILY_SUPPLY_CHARGE
            runningTotal.toFloat()
        }

        return BillForecast(
            periodStartMillis = start,
            completedDays = completedCosts.size,
            daysLeft = daysLeft,
            costSoFar = costSoFar,
            averageDailyCost = averageDailyCost,
            supplyCharge = supplyCharge,
            projectedBill = costSoFar + averageDailyCost * daysLeft + supplyCharge,
            lowEstimate = costSoFar + cheapDayCost * daysLeft + supplyCharge,
            highEstimate = costSoFar + expensiveDayCost * daysLeft + supplyCharge,
            cumulativeCost = cumulativeCost,
        )
    }

    /**
     * The energy in kWh the household can use on each remaining day and still
     * finish the period inside [budget]. Zero when the budget is already used up.
     */
    fun dailyAllowanceKwh(forecast: BillForecast, budget: Int, tariff: String): Double {
        if (forecast.daysLeft <= 0) return 0.0
        val moneyLeftForEnergy = budget - forecast.supplyCharge - forecast.costSoFar
        val perDay = moneyLeftForEnergy / forecast.daysLeft
        return (perDay / TariffCalculator.averageRate(tariff)).coerceAtLeast(0.0)
    }

    /** The budget spread evenly over the period: the running total that would end exactly on budget. */
    fun budgetPace(budget: Int): List<Float> =
        (1..BillRules.PERIOD_DAYS).map { day -> budget.toFloat() * day / BillRules.PERIOD_DAYS }

    /** Cost in dollars of one day of saved usage; zero when nothing is saved for the day. */
    private fun dailyCost(dayUsage: List<HourlyUsage>?, tariff: String): Double =
        if (dayUsage == null) 0.0 else TariffCalculator.cost(dayUsage, tariff)

    /** The average of the list, or zero for an empty list. */
    private fun List<Double>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()
}
