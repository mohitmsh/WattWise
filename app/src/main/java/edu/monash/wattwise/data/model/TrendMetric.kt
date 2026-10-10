package edu.monash.wattwise.data.model

/** The figures the usage report can show a trend for. Each one is worked out for a single day. */
enum class TrendMetric(val label: String, val unit: String) {
    ENERGY("Energy used", "kWh"),
    COST("Energy cost", "dollars"),
    PEAK_ENERGY("Peak-period energy", "kWh"),
    BASE_LOAD("Overnight base load", "kW"),
}
