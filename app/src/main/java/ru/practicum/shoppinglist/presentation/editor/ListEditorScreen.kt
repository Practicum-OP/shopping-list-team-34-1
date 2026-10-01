package ru.practicum.shoppinglist.presentation.editor

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
internal fun ListEditorScreen(
    viewModel: ListEditorViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val operationFailedMessage = stringResource(R.string.editor_operation_failed)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ListEditorEffect.OperationFailed -> {
                    snackbarHostState.showSnackbar(operationFailedMessage)
                }

                ListEditorEffect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    ListEditorContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}