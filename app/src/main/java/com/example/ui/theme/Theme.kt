package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AccessOsColorScheme = darkColorScheme(
    primary = AccessPrimaryLight,
    onPrimary = AccessBackground,
    primaryContainer = AccessPrimary,
    onPrimaryContainer = TextPrimary,
    secondary = AccessSecondary,
    onSecondary = TextPrimary,
    tertiary = AccessTertiary,
    onTertiary = AccessBackground,
    background = AccessBackground,
    onBackground = TextPrimary,
    surface = AccessSurface,
    onSurface = TextPrimary,
    surfaceVariant = AccessSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = AccessDanger,
    onError = TextPrimary,
    errorContainer = AccessDangerContainer,
    onErrorContainer = TextPrimary,
    outline = AccessSurfaceBorder
)

@Composable
fun AccessOsTheme(
    content: @Composable () -> Unit
) {
    // Enforce dark theme with high contrast for elderly readability
    MaterialTheme(
        colorScheme = AccessOsColorScheme,
        typography = Typography,
        content = content
    )
}
