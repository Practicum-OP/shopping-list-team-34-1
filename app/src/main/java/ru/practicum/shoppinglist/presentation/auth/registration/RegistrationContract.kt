package ru.practicum.shoppinglist.presentation.auth.registration

import ru.practicum.shoppinglist.domain.api.model.AuthError
import ru.practicum.shoppinglist.domain.validation.AuthValidator

internal data class RegistrationUiState(
    val email: String = "",
    val password: String = "",
    val repeatedPassword: String = "",
    val isEmailEdited: Boolean = false,
    val isPasswordEdited: Boolean = false,
    val isRepeatedPasswordEdited: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isRepeatedPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
) {
    val showEmailError: Boolean
        get() = isEmailEdited && !AuthValidator.isEmailValid(email)

    val showPasswordError: Boolean
        get() = isPasswordEdited && !AuthValidator.isPasswordValid(password)

    val showRepeatedPasswordError: Boolean
        get() = isRepeatedPasswordEdited &&
                !AuthValidator.doPasswordsMatch(password, repeatedPassword)

    val isRegistrationEnabled: Boolean
        get() = AuthValidator.isEmailValid(email) &&
                AuthValidator.isPasswordValid(password) &&
                AuthValidator.doPasswordsMatch(password, repeatedPassword) &&
                !isLoading
}

internal sealed interface RegistrationIntent {
    data class EmailChanged(val email: String) : RegistrationIntent
    data class PasswordChanged(val password: String) : RegistrationIntent

    data class RepeatedPasswordChanged(
        val password: String,
    ) : RegistrationIntent

    data object PasswordVisibilityClicked : RegistrationIntent
    data object RepeatedPasswordVisibilityClicked : RegistrationIntent
    data object RegistrationClicked : RegistrationIntent
    data object BackClicked : RegistrationIntent
}

internal sealed interface RegistrationEffect {
    data object NavigateBack : RegistrationEffect
    data object NavigateToShoppingLists : RegistrationEffect
    data class ShowError(val error: AuthError) : RegistrationEffect
}
