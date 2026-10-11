package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.FormatRules
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Converts raw values into the text shown on screen. */
object Formatters {

    /** Formats a timestamp for the usage date selector, for example "TUE, 13 OCT". */
    fun formatDisplayDate(millis: Long): String =
        SimpleDateFormat(FormatRules.DISPLAY_DATE_PATTERN, Locale.getDefault())
            .format(Date(millis))
            .uppercase()

    /** Formats a timestamp for form fields, for example "13 Oct 2026". */
    fun formatFormDate(millis: Long): String =
        SimpleDateFormat(FormatRules.FORM_DATE_PATTERN, Locale.getDefault()).format(Date(millis))

    /** Formats a dollar amount with two decimal places, for example "$1.84". */
    fun formatCurrency(amount: Double): String =
        String.format(Locale.getDefault(), "$%.2f", amount)

    fun formatDailyCost(dailyCost: Double): String = "${formatCurrency(dailyCost)} today"

    fun formatMonthlyCost(dailyCost: Double): String =
        formatCurrency(dailyCost * FormatRules.DAYS_PER_MONTH)

    /** Formats a whole hour on the 24-hour clock, for example "18:00". */
    fun formatHour(hour: Int): String = "$hour:00"
}
