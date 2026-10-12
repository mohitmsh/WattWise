package edu.monash.wattwise.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceReminder
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.model.HouseholdProfile

/** The app's Room database. Increase [version] whenever an entity changes. */
@Database(
    entities = [Appliance::class, HouseholdProfile::class, HourlyUsage::class, ApplianceReminder::class],
    version = 9,
    exportSchema = false,
)
abstract class WattWiseDatabase : RoomDatabase() {

    /** One function for each table gives the DAO that reads and writes it. */
    abstract fun applianceDao(): ApplianceDao
    abstract fun householdProfileDao(): HouseholdProfileDao
    abstract fun hourlyUsageDao(): HourlyUsageDao
    abstract fun applianceReminderDao(): ApplianceReminderDao

    companion object {
        private const val DATABASE_NAME = "wattwise_database"

        // @Volatile makes a newly created instance visible to all threads immediately.
        @Volatile
        private var INSTANCE: WattWiseDatabase? = null

        /** Returns the single database instance, creating it on first use. */
        fun getDatabase(context: Context): WattWiseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WattWiseDatabase::class.java,
                    DATABASE_NAME,
                )
                    // If the version changes without a migration, start again with empty tables.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
