package ru.practicum.shoppinglist.presentation.auth

import androidx.annotation.StringRes
import ru.practicum.shoppinglist.R
import ru.practicum.shoppinglist.domain.api.model.AuthError

@get:StringRes
internal val AuthError.messageResource: Int
    get() = when (this) {
        AuthError.INVALID_DATA -> R.string.auth_error_invalid_data
        AuthError.USER_ALREADY_EXISTS -> {
            R.string.auth_error_user_already_exists
        }

        AuthError.INVALID_CREDENTIALS -> {
            R.string.auth_error_invalid_credentials
        }

        AuthError.NETWORK_ERROR -> R.string.auth_error_network
        AuthError.SERVER_ERROR -> R.string.auth_error_server
        AuthError.UNKNOWN_ERROR -> R.string.auth_error_unknown
    }
