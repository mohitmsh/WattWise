package edu.monash.wattwise.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.monash.wattwise.constants.HouseholdOptions
import edu.monash.wattwise.constants.MapSettings
import edu.monash.wattwise.constants.WeatherApi
import edu.monash.wattwise.data.model.EnergyContext
import edu.monash.wattwise.data.model.MapPlace
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
import edu.monash.wattwise.ui.screens.insights.GoalsScreen
import edu.monash.wattwise.ui.screens.dashboard.EnergyMapScreen
import edu.monash.wattwise.ui.screens.profile.ProfileScreen
import edu.monash.wattwise.ui.theme.ThemeSettings
import edu.monash.wattwise.utils.ContextRules
import edu.monash.wattwise.utils.DateUtils
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.viewmodel.ApplianceViewModel
import edu.monash.wattwise.viewmodel.AuthViewModel
import edu.monash.wattwise.viewmodel.HouseholdViewModel
import edu.monash.wattwise.viewmodel.ScenarioViewModel
import edu.monash.wattwise.viewmodel.SmartMeterViewModel
import edu.monash.wattwise.viewmodel.UsageViewModel
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
    smartMeterViewModel: SmartMeterViewModel,
    usageViewModel: UsageViewModel,
    scenarioViewModel: ScenarioViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val appliances by applianceViewModel.appliances.collectAsState(initial = emptyList())
    val householdProfile by householdViewModel.profile.collectAsState(initial = null)
    val weather by weatherViewModel.retrofitResponse
    val placeWeather by weatherViewModel.placeWeather
    val reading by smartMeterViewModel.latestReading.collectAsState()
    val recentKilowatts by smartMeterViewModel.recentKilowatts.collectAsState()
    val todayKwh by smartMeterViewModel.todayKwh.collectAsState()
    val applianceRuns by smartMeterViewModel.applianceRuns.collectAsState()
    val usage by usageViewModel.allUsage.collectAsState(initial = emptyList())
    val scenarioPlan by scenarioViewModel.plan.collectAsState(initial = null)

    // The home location saved from the map, or Melbourne until one is chosen.
    val homePlace = MapPlace(
        name = householdProfile?.locationName ?: WeatherApi.DEFAULT_LOCATION_NAME,
        latitude = householdProfile?.latitude ?: WeatherApi.DEFAULT_LATITUDE,
        longitude = householdProfile?.longitude ?: WeatherApi.DEFAULT_LONGITUDE,
    )
    // Apply the theme saved in Room when the app opens and whenever it is changed.
    LaunchedEffect(householdProfile?.lightTheme) {
        ThemeSettings.isLight = householdProfile?.lightTheme ?: false
    }
    // Request the weather when the app opens and again whenever the home location changes.
    LaunchedEffect(homePlace) {
        weatherViewModel.getWeather(homePlace.latitude, homePlace.longitude)
    }

    val outsideTemperature = weather?.current?.temperatureCelsius
    val tariff = householdProfile?.tariff ?: HouseholdOptions.TARIFFS[HouseholdOptions.DEFAULT_TARIFF_INDEX]
    val dailyTargetKwh = householdProfile?.dailyTargetKwh ?: HouseholdOptions.DEFAULT_DAILY_TARGET_KWH.toInt()

    // Context-aware step: combine the newest meter reading (sensory data) with the
    // history, tariff, household form and weather (non-sensory data), then run the rules.
    val energyContext = reading?.let { latest ->
        EnergyContext(
            reading = latest,
            hour = latest.hour,
            todayKwh = todayKwh,
            typicalKw = remember(usage, latest.hour) { EnergyCalculator.typicalForHour(usage, latest.hour) },
            typicalKwhByNow = remember(usage, latest.hour) { EnergyCalculator.typicalUntilHour(usage, latest.hour) },
            dailyTargetKwh = dailyTargetKwh,
            tariff = tariff,
            isRenting = householdProfile?.isRenting ?: true,
            outsideTemperature = outsideTemperature,
            appliances = appliances,
        )
    }
    val recommendations = energyContext?.let { ContextRules.evaluate(it) } ?: emptyList()
    val goBack: () -> Unit = { navController.popBackStack() }

    // A user who is still signed in from last time goes straight to Home.
    val startDestination = remember { if (authViewModel.currentUser != null) Routes.TODAY else Routes.ONBOARDING }
    // After signing in, a user with no saved household is sent to the household form first.
    val openAfterSignIn: () -> Unit = {
        navController.navigateAndClearBackStack(if (householdProfile != null) Routes.TODAY else Routes.SETUP)
    }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        // Sign-in and first-time setup
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                isLoading = authViewModel.isLoading,
                errorMessage = authViewModel.errorMessage,
                infoMessage = authViewModel.infoMessage,
                onBack = goBack,
                onLogin = { email, password -> authViewModel.signIn(email, password, openAfterSignIn) },
                onGoogleIdToken = { idToken -> authViewModel.signInWithGoogle(idToken, openAfterSignIn) },
                onGoogleError = { message -> authViewModel.showError(message) },
                onForgotPassword = { email -> authViewModel.sendPasswordReset(email) },
                onRegister = {
                    authViewModel.clearMessages()
                    navController.navigate(Routes.REGISTER)
                },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                isLoading = authViewModel.isLoading,
                errorMessage = authViewModel.errorMessage,
                onBack = goBack,
                onRegister = { name, email, password ->
                    authViewModel.register(name, email, password, openAfterSignIn)
                },
                onGoogleIdToken = { idToken -> authViewModel.signInWithGoogle(idToken, openAfterSignIn) },
                onGoogleError = { message -> authViewModel.showError(message) },
            )
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
                outsideTemperature = outsideTemperature,
                energyContext = energyContext,
                applianceRuns = applianceRuns,
                topRecommendation = recommendations.firstOrNull(),
                onWeatherMap = { navController.navigateToTab(Routes.MAP) },
                onLive = { navController.navigate(Routes.LIVE) },
                onUsage = { navController.navigateToTab(Routes.TRENDS) },
                onCoach = { navController.navigateToTab(Routes.COACH) },
                onEfficiency = { navController.navigate(Routes.EFFICIENCY) },
                onAppliances = { navController.navigate(Routes.APPLIANCES) },
            )
        }
        composable(Routes.MAP) {
            // Load the forecast for every town, and for the home if it is not one of them.
            LaunchedEffect(homePlace) {
                weatherViewModel.getWeatherForPlaces((MapSettings.PLACES + homePlace).distinctBy { it.name })
            }
            EnergyMapScreen(
                savedPlace = homePlace,
                placeWeather = placeWeather,
                tariff = tariff,
                onPlaceSelected = { place ->
                    // A town's forecast is already loaded; a tapped point needs its own request.
                    if (place.name == MapSettings.CUSTOM_PLACE_NAME) weatherViewModel.getWeatherForPlaces(listOf(place))
                },
                onSave = { place -> householdViewModel.saveLocation(householdProfile, place) },
            )
        }
        composable(Routes.TRENDS) { TrendsScreen(usage, tariff, dailyTargetKwh) }
        composable(Routes.COACH) {
            CoachScreen(
                recommendations = recommendations,
                energyContext = energyContext,
                outsideTemperature = outsideTemperature,
                // Only the hours that have finished, so the line stops at now and does not drop to zero.
                todayHourlyKwh = EnergyCalculator.hourlyForDay(usage, DateUtils.startOfDay(System.currentTimeMillis()))
                    .take(DateUtils.hourOfDay(System.currentTimeMillis())),
                typicalHourlyKwh = (0 until 24).map { hour -> EnergyCalculator.typicalForHour(usage, hour).toFloat() },
                appliances = appliances,
                applianceRuns = applianceRuns,
                onEfficiency = { navController.navigate(Routes.EFFICIENCY) },
                onGoals = { navController.navigate(Routes.GOALS) },
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                profile = householdProfile,
                applianceCount = appliances.size,
                onEditHousehold = { navController.navigate(Routes.EDIT_HOUSEHOLD) },
                onLightThemeChange = { lightTheme -> householdViewModel.saveTheme(householdProfile, lightTheme) },
                onUsageAlertsChange = { enabled -> householdViewModel.saveUsageAlerts(householdProfile, enabled) },
                onHomeLocation = { navController.navigateToTab(Routes.MAP) },
                onAppliances = { navController.navigate(Routes.APPLIANCES) },
                onGoals = { navController.navigate(Routes.GOALS) },
                userName = authViewModel.currentUser?.displayName,
                userEmail = authViewModel.currentUser?.email,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigateAndClearBackStack(Routes.ONBOARDING)
                },
            )
        }

        // Screens opened from Home
        composable(Routes.LIVE) { LiveUsageScreen(energyContext, recentKilowatts, onBack = goBack) }

        // Appliance list, detail and form
        composable(Routes.APPLIANCES) {
            ApplianceListScreen(
                appliances = appliances,
                tariff = tariff,
                dailyTargetKwh = dailyTargetKwh,
                applianceRuns = applianceRuns,
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
                onSave = { name, category, watts, hoursPerDay, meterChannel ->
                    applianceViewModel.addAppliance(name, category, watts, hoursPerDay, meterChannel)
                    goBack()
                },
            )
        }
        composable(Routes.EDIT_APPLIANCE) {
            ApplianceFormScreen(
                existing = applianceViewModel.selectedAppliance,
                onCancel = goBack,
                onSave = { name, category, watts, hoursPerDay, meterChannel ->
                    applianceViewModel.updateSelectedAppliance(name, category, watts, hoursPerDay, meterChannel)
                    goBack()
                },
            )
        }
        composable(Routes.APPLIANCE_DETAIL) {
            // Nothing to show if the screen is restored without a selection.
            val appliance = applianceViewModel.selectedAppliance ?: return@composable
            ApplianceDetailScreen(
                appliance = appliance,
                tariff = tariff,
                dailyTargetKwh = dailyTargetKwh,
                applianceRuns = applianceRuns,
                latestReading = reading,
                onBack = goBack,
                onEdit = { navController.navigate(Routes.EDIT_APPLIANCE) },
                onDelete = {
                    applianceViewModel.deleteSelectedAppliance()
                    goBack()
                },
            )
        }

        // Screens opened from the tabs
        composable(Routes.GOALS) {
            GoalsScreen(
                plan = scenarioPlan,
                energyContext = energyContext,
                tariff = tariff,
                onSavePlan = { startHour, dryerDays, monthlySaving ->
                    val reminderEnabled = scenarioPlan?.reminderEnabled ?: false
                    scenarioViewModel.savePlan(startHour, dryerDays, monthlySaving, reminderEnabled)
                },
                onSetReminder = { enabled -> scenarioPlan?.let { scenarioViewModel.setReminder(it, enabled) } },
                onDeletePlan = { scenarioViewModel.deletePlan() },
                onBack = goBack,
            )
        }
        composable(Routes.EFFICIENCY) { EfficiencyScreen(energyContext, onBack = goBack) }
    }
}
