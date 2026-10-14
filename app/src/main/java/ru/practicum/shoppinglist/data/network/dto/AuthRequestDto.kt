package ru.practicum.shoppinglist.data.network.dto

import com.google.gson.annotations.SerializedName

internal data class AuthRequestDto(
    val email: String,
    val password: String,
)

internal data class AuthResponseDto(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user_id")
    val userId: Long,
)

internal data class RefreshTokenRequestDto(
    @SerializedName("refresh_token")
    val refreshToken: String,
)

internal data class RefreshTokenResponseDto(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
)

internal data class TokenCheckResponseDto(
    @SerializedName("is_valid")
    val isValid: Boolean,
)
