package edu.monash.wattwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.monash.wattwise.navigation.BottomDestination
import edu.monash.wattwise.navigation.WattWiseBottomBar
import edu.monash.wattwise.navigation.WattWiseNavHost
import edu.monash.wattwise.navigation.navigateToTab
import edu.monash.wattwise.ui.theme.GradientBottom
import edu.monash.wattwise.ui.theme.GradientTop
import edu.monash.wattwise.ui.theme.Ink
import edu.monash.wattwise.ui.theme.TextPrimary
import edu.monash.wattwise.viewmodel.ApplianceViewModel
import edu.monash.wattwise.viewmodel.AuthViewModel
import edu.monash.wattwise.viewmodel.HouseholdViewModel
import edu.monash.wattwise.viewmodel.ScenarioViewModel
import edu.monash.wattwise.viewmodel.SmartMeterViewModel
import edu.monash.wattwise.viewmodel.UsageViewModel
import edu.monash.wattwise.viewmodel.WeatherViewModel

/**
 * Root composable: lays out the bottom bar and the navigation graph. The
 * bottom bar is shown only on the main screens, not on sign-in or detail screens.
 */
@Composable
fun WattWiseApp(
    applianceViewModel: ApplianceViewModel,
    householdViewModel: HouseholdViewModel,
    weatherViewModel: WeatherViewModel,
    smartMeterViewModel: SmartMeterViewModel,
    usageViewModel: UsageViewModel,
    scenarioViewModel: ScenarioViewModel,
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = TextPrimary,
        bottomBar = {
            if (BottomDestination.contains(currentRoute)) {
                WattWiseBottomBar(currentRoute, onNavigate = navController::navigateToTab)
            }
        },
    ) { innerPadding ->
        WattWiseNavHost(
            navController = navController,
            applianceViewModel = applianceViewModel,
            householdViewModel = householdViewModel,
            weatherViewModel = weatherViewModel,
            smartMeterViewModel = smartMeterViewModel,
            usageViewModel = usageViewModel,
            scenarioViewModel = scenarioViewModel,
            authViewModel = authViewModel,
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GradientTop, Ink, GradientBottom)))
                .padding(innerPadding),
        )
    }
}
