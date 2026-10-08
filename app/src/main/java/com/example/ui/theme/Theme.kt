package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004D56),
    onPrimaryContainer = Color(0xFF80F0FF),
    secondary = PurpleAccent,
    onSecondary = Color(0xFF381E72),
    secondaryContainer = Color(0xFF4F378B),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = EmeraldSuccess,
    onTertiary = Color(0xFF003920),
    background = SlateDark,
    onBackground = TextLight,
    surface = SlateSurface,
    onSurface = TextLight,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSubtle,
    outline = SlateCardBorder
)

private val LightColorScheme = darkColorScheme( // High-contrast sleek dark theme default for pro browsers
    primary = CyanPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004D56),
    onPrimaryContainer = Color(0xFF80F0FF),
    secondary = PurpleAccent,
    onSecondary = Color.White,
    background = SlateDark,
    onBackground = TextLight,
    surface = SlateSurface,
    onSurface = TextLight,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSubtle,
    outline = SlateCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek multi-instance dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = SlateDark.toArgb()
                it.navigationBarColor = SlateDark.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
