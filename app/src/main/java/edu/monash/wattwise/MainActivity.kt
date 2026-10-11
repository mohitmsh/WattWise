package edu.monash.wattwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import edu.monash.wattwise.ui.WattWiseApp
import edu.monash.wattwise.ui.theme.WattWiseTheme
import edu.monash.wattwise.viewmodel.ApplianceViewModel
import edu.monash.wattwise.viewmodel.HouseholdViewModel
import edu.monash.wattwise.viewmodel.WeatherViewModel

/** Single-activity entry point. It creates the ViewModels and hands them to the Compose UI. */
class MainActivity : ComponentActivity() {

    // "by viewModels()" keeps each ViewModel alive across screen rotations.
    private val applianceViewModel: ApplianceViewModel by viewModels()
    private val householdViewModel: HouseholdViewModel by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WattWiseTheme {
                WattWiseApp(applianceViewModel, householdViewModel, weatherViewModel)
            }
        }
    }
}
