package edu.monash.wattwise.utils

/**
 * Number of stops a Slider needs strictly between [min] and [max] so that it
 * moves in increments of [step]. For example 1 to 6 in steps of 1 needs 4.
 */
fun sliderSteps(min: Float, max: Float, step: Float): Int = ((max - min) / step).toInt() - 1
