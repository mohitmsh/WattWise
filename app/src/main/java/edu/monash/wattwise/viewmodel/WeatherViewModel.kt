package edu.monash.wattwise.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.constants.WeatherApi
import edu.monash.wattwise.data.remote.WeatherResponse
import edu.monash.wattwise.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

/** Fetches the current weather and exposes it to the screens as Compose state. */
class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    /** Null until the first successful response, or if the request fails. */
    val retrofitResponse = mutableStateOf<WeatherResponse?>(null)

    // Fetch once when the ViewModel is created so the home screen has data straight away.
    init {
        getWeather()
    }

    fun getWeather(
        latitude: Double = WeatherApi.DEFAULT_LATITUDE,
        longitude: Double = WeatherApi.DEFAULT_LONGITUDE,
    ) {
        // Network calls must not run on the main thread, so use the IO dispatcher.
        viewModelScope.launch(Dispatchers.IO) {
            try {
                retrofitResponse.value = repository.getWeather(latitude, longitude)
                Log.d(TAG, "Weather loaded: ${retrofitResponse.value}")
            } catch (e: HttpException) {
                Log.e(TAG, e.message ?: "Unknown HTTP error")
            } catch (e: Exception) {
                Log.e(TAG, "Other error", e)
            }
        }
    }

    private companion object {
        const val TAG = "WEATHER"
    }
}
