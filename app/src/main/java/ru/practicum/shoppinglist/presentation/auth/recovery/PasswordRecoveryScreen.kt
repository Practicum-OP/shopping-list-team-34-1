package ru.practicum.shoppinglist.presentation.auth.recovery

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.presentation.auth.messageResource

@Composable
internal fun PasswordRecoveryScreen(
    onNavigateBack: () -> Unit,
    viewModel: PasswordRecoveryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LaunchedEffect(viewModel, resources) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PasswordRecoveryEffect.NavigateBack -> onNavigateBack()

                PasswordRecoveryEffect.RecoveryEmailSent -> {
                    snackbarHostState.showSnackbar(
                        resources.getString(
                            R.string.password_recovery_success,
                        ),
                    )
                }

                is PasswordRecoveryEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        resources.getString(effect.error.messageResource),
                    )
                }
            }
        }
    }

    PasswordRecoveryContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}
