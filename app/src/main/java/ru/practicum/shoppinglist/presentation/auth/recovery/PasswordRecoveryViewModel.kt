package ru.practicum.shoppinglist.presentation.auth.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.domain.api.model.AuthResult
import ru.practicum.shoppinglist.domain.api.repository.AuthRepository

internal class PasswordRecoveryViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(PasswordRecoveryUiState())
    val state = mutableState.asStateFlow()

    private val effectChannel =
        Channel<PasswordRecoveryEffect>(Channel.BUFFERED)

    val effects = effectChannel.receiveAsFlow()

    fun onIntent(intent: PasswordRecoveryIntent) {
        when (intent) {
            is PasswordRecoveryIntent.EmailChanged -> {
                updateEmail(intent.email)
            }

            PasswordRecoveryIntent.SubmitClicked -> recoverPassword()
            PasswordRecoveryIntent.BackClicked -> navigateBack()
        }
    }

    private fun updateEmail(email: String) {
        mutableState.update {
            it.copy(
                email = email,
                isEmailEdited = true,
            )
        }
    }

    private fun recoverPassword() {
        val currentState = mutableState.value

        if (!currentState.isSubmitEnabled) {
            return
        }

        mutableState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = authRepository.recoverPassword(
                currentState.email.trim(),
            )

            mutableState.update { it.copy(isLoading = false) }

            when (result) {
                AuthResult.Success -> {
                    effectChannel.send(
                        PasswordRecoveryEffect.RecoveryEmailSent,
                    )
                }

                is AuthResult.Failure -> {
                    effectChannel.send(
                        PasswordRecoveryEffect.ShowError(result.error),
                    )
                }
            }
        }
    }

    private fun navigateBack() {
        effectChannel.trySend(PasswordRecoveryEffect.NavigateBack)
    }
}
