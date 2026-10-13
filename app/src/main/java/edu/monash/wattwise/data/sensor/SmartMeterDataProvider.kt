package edu.monash.wattwise.data.sensor

import android.content.Context
import android.util.Log
import edu.monash.wattwise.constants.SmartMeter
import edu.monash.wattwise.data.model.MeterReading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

/**
 * Simulates a smart meter. The emulator has no real meter, so this class reads
 * one day of the public REFIT dataset from the assets folder and emits its
 * rows one at a time, the way a real sensor would report new values.
 */
class SmartMeterDataProvider(private val context: Context) {

    // "by lazy" reads the file only once, the first time the readings are needed.
    val dayReadings: List<MeterReading> by lazy { loadMeterReadings(SmartMeter.LIVE_FILE_NAME) }

    /** Emits readings at the speed they were recorded, starting at the current time of day. */
    fun sensorDataFlow(): Flow<MeterReading> {
        val meterFlow: Flow<MeterReading> = flow {
            val readings = dayReadings
            if (readings.isEmpty()) return@flow

            var index = indexForTime(readings, currentTimeText())
            Log.d(TAG, "Simulator started with ${readings.size} readings at ${readings[index].time}")
            while (true) {
                val current = readings[index]
                emit(current)
                // Go back to the first row after the last one, so the simulator never stops.
                index = (index + 1) % readings.size
                // Wait as long as the real meter did between these two rows (about seven seconds).
                val gapSeconds = readings[index].unixSeconds - current.unixSeconds
                delay(gapSeconds.coerceIn(SmartMeter.MIN_REFRESH_SECONDS, SmartMeter.MAX_REFRESH_SECONDS).seconds)
            }
        }
        return meterFlow
    }

    /** Reads every row of the CSV file in assets into a list. */
    private fun loadMeterReadings(fileName: String): List<MeterReading> {
        val readings = mutableListOf<MeterReading>()
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.useLines { lines ->
                lines.drop(1).forEach { line -> // Skip header
                    val values = line.split(',')
                    if (values.size >= SmartMeter.COLUMN_COUNT) {
                        readings.add(
                            MeterReading(
                                // The Time column is "yyyy-MM-dd HH:mm:ss"; keep only the time part.
                                time = values[SmartMeter.COLUMN_TIME].trim().substringAfter(" "),
                                unixSeconds = values[SmartMeter.COLUMN_UNIX].trim().toLong(),
                                aggregateWatts = values[SmartMeter.COLUMN_AGGREGATE].trim().toInt(),
                                tumbleDryerWatts = values[SmartMeter.COLUMN_TUMBLE_DRYER].trim().toInt(),
                                washingMachineWatts = values[SmartMeter.COLUMN_WASHING_MACHINE].trim().toInt(),
                                dishwasherWatts = values[SmartMeter.COLUMN_DISHWASHER].trim().toInt(),
                                heaterWatts = values[SmartMeter.COLUMN_HEATER].trim().toInt(),
                            ),
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not read $fileName", e)
        }
        return readings
    }

    /** Position of the first reading at or after [timeText], or 0 if there is none. */
    private fun indexForTime(readings: List<MeterReading>, timeText: String): Int {
        val index = readings.indexOfFirst { it.time >= timeText }
        return if (index >= 0) index else 0
    }

    /** The time now as text in the same format as the meter file, for example "18:42:07". */
    private fun currentTimeText(): String =
        SimpleDateFormat(SmartMeter.TIME_PATTERN, Locale.getDefault()).format(Date())

    private companion object {
        const val TAG = "SMART_METER"
    }
}
