package com.mochi.glucofin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BloodRedDark,
    onPrimary = OnBloodRedDark,
    primaryContainer = BloodRedContainerDark,
    onPrimaryContainer = OnBloodRedContainerDark,
    secondary = SecondaryBlood,
    onSecondary = OnSecondaryBlood,
    background = Color(0xFF1A1110),
    surface = Color(0xFF1A1110),
)

private val LightColorScheme = lightColorScheme(
    primary = BloodRed,
    onPrimary = Color.White,
    primaryContainer = BloodRedContainer,
    onPrimaryContainer = OnBloodRedContainer,
    secondary = SecondaryBlood,
    onSecondary = OnSecondaryBlood,
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
)

@Composable
fun GlucofinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Disabled by default to keep the branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
