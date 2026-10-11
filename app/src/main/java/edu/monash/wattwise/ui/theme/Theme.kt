package edu.monash.wattwise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

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

@Composable
fun WattWiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = wattWiseColors(),
        typography = WattWiseTypography,
    ) {
        // The screens sit on a transparent gradient, so set the text colour for the theme here.
        CompositionLocalProvider(LocalContentColor provides TextPrimary, content = content)
    }
}
