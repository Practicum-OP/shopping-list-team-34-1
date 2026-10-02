package ru.practicum.shoppinglist.presentation.auth.login

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
internal fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegistrationClick: () -> Unit,
    onPasswordRecoveryClick: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(viewModel, resources) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginEffect.NavigateToShoppingLists -> onLoginSuccess()
                LoginEffect.NavigateToRegistration -> onRegistrationClick()
                LoginEffect.NavigateToPasswordRecovery -> onPasswordRecoveryClick()

                is LoginEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        resources.getString(effect.error.messageResource),
                    )
                }
            }
        }
    }

    LoginContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}
