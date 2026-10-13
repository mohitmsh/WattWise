package edu.monash.wattwise.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.delay
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.constants.SmartMeter
import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.MeterAppliance
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.ChartLegendItem
import edu.monash.wattwise.ui.components.ComparisonLineChart
import edu.monash.wattwise.ui.components.MonitorTile
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters
import kotlin.math.abs

private const val ONE_SECOND_MILLIS = 1000L

/**
 * Shows the household's current power from the smart-meter simulator, how it
 * compares with the usual level for this hour, and which appliances are on.
 *
 * @param energyContext null until the simulator has emitted its first reading.
 * @param recentKilowatts the latest readings in kW, oldest first, for the chart.
 */
@Composable
fun LiveUsageScreen(energyContext: EnergyContext?, recentKilowatts: List<Float>, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPaddingCompact),
    ) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader("Smart meter", "Live power", "Updates with every reading from the meter.", onBack)
        Spacer(Modifier.height(16.dp))

        if (energyContext == null) {
            Text("Waiting for the first meter reading…", color = TextSecondary)
            return@Column
        }

        val reading = energyContext.reading

        // Counts the seconds since the newest reading, and starts again when the next one arrives.
        var secondsSinceReading by remember(reading) { mutableIntStateOf(0) }
        LaunchedEffect(reading) {
            while (true) {
                delay(ONE_SECOND_MILLIS)
                secondsSinceReading++
            }
        }
        CompactMetricRing(
            value = energyContext.liveKw.toFloat() / SmartMeter.MAX_LIVE_KW,
            valueText = Formatters.formatKilowatts(energyContext.liveKw),
            unit = "kW",
            label = "Live power",
            color = Cyan,
            modifier = Modifier.width(Dimens.HeroRingSize).align(Alignment.CenterHorizontally),
            hero = true,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            loadLevelText(energyContext),
            style = MaterialTheme.typography.labelLarge,
            color = loadLevelColor(energyContext),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            comparisonText(energyContext),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(20.dp))
        SurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "LAST ${recentKilowatts.size} READINGS (kW)",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "● UPDATED $secondsSinceReading S AGO",
                    style = MaterialTheme.typography.labelMedium,
                    color = StatusGood,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ChartLegendItem("Live power", Cyan)
                ChartLegendItem("Usual at ${Formatters.formatClockHour(energyContext.hour)}", TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            // The usual level is one number for this hour, so it is drawn as a flat line.
            ComparisonLineChart(
                actual = recentKilowatts,
                typical = recentKilowatts.map { energyContext.typicalKw.toFloat() },
                labels = recentKilowatts.indices.map { index -> if (index == recentKilowatts.lastIndex) "NOW" else "" },
                actualColor = Cyan,
                // New readings arrive every few seconds, so the line moves along without restarting.
                animateChanges = false,
                startAtZero = false,
            )
            Text(
                "Latest reading at ${reading.time}. The meter reports about every seven seconds.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(18.dp))
        Text("APPLIANCES ON THE METER", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        // Two tiles per row, one tile for each appliance channel in the meter data.
        MeterAppliance.entries.chunked(2).forEach { rowAppliances ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowAppliances.forEach { appliance ->
                    val watts = reading.wattsFor(appliance)
                    val isOn = watts >= ContextThresholds.APPLIANCE_ON_WATTS
                    MonitorTile(
                        icon = appliance.icon,
                        label = appliance.label,
                        value = "$watts",
                        unit = "W",
                        chipText = if (isOn) "Running now" else "Off or standby",
                        isGood = !isOn,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** LOW, MODERATE or HIGH, comparing live power with the usual level for this hour. */
private fun loadLevelText(energyContext: EnergyContext): String = when {
    energyContext.liveKw > energyContext.typicalKw * ContextThresholds.HIGH_LOAD_FACTOR -> "HIGH"
    energyContext.liveKw < energyContext.typicalKw * ContextThresholds.LOW_LOAD_FACTOR -> "LOW"
    else -> "MODERATE"
}

/** Colour for the load level: green for low, amber for moderate and red for high. */
private fun loadLevelColor(energyContext: EnergyContext): Color = when (loadLevelText(energyContext)) {
    "HIGH" -> Coral
    "LOW" -> StatusGood
    else -> Amber
}

/** One sentence that compares live power with the usual level for this hour. */
private fun comparisonText(energyContext: EnergyContext): String {
    val percent = energyContext.livePercentVsTypical
    val direction = if (percent >= 0) "above" else "below"
    return "Current load is ${abs(percent)}% $direction your usual " +
        "${Formatters.formatClockHour(energyContext.hour)} level"
}
