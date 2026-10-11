package edu.monash.wattwise.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import edu.monash.wattwise.R

// User-provided licensed assets: Proxima Nova for language, DIN Pro for data.
val ProximaNova = FontFamily(
    Font(R.font.proxima_nova_regular, FontWeight.Normal),
    Font(R.font.proxima_nova_regular, FontWeight.Medium),
    Font(R.font.proxima_nova_bold, FontWeight.SemiBold),
    Font(R.font.proxima_nova_bold, FontWeight.Bold),
)
val DinPro = FontFamily(Font(R.font.dinpro_bold, FontWeight.Bold))

val WattWiseTypography = Typography(
    displayLarge = TextStyle(fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 56.sp, letterSpacing = (-1.6).sp),
    displayMedium = TextStyle(fontFamily = DinPro, fontWeight = FontWeight.Bold, fontSize = 40.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.Bold, fontSize = 19.sp),
    titleMedium = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.15.sp),
    bodyLarge = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.15.sp),
    labelMedium = TextStyle(fontFamily = ProximaNova, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp),
)
