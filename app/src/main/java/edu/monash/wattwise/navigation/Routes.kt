package edu.monash.wattwise.navigation

import edu.monash.wattwise.R

/** Route names for every screen in the navigation graph. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SETUP = "setup"
    const val EDIT_HOUSEHOLD = "edit_household"
    const val TODAY = "today"
    const val LIVE = "live"
    const val TRENDS = "trends"
    const val COACH = "coach"
    const val PROFILE = "profile"
    const val APPLIANCES = "appliances"
    const val ADD_APPLIANCE = "add_appliance"
    const val EDIT_APPLIANCE = "edit_appliance"
    const val APPLIANCE_DETAIL = "appliance_detail"
    const val GOALS = "goals"
    const val EFFICIENCY = "efficiency"
    const val MAP = "map"
}

/** The main screens reachable from the bottom navigation bar, in display order. */
enum class BottomDestination(val route: String, val label: String, val icon: Int) {
    HOME(Routes.TODAY, "Home", R.drawable.home_icon),
    MAP(Routes.MAP, "Map", R.drawable.map_icon),
    USAGE(Routes.TRENDS, "Usage", R.drawable.usage_icon),
    INSIGHTS(Routes.COACH, "Insights", R.drawable.insights_icon),
    MORE(Routes.PROFILE, "More", R.drawable.more_icon);

    companion object {
        /** True when [route] is one of the bottom bar screens. */
        fun contains(route: String?): Boolean = entries.any { it.route == route }
    }
}
