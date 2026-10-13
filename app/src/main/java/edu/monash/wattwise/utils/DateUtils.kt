package edu.monash.wattwise.utils

import java.util.Calendar
import java.util.TimeZone

/** Date arithmetic used by the usage history and the report. */
object DateUtils {

    /** Midnight at the start of the day that contains [millis]. */
    fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /** Hour of the day, from 0 to 23, for [millis]. */
    fun hourOfDay(millis: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    /** Midnight [days] after [dayStartMillis]; a negative number goes back in time. */
    fun addDays(dayStartMillis: Long, days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dayStartMillis
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    /**
     * The Material date picker stores a date as midnight in UTC, but the app
     * stores midnight in the phone's own time zone. This converts the app's
     * value so the picker highlights the same calendar date.
     */
    fun localDayToPickerMillis(millis: Long): Long {
        val local = Calendar.getInstance()
        local.timeInMillis = millis
        val utc = Calendar.getInstance(TimeZone.getTimeZone(UTC))
        utc.clear()
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        return utc.timeInMillis
    }

    /** The opposite of [localDayToPickerMillis]: the picker's UTC date as local midnight. */
    fun pickerMillisToLocalDay(pickerMillis: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone(UTC))
        utc.timeInMillis = pickerMillis
        val local = Calendar.getInstance()
        local.clear()
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        return local.timeInMillis
    }

    private const val UTC = "UTC"
}
