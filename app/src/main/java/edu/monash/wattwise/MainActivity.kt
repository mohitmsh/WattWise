package edu.monash.wattwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import edu.monash.wattwise.ui.WattWiseApp
import edu.monash.wattwise.ui.theme.WattWiseTheme
import edu.monash.wattwise.viewmodel.ApplianceViewModel
import edu.monash.wattwise.viewmodel.AuthViewModel
import edu.monash.wattwise.viewmodel.HouseholdViewModel
import edu.monash.wattwise.viewmodel.ReminderViewModel
import edu.monash.wattwise.viewmodel.SmartMeterViewModel
import edu.monash.wattwise.viewmodel.UsageViewModel
import edu.monash.wattwise.viewmodel.WeatherViewModel
import edu.monash.wattwise.worker.UsageSummaryWorker
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

/** Single-activity entry point. It creates the ViewModels and hands them to the Compose UI. */
class MainActivity : ComponentActivity() {

    // "by viewModels()" keeps each ViewModel alive across screen rotations.
    private val applianceViewModel: ApplianceViewModel by viewModels()
    private val householdViewModel: HouseholdViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val smartMeterViewModel: SmartMeterViewModel by viewModels()
    private val usageViewModel: UsageViewModel by viewModels()
    private val reminderViewModel: ReminderViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MapLibre must be initialised before a map is created. No access token is needed.
        MapLibre.getInstance(applicationContext, "", WellKnownTileServer.MapLibre)

        // Start the background work that keeps the usage history in Room up to date.
        UsageSummaryWorker.schedule(this)

        setContent {
            WattWiseTheme {
                WattWiseApp(
                    applianceViewModel,
                    householdViewModel,
                    weatherViewModel,
                    smartMeterViewModel,
                    usageViewModel,
                    reminderViewModel,
                    authViewModel,
                )
            }
        }
    }
}
