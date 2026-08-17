package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RacingColorScheme = darkColorScheme(
    primary = SoftLavender,
    onPrimary = DeepPurple,
    secondary = LightPurple,
    onSecondary = CharcoalDark,
    tertiary = RoseBadgeText,
    background = CharcoalDark,
    onBackground = OnSurfaceText,
    surface = SurfaceDark,
    onSurface = OnSurfaceText,
    surfaceVariant = BorderDark,
    onSurfaceVariant = MutedText
)

@Composable
fun ApexDriftTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RacingColorScheme,
        typography = Typography,
        content = content
    )
}

