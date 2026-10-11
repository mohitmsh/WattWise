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

/** Explains today's efficiency score by listing the factors that produced it. */
@Composable
fun EfficiencyScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding),
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
            modifier = Modifier.width(Dimens.HeroRingSize).align(Alignment.CenterHorizontally),
            hero = true,
        )
        Spacer(Modifier.height(18.dp))
        SurfaceCard(contentPadding = PaddingValues(horizontal = Dimens.CardPadding)) {
            ScoreFactorRow("Peak usage", "Good", "18% below your peak-period baseline", StatusGood)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Base load", "Normal", "Within your usual overnight range", SoftBlue)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Usage vs typical", "−12%", "Compared with recent Tuesdays", StatusGood)
            HorizontalDivider(color = Border)
            ScoreFactorRow("Standby load", "High", "620 W detected overnight", Amber)
        }
        Spacer(Modifier.height(16.dp))
        SurfaceCard {
            Text("WHY THIS SCORE", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(7.dp))
            Text(
                "Lower daytime consumption and off-peak appliance use helped most. " +
                    "Overnight standby load prevented a higher score.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            Text("CONFIDENCE · HIGH", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                "Based on 28 days of simulated half-hour readings. This prototype provides guidance, " +
                    "not a utility-bill forecast.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(26.dp))
    }
}
