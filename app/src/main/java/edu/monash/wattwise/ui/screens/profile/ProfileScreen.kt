package edu.monash.wattwise.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.data.model.HouseholdProfile
import edu.monash.wattwise.ui.components.LabelledSwitch
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.components.SurfaceCard
import edu.monash.wattwise.ui.theme.Border
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters

/** Household summary read from the saved profile, notification preferences and sign-out. */
@Composable
fun ProfileScreen(
    profile: HouseholdProfile?,
    applianceCount: Int,
    onEditHousehold: () -> Unit,
    onAppliances: () -> Unit,
    onGoals: () -> Unit,
    onLogout: () -> Unit,
) {
    var usageAlerts by remember { mutableStateOf(true) }
    var weeklySummary by remember { mutableStateOf(true) }
    var enhancedContrast by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding),
    ) {
        Spacer(Modifier.height(18.dp))
        ScreenHeader(
            eyebrow = "Household",
            title = profile?.homeType ?: "Your household",
            subtitle = profile?.let { "${it.occupants} occupants · ${it.tariff} tariff" }
                ?: "Add your household details to personalise recommendations.",
        )
        Spacer(Modifier.height(22.dp))
        SettingsLink(
            title = "HOUSEHOLD DETAILS",
            detail = profile?.let { "Billing period from ${Formatters.formatFormDate(it.billingStartDateMillis)}" }
                ?: "Not set up yet",
            onClick = onEditHousehold,
        )
        SettingsLink("APPLIANCES", "$applianceCount saved profiles", onAppliances)
        SettingsLink(
            title = "ENERGY GOALS",
            detail = profile?.let { "${it.dailyTargetKwh} kWh daily target" } ?: "No daily target yet",
            onClick = onGoals,
        )
        Spacer(Modifier.height(22.dp))
        Text("PREFERENCES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        LabelledSwitch("Smart usage alerts", "Unusual load and peak pricing", usageAlerts) { usageAlerts = it }
        LabelledSwitch("Weekly summary", "Progress every Monday", weeklySummary) { weeklySummary = it }
        LabelledSwitch("Enhanced contrast", "Stronger borders and labels", enhancedContrast) {
            enhancedContrast = it
        }
        Spacer(Modifier.height(22.dp))
        SurfaceCard {
            Text("DATA & TRANSPARENCY", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(8.dp))
            Text(
                "Recommendations explain which simulated readings and preferences influenced them. " +
                    "Your prototype data remains on this device.",
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("SIGN OUT") }
        Spacer(Modifier.height(28.dp))
    }
}

/** Full-width row that opens another screen, with a divider underneath. */
@Composable
private fun SettingsLink(title: String, detail: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 14.dp),
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = TextPrimary)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        Text("›", style = MaterialTheme.typography.headlineMedium, color = Electric)
    }
    HorizontalDivider(color = Border)
}
