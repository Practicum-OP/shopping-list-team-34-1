package ru.practicum.shoppinglist.presentation.auth.recovery

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PasswordRecoveryEffect.NavigateBack -> onNavigateBack()

                PasswordRecoveryEffect.RecoveryEmailSent -> {
                    snackbarHostState.showSnackbar(
                        context.getString(
                            R.string.password_recovery_success,
                        ),
                    )
                }

                is PasswordRecoveryEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        context.getString(effect.error.messageResource),
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
