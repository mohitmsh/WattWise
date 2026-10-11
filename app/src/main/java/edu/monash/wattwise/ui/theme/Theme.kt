package edu.monash.wattwise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val WattWiseColors = darkColorScheme(
    primary = Electric,
    onPrimary = Ink,
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

@Composable
fun WattWiseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WattWiseColors,
        typography = WattWiseTypography,
    ) {
        // Transparent gradient screens must explicitly inherit the dark foreground.
        CompositionLocalProvider(LocalContentColor provides TextPrimary, content = content)
    }
}
