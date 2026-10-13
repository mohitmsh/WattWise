package edu.monash.wattwise.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * The user's choice of light or dark theme. Every colour below reads
 * [isLight], and because it is Compose state, the screens redraw with the
 * other set of colours as soon as it changes.
 */
object ThemeSettings {
    var isLight by mutableStateOf(false)
}

/** Returns [light] in the light theme and [dark] in the dark theme. */
private fun themed(dark: Long, light: Long): Color = Color(if (ThemeSettings.isLight) light else dark)

// Backgrounds and surfaces.
val Ink: Color get() = themed(dark = 0xFF101518, light = 0xFFEAF2F8)
val DeepNavy: Color get() = themed(dark = 0xFF171C1F, light = 0xFFFFFFFF)
val Surface: Color get() = themed(dark = 0xFF25292C, light = 0xFFFFFFFF)
val SurfaceRaised: Color get() = themed(dark = 0xFF31363A, light = 0xFFE3E8EB)
val Border: Color get() = themed(dark = 0x14FFFFFF, light = 0x1F000000)

// Text, from most to least prominent.
val TextPrimary: Color get() = themed(dark = 0xFFFFFFFF, light = 0xFF12181B)
val TextSecondary: Color get() = themed(dark = 0xFFA5ADB1, light = 0xFF47525A)
val TextTertiary: Color get() = themed(dark = 0xFF9AA2A7, light = 0xFF56616A)

// Accent and status colours. The light versions are darker so they stay readable on white.
// Every colour here has a contrast ratio of at least 4.5 to 1 against all four backgrounds of
// its theme, which is the WCAG 2.1 level AA minimum for normal text.
val Electric: Color get() = themed(dark = 0xFF00F19F, light = 0xFF006647)
val Cyan: Color get() = themed(dark = 0xFF4DB8F5, light = 0xFF005C99)
val Amber: Color get() = themed(dark = 0xFFFFDE00, light = 0xFF7D4A00)
val Coral: Color get() = themed(dark = 0xFFFF7A87, light = 0xFFB00D28)
// A soft green, so it does not compete with the brand green next to it.
val StatusGood: Color get() = themed(dark = 0xFF5BD98A, light = 0xFF17650F)
val SoftBlue: Color get() = themed(dark = 0xFF72B9DE, light = 0xFF1F5C80)

/** Text and icons placed on top of an accent colour, such as a badge or a selected chip. */
val OnAccent: Color get() = themed(dark = 0xFF101518, light = 0xFFFFFFFF)

// Background gradient behind every screen, from top to bottom. The light theme fades from soft blue to white.
val GradientTop: Color get() = themed(dark = 0xFF202C32, light = 0xFFCFE3F3)
val GradientBottom: Color get() = themed(dark = 0xFF0D1215, light = 0xFFF8FAFB)
