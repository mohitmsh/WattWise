package edu.monash.wattwise.data.model

/** The pricing period that applies to an hour of the day. */
enum class TariffPeriod(val label: String) {
    PEAK("Peak"),
    OFF_PEAK("Off-peak"),
    SOLAR("Solar hours"),
    FLAT("Flat rate"),
}
