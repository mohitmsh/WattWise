package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.constants.EnergyUnits
import edu.monash.wattwise.constants.TariffRules
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.model.TariffPeriod
import edu.monash.wattwise.data.model.TrendMetric
import java.util.Calendar

/** Calculations for the usage report: one value per day or hour for the chosen metric. */
object TrendCalculator {

    /** The [dayCount] days ending on [lastDay], oldest first, each as its midnight in milliseconds. */
    fun daysEndingOn(lastDay: Long, dayCount: Int): List<Long> =
        (dayCount - 1 downTo 0).map { daysBack -> DateUtils.addDays(lastDay, -daysBack) }

    /**
     * The value of [metric] for one day, from that day's saved hours. Returns
     * null when nothing is saved for the day, so it can be left out of averages.
     */
    fun dailyValue(dayUsage: List<HourlyUsage>?, metric: TrendMetric, tariff: String): Double? {
        if (dayUsage.isNullOrEmpty()) return null
        return when (metric) {
            TrendMetric.ENERGY -> dayUsage.sumOf { it.kwh }
            TrendMetric.COST -> TariffCalculator.cost(dayUsage, tariff)
            TrendMetric.PEAK_ENERGY -> dayUsage.filter { isPeakHour(it.hour) }.sumOf { it.kwh }
            TrendMetric.BASE_LOAD -> {
                // One kWh used over one hour is an average power of one kW.
                val nightHours = dayUsage.filter { it.hour in ContextThresholds.BASE_LOAD_HOURS }
                if (nightHours.isEmpty()) null else nightHours.sumOf { it.kwh } / nightHours.size
            }
        }
    }

    /** The 24 hourly values for one day: cost in dollars for the cost metric, otherwise kWh. */
    fun hourlyValues(dayUsage: List<HourlyUsage>?, metric: TrendMetric, tariff: String): List<Float> {
        val hours = MutableList(EnergyUnits.HOURS_PER_DAY) { 0f }
        dayUsage?.forEach { usage ->
            val value = if (metric == TrendMetric.COST) {
                usage.kwh * TariffCalculator.ratePerKwh(TariffCalculator.periodFor(tariff, usage.hour))
            } else {
                usage.kwh
            }
            hours[usage.hour] = value.toFloat()
        }
        return hours
    }

    /** Average of the values that exist; days with no data are skipped. */
    fun average(values: List<Double?>): Double {
        val present = values.filterNotNull()
        return if (present.isEmpty()) 0.0 else present.sum() / present.size
    }

    /** True when all 24 hours of the day are saved, so the day can be compared fairly. */
    fun isCompleteDay(dayUsage: List<HourlyUsage>?): Boolean = dayUsage?.size == EnergyUnits.HOURS_PER_DAY

    /**
     * The household's usual energy for the weekday of [day]: the average total
     * of the other complete days in the history that fall on the same weekday.
     */
    fun typicalForWeekday(usageByDay: Map<Long, List<HourlyUsage>>, day: Long): Double {
        val weekday = weekdayOf(day)
        val sameWeekdayTotals = usageByDay
            .filter { (otherDay, hours) -> otherDay != day && weekdayOf(otherDay) == weekday && isCompleteDay(hours) }
            .map { (_, hours) -> hours.sumOf { it.kwh } }
        return if (sameWeekdayTotals.isEmpty()) 0.0 else sameWeekdayTotals.sum() / sameWeekdayTotals.size
    }

    /** Energy in kWh for each part of the day, in the order night, morning, afternoon, evening. */
    fun dayPartTotals(usage: List<HourlyUsage>): List<Pair<String, Double>> = listOf(
        "Night" to (0 until 6),
        "Morning" to (6 until 12),
        "Afternoon" to (12 until 18),
        "Evening" to (18 until 24),
    ).map { (name, hours) -> name to usage.filter { it.hour in hours }.sumOf { it.kwh } }

    /** Peak hours are fixed, so peak-period energy can be compared whatever the tariff is. */
    fun isPeakHour(hour: Int): Boolean = hour in TariffRules.PEAK_START_HOUR until TariffRules.PEAK_END_HOUR

    /** Energy in kWh used in each tariff period that appears in [usage]. */
    fun energyByTariffPeriod(usage: List<HourlyUsage>, tariff: String): Map<TariffPeriod, Double> =
        usage.groupBy { TariffCalculator.periodFor(tariff, it.hour) }
            .mapValues { (_, hours) -> hours.sumOf { it.kwh } }

    private fun weekdayOf(day: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = day
        return calendar.get(Calendar.DAY_OF_WEEK)
    }
}
