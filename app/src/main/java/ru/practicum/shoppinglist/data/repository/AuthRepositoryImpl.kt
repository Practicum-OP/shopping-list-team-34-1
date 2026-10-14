package ru.practicum.shoppinglist.data.repository

import java.io.IOException
import retrofit2.Response
import ru.practicum.shoppinglist.data.local.storage.AuthTokenStorage
import ru.practicum.shoppinglist.data.local.storage.StoredAuthSession
import ru.practicum.shoppinglist.data.mapper.AuthErrorMapper
import ru.practicum.shoppinglist.data.network.api.AuthApi
import ru.practicum.shoppinglist.data.network.dto.AuthRequestDto
import ru.practicum.shoppinglist.data.network.dto.AuthResponseDto
import ru.practicum.shoppinglist.data.network.dto.RefreshTokenRequestDto
import ru.practicum.shoppinglist.domain.api.repository.AuthRepository
import ru.practicum.shoppinglist.domain.api.model.AuthError
import ru.practicum.shoppinglist.domain.api.model.AuthResult
import ru.practicum.shoppinglist.data.network.dto.RefreshTokenResponseDto

internal class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: AuthTokenStorage,
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
    ): AuthResult {
        return authenticate(
            request = {
                authApi.register(
                    AuthRequestDto(
                        email = email,
                        password = password,
                    ),
                )
            },
            errorMapper = AuthErrorMapper::registration,
        )
    }

    override suspend fun login(
        email: String,
        password: String,
    ): AuthResult {
        return authenticate(
            request = {
                authApi.login(
                    AuthRequestDto(
                        email = email,
                        password = password,
                    ),
                )
            },
            errorMapper = AuthErrorMapper::login,
        )
    }

    override suspend fun recoverPassword(email: String): AuthResult {
        return try {
            val response = authApi.recoverPassword(email)

            if (response.isSuccessful) {
                AuthResult.Success
            } else {
                AuthResult.Failure(
                    error = AuthErrorMapper.recovery(response.code()),
                )
            }
        } catch (_: IOException) {
            AuthResult.Failure(AuthError.NETWORK_ERROR)
        }
    }

    override suspend fun restoreSession(): AuthResult {
        val session = tokenStorage.getSession()
            ?: return AuthResult.Failure(AuthError.INVALID_CREDENTIALS)

        return try {
            val response = authApi.checkToken(
                authorization = "Bearer ${session.accessToken}",
            )

            if (response.isSuccessful && response.body()?.isValid == true) {
                AuthResult.Success
            } else if (response.isSuccessful) {
                refreshSession(session.refreshToken)
            } else {
                AuthResult.Failure(
                    AuthErrorMapper.common(response.code()),
                )
            }
        } catch (_: IOException) {
            AuthResult.Failure(AuthError.NETWORK_ERROR)
        }
    }

    override suspend fun logout() {
        tokenStorage.clearSession()
    }

    private suspend fun authenticate(
        request: suspend () -> Response<AuthResponseDto>,
        errorMapper: (Int) -> AuthError,
    ): AuthResult {
        return try {
            val response = request()

            if (response.isSuccessful) {
                saveAuthSession(response.body())
            } else {
                AuthResult.Failure(
                    error = errorMapper(response.code()),
                )
            }
        } catch (_: IOException) {
            AuthResult.Failure(AuthError.NETWORK_ERROR)
        }
    }

    private suspend fun refreshSession(
        refreshToken: String,
    ): AuthResult {
        val response = authApi.refreshToken(
            RefreshTokenRequestDto(refreshToken),
        )

        return if (response.isSuccessful) {
            updateSessionTokens(response.body())
        } else {
            handleRefreshFailure(response.code())
        }
    }

    private suspend fun handleRefreshFailure(
        responseCode: Int,
    ): AuthResult {
        val error = AuthErrorMapper.refresh(responseCode)

        if (error == AuthError.INVALID_CREDENTIALS) {
            tokenStorage.clearSession()
        }

        return AuthResult.Failure(error)
    }

    private suspend fun saveAuthSession(
        response: AuthResponseDto?,
    ): AuthResult {
        return if (response != null) {
            tokenStorage.saveSession(
                StoredAuthSession(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    userId = response.userId,
                ),
            )
            AuthResult.Success
        } else {
            AuthResult.Failure(AuthError.SERVER_ERROR)
        }
    }

    private suspend fun updateSessionTokens(
        response: RefreshTokenResponseDto?,
    ): AuthResult {
        return if (response != null) {
            tokenStorage.updateTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
            )
            AuthResult.Success
        } else {
            AuthResult.Failure(AuthError.SERVER_ERROR)
        }
    }
}
