package ru.practicum.shoppinglist.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DesignLightPrimary,
    onPrimary = Color.White,
    surfaceContainer = DesignLightSurfaceLow,
    primaryContainer = DesignLightPrimaryContainer,
    onPrimaryContainer = DesignLightOnPrimaryContainer,
    secondary = DesignLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = DesignLightSecondaryContainer,
    onSecondaryContainer = DesignLightOnSecondaryContainer,
    background = DesignLightBackground,
    onBackground = DesignLightInk,
    surface = DesignLightSurface,
    onSurface = DesignLightOnSurface,
    surfaceVariant = DesignLightSurfaceLow,
    onSurfaceVariant = DesignLightOnSurfaceVariant,
    outline = DesignLightOutline,
    outlineVariant = DesignLightOutlineVariant,
    error = DesignLightError,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = DesignDarkPrimary,
    onPrimary = DesignDarkOnPrimary,
    surfaceContainer = DesignDarkSurfaceContainer,
    primaryContainer = DesignDarkPrimaryContainer,
    onPrimaryContainer = DesignDarkOnPrimaryContainer,
    secondary = DesignDarkSecondary,
    onSecondary = Color(0xFF000000),
    secondaryContainer = DesignDarkSecondaryContainer,
    onSecondaryContainer = DesignDarkOnSecondaryContainer,
    background = DesignDarkBackground,
    onBackground = DesignDarkInk,
    surface = DesignDarkSurface,
    onSurface = DesignDarkOnSurface,
    surfaceVariant = DesignDarkSurfaceLow,
    onSurfaceVariant = DesignDarkOnSurfaceVariant,
    outline = DesignDarkOutline,
    outlineVariant = DesignDarkOutlineVariant,
    error = DesignDarkError,
    onError = Color(0xFF601410),
)

@Composable
fun ShoppingListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
