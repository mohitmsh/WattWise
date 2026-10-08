package edu.monash.wattwise.data.model

/** Time ranges the usage report can be customised by. */
enum class UsagePeriod(val label: String) {
    DAY("DAY"),
    WEEK("WEEK"),
    MONTH("MONTH"),
}
