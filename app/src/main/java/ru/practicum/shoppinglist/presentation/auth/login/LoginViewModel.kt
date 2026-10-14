package ru.practicum.shoppinglist.presentation.auth.login

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

internal class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow(LoginUiState())
    val state = mutableState.asStateFlow()

    private val effectChannel = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> updateEmail(intent.email)
            is LoginIntent.PasswordChanged -> updatePassword(intent.password)
            LoginIntent.PasswordVisibilityClicked -> togglePasswordVisibility()
            LoginIntent.LoginClicked -> login()
            LoginIntent.RegistrationClicked -> {
                effectChannel.trySend(LoginEffect.NavigateToRegistration)
            }
            LoginIntent.PasswordRecoveryClicked -> {
                effectChannel.trySend(LoginEffect.NavigateToPasswordRecovery)
            }
        }
    }

    private fun updateEmail(email: String) {
        mutableState.update { state ->
            state.copy(
                email = email,
                isEmailEdited = true,
            )
        }
    }

    private fun updatePassword(password: String) {
        mutableState.update { state ->
            state.copy(
                password = password,
                isPasswordEdited = true,
            )
        }
    }

    private fun togglePasswordVisibility() {
        mutableState.update { state ->
            state.copy(
                isPasswordVisible = !state.isPasswordVisible,
            )
        }
    }

    private fun login() {
        val currentState = mutableState.value

        if (!currentState.isLoginEnabled) {
            return
        }

        mutableState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            val result = authRepository.login(
                email = currentState.email.trim(),
                password = currentState.password,
            )

            mutableState.update { state ->
                state.copy(isLoading = false)
            }

            when (result) {
                AuthResult.Success -> {
                    effectChannel.send(
                        LoginEffect.NavigateToShoppingLists,
                    )
                }

                is AuthResult.Failure -> {
                    effectChannel.send(
                        LoginEffect.ShowError(result.error),
                    )
                }
            }
        }
    }
}
