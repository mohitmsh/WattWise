package edu.monash.wattwise.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import edu.monash.wattwise.constants.CloudBackup
import edu.monash.wattwise.data.local.WattWiseDatabase
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceCategory
import edu.monash.wattwise.data.model.ApplianceReminder
import edu.monash.wattwise.data.model.HouseholdProfile
import edu.monash.wattwise.data.model.MeterAppliance
import kotlinx.coroutines.tasks.await

/**
 * Keeps a copy of the user's household, appliances and reminder in Cloud
 * Firestore. Room is still the database the screens read. The copy in
 * Firestore means the data comes back after the app is installed again or
 * when the user signs in on another device.
 *
 * Each user has one document, users/{userId}, and the security rules let a
 * user read and write only their own document.
 */
class CloudBackupRepository(context: Context) {

    private val database = WattWiseDatabase.getDatabase(context)
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Copies everything the user has in Room to their Firestore document.
     * The ViewModels call this after each change. If the device is offline,
     * Firestore keeps the write and sends it when the connection returns.
     */
    suspend fun backup(userId: String) {
        val profile = database.householdProfileDao().getProfileOnce(userId) ?: return
        val appliances = database.applianceDao().getAppliancesOnce(userId)
        val reminder = database.applianceReminderDao().getReminderOnce(userId)

        val document = mapOf(
            CloudBackup.FIELD_HOUSEHOLD to householdToMap(profile),
            CloudBackup.FIELD_APPLIANCES to appliances.map { applianceToMap(it) },
            CloudBackup.FIELD_REMINDER to reminder?.let { reminderToMap(it) },
            CloudBackup.FIELD_UPDATED_AT to System.currentTimeMillis(),
        )
        firestore.collection(CloudBackup.USERS_COLLECTION).document(userId).set(document)
            .addOnSuccessListener { Log.d(TAG, "Backup saved to Firestore: ${appliances.size} appliances") }
            .addOnFailureListener { error -> Log.e(TAG, "Backup failed", error) }
    }

    /**
     * Reads the user's Firestore document and copies it into Room. Returns
     * true when a household was restored, and false when the user has no
     * backup or it could not be read, for example with no internet connection.
     */
    suspend fun restore(userId: String): Boolean {
        return try {
            val snapshot = firestore.collection(CloudBackup.USERS_COLLECTION).document(userId).get().await()
            val household = snapshot.get(CloudBackup.FIELD_HOUSEHOLD) as? Map<*, *>
            if (household == null) {
                Log.d(TAG, "No backup in Firestore for this user")
                return false
            }
            database.householdProfileDao().saveProfile(householdFromMap(userId, household))

            val appliances = (snapshot.get(CloudBackup.FIELD_APPLIANCES) as? List<*>).orEmpty()
                .filterIsInstance<Map<*, *>>()
                .map { applianceFromMap(userId, it) }
            if (appliances.isNotEmpty()) database.applianceDao().insertAll(appliances)

            val reminder = snapshot.get(CloudBackup.FIELD_REMINDER) as? Map<*, *>
            if (reminder != null) database.applianceReminderDao().saveReminder(reminderFromMap(userId, reminder))

            Log.d(TAG, "Backup restored from Firestore: household and ${appliances.size} appliances")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Backup could not be read", e)
            false
        }
    }

    // Firestore stores a document as a map of field names to values, so each
    // Room entity is turned into a map to save it and built again from a map to restore it.

    /** Turns the household into the map that Firestore saves. */
    private fun householdToMap(profile: HouseholdProfile): Map<String, Any> = mapOf(
        "homeType" to profile.homeType,
        "tariff" to profile.tariff,
        "occupants" to profile.occupants,
        "dailyTargetKwh" to profile.dailyTargetKwh,
        "billingStartDateMillis" to profile.billingStartDateMillis,
        "monthlyBudget" to profile.monthlyBudget,
        "isRenting" to profile.isRenting,
        "usageAlerts" to profile.usageAlerts,
        "lightTheme" to profile.lightTheme,
        "locationName" to profile.locationName,
        "latitude" to profile.latitude,
        "longitude" to profile.longitude,
    )

    /** Builds the household again from a Firestore map. */
    private fun householdFromMap(userId: String, map: Map<*, *>): HouseholdProfile {
        // A new profile supplies the default for any field that is missing from an older backup.
        val defaults = HouseholdProfile(
            userId = userId,
            homeType = map["homeType"] as? String ?: "",
            tariff = map["tariff"] as? String ?: "",
            occupants = (map["occupants"] as? Number)?.toInt() ?: 1,
            dailyTargetKwh = (map["dailyTargetKwh"] as? Number)?.toInt() ?: 0,
            billingStartDateMillis = (map["billingStartDateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        )
        // Firestore returns every number as a Long or a Double, so each one is converted.
        return defaults.copy(
            monthlyBudget = (map["monthlyBudget"] as? Number)?.toInt() ?: defaults.monthlyBudget,
            isRenting = map["isRenting"] as? Boolean ?: defaults.isRenting,
            usageAlerts = map["usageAlerts"] as? Boolean ?: defaults.usageAlerts,
            lightTheme = map["lightTheme"] as? Boolean ?: defaults.lightTheme,
            locationName = map["locationName"] as? String ?: defaults.locationName,
            latitude = (map["latitude"] as? Number)?.toDouble() ?: defaults.latitude,
            longitude = (map["longitude"] as? Number)?.toDouble() ?: defaults.longitude,
        )
    }

    /** Turns one appliance into the map that Firestore saves. */
    private fun applianceToMap(appliance: Appliance): Map<String, Any?> = mapOf(
        "name" to appliance.name,
        "category" to appliance.category.name,
        "watts" to appliance.watts,
        "hoursPerDay" to appliance.hoursPerDay,
        "meterChannel" to appliance.meterChannel?.name,
    )

    /** Builds one appliance again from a Firestore map. */
    private fun applianceFromMap(userId: String, map: Map<*, *>): Appliance = Appliance(
        userId = userId,
        name = map["name"] as? String ?: "",
        // An enum is saved by its name; an unknown name falls back to the first category.
        category = ApplianceCategory.entries.firstOrNull { it.name == map["category"] } ?: ApplianceCategory.entries.first(),
        watts = (map["watts"] as? Number)?.toInt() ?: 0,
        hoursPerDay = (map["hoursPerDay"] as? Number)?.toDouble() ?: 0.0,
        meterChannel = MeterAppliance.entries.firstOrNull { it.name == map["meterChannel"] },
    )

    /** Turns the reminder into the map that Firestore saves. */
    private fun reminderToMap(reminder: ApplianceReminder): Map<String, Any> = mapOf(
        "applianceLabel" to reminder.applianceLabel,
        "startHour" to reminder.startHour,
    )

    /** Builds the reminder again from a Firestore map. */
    private fun reminderFromMap(userId: String, map: Map<*, *>): ApplianceReminder = ApplianceReminder(
        userId = userId,
        applianceLabel = map["applianceLabel"] as? String ?: "",
        startHour = (map["startHour"] as? Number)?.toInt() ?: 0,
    )

    private companion object {
        const val TAG = "CLOUD_BACKUP"
    }
}
