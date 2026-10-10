package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import edu.monash.wattwise.data.model.HourlyUsage
import edu.monash.wattwise.data.repository.UsageHistoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Exposes the hourly usage history in Room to the report and the context rules.
 * The rows are written in the background by UsageSummaryWorker.
 */
class UsageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsageHistoryRepository(application)

    val allUsage: Flow<List<HourlyUsage>> = repository.allUsage
}
