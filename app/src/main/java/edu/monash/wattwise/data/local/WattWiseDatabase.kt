package edu.monash.wattwise.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.model.HouseholdProfile
import edu.monash.wattwise.data.model.ScenarioPlan

/** The app's Room database. Increase [version] whenever an entity changes. */
@Database(
    entities = [Appliance::class, HouseholdProfile::class, HourlyUsage::class, ScenarioPlan::class],
    version = 7,
    exportSchema = false,
)
abstract class WattWiseDatabase : RoomDatabase() {

    abstract fun applianceDao(): ApplianceDao
    abstract fun householdProfileDao(): HouseholdProfileDao
    abstract fun hourlyUsageDao(): HourlyUsageDao
    abstract fun scenarioPlanDao(): ScenarioPlanDao

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
