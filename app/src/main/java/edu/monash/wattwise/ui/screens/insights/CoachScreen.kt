package edu.monash.wattwise.ui.screens.insights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.data.SampleData
import edu.monash.wattwise.ui.components.ImpactBar
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.SmartActionCard
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextSecondary

/** Lists the behaviours that help or hurt the Energy Score, with links to deeper screens. */
@Composable
fun CoachScreen(onGoals: () -> Unit, onMaturity: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding)) {
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
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { MaturitySummaryCard(onClick = onMaturity) }
            items(SampleData.coachInsights) { insight ->
                ImpactBar(insight.title, insight.impactPercent, insight.isPositive)
            }
            item {
                Spacer(Modifier.height(6.dp))
                SmartActionCard(
                    title = "What if you shifted laundry later?",
                    reason = "Try the scenario simulator to compare cost, energy and carbon " +
                        "before changing your routine.",
                    saving = "EXPLORE",
                    action = "OPEN SIMULATOR",
                    onAction = onGoals,
                )
            }
        }
    }
}

/** Summary of the long-term maturity score; opens the Energy Maturity screen. */
@Composable
private fun MaturitySummaryCard(onClick: () -> Unit) {
    SurfaceCard(Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ENERGY MATURITY", style = MaterialTheme.typography.labelMedium, color = Electric)
                Spacer(Modifier.height(5.dp))
                Text("Advanced", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Top 18% of similar households",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }
            Text("82  ›", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
