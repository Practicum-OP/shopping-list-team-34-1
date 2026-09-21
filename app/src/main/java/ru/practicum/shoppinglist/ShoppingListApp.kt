package ru.practicum.shoppinglist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.practicum.shoppinglist.presentation.navigation.ShoppingListNavHost
import ru.practicum.shoppinglist.ui.theme.ShoppingListTheme

@Composable
internal fun ShoppingListApp() {
    ShoppingListTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ShoppingListNavHost()
        }
    }
}
