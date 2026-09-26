package ru.practicum.shoppinglist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PracticumYellow,
    onPrimary = PracticumInk,
    primaryContainer = PracticumYellowContainer,
    onPrimaryContainer = PracticumInk,
    secondary = PracticumYellowContainer,
    background = PracticumDarkBackground,
    onBackground = PracticumDarkOnSurface,
    surface = PracticumDarkSurface,
    onSurface = PracticumDarkOnSurface,
    surfaceVariant = PracticumDarkSurfaceVariant,
    onSurfaceVariant = PracticumDarkOnSurface,
    outline = PracticumDarkOutline,
    error = PracticumError,
)

private val LightColorScheme = lightColorScheme(
    primary = PracticumYellow,
    onPrimary = PracticumInk,
    primaryContainer = PracticumYellowContainer,
    onPrimaryContainer = PracticumInk,
    secondary = PracticumInk,
    onSecondary = PracticumSurface,
    background = PracticumBackground,
    onBackground = PracticumInk,
    surface = PracticumSurface,
    onSurface = PracticumInk,
    surfaceVariant = PracticumSurfaceVariant,
    onSurfaceVariant = PracticumMuted,
    outline = PracticumOutline,
    error = PracticumError,
)

@Composable
fun ShoppingListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
