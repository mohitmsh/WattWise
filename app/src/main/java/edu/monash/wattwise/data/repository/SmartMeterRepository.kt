package edu.monash.wattwise.data.repository

import edu.monash.wattwise.data.model.MeterReading
import edu.monash.wattwise.data.sensor.SmartMeterDataProvider
import kotlinx.coroutines.flow.Flow

/** Single access point for smart-meter readings, so the ViewModel never touches the simulator directly. */
class SmartMeterRepository(private val dataProvider: SmartMeterDataProvider) {

    /** The meter readings as a Flow. A new reading arrives every few seconds. */
    fun getSensorData(): Flow<MeterReading> {
        return dataProvider.sensorDataFlow()
    }

    /** The whole simulated day, used to work out the energy used before the app was opened. */
    fun getDayReadings(): List<MeterReading> {
        return dataProvider.dayReadings
    }
}
