package edu.monash.wattwise.data.model

import edu.monash.wattwise.R

/** The appliances that have their own channel in the smart-meter data, with the icon shown for each. */
enum class MeterAppliance(val label: String, val icon: Int) {
    TUMBLE_DRYER("Tumble dryer", R.drawable.tumble_dryer_icon),
    WASHING_MACHINE("Washing machine", R.drawable.washing_machine_icon),
    DISHWASHER("Dishwasher", R.drawable.dishwasher_icon),
    HEATER("Electric heater", R.drawable.heater_icon),
}
