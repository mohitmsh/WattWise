package edu.monash.wattwise.constants

import edu.monash.wattwise.data.model.MapPlace

/** Rules shared by the login and registration forms. */
object ValidationRules {
    const val MIN_PASSWORD_LENGTH = 8
    /** Longest name accepted for a person or an appliance. */
    const val MAX_NAME_LENGTH = 30
    const val PASSWORD_HINT = "At least $MIN_PASSWORD_LENGTH characters, with a letter and a number"
}

/** Options and slider limits for the household setup form. */
object HouseholdOptions {
    val HOME_TYPES = listOf("Apartment", "Townhouse", "Detached house", "Shared accommodation")
    val TARIFFS = listOf("Flat rate", "Time of use", "Solar feed-in")

    // Renters cannot change the building, so they get advice about habits and settings only.
    const val RENTING = "I rent this home"
    const val OWNING = "I own this home"
    val OWNERSHIP_OPTIONS = listOf(RENTING, OWNING)
    const val DEFAULT_TARIFF_INDEX = 1

    const val MIN_OCCUPANTS = 1f
    const val MAX_OCCUPANTS = 6f
    const val DEFAULT_OCCUPANTS = 2f

    const val MIN_DAILY_TARGET_KWH = 8f
    const val MAX_DAILY_TARGET_KWH = 30f
    const val DEFAULT_DAILY_TARGET_KWH = 15f
    const val DAILY_TARGET_STEP_KWH = 1f
}

/** Slider limits for the appliance power rating. */
object ApplianceLimits {
    const val MIN_WATTS = 50f
    const val MAX_WATTS = 3000f
    const val DEFAULT_WATTS = 800f
    const val WATTS_STEP = 50f

    const val MIN_HOURS_PER_DAY = 0.5f
    const val MAX_HOURS_PER_DAY = 24f
    const val DEFAULT_HOURS_PER_DAY = 2f
    const val HOURS_STEP = 0.5f

    /** Dropdown choice for an appliance that has no meter channel. */
    const val NOT_MEASURED = "Not measured (estimate only)"

    /** An appliance using at least this share of the daily target is marked as high use. */
    const val HIGH_USE_TARGET_SHARE = 0.25
}

/** Settings for the bill forecast and the monthly budget slider. */
object BillRules {
    /** Length of one billing period in days. */
    const val PERIOD_DAYS = 30
    /** Fixed charge for being connected to the grid, in dollars for each day. */
    const val DAILY_SUPPLY_CHARGE = 1.10
    /** Number of cheapest and most expensive days used for the low and high estimates. */
    const val RANGE_DAYS = 7
    const val MIN_BUDGET = 60f
    const val MAX_BUDGET = 300f
    const val BUDGET_STEP = 10f
    const val DEFAULT_BUDGET = 150
}

/** Patterns and conversion factors used by the formatting helpers. */
object FormatRules {
    const val DISPLAY_DATE_PATTERN = "EEE, dd MMM"
    const val FORM_DATE_PATTERN = "dd MMM yyyy"
    const val WEEKDAY_PATTERN = "EEE"
    const val SHORT_DATE_PATTERN = "dd MMM"
    const val DAY_OF_MONTH_PATTERN = "d"
    const val DAYS_PER_MONTH = 30
    const val PERCENT = 100
    const val HOURS_PER_DAY = 24
    const val MINUTES_PER_HOUR = 60
}

/** Settings for the Open-Meteo weather API call. */
object WeatherApi {
    const val BASE_URL = "https://api.open-meteo.com/v1/"
    const val CURRENT_FIELDS = "temperature_2m,relative_humidity_2m"
    const val DAILY_FIELDS = "temperature_2m_max,temperature_2m_min"
    const val HOURLY_FIELDS = "shortwave_radiation"

    /** "auto" makes the hourly list start at midnight in the location's own time zone. */
    const val TIMEZONE = "auto"
    const val FORECAST_DAYS = 1

    /** Melbourne; used until the household has chosen its own location. */
    const val DEFAULT_LOCATION_NAME = "Melbourne"
    const val DEFAULT_LATITUDE = -37.8136
    const val DEFAULT_LONGITUDE = 144.9631
}

/** Settings for the smart-meter simulator and the layout of the REFIT CSV files in assets. */
object SmartMeter {
    /** One day of raw readings from REFIT House 1, about one row every seven seconds. */
    const val LIVE_FILE_NAME = "refit_house1_live.csv"

    /** Hourly energy totals for 22 days of REFIT House 1. */
    const val HOURLY_FILE_NAME = "refit_house1_hourly.csv"

    // Shortest and longest wait between two emitted readings.
    const val MIN_REFRESH_SECONDS = 1L
    const val MAX_REFRESH_SECONDS = 10L
    const val TIME_PATTERN = "HH:mm:ss"
    const val DATE_PATTERN = "yyyy-MM-dd"

    // Column positions in the live file.
    const val COLUMN_TIME = 0
    const val COLUMN_UNIX = 1
    const val COLUMN_AGGREGATE = 2
    const val COLUMN_TUMBLE_DRYER = 6
    const val COLUMN_WASHING_MACHINE = 7
    const val COLUMN_DISHWASHER = 8
    const val COLUMN_HEATER = 11
    const val COLUMN_COUNT = 13

    /** The date in the hourly file that matches the live file, used for today's hours. */
    const val LIVE_DAY = "2013-11-22"

    /** Number of readings kept for the live chart. */
    const val CHART_READING_COUNT = 60

    /** A gap longer than this between two rows is a logging outage, not real usage. */
    const val MAX_GAP_SECONDS = 60L

    /** Days of history added to Room the first time the app runs. */
    const val HISTORY_DAYS = 30

    /** The live ring is full at this power. */
    const val MAX_LIVE_KW = 4f
}

/** Unit conversions for power and energy. */
object EnergyUnits {
    const val WATTS_PER_KILOWATT = 1000.0
    const val SECONDS_PER_HOUR = 3600.0
    const val HOURS_PER_DAY = 24
    const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
}

/** Tariff periods and prices used for cost estimates and recommendations. */
object TariffRules {
    const val TIME_OF_USE = "Time of use"
    const val SOLAR_FEED_IN = "Solar feed-in"

    /** Peak pricing runs from 3 PM up to 9 PM on a time-of-use tariff. */
    const val PEAK_START_HOUR = 15
    const val PEAK_END_HOUR = 21

    /** Hours when rooftop solar normally covers most household use. */
    const val SOLAR_START_HOUR = 10
    const val SOLAR_END_HOUR = 15

    // Prices in dollars per kWh.
    const val PEAK_RATE = 0.38
    const val OFF_PEAK_RATE = 0.22
    const val FLAT_RATE = 0.30
    const val SOLAR_RATE = 0.08
}

/** Thresholds used by the context-aware recommendation rules. */
object ContextThresholds {
    /** An appliance channel above this power counts as switched on. */
    const val APPLIANCE_ON_WATTS = 200

    /** Live power this many times the usual level for the hour counts as high. */
    const val HIGH_LOAD_FACTOR = 1.5

    /** Live power below this share of the usual level for the hour counts as low. */
    const val LOW_LOAD_FACTOR = 0.8

    // Outside temperatures in degrees Celsius.
    const val COLD_TEMPERATURE = 12.0
    const val MILD_TEMPERATURE = 18.0
    const val HOT_TEMPERATURE = 28.0

    /** Efficiency score given when today's usage equals the household's usual usage. */
    const val SCORE_AT_TYPICAL = 80
    const val MAX_SCORE = 100

    /** Two "on" periods closer together than this are one run, for example a washer between cycles. */
    const val RUN_MERGE_GAP_SECONDS = 45L * 60

    /** Shorter "on" periods than this are ignored as noise. */
    const val MIN_RUN_SECONDS = 3L * 60

    /** A day up to this share above the daily target counts as close to target. */
    const val CLOSE_TO_TARGET_FACTOR = 1.2

    /** Hours used to measure the overnight base load, when little is switched on by hand. */
    val BASE_LOAD_HOURS = 1..4

}

/** Settings for the background usage summary run by WorkManager. */
object BackgroundWork {
    const val PERIODIC_WORK_NAME = "usage_summary_periodic"
    const val STARTUP_WORK_NAME = "usage_summary_startup"

    /** WorkManager does not allow periodic work more often than every 15 minutes. */
    const val REPEAT_INTERVAL_MINUTES = 15L
}

/** Settings for the app's notifications and the appliance reminder scheduled with AlarmManager. */
object Reminders {
    const val CHANNEL_ID = "energy_notifications"
    const val CHANNEL_NAME = "Reminders and usage alerts"
    const val NOTIFICATION_ID = 1001
    const val TARGET_ALERT_NOTIFICATION_ID = 1002
    const val ALARM_REQUEST_CODE = 2001
    const val EXTRA_START_HOUR = "start_hour"
    const val EXTRA_APPLIANCE = "appliance"
    /** The reminder arrives this many minutes before the planned start. */
    const val LEAD_MINUTES = 15
    /** Start hour used if an alarm arrives without one; the end of the peak period. */
    const val DEFAULT_START_HOUR = 21
    const val MINUTES_PER_HOUR = 60
}

/** Settings for the cloud backup of the household in Firestore. */
object CloudBackup {
    /** One document for each user, named with the user's Firebase id. */
    const val USERS_COLLECTION = "users"
    const val FIELD_HOUSEHOLD = "household"
    const val FIELD_APPLIANCES = "appliances"
    const val FIELD_REMINDER = "reminder"
    const val FIELD_UPDATED_AT = "updatedAt"
}

/** Settings for the home location map. */
object MapSettings {
    // Free map styles from OpenFreeMap, one for each app theme; no API key is needed.
    const val DARK_STYLE_URL = "https://tiles.openfreemap.org/styles/dark"
    const val LIGHT_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

    // Centre and zoom that show every town in the list at once.
    const val OVERVIEW_LATITUDE = -37.55
    const val OVERVIEW_LONGITUDE = 144.60
    const val OVERVIEW_ZOOM = 7.0

    const val HOME_MARKER_TITLE = "Home"

    // Size of the label drawn for each town marker.
    const val MARKER_WIDTH_DP = 54
    const val MARKER_HEIGHT_DP = 26
    const val CUSTOM_PLACE_NAME = "Chosen location"
    /** Name given to the point found from the device's own location. */
    const val MY_LOCATION_NAME = "My location"
    /** Zoom used when the map moves to the device's location; close enough to see the suburb. */
    const val MY_LOCATION_ZOOM = 10.0

    /** Places shown as markers so the user can pick a nearby town with one tap. */
    val PLACES = listOf(
        MapPlace("Melbourne", -37.8136, 144.9631),
        MapPlace("Clayton", -37.9078, 145.1340),
        MapPlace("Geelong", -38.1499, 144.3617),
        MapPlace("Ballarat", -37.5622, 143.8503),
        MapPlace("Bendigo", -36.7570, 144.2794),
        MapPlace("Frankston", -38.1414, 145.1225),
    )
}

/** Limits used to turn the weather forecast into energy advice on the map. */
object WeatherEnergyRules {
    /** Below this average temperature a home usually needs heating. */
    const val HEATING_BASE_TEMPERATURE = 18.0

    /** Above this average temperature a home usually needs cooling. */
    const val COOLING_BASE_TEMPERATURE = 24.0

    // Degrees beyond the base temperature that count as medium or high demand.
    const val MEDIUM_DEMAND_DEGREES = 2.0
    const val HIGH_DEMAND_DEGREES = 6.0

    // Sunlight strength in watts per square metre.
    const val GOOD_SOLAR_RADIATION = 300.0
    const val STRONG_SOLAR_RADIATION = 600.0
}
