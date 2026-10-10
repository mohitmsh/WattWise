package edu.monash.wattwise.data.model

/** Time ranges the usage report can be customised by, with the number of days each one covers. */
enum class UsagePeriod(val label: String, val dayCount: Int) {
    DAY("DAY", 1),
    WEEK("WEEK", 7),
    MONTH("MONTH", 30),
}
