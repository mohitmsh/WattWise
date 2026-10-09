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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.R
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceStatus
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.StatusPill
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.Surface
import edu.monash.wattwise.ui.theme.SurfaceRaised
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters

/** Searchable list of the household's appliances. Tapping a row opens its detail screen. */
@Composable
fun ApplianceListScreen(
    appliances: List<Appliance>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onSelect: (Appliance) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    // Search matches either the appliance name or its category.
    val matchingAppliances = appliances.filter {
        it.name.contains(query, ignoreCase = true) || it.category.label.contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding)) {
        Spacer(Modifier.height(16.dp))
        ScreenHeader("Your devices", "Appliances", "Tap a device to see its impact.", onBack)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search appliances") },
            leadingIcon = { Icon(painterResource(R.drawable.search_icon), contentDescription = null) },
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${matchingAppliances.size} APPLIANCES",
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
            items(matchingAppliances, key = { it.id }) { appliance ->
                ApplianceRow(appliance, onClick = { onSelect(appliance) })
            }
            if (matchingAppliances.isEmpty()) {
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

/** One appliance in the list: initial, name, category, power, cost and status. */
@Composable
private fun ApplianceRow(appliance: Appliance, onClick: () -> Unit) {
    val statusColor = if (appliance.status == ApplianceStatus.HIGH) Coral else Electric
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
                "${appliance.category.label} · ${appliance.watts} W",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(Formatters.formatDailyCost(appliance.dailyCost), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(5.dp))
            StatusPill(appliance.status.label, statusColor)
        }
    }
}
