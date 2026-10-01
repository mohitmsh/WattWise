package edu.monash.wattwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.StatusPill
import edu.monash.wattwise.ui.model.Appliance
import edu.monash.wattwise.ui.theme.*

@Composable
fun ApplianceListScreen(
    appliances: List<Appliance>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onSelect: (Appliance) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = appliances.filter {
        it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        ScreenHeader("Your devices", "Appliances", "Tap a device to see its impact.", onBack)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search appliances") },
            leadingIcon = { Text("⌕") },
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${filtered.size} APPLIANCES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.weight(1f))
            FilledTonalButton(onClick = onAdd) { Text("+ ADD") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(filtered, key = { it.id }) { appliance ->
                ApplianceRow(appliance, onClick = { onSelect(appliance) })
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "No appliances match “$query”. Try another name or category.",
                        modifier = Modifier.padding(vertical = 34.dp),
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplianceRow(appliance: Appliance, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .background(Surface, RoundedCornerShape(18.dp)).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(46.dp).background(SurfaceRaised, CircleShape), contentAlignment = Alignment.Center) {
            Text(appliance.name.take(1), style = MaterialTheme.typography.titleLarge, color = Electric)
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(appliance.name, style = MaterialTheme.typography.titleMedium)
            Text("${appliance.category} · ${appliance.watts} W", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(appliance.cost, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(5.dp))
            StatusPill(appliance.status, if (appliance.status == "High") Coral else Electric)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplianceFormScreen(
    onBack: () -> Unit,
    onSave: (String, String, Int) -> Unit,
    existing: Appliance? = null,
) {
    val categories = listOf("Kitchen", "Cooling", "Heating", "Laundry", "Electronics")
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var category by remember(existing?.id) { mutableStateOf(existing?.category ?: categories.first()) }
    var watts by remember(existing?.id) { mutableFloatStateOf(existing?.watts?.toFloat() ?: 800f) }
    var expanded by remember { mutableStateOf(false) }
    var smartAlerts by remember { mutableStateOf(true) }
    var attempted by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader("Appliance profile", if (existing == null) "Add appliance" else "Edit appliance", "Provide only what is needed for useful estimates.", onBack)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            name, { name = it }, label = { Text("Appliance name") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            isError = attempted && name.isBlank(),
            supportingText = if (attempted && name.isBlank()) ({ Text("Give the appliance a recognisable name") }) else null,
        )
        Spacer(Modifier.height(12.dp))
        ExposedDropdownMenuBox(expanded, { expanded = it }) {
            OutlinedTextField(
                category, {}, readOnly = true, label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded, { expanded = false }) {
                categories.forEach { item -> DropdownMenuItem({ Text(item) }, { category = item; expanded = false }) }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("POWER RATING", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Text("${watts.toInt()} watts", style = MaterialTheme.typography.titleLarge)
        Slider(value = watts, onValueChange = { watts = it }, valueRange = 50f..3000f, steps = 58)
        Text("You can usually find this number on the appliance label.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Smart usage alerts", style = MaterialTheme.typography.titleMedium)
                Text("Notify me about unusual usage", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Switch(smartAlerts, { smartAlerts = it })
        }
        Spacer(Modifier.weight(1f))
        PrimaryAction(if (existing == null) "Save appliance" else "Save changes") {
            attempted = true
            if (name.isNotBlank()) onSave(name, category, watts.toInt())
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ApplianceDetailScreen(appliance: Appliance, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader(appliance.category, appliance.name, "Device-level estimate", onBack)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            edu.monash.wattwise.ui.components.MetricTile("Power", "${appliance.watts} W", "Current rating", Cyan, Modifier.weight(1f))
            edu.monash.wattwise.ui.components.MetricTile("Cost", "$31.20", "Monthly est.", Electric, Modifier.weight(1f))
        }
        Spacer(Modifier.height(22.dp))
        Text("WHY IT MATTERS", style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.height(8.dp))
        Text(
            if (appliance.status == "High") "Usage is 22% above this appliance’s normal range. Most of the increase occurred during peak pricing."
            else "Usage is within its expected range and is not currently driving a household anomaly.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(18.dp))
        Text("Recommended action", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Move flexible use after 9 PM and review the result in tomorrow’s Energy Score.", color = TextSecondary)
        Spacer(Modifier.weight(1f))
        PrimaryAction("Edit appliance", onClick = onEdit)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { showDelete = true }, modifier = Modifier.fillMaxWidth()) { Text("DELETE APPLIANCE", color = Coral) }
        Spacer(Modifier.height(16.dp))
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete ${appliance.name}?") },
            text = { Text("This removes the appliance from this prototype’s in-memory list.") },
            confirmButton = { TextButton(onClick = onDelete) { Text("DELETE", color = Coral) } },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("CANCEL") } },
        )
    }
}
