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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.HouseholdOptions
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
 */
@Composable
fun HouseholdSetupScreen(
    onSave: (HouseholdProfile) -> Unit,
    onCancel: () -> Unit,
    existing: HouseholdProfile? = null,
    isEditing: Boolean = false,
) {
    var homeType by remember(existing) {
        mutableStateOf(existing?.homeType ?: HouseholdOptions.HOME_TYPES.first())
    }
    var tariff by remember(existing) {
        mutableStateOf(existing?.tariff ?: HouseholdOptions.TARIFFS[HouseholdOptions.DEFAULT_TARIFF_INDEX])
    }
    var occupants by remember(existing) {
        mutableFloatStateOf(existing?.occupants?.toFloat() ?: HouseholdOptions.DEFAULT_OCCUPANTS)
    }
    var dailyTargetKwh by remember(existing) {
        mutableFloatStateOf(existing?.dailyTargetKwh?.toFloat() ?: HouseholdOptions.DEFAULT_DAILY_TARGET_KWH)
    }
    var billingStartDateMillis by remember(existing) {
        mutableLongStateOf(existing?.billingStartDateMillis ?: System.currentTimeMillis())
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPaddingWide),
    ) {
        Text(
            if (isEditing) "HOUSEHOLD DETAILS" else "STEP 1 OF 1",
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
        Spacer(Modifier.height(28.dp))
        DropdownField("Home type", HouseholdOptions.HOME_TYPES, homeType, onSelected = { homeType = it })
        Spacer(Modifier.height(14.dp))
        DropdownField("Electricity tariff", HouseholdOptions.TARIFFS, tariff, onSelected = { tariff = it })
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
            valueText = "${occupants.roundToInt()} people",
            value = occupants,
            onValueChange = { occupants = it },
            valueRange = HouseholdOptions.MIN_OCCUPANTS..HouseholdOptions.MAX_OCCUPANTS,
            steps = sliderSteps(HouseholdOptions.MIN_OCCUPANTS, HouseholdOptions.MAX_OCCUPANTS, step = 1f),
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
            hint = "You can change this target later.",
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAction(if (isEditing) "Save changes" else "Create my dashboard") {
            onSave(
                HouseholdProfile(
                    homeType = homeType,
                    tariff = tariff,
                    occupants = occupants.roundToInt(),
                    dailyTargetKwh = dailyTargetKwh.roundToInt(),
                    billingStartDateMillis = billingStartDateMillis,
                ),
            )
        }
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("CANCEL", color = TextSecondary)
        }
    }
}
