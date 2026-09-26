package ru.practicum.shoppinglist.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.practicum.shoppinglist.presentation.lists.ShoppingListsScreen

@Composable
internal fun ShoppingListNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.SHOPPING_LISTS,
    ) {
        composable(route = AppRoute.SHOPPING_LISTS) {
            ShoppingListsScreen()
        }
    }
}
