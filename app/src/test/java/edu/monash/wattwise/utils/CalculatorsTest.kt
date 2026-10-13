package edu.monash.wattwise.utils

import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.model.MeterReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks for the calculations behind the screens. These classes use no Android
 * code, so the tests run on the computer without an emulator.
 */
class CalculatorsTest {

    // ---- BillCalculator

    @Test
    fun billingPeriodStartsOnTheBillingDate() {
        val now = System.currentTimeMillis()
        val tenDaysAgo = DateUtils.addDays(DateUtils.startOfDay(now), -10)
        assertEquals(tenDaysAgo, BillCalculator.periodStart(tenDaysAgo, now))
    }

    @Test
    fun billingPeriodRollsForwardEveryThirtyDays() {
        val now = System.currentTimeMillis()
        val today = DateUtils.startOfDay(now)
        // A billing date 40 days ago means the current period started 10 days ago.
        val start = BillCalculator.periodStart(DateUtils.addDays(today, -40), now)
        assertEquals(DateUtils.addDays(today, -10), start)
    }

    @Test
    fun forecastAddsFinishedDaysUsualDaysAndSupplyCharge() {
        val now = System.currentTimeMillis()
        val today = DateUtils.startOfDay(now)
        // Ten finished days of 10 kWh each on a flat rate of $0.30: $3 a day.
        val usage = (1..10).map { daysAgo ->
            HourlyUsage(dayStartMillis = DateUtils.addDays(today, -daysAgo), hour = 12, kwh = 10.0)
        }
        val forecast = BillCalculator.forecast(usage, FLAT_RATE, DateUtils.addDays(today, -10), now)

        assertEquals(10, forecast.completedDays)
        assertEquals(20, forecast.daysLeft)
        assertEquals(30.0, forecast.costSoFar, TOLERANCE)
        assertEquals(3.0, forecast.averageDailyCost, TOLERANCE)
        // $30 used + 20 days at $3 + 30 days of supply charge at $1.10.
        assertEquals(30.0 + 60.0 + 33.0, forecast.projectedBill, TOLERANCE)
        // Every day costs the same, so the low and high estimates equal the estimate.
        assertEquals(forecast.projectedBill, forecast.lowEstimate, TOLERANCE)
        assertEquals(forecast.projectedBill, forecast.highEstimate, TOLERANCE)
        assertEquals(forecast.projectedBill.toFloat(), forecast.cumulativeCost.last(), 0.01f)
    }

    @Test
    fun dailyAllowanceIsZeroWhenTheBudgetIsUsedUp() {
        val now = System.currentTimeMillis()
        val today = DateUtils.startOfDay(now)
        val usage = (1..10).map { daysAgo ->
            HourlyUsage(dayStartMillis = DateUtils.addDays(today, -daysAgo), hour = 12, kwh = 100.0)
        }
        val forecast = BillCalculator.forecast(usage, FLAT_RATE, DateUtils.addDays(today, -10), now)
        assertEquals(0.0, BillCalculator.dailyAllowanceKwh(forecast, budget = 100, tariff = FLAT_RATE), TOLERANCE)
    }

    @Test
    fun dailyAllowanceSharesTheMoneyLeftOverTheDaysLeft() {
        val now = System.currentTimeMillis()
        val today = DateUtils.startOfDay(now)
        val usage = (1..10).map { daysAgo ->
            HourlyUsage(dayStartMillis = DateUtils.addDays(today, -daysAgo), hour = 12, kwh = 10.0)
        }
        val forecast = BillCalculator.forecast(usage, FLAT_RATE, DateUtils.addDays(today, -10), now)
        // $123 budget - $33 supply - $30 used = $60 for 20 days = $3 a day = 10 kWh at $0.30.
        assertEquals(10.0, BillCalculator.dailyAllowanceKwh(forecast, budget = 123, tariff = FLAT_RATE), TOLERANCE)
    }

    // ---- Formatters

    @Test
    fun countAddsAnSOnlyForMoreThanOne() {
        assertEquals("1 day", Formatters.formatCount(1, "day"))
        assertEquals("3 days", Formatters.formatCount(3, "day"))
        assertEquals("0 appliances", Formatters.formatCount(0, "appliance"))
    }

    @Test
    fun reminderTimeIsBeforeTheStartHour() {
        assertEquals("8:45 PM", Formatters.formatReminderTime(startHour = 21, leadMinutes = 15))
        assertEquals("11:45 PM", Formatters.formatReminderTime(startHour = 0, leadMinutes = 15))
    }

    @Test
    fun percentIsRoundedNotCutOff() {
        assertEquals(27, Formatters.toPercent(0.268))
        assertEquals(129, Formatters.toPercent(1.29f))
    }

    // ---- DateUtils

    @Test
    fun pickerDateConvertsBackToTheSameDay() {
        val now = System.currentTimeMillis()
        val pickerMillis = DateUtils.localDayToPickerMillis(now)
        assertEquals(DateUtils.startOfDay(now), DateUtils.pickerMillisToLocalDay(pickerMillis))
    }

    // ---- EnergyCalculator

    @Test
    fun liveHourMakesTodayMatchTheMeter() {
        val now = System.currentTimeMillis()
        val today = DateUtils.startOfDay(now)
        // Midnight has always finished, unless the test runs in the first hour of the day.
        val saved = if (DateUtils.hourOfDay(now) == 0) emptyList() else listOf(HourlyUsage(dayStartMillis = today, hour = 0, kwh = 2.0))
        val withLive = EnergyCalculator.withLiveHour(saved, todayKwh = 5.0, nowMillis = now)
        assertEquals(5.0, withLive.filter { it.dayStartMillis == today }.sumOf { it.kwh }, TOLERANCE)
    }

    @Test
    fun liveHourAddsNothingWhenRoomIsUpToDate() {
        val now = System.currentTimeMillis()
        val saved = listOf(HourlyUsage(dayStartMillis = DateUtils.startOfDay(now), hour = 0, kwh = 2.0))
        assertEquals(saved, EnergyCalculator.withLiveHour(saved, todayKwh = 2.0, nowMillis = now))
    }

    // ---- TrendCalculator

    @Test
    fun averageSkipsDaysWithNoData() {
        assertEquals(15.0, TrendCalculator.average(listOf(10.0, null, 20.0)), TOLERANCE)
        assertEquals(0.0, TrendCalculator.average(listOf(null, null)), TOLERANCE)
    }

    // ---- ContextRules

    @Test
    fun cheapPeriodIsSuggestedWhileUnderTarget() {
        val rules = ContextRules.evaluate(context(todayKwh = 5.0)).map { it.ruleName }
        assertTrue(rules.contains(CHEAP_WINDOW_RULE))
    }

    @Test
    fun cheapPeriodIsNotSuggestedOnceOverTarget() {
        // Over target the app advises waiting, so it must not also suggest starting a load now.
        val rules = ContextRules.evaluate(context(todayKwh = 20.0)).map { it.ruleName }
        assertTrue(rules.contains(DAILY_TARGET_RULE))
        assertFalse(rules.contains(CHEAP_WINDOW_RULE))
    }

    /** A quiet household at 10 PM on a time-of-use tariff, which is the off-peak period. */
    private fun context(todayKwh: Double) = EnergyContext(
        reading = MeterReading(
            time = "22:00:00",
            unixSeconds = 0,
            aggregateWatts = 400,
            tumbleDryerWatts = 0,
            washingMachineWatts = 0,
            dishwasherWatts = 0,
            heaterWatts = 0,
        ),
        hour = 22,
        todayKwh = todayKwh,
        typicalKw = 0.5,
        typicalKwhByNow = 12.0,
        dailyTargetKwh = 15,
        tariff = TIME_OF_USE,
        isRenting = true,
        outsideTemperature = 15.0,
    )

    private companion object {
        const val TIME_OF_USE = "Time of use"
        const val FLAT_RATE = "Flat rate"
        const val CHEAP_WINDOW_RULE = "Cheap-period window"
        const val DAILY_TARGET_RULE = "Daily target"
        const val TOLERANCE = 0.001
    }
}
