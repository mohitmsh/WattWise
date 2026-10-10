package edu.monash.wattwise.ui.screens.appliance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceStatus
import edu.monash.wattwise.ui.components.MetricTile
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.constants.TariffRules
import edu.monash.wattwise.data.model.ApplianceRun
import edu.monash.wattwise.data.model.MeterReading
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.TariffCalculator

/**
 * Shows what one appliance uses and costs, worked out from its power, its
 * hours of use and the household's tariff, with options to edit or delete it.
 */
@Composable
fun ApplianceDetailScreen(
    appliance: Appliance,
    tariff: String,
    dailyTargetKwh: Int,
    applianceRuns: List<ApplianceRun>,
    latestReading: MeterReading?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Linked to a meter channel: use what the meter measured. Not linked: estimate from power and hours.
    val channel = appliance.meterChannel
    val measuredKwh = EnergyCalculator.measuredKwhToday(appliance, applianceRuns)
    val isMeasured = measuredKwh != null
    val dailyKwh = measuredKwh ?: EnergyCalculator.applianceDailyKwh(appliance)
    val liveWatts = if (channel != null) latestReading?.wattsFor(channel) else null
    val todayRuns = applianceRuns.filter { it.appliance == channel }
    val dailyCost = dailyKwh * TariffCalculator.averageRate(tariff)
    val targetShare = if (dailyTargetKwh > 0) (dailyKwh / dailyTargetKwh * 100).toInt() else 0
    val isHighUse = EnergyCalculator.applianceStatus(appliance, dailyTargetKwh) == ApplianceStatus.HIGH

    // The advice depends on whether the appliance can be moved to a cheaper time.
    val recommendedAction = when {
        appliance.category.isFlexible && tariff == TariffRules.TIME_OF_USE ->
            "This appliance can run at any time. Use it before " +
                "${Formatters.formatClockHour(TariffRules.PEAK_START_HOUR)} or after " +
                "${Formatters.formatClockHour(TariffRules.PEAK_END_HOUR)} to pay the off-peak price."
        appliance.category.isFlexible && tariff == TariffRules.SOLAR_FEED_IN ->
            "This appliance can run at any time. Use it in the middle of the day, when your solar panels produce the most."
        else ->
            "Each hour less a day saves about " +
                "${Formatters.formatMonthlyCost(EnergyCalculator.wattsToKilowatts(appliance.watts) * TariffCalculator.averageRate(tariff))} a month."
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader(
            eyebrow = appliance.category.label,
            title = appliance.name,
            subtitle = if (channel != null) {
                "Measured by the meter’s ${channel.label.lowercase()} channel"
            } else {
                "Estimate: ${appliance.watts} W for ${appliance.hoursPerDay} hours a day"
            },
            onBack = onBack,
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                label = "Energy",
                value = "${Formatters.formatKwh(dailyKwh)} kWh",
                trend = if (isMeasured) "Measured today" else "Estimate each day",
                color = Cyan,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                label = "Cost",
                value = if (isMeasured) Formatters.formatCurrency(dailyCost) else Formatters.formatMonthlyCost(dailyCost),
                trend = if (isMeasured) "Today so far" else "Estimate each month",
                color = Electric,
                modifier = Modifier.weight(1f),
            )
        }
        if (liveWatts != null) {
            Spacer(Modifier.height(14.dp))
            // Live figures from the meter for this appliance, and each time it ran today.
            SurfaceCard {
                Text("FROM THE METER NOW", style = MaterialTheme.typography.labelMedium, color = Electric)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (liveWatts >= ContextThresholds.APPLIANCE_ON_WATTS) "Running at $liveWatts W" else "Off or standby ($liveWatts W)",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    if (todayRuns.isEmpty()) {
                        "No runs recorded today."
                    } else {
                        "Ran today: " + todayRuns.reversed().joinToString { "${it.startTime}–${it.endTime}" }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Text("WHY IT MATTERS", style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.height(8.dp))
        Text(
            (if (isMeasured) "Today this appliance has used" else "This appliance is estimated to use") +
                " $targetShare% of your $dailyTargetKwh kWh daily target" +
                if (isHighUse) ", which makes it one of your largest loads." else ".",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(18.dp))
        Text("Recommended action", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(recommendedAction, color = TextSecondary)
        Spacer(Modifier.height(28.dp))
        PrimaryAction("Edit appliance", onClick = onEdit)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("DELETE APPLIANCE", color = Coral)
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${appliance.name}?") },
            text = { Text("This removes the appliance and its cost estimate.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) { Text("DELETE", color = Coral) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") } },
        )
    }
}
