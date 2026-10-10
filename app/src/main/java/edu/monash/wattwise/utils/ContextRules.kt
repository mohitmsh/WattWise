package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.ContextThresholds
import edu.monash.wattwise.constants.TariffRules
import edu.monash.wattwise.data.model.ComparisonVisual
import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.Recommendation
import edu.monash.wattwise.data.model.TariffPeriod
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The context-aware rules. Each rule is one "if" that checks the current
 * [EnergyContext] and, when it applies, adds a [Recommendation] with the
 * reason it applied. The rules only give advice; they never control a device.
 */
object ContextRules {

    /** Returns the recommendations that apply right now, most important first. */
    fun evaluate(context: EnergyContext): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        val reading = context.reading
        val period = context.tariffPeriod
        val temperature = context.outsideTemperature

        // The flexible appliance using the most power, if any is switched on.
        val flexibleAppliance = listOf(
            "tumble dryer" to reading.tumbleDryerWatts,
            "washing machine" to reading.washingMachineWatts,
            "dishwasher" to reading.dishwasherWatts,
        ).filter { it.second >= ContextThresholds.APPLIANCE_ON_WATTS }.maxByOrNull { it.second }

        // Rule 1: a flexible appliance is running while electricity is most expensive.
        if (period == TariffPeriod.PEAK && flexibleAppliance != null) {
            val (name, watts) = flexibleAppliance
            val extraCostPerHour = EnergyCalculator.wattsToKilowatts(watts) *
                (TariffRules.PEAK_RATE - TariffRules.OFF_PEAK_RATE)
            recommendations.add(
                Recommendation(
                    ruleName = "Peak-period appliance",
                    title = "Run the $name after ${Formatters.formatClockHour(TariffRules.PEAK_END_HOUR)}",
                    advice = "The $name is running during the peak period. Starting it after " +
                        "${Formatters.formatClockHour(TariffRules.PEAK_END_HOUR)} uses the cheaper off-peak price.",
                    reason = "Your tariff is time of use, the time is in the peak period, and the meter " +
                        "shows the $name using $watts W.",
                    saving = "${Formatters.formatCurrency(extraCostPerHour)} per hour",
                    isWarning = true,
                    visual = ComparisonVisual(
                        label = "Price now",
                        valueText = "${Formatters.formatCurrency(TariffRules.PEAK_RATE)} per kWh",
                        fraction = 1f,
                        markerLabel = "Off-peak price",
                        markerFraction = (TariffRules.OFF_PEAK_RATE / TariffRules.PEAK_RATE).toFloat(),
                    ),
                ),
            )
        }

        // Rule 2: a flexible appliance is running at a cheap time, so confirm the good habit.
        if ((period == TariffPeriod.OFF_PEAK || period == TariffPeriod.SOLAR) && flexibleAppliance != null) {
            val (name, watts) = flexibleAppliance
            recommendations.add(
                Recommendation(
                    ruleName = "Cheap-period appliance",
                    title = "Good timing for the $name",
                    advice = "The $name is running in a cheaper period. Keep using this time for " +
                        "laundry and dishwashing.",
                    reason = "Your tariff period is ${period.label.lowercase()} and the meter shows the " +
                        "$name using $watts W.",
                    saving = "Lower price",
                    isWarning = false,
                ),
            )
        }

        // Rule 3: the heater is on although it is mild outside.
        if (reading.heaterWatts >= ContextThresholds.APPLIANCE_ON_WATTS &&
            temperature != null && temperature >= ContextThresholds.MILD_TEMPERATURE
        ) {
            val advice = forOwnership(
                context,
                renter = "Turn the heater down or set its timer, and use a door draught stopper. " +
                    "None of this needs your landlord.",
                owner = "Turn the heater down, then check the thermostat schedule, door seals and " +
                    "ceiling insulation.",
            )
            recommendations.add(
                Recommendation(
                    ruleName = "Heating in mild weather",
                    title = "Heater is on while it is ${temperature.roundToInt()}°C outside",
                    advice = advice,
                    reason = "The meter shows the electric heater using ${reading.heaterWatts} W, the weather " +
                        "service reports ${temperature.roundToInt()}°C, and you ${ownershipText(context)}.",
                    saving = "${EnergyCalculator.wattsToKilowatts(reading.heaterWatts)} kW",
                    isWarning = true,
                ),
            )
        }

        // Rule 4: live power is well above what this household normally uses at this hour.
        if (context.typicalKw > 0.0 && context.liveKw > context.typicalKw * ContextThresholds.HIGH_LOAD_FACTOR) {
            val likelyCause = when {
                temperature != null && temperature <= ContextThresholds.COLD_TEMPERATURE ->
                    "It is cold outside, so heating is a likely cause."
                temperature != null && temperature >= ContextThresholds.HOT_TEMPERATURE ->
                    "It is hot outside, so cooling is a likely cause."
                else -> "Check which large appliances are on."
            }
            // First choice: a saved appliance whose meter channel shows it running right now.
            val runningAppliance = context.appliances.firstOrNull { appliance ->
                val channel = appliance.meterChannel
                channel != null && reading.wattsFor(channel) >= ContextThresholds.APPLIANCE_ON_WATTS
            }
            // Otherwise: the largest saved appliance that fits within the live load.
            val candidate = context.appliances
                .filter { it.watts <= reading.aggregateWatts }
                .maxByOrNull { it.watts }
            val candidateText = when {
                runningAppliance != null ->
                    " The meter shows your ${runningAppliance.name} running now."
                candidate != null ->
                    " From your appliance list, the ${candidate.name} (${candidate.watts} W) is large enough to explain it."
                else -> ""
            }
            val nextStep = forOwnership(
                context,
                renter = "Switch off appliances at the wall when they are not in use.",
                owner = "If this happens often, consider replacing the oldest large appliance with an efficient model.",
            )
            recommendations.add(
                Recommendation(
                    ruleName = "Above usual load",
                    title = "Usage is higher than usual for this hour",
                    advice = "$likelyCause$candidateText $nextStep",
                    reason = "Live power is ${Formatters.formatKilowatts(context.liveKw)} kW and your usual level " +
                        "at ${Formatters.formatClockHour(context.hour)} is " +
                        "${Formatters.formatKilowatts(context.typicalKw)} kW" +
                        (temperature?.let { ", with ${it.roundToInt()}°C outside" } ?: "") +
                        ". You ${ownershipText(context)}.",
                    saving = "+${context.livePercentVsTypical}%",
                    isWarning = true,
                    visual = comparison(
                        label = "Live power",
                        value = context.liveKw,
                        valueText = "${Formatters.formatKilowatts(context.liveKw)} kW",
                        reference = context.typicalKw,
                        referenceLabel = "Your usual level",
                    ),
                ),
            )
        }

        // Rule 5: today's energy has passed the daily target from the household form.
        if (context.dailyTargetKwh > 0 && context.todayKwh > context.dailyTargetKwh) {
            recommendations.add(
                Recommendation(
                    ruleName = "Daily target",
                    title = "Today has passed your daily target",
                    advice = "Delay any appliance that can wait until tomorrow, or raise the target in " +
                        "Household details if it no longer fits your home.",
                    reason = "You have used ${Formatters.formatKwh(context.todayKwh)} kWh today and your " +
                        "target is ${context.dailyTargetKwh} kWh.",
                    saving = "${(context.targetProgress * 100).toInt()}% of target",
                    isWarning = true,
                    visual = comparison(
                        label = "Used today",
                        value = context.todayKwh,
                        valueText = "${Formatters.formatKwh(context.todayKwh)} kWh",
                        reference = context.dailyTargetKwh.toDouble(),
                        referenceLabel = "Daily target",
                    ),
                ),
            )
        }

        // Rule 6: nothing flexible is running and electricity is cheap, so suggest using it now.
        if (flexibleAppliance == null && (period == TariffPeriod.OFF_PEAK || period == TariffPeriod.SOLAR)) {
            recommendations.add(
                Recommendation(
                    ruleName = "Cheap-period window",
                    title = "Good time to run laundry or the dishwasher",
                    advice = "Electricity is in a cheaper period now. If a load is waiting, this is a " +
                        "good time to start it.",
                    reason = "Your tariff period is ${period.label.lowercase()} and the meter shows no washing " +
                        "machine, dryer or dishwasher running.",
                    saving = "${Formatters.formatCurrency(TariffCalculator.ratePerKwh(period))} per kWh",
                    isWarning = false,
                ),
            )
        }

        // Rule 7: nothing above applied, so report how today compares with the usual pattern.
        if (recommendations.isEmpty()) {
            val percent = context.todayPercentVsTypical
            val comparison = if (percent <= 0) "${abs(percent)}% below" else "$percent% above"
            recommendations.add(
                Recommendation(
                    ruleName = "Daily comparison",
                    title = if (percent <= 0) "Good energy day" else "Steady energy day",
                    advice = "Your electricity use is $comparison your usual amount for this time of day.",
                    reason = "You have used ${Formatters.formatKwh(context.todayKwh)} kWh today and usually " +
                        "use ${Formatters.formatKwh(context.typicalKwhByNow)} kWh by " +
                        "${Formatters.formatClockHour(context.hour)}.",
                    saving = if (percent <= 0) "−${abs(percent)}%" else "+$percent%",
                    isWarning = false,
                    visual = comparison(
                        label = "Used today",
                        value = context.todayKwh,
                        valueText = "${Formatters.formatKwh(context.todayKwh)} kWh",
                        reference = context.typicalKwhByNow,
                        referenceLabel = "Usual by now",
                    ),
                ),
            )
        }

        // Rule 8: always finish with one step matched to the weather and to renting or owning.
        val isCold = temperature != null && temperature <= ContextThresholds.COLD_TEMPERATURE
        val isHot = temperature != null && temperature >= ContextThresholds.HOT_TEMPERATURE
        val step = when {
            isCold -> forOwnership(
                context,
                renter = "Close curtains at dusk and heat only the room you are using.",
                owner = "Ask for a quote on ceiling insulation; it keeps heat in all winter.",
            )
            isHot -> forOwnership(
                context,
                renter = "Close blinds on sunny windows and use a fan before the air conditioner.",
                owner = "Consider outside shading for sunny windows to cut cooling use.",
            )
            else -> forOwnership(
                context,
                renter = "Switch off standby devices at the wall overnight.",
                owner = "Mild days are a good time to service heating and cooling before the next season.",
            )
        }
        val weatherText = when {
            isCold -> "cold"
            isHot -> "hot"
            else -> "mild"
        }
        recommendations.add(
            Recommendation(
                ruleName = if (context.isRenting) "Step for renters" else "Step for owners",
                title = if (context.isRenting) "A low-cost step for renters" else "An upgrade step for owners",
                advice = step,
                reason = "Your household form says you ${ownershipText(context)}, and the weather service " +
                    "reports $weatherText weather" + (temperature?.let { " (${it.roundToInt()}°C)." } ?: "."),
                saving = if (context.isRenting) "No cost" else "Upgrade",
                isWarning = false,
            ),
        )

        return recommendations
    }

    /**
     * Builds the bar for a recommendation. The bar's full width is a little
     * more than the larger of the two numbers, so both always fit on it.
     */
    private fun comparison(
        label: String,
        value: Double,
        valueText: String,
        reference: Double,
        referenceLabel: String,
    ): ComparisonVisual {
        val fullWidth = (maxOf(value, reference) * BAR_HEADROOM).coerceAtLeast(MIN_BAR_WIDTH)
        return ComparisonVisual(
            label = label,
            valueText = valueText,
            fraction = (value / fullWidth).toFloat(),
            markerLabel = referenceLabel,
            markerFraction = (reference / fullWidth).toFloat(),
        )
    }

    /** Extra width beyond the largest value, so the bar never looks completely full. */
    private const val BAR_HEADROOM = 1.15
    private const val MIN_BAR_WIDTH = 0.001

    /**
     * Personalisation by ownership: renters get advice they can act on without
     * changing the building, and owners also get advice about upgrades.
     */
    private fun forOwnership(context: EnergyContext, renter: String, owner: String): String =
        if (context.isRenting) renter else owner

    private fun ownershipText(context: EnergyContext): String =
        if (context.isRenting) "rent your home" else "own your home"
}
