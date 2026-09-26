package ru.practicum.shoppinglist.presentation.lists

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.practicum.shoppinglist.R

@Composable
internal fun ShoppingListsScreen(
    viewModel: ShoppingListsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val operationFailedMessage = stringResource(R.string.lists_operation_failed)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ShoppingListsEffect.OperationFailed -> {
                    snackbarHostState.showSnackbar(operationFailedMessage)
                }
            }
        }
    }

    ShoppingListsContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
    )
}
