package edu.monash.wattwise.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.data.SampleData
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.DashboardTopBar
import edu.monash.wattwise.ui.components.EnergyLineChart
import edu.monash.wattwise.ui.components.MetricRow
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Border
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.ui.theme.TextTertiary

/** Shows the household's current power draw and the electrical readings behind it. */
@Composable
fun LiveUsageScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPaddingCompact),
    ) {
        Spacer(Modifier.height(8.dp))
        DashboardTopBar()
        Spacer(Modifier.height(10.dp))
        Text("LIVE POWER", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Spacer(Modifier.height(14.dp))
        CompactMetricRing(
            value = .71f,
            valueText = "2.84",
            unit = "kW",
            label = "Live power",
            color = Cyan,
            modifier = Modifier.width(Dimens.HeroRingSize).align(Alignment.CenterHorizontally),
            hero = true,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "MODERATE",
            style = MaterialTheme.typography.labelLarge,
            color = Amber,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            "Current load is 9% above your usual afternoon range",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(20.dp))
        SurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("LAST 30 MINUTES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.weight(1f))
                Text("LIVE ●", style = MaterialTheme.typography.labelMedium, color = StatusGood)
            }
            Spacer(Modifier.height(18.dp))
            EnergyLineChart(SampleData.livePowerReadingsKw, Cyan)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("−30 MIN", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                Text("NOW", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
        Spacer(Modifier.height(18.dp))
        SurfaceCard(contentPadding = PaddingValues(horizontal = Dimens.CardPadding)) {
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
