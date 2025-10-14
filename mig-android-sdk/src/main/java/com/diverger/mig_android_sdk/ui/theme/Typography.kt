package com.diverger.mig_android_sdk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.diverger.mig_android_sdk.R

val MadridingameFont = FontFamily(
    Font(R.font.madrid_in_game_font)
)

// Definir la tipografía general del módulo
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 32.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 28.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 24.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 22.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 20.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 18.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 16.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 14.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 12.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MadridingameFont,
        fontSize = 10.sp
    )
)
