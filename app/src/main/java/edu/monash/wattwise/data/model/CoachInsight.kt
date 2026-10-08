package edu.monash.wattwise.data.model

/** A behaviour that raises or lowers the household's Energy Score. */
data class CoachInsight(
    val title: String,
    val detail: String,
    val impactPercent: Int,
    val isPositive: Boolean,
)
