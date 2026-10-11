package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.data.model.ScenarioPlan
import edu.monash.wattwise.data.repository.ScenarioRepository
import edu.monash.wattwise.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Saves the scenario plan to Room and keeps its reminder alarm in step with it. */
class ScenarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScenarioRepository(application)
    private val reminderScheduler = ReminderScheduler(application)

    val plan: Flow<ScenarioPlan?> = repository.plan

    /** Saves the plan. If a reminder is on, it is moved to the new start time. */
    fun savePlan(startHour: Int, dryerDaysPerWeek: Int, monthlySaving: Double, reminderEnabled: Boolean) {
        val plan = ScenarioPlan(
            startHour = startHour,
            dryerDaysPerWeek = dryerDaysPerWeek,
            monthlySaving = monthlySaving,
            reminderEnabled = reminderEnabled,
        )
        viewModelScope.launch(Dispatchers.IO) { repository.save(plan) }
        if (reminderEnabled) reminderScheduler.schedule(startHour)
    }

    /** Turns the reminder for the saved [plan] on or off. */
    fun setReminder(plan: ScenarioPlan, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { repository.save(plan.copy(reminderEnabled = enabled)) }
        if (enabled) reminderScheduler.schedule(plan.startHour) else reminderScheduler.cancel()
    }

    fun deletePlan() {
        viewModelScope.launch(Dispatchers.IO) { repository.delete() }
        reminderScheduler.cancel()
    }
}
