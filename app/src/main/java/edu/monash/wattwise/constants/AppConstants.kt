package edu.monash.wattwise.constants

/** Rules shared by the login and registration forms. */
object ValidationRules {
    const val MIN_PASSWORD_LENGTH = 8
    const val EMAIL_REQUIRED_SYMBOL = "@"
}

/** Options and slider limits for the household setup form. */
object HouseholdOptions {
    val HOME_TYPES = listOf("Apartment", "Townhouse", "Detached house", "Shared accommodation")
    val TARIFFS = listOf("Flat rate", "Time of use", "Solar feed-in")
    const val DEFAULT_TARIFF_INDEX = 1

    const val MIN_OCCUPANTS = 1f
    const val MAX_OCCUPANTS = 6f
    const val DEFAULT_OCCUPANTS = 2f

    const val MIN_DAILY_TARGET_KWH = 8f
    const val MAX_DAILY_TARGET_KWH = 30f
    const val DEFAULT_DAILY_TARGET_KWH = 15f
    const val DAILY_TARGET_STEP_KWH = 2f
}

/** Slider limits for the appliance power rating. */
object ApplianceLimits {
    const val MIN_WATTS = 50f
    const val MAX_WATTS = 3000f
    const val DEFAULT_WATTS = 800f
    const val WATTS_STEP = 50f
}

/** Inputs and weights used by the laundry scenario planner. */
object ScenarioRules {
    const val EARLIEST_START_HOUR = 18f
    const val LATEST_START_HOUR = 23f
    const val MIN_DRYER_DAYS = 1f
    const val MAX_DRYER_DAYS = 7f
    const val DEFAULT_DRYER_DAYS = 4f

    /** Dollars saved per month for each hour the start time moves past the peak period. */
    const val SAVING_PER_HOUR_SHIFTED = 0.8f

    /** Dollars saved per month for each day per week the dryer is not used. */
    const val SAVING_PER_DRYER_DAY_AVOIDED = 2.1f

    const val REMINDER_LEAD_MINUTES = 15
}

/** Patterns and conversion factors used by the formatting helpers. */
object FormatRules {
    const val DISPLAY_DATE_PATTERN = "EEE, dd MMM"
    const val FORM_DATE_PATTERN = "dd MMM yyyy"
    const val DAYS_PER_MONTH = 30
}

/** Settings for the Open-Meteo weather API call. */
object WeatherApi {
    const val BASE_URL = "https://api.open-meteo.com/v1/"
    const val CURRENT_FIELDS = "temperature_2m,relative_humidity_2m"

    /** Melbourne; used until the household has chosen its own location. */
    const val DEFAULT_LATITUDE = -37.8136
    const val DEFAULT_LONGITUDE = 144.9631
}
