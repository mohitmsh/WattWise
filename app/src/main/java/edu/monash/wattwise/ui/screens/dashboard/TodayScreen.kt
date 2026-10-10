package edu.monash.wattwise.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.constants.SmartMeter
import edu.monash.wattwise.data.model.ApplianceRun
import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.Recommendation
import edu.monash.wattwise.R
import edu.monash.wattwise.ui.components.ActivityRow
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.DashboardTopBar
import edu.monash.wattwise.ui.components.MonitorCard
import edu.monash.wattwise.ui.components.SectionTitle
import edu.monash.wattwise.ui.components.SmartActionCard
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.SoftBlue
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.TariffCalculator
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Home screen: a summary of live power, today's usage and efficiency, with
 * shortcuts to the screens that explain each figure.
 *
 * @param energyContext null until the simulator has emitted its first reading.
 * @param applianceRuns the appliance runs the meter has recorded today, newest first.
 * @param topRecommendation the most important advice from the context rules.
 */
@Composable
fun TodayScreen(
    outsideTemperature: Double?,
    energyContext: EnergyContext?,
    applianceRuns: List<ApplianceRun>,
    topRecommendation: Recommendation?,
    onWeatherMap: () -> Unit,
    onLive: () -> Unit,
    onUsage: () -> Unit,
    onCoach: () -> Unit,
    onEfficiency: () -> Unit,
    onAppliances: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPaddingCompact),
    ) {
        Spacer(Modifier.height(8.dp))
        DashboardTopBar(
            dateText = Formatters.formatDisplayDate(System.currentTimeMillis()),
            isMeterConnected = energyContext != null,
            onMeterClick = onLive,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "WATTWISE",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(9.dp))
        ContextChip(
            outsideTemperature = outsideTemperature,
            tariffText = energyContext?.let { TariffCalculator.periodText(it.tariff, it.hour) } ?: "METER STARTING",
            // Tapping the weather chip opens the weather map.
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onWeatherMap),
        )
        Spacer(Modifier.height(12.dp))

        if (energyContext == null) {
            Text("Waiting for the first meter reading…", color = TextSecondary)
            return@Column
        }

        Row(Modifier.padding(horizontal = 13.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            CompactMetricRing(
                value = energyContext.liveKw.toFloat() / SmartMeter.MAX_LIVE_KW,
                valueText = Formatters.formatKwh(energyContext.liveKw),
                unit = "kW",
                label = "Live",
                color = Cyan,
                modifier = Modifier.weight(1f),
                onClick = onLive,
            )
            CompactMetricRing(
                value = energyContext.targetProgress,
                valueText = Formatters.formatKwh(energyContext.todayKwh),
                unit = "kWh",
                label = "Today",
                color = SoftBlue,
                modifier = Modifier.weight(1f),
                onClick = onUsage,
            )
            CompactMetricRing(
                value = energyContext.efficiencyScore / ContextThresholds.MAX_SCORE.toFloat(),
                valueText = "${energyContext.efficiencyScore}",
                unit = "%",
                label = "Efficiency",
                color = StatusGood,
                modifier = Modifier.weight(1f),
                onClick = onEfficiency,
            )
        }
        Spacer(Modifier.height(18.dp))

        val percent = energyContext.todayPercentVsTypical
        val isBelowTypical = percent <= 0
        val comparison = if (isBelowTypical) "${abs(percent)}% below" else "$percent% above"
        // Tapping the summary opens Insights, where the advice behind it is explained.
        SurfaceCard(
            modifier = Modifier.clickable(onClick = onCoach),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isBelowTypical) "USAGE LOOKS GOOD" else "USAGE IS ABOVE USUAL",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.weight(1f))
                Text(if (isBelowTypical) "✓" else "!", color = if (isBelowTypical) StatusGood else Amber)
            }
            Spacer(Modifier.height(5.dp))
            Text(
                "You’re $comparison your usual usage for this time of day. " +
                    "Your tariff period is ${energyContext.tariffPeriod.label.lowercase()}" +
                    (outsideTemperature?.let { " and it is ${it.roundToInt()}°C outside." } ?: "."),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val isWithinTarget = energyContext.targetProgress <= 1f
            MonitorCard(
                title = "Energy status",
                status = if (isWithinTarget) "Within target" else "Over target",
                detail = "${(energyContext.targetProgress * 100).toInt()}% of ${energyContext.dailyTargetKwh} kWh",
                statusColor = if (isWithinTarget) StatusGood else Amber,
                modifier = Modifier.weight(1f).clickable(onClick = onUsage),
            )
            MonitorCard(
                title = "Meter status",
                status = "Connected",
                detail = "Reading at ${energyContext.reading.time}",
                statusColor = StatusGood,
                modifier = Modifier.weight(1f).clickable(onClick = onLive),
            )
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("My Energy", "Details", onUsage)
        Spacer(Modifier.height(6.dp))
        SurfaceCard(contentPadding = PaddingValues(10.dp)) {
            Text("ACTIVITIES", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(4.dp))
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // All energy today first, then each appliance run the meter has seen, newest first.
                ActivityRow(
                    icon = R.drawable.live_icon,
                    value = Formatters.formatKwh(energyContext.todayKwh),
                    label = "All usage (kWh)",
                    startTime = "00:00",
                    endTime = energyContext.reading.time.take(5),
                    color = SoftBlue,
                    onClick = onUsage,
                )
                applianceRuns.forEach { run ->
                    ActivityRow(
                        icon = run.appliance.icon,
                        value = Formatters.formatKwh(run.kwh),
                        label = run.appliance.label,
                        startTime = run.startTime,
                        endTime = if (run.isRunning) "NOW" else run.endTime,
                        color = if (run.isRunning) Amber else Cyan,
                        onClick = onLive,
                    )
                }
            }
            if (applianceRuns.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "No washer, dryer, dishwasher or heater use recorded yet today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(4.dp),
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        SectionTitle("Today’s insight", "All insights", onCoach)
        Spacer(Modifier.height(6.dp))
        if (topRecommendation != null) {
            SmartActionCard(
                title = topRecommendation.title.uppercase(),
                reason = topRecommendation.advice,
                saving = topRecommendation.saving,
                action = "WHY THIS ADVICE",
                onAction = onCoach,
            )
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onAppliances, modifier = Modifier.fillMaxWidth()) {
            Text("VIEW APPLIANCES  ›", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Shows the context the recommendations are based on: outside temperature and tariff period. */
@Composable
private fun ContextChip(outsideTemperature: Double?, tariffText: String, modifier: Modifier = Modifier) {
    // The temperature is null while the weather request is loading or if it failed.
    val temperatureText = outsideTemperature?.let { "${it.roundToInt()}°C OUTSIDE" } ?: "WEATHER UNAVAILABLE"
    Row(
        modifier
            .background(Surface, RoundedCornerShape(Dimens.PILL_CORNER_PERCENT))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(temperatureText, style = MaterialTheme.typography.labelMedium, color = SoftBlue)
        Spacer(Modifier.width(9.dp))
        Box(Modifier.size(3.dp).background(TextTertiary, CircleShape))
        Spacer(Modifier.width(9.dp))
        Text(tariffText, style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.width(9.dp))
        Text("MAP ›", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
    }
}
