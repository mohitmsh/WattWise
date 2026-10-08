package edu.monash.wattwise.data.repository

import edu.monash.wattwise.constants.WeatherApi
import edu.monash.wattwise.data.remote.RetrofitObject
import edu.monash.wattwise.data.remote.WeatherResponse

/** Layer between the ViewModel and Retrofit; hides how the weather is fetched. */
class WeatherRepository {

    private val weatherService = RetrofitObject.retrofitService

    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        return weatherService.getWeather(latitude, longitude, WeatherApi.CURRENT_FIELDS)
    }
}
