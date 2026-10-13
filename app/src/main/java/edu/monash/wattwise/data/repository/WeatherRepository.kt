package edu.monash.wattwise.data.repository

import edu.monash.wattwise.constants.WeatherApi
import edu.monash.wattwise.data.remote.RetrofitObject
import edu.monash.wattwise.data.remote.WeatherResponse

/** Layer between the ViewModel and Retrofit; hides how the weather is fetched. */
class WeatherRepository {

    private val weatherService = RetrofitObject.retrofitService

    /** Requests the forecast for one location from the Open-Meteo API. */
    suspend fun getWeather(latitude: Double, longitude: Double): WeatherResponse {
        return weatherService.getWeather(
            latitude = latitude,
            longitude = longitude,
            currentFields = WeatherApi.CURRENT_FIELDS,
            dailyFields = WeatherApi.DAILY_FIELDS,
            hourlyFields = WeatherApi.HOURLY_FIELDS,
            timezone = WeatherApi.TIMEZONE,
            forecastDays = WeatherApi.FORECAST_DAYS,
        )
    }
}
