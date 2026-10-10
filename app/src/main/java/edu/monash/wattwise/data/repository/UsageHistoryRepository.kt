package edu.monash.wattwise.data.repository

import android.content.Context
import android.util.Log
import edu.monash.wattwise.constants.EnergyUnits
import edu.monash.wattwise.constants.SmartMeter
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.InputStreamReader

/** Single access point for the hourly usage history saved in Room. */
class UsageHistoryRepository(private val context: Context) {

    private val hourlyUsageDao = WattWiseDatabase.getDatabase(context).hourlyUsageDao()

    val allUsage: Flow<List<HourlyUsage>> = hourlyUsageDao.getAllUsage()

    /** Total kWh saved so far for today. */
    suspend fun getTodayTotal(): Double =
        hourlyUsageDao.getTotalForDay(DateUtils.startOfDay(System.currentTimeMillis())) ?: 0.0

    /**
     * Saves every finished hour that is not in Room yet and returns how many
     * were added. The first run adds [SmartMeter.HISTORY_DAYS] days of history;
     * later runs add only the hours since the previous run.
     *
     * Today's hours come from the same REFIT day the live simulator plays, and
     * earlier days repeat the other days in the hourly file in order.
     */
    suspend fun saveMissingHours(nowMillis: Long = System.currentTimeMillis()): Int = saveLock.withLock {
        val datasetDays = loadHourlyFile(SmartMeter.HOURLY_FILE_NAME)
        val historyDates = datasetDays.keys.filter { it != SmartMeter.LIVE_DAY }.sorted()
        if (historyDates.isEmpty()) return@withLock 0

        val todayStart = DateUtils.startOfDay(nowMillis)
        val currentHour = DateUtils.hourOfDay(nowMillis)

        // Continue from the hour after the last one saved, or from the start of the history.
        val latest = hourlyUsageDao.getLatestUsage()
        var day = latest?.dayStartMillis ?: DateUtils.addDays(todayStart, -SmartMeter.HISTORY_DAYS)
        var hour = if (latest == null) 0 else latest.hour + 1

        val newRows = mutableListOf<HourlyUsage>()
        while (true) {
            if (hour == EnergyUnits.HOURS_PER_DAY) {
                day = DateUtils.addDays(day, 1)
                hour = 0
            }
            val reachedCurrentHour = day > todayStart || (day == todayStart && hour >= currentHour)
            if (reachedCurrentHour) break

            val datasetDate = if (day == todayStart) {
                SmartMeter.LIVE_DAY
            } else {
                // The day number picks the next dataset day, going round again at the end.
                val dayNumber = day / EnergyUnits.MILLIS_PER_DAY
                historyDates[(dayNumber % historyDates.size).toInt()]
            }
            val kwh = datasetDays[datasetDate]?.get(hour) ?: 0.0
            newRows.add(HourlyUsage(dayStartMillis = day, hour = hour, kwh = kwh))
            hour++
        }

        if (newRows.isNotEmpty()) hourlyUsageDao.insertAll(newRows)
        Log.d(TAG, "Saved ${newRows.size} new hourly usage rows to Room")
        newRows.size
    }

    /** Reads the hourly CSV file into a map from date to that day's kWh for each hour. */
    private fun loadHourlyFile(fileName: String): Map<String, Map<Int, Double>> {
        val days = mutableMapOf<String, MutableMap<Int, Double>>()
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.useLines { lines ->
                lines.drop(1).forEach { line -> // Skip header
                    val values = line.split(',')
                    if (values.size >= 3) {
                        val date = values[0].trim()
                        val hours = days.getOrPut(date) { mutableMapOf() }
                        hours[values[1].trim().toInt()] = values[2].trim().toDouble()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not read $fileName", e)
        }
        return days
    }

    private companion object {
        const val TAG = "USAGE_HISTORY"

        // Only one save may run at a time, so two workers can never save the same hour twice.
        val saveLock = Mutex()
    }
}
