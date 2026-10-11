package edu.monash.wattwise.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** The HTTP requests the app makes to Open-Meteo. */
interface RetrofitInterface {

    /** GET forecast?latitude=..&longitude=..&current=.. returns the current conditions. */
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentFields: String,
    ): WeatherResponse
}
