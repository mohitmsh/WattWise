package edu.monash.wattwise.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.monash.wattwise.ui.screens.appliance.ApplianceDetailScreen
import edu.monash.wattwise.ui.screens.appliance.ApplianceFormScreen
import edu.monash.wattwise.ui.screens.appliance.ApplianceListScreen
import edu.monash.wattwise.ui.screens.auth.HouseholdSetupScreen
import edu.monash.wattwise.ui.screens.auth.LoginScreen
import edu.monash.wattwise.ui.screens.auth.OnboardingScreen
import edu.monash.wattwise.ui.screens.auth.RegisterScreen
import edu.monash.wattwise.ui.screens.dashboard.LiveUsageScreen
import edu.monash.wattwise.ui.screens.dashboard.TodayScreen
import edu.monash.wattwise.ui.screens.dashboard.TrendsScreen
import edu.monash.wattwise.ui.screens.insights.CoachScreen
import edu.monash.wattwise.ui.screens.insights.EfficiencyScreen
import edu.monash.wattwise.ui.screens.insights.EnergyMaturityScreen
import edu.monash.wattwise.ui.screens.insights.GoalsScreen
import edu.monash.wattwise.ui.screens.profile.ProfileScreen
import edu.monash.wattwise.viewmodel.ApplianceViewModel
import edu.monash.wattwise.viewmodel.HouseholdViewModel
import edu.monash.wattwise.viewmodel.WeatherViewModel

/**
 * Navigation graph: maps each route to its screen and turns screen events
 * into navigation or ViewModel calls. Screens never see the NavController or
 * the ViewModels; they receive plain data and callbacks.
 */
@Composable
fun WattWiseNavHost(
    navController: NavHostController,
    applianceViewModel: ApplianceViewModel,
    householdViewModel: HouseholdViewModel,
    weatherViewModel: WeatherViewModel,
    modifier: Modifier = Modifier,
) {
    val appliances by applianceViewModel.appliances.collectAsState(initial = emptyList())
    val householdProfile by householdViewModel.profile.collectAsState(initial = null)
    val weather by weatherViewModel.retrofitResponse
    val goBack: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.ONBOARDING, modifier = modifier) {

        // Sign-in and first-time setup
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = goBack,
                onLogin = { navController.navigateAndClearBackStack(Routes.TODAY) },
                onRegister = { navController.navigate(Routes.REGISTER) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onBack = goBack, onContinue = { navController.navigate(Routes.SETUP) })
        }
        composable(Routes.SETUP) {
            HouseholdSetupScreen(
                onSave = { profile ->
                    householdViewModel.saveProfile(profile)
                    navController.navigateAndClearBackStack(Routes.TODAY)
                },
                onCancel = goBack,
            )
        }
        composable(Routes.EDIT_HOUSEHOLD) {
            HouseholdSetupScreen(
                existing = householdProfile,
                isEditing = true,
                onSave = { profile ->
                    householdViewModel.saveProfile(profile)
                    goBack()
                },
                onCancel = goBack,
            )
        }

        // Bottom bar screens
        composable(Routes.TODAY) {
            TodayScreen(
                outsideTemperature = weather?.current?.temperatureCelsius,
                onLive = { navController.navigateToTab(Routes.LIVE) },
                onUsage = { navController.navigateToTab(Routes.TRENDS) },
                onCoach = { navController.navigateToTab(Routes.COACH) },
                onEfficiency = { navController.navigate(Routes.EFFICIENCY) },
                onAppliances = { navController.navigate(Routes.APPLIANCES) },
            )
        }
        composable(Routes.LIVE) { LiveUsageScreen() }
        composable(Routes.TRENDS) { TrendsScreen() }
        composable(Routes.COACH) {
            CoachScreen(
                onGoals = { navController.navigate(Routes.GOALS) },
                onMaturity = { navController.navigate(Routes.MATURITY) },
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                profile = householdProfile,
                applianceCount = appliances.size,
                onEditHousehold = { navController.navigate(Routes.EDIT_HOUSEHOLD) },
                onAppliances = { navController.navigate(Routes.APPLIANCES) },
                onGoals = { navController.navigate(Routes.GOALS) },
                onLogout = { navController.navigateAndClearBackStack(Routes.ONBOARDING) },
            )
        }

        // Appliance list, detail and form
        composable(Routes.APPLIANCES) {
            ApplianceListScreen(
                appliances = appliances,
                onBack = goBack,
                onAdd = { navController.navigate(Routes.ADD_APPLIANCE) },
                onSelect = { appliance ->
                    applianceViewModel.selectAppliance(appliance)
                    navController.navigate(Routes.APPLIANCE_DETAIL)
                },
            )
        }
        composable(Routes.ADD_APPLIANCE) {
            ApplianceFormScreen(
                onCancel = goBack,
                onSave = { name, category, watts ->
                    applianceViewModel.addAppliance(name, category, watts)
                    goBack()
                },
            )
        }
        composable(Routes.EDIT_APPLIANCE) {
            ApplianceFormScreen(
                existing = applianceViewModel.selectedAppliance,
                onCancel = goBack,
                onSave = { name, category, watts ->
                    applianceViewModel.updateSelectedAppliance(name, category, watts)
                    goBack()
                },
            )
        }
        composable(Routes.APPLIANCE_DETAIL) {
            // Nothing to show if the screen is restored without a selection.
            val appliance = applianceViewModel.selectedAppliance ?: return@composable
            ApplianceDetailScreen(
                appliance = appliance,
                onBack = goBack,
                onEdit = { navController.navigate(Routes.EDIT_APPLIANCE) },
                onDelete = {
                    applianceViewModel.deleteSelectedAppliance()
                    goBack()
                },
            )
        }

        // Screens opened from the tabs
        composable(Routes.GOALS) { GoalsScreen(onBack = goBack) }
        composable(Routes.EFFICIENCY) { EfficiencyScreen(onBack = goBack) }
        composable(Routes.MATURITY) { EnergyMaturityScreen(onBack = goBack) }
    }
}
