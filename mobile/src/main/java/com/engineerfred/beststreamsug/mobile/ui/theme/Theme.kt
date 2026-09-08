package com.engineerfred.beststreamsug.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CinematicColorScheme = darkColorScheme(
    primary = CinematicPrimary,
    onPrimary = CinematicBackground,
    primaryContainer = CinematicSurfaceContainerHigh,
    onPrimaryContainer = CinematicText,
    secondary = CinematicSecondary,
    onSecondary = CinematicBackground,
    secondaryContainer = CinematicSurfaceContainerHigh,
    onSecondaryContainer = CinematicText,
    background = CinematicBackground,
    onBackground = CinematicText,
    surface = CinematicSurface,
    onSurface = CinematicText,
    surfaceVariant = CinematicSurfaceVariant,
    onSurfaceVariant = CinematicMutedText,
    surfaceContainer = CinematicSurfaceContainer,
    surfaceContainerHigh = CinematicSurfaceContainerHigh,
    surfaceContainerHighest = CinematicSurfaceVariant,
    outline = CinematicDivider,
    outlineVariant = CinematicSurfaceContainerHigh,
    error = CinematicSecondary,
    onError = CinematicBackground,
)

@Composable
fun BestStreamsUGTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CinematicColorScheme,
        typography = Typography,
        content = content,
    )
}