package ru.practicum.shoppinglist.data.network.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import ru.practicum.shoppinglist.data.network.dto.AuthRequestDto
import ru.practicum.shoppinglist.data.network.dto.AuthResponseDto
import ru.practicum.shoppinglist.data.network.dto.RefreshTokenRequestDto
import ru.practicum.shoppinglist.data.network.dto.RefreshTokenResponseDto
import ru.practicum.shoppinglist.data.network.dto.TokenCheckResponseDto

internal interface AuthApi {

    @POST("auth/registration")
    suspend fun register(
        @Body request: AuthRequestDto,
    ): Response<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(
        @Body request: AuthRequestDto,
    ): Response<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequestDto,
    ): Response<RefreshTokenResponseDto>

    @GET("auth/check")
    suspend fun checkToken(
        @Header("Authorization") authorization: String,
    ): Response<TokenCheckResponseDto>

    @POST("auth/recovery")
    suspend fun recoverPassword(
        @Header("email") email: String,
    ): Response<Unit>
}
