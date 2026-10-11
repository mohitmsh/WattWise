package edu.monash.wattwise.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.DashboardTopBar
import edu.monash.wattwise.ui.components.EnergyTimelineRow
import edu.monash.wattwise.ui.components.MonitorCard
import edu.monash.wattwise.ui.components.SectionTitle
import edu.monash.wattwise.ui.components.SmartActionCard
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Border
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.SoftBlue
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary
import kotlin.math.roundToInt

/**
 * Home screen: a summary of live power, today's usage and efficiency, with
 * shortcuts to the screens that explain each figure.
 */
@Composable
fun TodayScreen(
    outsideTemperature: Double?,
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
        DashboardTopBar()
        Spacer(Modifier.height(8.dp))
        Text(
            "WATTWISE",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(9.dp))
        ContextChip(outsideTemperature, Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.padding(horizontal = 13.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            CompactMetricRing(.56f, "2.8", "kW", "Live", Cyan, Modifier.weight(1f), onLive)
            CompactMetricRing(.73f, "14.6", "kWh", "Today", SoftBlue, Modifier.weight(1f), onUsage)
            CompactMetricRing(.82f, "82", "%", "Efficiency", StatusGood, Modifier.weight(1f), onEfficiency)
        }
        Spacer(Modifier.height(18.dp))
        SurfaceCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("USAGE LOOKS GOOD", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.weight(1f))
                Text("✓", color = StatusGood)
            }
            Spacer(Modifier.height(5.dp))
            Text(
                "You’re 12% below your typical Tuesday. Mild weather is reducing cooling demand, " +
                    "and peak pricing begins at 5 PM.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MonitorCard("Energy status", "Within target", "12% below average", StatusGood, Modifier.weight(1f))
            MonitorCard("Meter status", "Connected", "Updated 4 sec ago", StatusGood, Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("My Energy", "Details", onUsage)
        Spacer(Modifier.height(6.dp))
        SurfaceCard(contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp)) {
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

/** Shows the context the recommendations are based on: outside temperature and tariff period. */
@Composable
private fun ContextChip(outsideTemperature: Double?, modifier: Modifier = Modifier) {
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
        Text("OFF-PEAK UNTIL 3 PM", style = MaterialTheme.typography.labelMedium, color = Electric)
    }
}
