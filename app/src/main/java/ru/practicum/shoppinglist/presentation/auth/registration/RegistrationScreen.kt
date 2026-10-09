package ru.practicum.shoppinglist.presentation.auth.registration

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.practicum.shoppinglist.presentation.auth.messageResource

@Composable
internal fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(viewModel, resources) {
        viewModel.effects.collect { effect ->
            when (effect) {
                RegistrationEffect.NavigateBack -> onNavigateBack()

                RegistrationEffect.NavigateToShoppingLists -> {
                    onRegistrationSuccess()
                }

                is RegistrationEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        resources.getString(effect.error.messageResource),
                    )
                }
            }
        }
    }

    RegistrationContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}
