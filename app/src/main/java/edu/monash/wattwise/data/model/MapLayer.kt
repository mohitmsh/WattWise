package edu.monash.wattwise.data.model

/** What the markers on the energy map show. */
enum class MapLayer(val label: String) {
    TEMPERATURE("Temperature"),
    DEMAND("Heating & cooling"),
    SOLAR("Solar"),
}
