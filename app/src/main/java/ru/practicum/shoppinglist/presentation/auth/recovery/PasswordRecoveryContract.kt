package ru.practicum.shoppinglist.presentation.auth.recovery

import ru.practicum.shoppinglist.domain.api.model.AuthError
import ru.practicum.shoppinglist.domain.validation.AuthValidator

internal data class PasswordRecoveryUiState(
    val email: String = "",
    val isEmailEdited: Boolean = false,
    val isLoading: Boolean = false,
) {
    val showEmailError: Boolean
        get() = isEmailEdited && !AuthValidator.isEmailValid(email)

    val isSubmitEnabled: Boolean
        get() = AuthValidator.isEmailValid(email) && !isLoading
}

internal sealed interface PasswordRecoveryIntent {
    data class EmailChanged(val email: String) : PasswordRecoveryIntent
    data object SubmitClicked : PasswordRecoveryIntent
    data object BackClicked : PasswordRecoveryIntent
}

internal sealed interface PasswordRecoveryEffect {
    data object NavigateBack : PasswordRecoveryEffect
    data object RecoveryEmailSent : PasswordRecoveryEffect
    data class ShowError(val error: AuthError) : PasswordRecoveryEffect
}
