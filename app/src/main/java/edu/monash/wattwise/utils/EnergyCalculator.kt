package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.ApplianceLimits
import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.constants.EnergyUnits
import edu.monash.wattwise.constants.SmartMeter
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceRun
import edu.monash.wattwise.data.model.ApplianceStatus
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.model.MeterAppliance
import edu.monash.wattwise.data.model.MeterReading

/** Turns meter readings and saved hourly usage into the figures the screens show. */
object EnergyCalculator {

    fun wattsToKilowatts(watts: Int): Double = watts / EnergyUnits.WATTS_PER_KILOWATT

    /** Energy in kWh an appliance uses on a normal day: its power multiplied by its hours of use. */
    fun applianceDailyKwh(appliance: Appliance): Double = wattsToKilowatts(appliance.watts) * appliance.hoursPerDay

    /**
     * Energy in kWh the meter has measured for [appliance] today, added up from
     * its runs. Returns null when the appliance is not linked to a meter channel.
     */
    fun measuredKwhToday(appliance: Appliance, runs: List<ApplianceRun>): Double? {
        val channel = appliance.meterChannel ?: return null
        return runs.filter { it.appliance == channel }.sumOf { it.kwh }
    }

    /** High use when the appliance alone takes a large share of the daily target. */
    fun applianceStatus(appliance: Appliance, dailyTargetKwh: Int): ApplianceStatus =
        if (applianceDailyKwh(appliance) >= dailyTargetKwh * ApplianceLimits.HIGH_USE_TARGET_SHARE) {
            ApplianceStatus.HIGH
        } else {
            ApplianceStatus.NORMAL
        }

    /** Energy in kWh used between two readings: power multiplied by the time between them. */
    fun energyBetween(previous: MeterReading, current: MeterReading): Double {
        val seconds = (current.unixSeconds - previous.unixSeconds).coerceIn(0, SmartMeter.MAX_GAP_SECONDS)
        return wattsToKilowatts(current.aggregateWatts) * seconds / EnergyUnits.SECONDS_PER_HOUR
    }

    /** Energy in kWh used from the start of the simulated day up to [reading]. */
    fun energyUsedBefore(dayReadings: List<MeterReading>, reading: MeterReading): Double {
        var total = 0.0
        for (index in 1 until dayReadings.size) {
            if (dayReadings[index].unixSeconds > reading.unixSeconds) break
            total += energyBetween(dayReadings[index - 1], dayReadings[index])
        }
        return total
    }

    /** Total kWh saved for one day. */
    fun totalForDay(usage: List<HourlyUsage>, dayStartMillis: Long): Double =
        usage.filter { it.dayStartMillis == dayStartMillis }.sumOf { it.kwh }

    /** The 24 hourly kWh values for one day; hours with nothing saved are zero. */
    fun hourlyForDay(usage: List<HourlyUsage>, dayStartMillis: Long): List<Float> {
        val hours = MutableList(EnergyUnits.HOURS_PER_DAY) { 0f }
        usage.filter { it.dayStartMillis == dayStartMillis }.forEach { hours[it.hour] = it.kwh.toFloat() }
        return hours
    }

    /** Total kWh for each of the [dayCount] days ending on [lastDayStartMillis], oldest first. */
    fun dailyTotals(usage: List<HourlyUsage>, lastDayStartMillis: Long, dayCount: Int): List<Float> =
        (dayCount - 1 downTo 0).map { daysBack ->
            totalForDay(usage, DateUtils.addDays(lastDayStartMillis, -daysBack)).toFloat()
        }

    /**
     * The household's own baseline: average kWh used during [hour] across the
     * saved history. One kWh used over one hour is an average power of one kW,
     * so the same number is also the typical power in kW for that hour.
     */
    fun typicalForHour(usage: List<HourlyUsage>, hour: Int): Double {
        val sameHour = usage.filter { it.hour == hour }
        return if (sameHour.isEmpty()) 0.0 else sameHour.sumOf { it.kwh } / sameHour.size
    }

    /** Typical kWh used from midnight up to the start of [hour]. */
    fun typicalUntilHour(usage: List<HourlyUsage>, hour: Int): Double =
        (0 until hour).sumOf { typicalForHour(usage, it) }

    /** Percentage that [actual] is above (positive) or below (negative) [typical]. */
    fun percentDifference(actual: Double, typical: Double): Int =
        if (typical <= 0.0) 0 else ((actual - typical) / typical * 100).toInt()

    /**
     * Finds the periods today when each appliance was switched on, newest first.
     * Only readings up to [latest] are used, so the list grows during the day.
     */
    fun applianceRuns(dayReadings: List<MeterReading>, latest: MeterReading): List<ApplianceRun> {
        val runs = mutableListOf<ApplianceRun>()
        val readingsSoFar = dayReadings.filter { it.unixSeconds <= latest.unixSeconds }

        for (appliance in MeterAppliance.entries) {
            var start: MeterReading? = null
            var end: MeterReading? = null
            var previous: MeterReading? = null
            var kwh = 0.0

            // Saves the run that has just ended, unless it was too short to be real.
            fun closeRun() {
                val first = start ?: return
                val last = end ?: return
                if (last.unixSeconds - first.unixSeconds >= ContextThresholds.MIN_RUN_SECONDS) {
                    val isRunning = latest.unixSeconds - last.unixSeconds < ContextThresholds.RUN_MERGE_GAP_SECONDS &&
                        latest.wattsFor(appliance) >= ContextThresholds.APPLIANCE_ON_WATTS
                    runs.add(ApplianceRun(appliance, first.time.take(5), last.time.take(5), kwh, isRunning))
                }
                start = null
                end = null
                kwh = 0.0
            }

            for (reading in readingsSoFar) {
                val watts = reading.wattsFor(appliance)
                if (watts >= ContextThresholds.APPLIANCE_ON_WATTS) {
                    val lastOn = end
                    // A long gap since the appliance was last on means a new run has started.
                    if (lastOn != null && reading.unixSeconds - lastOn.unixSeconds > ContextThresholds.RUN_MERGE_GAP_SECONDS) {
                        closeRun()
                    }
                    if (start == null) start = reading
                    end = reading
                }
                val last = previous
                if (last != null && start != null) {
                    val seconds = (reading.unixSeconds - last.unixSeconds).coerceIn(0, SmartMeter.MAX_GAP_SECONDS)
                    kwh += wattsToKilowatts(watts) * seconds / EnergyUnits.SECONDS_PER_HOUR
                }
                previous = reading
            }
            closeRun()
        }
        return runs.sortedByDescending { it.startTime }
    }
}
