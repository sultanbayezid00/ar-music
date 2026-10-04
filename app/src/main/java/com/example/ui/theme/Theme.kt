package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ARPrimary,
    onPrimary = AROnPrimary,
    primaryContainer = ARPrimaryContainer,
    onPrimaryContainer = ARPrimary,
    secondary = ARSecondary,
    onSecondary = AROnSecondary,
    secondaryContainer = ARSecondaryContainer,
    onSecondaryContainer = ARSecondary,
    tertiary = ARTertiary,
    onTertiary = AROnTertiary,
    tertiaryContainer = ARTertiaryContainer,
    onTertiaryContainer = ARTertiary,
    background = ARBackground,
    onBackground = AROnBackground,
    surface = ARSurface,
    onSurface = AROnSurface,
    surfaceVariant = ARSurfaceVariant,
    onSurfaceVariant = AROnSurfaceVariant,
    outline = AROutline,
    outlineVariant = AROutlineVariant
)

@Composable
fun ARMusicTheme(
    darkTheme: Boolean = true, // Music app default is dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
