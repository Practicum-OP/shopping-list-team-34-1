package ru.practicum.shoppinglist.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.practicum.shoppinglist.presentation.editor.ListEditorScreen
import ru.practicum.shoppinglist.presentation.lists.ShoppingListsScreen

@Composable
internal fun ShoppingListNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.SHOPPING_LISTS,
    ) {
        composable(route = AppRoute.SHOPPING_LISTS) {
            ShoppingListsScreen(
                onListClick = { listId: Long, listName: String ->
                    navController.navigate(AppRoute.createListEditorRoute(listId, listName))
                }
            )
        }

        composable(
            route = AppRoute.LIST_EDITOR,
            arguments = listOf(
                navArgument("listId") { type = NavType.LongType },
                navArgument("listName") { type = NavType.StringType }
            )
        ) {
            ListEditorScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
