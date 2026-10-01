package ru.practicum.shoppinglist.data.mapper

import ru.practicum.shoppinglist.domain.api.model.AuthError


internal object AuthErrorMapper {

    fun registration(responseCode: Int): AuthError {
        return when (responseCode) {
            BAD_REQUEST -> AuthError.INVALID_DATA
            CONFLICT -> AuthError.USER_ALREADY_EXISTS
            else -> common(responseCode)
        }
    }

    fun login(responseCode: Int): AuthError {
        return when (responseCode) {
            BAD_REQUEST -> AuthError.INVALID_DATA
            UNAUTHORIZED -> AuthError.INVALID_CREDENTIALS
            else -> common(responseCode)
        }
    }

    fun recovery(responseCode: Int): AuthError {
        return when (responseCode) {
            BAD_REQUEST -> AuthError.INVALID_DATA
            else -> common(responseCode)
        }
    }

    fun refresh(responseCode: Int): AuthError {
        return when (responseCode) {
            BAD_REQUEST,
            UNAUTHORIZED,
                -> AuthError.INVALID_CREDENTIALS

            else -> common(responseCode)
        }
    }

    fun common(responseCode: Int): AuthError {
        return if (responseCode >= SERVER_ERROR_START) {
            AuthError.SERVER_ERROR
        } else {
            AuthError.UNKNOWN_ERROR
        }
    }

    private const val BAD_REQUEST = 400
    private const val UNAUTHORIZED = 401
    private const val CONFLICT = 409
    private const val SERVER_ERROR_START = 500
}
