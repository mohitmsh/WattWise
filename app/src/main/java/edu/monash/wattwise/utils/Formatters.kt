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

    fun formatDailyCost(dailyCost: Double): String = "${formatCurrency(dailyCost)} a day"

    fun formatMonthlyCost(dailyCost: Double): String =
        formatCurrency(dailyCost * FormatRules.DAYS_PER_MONTH)

    /** Formats a whole hour on the 24-hour clock, for example "18:00". */
    fun formatHour(hour: Int): String = "$hour:00"

    /** Formats an hour on the 12-hour clock, for example "9 PM". */
    fun formatClockHour(hour: Int): String = when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }

    /** Formats power with two decimal places, for example "2.84". */
    fun formatKilowatts(kilowatts: Double): String =
        String.format(Locale.getDefault(), "%.2f", kilowatts)

    /** Formats energy with one decimal place, for example "14.6". */
    fun formatKwh(kwh: Double): String = String.format(Locale.getDefault(), "%.1f", kwh)

    /** Short weekday name for a chart label, for example "Tue". */
    fun formatWeekday(millis: Long): String =
        SimpleDateFormat(FormatRules.WEEKDAY_PATTERN, Locale.getDefault()).format(Date(millis))

    /** Day of the month for a chart label, for example "13". */
    fun formatDayOfMonth(millis: Long): String =
        SimpleDateFormat(FormatRules.DAY_OF_MONTH_PATTERN, Locale.getDefault()).format(Date(millis))

    /** Formats a latitude or longitude with four decimal places, for example "-37.8136". */
    fun formatCoordinate(degrees: Double): String = String.format(Locale.getDefault(), "%.4f", degrees)

    /** Short date for the report's date range, for example "05 OCT". */
    fun formatShortDate(millis: Long): String =
        SimpleDateFormat(FormatRules.SHORT_DATE_PATTERN, Locale.getDefault()).format(Date(millis)).uppercase()
}
