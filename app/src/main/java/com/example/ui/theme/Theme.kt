package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RomanDarkColorScheme = darkColorScheme(
    primary = RomanGold,
    onPrimary = RomanDarkSurface,
    primaryContainer = RomanCrimsonDark,
    onPrimaryContainer = RomanGoldLight,
    secondary = RomanGoldLight,
    onSecondary = RomanDarkSurface,
    secondaryContainer = RomanBronzeDark,
    onSecondaryContainer = RomanParchmentDark,
    tertiary = RomanLaurelGreen,
    onTertiary = Color.White,
    background = RomanDarkSurface,
    onBackground = RomanTextLight,
    surface = RomanDarkSurfaceCard,
    onSurface = RomanTextLight,
    surfaceVariant = RomanBronzeDark,
    onSurfaceVariant = RomanTextGold,
    outline = RomanGoldDark
)

private val RomanLightColorScheme = lightColorScheme(
    primary = RomanCrimson,
    onPrimary = Color.White,
    primaryContainer = RomanGoldLight,
    onPrimaryContainer = RomanCrimsonDark,
    secondary = RomanGoldDark,
    onSecondary = Color.White,
    secondaryContainer = RomanParchmentDark,
    onSecondaryContainer = RomanTextDark,
    tertiary = RomanLaurelGreen,
    onTertiary = Color.White,
    background = RomanParchment,
    onBackground = RomanTextDark,
    surface = RomanParchmentCard,
    onSurface = RomanTextDark,
    surfaceVariant = RomanParchmentDark,
    onSurfaceVariant = RomanTextMuted,
    outline = RomanBronze
)

@Composable
fun LegioInvictaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use authentic Roman palette by default for deep theme atmosphere
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RomanDarkColorScheme else RomanLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LegioInvictaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

