package edu.monash.wattwise.ui.screens.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.Reminders
import edu.monash.wattwise.data.model.ApplianceReminder
import edu.monash.wattwise.data.model.FlexibleAppliance
import edu.monash.wattwise.data.remote.WeatherResponse
import edu.monash.wattwise.ui.components.MetricTile
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Ink
import edu.monash.wattwise.ui.theme.OnAccent
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.WeatherCalculator

/**
 * Bottom sheet opened from the energy map. The user picks an appliance, sees
 * the cheapest time to start it for this place's forecast and their tariff,
 * and can set a daily reminder for that time.
 *
 * @param placeName the place selected on the map, shown so the user knows which forecast is used.
 * @param weather today's forecast for that place.
 * @param reminder the reminder saved in Room, or null when none is set.
 * @param onSetReminder called with the appliance and the start hour when the user asks for a reminder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanApplianceSheet(
    placeName: String,
    weather: WeatherResponse,
    tariff: String,
    reminder: ApplianceReminder?,
    onSetReminder: (FlexibleAppliance, Int) -> Unit,
    onRemoveReminder: () -> Unit,
    onDismiss: () -> Unit,
) {
    var appliance by rememberSaveable { mutableStateOf(FlexibleAppliance.WASHING_MACHINE) }

    // Context-aware step: the tariff and the sunlight forecast decide the best start time.
    val startHour = WeatherCalculator.recommendedStartHour(tariff, weather)
    val bestCost = appliance.loadKwh * WeatherCalculator.rateAt(tariff, startHour)
    val highestCost = appliance.loadKwh * WeatherCalculator.highestRate(tariff)
    val saving = highestCost - bestCost
    val reminderTime = Formatters.formatReminderTime(startHour, Reminders.LEAD_MINUTES)
    val hasThisReminder = reminder?.applianceLabel == appliance.label && reminder.startHour == startHour

    // From Android 13 the app must ask before it can show the reminder notification.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // The alarm is set either way; the notification appears only if it was allowed.
        onSetReminder(appliance, startHour)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Ink, contentColor = TextPrimary) {
        Column(Modifier.fillMaxWidth().padding(horizontal = Dimens.ScreenPadding)) {
            Text("PLAN AN APPLIANCE", style = MaterialTheme.typography.labelMedium, color = Electric)
            Text("Find the cheapest time to start", style = MaterialTheme.typography.titleLarge)
            Text(
                "Uses today’s forecast for $placeName and your ${tariff.lowercase()} tariff.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlexibleAppliance.entries.forEach { option ->
                    FilterChip(
                        selected = appliance == option,
                        onClick = { appliance = option },
                        label = { Text(option.shortLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Electric,
                            selectedLabelColor = OnAccent,
                            containerColor = Surface,
                            labelColor = TextPrimary,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(18.dp))

            Text("BEST START TIME", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(Formatters.formatClockHour(startHour), style = MaterialTheme.typography.headlineLarge, color = Electric)
            Text(
                WeatherCalculator.recommendedStartReason(tariff, weather),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(
                    label = "At the highest price",
                    value = Formatters.formatCurrency(highestCost),
                    trend = "One ${Formatters.formatKwh(appliance.loadKwh)} kWh cycle",
                    color = Amber,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    label = "At ${Formatters.formatClockHour(startHour)}",
                    value = Formatters.formatCurrency(bestCost),
                    trend = if (saving > 0.0) "Save ${Formatters.formatCurrency(saving)} each time" else "Same price all day",
                    color = StatusGood,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(18.dp))

            if (hasThisReminder) {
                SurfaceCard {
                    Text("REMINDER SET", style = MaterialTheme.typography.labelMedium, color = StatusGood)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You get a reminder for the ${appliance.label.lowercase()} every day at $reminderTime.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onRemoveReminder, contentPadding = PaddingValues(0.dp)) {
                        Text("REMOVE REMINDER", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    }
                }
            } else {
                PrimaryAction("Remind me every day at $reminderTime") {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onSetReminder(appliance, startHour)
                    }
                }
                if (reminder != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This replaces your reminder for the ${reminder.applianceLabel.lowercase()} at " +
                            "${Formatters.formatReminderTime(reminder.startHour, Reminders.LEAD_MINUTES)}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
