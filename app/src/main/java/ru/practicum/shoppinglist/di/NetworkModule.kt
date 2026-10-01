package ru.practicum.shoppinglist.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.shoppinglist.BuildConfig
import ru.practicum.shoppinglist.data.network.api.AuthApi
import org.koin.android.ext.koin.androidContext
import ru.practicum.shoppinglist.data.local.storage.AuthTokenStorage
import ru.practicum.shoppinglist.data.local.storage.DataStoreAuthTokenStorage
import ru.practicum.shoppinglist.data.repository.AuthRepositoryImpl
import ru.practicum.shoppinglist.domain.api.repository.AuthRepository
import org.koin.core.module.dsl.viewModelOf
import ru.practicum.shoppinglist.presentation.auth.check.AuthCheckViewModel
import ru.practicum.shoppinglist.presentation.auth.login.LoginViewModel
import ru.practicum.shoppinglist.presentation.auth.recovery.PasswordRecoveryViewModel
import ru.practicum.shoppinglist.presentation.auth.registration.RegistrationViewModel

private const val BASE_URL =
    "https://faiwlkhyssrgofauzegs.supabase.co/functions/v1/"

internal val networkModule = module {

    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<AuthApi> {
        get<Retrofit>().create(AuthApi::class.java)
    }

    single<AuthTokenStorage> {
        DataStoreAuthTokenStorage(
            context = androidContext(),
        )
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            tokenStorage = get(),
        )
    }

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
    viewModelOf(::PasswordRecoveryViewModel)
    viewModelOf(::AuthCheckViewModel)

}
