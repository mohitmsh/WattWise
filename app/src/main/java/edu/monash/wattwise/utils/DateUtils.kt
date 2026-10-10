package edu.monash.wattwise.utils

import java.util.Calendar

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
}
