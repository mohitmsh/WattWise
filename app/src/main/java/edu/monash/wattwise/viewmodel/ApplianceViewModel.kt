package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceCategory
import edu.monash.wattwise.data.model.MeterAppliance
import edu.monash.wattwise.data.repository.ApplianceRepository
import edu.monash.wattwise.data.repository.CloudBackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Holds appliance state for the list, detail and form screens, and passes
 * add, edit and delete requests to the repository.
 */
class ApplianceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApplianceRepository(application)
    private val cloudBackup = CloudBackupRepository(application)

    /** The appliances of [userId]; an empty list when nobody is signed in. */
    fun getAppliances(userId: String?): Flow<List<Appliance>> =
        if (userId == null) flowOf(emptyList()) else repository.getAppliances(userId)

    /** The text typed in the search box of the appliance list. */
    var searchQuery by mutableStateOf("")
        private set

    /** The appliances that match [searchQuery], read from Room. */
    var searchResults by mutableStateOf<List<Appliance>>(emptyList())
        private set

    private var searchJob: Job? = null

    /**
     * Runs the search query in Room for [query]. The previous search is
     * cancelled first, so only the newest one updates [searchResults]. The
     * results also update by themselves when an appliance is added, edited or deleted.
     */
    fun search(userId: String?, query: String) {
        searchQuery = query
        searchJob?.cancel()
        if (userId == null) {
            searchResults = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            repository.searchAppliances(userId, query.trim()).collect { matches -> searchResults = matches }
        }
    }

    /** The appliance currently open in the detail or edit screen. */
    var selectedAppliance by mutableStateOf<Appliance?>(null)
        private set

    /** Remembers which appliance the user tapped, so the detail and edit screens can show it. */
    fun selectAppliance(appliance: Appliance) {
        selectedAppliance = appliance
    }

    // Database writes run on the IO dispatcher so they never block the UI thread.
    // After each write the user's data is copied to the cloud backup.
    fun addAppliance(
        userId: String,
        name: String,
        category: ApplianceCategory,
        watts: Int,
        hoursPerDay: Double,
        meterChannel: MeterAppliance?,
    ) {
        val newAppliance = Appliance(
            userId = userId,
            name = name,
            category = category,
            watts = watts,
            hoursPerDay = hoursPerDay,
            meterChannel = meterChannel,
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(newAppliance)
            cloudBackup.backup(userId)
        }
    }

    /** Saves new values for the selected appliance. */
    fun updateSelectedAppliance(
        name: String,
        category: ApplianceCategory,
        watts: Int,
        hoursPerDay: Double,
        meterChannel: MeterAppliance?,
    ) {
        val updated = selectedAppliance?.copy(
            name = name,
            category = category,
            watts = watts,
            hoursPerDay = hoursPerDay,
            meterChannel = meterChannel,
        ) ?: return
        selectedAppliance = updated
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(updated)
            cloudBackup.backup(updated.userId)
        }
    }

    /** Deletes the selected appliance. */
    fun deleteSelectedAppliance() {
        val appliance = selectedAppliance ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(appliance)
            cloudBackup.backup(appliance.userId)
        }
    }
}
