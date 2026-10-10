package edu.monash.wattwise.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** The HTTP requests the app makes to Open-Meteo. */
interface RetrofitInterface {

    /**
     * GET forecast?latitude=..&longitude=..&current=..&daily=..&hourly=.. returns the
     * current conditions with today's high and low and the hourly sunlight strength.
     */
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentFields: String,
        @Query("daily") dailyFields: String,
        @Query("hourly") hourlyFields: String,
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int,
    ): WeatherResponse
}
