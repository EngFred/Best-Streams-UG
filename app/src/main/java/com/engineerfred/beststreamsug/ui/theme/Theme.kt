package com.engineerfred.beststreamsug.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BestStreamsUGTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = CinematicPrimary,
        onPrimary = CinematicBackground,
        secondary = CinematicSecondary,
        background = CinematicBackground,
        onBackground = CinematicText,
        surface = CinematicSurface,
        onSurface = CinematicText,
        surfaceVariant = CinematicSurfaceVariant,
        onSurfaceVariant = CinematicText,
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}