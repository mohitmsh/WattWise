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
import androidx.compose.ui.graphics.RectangleShape
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
    onLightThemeChange: (Boolean) -> Unit,
    onUsageAlertsChange: (Boolean) -> Unit,
    onHomeLocation: () -> Unit,
    onAppliances: () -> Unit,
    onBill: () -> Unit,
    userName: String?,
    userEmail: String?,
    onLogout: () -> Unit,
) {

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
            subtitle = profile?.let {
                "${if (it.isRenting) "Renting" else "Owner"} · ${it.occupants} occupants · ${it.tariff} tariff"
            }
                ?: "Add your household details to personalise recommendations.",
        )
        Spacer(Modifier.height(22.dp))
        SettingsLink(
            title = "HOUSEHOLD DETAILS",
            detail = profile?.let { "Billing period from ${Formatters.formatFormDate(it.billingStartDateMillis)}" }
                ?: "Not set up yet",
            onClick = onEditHousehold,
        )
        SettingsLink(
            title = "ENERGY MAP & HOME LOCATION",
            detail = profile?.let { "${it.locationName} · used for weather advice" } ?: "Melbourne (default)",
            onClick = onHomeLocation,
        )
        SettingsLink("APPLIANCES", "${Formatters.formatCount(applianceCount, "saved profile")}", onAppliances)
        SettingsLink(
            title = "BILL FORECAST & BUDGET",
            detail = profile?.let { "$${it.monthlyBudget} budget for each billing period" } ?: "No budget yet",
            onClick = onBill,
        )
        Spacer(Modifier.height(22.dp))
        Text("PREFERENCES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        LabelledSwitch(
            title = "Smart usage alerts",
            detail = "A notification on the day you pass your daily target",
            checked = profile?.usageAlerts ?: true,
            onCheckedChange = onUsageAlertsChange,
        )
        LabelledSwitch(
            title = "Light theme",
            detail = "A bright background, easier to read in daylight",
            checked = profile?.lightTheme ?: false,
            onCheckedChange = onLightThemeChange,
        )
        Spacer(Modifier.height(22.dp))
        SurfaceCard {
            Text("DATA & TRANSPARENCY", style = MaterialTheme.typography.labelMedium, color = Electric)
            Spacer(Modifier.height(8.dp))
            Text(
                "Every recommendation shows the readings and settings that produced it. Meter readings " +
                    "are simulated from the public REFIT dataset. Your household and appliances are saved on this device " +
                    "and backed up to your account, so they return if you install the app again.",
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(24.dp))
        // The account that is signed in, from Firebase Authentication.
        Text("SIGNED IN AS", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Text(userName ?: "WattWise user", style = MaterialTheme.typography.titleMedium)
        Text(userEmail ?: "", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(12.dp))
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
        // A square shape: the button's default rounded corners cut into the first letter of the title.
        shape = RectangleShape,
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
