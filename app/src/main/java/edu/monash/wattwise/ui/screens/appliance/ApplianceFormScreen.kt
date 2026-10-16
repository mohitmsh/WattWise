package edu.monash.wattwise.ui.screens.appliance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.ApplianceLimits
import edu.monash.wattwise.constants.ValidationRules
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceCategory
import edu.monash.wattwise.data.model.MeterAppliance
import edu.monash.wattwise.ui.components.DropdownField
import edu.monash.wattwise.ui.components.LabelledSlider
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Formatters
import edu.monash.wattwise.utils.Validators
import edu.monash.wattwise.utils.sliderSteps
import kotlin.math.roundToInt

/**
 * Form for adding or editing an appliance. Passing [existing] fills the form
 * with that appliance's values and switches the screen to edit mode.
 *
 * @param otherNames names of the user's other appliances, used to stop a repeated name.
 */
@Composable
fun ApplianceFormScreen(
    onCancel: () -> Unit,
    onSave: (
        name: String,
        category: ApplianceCategory,
        watts: Int,
        hoursPerDay: Double,
        meterChannel: MeterAppliance?,
    ) -> Unit,
    existing: Appliance? = null,
    otherNames: List<String> = emptyList(),
) {
    val isEditing = existing != null
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var category by rememberSaveable(existing?.id) {
        mutableStateOf(existing?.category ?: ApplianceCategory.entries.first())
    }
    var watts by rememberSaveable(existing?.id) {
        mutableFloatStateOf(existing?.watts?.toFloat() ?: ApplianceLimits.DEFAULT_WATTS)
    }
    var hoursPerDay by rememberSaveable(existing?.id) {
        mutableFloatStateOf(existing?.hoursPerDay?.toFloat() ?: ApplianceLimits.DEFAULT_HOURS_PER_DAY)
    }
    var meterChannel by rememberSaveable(existing?.id) { mutableStateOf(existing?.meterChannel) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    // A slider gives a Float such as 2.4999998, so round it to the nearest step before it is shown or saved.
    val roundedWatts = ((watts / ApplianceLimits.WATTS_STEP).roundToInt() * ApplianceLimits.WATTS_STEP).roundToInt()
    val roundedHours = (hoursPerDay / ApplianceLimits.HOURS_STEP).roundToInt() * ApplianceLimits.HOURS_STEP.toDouble()

    // Two appliances with the same name cannot be told apart in the list, so a repeat is not allowed.
    val isDuplicate = otherNames.any { it.equals(name.trim(), ignoreCase = true) }
    val nameValid = Validators.isValidName(name) && !isDuplicate
    val nameError = attempted && !nameValid

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding)) {
        Spacer(Modifier.height(14.dp))
        ScreenHeader(
            eyebrow = "Appliance profile",
            title = if (isEditing) "Edit appliance" else "Add appliance",
            subtitle = "Provide only what is needed for useful estimates.",
            onBack = onCancel,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            // Extra characters are not accepted once the name reaches the longest length allowed.
            onValueChange = { if (it.length <= ValidationRules.MAX_NAME_LENGTH) name = it },
            label = { Text("Appliance name *") },
            placeholder = { Text("e.g. Living room heater") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError,
            supportingText = {
                Text(
                    when {
                        nameError && isDuplicate -> "You already have an appliance with this name"
                        nameError -> "Enter a name for the appliance"
                        else -> "* Required · ${name.length} of ${ValidationRules.MAX_NAME_LENGTH} characters"
                    },
                )
            },
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        DropdownField(
            label = "Category",
            options = ApplianceCategory.entries.map { it.label },
            selected = category.label,
            onSelected = { label -> category = ApplianceCategory.entries.first { it.label == label } },
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        // Linking a channel works like a smart plug: the appliance's energy is then measured, not estimated.
        DropdownField(
            label = "Measured by meter channel",
            options = listOf(ApplianceLimits.NOT_MEASURED) + MeterAppliance.entries.map { it.label },
            selected = meterChannel?.label ?: ApplianceLimits.NOT_MEASURED,
            onSelected = { label -> meterChannel = MeterAppliance.entries.firstOrNull { it.label == label } },
        )
        Text(
            "Choose a channel if your meter monitors this appliance. Otherwise its energy is " +
                "estimated from the power and hours below.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
        Spacer(Modifier.height(24.dp))
        LabelledSlider(
            label = "Power rating",
            valueText = "$roundedWatts watts",
            value = watts,
            onValueChange = { watts = it },
            valueRange = ApplianceLimits.MIN_WATTS..ApplianceLimits.MAX_WATTS,
            steps = sliderSteps(ApplianceLimits.MIN_WATTS, ApplianceLimits.MAX_WATTS, ApplianceLimits.WATTS_STEP),
            hint = "You can usually find this number on the appliance label.",
        )
        Spacer(Modifier.height(18.dp))
        LabelledSlider(
            label = "Use on a normal day",
            valueText = "${Formatters.formatKwh(roundedHours)} hours",
            value = hoursPerDay,
            onValueChange = { hoursPerDay = it },
            valueRange = ApplianceLimits.MIN_HOURS_PER_DAY..ApplianceLimits.MAX_HOURS_PER_DAY,
            steps = sliderSteps(ApplianceLimits.MIN_HOURS_PER_DAY, ApplianceLimits.MAX_HOURS_PER_DAY, ApplianceLimits.HOURS_STEP),
            hint = "Power and hours together give the appliance's daily energy and cost.",
        )
        Spacer(Modifier.height(28.dp))
        PrimaryAction(if (isEditing) "Save changes" else "Save appliance") {
            attempted = true
            if (nameValid) onSave(name.trim(), category, roundedWatts, roundedHours, meterChannel)
        }
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("CANCEL", color = TextSecondary)
        }
        Spacer(Modifier.height(16.dp))
    }
}
