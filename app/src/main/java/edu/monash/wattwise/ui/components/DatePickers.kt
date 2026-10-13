package edu.monash.wattwise.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import edu.monash.wattwise.R
import edu.monash.wattwise.utils.DateUtils
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
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
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

/**
 * Date picker dialog; reports the chosen date only when the user confirms.
 * Dates after today cannot be chosen, because every date in the app is one
 * that has already happened (a bill date or a day of saved usage).
 *
 * @param initialDateMillis the date to highlight, in the phone's own time zone.
 * @param onDateSelected receives midnight of the chosen day in the phone's own time zone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerPopup(
    initialDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayInPicker = DateUtils.localDayToPickerMillis(System.currentTimeMillis())
    val datePickerState = rememberDatePickerState(
        // The picker works in UTC, so convert the app's local date before showing it.
        initialSelectedDateMillis = DateUtils.localDayToPickerMillis(initialDateMillis),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayInPicker
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(DateUtils.pickerMillisToLocalDay(it)) }
                    onDismiss()
                },
            ) { Text("SELECT") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } },
    ) {
        DatePicker(datePickerState)
    }
}
