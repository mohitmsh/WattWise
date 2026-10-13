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
import edu.monash.wattwise.ui.screens.insights.BillForecastScreen
import edu.monash.wattwise.ui.screens.dashboard.EnergyMapScreen
import edu.monash.wattwise.ui.screens.profile.ProfileScreen
import edu.monash.wattwise.ui.theme.ThemeSettings
import edu.monash.wattwise.utils.ContextRules
import edu.monash.wattwise.utils.DateUtils
import edu.monash.wattwise.utils.EnergyCalculator
import edu.monash.wattwise.viewmodel.ApplianceViewModel
import edu.monash.wattwise.viewmodel.AuthViewModel
import edu.monash.wattwise.viewmodel.HouseholdViewModel
import edu.monash.wattwise.viewmodel.ReminderViewModel
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
    reminderViewModel: ReminderViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    // Household, appliances and plan belong to the signed-in user, so they are read for that user's id
    // and read again when a different user signs in.
    val userId = authViewModel.currentUser?.uid
    val appliances by remember(userId) { applianceViewModel.getAppliances(userId) }
        .collectAsState(initial = emptyList())
    val householdProfile by remember(userId) { householdViewModel.getProfile(userId) }
        .collectAsState(initial = null)
    val weather by weatherViewModel.retrofitResponse
    val placeWeather by weatherViewModel.placeWeather
    val placeWeatherFailed by weatherViewModel.placeWeatherFailed
    val reading by smartMeterViewModel.latestReading.collectAsState()
    val recentKilowatts by smartMeterViewModel.recentKilowatts.collectAsState()
    val todayKwh by smartMeterViewModel.todayKwh.collectAsState()
    val applianceRuns by smartMeterViewModel.applianceRuns.collectAsState()
    val usage by usageViewModel.allUsage.collectAsState(initial = emptyList())
    val reminder by remember(userId) { reminderViewModel.getReminder(userId) }.collectAsState(initial = null)

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
    // The profile is read once from Room here, because the new user's profile has not been collected yet.
    val openAfterSignIn: () -> Unit = {
        val signedInUserId = authViewModel.currentUser?.uid
        if (signedInUserId != null) {
            householdViewModel.checkProfileExists(signedInUserId) { hasProfile ->
                // The check may restore the reminder from the cloud backup, so set its alarm afterwards.
                reminderViewModel.restoreReminder(signedInUserId)
                navController.navigateAndClearBackStack(if (hasProfile) Routes.TODAY else Routes.SETUP)
            }
        }
    }

    // When the app opens with a user still signed in: restore their reminder, and send a user
    // who never finished the household form back to it.
    LaunchedEffect(Unit) {
        val signedInUserId = authViewModel.currentUser?.uid
        if (signedInUserId != null) {
            householdViewModel.checkProfileExists(signedInUserId) { hasProfile ->
                // Android removes alarms when an app is updated or force-stopped, so set the reminder again.
                // This runs after the check, because the check may restore the reminder from the cloud backup.
                reminderViewModel.restoreReminder(signedInUserId)
                if (!hasProfile) navController.navigateAndClearBackStack(Routes.SETUP)
            }
        }
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
            // The form is for a signed-in user; without one there is nobody to save it for.
            val signedInUserId = userId ?: return@composable
            HouseholdSetupScreen(
                userId = signedInUserId,
                onSave = { profile ->
                    householdViewModel.saveProfile(profile)
                    navController.navigateAndClearBackStack(Routes.TODAY)
                },
                // There is no earlier screen to go back to, so cancelling signs out and returns to the start.
                onCancel = {
                    authViewModel.signOut()
                    navController.navigateAndClearBackStack(Routes.ONBOARDING)
                },
            )
        }
        composable(Routes.EDIT_HOUSEHOLD) {
            // Wait for the saved profile, so the form opens with the saved values and keeps
            // what the user has typed if the screen is rotated.
            val profile = householdProfile ?: return@composable
            HouseholdSetupScreen(
                userId = profile.userId,
                existing = profile,
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
            // If an earlier weather request failed, for example while offline, ask again when Home opens.
            LaunchedEffect(Unit) {
                if (weather == null) weatherViewModel.getWeather(homePlace.latitude, homePlace.longitude)
            }
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
            val mapPlaces = (MapSettings.PLACES + homePlace).distinctBy { it.name }
            LaunchedEffect(homePlace) { weatherViewModel.getWeatherForPlaces(mapPlaces) }
            EnergyMapScreen(
                savedPlace = homePlace,
                placeWeather = placeWeather,
                tariff = tariff,
                loadFailed = placeWeatherFailed,
                onRetry = { weatherViewModel.getWeatherForPlaces(mapPlaces) },
                reminder = reminder,
                onSetReminder = { appliance, startHour ->
                    userId?.let { reminderViewModel.setReminder(it, appliance.label, startHour) }
                },
                onRemoveReminder = { userId?.let { reminderViewModel.removeReminder(it) } },
                onFindMyLocation = { onResult -> householdViewModel.findDeviceLocation(onResult) },
                onPlaceSelected = { place ->
                    // A town's forecast is already loaded; a tapped point or the device's location needs its own request.
                    if (place !in MapSettings.PLACES) weatherViewModel.getWeatherForPlaces(listOf(place))
                },
                onSave = { place -> userId?.let { householdViewModel.saveLocation(it, householdProfile, place) } },
            )
        }
        composable(Routes.TRENDS) {
            // Room holds finished hours only. Adding the energy used so far in the current hour
            // makes today's figure in the report match the one on Home.
            val usageWithLiveHour = remember(usage, todayKwh) { EnergyCalculator.withLiveHour(usage, todayKwh) }
            TrendsScreen(usageWithLiveHour, tariff, dailyTargetKwh)
        }
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
                onBill = { navController.navigate(Routes.BILL) },
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                profile = householdProfile,
                applianceCount = appliances.size,
                onEditHousehold = { navController.navigate(Routes.EDIT_HOUSEHOLD) },
                onLightThemeChange = { lightTheme ->
                    userId?.let { householdViewModel.saveTheme(it, householdProfile, lightTheme) }
                },
                onUsageAlertsChange = { enabled ->
                    userId?.let { householdViewModel.saveUsageAlerts(it, householdProfile, enabled) }
                },
                onHomeLocation = { navController.navigateToTab(Routes.MAP) },
                onAppliances = { navController.navigate(Routes.APPLIANCES) },
                onBill = { navController.navigate(Routes.BILL) },
                userName = authViewModel.currentUser?.displayName,
                userEmail = authViewModel.currentUser?.email,
                onLogout = {
                    // The reminder belongs to this user, so its alarm stops until they sign in again.
                    reminderViewModel.cancelReminderAlarm()
                    authViewModel.signOut()
                    navController.navigateAndClearBackStack(Routes.ONBOARDING)
                },
            )
        }

        // Screens opened from Home
        composable(Routes.LIVE) { LiveUsageScreen(energyContext, recentKilowatts, onBack = goBack) }

        // Appliance list, detail and form
        composable(Routes.APPLIANCES) {
            // Run the search when the screen opens, so the list is ready with the last search text.
            LaunchedEffect(userId) { applianceViewModel.search(userId, applianceViewModel.searchQuery) }
            ApplianceListScreen(
                appliances = applianceViewModel.searchResults,
                query = applianceViewModel.searchQuery,
                onQueryChange = { text -> applianceViewModel.search(userId, text) },
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
                otherNames = appliances.map { it.name },
                onCancel = goBack,
                onSave = { name, category, watts, hoursPerDay, meterChannel ->
                    userId?.let { applianceViewModel.addAppliance(it, name, category, watts, hoursPerDay, meterChannel) }
                    goBack()
                },
            )
        }
        composable(Routes.EDIT_APPLIANCE) {
            val editing = applianceViewModel.selectedAppliance
            ApplianceFormScreen(
                existing = editing,
                // Its own name is left out, so saving without renaming is not treated as a repeat.
                otherNames = appliances.filter { it.id != editing?.id }.map { it.name },
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
        composable(Routes.BILL) {
            // The forecast needs the billing date and the budget, so wait for the saved profile.
            val profile = householdProfile ?: return@composable
            BillForecastScreen(
                usage = usage,
                tariff = tariff,
                billingStartMillis = profile.billingStartDateMillis,
                savedBudget = profile.monthlyBudget,
                appliances = appliances,
                onSaveBudget = { budget -> householdViewModel.saveBudget(profile, budget) },
                onBack = goBack,
            )
        }
        composable(Routes.EFFICIENCY) { EfficiencyScreen(energyContext, onBack = goBack) }
    }
}
