package ru.practicum.shoppinglist.presentation.auth.check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.practicum.shoppinglist.domain.api.model.AuthResult
import ru.practicum.shoppinglist.domain.api.repository.AuthRepository

internal class AuthCheckViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val effectChannel =
        Channel<AuthCheckEffect>(Channel.BUFFERED)

    val effects = effectChannel.receiveAsFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val effect = when (authRepository.restoreSession()) {
                AuthResult.Success -> {
                    AuthCheckEffect.NavigateToShoppingLists
                }

                is AuthResult.Failure -> {
                    AuthCheckEffect.NavigateToLogin
                }
            }

            effectChannel.send(effect)
        }
    }
}

internal sealed interface AuthCheckEffect {
    data object NavigateToLogin : AuthCheckEffect
    data object NavigateToShoppingLists : AuthCheckEffect
}
