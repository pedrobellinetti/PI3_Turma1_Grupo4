package com.example.superid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.superid.ui.theme.*
import com.example.superid.ui.theme.*

private val LightColorScheme = lightColorScheme(
    primary = LightButtonColor,
    onPrimary = LightTextOnPrimary,
    background = LightBackgroundColor,
    surface = LightTopContainer,
    onBackground = LightTextPrimary,
    onSurface = LightTextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkButtonColor,
    onPrimary = DarkTextOnPrimary,
    background = DarkBackgroundColor,
    surface = DarkTopContainer,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextSecondary,
)
@Composable
fun SuperIDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android S+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = Shapes,
        content = content
    )
}