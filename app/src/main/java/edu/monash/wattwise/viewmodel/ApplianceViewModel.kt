package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.data.model.Appliance
import edu.monash.wattwise.data.model.ApplianceCategory
import edu.monash.wattwise.data.model.ApplianceStatus
import edu.monash.wattwise.data.repository.ApplianceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Holds appliance state for the list, detail and form screens, and passes
 * add, edit and delete requests to the repository.
 */
class ApplianceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApplianceRepository(application)

    val appliances: Flow<List<Appliance>> = repository.allAppliances

    /** The appliance currently open in the detail or edit screen. */
    var selectedAppliance by mutableStateOf<Appliance?>(null)
        private set

    fun selectAppliance(appliance: Appliance) {
        selectedAppliance = appliance
    }

    // Database writes run on the IO dispatcher so they never block the UI thread.
    fun addAppliance(name: String, category: ApplianceCategory, watts: Int) {
        val newAppliance = Appliance(
            name = name,
            category = category,
            watts = watts,
            dailyCost = 0.0,
            status = ApplianceStatus.NEW,
        )
        viewModelScope.launch(Dispatchers.IO) { repository.insert(newAppliance) }
    }

    fun updateSelectedAppliance(name: String, category: ApplianceCategory, watts: Int) {
        val updated = selectedAppliance?.copy(name = name, category = category, watts = watts) ?: return
        selectedAppliance = updated
        viewModelScope.launch(Dispatchers.IO) { repository.update(updated) }
    }

    fun deleteSelectedAppliance() {
        val appliance = selectedAppliance ?: return
        viewModelScope.launch(Dispatchers.IO) { repository.delete(appliance) }
    }
}
