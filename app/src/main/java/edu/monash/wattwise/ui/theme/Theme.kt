package edu.monash.wattwise.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Builds the Material colour scheme from the app's colours for the chosen theme. */
private fun wattWiseColors() = if (ThemeSettings.isLight) {
    lightColorScheme(
        primary = Electric,
        onPrimary = OnAccent,
        secondary = Cyan,
        tertiary = Amber,
        background = Ink,
        onBackground = TextPrimary,
        surface = Surface,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceRaised,
        onSurfaceVariant = TextSecondary,
        outline = TextTertiary,
        error = Coral,
        onError = Color.White,
    )
} else {
    darkColorScheme(
        primary = Electric,
        onPrimary = OnAccent,
        secondary = Cyan,
        tertiary = Amber,
        background = Ink,
        onBackground = TextPrimary,
        surface = Surface,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceRaised,
        onSurfaceVariant = TextSecondary,
        outline = Border,
        error = Coral,
        onError = Color.White,
    )
}

/** Applies the WattWise colours and typography to [content] for the theme the user selected. */
@Composable
fun WattWiseTheme(content: @Composable () -> Unit) {
    // Dark status bar icons on the light theme and light icons on the dark theme, so the
    // clock and battery stay readable against the app's background.
    val view = LocalView.current
    val isLight = ThemeSettings.isLight
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLight
        }
    }
    MaterialTheme(
        colorScheme = wattWiseColors(),
        typography = WattWiseTypography,
    ) {
        // The screens sit on a transparent gradient, so set the text colour for the theme here.
        CompositionLocalProvider(LocalContentColor provides TextPrimary, content = content)
    }
}
