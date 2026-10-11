package edu.monash.wattwise.ui.screens.insights

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import edu.monash.wattwise.ui.components.CompactMetricRing
import edu.monash.wattwise.ui.components.ScoreFactorRow
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Border
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.SoftBlue
import edu.monash.wattwise.ui.theme.StatusGood
import edu.monash.wattwise.ui.theme.TextSecondary

/**
 * Long-term view of how consistently the household uses energy over 90 days.
 * It uses the same ring and factor rows as the Efficiency screen so the two
 * scores read the same way; Efficiency covers today, Maturity covers the trend.
 */
@Composable
fun EnergyMaturityScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding),
    ) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader(
            eyebrow = "Last 90 days",
            title = "Energy maturity",
            subtitle = "A long-term view of how consistently your household uses energy.",
            onBack = onBack,
        )
        Spacer(Modifier.height(18.dp))
        CompactMetricRing(
            value = .82f,
            valueText = "82",
            unit = "/ 100",
            label = "Maturity",
            color = Electric,
            modifier = Modifier.width(Dimens.HeroRingSize).align(Alignment.CenterHorizontally),
            hero = true,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            "ADVANCED",
            style = MaterialTheme.typography.labelLarge,
            color = Electric,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            "Top 18% of similar households",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(22.dp))
        SurfaceCard(contentPadding = PaddingValues(horizontal = Dimens.CardPadding)) {
            ScoreFactorRow("Base-load health", "78", "Improving", StatusGood)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Peak-demand behaviour", "86", "Strong", Electric)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Appliance efficiency", "74", "Opportunity", Amber)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Usage consistency", "88", "Strong", SoftBlue)
        }
        Spacer(Modifier.height(16.dp))
        SurfaceCard {
            Text("YOUR PROGRESS", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(6.dp))
            Text("Improved 12% this month", style = MaterialTheme.typography.titleLarge)
            Text(
                "Shifting laundry outside peak hours created the largest improvement. " +
                    "Reducing overnight standby load is your next best action.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.height(12.dp))
            Text("HOW IT IS CALCULATED", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                "A prototype score derived from 90 days of simulated usage patterns and household " +
                    "settings. It is comparative guidance, not an energy-rating certificate.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}
