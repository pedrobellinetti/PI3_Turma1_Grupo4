package com.example.superid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.superid.ui.theme.TopContainer

private val LightColorScheme = lightColorScheme(
    primary = ButtonColor,
    onPrimary = TextOnPrimary,
    onSecondary = TextSecondary,
    background = BackgroundColor,
    surface = InputFieldColor,
    onSurface = TextPrimary,
    primaryContainer = TopContainer,
    onPrimaryContainer = BackgroundColor,
    secondaryContainer = UserItemContainer
    /*
    secondary = ...,
    onSecondary = ...,
    tertiary = ...,
    onTertiary = ...,
    error = ...,
    onError = ...,
    surfaceVariant = ...,
    onSurfaceVariant = ...,
    outline = ...
    */
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkButtonColor,
    onPrimary = DarkButtonText,
    onSecondary = DarkTextSecondary,
    background = DarkBackgroundColor,
    surface = DarkTopContainer,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextSecondary,
    secondaryContainer = DarkUserItemContainer
)

@Composable
fun SuperIDTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android S+
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
        typography = AppTypography,
        shapes = Shapes,
        content = content
    )
}
