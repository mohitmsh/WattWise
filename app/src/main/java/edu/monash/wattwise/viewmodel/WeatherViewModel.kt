package edu.monash.wattwise.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.monash.wattwise.constants.WeatherApi
import edu.monash.wattwise.data.model.MapPlace
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

    /** True when the last request for the home weather failed, so a screen can offer to try again. */
    val weatherFailed = mutableStateOf(false)

    /** Requests the forecast for the home location and puts the result in [retrofitResponse]. */
    fun getWeather(
        latitude: Double = WeatherApi.DEFAULT_LATITUDE,
        longitude: Double = WeatherApi.DEFAULT_LONGITUDE,
    ) {
        // Network calls must not run on the main thread, so use the IO dispatcher.
        viewModelScope.launch(Dispatchers.IO) {
            // Clear the old value so the screens do not show another location's weather.
            retrofitResponse.value = null
            weatherFailed.value = false
            try {
                retrofitResponse.value = repository.getWeather(latitude, longitude)
                Log.d(TAG, "Weather loaded: ${retrofitResponse.value}")
            } catch (e: HttpException) {
                weatherFailed.value = true
                Log.e(TAG, e.message ?: "Unknown HTTP error")
            } catch (e: Exception) {
                // No internet connection ends up here.
                weatherFailed.value = true
                Log.e(TAG, "Other error", e)
            }
        }
    }

    /** Current weather for each town on the weather map, keyed by the town's name. */
    val placeWeather = mutableStateOf<Map<String, WeatherResponse>>(emptyMap())

    /** True when a forecast for the map could not be loaded. */
    val placeWeatherFailed = mutableStateOf(false)

    /** Requests the weather for every place in [places], adding each result as it arrives. */
    fun getWeatherForPlaces(places: List<MapPlace>) {
        // Remove the old values first so the map never shows weather from a different point.
        placeWeather.value = placeWeather.value - places.map { it.name }.toSet()
        placeWeatherFailed.value = false
        viewModelScope.launch(Dispatchers.IO) {
            for (place in places) {
                try {
                    val response = repository.getWeather(place.latitude, place.longitude)
                    placeWeather.value = placeWeather.value + (place.name to response)
                    Log.d(TAG, "Weather loaded for ${place.name}: ${response.current}")
                } catch (e: Exception) {
                    placeWeatherFailed.value = true
                    Log.e(TAG, "Could not load weather for ${place.name}", e)
                }
            }
        }
    }

    private companion object {
        const val TAG = "WEATHER"
    }
}
