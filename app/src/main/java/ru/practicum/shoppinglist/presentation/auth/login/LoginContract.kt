package ru.practicum.shoppinglist.presentation.auth.login

import ru.practicum.shoppinglist.domain.api.model.AuthError
import ru.practicum.shoppinglist.domain.validation.AuthValidator

internal data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isEmailEdited: Boolean = false,
    val isPasswordEdited: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
) {
    val isEmailValid: Boolean
        get() = AuthValidator.isEmailValid(email)

    val isPasswordValid: Boolean
        get() = AuthValidator.isPasswordValid(password)

    val showEmailError: Boolean
        get() = isEmailEdited && !isEmailValid

    val showPasswordError: Boolean
        get() = isPasswordEdited && !isPasswordValid

    val isLoginEnabled: Boolean
        get() = isEmailValid &&
                isPasswordValid &&
                !isLoading
}

internal sealed interface LoginIntent {

    data class EmailChanged(
        val email: String,
    ) : LoginIntent

    data class PasswordChanged(
        val password: String,
    ) : LoginIntent

    data object PasswordVisibilityClicked : LoginIntent

    data object LoginClicked : LoginIntent

    data object RegistrationClicked : LoginIntent

    data object PasswordRecoveryClicked : LoginIntent
}

internal sealed interface LoginEffect {

    data object NavigateToShoppingLists : LoginEffect

    data object NavigateToRegistration : LoginEffect

    data object NavigateToPasswordRecovery : LoginEffect

    data class ShowError(
        val error: AuthError,
    ) : LoginEffect
}
