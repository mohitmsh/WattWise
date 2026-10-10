package edu.monash.wattwise.data.model

/**
 * Advice produced by one context rule.
 *
 * @param ruleName short name of the rule, shown so the user can see where the advice came from.
 * @param reason the readings and settings that made the rule apply.
 * @param isWarning true when the advice is about something to change; false for positive feedback.
 */
data class Recommendation(
    val ruleName: String,
    val title: String,
    val advice: String,
    val reason: String,
    val saving: String,
    val isWarning: Boolean,
    /** A bar that shows the numbers behind the advice; null when there is nothing to compare. */
    val visual: ComparisonVisual? = null,
)

/**
 * Data for one comparison bar: a measured value against a reference such as
 * the usual level or the daily target.
 *
 * @param fraction how far the bar is filled, from 0 to 1.
 * @param markerFraction where the reference line sits, from 0 to 1.
 */
data class ComparisonVisual(
    val label: String,
    val valueText: String,
    val fraction: Float,
    val markerLabel: String,
    val markerFraction: Float,
)
