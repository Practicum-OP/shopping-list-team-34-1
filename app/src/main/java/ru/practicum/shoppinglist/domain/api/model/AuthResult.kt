package ru.practicum.shoppinglist.domain.api.model

sealed interface AuthResult {

    data object Success : AuthResult

    data class Failure(
        val error: AuthError,
    ) : AuthResult
}

enum class AuthError {
    INVALID_DATA,
    USER_ALREADY_EXISTS,
    INVALID_CREDENTIALS,
    NETWORK_ERROR,
    SERVER_ERROR,
    UNKNOWN_ERROR,
}
