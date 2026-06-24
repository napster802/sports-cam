package com.sportcasterpro.app.core.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette: deep broadcast navy + live-orange accent.
val NavyDark = Color(0xFF050E1C)
val Navy = Color(0xFF0B1E3A)
val NavySurface = Color(0xFF132A4D)
val Orange = Color(0xFFFF6A1A)
val OrangeLight = Color(0xFFFF9255)
val LiveRed = Color(0xFFE53935)
val OnDark = Color(0xFFF4F6FA)
val Slate = Color(0xFF8A94A6)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Orange,
    onPrimary = NavyDark,
    secondary = OrangeLight,
    onSecondary = NavyDark,
    background = NavyDark,
    onBackground = OnDark,
    surface = Navy,
    onSurface = OnDark,
    surfaceVariant = NavySurface,
    onSurfaceVariant = Slate,
    error = LiveRed,
)

val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Navy,
    onSecondary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Navy,
    surface = Color.White,
    onSurface = Navy,
    surfaceVariant = Color(0xFFE7EAF0),
    onSurfaceVariant = Slate,
    error = LiveRed,
)
