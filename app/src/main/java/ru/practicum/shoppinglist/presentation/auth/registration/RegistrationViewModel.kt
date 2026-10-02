package ru.practicum.shoppinglist.presentation.auth.registration

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

internal class RegistrationViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(RegistrationUiState())
    val state = mutableState.asStateFlow()

    private val effectChannel = Channel<RegistrationEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onIntent(intent: RegistrationIntent) {
        when (intent) {
            is RegistrationIntent.EmailChanged -> updateEmail(intent.email)
            is RegistrationIntent.PasswordChanged -> updatePassword(intent.password)
            is RegistrationIntent.RepeatedPasswordChanged -> {
                updateRepeatedPassword(intent.password)
            }

            RegistrationIntent.PasswordVisibilityClicked -> {
                togglePasswordVisibility()
            }

            RegistrationIntent.RepeatedPasswordVisibilityClicked -> {
                toggleRepeatedPasswordVisibility()
            }

            RegistrationIntent.RegistrationClicked -> register()
            RegistrationIntent.BackClicked -> navigateBack()
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

    private fun updatePassword(password: String) {
        mutableState.update {
            it.copy(
                password = password,
                isPasswordEdited = true,
            )
        }
    }

    private fun updateRepeatedPassword(password: String) {
        mutableState.update {
            it.copy(
                repeatedPassword = password,
                isRepeatedPasswordEdited = true,
            )
        }
    }

    private fun togglePasswordVisibility() {
        mutableState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    private fun toggleRepeatedPasswordVisibility() {
        mutableState.update {
            it.copy(
                isRepeatedPasswordVisible = !it.isRepeatedPasswordVisible,
            )
        }
    }

    private fun register() {
        val currentState = mutableState.value

        if (!currentState.isRegistrationEnabled) {
            return
        }

        mutableState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = authRepository.register(
                email = currentState.email.trim(),
                password = currentState.password,
            )

            mutableState.update { it.copy(isLoading = false) }

            when (result) {
                AuthResult.Success -> {
                    effectChannel.send(
                        RegistrationEffect.NavigateToShoppingLists,
                    )
                }

                is AuthResult.Failure -> {
                    effectChannel.send(
                        RegistrationEffect.ShowError(result.error),
                    )
                }
            }
        }
    }

    private fun navigateBack() {
        effectChannel.trySend(RegistrationEffect.NavigateBack)
    }
}
