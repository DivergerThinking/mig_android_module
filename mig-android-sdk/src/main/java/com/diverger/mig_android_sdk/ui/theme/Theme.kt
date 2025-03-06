package com.diverger.mig_android_sdk.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun MIGAndroidSDKTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Aplicamos la tipografía personalizada
        content = content
    )
}


val DarkColorScheme = darkColorScheme(
    primary = Color.Cyan,
    onPrimary = Color.Black,
    secondary = Color.Magenta,
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.DarkGray,
    onSurface = Color.White
)

val LightColorScheme = lightColorScheme(
    primary = Color.Cyan,
    onPrimary = Color.White,
    secondary = Color.Magenta,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.LightGray,
    onSurface = Color.Black
)
