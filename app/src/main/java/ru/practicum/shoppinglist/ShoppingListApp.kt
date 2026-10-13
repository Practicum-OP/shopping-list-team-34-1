package ru.practicum.shoppinglist

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.presentation.navigation.ShoppingListNavHost
import ru.practicum.shoppinglist.ui.theme.ShoppingListTheme

private val Context.appearanceDataStore by preferencesDataStore(name = "appearance")
private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

@Composable
internal fun ShoppingListApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themePreferenceFlow = remember(context) {
        context.appearanceDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences -> preferences[DARK_THEME_KEY] }
    }
    val savedDarkTheme by themePreferenceFlow.collectAsState(initial = null)
    val isDarkTheme = savedDarkTheme ?: isSystemInDarkTheme()
    val view = LocalView.current

    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDarkTheme
                isAppearanceLightNavigationBars = !isDarkTheme
            }
        }
    }

    ShoppingListTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ShoppingListNavHost(
                isDarkTheme = isDarkTheme,
                onToggleTheme = {
                    scope.launch {
                        context.appearanceDataStore.edit { preferences ->
                            preferences[DARK_THEME_KEY] =
                                !(preferences[DARK_THEME_KEY] ?: isDarkTheme)
                        }
                    }
                },
            )
        }
    }
}
