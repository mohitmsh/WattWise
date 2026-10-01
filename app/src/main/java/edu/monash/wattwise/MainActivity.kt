package edu.monash.wattwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import edu.monash.wattwise.ui.WattWiseApp
import edu.monash.wattwise.ui.theme.WattWiseTheme

/** Single-activity entry point. Navigation and screen state live in Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WattWiseTheme {
                WattWiseApp()
            }
        }
    }
}
