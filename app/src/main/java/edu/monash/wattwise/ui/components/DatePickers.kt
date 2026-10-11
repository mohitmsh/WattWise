package edu.monash.wattwise.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import edu.monash.wattwise.R
import edu.monash.wattwise.utils.Formatters

/**
 * Read-only form field showing a date. Tapping the calendar icon opens a
 * date picker, so the user never has to type a date by hand.
 */
@Composable
fun DateField(
    label: String,
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = Formatters.formatFormDate(dateMillis),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(painterResource(R.drawable.calendar_icon), contentDescription = "Select date")
            }
        },
        supportingText = { if (hint != null) Text(hint) },
        modifier = modifier.fillMaxWidth(),
    )
    if (showDatePicker) {
        DatePickerPopup(
            initialDateMillis = dateMillis,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePicker = false },
        )
    }
}

/** Date picker dialog; reports the chosen date only when the user confirms. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerPopup(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onDateSelected)
                    onDismiss()
                },
            ) { Text("SELECT") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } },
    ) {
        DatePicker(datePickerState)
    }
}
