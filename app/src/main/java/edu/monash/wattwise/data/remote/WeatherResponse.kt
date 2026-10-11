package edu.monash.wattwise.data.remote

import com.google.gson.annotations.SerializedName

/** Maps the parts of the Open-Meteo JSON response that the app uses. */
data class WeatherResponse(
    val current: CurrentWeather,
)

/** @SerializedName links each JSON key to a property with a Kotlin-style name. */
data class CurrentWeather(
    @SerializedName("temperature_2m") val temperatureCelsius: Double,
    @SerializedName("relative_humidity_2m") val humidityPercent: Int,
)
