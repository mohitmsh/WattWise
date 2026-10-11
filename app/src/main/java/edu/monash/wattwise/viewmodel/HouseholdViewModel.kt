package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.data.model.HouseholdProfile
import edu.monash.wattwise.data.repository.HouseholdRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Exposes the saved household profile and saves changes from the setup form. */
class HouseholdViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HouseholdRepository(application)

    val profile: Flow<HouseholdProfile?> = repository.profile

    fun saveProfile(profile: HouseholdProfile) {
        viewModelScope.launch(Dispatchers.IO) { repository.save(profile) }
    }
}
