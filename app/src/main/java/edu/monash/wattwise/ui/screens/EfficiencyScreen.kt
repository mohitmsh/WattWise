package edu.monash.wattwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.*

/** Explains the score instead of presenting an unexplained sustainability number. */
@Composable
fun EfficiencyScreen(onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader("Today", "Energy efficiency", "How today’s household pattern produced your score.", onBack)
        Spacer(Modifier.height(18.dp))
        CompactMetricRing(
            value = .82f,
            valueText = "82",
            unit = "%",
            label = "Efficiency",
            color = StatusGood,
            modifier = Modifier.width(220.dp).align(Alignment.CenterHorizontally),
            hero = true,
        )
        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(horizontal = 14.dp)) {
            ScoreFactorRow("Peak usage", "Good", "18% below your peak-period baseline", StatusGood)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Base load", "Normal", "Within your usual overnight range", SoftBlue)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Usage vs typical", "−12%", "Compared with recent Tuesdays", StatusGood)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Standby load", "High", "620 W detected overnight", Amber)
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(13.dp)).padding(14.dp)) {
            Text("WHY THIS SCORE", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(7.dp))
            Text(
                "Lower daytime consumption and off-peak appliance use helped most. Overnight standby load prevented a higher score.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text("CONFIDENCE · HIGH", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                "Based on 28 days of simulated half-hour readings. This prototype provides guidance, not a utility-bill forecast.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun ScoreFactorRow(label: String, value: String, explanation: String, signal: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(signal, RoundedCornerShape(50)))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(explanation, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Spacer(Modifier.width(10.dp))
        Text(value.uppercase(), style = MaterialTheme.typography.labelLarge, color = signal)
    }
}
