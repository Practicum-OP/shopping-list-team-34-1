package ru.practicum.shoppinglist.data.local.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

private const val AUTH_DATA_STORE_NAME = "auth_session"

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AUTH_DATA_STORE_NAME,
)

internal data class StoredAuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
)

internal interface AuthTokenStorage {

    suspend fun saveSession(session: StoredAuthSession)

    suspend fun getSession(): StoredAuthSession?

    suspend fun updateTokens(
        accessToken: String,
        refreshToken: String,
    )

    suspend fun clearSession()
}

internal class DataStoreAuthTokenStorage(
    private val context: Context,
) : AuthTokenStorage {

    override suspend fun saveSession(session: StoredAuthSession) {
        context.authDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = session.accessToken
            preferences[REFRESH_TOKEN_KEY] = session.refreshToken
            preferences[USER_ID_KEY] = session.userId
        }
    }

    override suspend fun getSession(): StoredAuthSession? {
        val preferences = context.authDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .first()

        val accessToken = preferences[ACCESS_TOKEN_KEY]
        val refreshToken = preferences[REFRESH_TOKEN_KEY]
        val userId = preferences[USER_ID_KEY]

        return if (
            accessToken != null &&
            refreshToken != null &&
            userId != null
        ) {
            StoredAuthSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = userId,
            )
        } else {
            null
        }
    }

    override suspend fun updateTokens(
        accessToken: String,
        refreshToken: String,
    ) {
        context.authDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    override suspend fun clearSession() {
        context.authDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
    }
}
