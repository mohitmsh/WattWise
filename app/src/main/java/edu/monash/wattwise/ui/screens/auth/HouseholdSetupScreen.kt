package edu.monash.wattwise.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.HouseholdOptions
import edu.monash.wattwise.constants.WeatherApi
import edu.monash.wattwise.data.model.HouseholdProfile
import edu.monash.wattwise.ui.components.DateField
import edu.monash.wattwise.ui.components.DropdownField
import edu.monash.wattwise.ui.components.LabelledSlider
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.sliderSteps
import kotlin.math.roundToInt

/**
 * Form that collects the household details recommendations are based on.
 * [existing] fills the form with the saved profile, and [isEditing] switches
 * the wording from first-time setup to updating details from the profile screen.
 *
 * @param userId the signed-in user the saved household belongs to.
 */
@Composable
fun HouseholdSetupScreen(
    userId: String,
    onSave: (HouseholdProfile) -> Unit,
    onCancel: () -> Unit,
    existing: HouseholdProfile? = null,
    isEditing: Boolean = false,
) {
    var homeType by rememberSaveable(existing?.userId) {
        mutableStateOf(existing?.homeType ?: HouseholdOptions.HOME_TYPES.first())
    }
    var tariff by rememberSaveable(existing?.userId) {
        mutableStateOf(existing?.tariff ?: HouseholdOptions.TARIFFS[HouseholdOptions.DEFAULT_TARIFF_INDEX])
    }
    var occupants by rememberSaveable(existing?.userId) {
        mutableFloatStateOf(existing?.occupants?.toFloat() ?: HouseholdOptions.DEFAULT_OCCUPANTS)
    }
    var dailyTargetKwh by rememberSaveable(existing?.userId) {
        mutableFloatStateOf(existing?.dailyTargetKwh?.toFloat() ?: HouseholdOptions.DEFAULT_DAILY_TARGET_KWH)
    }
    var isRenting by rememberSaveable(existing?.userId) { mutableStateOf(existing?.isRenting ?: true) }
    var billingStartDateMillis by rememberSaveable(existing?.userId) {
        mutableLongStateOf(existing?.billingStartDateMillis ?: System.currentTimeMillis())
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPaddingWide),
    ) {
        Text(
            if (isEditing) "HOUSEHOLD DETAILS" else "STEP 2 OF 2",
            style = MaterialTheme.typography.labelMedium,
            color = Electric,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isEditing) "Update your household" else "Build your household baseline",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            "We use these details to make recommendations relevant.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(18.dp))
        Text("* Required", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(10.dp))
        DropdownField("Home type *", HouseholdOptions.HOME_TYPES, homeType, onSelected = { homeType = it })
        Spacer(Modifier.height(14.dp))
        // Radio buttons suit a choice between two options where both should stay visible.
        Text("DO YOU RENT OR OWN? *", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        HouseholdOptions.OWNERSHIP_OPTIONS.forEach { option ->
            val isOptionRenting = option == HouseholdOptions.RENTING
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = isOptionRenting == isRenting, onClick = { isRenting = isOptionRenting }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = isOptionRenting == isRenting, onClick = null)
                Spacer(Modifier.width(10.dp))
                Text(option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 10.dp))
            }
        }
        Text(
            "Renters get advice about habits and settings. Owners also get advice about upgrades.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(14.dp))
        DropdownField("Electricity tariff *", HouseholdOptions.TARIFFS, tariff, onSelected = { tariff = it })
        Spacer(Modifier.height(14.dp))
        DateField(
            label = "Billing period start",
            dateMillis = billingStartDateMillis,
            onDateSelected = { billingStartDateMillis = it },
            hint = "The first day on your latest electricity bill",
        )
        Spacer(Modifier.height(16.dp))
        LabelledSlider(
            label = "Occupants",
            valueText = if (occupants.roundToInt() == 1) "1 person" else "${occupants.roundToInt()} people",
            value = occupants,
            onValueChange = { occupants = it },
            valueRange = HouseholdOptions.MIN_OCCUPANTS..HouseholdOptions.MAX_OCCUPANTS,
            steps = sliderSteps(HouseholdOptions.MIN_OCCUPANTS, HouseholdOptions.MAX_OCCUPANTS, step = 1f),
            hint = "Everyone who lives in the home. More people usually means more energy.",
        )
        Spacer(Modifier.height(16.dp))
        LabelledSlider(
            label = "Daily energy target",
            valueText = "${dailyTargetKwh.roundToInt()} kWh",
            value = dailyTargetKwh,
            onValueChange = { dailyTargetKwh = it },
            valueRange = HouseholdOptions.MIN_DAILY_TARGET_KWH..HouseholdOptions.MAX_DAILY_TARGET_KWH,
            steps = sliderSteps(
                HouseholdOptions.MIN_DAILY_TARGET_KWH,
                HouseholdOptions.MAX_DAILY_TARGET_KWH,
                HouseholdOptions.DAILY_TARGET_STEP_KWH,
            ),
            // 12 kWh a day is the Victorian average for a home of two or three people (ACIL Allen Consulting, 2019).
            hint = "A typical two-person home uses about 12 kWh a day. Home and Usage show each day " +
                "against this target, and you can change it later.",
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAction(if (isEditing) "Save changes" else "Create my dashboard") {
            onSave(
                // The home location is chosen on the map, so keep the saved one when editing.
                HouseholdProfile(
                    userId = userId,
                    homeType = homeType,
                    tariff = tariff,
                    occupants = occupants.roundToInt(),
                    dailyTargetKwh = dailyTargetKwh.roundToInt(),
                    billingStartDateMillis = billingStartDateMillis,
                    isRenting = isRenting,
                    lightTheme = existing?.lightTheme ?: false,
                    usageAlerts = existing?.usageAlerts ?: true,
                    locationName = existing?.locationName ?: WeatherApi.DEFAULT_LOCATION_NAME,
                    latitude = existing?.latitude ?: WeatherApi.DEFAULT_LATITUDE,
                    longitude = existing?.longitude ?: WeatherApi.DEFAULT_LONGITUDE,
                ),
            )
        }
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("CANCEL", color = TextSecondary)
        }
    }
}
