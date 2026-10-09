package edu.monash.wattwise.navigation

import androidx.navigation.NavController

/**
 * Switches bottom bar tabs. Home is the root of the signed-in screens, so
 * selecting it pops back to the existing Home entry; other tabs are opened
 * above Home with their state saved and restored.
 */
fun NavController.navigateToTab(route: String) {
    if (route == Routes.TODAY) {
        val returnedHome = popBackStack(Routes.TODAY, inclusive = false)
        if (!returnedHome) navigate(Routes.TODAY) { launchSingleTop = true }
        return
    }
    navigate(route) {
        popUpTo(Routes.TODAY) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Opens [route] and removes every earlier screen, so Back cannot return to them. */
fun NavController.navigateAndClearBackStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
    }
}
