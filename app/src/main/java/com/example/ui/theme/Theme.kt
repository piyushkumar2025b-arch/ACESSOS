package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AccessOsColorScheme = darkColorScheme(
    primary = AccessPrimaryLight,
    onPrimary = AccessBackground,
    primaryContainer = AccessPrimary,
    onPrimaryContainer = AccessOnPrimary,
    secondary = AccessSecondary,
    onSecondary = AccessBackground,
    secondaryContainer = AccessSecondarySoft,
    onSecondaryContainer = TextPrimary,
    tertiary = AccessTertiary,
    onTertiary = AccessBackground,
    tertiaryContainer = AccessTertiarySoft,
    onTertiaryContainer = TextAccent,
    background = AccessBackground,
    onBackground = TextPrimary,
    surface = AccessSurface,
    onSurface = TextPrimary,
    surfaceVariant = AccessSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceTint = AccessPrimaryLight,
    error = AccessDanger,
    onError = TextPrimary,
    errorContainer = AccessDangerContainer,
    onErrorContainer = TextPrimary,
    outline = AccessSurfaceBorder,
    outlineVariant = AccessSurfaceBorder.copy(alpha = 0.5f)
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
