package ru.practicum.shoppinglist.presentation.editor

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Effect
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Intent
import ru.practicum.shoppinglist.presentation.editor.ShoppingListEditorContract.Message

@Composable
internal fun ShoppingListEditorScreen(
    listId: Long?,
    onBack: () -> Unit,
) {
    if (listId == null || listId <= 0) {
        InvalidListContent(onBack = onBack)
        return
    }

    val viewModel: ShoppingListEditorViewModel = koinViewModel(
        key = "shopping_list_editor_$listId",
        parameters = { parametersOf(listId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                Effect.NavigateBack -> onBack()
            }
        }
    }

    val message = state.message
    if (message != null) {
        val messageText = stringResource(message.stringResource)
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.onIntent(Intent.MessageShown)
        }
    }

    ShoppingListEditorContent(
        state = state,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        onBack = onBack,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun InvalidListContent(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(R.string.editor_invalid_list_title))
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.editor_invalid_list_subtitle),
        )
        Button(
            modifier = Modifier.padding(top = 20.dp),
            onClick = onBack,
        ) {
            Text(text = stringResource(R.string.editor_back))
        }
    }
}

internal val Message.stringResource: Int
    @StringRes get() = when (this) {
        Message.LIST_NOT_FOUND -> R.string.editor_list_not_found
        Message.LOAD_FAILED -> R.string.editor_load_failed
        Message.SAVE_FAILED -> R.string.editor_save_failed
    }
