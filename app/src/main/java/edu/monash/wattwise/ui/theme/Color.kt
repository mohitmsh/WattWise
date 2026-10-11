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
val TextTertiary: Color get() = themed(dark = 0xFF737B80, light = 0xFF75818A)

// Accent and status colours. The light versions are darker so they stay readable on white.
val Electric: Color get() = themed(dark = 0xFF00F19F, light = 0xFF00805A)
val Cyan: Color get() = themed(dark = 0xFF0093E7, light = 0xFF0072BC)
val Amber: Color get() = themed(dark = 0xFFFFDE00, light = 0xFFB06A00)
val Coral: Color get() = themed(dark = 0xFFFF0026, light = 0xFFC8102E)
val StatusGood: Color get() = themed(dark = 0xFF16EC06, light = 0xFF1B7F12)
val SoftBlue: Color get() = themed(dark = 0xFF72B9DE, light = 0xFF2C7AA8)

/** Text and icons placed on top of an accent colour, such as a badge or a selected chip. */
val OnAccent: Color get() = themed(dark = 0xFF101518, light = 0xFFFFFFFF)

// Background gradient behind every screen, from top to bottom. The light theme fades from soft blue to white.
val GradientTop: Color get() = themed(dark = 0xFF202C32, light = 0xFFCFE3F3)
val GradientBottom: Color get() = themed(dark = 0xFF0D1215, light = 0xFFF8FAFB)
