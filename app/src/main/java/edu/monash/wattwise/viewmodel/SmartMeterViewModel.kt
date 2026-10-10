package edu.monash.wattwise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.constants.SmartMeter
import edu.monash.wattwise.data.model.ApplianceRun
import edu.monash.wattwise.data.model.MeterReading
import edu.monash.wattwise.data.repository.SmartMeterRepository
import edu.monash.wattwise.data.sensor.SmartMeterDataProvider
import edu.monash.wattwise.utils.EnergyCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Collects readings from the smart-meter simulator and exposes them to the screens. */
class SmartMeterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmartMeterRepository(SmartMeterDataProvider(application))

    /** The newest reading; null until the simulator has emitted its first value. */
    private val _latestReading = MutableStateFlow<MeterReading?>(null)
    val latestReading: StateFlow<MeterReading?> = _latestReading.asStateFlow()

    /** Power in kW for the most recent readings, oldest first, for the live chart. */
    private val _recentKilowatts = MutableStateFlow<List<Float>>(emptyList())
    val recentKilowatts: StateFlow<List<Float>> = _recentKilowatts.asStateFlow()

    /** Energy in kWh used so far in the simulated day. */
    private val _todayKwh = MutableStateFlow(0.0)
    val todayKwh: StateFlow<Double> = _todayKwh.asStateFlow()

    /** Appliance runs found in today's readings so far, newest first. */
    private val _applianceRuns = MutableStateFlow<List<ApplianceRun>>(emptyList())
    val applianceRuns: StateFlow<List<ApplianceRun>> = _applianceRuns.asStateFlow()

    init {
        // Reading the file must not block the UI, so collect on the IO dispatcher.
        viewModelScope.launch(Dispatchers.IO) {
            var previous: MeterReading? = null
            // collect starts the flow and runs this block for every reading it emits.
            repository.getSensorData().collect { reading ->
                val last = previous
                if (last == null || reading.unixSeconds < last.unixSeconds) {
                    // First reading, or the simulated day has started again from midnight.
                    _todayKwh.value = EnergyCalculator.energyUsedBefore(repository.getDayReadings(), reading)
                } else {
                    _todayKwh.value += EnergyCalculator.energyBetween(last, reading)
                }
                previous = reading

                val kilowatts = EnergyCalculator.wattsToKilowatts(reading.aggregateWatts).toFloat()
                _recentKilowatts.value = (_recentKilowatts.value + kilowatts).takeLast(SmartMeter.CHART_READING_COUNT)
                _applianceRuns.value = EnergyCalculator.applianceRuns(repository.getDayReadings(), reading)
                _latestReading.value = reading
            }
        }
    }
}
