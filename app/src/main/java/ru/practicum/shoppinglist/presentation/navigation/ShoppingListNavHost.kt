package ru.practicum.shoppinglist.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import ru.practicum.shoppinglist.presentation.auth.check.AuthCheckScreen
import ru.practicum.shoppinglist.presentation.auth.login.LoginScreen
import ru.practicum.shoppinglist.presentation.auth.recovery.PasswordRecoveryScreen
import ru.practicum.shoppinglist.presentation.auth.registration.RegistrationScreen
import ru.practicum.shoppinglist.presentation.editor.ListEditorScreen
import ru.practicum.shoppinglist.presentation.lists.ShoppingListsScreen

@Composable
internal fun ShoppingListNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.AUTH_GRAPH,
    ) {
        authGraph(navController)
        shoppingListsDestination(navController)
        listEditorDestination(navController)
    }
}

private fun NavGraphBuilder.authGraph(
    navController: NavHostController,
) {
    navigation(
        startDestination = AppRoute.AUTH_CHECK,
        route = AppRoute.AUTH_GRAPH,
    ) {
        authCheckDestination(navController)
        loginDestination(navController)
        registrationDestination(navController)
        passwordRecoveryDestination(navController)
    }
}

private fun NavGraphBuilder.authCheckDestination(
    navController: NavHostController,
) {
    composable(route = AppRoute.AUTH_CHECK) {
        AuthCheckScreen(
            onAuthenticated = {
                navController.openShoppingLists()
            },
            onAuthenticationRequired = {
                navController.navigate(AppRoute.LOGIN) {
                    popUpTo(AppRoute.AUTH_CHECK) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
        )
    }
}

private fun NavGraphBuilder.loginDestination(
    navController: NavHostController,
) {
    composable(route = AppRoute.LOGIN) {
        LoginScreen(
            onLoginSuccess = {
                navController.openShoppingLists()
            },
            onRegistrationClick = {
                navController.navigate(AppRoute.REGISTRATION) {
                    launchSingleTop = true
                }
            },
            onPasswordRecoveryClick = {
                navController.navigate(AppRoute.PASSWORD_RECOVERY) {
                    launchSingleTop = true
                }
            },
        )
    }
}

private fun NavGraphBuilder.registrationDestination(
    navController: NavHostController,
) {
    composable(route = AppRoute.REGISTRATION) {
        RegistrationScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onRegistrationSuccess = {
                navController.openShoppingLists()
            },
        )
    }
}

private fun NavGraphBuilder.passwordRecoveryDestination(
    navController: NavHostController,
) {
    composable(route = AppRoute.PASSWORD_RECOVERY) {
        PasswordRecoveryScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
        )
    }
}

private fun NavGraphBuilder.shoppingListsDestination(
    navController: NavHostController,
) {
    composable(route = AppRoute.SHOPPING_LISTS) {
        ShoppingListsScreen(
            onListClick = { listId: Long, listName: String ->
                navController.navigate(
                    AppRoute.createListEditorRoute(
                        listId = listId,
                        listName = listName,
                    ),
                )
            },
        )
    }
}

private fun NavGraphBuilder.listEditorDestination(
    navController: NavHostController,
) {
    composable(
        route = AppRoute.LIST_EDITOR,
        arguments = listOf(
            navArgument("listId") {
                type = NavType.LongType
            },
            navArgument("listName") {
                type = NavType.StringType
            },
        ),
    ) {
        ListEditorScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
        )
    }
}

private fun NavHostController.openShoppingLists() {
    navigate(AppRoute.SHOPPING_LISTS) {
        popUpTo(AppRoute.AUTH_GRAPH) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
