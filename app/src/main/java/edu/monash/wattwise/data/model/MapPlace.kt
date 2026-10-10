package edu.monash.wattwise.data.model

/** A named point that can be shown as a marker on the map. */
data class MapPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)
