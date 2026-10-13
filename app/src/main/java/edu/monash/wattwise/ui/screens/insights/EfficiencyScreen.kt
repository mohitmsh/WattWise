package edu.monash.wattwise.ui.screens.insights

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.R
import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.TariffPeriod
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.MonitorTile
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.TariffCalculator

/**
 * Explains today's efficiency score by listing the figures that produced it.
 *
 * @param energyContext null until the simulator has emitted its first reading.
 */
@Composable
fun EfficiencyScreen(energyContext: EnergyContext?, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding),
    ) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader("Today", "Energy efficiency", "How today’s household pattern produced your score.", onBack)
        Spacer(Modifier.height(18.dp))

        if (energyContext == null) {
            Text("Waiting for the first meter reading…", color = TextSecondary)
            return@Column
        }

        CompactMetricRing(
            value = energyContext.efficiencyScore / ContextThresholds.MAX_SCORE.toFloat(),
            valueText = "${energyContext.efficiencyScore}",
            unit = "%",
            label = "Efficiency",
            color = StatusGood,
            modifier = Modifier.width(Dimens.HeroRingSize).align(Alignment.CenterHorizontally),
            hero = true,
        )
        Spacer(Modifier.height(18.dp))
        Row {
            Text("TODAY’S FIGURES", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text("VS. YOUR TYPICAL", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MonitorTile(
                icon = R.drawable.usage_icon,
                label = "Used today",
                value = Formatters.formatKwh(energyContext.todayKwh),
                unit = "kWh",
                chipText = "${signedPercent(energyContext.todayPercentVsTypical)} vs ${Formatters.formatKwh(energyContext.typicalKwhByNow)}",
                isGood = energyContext.todayPercentVsTypical <= 0,
                modifier = Modifier.weight(1f),
            )
            MonitorTile(
                icon = R.drawable.live_icon,
                label = "Live load",
                value = Formatters.formatKilowatts(energyContext.liveKw),
                unit = "kW",
                chipText = "${signedPercent(energyContext.livePercentVsTypical)} vs ${Formatters.formatKilowatts(energyContext.typicalKw)}",
                isGood = energyContext.livePercentVsTypical <= 0,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MonitorTile(
                icon = R.drawable.insights_icon,
                label = "Daily target",
                value = "${Formatters.toPercent(energyContext.targetProgress)}",
                unit = "%",
                chipText = if (energyContext.targetProgress <= 1f) "Within ${energyContext.dailyTargetKwh} kWh" else "Over ${energyContext.dailyTargetKwh} kWh",
                isGood = energyContext.targetProgress <= 1f,
                modifier = Modifier.weight(1f),
            )
            MonitorTile(
                icon = R.drawable.calendar_icon,
                label = "Tariff period",
                value = energyContext.tariffPeriod.label,
                unit = "",
                chipText = "${Formatters.formatCurrency(TariffCalculator.ratePerKwh(energyContext.tariffPeriod))} per kWh",
                isGood = energyContext.tariffPeriod != TariffPeriod.PEAK,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(16.dp))
        SurfaceCard {
            Text("WHY THIS SCORE", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(7.dp))
            Text(
                "Using your usual amount of energy by this time of day scores " +
                    "${ContextThresholds.SCORE_AT_TYPICAL}. Using less raises the score and using more lowers it.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text("WHERE THE DATA COMES FROM", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                "Readings are simulated from the public REFIT smart-meter dataset and compared with " +
                    "this household’s own history saved on the device. This is guidance, not a bill forecast.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(26.dp))
    }
}

/** Adds a plus sign to positive percentages so the direction is clear without colour. */
private fun signedPercent(percent: Int): String = if (percent > 0) "+$percent%" else "$percent%"
