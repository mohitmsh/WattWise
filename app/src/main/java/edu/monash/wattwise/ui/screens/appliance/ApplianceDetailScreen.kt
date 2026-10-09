package edu.monash.wattwise.ui.screens.appliance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceStatus
import edu.monash.wattwise.ui.components.MetricTile
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.Coral
import edu.monash.wattwise.ui.theme.Cyan
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters

/** Shows one appliance's rating, cost and advice, with options to edit or delete it. */
@Composable
fun ApplianceDetailScreen(appliance: Appliance, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val usageExplanation = if (appliance.status == ApplianceStatus.HIGH) {
        "Usage is 22% above this appliance’s normal range. Most of the increase occurred during peak pricing."
    } else {
        "Usage is within its expected range and is not currently driving a household anomaly."
    }

    Column(Modifier.fillMaxSize().padding(Dimens.ScreenPadding)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader(appliance.category.label, appliance.name, "Device-level estimate", onBack)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile("Power", "${appliance.watts} W", "Current rating", Cyan, Modifier.weight(1f))
            MetricTile(
                label = "Cost",
                value = Formatters.formatMonthlyCost(appliance.dailyCost),
                trend = "Monthly est.",
                color = Electric,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(22.dp))
        Text("WHY IT MATTERS", style = MaterialTheme.typography.labelMedium, color = Electric)
        Spacer(Modifier.height(8.dp))
        Text(usageExplanation, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(18.dp))
        Text("Recommended action", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Move flexible use after 9 PM and review the result in tomorrow’s Energy Score.",
            color = TextSecondary,
        )
        Spacer(Modifier.weight(1f))
        PrimaryAction("Edit appliance", onClick = onEdit)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("DELETE APPLIANCE", color = Coral)
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${appliance.name}?") },
            text = { Text("This removes the appliance and its usage estimates.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) { Text("DELETE", color = Coral) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") } },
        )
    }
}
