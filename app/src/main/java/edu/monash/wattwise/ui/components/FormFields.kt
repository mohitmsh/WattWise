package edu.monash.wattwise.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import edu.monash.wattwise.R
import edu.monash.wattwise.ui.theme.Dimens
import edu.monash.wattwise.ui.theme.Electric
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.ui.theme.TextSecondary

/**
 * Full-width button for the main action on a screen. While [isLoading] is
 * true it shows a progress indicator and cannot be tapped a second time.
 */
@Composable
fun PrimaryAction(text: String, modifier: Modifier = Modifier, isLoading: Boolean = false, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.fillMaxWidth().height(Dimens.PrimaryButtonHeight),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        border = BorderStroke(1.dp, Electric),
        shape = RoundedCornerShape(Dimens.PILL_CORNER_PERCENT),
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = Electric, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Password input with a show/hide toggle. The hint stays visible so the
 * password rule is known before the user makes a mistake.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                PasswordVisibilityIcon(passwordVisible)
            }
        },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        supportingText = { if (hint.isNotEmpty()) Text(hint) },
    )
}

/** Eye icon for the password toggle; the crossed-out eye means tapping will hide it. */
@Composable
private fun PasswordVisibilityIcon(passwordVisible: Boolean) {
    Icon(
        painter = painterResource(
            id = if (passwordVisible) R.drawable.visibility_off_icon else R.drawable.visibility_icon,
        ),
        contentDescription = if (passwordVisible) "Hide password" else "Show password",
    )
}

/** Read-only text field that opens a menu of [options] when tapped. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * Slider with its label and current value above it. Every slider in the app
 * uses this composable so they look and behave the same way.
 */
@Composable
fun LabelledSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Text(valueText, style = MaterialTheme.typography.titleLarge)
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, steps = steps)
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

/**
 * Setting row with a title, a short explanation and an on/off switch. The
 * whole row can be tapped, and a screen reader announces it as one switch.
 */
@Composable
fun LabelledSwitch(title: String, detail: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
        // The row handles the tap, so the switch itself only shows the state.
        Switch(checked = checked, onCheckedChange = null)
    }
}
