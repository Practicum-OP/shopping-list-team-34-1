package ru.practicum.shoppinglist.domain.api.repository
import ru.practicum.shoppinglist.domain.api.model.AuthResult

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String,
    ): AuthResult

    suspend fun login(
        email: String,
        password: String,
    ): AuthResult

    suspend fun recoverPassword(
        email: String,
    ): AuthResult

    suspend fun restoreSession(): AuthResult

    suspend fun logout()
}
