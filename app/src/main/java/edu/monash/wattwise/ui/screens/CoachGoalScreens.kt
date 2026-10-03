package edu.monash.wattwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.ui.components.*
import edu.monash.wattwise.ui.theme.*
import kotlin.math.roundToInt

private data class CoachInsight(val title: String, val detail: String, val impact: Int, val positive: Boolean)

@Composable
fun CoachScreen(onGoals: () -> Unit, onMaturity: () -> Unit) {
    val insights = listOf(
        CoachInsight("Off-peak appliance use", "Running flexible appliances after 9 PM", 24, true),
        CoachInsight("Cooling at 24°C", "Maintaining an efficient cooling set point", 18, true),
        CoachInsight("Shorter dryer cycles", "Reducing high-load laundry periods", 12, true),
        CoachInsight("Standby devices", "Devices drawing power overnight", 7, false),
        CoachInsight("Peak-hour cooking", "Oven use during evening demand", 5, false),
    )
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        ScreenHeader("Refreshed daily", "WattWise Coach", "See which behaviours help or hurt your Energy Score.")
        Spacer(Modifier.height(18.dp))
        Row {
            Text("HURTS", style = MaterialTheme.typography.labelMedium, color = Amber)
            Spacer(Modifier.weight(1f))
            Text("IMPACT", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.weight(1f))
            Text("HELPS", style = MaterialTheme.typography.labelMedium, color = Electric)
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Row(
                    Modifier.fillMaxWidth().clickable(onClick = onMaturity)
                        .background(Surface, RoundedCornerShape(13.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("ENERGY MATURITY", style = MaterialTheme.typography.labelMedium, color = Electric)
                        Spacer(Modifier.height(5.dp))
                        Text("Advanced", style = MaterialTheme.typography.titleLarge)
                        Text("Top 18% of similar households", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Text("82  ›", style = MaterialTheme.typography.headlineMedium)
                }
            }
            items(insights) { insight ->
                ImpactBar(insight.title, insight.impact, insight.positive)
            }
            item {
                Spacer(Modifier.height(6.dp))
                SmartActionCard(
                    title = "What if you shifted laundry later?",
                    reason = "Try the scenario simulator to compare cost, energy and carbon before changing your routine.",
                    saving = "EXPLORE",
                    action = "OPEN SIMULATOR",
                    onAction = onGoals,
                )
            }
        }
    }
}

@Composable
fun GoalsScreen(onBack: () -> Unit) {
    var hour by remember { mutableFloatStateOf(18f) }
    var days by remember { mutableFloatStateOf(4f) }
    var scenarioSaved by remember { mutableStateOf(false) }
    var reminderScheduled by remember { mutableStateOf(false) }
    val monthlySaving = ((hour - 18f).coerceAtLeast(0f) * .8f + (7f - days) * 2.1f).coerceAtLeast(0f)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader("Goals & planning", "Test a better routine", "Explore estimates before committing to a change.", onBack)
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Energy goal", "342 kWh", "72% on track", Electric, Modifier.weight(1f))
            MetricTile("Carbon goal", "−18%", "5 kg to go", Cyan, Modifier.weight(1f))
        }
        Spacer(Modifier.height(26.dp))
        Text("LAUNDRY SCENARIO", style = MaterialTheme.typography.labelMedium, color = Electric)
        Text("Shift flexible appliance use", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(20.dp))
        Text("Start time: ${hour.roundToInt()}:00", style = MaterialTheme.typography.titleMedium)
        Slider(hour, { hour = it }, valueRange = 18f..23f, steps = 4)
        Text("Later use may avoid peak pricing.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(18.dp))
        Text("Dryer use: ${days.roundToInt()} days per week", style = MaterialTheme.typography.titleMedium)
        Slider(days, { days = it }, valueRange = 1f..7f, steps = 5)
        Spacer(Modifier.height(22.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
            Text("ESTIMATED MONTHLY DIFFERENCE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Text("$${String.format("%.2f", monthlySaving)} saved", style = MaterialTheme.typography.headlineLarge, color = Electric)
            Spacer(Modifier.height(12.dp))
            Text("Estimate based on your selected tariff and current household baseline. Actual results may vary.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(Modifier.height(24.dp))
        PrimaryAction(if (scenarioSaved) "Scenario saved" else "Save this scenario") {
            scenarioSaved = true
        }
        if (scenarioSaved) {
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
                Text("PLAN READY", style = MaterialTheme.typography.labelMedium, color = StatusGood)
                Spacer(Modifier.height(5.dp))
                Text("Laundry after ${hour.roundToInt()}:00 · ${days.roundToInt()} days each week", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (reminderScheduled) "Reminder scheduled for 15 minutes before the planned start."
                    else "Add a reminder so the recommendation becomes an actionable routine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { reminderScheduled = !reminderScheduled }) {
                    Text(if (reminderScheduled) "REMOVE REMINDER" else "SCHEDULE REMINDER", color = Electric)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ProfileScreen(onAppliances: () -> Unit, onGoals: () -> Unit, onLogout: () -> Unit) {
    var alerts by remember { mutableStateOf(true) }
    var weeklySummary by remember { mutableStateOf(true) }
    var highContrast by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(18.dp))
        ScreenHeader("Household", "Alex’s apartment", "2 occupants · Time-of-use tariff")
        Spacer(Modifier.height(22.dp))
        SettingsLink("APPLIANCES", "5 connected profiles", onAppliances)
        SettingsLink("ENERGY GOALS", "15 kWh daily target", onGoals)
        Spacer(Modifier.height(22.dp))
        Text("PREFERENCES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        SettingSwitch("Smart usage alerts", "Unusual load and peak pricing", alerts) { alerts = it }
        SettingSwitch("Weekly summary", "Progress every Monday", weeklySummary) { weeklySummary = it }
        SettingSwitch("Enhanced contrast", "Stronger borders and labels", highContrast) { highContrast = it }
        Spacer(Modifier.height(22.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
            Text("DATA & TRANSPARENCY", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(8.dp))
            Text("Recommendations explain which simulated readings and preferences influenced them. Your prototype data remains on this device.", color = TextSecondary)
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("SIGN OUT") }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsLink(title: String, detail: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 14.dp)) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Text("›", style = MaterialTheme.typography.headlineMedium, color = Electric)
    }
    HorizontalDivider(color = Border)
}

@Composable
private fun SettingSwitch(title: String, detail: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Switch(value, onChange)
    }
}
