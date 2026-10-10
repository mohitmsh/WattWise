package edu.monash.wattwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Energy used by the household during one hour of one day, in kilowatt-hours. */
@Entity(tableName = "hourly_usage")
data class HourlyUsage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** Midnight at the start of the day, in milliseconds. */
    val dayStartMillis: Long,
    val hour: Int,
    val kwh: Double,
)
