package edu.monash.wattwise.utils

import edu.monash.wattwise.constants.EnergyUnits
import edu.monash.wattwise.constants.TariffRules
import edu.monash.wattwise.constants.WeatherEnergyRules
import edu.monash.wattwise.data.remote.WeatherResponse

/** How much heating or cooling the day's weather is likely to need. */
enum class DemandLevel(val label: String) {
    LOW("Low"),
    MEDIUM("Med"),
    HIGH("High"),
}

/** Turns a weather forecast into the figures the energy map shows. */
object WeatherCalculator {

    /** Today's average temperature: halfway between the forecast high and low. */
    fun averageTemperature(weather: WeatherResponse): Double {
        val high = weather.daily?.maxTemperatures?.firstOrNull() ?: weather.current.temperatureCelsius
        val low = weather.daily?.minTemperatures?.firstOrNull() ?: weather.current.temperatureCelsius
        return (high + low) / 2
    }

    /** Degrees the day's average is below the heating base; zero when no heating is needed. */
    fun heatingDegrees(weather: WeatherResponse): Double =
        (WeatherEnergyRules.HEATING_BASE_TEMPERATURE - averageTemperature(weather)).coerceAtLeast(0.0)

    /** Degrees the day's average is above the cooling base; zero when no cooling is needed. */
    fun coolingDegrees(weather: WeatherResponse): Double =
        (averageTemperature(weather) - WeatherEnergyRules.COOLING_BASE_TEMPERATURE).coerceAtLeast(0.0)

    /** Low, medium or high demand, from whichever of heating or cooling is larger. */
    fun demandLevel(weather: WeatherResponse): DemandLevel {
        val degrees = maxOf(heatingDegrees(weather), coolingDegrees(weather))
        return when {
            degrees >= WeatherEnergyRules.HIGH_DEMAND_DEGREES -> DemandLevel.HIGH
            degrees >= WeatherEnergyRules.MEDIUM_DEMAND_DEGREES -> DemandLevel.MEDIUM
            else -> DemandLevel.LOW
        }
    }

    /** The strongest sunlight forecast for today, in watts per square metre. */
    fun peakSolar(weather: WeatherResponse): Double = weather.hourly?.solarRadiation?.maxOrNull() ?: 0.0

    /** The hour of the day (0 to 23) when sunlight is strongest. */
    fun peakSolarHour(weather: WeatherResponse): Int {
        val radiation = weather.hourly?.solarRadiation ?: return 0
        return radiation.indices.maxByOrNull { radiation[it] } ?: 0
    }

    /** The hours today with good sunlight, or null when there are none. */
    fun goodSolarHours(weather: WeatherResponse): IntRange? {
        val radiation = weather.hourly?.solarRadiation ?: return null
        val goodHours = radiation.indices.filter { radiation[it] >= WeatherEnergyRules.GOOD_SOLAR_RADIATION }
        return if (goodHours.isEmpty()) null else goodHours.first()..goodHours.last()
    }

    /**
     * Context-aware advice for the map: combines the household's tariff with
     * the sunlight forecast to suggest when to run flexible appliances.
     */
    fun bestTimeText(tariff: String, weather: WeatherResponse): String {
        val solarHours = goodSolarHours(weather)
        return when {
            tariff == TariffRules.SOLAR_FEED_IN && solarHours != null ->
                "${Formatters.formatClockHour(solarHours.first)} to " +
                    "${Formatters.formatClockHour(solarHours.last + 1)}, while your solar is strongest"
            tariff == TariffRules.SOLAR_FEED_IN -> "Any time: little sun is forecast today"
            tariff == TariffRules.TIME_OF_USE ->
                "Before ${Formatters.formatClockHour(TariffRules.PEAK_START_HOUR)} or after " +
                    "${Formatters.formatClockHour(TariffRules.PEAK_END_HOUR)}, outside the peak price"
            else -> "Any time: your flat rate is the same all day"
        }
    }

    /**
     * The hour of the day that is cheapest to start a flexible appliance, from
     * the household's tariff and the sunlight forecast:
     * - solar tariff with good sun: the sunniest hour, when the panels cover the load;
     * - time of use: the hour the peak price ends;
     * - flat rate: the sunniest hour, because the price is the same all day.
     */
    fun recommendedStartHour(tariff: String, weather: WeatherResponse): Int = when {
        tariff == TariffRules.TIME_OF_USE -> TariffRules.PEAK_END_HOUR
        else -> peakSolarHour(weather)
    }

    /** One sentence that explains [recommendedStartHour] to the user. */
    fun recommendedStartReason(tariff: String, weather: WeatherResponse): String = when {
        tariff == TariffRules.TIME_OF_USE ->
            "Peak pricing ends at ${Formatters.formatClockHour(TariffRules.PEAK_END_HOUR)} on your time of use tariff."
        tariff == TariffRules.SOLAR_FEED_IN && goodSolarHours(weather) != null ->
            "The forecast shows the strongest sun then, so your solar panels cover most of the load."
        tariff == TariffRules.SOLAR_FEED_IN ->
            "Little sun is forecast today. This is the brightest hour, but the saving will be small."
        else ->
            "Your flat rate is the same all day, so this is the sunniest hour, when washing can dry outside."
    }

    /** The highest price in dollars per kWh at any hour of the day on [tariff]. */
    fun highestRate(tariff: String): Double =
        (0 until EnergyUnits.HOURS_PER_DAY).maxOf { hour -> TariffCalculator.ratePerKwh(TariffCalculator.periodFor(tariff, hour)) }

    /** The price in dollars per kWh at [hour] on [tariff]. */
    fun rateAt(tariff: String, hour: Int): Double = TariffCalculator.ratePerKwh(TariffCalculator.periodFor(tariff, hour))
}
