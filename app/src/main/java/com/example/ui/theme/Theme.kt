package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MelonaMintColorScheme = lightColorScheme(
    primary = VividMintPrimary,
    onPrimary = VividMintOnPrimary,
    primaryContainer = VividMintContainer,
    onPrimaryContainer = VividMintOnContainer,
    secondary = VividMintSecondary,
    onSecondary = VividMintOnSecondary,
    secondaryContainer = VividMintSecondaryContainer,
    onSecondaryContainer = VividMintOnSecondaryContainer,
    tertiary = VividMintTertiary,
    onTertiary = VividMintOnTertiary,
    tertiaryContainer = VividMintTertiaryContainer,
    onTertiaryContainer = VividMintOnTertiaryContainer,
    background = MelonaBackground,
    onBackground = MelonaOnBackground,
    surface = MelonaSurface,
    onSurface = MelonaOnSurface,
    surfaceVariant = MelonaSurfaceVariant,
    onSurfaceVariant = MelonaOnSurfaceVariant,
    outline = MelonaOutline,
    outlineVariant = MelonaOutlineVariant
)

@Composable
fun DayCheckTheme(
    // Always render the requested Melona Background + Vivid Mint theme, avoiding dark mode black screens
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MelonaMintColorScheme,
        typography = Typography,
        content = content
    )
}
