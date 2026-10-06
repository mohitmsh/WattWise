package edu.monash.wattwise.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.monash.wattwise.ui.model.Appliance
import edu.monash.wattwise.ui.model.sampleAppliances
import edu.monash.wattwise.ui.screens.*
import edu.monash.wattwise.ui.theme.*

private object Route {
    const val Onboarding = "onboarding"
    const val Login = "login"
    const val Register = "register"
    const val Setup = "setup"
    const val Today = "today"
    const val Live = "live"
    const val Trends = "trends"
    const val Coach = "coach"
    const val Profile = "profile"
    const val Appliances = "appliances"
    const val AddAppliance = "add_appliance"
    const val EditAppliance = "edit_appliance"
    const val ApplianceDetail = "appliance_detail"
    const val Goals = "goals"
    const val Efficiency = "efficiency"
    const val Maturity = "maturity"
}

private data class BottomDestination(val route: String, val label: String, val glyph: String)

private val bottomDestinations = listOf(
    BottomDestination(Route.Today, "Home", "home"),
    BottomDestination(Route.Live, "Live", "live"),
    BottomDestination(Route.Trends, "Usage", "usage"),
    BottomDestination(Route.Coach, "Insights", "insights"),
    BottomDestination(Route.Profile, "More", "more"),
)

/**
 * App-level coordinator: owns navigation and prototype-only in-memory state.
 * Screens remain focused on presentation and emit user events upward, which keeps
 * the code easy to replace with ViewModels and repositories in Assessment 4.
 */
@Composable
fun WattWiseApp() {
    val navController = rememberNavController()
    val appliances = remember { mutableStateListOf<Appliance>().apply { addAll(sampleAppliances) } }
    var selectedAppliance by remember { mutableStateOf(appliances.first()) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomDestinations.map { it.route }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = TextPrimary,
        bottomBar = {
            if (showBottomBar) {
                WattWiseBottomBar(currentRoute) { route ->
                    if (route == Route.Today) {
                        // Home is the root of the signed-in tab stack. Restoring a saved
                        // Today state can also restore the Usage entry that was above it,
                        // so return directly to the existing Home destination instead.
                        val returnedHome = navController.popBackStack(Route.Today, inclusive = false)
                        if (!returnedHome && currentRoute != Route.Today) {
                            navController.navigate(Route.Today) { launchSingleTop = true }
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(Route.Today) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Onboarding,
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF202C32), Ink, Color(0xFF0D1215))))
                .padding(innerPadding),
        ) {
            composable(Route.Onboarding) {
                OnboardingScreen(
                    onGetStarted = { navController.navigate(Route.Register) },
                    onLogin = { navController.navigate(Route.Login) },
                )
            }
            composable(Route.Login) {
                LoginScreen(
                    onBack = { navController.popBackStack() },
                    onLogin = { navController.navigate(Route.Today) },
                    onRegister = { navController.navigate(Route.Register) },
                )
            }
            composable(Route.Register) {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Route.Setup) },
                )
            }
            composable(Route.Setup) {
                HouseholdSetupScreen(onComplete = { navController.navigate(Route.Today) })
            }
            composable(Route.Today) {
                TodayScreen(
                    onLive = { navController.navigate(Route.Live) },
                    onUsage = { navController.navigate(Route.Trends) },
                    onAppliances = { navController.navigate(Route.Appliances) },
                    onCoach = { navController.navigate(Route.Coach) },
                    onEfficiency = { navController.navigate(Route.Efficiency) },
                )
            }
            composable(Route.Live) { LiveUsageScreen() }
            composable(Route.Trends) { TrendsScreen() }
            composable(Route.Coach) {
                CoachScreen(
                    onGoals = { navController.navigate(Route.Goals) },
                    onMaturity = { navController.navigate(Route.Maturity) },
                )
            }
            composable(Route.Profile) {
                ProfileScreen(
                    onAppliances = { navController.navigate(Route.Appliances) },
                    onGoals = { navController.navigate(Route.Goals) },
                    onLogout = {
                        navController.navigate(Route.Onboarding) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                )
            }
            composable(Route.Appliances) {
                ApplianceListScreen(
                    appliances = appliances,
                    onBack = { navController.popBackStack() },
                    onAdd = { navController.navigate(Route.AddAppliance) },
                    onSelect = { appliance ->
                        selectedAppliance = appliance
                        navController.navigate(Route.ApplianceDetail)
                    },
                )
            }
            composable(Route.AddAppliance) {
                ApplianceFormScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { name, category, watts ->
                        appliances.add(
                            Appliance(
                                id = (appliances.maxOfOrNull { it.id } ?: 0) + 1,
                                name = name,
                                category = category,
                                watts = watts,
                                cost = "$0.00 today",
                                status = "New",
                            ),
                        )
                        navController.popBackStack()
                    },
                )
            }
            composable(Route.EditAppliance) {
                ApplianceFormScreen(
                    existing = selectedAppliance,
                    onBack = { navController.popBackStack() },
                    onSave = { name, category, watts ->
                        val index = appliances.indexOfFirst { it.id == selectedAppliance.id }
                        if (index >= 0) {
                            val updated = selectedAppliance.copy(name = name, category = category, watts = watts)
                            appliances[index] = updated
                            selectedAppliance = updated
                        }
                        navController.popBackStack()
                    },
                )
            }
            composable(Route.ApplianceDetail) {
                ApplianceDetailScreen(
                    appliance = selectedAppliance,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Route.EditAppliance) },
                    onDelete = {
                        appliances.removeAll { it.id == selectedAppliance.id }
                        navController.popBackStack()
                    },
                )
            }
            composable(Route.Goals) { GoalsScreen(onBack = { navController.popBackStack() }) }
            composable(Route.Efficiency) { EfficiencyScreen(onBack = { navController.popBackStack() }) }
            composable(Route.Maturity) { EnergyMaturityScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
private fun WattWiseBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(DeepNavy).navigationBarsPadding().padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        bottomDestinations.forEach { destination ->
            val selected = currentRoute == destination.route
            Column(
                Modifier.weight(1f).heightIn(min = 46.dp).clickable { onNavigate(destination.route) }.padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MinimalNavIcon(destination.glyph, if (selected) TextPrimary else TextTertiary)
                Spacer(Modifier.height(2.dp))
                Text(destination.label.uppercase(), style = MaterialTheme.typography.labelMedium, color = if (selected) TextPrimary else TextTertiary)
            }
        }
    }
}

@Composable
private fun MinimalNavIcon(kind: String, color: Color) {
    Canvas(Modifier.size(21.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx())
        when (kind) {
            "home" -> {
                val p = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * .12f, size.height * .48f)
                    lineTo(size.width * .5f, size.height * .16f)
                    lineTo(size.width * .88f, size.height * .48f)
                    lineTo(size.width * .78f, size.height * .48f)
                    lineTo(size.width * .78f, size.height * .84f)
                    lineTo(size.width * .22f, size.height * .84f)
                    lineTo(size.width * .22f, size.height * .48f)
                    close()
                }
                drawPath(p, color, style = stroke)
            }
            "live" -> {
                drawLine(color, Offset(size.width * .08f, size.height * .58f), Offset(size.width * .28f, size.height * .58f), stroke.width)
                drawLine(color, Offset(size.width * .28f, size.height * .58f), Offset(size.width * .41f, size.height * .25f), stroke.width)
                drawLine(color, Offset(size.width * .41f, size.height * .25f), Offset(size.width * .58f, size.height * .78f), stroke.width)
                drawLine(color, Offset(size.width * .58f, size.height * .78f), Offset(size.width * .72f, size.height * .42f), stroke.width)
                drawLine(color, Offset(size.width * .72f, size.height * .42f), Offset(size.width * .92f, size.height * .42f), stroke.width)
            }
            "usage" -> repeat(3) { index ->
                val x = size.width * (.2f + index * .3f)
                drawLine(color, Offset(x, size.height * (.75f - index * .17f)), Offset(x, size.height * .86f), 3.5.dp.toPx())
            }
            "insights" -> {
                drawCircle(color, size.width * .34f, style = stroke)
                drawLine(color, Offset(size.width * .73f, size.height * .73f), Offset(size.width * .92f, size.height * .92f), stroke.width)
            }
            else -> {
                repeat(3) { index -> drawCircle(color, 1.6.dp.toPx(), Offset(size.width * (.25f + index * .25f), size.height * .5f)) }
            }
        }
    }
}
