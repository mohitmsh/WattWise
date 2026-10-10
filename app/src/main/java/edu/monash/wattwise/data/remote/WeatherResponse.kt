package edu.monash.wattwise.data.remote

import com.google.gson.annotations.SerializedName

/** Maps the parts of the Open-Meteo JSON response that the app uses. */
data class WeatherResponse(
    val current: CurrentWeather,
    /** Today's forecast; null if the API leaves it out. */
    val daily: DailyWeather?,
    val hourly: HourlyWeather?,
)

/** @SerializedName links each JSON key to a property with a Kotlin-style name. */
data class CurrentWeather(
    @SerializedName("temperature_2m") val temperatureCelsius: Double,
    @SerializedName("relative_humidity_2m") val humidityPercent: Int,
)

/** Daily values arrive as lists with one entry per day; the app requests one day. */
data class DailyWeather(
    @SerializedName("temperature_2m_max") val maxTemperatures: List<Double>,
    @SerializedName("temperature_2m_min") val minTemperatures: List<Double>,
)

/** Hourly values arrive as a list with one entry for each hour of the day. */
data class HourlyWeather(
    /** Strength of sunlight reaching the ground, in watts per square metre. */
    @SerializedName("shortwave_radiation") val solarRadiation: List<Double>,
)
