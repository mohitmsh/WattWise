package edu.monash.wattwise.ui.screens.appliance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.R
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceRun
import edu.monash.wattwise.data.model.ApplianceStatus
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.StatusPill
import edu.monash.wattwise.ui.theme.Amber
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.TariffCalculator

/**
 * Searchable list of the household's appliances. Tapping a row opens its detail screen.
 *
 * @param appliances the appliances that match [query], already filtered by a Room query.
 * @param query the text in the search box; it is kept in the ViewModel.
 */
@Composable
fun ApplianceListScreen(
    appliances: List<Appliance>,
    query: String,
    onQueryChange: (String) -> Unit,
    tariff: String,
    dailyTargetKwh: Int,
    applianceRuns: List<ApplianceRun>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onSelect: (Appliance) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding)) {
        Spacer(Modifier.height(16.dp))
        ScreenHeader("Your devices", "Appliances", "Search by name or category. Tap a device to see its cost.", onBack)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search by name or category") },
            placeholder = { Text("e.g. heater or laundry") },
            leadingIcon = { Icon(painterResource(R.drawable.search_icon), contentDescription = null) },
            // A clear button, so an old search never hides new appliances by mistake.
            trailingIcon = {
                if (query.isNotEmpty()) {
                    TextButton(onClick = { onQueryChange("") }) { Text("CLEAR", color = TextSecondary) }
                }
            },
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                Formatters.formatCount(appliances.size, "appliance").uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Spacer(Modifier.weight(1f))
            FilledTonalButton(onClick = onAdd) { Text("+ ADD") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(appliances, key = { it.id }) { appliance ->
                ApplianceRow(appliance, tariff, dailyTargetKwh, applianceRuns, onClick = { onSelect(appliance) })
            }
            if (appliances.isEmpty()) {
                item {
                    Text(
                        text = if (query.isBlank()) {
                            "No appliances yet. Tap + ADD to create your first one."
                        } else {
                            "No appliances match “$query”. Try another name or category."
                        },
                        modifier = Modifier.padding(vertical = 34.dp),
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

/** One appliance in the list: initial, name, category, daily energy, daily cost and status. */
@Composable
private fun ApplianceRow(
    appliance: Appliance,
    tariff: String,
    dailyTargetKwh: Int,
    applianceRuns: List<ApplianceRun>,
    onClick: () -> Unit,
) {
    // A linked appliance shows what the meter measured today; others show the estimate.
    val measuredKwh = EnergyCalculator.measuredKwhToday(appliance, applianceRuns)
    val dailyKwh = measuredKwh ?: EnergyCalculator.applianceDailyKwh(appliance)
    val status = EnergyCalculator.applianceStatus(appliance, dailyTargetKwh)
    val statusColor = if (status == ApplianceStatus.HIGH) Amber else Electric
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Surface, RoundedCornerShape(Dimens.TileRadius))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(46.dp).background(SurfaceRaised, CircleShape), contentAlignment = Alignment.Center) {
            Text(appliance.name.take(1), style = MaterialTheme.typography.titleLarge, color = Electric)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(appliance.name, style = MaterialTheme.typography.titleMedium)
            Text(
                if (measuredKwh != null) {
                    "Measured · ${Formatters.formatKwh(dailyKwh)} kWh today"
                } else {
                    "Estimate · ${Formatters.formatKwh(dailyKwh)} kWh a day"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                Formatters.formatDailyCost(dailyKwh * TariffCalculator.averageRate(tariff)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(5.dp))
            StatusPill(status.label, statusColor)
        }
    }
}
