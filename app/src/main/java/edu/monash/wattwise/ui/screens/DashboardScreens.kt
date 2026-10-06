package edu.monash.wattwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.monash.wattwise.ui.components.*
import edu.monash.wattwise.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(
    onLive: () -> Unit,
    onUsage: () -> Unit,
    onCoach: () -> Unit,
    onEfficiency: () -> Unit,
    onAppliances: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        WhoopStyleTopBar()
        Spacer(Modifier.height(8.dp))
        Text("WATTWISE", style = MaterialTheme.typography.labelMedium, color = TextTertiary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(9.dp))
        Row(
            Modifier.align(Alignment.CenterHorizontally).background(Surface, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("18°C OUTSIDE", style = MaterialTheme.typography.labelMedium, color = SoftBlue)
            Spacer(Modifier.width(9.dp))
            Box(Modifier.size(3.dp).background(TextTertiary, RoundedCornerShape(50)))
            Spacer(Modifier.width(9.dp))
            Text("OFF-PEAK UNTIL 3 PM", style = MaterialTheme.typography.labelMedium, color = Electric)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.padding(horizontal = 13.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            CompactMetricRing(.56f, "2.8", "kW", "Live", Cyan, Modifier.weight(1f), onLive)
            CompactMetricRing(.73f, "14.6", "kWh", "Today", SoftBlue, Modifier.weight(1f), onUsage)
            CompactMetricRing(.82f, "82", "%", "Efficiency", StatusGood, Modifier.weight(1f), onEfficiency)
        }
        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("USAGE LOOKS GOOD", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                Text("✓", color = StatusGood)
            }
            Spacer(Modifier.height(5.dp))
            Text("You’re 12% below your typical Tuesday. Mild weather is reducing cooling demand, and peak pricing begins at 5 PM.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MonitorCard("Energy status", "Within target", "12% below average", StatusGood, Modifier.weight(1f))
            MonitorCard("Meter status", "Connected", "Updated 4 sec ago", StatusGood, Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("My Energy", "Details", onUsage)
        Spacer(Modifier.height(6.dp))
        Column(
            Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
            EnergyTimelineRow("1.8 kWh", "Morning usage", "08:00", "Below typical", SoftBlue)
            HorizontalDivider(color = Border)
            EnergyTimelineRow("2.4 kWh", "High usage", "12:15", "Cooling spike", Amber)
            HorizontalDivider(color = Border)
            EnergyTimelineRow("3.1 kWh", "Peak period", "18:30", "Forecast event", Coral)
        }
        Spacer(Modifier.height(26.dp))
        SectionTitle("Today’s insight", "All insights", onCoach)
        Spacer(Modifier.height(6.dp))
        SmartActionCard(
            title = "GOOD ENERGY DAY",
            reason = "Your electricity consumption is 14% lower than your typical Tuesday at this time.",
            saving = "−2.1 kWh",
            action = "VIEW DETAILS",
            onAction = onCoach,
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onAppliances, modifier = Modifier.fillMaxWidth()) {
            Text("VIEW APPLIANCES  ›", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun LiveUsageScreen() {
    val powerReadings = listOf(1.1f, 1.3f, 1.2f, 1.7f, 1.5f, 2.2f, 2.0f, 2.7f, 2.4f, 2.84f)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(8.dp))
        WhoopStyleTopBar()
        Spacer(Modifier.height(10.dp))
        Text("LIVE POWER", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Spacer(Modifier.height(14.dp))
        CompactMetricRing(.71f, "2.84", "kW", "Live power", Cyan, Modifier.width(220.dp).align(Alignment.CenterHorizontally), hero = true)
        Spacer(Modifier.height(10.dp))
        Text("MODERATE", style = MaterialTheme.typography.labelLarge, color = Amber, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text("Current load is 9% above your usual afternoon range", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("LAST 30 MINUTES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.weight(1f))
                Text("LIVE ●", style = MaterialTheme.typography.labelMedium, color = StatusGood)
            }
            Spacer(Modifier.height(18.dp))
            EnergyLineChart(powerReadings, Cyan)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("−30 MIN", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                Text("NOW", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(horizontal = 14.dp)) {
            MetricRow("Voltage", "235 V", "+1 V vs average", Cyan)
            HorizontalDivider(color = Border)
            MetricRow("Current", "4.9 A", "Stable", Cyan)
            HorizontalDivider(color = Border)
            MetricRow("Power factor", "0.94", "Good", StatusGood)
            HorizontalDivider(color = Border)
            MetricRow("Frequency", "50 Hz", "Normal range", StatusGood)
        }
        Spacer(Modifier.height(26.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen() {
    var period by remember { mutableStateOf("DAY") }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val dateText = remember(selectedDate) { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(selectedDate)).uppercase() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(8.dp))
        WhoopStyleTopBar()
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ENERGY USAGE", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { showDatePicker = true }) { Text(dateText, style = MaterialTheme.typography.labelMedium, color = TextSecondary) }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(50)).padding(4.dp)) {
            listOf("DAY", "WEEK", "MONTH").forEach { item ->
                val selected = period == item
                Box(
                    Modifier.weight(1f).clickable { period = item }
                        .background(if (selected) SurfaceRaised else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(50))
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(item, style = MaterialTheme.typography.labelMedium, color = if (selected) TextPrimary else TextTertiary)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(if (period == "DAY") "18.6" else if (period == "WEEK") "84.6" else "342", fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 52.sp)
            Spacer(Modifier.width(6.dp))
            Text("kWh", style = MaterialTheme.typography.titleMedium, color = TextSecondary, modifier = Modifier.padding(bottom = 9.dp))
        }
        Text("↓ 8% vs typical ${period.lowercase()}", style = MaterialTheme.typography.bodyMedium, color = StatusGood)
        Spacer(Modifier.height(22.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
            EnergyLineChart(listOf(8f, 11f, 9f, 15f, 13f, 12f, 10f), Cyan)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { Text(it, style = MaterialTheme.typography.labelMedium, color = TextTertiary) }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("USAGE VS TYPICAL RANGE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(horizontal = 14.dp, vertical = 5.dp)) {
            RangeBar("Morning", "4.8 kWh", .48f)
            RangeBar("Afternoon", "5.2 kWh", .56f)
            RangeBar("Evening", "8.6 kWh", .86f, Amber)
        }
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
            Text("ON TRACK", style = MaterialTheme.typography.labelMedium, color = StatusGood)
            Spacer(Modifier.height(9.dp))
            Text("You’re projected to spend $18 less than your typical monthly usage.", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.height(26.dp))
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { selectedDate = it }; showDatePicker = false }) { Text("SELECT") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("CANCEL") } },
        ) { DatePicker(state) }
    }
}
