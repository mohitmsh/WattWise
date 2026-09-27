package edu.monash.wattwise.ui.model

/** UI-facing model. A Room entity can replace it in Assessment 4. */
data class Appliance(
    val id: Int,
    val name: String,
    val category: String,
    val watts: Int,
    val cost: String,
    val status: String,
)

val sampleAppliances = listOf(
    Appliance(1, "Air conditioner", "Cooling", 2400, "$1.84 today", "High"),
    Appliance(2, "Refrigerator", "Kitchen", 180, "$0.42 today", "Normal"),
    Appliance(3, "Dishwasher", "Kitchen", 1500, "$0.36 today", "Scheduled"),
    Appliance(4, "Washing machine", "Laundry", 900, "$0.28 today", "Normal"),
    Appliance(5, "Home office", "Electronics", 320, "$0.31 today", "Normal"),
)
