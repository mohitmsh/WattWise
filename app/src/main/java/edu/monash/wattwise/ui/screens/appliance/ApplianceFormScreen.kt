package edu.monash.wattwise.ui.screens.appliance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.constants.ApplianceLimits
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceCategory
import edu.monash.wattwise.ui.components.DropdownField
import edu.monash.wattwise.ui.components.LabelledSlider
import edu.monash.wattwise.ui.components.LabelledSwitch
import edu.monash.wattwise.ui.components.PrimaryAction
import edu.monash.wattwise.ui.components.ScreenHeader
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.TextSecondary
import edu.monash.wattwise.utils.Validators
import edu.monash.wattwise.utils.sliderSteps

/**
 * Form for adding or editing an appliance. Passing [existing] fills the form
 * with that appliance's values and switches the screen to edit mode.
 */
@Composable
fun ApplianceFormScreen(
    onCancel: () -> Unit,
    onSave: (name: String, category: ApplianceCategory, watts: Int) -> Unit,
    existing: Appliance? = null,
) {
    val isEditing = existing != null
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var category by remember(existing?.id) {
        mutableStateOf(existing?.category ?: ApplianceCategory.entries.first())
    }
    var watts by remember(existing?.id) {
        mutableFloatStateOf(existing?.watts?.toFloat() ?: ApplianceLimits.DEFAULT_WATTS)
    }
    var smartAlerts by remember { mutableStateOf(true) }
    var attempted by remember { mutableStateOf(false) }
    val nameError = attempted && !Validators.isValidName(name)

    Column(Modifier.fillMaxSize().padding(Dimens.ScreenPadding)) {
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
            onValueChange = { name = it },
            label = { Text("Appliance name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nameError,
            supportingText = { if (nameError) Text("Give the appliance a recognisable name") },
        )
        Spacer(Modifier.height(Dimens.FieldSpacing))
        DropdownField(
            label = "Category",
            options = ApplianceCategory.entries.map { it.label },
            selected = category.label,
            onSelected = { label -> category = ApplianceCategory.entries.first { it.label == label } },
        )
        Spacer(Modifier.height(24.dp))
        LabelledSlider(
            label = "Power rating",
            valueText = "${watts.toInt()} watts",
            value = watts,
            onValueChange = { watts = it },
            valueRange = ApplianceLimits.MIN_WATTS..ApplianceLimits.MAX_WATTS,
            steps = sliderSteps(ApplianceLimits.MIN_WATTS, ApplianceLimits.MAX_WATTS, ApplianceLimits.WATTS_STEP),
            hint = "You can usually find this number on the appliance label.",
        )
        Spacer(Modifier.height(18.dp))
        LabelledSwitch(
            title = "Smart usage alerts",
            detail = "Notify me about unusual usage",
            checked = smartAlerts,
            onCheckedChange = { smartAlerts = it },
        )
        Spacer(Modifier.weight(1f))
        PrimaryAction(if (isEditing) "Save changes" else "Save appliance") {
            attempted = true
            if (Validators.isValidName(name)) onSave(name.trim(), category, watts.toInt())
        }
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("CANCEL", color = TextSecondary)
        }
        Spacer(Modifier.height(16.dp))
    }
}
