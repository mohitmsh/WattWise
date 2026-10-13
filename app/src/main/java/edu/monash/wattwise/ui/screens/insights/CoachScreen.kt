package edu.monash.wattwise.ui.screens.insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.EnergyUnits
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceRun
import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.Recommendation
import edu.monash.wattwise.ui.components.BreakdownBar
import edu.monash.wattwise.ui.components.BreakdownSegment
import edu.monash.wattwise.ui.components.ChartLegendItem
import edu.monash.wattwise.ui.components.CompareBar
import edu.monash.wattwise.ui.components.ComparisonLineChart
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.SmartActionCard
import edu.monash.wattwise.ui.components.StatTile
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.SoftBlue
import edu.monash.wattwise.ui.theme.TextTertiary
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.utils.Formatters
import kotlin.math.roundToInt

/**
 * Lists the advice the context rules produced for the current situation. Each
 * card can be opened to see which rule and which data produced the advice.
 */
@Composable
fun CoachScreen(
    recommendations: List<Recommendation>,
    energyContext: EnergyContext?,
    outsideTemperature: Double?,
    todayHourlyKwh: List<Float>,
    typicalHourlyKwh: List<Float>,
    appliances: List<Appliance>,
    applianceRuns: List<ApplianceRun>,
    onEfficiency: () -> Unit,
    onBill: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding)) {
        Spacer(Modifier.height(18.dp))
        ScreenHeader(
            eyebrow = "Updated with every reading",
            title = "WattWise Coach",
            subtitle = "Advice based on your meter, tariff, weather and household. Tap a card to see why.",
        )
        Spacer(Modifier.height(18.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            if (energyContext != null) {
                item { ContextCard(energyContext, outsideTemperature) }
            }
            if (energyContext != null) {
                item { TodayChartCard(todayHourlyKwh, typicalHourlyKwh) }
                item { EnergySplitCard(energyContext, appliances, applianceRuns) }
            }
            if (recommendations.isEmpty()) {
                item { Text("Waiting for the first meter reading…", color = TextSecondary) }
            }
            items(recommendations) { recommendation ->
                RecommendationCard(recommendation)
            }
            item {
                Spacer(Modifier.height(6.dp))
                SmartActionCard(
                    title = "How was my score calculated?",
                    reason = "See the figures behind today’s efficiency score.",
                    saving = "EXPLAIN",
                    action = "OPEN EFFICIENCY",
                    onAction = onEfficiency,
                )
            }
            item {
                SmartActionCard(
                    title = "Will this bill fit your budget?",
                    reason = "See the estimated bill for this billing period and how much you can use each day.",
                    saving = "FORECAST",
                    action = "OPEN BILL FORECAST",
                    onAction = onBill,
                )
            }
        }
    }
}

/**
 * Shows the inputs the rules are using right now: one from the meter (sensory)
 * and three that are not from a sensor, so the user can see what the advice is based on.
 */
@Composable
private fun ContextCard(energyContext: EnergyContext, outsideTemperature: Double?) {
    SurfaceCard {
        Text("WHAT YOUR ADVICE IS BASED ON", style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("Meter", "${Formatters.formatKilowatts(energyContext.liveKw)} kW", false, Modifier.weight(1f))
            StatTile("Tariff", energyContext.tariffPeriod.label, false, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("Outside", outsideTemperature?.let { "${it.roundToInt()}°C" } ?: "…", false, Modifier.weight(1f))
            StatTile("Home", if (energyContext.isRenting) "Renting" else "Owner", false, Modifier.weight(1f))
        }
    }
}

/** Line chart of today's energy for each hour against the household's usual hour. */
@Composable
private fun TodayChartCard(todayHourlyKwh: List<Float>, typicalHourlyKwh: List<Float>) {
    SurfaceCard {
        Text("TODAY VS. YOUR USUAL (kWh EACH HOUR)", style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChartLegendItem("Today", Electric)
            ChartLegendItem("Your usual", TextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        ComparisonLineChart(
            actual = todayHourlyKwh,
            typical = typicalHourlyKwh,
            labels = (0 until 24).map { hour -> "$hour:00" },
            actualColor = Electric,
        )
    }
}

/**
 * Splits today's metered energy between the appliances the user has saved and
 * everything else. An appliance linked to a meter channel uses its measured
 * energy; the others use their daily estimate scaled to the part of the day
 * that has passed. "Other" is what the meter measured beyond that.
 */
@Composable
private fun EnergySplitCard(
    energyContext: EnergyContext,
    appliances: List<Appliance>,
    applianceRuns: List<ApplianceRun>,
) {
    SurfaceCard {
        Text("WHERE TODAY’S ENERGY GOES", style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.height(10.dp))
        if (appliances.isEmpty()) {
            Text(
                "Add your appliances in More › Appliances to see how much of the meter reading they explain.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        } else {
            val dayFraction = (energyContext.hour + 1) / EnergyUnits.HOURS_PER_DAY.toDouble()
            val colors = listOf(Cyan, Amber, SoftBlue, Coral, Electric)
            val applianceSegments = appliances.mapIndexed { index, appliance ->
                // Measured energy for appliances linked to a meter channel, an estimate for the rest.
                val measuredKwh = EnergyCalculator.measuredKwhToday(appliance, applianceRuns)
                val kwhSoFar = measuredKwh ?: (EnergyCalculator.applianceDailyKwh(appliance) * dayFraction)
                BreakdownSegment(
                    amountText = "${Formatters.formatKwh(kwhSoFar)} kWh",
                    label = "${appliance.name} (${if (measuredKwh != null) "measured" else "estimate"})",
                    weight = kwhSoFar.toFloat(),
                    color = colors[index % colors.size],
                )
            }
            val listedKwh = applianceSegments.sumOf { it.weight.toDouble() }
            val otherKwh = (energyContext.todayKwh - listedKwh).coerceAtLeast(0.0)
            BreakdownBar(
                applianceSegments + BreakdownSegment(
                    amountText = "${Formatters.formatKwh(otherKwh)} kWh",
                    label = "Other and standby",
                    weight = otherKwh.toFloat(),
                    color = TextTertiary,
                ),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "The meter has measured ${Formatters.formatKwh(energyContext.todayKwh)} kWh today. Estimates " +
                    "come from the power and hours you entered; link an appliance to a meter channel to measure it.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

/** One piece of advice. Tapping the card shows or hides the reason behind it. */
@Composable
private fun RecommendationCard(recommendation: Recommendation) {
    var showReason by rememberSaveable { mutableStateOf(false) }
    val color = if (recommendation.isWarning) Amber else Electric

    SurfaceCard(Modifier.clickable { showReason = !showReason }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // The label says whether this is a change or praise, so colour is not the only cue.
            Text(
                if (recommendation.isWarning) "SUGGESTED CHANGE" else "GOOD TO KNOW",
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
            Spacer(Modifier.weight(1f))
            Text(recommendation.saving, style = MaterialTheme.typography.labelMedium, color = color)
        }
        Spacer(Modifier.height(8.dp))
        Text(recommendation.title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(5.dp))
        Text(recommendation.advice, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        // The numbers behind the advice as a bar, so it can be understood at a glance.
        if (recommendation.visual != null) {
            Spacer(Modifier.height(10.dp))
            CompareBar(
                label = recommendation.visual.label,
                valueText = recommendation.visual.valueText,
                fraction = recommendation.visual.fraction,
                markerLabel = recommendation.visual.markerLabel,
                markerFraction = recommendation.visual.markerFraction,
                color = color,
            )
        }
        Spacer(Modifier.height(10.dp))
        if (showReason) {
            Text(
                "WHY · ${recommendation.ruleName.uppercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = Electric,
            )
            Spacer(Modifier.height(4.dp))
            Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }
        Text(
            if (showReason) "HIDE REASON" else "WHY THIS ADVICE?  ›",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
        )
    }
}
