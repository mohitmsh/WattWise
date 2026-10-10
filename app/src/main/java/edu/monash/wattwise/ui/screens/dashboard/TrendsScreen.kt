package edu.monash.wattwise.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.R
import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.model.TariffPeriod
import edu.monash.wattwise.data.model.TrendMetric
import edu.monash.wattwise.data.model.UsagePeriod
import edu.monash.wattwise.ui.components.BreakdownBar
import edu.monash.wattwise.ui.components.BreakdownSegment
import edu.monash.wattwise.ui.components.ChartLegendItem
import edu.monash.wattwise.ui.components.ComparisonLineChart
import edu.monash.wattwise.ui.components.DatePickerPopup
import edu.monash.wattwise.ui.components.DropdownField
import edu.monash.wattwise.ui.components.ShareBar
import edu.monash.wattwise.ui.components.StatTile
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.components.TrendBarChart
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.DinPro
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.SoftBlue
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary
import edu.monash.wattwise.utils.DateUtils
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.TariffCalculator
import edu.monash.wattwise.utils.TrendCalculator
import kotlin.math.abs

/**
 * Usage report built from the hourly usage saved in Room. The user chooses a
 * metric, a period of a day, week or month, and the date the period ends on.
 * Every figure and chart below is recalculated from Room for that choice.
 *
 * @param usage every hour saved in Room; the report updates when a new hour is added.
 * @param tariff the household's tariff, used for costs and the peak-period split.
 * @param dailyTargetKwh the daily target from the household form.
 */
@Composable
fun TrendsScreen(usage: List<HourlyUsage>, tariff: String, dailyTargetKwh: Int) {
    var metric by remember { mutableStateOf(TrendMetric.ENERGY) }
    var period by remember { mutableStateOf(UsagePeriod.WEEK) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Group the saved hours by day once, so each day's hours can be looked up quickly.
    val usageByDay = remember(usage) { usage.groupBy { it.dayStartMillis } }
    val today = DateUtils.startOfDay(System.currentTimeMillis())
    val lastDay = DateUtils.startOfDay(selectedDateMillis)
    val days = TrendCalculator.daysEndingOn(lastDay, period.dayCount)
    val periodUsage = days.flatMap { usageByDay[it] ?: emptyList() }
    val isDayView = period == UsagePeriod.DAY

    // One value of the chosen metric for each day in this period and in the period before it.
    val dailyValues = days.map { TrendCalculator.dailyValue(usageByDay[it], metric, tariff) }
    val previousDays = TrendCalculator.daysEndingOn(DateUtils.addDays(lastDay, -period.dayCount), period.dayCount)
    val previousValues = previousDays.map { TrendCalculator.dailyValue(usageByDay[it], metric, tariff) }
    val average = TrendCalculator.average(dailyValues)
    val previousAverage = TrendCalculator.average(previousValues)
    val changePercent = EnergyCalculator.percentDifference(average, previousAverage)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPaddingCompact),
    ) {
        Spacer(Modifier.height(18.dp))
        Text(
            "TREND VIEW",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(14.dp))
        DropdownField(
            label = "Metric",
            options = TrendMetric.entries.map { it.label },
            selected = metric.label,
            onSelected = { label -> metric = TrendMetric.entries.first { it.label == label } },
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (isDayView) "TOTAL" else "DAILY AVERAGE",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatMetric(average, metric), fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 40.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        metric.unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 7.dp),
                    )
                }
                ChangePill(changePercent, period)
            }
            PeriodSelector(selected = period, onSelected = { period = it }, modifier = Modifier.width(170.dp))
        }
        Spacer(Modifier.height(6.dp))

        // Arrows move the report one period back or forward; the calendar jumps to any date.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            ArrowButton("‹", "Previous period", enabled = true) {
                selectedDateMillis = DateUtils.addDays(lastDay, -period.dayCount)
            }
            Text(dateRangeText(days), style = MaterialTheme.typography.labelLarge)
            ArrowButton("›", "Next period", enabled = lastDay < today) {
                selectedDateMillis = minOf(DateUtils.addDays(lastDay, period.dayCount), today)
            }
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    painterResource(R.drawable.calendar_icon),
                    contentDescription = "Choose report date",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        if (periodUsage.isEmpty()) {
            SurfaceCard {
                Text("No usage is saved for this period. Choose a more recent date.", color = TextSecondary)
            }
            Spacer(Modifier.height(26.dp))
        } else {
            Text(
                insightText(metric, period, average, previousAverage),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(16.dp))

            // Chart 1 (bar chart): the chosen metric for each hour of the day or each day of the period.
            SurfaceCard {
                if (isDayView) {
                    val hourlyValues = TrendCalculator.hourlyValues(usageByDay[lastDay], metric, tariff)
                    val unit = if (metric == TrendMetric.COST) "$" else "kWh"
                    Text("$unit FOR EACH HOUR", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    TrendBarChart(
                        values = hourlyValues,
                        labels = (0 until 24).map { hour -> "$hour:00" },
                        // Each bar takes the colour of its tariff period, so expensive hours stand out.
                        barColors = (0 until 24).map { hour -> periodColor(TariffCalculator.periodFor(tariff, hour)) },
                        average = hourlyValues.average().toFloat(),
                    )
                } else {
                    Text(
                        "${metric.label.uppercase()} (${metric.unit}) FOR EACH DAY",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(8.dp))
                    TrendBarChart(
                        values = dailyValues.map { (it ?: 0.0).toFloat() },
                        labels = days.map { dayLabel(it, period) },
                        barColors = dailyValues.map { dayColor(it, metric, dailyTargetKwh, average) },
                        average = average.toFloat(),
                    )
                }
                Spacer(Modifier.height(16.dp))
                if (isDayView) {
                    TariffBreakdown(periodUsage, tariff)
                } else {
                    DayBreakdown(dailyValues, metric, dailyTargetKwh, average)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Chart 2 (line chart): energy compared with what this household usually uses.
            SurfaceCard {
                Text("ENERGY VS. TYPICAL (kWh)", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ChartLegendItem("Energy used", Electric)
                    ChartLegendItem("Your typical", TextSecondary)
                }
                Spacer(Modifier.height(8.dp))
                if (isDayView) {
                    ComparisonLineChart(
                        actual = EnergyCalculator.hourlyForDay(usage, lastDay),
                        typical = (0 until 24).map { hour -> EnergyCalculator.typicalForHour(usage, hour).toFloat() },
                        labels = (0 until 24).map { hour -> "$hour:00" },
                        actualColor = Electric,
                    )
                } else {
                    ComparisonLineChart(
                        actual = days.map { (usageByDay[it]?.sumOf { hour -> hour.kwh } ?: 0.0).toFloat() },
                        typical = days.map { TrendCalculator.typicalForWeekday(usageByDay, it).toFloat() },
                        labels = days.map { dayLabel(it, period) },
                        actualColor = Electric,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isDayView) {
                        "Typical is your average for each hour across all saved days."
                    } else {
                        "Typical is your average for the same weekday across all saved days."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(16.dp))

            // Share of energy in each part of the day, against the household's usual share.
            Text("WHEN YOU USE ENERGY", style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(2.dp).height(10.dp).background(TextPrimary))
                Spacer(Modifier.width(6.dp))
                Text("YOUR TYPICAL SHARE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            DayPartBars(periodUsage, usage)
            Spacer(Modifier.height(20.dp))

            // Summary of the period; tapping a tile switches the report to that metric.
            Text(
                if (isDayView) "THIS DAY" else "${period.dayCount}-DAY AVERAGE",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TrendMetric.entries.forEach { option ->
                    val optionAverage = TrendCalculator.average(
                        days.map { TrendCalculator.dailyValue(usageByDay[it], option, tariff) },
                    )
                    StatTile(
                        label = tileLabel(option),
                        value = formatMetric(optionAverage, option),
                        isSelected = option == metric,
                        modifier = Modifier.weight(1f).clickable { metric = option },
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }

    if (showDatePicker) {
        DatePickerPopup(
            initialDateMillis = selectedDateMillis,
            onDateSelected = { selectedDateMillis = minOf(it, System.currentTimeMillis()) },
            onDismiss = { showDatePicker = false },
        )
    }
}

/** Small label showing the change from the previous period, with an arrow for the direction. */
@Composable
private fun ChangePill(changePercent: Int, period: UsagePeriod) {
    val arrow = when {
        changePercent > 0 -> "▲"
        changePercent < 0 -> "▼"
        else -> "●"
    }
    // For every metric here lower is better, so a fall is shown in green.
    val color = if (changePercent > 0) Amber else StatusGood
    Row(
        Modifier
            .padding(top = 4.dp)
            .background(SurfaceRaised, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(arrow, fontSize = 9.sp, color = color)
        Spacer(Modifier.width(5.dp))
        Text(
            "${abs(changePercent)}% vs previous ${period.label.lowercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
        )
    }
}

@Composable
private fun ArrowButton(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) {
        Text(symbol, fontSize = 26.sp, color = if (enabled) TextPrimary else TextTertiary)
    }
}

/** Counts the days in the period by status and shows them as one split bar with a legend. */
@Composable
private fun DayBreakdown(dailyValues: List<Double?>, metric: TrendMetric, dailyTargetKwh: Int, average: Double) {
    val values = dailyValues.filterNotNull()
    Text("${metric.label.uppercase()} BREAKDOWN (DAYS)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    Spacer(Modifier.height(8.dp))
    if (metric == TrendMetric.ENERGY) {
        val closeLimit = dailyTargetKwh * ContextThresholds.CLOSE_TO_TARGET_FACTOR
        val within = values.count { it <= dailyTargetKwh }
        val close = values.count { it > dailyTargetKwh && it <= closeLimit }
        val over = values.count { it > closeLimit }
        BreakdownBar(
            listOf(
                BreakdownSegment("${within}x", "Within target (up to $dailyTargetKwh kWh)", within.toFloat(), StatusGood),
                BreakdownSegment("${close}x", "Close (up to ${Formatters.formatKwh(closeLimit)} kWh)", close.toFloat(), Amber),
                BreakdownSegment("${over}x", "Over target", over.toFloat(), Coral),
            ),
        )
    } else {
        val below = values.count { it <= average }
        val above = values.count { it > average }
        BreakdownBar(
            listOf(
                BreakdownSegment("${below}x", "At or below the average", below.toFloat(), Cyan),
                BreakdownSegment("${above}x", "Above the average", above.toFloat(), Amber),
            ),
        )
    }
}

/** Splits one day's energy by tariff period, so the user sees how much was used at each price. */
@Composable
private fun TariffBreakdown(dayUsage: List<HourlyUsage>, tariff: String) {
    Text("ENERGY BREAKDOWN (TARIFF PERIOD)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    Spacer(Modifier.height(8.dp))
    BreakdownBar(
        TrendCalculator.energyByTariffPeriod(dayUsage, tariff).map { (tariffPeriod, kwh) ->
            BreakdownSegment(
                amountText = "${Formatters.formatKwh(kwh)} kWh",
                label = "${tariffPeriod.label} at ${Formatters.formatCurrency(TariffCalculator.ratePerKwh(tariffPeriod))} per kWh",
                weight = kwh.toFloat(),
                color = periodColor(tariffPeriod),
            )
        },
    )
}

/** One [ShareBar] for each part of the day, comparing this period with the whole history. */
@Composable
private fun DayPartBars(periodUsage: List<HourlyUsage>, allUsage: List<HourlyUsage>) {
    val periodParts = TrendCalculator.dayPartTotals(periodUsage)
    val typicalParts = TrendCalculator.dayPartTotals(allUsage)
    val periodTotal = periodParts.sumOf { it.second }.coerceAtLeast(0.001)
    val typicalTotal = typicalParts.sumOf { it.second }.coerceAtLeast(0.001)
    val colors = listOf(SoftBlue, Electric, Amber, Coral)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        periodParts.forEachIndexed { index, (name, kwh) ->
            ShareBar(
                label = name,
                valueText = "${Formatters.formatKwh(kwh)} kWh",
                share = (kwh / periodTotal).toFloat(),
                typicalShare = (typicalParts[index].second / typicalTotal).toFloat(),
                color = colors[index],
            )
        }
    }
}

/** Row of buttons for switching the report between day, week and month. */
@Composable
private fun PeriodSelector(selected: UsagePeriod, onSelected: (UsagePeriod) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Row(modifier.background(Surface, shape).padding(3.dp)) {
        UsagePeriod.entries.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .clickable { onSelected(option) }
                    .background(if (isSelected) SurfaceRaised else Color.Transparent, shape)
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    option.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) TextPrimary else TextTertiary,
                )
            }
        }
    }
}

/** Sentence under the header that puts the average into words. */
private fun insightText(metric: TrendMetric, period: UsagePeriod, average: Double, previousAverage: Double): String {
    val name = metric.label.lowercase()
    val current = "${formatMetric(average, metric)} ${metric.unit}"
    if (previousAverage <= 0.0) {
        return "Your $name for this ${period.label.lowercase()} was $current. There is no earlier period to compare with yet."
    }
    val direction = when {
        average > previousAverage -> "above"
        average < previousAverage -> "below"
        else -> "the same as"
    }
    val subject = if (period == UsagePeriod.DAY) "Your $name on this day" else "Your average daily $name this ${period.label.lowercase()}"
    return "$subject ($current) was $direction the previous ${period.label.lowercase()} " +
        "(${formatMetric(previousAverage, metric)} ${metric.unit})."
}

private fun formatMetric(value: Double, metric: TrendMetric): String = when (metric) {
    TrendMetric.ENERGY, TrendMetric.PEAK_ENERGY -> Formatters.formatKwh(value)
    TrendMetric.COST, TrendMetric.BASE_LOAD -> Formatters.formatKilowatts(value)
}

private fun tileLabel(metric: TrendMetric): String = when (metric) {
    TrendMetric.ENERGY -> "Energy"
    TrendMetric.COST -> "Cost $"
    TrendMetric.PEAK_ENERGY -> "Peak kWh"
    TrendMetric.BASE_LOAD -> "Base kW"
}

/** Green, amber or red for energy against the daily target; one colour for the other metrics. */
private fun dayColor(value: Double?, metric: TrendMetric, dailyTargetKwh: Int, average: Double): Color {
    if (value == null) return TextTertiary
    if (metric != TrendMetric.ENERGY) return if (value > average) Amber else Cyan
    return when {
        value <= dailyTargetKwh -> StatusGood
        value <= dailyTargetKwh * ContextThresholds.CLOSE_TO_TARGET_FACTOR -> Amber
        else -> Coral
    }
}

private fun periodColor(period: TariffPeriod): Color = when (period) {
    TariffPeriod.PEAK -> Amber
    TariffPeriod.OFF_PEAK -> Cyan
    TariffPeriod.SOLAR -> StatusGood
    TariffPeriod.FLAT -> Cyan
}

/** X-axis label for a day: the weekday for a week, the day of the month for a month. */
private fun dayLabel(day: Long, period: UsagePeriod): String =
    if (period == UsagePeriod.WEEK) Formatters.formatWeekday(day) else Formatters.formatDayOfMonth(day)

/** The dates the report covers, for example "05 OCT - 11 OCT". */
private fun dateRangeText(days: List<Long>): String {
    val first = Formatters.formatShortDate(days.first())
    val last = Formatters.formatShortDate(days.last())
    return if (first == last) first else "$first - $last"
}
