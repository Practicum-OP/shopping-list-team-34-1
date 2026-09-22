package ru.practicum.shoppinglist.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorScreen
import ru.practicum.shoppinglist.presentation.lists.ShoppingListsScreen

private const val NEW_LIST_ID = -1L

@Composable
internal fun ShoppingListNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.SHOPPING_LISTS,
    ) {
        composable(route = AppRoute.SHOPPING_LISTS) {
            ShoppingListsScreen(
                onCreateList = {
                    navController.navigate(AppRoute.listEditor())
                },
            )
        }

        composable(
            route = AppRoute.LIST_EDITOR_ROUTE,
            arguments = listOf(
                navArgument(AppRoute.LIST_ID_ARGUMENT) {
                    type = NavType.LongType
                    defaultValue = NEW_LIST_ID
                },
            ),
        ) { backStackEntry ->
            val listId = backStackEntry.arguments
                ?.getLong(AppRoute.LIST_ID_ARGUMENT)
                ?.takeUnless { it == NEW_LIST_ID }

            ShoppingListEditorScreen(
                listId = listId,
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
