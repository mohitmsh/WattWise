package edu.monash.wattwise.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import edu.monash.wattwise.R
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.ui.theme.Border
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.DinPro
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.SoftBlue
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary
import java.util.Locale

private val ChartHeight = 230.dp
private const val CHART_ANIMATION_MILLIS = 600

/** Up to this many points, the value is printed next to each bar or point. */
private const val MAX_POINTS_WITH_VALUES = 7

/** Most labels the x-axis shows, so they never overlap. */
private const val MAX_AXIS_LABELS = 7

/**
 * Bar chart with one bar per entry in [values] and a dashed line at [average].
 * The chart is an Android View from the MPAndroidChart library, so it is
 * placed in Compose with AndroidView.
 *
 * @param barColors one colour per bar, so a bar can show its status.
 * @param animationKey the chart animates only when this changes, for example a new period.
 */
@Composable
fun TrendBarChart(
    values: List<Float>,
    labels: List<String>,
    barColors: List<Color>,
    average: Float,
    modifier: Modifier = Modifier,
    animationKey: Any = labels,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(ChartHeight),
        // factory runs once and creates the chart with the settings that never change.
        factory = { context -> BarChart(context).apply { applyChartStyle(this, context) } },
        // update runs again whenever the values change, so the chart follows Room.
        update = { chart ->
            val barEntries = values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
            val barDataSet = BarDataSet(barEntries, "")
            barDataSet.colors = barColors.map { it.toArgb() }
            barDataSet.valueTextColor = TextPrimary.toArgb()
            barDataSet.valueTextSize = 10f
            barDataSet.valueFormatter = OneDecimalFormatter()
            barDataSet.setDrawValues(values.size <= MAX_POINTS_WITH_VALUES)
            val barData = BarData(barDataSet)
            barData.barWidth = 0.62f
            chart.data = barData
            chart.setFitBars(true)

            // Dashed line showing the average, so each bar can be judged against it.
            chart.axisLeft.removeAllLimitLines()
            if (average > 0f) {
                val averageLine = LimitLine(average, "AVG")
                averageLine.enableDashedLine(10f, 8f, 0f)
                averageLine.lineColor = TextSecondary.toArgb()
                averageLine.textColor = TextPrimary.toArgb()
                averageLine.textSize = 9f
                chart.axisLeft.addLimitLine(averageLine)
            }
            showLabels(chart, labels)
            animateWhenChanged(chart, animationKey)
        },
    )
}

/**
 * Line chart comparing [actual] with [typical], one point per label. With a
 * few points each one is drawn as a circle with its value; with many points
 * the actual line is filled underneath instead.
 *
 * @param animateChanges false for a chart that receives new readings every few
 * seconds, so it updates in place and does not replay its animation each time.
 * @param startAtZero false makes the y-axis fit the values, so small changes are easy to see.
 * @param animationKey the chart animates only when this changes, for example a new period.
 */
@Composable
fun ComparisonLineChart(
    actual: List<Float>,
    typical: List<Float>,
    labels: List<String>,
    actualColor: Color,
    modifier: Modifier = Modifier,
    animateChanges: Boolean = true,
    startAtZero: Boolean = true,
    animationKey: Any = labels,
) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(ChartHeight),
        factory = { context -> LineChart(context).apply { applyChartStyle(this, context) } },
        update = { chart ->
            val showPoints = actual.size <= MAX_POINTS_WITH_VALUES

            val typicalDataSet = LineDataSet(typical.mapIndexed { index, value -> Entry(index.toFloat(), value) }, "")
            styleLine(typicalDataSet, TextSecondary, showPoints)

            val actualDataSet = LineDataSet(actual.mapIndexed { index, value -> Entry(index.toFloat(), value) }, "")
            styleLine(actualDataSet, actualColor, showPoints)
            if (!showPoints) {
                actualDataSet.setDrawFilled(true)
                actualDataSet.fillColor = actualColor.toArgb()
                actualDataSet.fillAlpha = 45
            }

            // A dot on the newest value, so the eye can follow where the line is growing.
            val latestDataSet = LineDataSet(listOfNotNull(actualDataSet.values.lastOrNull()), "")
            styleLine(latestDataSet, actualColor, showPoints = true)
            latestDataSet.setDrawValues(false)

            // With startAtZero off, the axis follows the data and is not fixed at zero.
            if (startAtZero) chart.axisLeft.axisMinimum = 0f else chart.axisLeft.resetAxisMinimum()

            // The typical line is added first so the actual line is drawn on top of it.
            chart.data = LineData(typicalDataSet, actualDataSet, latestDataSet)
            showLabels(chart, labels)
            if (animateChanges) animateWhenChanged(chart, animationKey) else chart.invalidate()
        },
    )
}

/** Colours, grid and fonts shared by the bar and line charts so they match the app's theme. */
private fun applyChartStyle(chart: BarLineChartBase<*>, context: Context) {
    val dataFont = ResourcesCompat.getFont(context, R.font.dinpro_bold)
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.setScaleEnabled(false)
    chart.setNoDataText("No usage saved for this period")
    chart.setNoDataTextColor(TextSecondary.toArgb())
    chart.extraBottomOffset = 6f

    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    chart.xAxis.setDrawGridLines(false)
    chart.xAxis.setDrawAxisLine(false)
    chart.xAxis.granularity = 1f
    chart.xAxis.textColor = TextSecondary.toArgb()
    chart.xAxis.typeface = dataFont

    chart.axisLeft.textColor = TextTertiary.toArgb()
    chart.axisLeft.typeface = dataFont
    chart.axisLeft.gridColor = Border.toArgb()
    chart.axisLeft.setDrawAxisLine(false)
    chart.axisLeft.axisMinimum = 0f
    chart.axisLeft.setLabelCount(5, false)
    chart.axisRight.isEnabled = false
}

/** Sets the colour, width and points of one line on a line chart. */
private fun styleLine(dataSet: LineDataSet, color: Color, showPoints: Boolean) {
    dataSet.color = color.toArgb()
    dataSet.lineWidth = 2f
    dataSet.setDrawCircles(showPoints)
    dataSet.setCircleColor(color.toArgb())
    dataSet.circleRadius = 5f
    dataSet.circleHoleRadius = 3f
    dataSet.circleHoleColor = Surface.toArgb()
    dataSet.setDrawValues(showPoints)
    dataSet.valueTextColor = color.toArgb()
    dataSet.valueTextSize = 10f
    dataSet.valueFormatter = OneDecimalFormatter()
    dataSet.isHighlightEnabled = false
}

/** Puts [labels] under the chart, one for each value. */
private fun showLabels(chart: BarLineChartBase<*>, labels: List<String>) {
    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
    chart.xAxis.labelCount = minOf(labels.size, MAX_AXIS_LABELS)
}

/**
 * Animates only when [key] is different from last time, for example when the
 * user picks another period. When only the values change, such as a new meter
 * reading every few seconds, the chart is redrawn in place and does not flicker.
 */
private fun animateWhenChanged(chart: BarLineChartBase<*>, key: Any) {
    if (chart.tag != key) {
        chart.tag = key
        chart.animateY(CHART_ANIMATION_MILLIS)
    } else {
        chart.invalidate()
    }
}

/** Shows chart values with one decimal place, for example "14.6". */
class OneDecimalFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f", value)
    }
}

/** One coloured part of a [BreakdownBar], with the text shown in its legend row. */
data class BreakdownSegment(val amountText: String, val label: String, val weight: Float, val color: Color)

/** A single bar split into coloured segments by weight, with a legend row for each segment. */
@Composable
fun BreakdownBar(segments: List<BreakdownSegment>) {
    val visibleSegments = segments.filter { it.weight > 0f }
    Column {
        Row(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(3.dp))) {
            visibleSegments.forEach { segment ->
                Box(Modifier.weight(segment.weight).fillMaxHeight().background(segment.color))
                Spacer(Modifier.width(2.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        segments.forEach { segment ->
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(9.dp).background(segment.color))
                Spacer(Modifier.width(9.dp))
                Text(
                    segment.amountText,
                    fontFamily = DinPro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.width(74.dp),
                )
                Text(segment.label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}

/**
 * Horizontal bar for one part of a total, with a white marker showing the
 * household's typical share so the two can be compared at a glance.
 *
 * @param share and [typicalShare] are fractions from 0 to 1.
 */
@Composable
fun ShareBar(label: String, valueText: String, share: Float, typicalShare: Float, color: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceRaised, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(8.dp))
            Text("${Formatters.toPercent(share)}%", style = MaterialTheme.typography.labelMedium, color = color)
            Spacer(Modifier.weight(1f))
            Text(valueText, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(12.dp)) {
            Box(Modifier.fillMaxWidth().fillMaxHeight().background(TextTertiary.copy(alpha = .22f), RoundedCornerShape(3.dp)))
            Box(
                Modifier
                    .fillMaxWidth(share.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp)),
            )
            // Marker for the typical share: an empty box as wide as the share, then a thin line.
            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                Spacer(Modifier.fillMaxWidth(typicalShare.coerceIn(0f, 1f)))
                Box(Modifier.width(2.dp).fillMaxHeight().background(TextPrimary))
            }
        }
    }
}

/**
 * A bar comparing one value with a reference, such as today's energy with
 * the daily target. The white line marks the reference.
 *
 * @param fraction how far the bar is filled, and [markerFraction] where the line sits, both from 0 to 1.
 */
@Composable
fun CompareBar(
    label: String,
    valueText: String,
    fraction: Float,
    markerLabel: String,
    markerFraction: Float,
    color: Color,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(SurfaceRaised, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.weight(1f))
            Text(valueText, fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(12.dp)) {
            Box(Modifier.fillMaxWidth().fillMaxHeight().background(TextTertiary.copy(alpha = .22f), RoundedCornerShape(3.dp)))
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp)),
            )
            // Reference marker: an empty space as wide as the reference, then a thin line.
            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                Spacer(Modifier.fillMaxWidth(markerFraction.coerceIn(0f, 1f)))
                Box(Modifier.width(2.dp).fillMaxHeight().background(TextPrimary))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(2.dp).height(10.dp).background(TextPrimary))
            Spacer(Modifier.width(6.dp))
            Text(markerLabel.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

/** Legend entry: a ring in the line's colour followed by its name. */
@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(11.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(6.dp).background(Surface, CircleShape))
        }
        Spacer(Modifier.width(7.dp))
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}

/** Small tile for the summary row under the charts; the selected tile is highlighted. */
@Composable
fun StatTile(label: String, value: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(SurfaceRaised, RoundedCornerShape(Dimens.CardRadius))
            .padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Electric else TextSecondary,
            maxLines = 1,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontFamily = DinPro,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            color = if (isSelected) TextPrimary else SoftBlue,
        )
    }
}
